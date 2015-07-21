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
import com.mycheez.model.User;
import com.mycheez.util.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ahetawal on 7/15/15.
 */
public class PlayersListAdapter extends RecyclerView.Adapter<PlayersListAdapter.PlayerViewHolder> {

    private List<User> players;
    private Map<String, User> playerMap;
    private ChildEventListener mListener;
    private Query mRef;
    private Activity activity;

    public PlayersListAdapter(Activity activity, Query query) {
        this.activity = activity;
        players = new ArrayList<>();
        playerMap =  new HashMap<>();
        mRef = query;

        mListener = this.mRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                User model = dataSnapshot.getValue(User.class);
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
    public PlayerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.player_row, viewGroup, false);
        PlayerViewHolder cvh = new PlayerViewHolder(v);
        return cvh;
    }

    @Override
    public void onBindViewHolder(PlayerViewHolder playerViewHolder, int i) {
        User player = players.get(i);
        playerViewHolder.counterTextView.setText(Integer.toString(player.getCheeseCount()));
        //use Picasso to load image into ImageView
        String imageUrl = player.getProfilePicUrl() + "?type=normal";
        Picasso.with(activity).load(imageUrl)
                .fit()
                .centerCrop()
                .into(playerViewHolder.playerImageView);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class PlayerViewHolder extends RecyclerView.ViewHolder {

        TextView counterTextView;
        CircularImageView playerImageView;
        ImageView cheeseAnimationImageView;

        PlayerViewHolder(View itemView) {
            super(itemView);
            counterTextView = (TextView)itemView.findViewById(R.id.counterTextView);
            playerImageView = (CircularImageView)itemView.findViewById(R.id.playerImageView);
            cheeseAnimationImageView = (ImageView) itemView.findViewById(R.id.cheeseAnimationImageView);
        }
    }


}