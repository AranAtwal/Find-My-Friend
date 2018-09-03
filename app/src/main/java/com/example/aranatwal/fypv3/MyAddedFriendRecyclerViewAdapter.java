package com.example.aranatwal.fypv3;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aranatwal.fypv3.AddedFriendFragment.OnListFragmentInteractionListener;
import com.example.aranatwal.fypv3.Model.User;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

import static android.app.PendingIntent.getActivity;
import static com.example.aranatwal.fypv3.FriendsTabbedActivity.frnddtls;
import static com.example.aranatwal.fypv3.FriendsTabbedActivity.frndlocs;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class MyAddedFriendRecyclerViewAdapter extends RecyclerView.Adapter<MyAddedFriendRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<String> mValues;
    private final OnListFragmentInteractionListener mListener;
    private HashMap<String, User> details;
    private HashMap<String, LatLng> locations;

//    private HashMap<String,User> details;
//    private HashMap<String,LatLng>locations;

    public MyAddedFriendRecyclerViewAdapter(ArrayList<String> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_addedfriend, parent, false);

        details = frnddtls;
        locations = frndlocs;

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        while (details == null) {

        }
        holder.mItem = details.get(mValues.get(position));
        if (holder.mItem.getFirstname() != null && holder.mItem.getLastname()!=null) {//occupies fields with friend details
            holder.mIdView.setText(details.get(mValues.get(position)).getFirstname() + " " + details.get(mValues.get(position)).getLastname());

            if (locations.get(mValues.get(position)) != null) {//only shows buttons if the user has a location as they might not be sharing and this would cause a null pointer

                holder.mFind.setVisibility(View.VISIBLE);
                holder.mHalfway.setVisibility(View.VISIBLE);
            }
        }



        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
//                    mListener.onAddedListFragmentInteraction(holder.mItem);
                }
            }
        });



        holder.mFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (UserMapActivity.isSharingLocation) {
                    Toast.makeText(view.getContext(), "Locating...", Toast.LENGTH_SHORT).show();
//
                    mListener.onAddedListFragmentInteraction(holder.mItem);//goes to listener which sets result and returns to UserMapActivity
                } else {
                    Toast.makeText(view.getContext(), "You must enable Share Location", Toast.LENGTH_LONG).show();

                }
            }
        });

        holder.mHalfway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (UserMapActivity.isSharingLocation) {
                    mListener.onHalfwayFragmentInteraction(holder.mItem);//goes to listener which sets result and returns to UserMapActivity

                } else {
                    Toast.makeText(view.getContext(), "You must enable Share Location", Toast.LENGTH_LONG).show();

                }

            }
        });




    }


    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {//holds the view for each list object
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public User mItem;

        ImageButton mFind, mHalfway;

        public ViewHolder(View view) {//items declared so they can be occupied or used
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);

            mFind = (ImageButton) view.findViewById(R.id.find_friend);
            mHalfway = (ImageButton) view.findViewById(R.id.find_halfway);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
