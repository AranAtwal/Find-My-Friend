package com.example.aranatwal.fypv3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aranatwal.fypv3.Model.Friend;
import com.example.aranatwal.fypv3.Model.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class UserMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    //location variables
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    SupportMapFragment mapFragment;

    FirebaseAuth mAuth;
    User currentUser;

    //bottom information
    private TextView mUpdate_time, mUpdate_latitude, mUpdate_longitude, mFriend_Info;
    private ImageButton mRefreshMap, mLocateFriends, mNavigateHalfway, mEmailFriend;
    private String userID;

    //friend data
    public Friend friend = Friend.getInstance();
    public LatLng userLatLng;
    private ArrayList<String> frnds;
    private HashMap<String, User> frnddtls;
    private HashMap<String, LatLng> frndslocs;

    private String value;

    //display and marker data
    final int LOCATION_REQUEST_CODE = 1;
    Display display;
    private int display_width;
    private int display_height;
    private Point size;
    private boolean markerAdded;

    //halfway variables
    public static boolean isSharingLocation = true;
    private Place halfwayPoint;
    private String halfwayKey;
    private String lastFriendHalfway;
    private Menu mOptionsMenu;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_map);

        currentUser = new User();
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        //initialising buttons
        mRefreshMap = (ImageButton) findViewById(R.id.refreshMap);
        mLocateFriends = (ImageButton) findViewById(R.id.locateFriends);
        mNavigateHalfway = (ImageButton) findViewById(R.id.navigateHalfway);
        mEmailFriend = (ImageButton) findViewById(R.id.emaiFriend);

        frnds = new ArrayList<String>();
        frnddtls = new HashMap<String, User>();
        frndslocs = new HashMap<String, LatLng>();

        frnds.clear();
        frnddtls.clear();
        frndslocs.clear();

        frndslocs = friend.getFriendLocations();
        frnddtls = friend.returnFriendDetails();

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        display_width = size.x;
        display_height = size.y;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //checks permissions for location
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(UserMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }else{
            mapFragment.getMapAsync(this);//sets up map
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//sets top bar menu
        MenuInflater inflater = getMenuInflater();
        mOptionsMenu = menu;

        inflater.inflate(R.menu.user_map_menu, mOptionsMenu);

        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {//deals with menu items being clicked

        switch (item.getItemId()) {

            case R.id.action_friends://sends user to friend activity

                if(frnddtls != null) {
                    Intent intent = new Intent(getApplicationContext(), FriendsTabbedActivity.class);
                    startActivityForResult(intent, LOCATION_REQUEST_CODE);
                }
                return true;

            case R.id.action_closest_friend://checks if sharing location and calls method

                if (frndslocs != null) {

                    if(isSharingLocation) {
                        findNearestFriend();
                    } else {
                        Toast.makeText(getApplicationContext(), "You must enable Share Location", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No friends found", Toast.LENGTH_SHORT).show();
                }

                return true;

            case R.id.action_toggle_sharing://check status and changes it to the other option in the toggle
                isSharingLocation = !item.isChecked();
                item.setChecked(isSharingLocation);

                if(!isSharingLocation) {
                    stopSharing();
                    if (ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(UserMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

                    }
                    mMap.setMyLocationEnabled(false);
                    userLatLng = null;
                    stopSharing();


                    Toast.makeText(getApplicationContext(), "Location Sharing Disabled", Toast.LENGTH_SHORT).show();
                } else {
                    mMap.setMyLocationEnabled(true);
                    LatLng tempLL = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    userLatLng = tempLL;
                    Toast.makeText(getApplicationContext(), "Location Sharing Enabled", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.log_out:

                //stopSharing();
                //this has been commented out for demonstration purposes
                //usually a users last location would be removed from the database using this method to avoid other users having access to this information
                //this must be commented out as multiple devices would be required in order to demonstrate the application in the open and closed demos



                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();

                FirebaseAuth.getInstance().signOut();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {//sets all settings for the map
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        //tailoring map to desire
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(final Location location) {

        mLastLocation = location;

        friend.getFriendsLocations();//updates friends locations
        friend.getFriendDetails();
        userLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        updateLocationDB();
        updateStats();//calls to update stats to latLng is up to date at the bottom of the screen
    }

    public void updateLocationDB() {//updates location in firebase realtime database

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();//gets current user logged in
        DatabaseReference friendsSharing = FirebaseDatabase.getInstance().getReference("friendsSharing");

        GeoFire geoFire = new GeoFire(friendsSharing);
        geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        }

    }

    public void updateStats() {

        mUpdate_latitude = findViewById(R.id.current_latitude);
        mUpdate_longitude = findViewById(R.id.current_longitude);
        mUpdate_time = findViewById(R.id.time_of_last_update);
        mFriend_Info = findViewById(R.id.friends_found_info);

        String last_lat = mLastLocation.convert(mLastLocation.getLatitude(), Location.FORMAT_DEGREES);
        mUpdate_latitude.setText(last_lat);
        String last_long = mLastLocation.convert(mLastLocation.getLongitude(), Location.FORMAT_DEGREES);
        mUpdate_longitude.setText(last_long);

        Date d=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss");
        String currentDateTimeString = sdf.format(d);
        mUpdate_time.setText(currentDateTimeString);


        String friendSize = Integer.toString(frnds.size());
        mFriend_Info.setText(friendSize);

        if(friend.friendsFound == true ) {

            if(friend.friendsLocationsFound == true) {


                if(friend.friendsDetailsFound == true) {

                    mOptionsMenu.getItem(0).setVisible(true);//sets buttons to true only if data is ready, avoids null pointers when internet connection is slow or device is slow
                    mRefreshMap.setVisibility(View.VISIBLE);
                    mLocateFriends.setVisibility(View.VISIBLE);
                }
            }

        }

    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(@Nullable final Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);//refresh interval for user location
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(UserMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }

//        if(isSharingLocation) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


            mRefreshMap.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    if(isSharingLocation) {
                        onConnected(bundle);//calls method to actively refresh details up to date
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17));//zooms to user with appropriate level of zoom

                    } else {
                        Toast.makeText(getApplicationContext(), "You must enable Share Location", Toast.LENGTH_LONG).show();
                    }

                }
            });

//        }


        mLocateFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isSharingLocation) {
                    if (frnddtls!=null) {
                        setMarkers();//calls method if sharing

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You must enable Share Location", Toast.LENGTH_LONG).show();//else shows message to change setting
                }


            }
        });


        mNavigateHalfway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (halfwayPoint != null) {
                    openGoogleMap(halfwayPoint);
                }
            }
        });

        mEmailFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail(halfwayPoint);
            }
        });



    }


    protected void sendEmail(Place place) {//creates email intent and passes appropriate information

        String emailAddress = frnddtls.get(lastFriendHalfway).email;
        String[] TO = {emailAddress};

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));

        String destination = Uri.encode(place.getAddress().toString());

        //the link to send in the email
        Log.d("GOOGURL", "https://www.google.com/maps/dir/?api=1&destination="+destination);

        String emailText = "Follow this link\n\n\nhttps://www.google.com/maps/dir/?api=1&destination="+destination;

        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your friend has shared a location!");
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailText);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
//            finish();
            Log.i("Finished sending email", "");

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }

    }


    public void setMarkers() {

        mRefreshMap.setVisibility(View.INVISIBLE);
        mLocateFriends.setVisibility(View.INVISIBLE);


        //Calculate the markers to get their position
        LatLngBounds.Builder b = new LatLngBounds.Builder();


        frnds = Friend.friends;

        frnddtls = Friend.friendDetails;

        frndslocs = Friend.friendLocations;
//pulls all data from friends
        if (frnds.isEmpty() || frnddtls.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Friends not found", Toast.LENGTH_SHORT).show();
        } else if (frndslocs.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Friends not located", Toast.LENGTH_SHORT).show();
        } else {

            mMap.clear();//clears any existing map markers and data


            for (String key :
                    frnds) {

                if (frndslocs.containsKey(key) && frnddtls.containsKey(key)) {//ensures user has a location to find as they might not be sharing

                    User details = frnddtls.get(key);

                    Marker mrkr = mMap.addMarker(new MarkerOptions().title(details.getFirstname() + " " + details.getLastname()).position(frndslocs.get(key)));

                    b.include(frndslocs.get(key));

                    Polyline line = mMap.addPolyline(new PolylineOptions()
                            .add(userLatLng, frndslocs.get(key))
                            .width(5)
                            .color(Color.RED));

                    line.isVisible();

                    markerAdded = true;

                }

            }


            if(markerAdded == true) {
                LatLngBounds bounds = null;
                bounds = b.build();

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, display_width, display_height, display_width / 3));//sets camera to appropriate zoom to include all markers on screen
            }

            markerAdded = false;
        }

    }


    public void findFriend(String key) {

        if (frndslocs == null) {
            frndslocs = friend.getFriendLocations();
        }

        Log.d("KEY", key);

        User frienddetails = frnddtls.get(key);

        LatLng friendFound = frndslocs.get(key);

        mMap.clear();

        Marker m;

        if (frndslocs.get(key) != null) {


            m = mMap.addMarker(new MarkerOptions().title(frienddetails.getFirstname() + " " + frienddetails.getLastname()).position(friendFound));

            m.showInfoWindow();

            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(userLatLng, frndslocs.get(key))
                    .width(5)
                    .color(Color.RED));

            line.isVisible();

            LatLngBounds.Builder b = new LatLngBounds.Builder();

            b.include(friendFound);
            b.include(userLatLng);

            LatLngBounds bounds = null;
            bounds = b.build();

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, display_width, display_height, display_height/7));

        }
    }


    @SuppressLint("RestrictedApi")
    public void findHalfway(String key) {



        if (frndslocs == null) {
            frndslocs = friend.getFriendLocations();
        }

        LatLng friendFound = frndslocs.get(key);

        if (frndslocs.get(key) != null) {

            LatLngBounds.Builder b = new LatLngBounds.Builder();

            b.include(friendFound);
            b.include(userLatLng);

            LatLngBounds bounds = null;
            bounds = b.build();


                int PLACE_PICKER_REQUEST = 33;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                builder.setLatLngBounds(bounds);

                lastFriendHalfway = key;

                try {
                    startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
//                    startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//deals with different intents
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_CANCELED) {//makes sure its not cancelled in cases of user pressing back


            switch (resultCode) {

                case -1:

                if (requestCode == LOCATION_REQUEST_CODE && data != null) {
                    value = data.getStringExtra("friendKey");
                    if (value != null) {
                        findFriend(value);

                        mNavigateHalfway.setVisibility(View.GONE);//removes buttons if shown
                        mEmailFriend.setVisibility(View.GONE);

                    } else {
                        Toast.makeText(getApplicationContext(), "Friend location could not be retrieved", Toast.LENGTH_LONG).show();
                    }
                }

                break;

                case 22:

                    halfwayKey = data.getStringExtra("halfwayKey");

                    Toast.makeText(getApplicationContext(), "Locating halfway point...", Toast.LENGTH_LONG).show();
                    findHalfway(halfwayKey);//passes key to method to find particular friend halfway point

                break;

            }


            if(requestCode == 33) {

                Place place = PlacePicker.getPlace(this, data);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();//displays place name for user

                halfwayPoint = place;

                mNavigateHalfway.setVisibility(View.VISIBLE);
                mEmailFriend.setVisibility(View.VISIBLE);//makes buttons visibile as they can be used now

                showHalfwayOnMap(lastFriendHalfway, halfwayPoint);//passes location and friend id to set markers

            }

        }

    }

    private void showHalfwayOnMap(String friendKey, Place halfway) {

        if (frndslocs == null) {
            frndslocs = friend.getFriendLocations();
        }

        User frienddetails = frnddtls.get(friendKey);

        LatLng friendFound = frndslocs.get(friendKey);

        mMap.clear();

        if (frndslocs.get(friendKey) != null) {

            mMap.addMarker(new MarkerOptions().title(frienddetails.getFirstname() + " " + frienddetails.getLastname()).position(friendFound));
            mMap.addMarker(new MarkerOptions().title("Meeting Point").position(halfway.getLatLng()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            //adds two markers
            //on at the friend and one at the meeting point which is green
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(userLatLng, halfway.getLatLng())
                    .width(5)
                    .color(Color.RED));

            line.isVisible();

            Polyline line2 = mMap.addPolyline(new PolylineOptions()
                    .add(friendFound, halfway.getLatLng())
                    .width(5)
                    .color(Color.RED));

            line2.isVisible();

            //draws two lines
            //one from friend to the meeting point
            //one from the user to the meeting point

            LatLngBounds.Builder b = new LatLngBounds.Builder();

            b.include(friendFound);
            b.include(userLatLng);
            b.include(halfway.getLatLng());

            LatLngBounds bounds;
            bounds = b.build();

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, display_width, display_height, display_height / 7)); //displays on map


        }



    }

    public void openGoogleMap(Place place) {//opens google maps and passes the meeting point so that they can navigate there
        String dest = Uri.encode(place.getAddress().toString());
        Uri gmmIntentUri = Uri.parse("geo:0,0?q="+dest);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);


    }

    public void findNearestFriend() {

        if (frndslocs == null) {
            frndslocs = friend.getFriendLocations();
        }

        frnds = Friend.friends;

        float distanceInMeters;
        float closestDistance = 0;
        String closestFriend = null;
        LatLng closestFriendPosition;

        for (String friend://loops all friends and checks location distance, shortest is saved and then passed to findFriend to show on the map
             frnds) {
            if (frndslocs.containsKey(friend)) {

                LatLng loc = frndslocs.get(friend);

                Location l = new Location("");
                l.setLatitude(loc.latitude);
                l.setLongitude(loc.longitude);
                distanceInMeters = mLastLocation.distanceTo(l);

                if (distanceInMeters<closestDistance || closestDistance == 0) {
                        closestDistance = distanceInMeters;
                        closestFriend = friend;
                        closestFriendPosition = loc;
                }


            }
        }

        if (closestFriend != null) {
            findFriend(closestFriend);
        } else {
            Toast.makeText(getApplicationContext(), "No friends found", Toast.LENGTH_LONG).show();
        }

    }

    public void stopSharing() {//removes location, important to give user control of their data
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();//gets current user logged in
        DatabaseReference friendsSharing = FirebaseDatabase.getInstance().getReference("friendsSharing");

        GeoFire geoFire = new GeoFire(friendsSharing);
        geoFire.removeLocation(userId);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    mapFragment.getMapAsync(this);
                }
                else {
                    Toast.makeText(UserMapActivity.this, "You cannot use the map without providing permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        stopSharing();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
