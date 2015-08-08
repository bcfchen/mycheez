package com.mycheez.adapter;

import android.app.Activity;
import android.graphics.Color;
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
import com.firebase.client.ValueEventListener;
import com.mycheez.R;
import com.mycheez.activity.TheftActivity;
import com.mycheez.application.MyCheezApplication;
import com.mycheez.model.User;
import com.mycheez.util.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ahetawal on 7/15/15.
 */
public class PlayersListAdapter extends RecyclerView.Adapter<PlayersListAdapter.PlayerViewHolder> {

    private LinkedList<User> players;
    private Map<String, User> playerMap;
    private ChildEventListener childListener;
    private Activity theftActivity;
    private String TAG = "PlayersList";
    private Map<Integer, Boolean> onClickLockMap;
    private Query allUsersRef = MyCheezApplication.getMyCheezFirebaseRef().child("users");
    private boolean isFirstTimeLoaded = true;
    private Map<String, Integer> listLocationMap;

    public PlayersListAdapter(Activity activity, final User currentUser, Map<String, Integer> playerListScrollLocationMap) {
        this.theftActivity = activity;
        players = new LinkedList<>();
        playerMap =  new HashMap<>();
        onClickLockMap = new HashMap<>();
        listLocationMap = playerListScrollLocationMap;

        final String currentUserFacebookId = currentUser.getFacebookId();
        final List<String> currentUserFriendsList = currentUser.getFriends();

        allUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    User model = child.getValue(User.class);
                    // Dont add the current user to the players list
                    if(currentUserFacebookId.equals(model.getFacebookId())){
                        continue;
                    }
                    if(currentUserFriendsList.contains(model.getFacebookId())){
                        players.addFirst(model);
                    }else {
                        players.addLast(model);
                    }
                    playerMap.put(child.getKey(), model);
                }
                for(int i = 0; i < players.size(); i++){
                    User u = players.get(i);
                    listLocationMap.put(u.getFacebookId(), i);
                    onClickLockMap.put(i, true);
                }
                setUpChildListeners(currentUser);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("PlayerListAdapter", "All users data listener error "+ firebaseError.getDetails());
            }
        });
    }

    private void setUpChildListeners(final User currentUser) {
        childListener = allUsersRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                User model = dataSnapshot.getValue(User.class);

                // skipping all initial child added calls
                if(playerMap.containsKey(dataSnapshot.getKey()) ||
                        currentUser.getFacebookId().equals(dataSnapshot.getKey())){
                    return;
                }
                playerMap.put(dataSnapshot.getKey(), model);
                players.addLast(model);
                int size = players.size();
                onClickLockMap.put(size, true);
                listLocationMap.put(dataSnapshot.getKey(), size);
                notifyItemInserted(size);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // One of the mModels changed. Replace it in our list and name mapping
                String modelName = dataSnapshot.getKey();
                // Dont add the current user to the players list
                if(currentUser.getFacebookId().equals(modelName)){
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
                listLocationMap.remove(modelName);
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
        allUsersRef.removeEventListener(childListener);
        players.clear();
        playerMap.clear();
    }

    @Override
    public PlayerViewHolder onCreateViewHolder(ViewGroup viewGroup, final int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.player_row, viewGroup, false);

        // Better way to setup onlick listeners
        PlayerViewHolder cvh = new PlayerViewHolder(v, new PlayerViewHolder.SetupPlayerOnClicks() {
            @Override
            public void doOnClick(PlayerViewHolder holder) {
                final PlayerViewHolder tempHolder = holder;
                final int position = tempHolder.getPosition();
                final ImageView playerImage = tempHolder.playerImageView;
                final ImageView cheeseAnimationImageView = holder.cheeseAnimationImageView;

                handleOnClickLock(true, tempHolder, position);

                ((TheftActivity)theftActivity).onCheeseTheft(playerImage, players.get(position), cheeseAnimationImageView);

                playerImage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handleOnClickLock(false, tempHolder, position);
                    }
                }, 2000);
            }
        });
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

    public static class PlayerViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{

        TextView counterTextView;
        CircularImageView playerImageView;
        ImageView cheeseAnimationImageView;
        SetupPlayerOnClicks playerClickListener;

        PlayerViewHolder(View itemView, SetupPlayerOnClicks listener) {
            super(itemView);
            playerClickListener = listener;
            counterTextView = (TextView)itemView.findViewById(R.id.counterTextView);
            playerImageView = (CircularImageView)itemView.findViewById(R.id.playerImageView);
            cheeseAnimationImageView = (ImageView) itemView.findViewById(R.id.cheeseAnimationImageView);
            playerImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            playerClickListener.doOnClick(this);
        }


        public interface SetupPlayerOnClicks {
            void doOnClick(PlayerViewHolder holder);
        }
    }


}