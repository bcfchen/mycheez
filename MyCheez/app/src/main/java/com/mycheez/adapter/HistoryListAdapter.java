package com.mycheez.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.mycheez.R;
import com.mycheez.activity.TheftActivity;
import com.mycheez.model.History;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ahetawal on 7/15/15.
 */
public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.HistoryViewHolder> {

    private List<History> historyList;
    private Map<String, History> historyMap;
    private ChildEventListener mListener;
    private Query mRef;
    private Activity activity;
    private TheftActivity.PlayerListScroller scroller;

    public HistoryListAdapter(Activity activity, Query query, TheftActivity.PlayerListScroller playerListScroller) {
        this.activity = activity;
        historyList = new ArrayList<>();
        historyMap =  new HashMap<>();
        mRef = query;
        scroller = playerListScroller;

        mListener = this.mRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                History model = dataSnapshot.getValue(History.class);
                historyMap.put(dataSnapshot.getKey(), model);

                // Insert into the correct location, based on previousChildName
                int newIdx = 0;
                if (previousChildName == null) {
                    historyList.add(newIdx, model);
                } else {
                    History previousModel = historyMap.get(previousChildName);
                    int previousIndex = historyList.indexOf(previousModel);
                    int nextIndex = previousIndex + 1;
                    newIdx = nextIndex;
                    if (nextIndex == historyList.size()) {
                        historyList.add(model);
                    } else {
                        historyList.add(nextIndex, model);
                    }
                }
                notifyItemInserted(newIdx);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // One of the mModels changed. Replace it in our list and name mapping
                String modelName = dataSnapshot.getKey();
                History oldModel = historyMap.get(modelName);
                History newModel = dataSnapshot.getValue(History.class);
                int index = historyList.indexOf(oldModel);

                historyList.set(index, newModel);
                historyMap.put(modelName, newModel);

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // A model was removed from the list. Remove it from our list and the name mapping
                String modelName = dataSnapshot.getKey();
                History oldModel = historyMap.get(modelName);
                historyList.remove(oldModel);
                historyMap.remove(modelName);
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
        historyList.clear();
        historyMap.clear();
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.history_row, viewGroup, false);
        HistoryViewHolder cvh = new HistoryViewHolder(v, new HistoryViewHolder.HistoryOnClicks() {
            @Override
            public void doOnClick(HistoryViewHolder holder) {
                final HistoryViewHolder tempHolder = holder;
                final int position = tempHolder.getPosition();
                History thief = historyList.get(position);
                scroller.scrollPlayerList(thief.getThiefId());
            }
        });
        return cvh;
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder theftHistoryViewHolder, int i) {
        History theft = historyList.get(i);
        theftHistoryViewHolder.friendNameTextView.setText(theft.getThiefName());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{

        TextView friendNameTextView;
        TextView stoleCheeseTextView;
        HistoryOnClicks clickListener;

        HistoryViewHolder(View itemView, HistoryOnClicks listener) {
            super(itemView);
            friendNameTextView = (TextView) itemView.findViewById(R.id.friendNameTextview);
            stoleCheeseTextView=(TextView)itemView.findViewById(R.id.stoleCheeseTextView);
            clickListener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.doOnClick(this);
        }


        public interface HistoryOnClicks {
            void doOnClick(HistoryViewHolder holder);
        }
    }


}