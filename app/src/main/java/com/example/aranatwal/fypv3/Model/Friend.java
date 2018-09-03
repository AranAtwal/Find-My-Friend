package com.example.aranatwal.fypv3.Model;

import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class Friend {

    private String id;
    public static ArrayList<String> friends;
    public static HashMap<String, User> friendDetails;

    public static int counter = 0;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static HashMap<String, LatLng> friendLocations;

    private static Friend instance;
    private FirebaseAuth mAuth;

    public boolean friendsFound = false;
    public boolean friendsDetailsFound = false;
    public boolean friendsLocationsFound = false;
    //checks to see if all details are available to avoid syncing up missing data



    public Friend() {

        mAuth = FirebaseAuth.getInstance();
        id = mAuth.getCurrentUser().getUid();

        friends = new ArrayList<String>();
        friendLocations = new HashMap<String, LatLng>();
        friendDetails = new HashMap<String, User>();
        getFriends();
        getFriendsLocations();
        //calling methods to find friends and their locations
    }

    public static Friend getInstance() {
        if (instance == null) {
            instance = new Friend();
        }
        return  instance;
    }

    public void getFriends() {

        final DocumentReference userRef = db.collection("Users").document(id);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isComplete()) {

                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        Log.d(TAG, document.get("friends").toString());


                        friends = (ArrayList<String>) document.get("friends");

                        counter=counter+1;
                        //incrementing static counter to show number of friends on map screen

                        friendsFound = true;

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }


            }
        });

    }


    public void getFriendsLocations(){

        if(friends== null) {
            getFriends();
        }
        //needs string keys before trying to find locations

        if(!friendLocations.isEmpty()) {
            Log.d("notEMPTY", friendLocations.toString());
            friendLocations.clear();
            Log.d("OCCURRED", friendLocations.toString());
        }

        DatabaseReference friendsSharing = FirebaseDatabase.getInstance().getReference("friendsSharing");

        GeoFire geoFire = new GeoFire(friendsSharing);

        for (String friend:
             friends) {

            geoFire.getLocation(friend, new LocationCallback() {

                @Override
                public void onLocationResult(String key, GeoLocation location) {

                    if (location!=null) {//checks to see if that friend has a location, if they dont a null pointer will be thrown
                        double dlat = location.latitude;
                        double dlon = location.longitude;
                        LatLng latLng = new LatLng(dlat, dlon);
                        Log.d(key, latLng.toString());

                        friendLocations.put(key, latLng);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        counter=counter+1;
        friendsLocationsFound = true;
    }

    public void getFriendDetails() {

        if(friends== null) {
            getFriends();
        }
        final CollectionReference friendRef = db.collection("Users");

        for (final String friend:
             friends) {

            friendRef.document(friend).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isComplete()) {


                            String f = task.getResult().get("firstname").toString();
                            String l = task.getResult().get("lastname").toString();
                            String e = task.getResult().get("email").toString();
                            String i = friend;

                            User u = new User(f,l,e,i);

                            friendDetails.put(friend, u);

                            //rebuilds user using data from database
                        //User is a much easier format to hold the data
                    }


                }
            });

        }

        counter=counter+1;
        friendsDetailsFound = true;

    }

    public HashMap<String, LatLng> getFriendLocations() {
        if (friendLocations==null) {
            getFriendsLocations();
        }

        return friendLocations;
    }

    public HashMap<String, User> returnFriendDetails() {
        if (friendLocations==null) {
            getFriendDetails();
        }
        return friendDetails;
    }

    public int getCounter () {
        return counter;
    }

}
