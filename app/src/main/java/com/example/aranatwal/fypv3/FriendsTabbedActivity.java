package com.example.aranatwal.fypv3;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.aranatwal.fypv3.Model.Friend;
import com.example.aranatwal.fypv3.Model.User;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class FriendsTabbedActivity extends AppCompatActivity implements AddFriendsFragment.OnFragmentInteractionListener, AddedFriendFragment.OnFragmentInteractionListener, AddedFriendFragment.OnListFragmentInteractionListener, FriendRequestFragment.OnListFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    Friend friend;
    public static HashMap<String, User> frnddtls;
    public static HashMap<String, LatLng> frndlocs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_tabbed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        toolbar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        Friend.getInstance();//creates friends object or calls existing instance and calls methods pulling data to be available to the user
        frnddtls = Friend.friendDetails;
        frndlocs = Friend.friendLocations;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends_tabbed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(User item) {

    }

    @Override
    public void onChangedListFragmentInteraction() {

    }

    @Override
    public void onAddedListFragmentInteraction(User item) {
        String i = item.getUid();


        Intent in = new Intent();
        in.putExtra("friendKey", i);
        setResult(RESULT_OK, in);
        finish();
    }

    @Override
    public void onHalfwayFragmentInteraction(User item) {
        String s = item.getUid();

        Intent in = new Intent();
        in.putExtra("halfwayKey", s);
        setResult(22, in);//sets result so that the UserMapActivity can deal with it with appropriate action
        finish();
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {//creating different fragments to hold in the activity

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
//            return PlaceholderFragment.newInstance(position + 1);
            switch (position) {
                case 0:
                    AddedFriendFragment addedFriendFragment = new AddedFriendFragment();
                    return addedFriendFragment;
                case 1:
                    AddFriendsFragment addFriendsFragment = new AddFriendsFragment();
                    return addFriendsFragment;
                case 2:
                    FriendRequestFragment friendRequestFragment = new FriendRequestFragment();
                    return friendRequestFragment;
                default:
                    AddedFriendFragment addedFriendFragment2 = new AddedFriendFragment();
                    return addedFriendFragment2;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        public HashMap<String, User> getFriendDetails() {
            return frnddtls;
        }
    }
}
