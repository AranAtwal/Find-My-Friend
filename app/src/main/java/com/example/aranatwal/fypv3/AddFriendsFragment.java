package com.example.aranatwal.fypv3;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddFriendsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddFriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddFriendsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    private static final String TAG = "docs";
    private FirebaseAuth mAuth;
    private String userID;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public ArrayList<String> requests;
    int i;
    private ActionBar supportActionBar;


    public AddFriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddFriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddFriendsFragment newInstance(String param1, String param2) {
        AddFriendsFragment fragment = new AddFriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_add_friends, container, false);

        final Button mSearchFriend = (Button) view.findViewById(R.id.searchFriend); //the button for onclick action
        final EditText mFriendEmail = (EditText) view.findViewById(R.id.friendEmail); //the edit text used to enter the email

        mSearchFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String search_user = mFriendEmail.getText().toString();//to be used to search the array
                mAuth = FirebaseAuth.getInstance();
                userID = mAuth.getCurrentUser().getUid();

                final CollectionReference mUserRef = db.collection("Users"); //database reference to the right tree

                if (TextUtils.isEmpty(search_user)) {//conditions checked
                    mFriendEmail.setError("An Email is required");//make sure the text field is not blank
                }
                else {
                    mUserRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            boolean foundUser = false;

                            for (DocumentSnapshot document : task.getResult()) {//check against db for users
                                Log.d(TAG, document.getId() + " => " + document.getData());


                                if (document.getData().get("email").equals(search_user) && document.exists()) {//does the email match
                                    foundUser = true;
                                    performChecks(userID, document.getId());//pass the current users id and the searched user to perform more checks

                                } else if (!document.getData().get("email").equals(search_user) && document.exists() && foundUser != true) {
                                    foundUser = false;
                                }

                            }

                            if (!foundUser) {
                                Toast.makeText(getContext(), "User could not be found", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());

                        }
                        }
                    });

                }

            }
        });



        return view;
    }

    private void performChecks(String currentUserID, String searchedUserId) {

        i = 0;

        //check if user searched is the current user

        isCurrentUser(currentUserID, searchedUserId);

        //check if already a friend


        //check if sent a request already


        //send friend request

    }

    public void isCurrentUser(String currentUserId, String searchedUserId) {//cannot search for yourself

        Log.i("current", currentUserId);
        Log.i("searched", searchedUserId);


        if(searchedUserId.equals(currentUserId)) {
            Toast.makeText(getContext(), "Cannot search for your own user account", Toast.LENGTH_LONG).show();
        } else {
            i++;
            Log.d("I", Integer.toString(i));

            //run next check
            checkIfFriendExists(currentUserId, searchedUserId);

        }



    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void sendFriendRequest(String id) {
        final String finalId = id;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String currentUserId = currentUser.getUid();

        final CollectionReference collection = db.collection("Users");
        Task<DocumentSnapshot> usr = collection.document(currentUserId).get();

        while (!usr.isComplete()) {

        }

                Object friendRequestSent = usr.getResult().get("friendRequestsSent");

                System.out.print(friendRequestSent);
                ArrayList<String> localFriendRequests;
                if (friendRequestSent == null) {
                    localFriendRequests = new ArrayList<String>();
                }
                localFriendRequests = (ArrayList<String>) friendRequestSent;

                localFriendRequests.add(finalId);

                HashMap<String, Object> friendRequest = new HashMap<>();

                friendRequest.put("friendRequestsSent", localFriendRequests);//adds updated arraylist and then adds to document

                collection.document(currentUserId).update(friendRequest);




        Task<DocumentSnapshot> targetusr = collection.document(finalId).get();

        while (!targetusr.isComplete()) {

        }

//                sending request to user
                Object friendRequestReceived = targetusr.getResult().get("friendRequestsReceived");

                ArrayList<String> localFriendRequestsReceived;
                if (friendRequestReceived == null) {
                    localFriendRequestsReceived = new ArrayList<String>();
                }
                localFriendRequestsReceived = (ArrayList<String>) friendRequestReceived;

                localFriendRequestsReceived.add(currentUserId);

                HashMap<String, Object> userObject = new HashMap<>();

                userObject.put("friendRequestsReceived", localFriendRequestsReceived);//does the same for the other user

                collection.document(finalId).update(userObject);


    }


    public void checkIfRequestSent(final String currentUserID, final String searchedUserID){//checks to see if a request has already been sent
        //avoids spam of target user with friend requests

        CollectionReference collection3 = db.collection("Users");
        Task<DocumentSnapshot> currentUsr = collection3.document(currentUserID).get();

        while (!currentUsr.isComplete()) {

        }
        DocumentSnapshot d = currentUsr.getResult();


            Object friendRequestSent = d.get("friendRequestsSent");

            System.out.print(friendRequestSent);
            ArrayList<String> localFriendRequests;
            if (friendRequestSent == null) {
                localFriendRequests = new ArrayList<String>();
            }
            localFriendRequests = (ArrayList<String>) friendRequestSent;

            if (!localFriendRequests.contains(searchedUserID)) {
                i++;

                checkIfRequestReceived(currentUserID, searchedUserID);



            } else {
                Toast.makeText(getContext(), "A Friend Request has already been sent", Toast.LENGTH_LONG).show();
            }


    }


    public void checkIfRequestReceived(final String currentUserID, final String searchedUserID) {//the same but for the other user to see if data is lined up


        CollectionReference collection3 = db.collection("Users");
        Task<DocumentSnapshot> currentUsr = collection3.document(currentUserID).get();

        while (!currentUsr.isComplete()) {

        }
        DocumentSnapshot d = currentUsr.getResult();

        Object friendRequestSent = d.get("friendRequestsReceived");

        System.out.print(friendRequestSent);
        ArrayList<String> localFriendRequests;
        if (friendRequestSent == null) {
            localFriendRequests = new ArrayList<String>();
        }
        localFriendRequests = (ArrayList<String>) friendRequestSent;

        if (!localFriendRequests.contains(searchedUserID)) {
            i++;

            sendFriendRequest(searchedUserID);


        } else {
            Toast.makeText(getContext(), "A Friend Request has already been sent", Toast.LENGTH_LONG).show();
        }

    }



    public void checkIfFriendExists(final String currentUserID, final String searchedUserID){//the user should not be able to search for a user who they already have as a friend
        CollectionReference collection3 = db.collection("Users");
        Task<DocumentSnapshot> currentUsr = collection3.document(currentUserID).get();

        while (!currentUsr.isComplete()) {

        }
        DocumentSnapshot d = currentUsr.getResult();

        Object currentFriends = d.get("friends");

        System.out.print(currentFriends);
        ArrayList<String> currentFriendsFound;
        if (currentFriends == null) {
            currentFriendsFound = new ArrayList<String>();
        }
        currentFriendsFound = (ArrayList<String>) currentFriends;

        if (currentFriendsFound.contains(searchedUserID)) {

        } else {
            i++;
        }


        Task<DocumentSnapshot> targetUsr = collection3.document(searchedUserID).get();
        while (!targetUsr.isComplete()) {

        }
        DocumentSnapshot t = targetUsr.getResult();

        Object searchedFriends = t.get("friends");

        System.out.print(searchedFriends);
        ArrayList<String> searchedFriendsFound;
        if (searchedFriends == null) {
            searchedFriendsFound = new ArrayList<String>();
        }
        searchedFriendsFound = (ArrayList<String>) searchedFriends;

        if(searchedFriendsFound.contains(currentUserID)) {

            Toast.makeText(getContext(), "User already added", Toast.LENGTH_LONG).show();

        } else {
            i++;

            checkIfRequestSent(currentUserID, searchedUserID);
        }

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public ActionBar getSupportActionBar() {
        return supportActionBar;
    }

    public void setSupportActionBar(ActionBar supportActionBar) {
        this.supportActionBar = supportActionBar;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}