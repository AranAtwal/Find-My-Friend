package com.example.aranatwal.fypv3;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.aranatwal.fypv3.FriendRequestFragment.OnListFragmentInteractionListener;
import com.example.aranatwal.fypv3.Model.User;

import java.util.ArrayList;
import java.util.List;

import static android.app.PendingIntent.getActivity;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class MyFriendRequestRecyclerViewAdapter extends RecyclerView.Adapter<MyFriendRequestRecyclerViewAdapter.ViewHolder> {

    private final List<User> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyFriendRequestRecyclerViewAdapter(ArrayList<User> items, OnListFragmentInteractionListener listener) {
        mValues = items;//the list of users which is passed through when the adapter is set in the fragment
        mListener = listener;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friendrequest, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        String first = mValues.get(position).getFirstname();
        String second = mValues.get(position).getLastname();
        String full = first + " " + second;
        holder.mIdView.setText(full);//combines name to use one variable

        final int pos = position;

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });

        holder.mAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//accept function
                FriendRequestData friendRequestData = FriendRequestData.getInstance();
                friendRequestData.acceptFriendRequest((ArrayList<User>) mValues, pos);
//                Toast.makeText(view.getContext(), "Item clicked" + position, Toast.LENGTH_LONG).show();


                mValues.remove(pos);//removes from list to avoid multiple clicks
                MyFriendRequestRecyclerViewAdapter.this.notifyDataSetChanged();//updates list



            }
        });

        holder.mDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendRequestData friendRequestData = FriendRequestData.getInstance();
                friendRequestData.declineFriendRequest((ArrayList<User>) mValues, pos);
//                Toast.makeText(view.getContext(), "Item clicked" + position, Toast.LENGTH_LONG).show();

                mValues.remove(pos);
                MyFriendRequestRecyclerViewAdapter.this.notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {//sets list variables to use later with data
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;

        Button mAccept;
        Button mDecline;

        public User mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);

            mAccept = (Button) view.findViewById(R.id.accept_request);//accept and decline buttons
            mDecline = (Button) view.findViewById(R.id.decline_request);

            mContentView = (TextView) view.findViewById(R.id.content);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }


    }

}
