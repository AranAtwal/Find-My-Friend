package com.example.aranatwal.fypv3;

import com.example.aranatwal.fypv3.Model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class FriendRequestData {

    private static FriendRequestData instance;
    public final ArrayList<User> requests;

    private FirebaseAuth mAuth;
    private String userID;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    final CollectionReference mUserRef = db.collection("Users");

    private FriendRequestData() {
        requests = new ArrayList<User>();

        createData();
    }

    public static FriendRequestData getInstance() {
        if (instance == null) {
            instance = new FriendRequestData();
        }
        return instance;
    }

    private void createData() {
        // create the list of holidays (code not shown)
    }

    public ArrayList<User> getRequests() {

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();



        mUserRef.document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Object friendRequestReceived = documentSnapshot.get("friendRequestsReceived");

                ArrayList<String> FriendRequests;

                FriendRequests = (ArrayList<String>) friendRequestReceived;
                int size = FriendRequests.size();
                if (requests.size()<size) {
                    for (final String friendRequest: FriendRequests) {
                        mUserRef.document(friendRequest).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                String first = documentSnapshot.get("firstname").toString();
                                String last = documentSnapshot.get("lastname").toString();
                                String email = documentSnapshot.get("email").toString();

                                User u = new User(first, last, email, friendRequest);

                                requests.add(u); //adds user details to a list which is to be passes into the recyclerview list adapter
                                //user details pulled so that the app can display firstname + lastname to users instead of ids

                            }
                        });

                    }
                }


            }
        });



        return requests;
    }

    public User getFriendRequest(int  num) {
        return requests.get(num-1);
    }

    public void acceptFriendRequest(ArrayList<User> fRequests, int user) {
        final String acceptedID = fRequests.get(user).uid;


        Task<DocumentSnapshot> usr = mUserRef.document(userID).get();
        usr.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Object friendsAdded = documentSnapshot.get("friends");

                System.out.print(friendsAdded);
                ArrayList<String> friends;
                if (friendsAdded == null) {
                    friends = new ArrayList<String>();
                }
                friends = (ArrayList<String>) friendsAdded;

                friends.add(acceptedID); //adds id to list

                HashMap<String, Object> friendRequest = new HashMap<>();

                friendRequest.put("friends", friends);

                mUserRef.document(userID).update(friendRequest);

            }

        });

        Task<DocumentSnapshot> frnd = mUserRef.document(acceptedID).get();
        frnd.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Object friendsAdded = documentSnapshot.get("friends");

                System.out.print(friendsAdded);
                ArrayList<String> friends;
                if (friendsAdded == null) {
                    friends = new ArrayList<String>();
                }
                friends = (ArrayList<String>) friendsAdded;

                friends.add(userID);

                HashMap<String, Object> friendRequest = new HashMap<>();

                friendRequest.put("friends", friends);//adds id to other user so they both have each other added

                mUserRef.document(acceptedID).update(friendRequest);

            }

        });

        removeRequest(userID, acceptedID); //received and sent requests need to be removed as the request has been handled

    }

    public void declineFriendRequest(ArrayList<User> fRequests, int user) {
        String currentUser = userID;
        String declinedID = fRequests.get(user).uid;

        removeRequest(currentUser, declinedID); //removing users from received and sent when declined

    }


    private void removeRequest(final String receiver, final String sender) {//searches both users and removes the request as it has been handled to avoid user repeatedly accepting or declining request

        Task<DocumentSnapshot> usr = mUserRef.document(receiver).get();
        usr.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Object friendsAdded = documentSnapshot.get("friendRequestsReceived");

                System.out.print(friendsAdded);
                ArrayList<String> friendRequests;
                if (friendsAdded == null) {
                    friendRequests = new ArrayList<String>();
                }
                friendRequests = (ArrayList<String>) friendsAdded;
                if (friendRequests.contains(sender)) {
                    friendRequests.remove(sender);
                }

                HashMap<String, Object> friendRequest = new HashMap<>();

                friendRequest.put("friendRequestsReceived", friendRequests);

                mUserRef.document(receiver).update(friendRequest);

            }

        });


        Task<DocumentSnapshot> usr2 = mUserRef.document(sender).get();
        usr2.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Object friendsAdded = documentSnapshot.get("friendRequestsSent");

                System.out.print(friendsAdded);
                ArrayList<String> friendRequests;
                if (friendsAdded == null) {
                    friendRequests = new ArrayList<String>();
                }
                friendRequests = (ArrayList<String>) friendsAdded;
                if (friendRequests.contains(receiver)) {
                    friendRequests.remove(receiver);
                }

                HashMap<String, Object> friendRequest = new HashMap<>();

                friendRequest.put("friendRequestsSent", friendRequests);

                mUserRef.document(sender).update(friendRequest);

            }

        });

    }


}