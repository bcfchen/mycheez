package com.mycheez.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.mycheez.R;
import com.mycheez.activity.RankingsActivity;
import com.mycheez.activity.TheftActivity;
import com.mycheez.model.User;
import com.mycheez.util.CircleTransform;
import com.mycheez.util.CircularImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingsListAdapter extends RecyclerView.Adapter<RankingsListAdapter.RankingViewHolder> {

    private List<User> players;
    private Map<String, User> playerMap;
    private ChildEventListener mListener;
    private Query mRef;
    private Activity rankingsActivity;
    private String TAG = "RankingsListAdapter";

    public RankingsListAdapter(Activity activity, Query query, final String currentUserFacebookId) {
        this.rankingsActivity = activity;
        players = new ArrayList<>();
        playerMap =  new HashMap<>();
        mRef = query;

        mListener = this.mRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                User model = dataSnapshot.getValue(User.class);

                // Dont add the current user to the players list
                if(currentUserFacebookId.equals(model.getFacebookId())){
                    return;
                }

                playerMap.put(dataSnapshot.getKey(), model);
                // Insert into the correct location, based on previousChildName
                if (previousChildName == null) {
                    players.add(0, model);
                } else {
                    User previousModel = playerMap.get(previousChildName);
                    int previousIndex = players.indexOf(previousModel);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == players.size()) {
                        players.add(model);
                    } else {
                        players.add(nextIndex, model);
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // One of the mModels changed. Replace it in our list and name mapping
                String modelName = dataSnapshot.getKey();
                User oldModel = playerMap.get(modelName);
                User newModel = dataSnapshot.getValue(User.class);
                int index = players.indexOf(oldModel);

                players.set(index, newModel);
                playerMap.put(modelName, newModel);

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // A model was removed from the list. Remove it from our list and the name mapping
                String modelName = dataSnapshot.getKey();
                User oldModel = playerMap.get(modelName);
                players.remove(oldModel);
                playerMap.remove(modelName);
                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //NONE
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("FirebaseListAdapter", "Listen was cancelled, no more updates will occur");
            }

        });

    }

    public void cleanup() {
        // We're being destroyed, let go of our mListener and forget about all of the mModels
        mRef.removeEventListener(mListener);
        players.clear();
        playerMap.clear();
    }

    @Override
    public RankingViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ranking_row, viewGroup, false);
        RankingViewHolder cvh = new RankingViewHolder(v);
        return cvh;
    }

    @Override
    public void onBindViewHolder(final RankingViewHolder playerViewHolder, final int position) {
        final User player = players.get(position);
        String cheeseCountText = "x "+ Integer.toString(player.getCheeseCount());
        playerViewHolder.cheeseCountTextView.setText(cheeseCountText);
        playerViewHolder.playerNameTextview.setText(player.getFirstName());

        //use Picasso to load image into ImageView
        String imageUrl = player.getProfilePicUrl() + "?type=normal";
        Transformation circleTransform = new CircleTransform();
        Picasso.with(rankingsActivity).load(imageUrl)
                .transform(circleTransform)
                .into(playerViewHolder.playerImageView);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class RankingViewHolder extends RecyclerView.ViewHolder {

        TextView cheeseCountTextView;
        TextView playerNameTextview;
        ImageView playerImageView;

        RankingViewHolder(View itemView) {
            super(itemView);
            cheeseCountTextView = (TextView)itemView.findViewById(R.id.cheeseCountTextView);
            playerNameTextview = (TextView)itemView.findViewById(R.id.playerNameTextview);
            playerImageView = (ImageView) itemView.findViewById(R.id.playerImageView);
        }
    }


}