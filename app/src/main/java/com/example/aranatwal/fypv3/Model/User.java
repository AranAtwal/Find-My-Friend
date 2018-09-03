package com.example.aranatwal.fypv3.Model;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.PendingIntent.getActivity;

public class User implements Serializable {

    public String firstname, lastname, email, uid;

    public ArrayList<String> friendRequestsSent, friendRequestsReceived, friends;

    HashMap<String, Object> result = new HashMap<>();

    public User() {

    }

    public User(String firstname, String lastname, String email) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        friendRequestsSent = new ArrayList<String>();
        friendRequestsReceived = new ArrayList<String>();
        friends = new ArrayList<String>();

    }

    public User(String firstname, String lastname, String email, String uid) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.uid = uid;
    }
    //different constructor creating a new user and assembling a user from data in the database

    public Map<String, Object> toMap() {//allows user details to be put in correct fields
        result.put("firstname", firstname);
        result.put("lastname", lastname);
        result.put("email", email);
        result.put("friendRequestsSent", friendRequestsSent );
        result.put("friendRequestsReceived", friendRequestsReceived);
        result.put("friends", friends);

        return result;
    }
    //getters and setters

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<String> getFriendRequestsSent() {
        return friendRequestsSent;
    }

    public void setFriendRequestsSent(ArrayList<String> friendRequestsSent) {
        this.friendRequestsSent = friendRequestsSent;
    }

    public ArrayList<String> getFriendRequestsReceived() {
        return friendRequestsReceived;
    }

    public void setFriendRequestsReceived(ArrayList<String> friendRequestsReceived) {
        this.friendRequestsReceived = friendRequestsReceived;
    }

    public String getUid() {
        return uid;
    }


}
