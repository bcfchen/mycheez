package com.mycheez.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.mycheez.R;
import com.mycheez.activity.TheftActivity;
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
    private Activity theftActivity;
    private String TAG = "PlayersList";
    private Map<Integer, Boolean> onClickLockMap;
    private Animation pulseAnimation;

    public PlayersListAdapter(Activity activity, Query query, final String currentUserFacebookId) {
        this.theftActivity = activity;
        players = new ArrayList<>();
        playerMap =  new HashMap<>();
        onClickLockMap = new HashMap<>();

        pulseAnimation = AnimationUtils.loadAnimation(theftActivity, R.anim.pulse);

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
                int nextIndex = 0;
                if (previousChildName == null) {
                    players.add(nextIndex, model);
                } else {
                    User previousModel = playerMap.get(previousChildName);
                    int previousIndex = players.indexOf(previousModel);
                    nextIndex = previousIndex + 1;
                    if (nextIndex == players.size()) {
                        players.add(model);
                    } else {
                        players.add(nextIndex, model);
                    }
                }
                onClickLockMap.put(nextIndex, true);
                notifyItemInserted(nextIndex);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // One of the mModels changed. Replace it in our list and name mapping
                String modelName = dataSnapshot.getKey();
                // Dont add the current user to the players list
                if(currentUserFacebookId.equals(modelName)){
                    return;
                }

                User oldModel = playerMap.get(modelName);
                User newModel = dataSnapshot.getValue(User.class);
                int index = players.indexOf(oldModel);

                players.set(index, newModel);
                playerMap.put(modelName, newModel);

               // notifyDataSetChanged();
                notifyItemChanged(index);
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
    public void onBindViewHolder(final PlayerViewHolder playerViewHolder, final int position) {
        if(!onClickLockMap.containsKey(position)){
            onClickLockMap.put(position, true);
        }
        final User player = players.get(position);
        playerViewHolder.counterTextView.setText(Integer.toString(player.getCheeseCount()));
        //use Picasso to load image into ImageView
        String imageUrl = player.getProfilePicUrl() + "?type=normal";
        Picasso.with(theftActivity).load(imageUrl)
                .fit()
                .centerCrop()
                .into(playerViewHolder.playerImageView);

        setOnlineStatus(playerViewHolder, position);
        setOnClickListenerOnPlayers(playerViewHolder, position);
    }


    private void setOnlineStatus(PlayerViewHolder playerViewHolder, int position) {
        User player = players.get(position);
        if(player.getIsOnline() && player.getCheeseCount() > 0){
            playerViewHolder.playerImageView.setBorderColor(Color.GREEN);
        }else {
            playerViewHolder.playerImageView.setBorderColor(Color.RED);
        }
    }

    /**
     * Method used for setting up the image onclick listeners on each player image
     * based on their current cheese counts. Stealable vs Non-stealable.
     * @param playerViewHolder
     * @param position
     *
     */
    private void setOnClickListenerOnPlayers(final PlayerViewHolder playerViewHolder, final int position) {
        if(canCheeseBeStolenAtThisLocation(players.get(position).getCheeseCount(), position)){
            unlockImageClick(playerViewHolder);
            playerViewHolder.playerImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleOnClickLock(true, playerViewHolder, position);
                    ((TheftActivity)theftActivity).onCheeseTheft(playerViewHolder.playerImageView, players.get(position),
                            playerViewHolder.cheeseAnimationImageView);

                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handleOnClickLock(false, playerViewHolder, position);
                        }
                    },5000);
                }
            });
        } else {
            lockImageClick(playerViewHolder);
        }
    }

    private boolean canCheeseBeStolenAtThisLocation(int cheeseCount , int position) {
        return cheeseCount > 0 && onClickLockMap.get(position);
    }

    public void lockImageClick(PlayerViewHolder playerViewHolder) {
        playerViewHolder.playerImageView.setAlpha(0.2f);
        playerViewHolder.counterTextView.setAlpha(0.2f);
        playerViewHolder.playerImageView.setClickable(false);
    }

    public void unlockImageClick(PlayerViewHolder playerViewHolder) {
        playerViewHolder.playerImageView.setAlpha(1f);
        playerViewHolder.counterTextView.setAlpha(1f);
        playerViewHolder.playerImageView.setClickable(true);
    }

    public void handleOnClickLock(boolean lockIt, PlayerViewHolder playerViewHolder, int position){
        if(lockIt){
            lockImageClick(playerViewHolder);
            onClickLockMap.put(position, false);
        } else {
            onClickLockMap.put(position, true);
            // Only unlock is count is > 0, otherwise keep it locked
            User player = players.get(position);
            if(player.getCheeseCount() > 0){
                unlockImageClick(playerViewHolder);
            }

        }
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