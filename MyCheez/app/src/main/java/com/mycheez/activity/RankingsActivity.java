package com.mycheez.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.mycheez.R;
import com.mycheez.adapter.RankingsListAdapter;
import com.mycheez.application.MyCheezApplication;
import com.mycheez.firebase.FirebaseProxy;
import com.mycheez.model.User;
import com.mycheez.util.CircleTransform;
import com.mycheez.util.RecyclerViewLinearLayoutManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class RankingsActivity extends Activity {
	RankingsListAdapter rankingsListAdapter;
    RecyclerView rankingsListView;
	TextView userRankingTextView;
	ImageView backButtonImageView;
	ImageView shareButtonImageView;
	View backButtonContainer;
	int userCheeseCount;
	String currentUserFacebookId;
    private Firebase mFirebaseRef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rankings);
		Bundle extras = getIntent().getExtras();
		currentUserFacebookId = extras.getString("facebookId");
        mFirebaseRef = MyCheezApplication.getMyCheezFirebaseRef();
		initializeUIControls();
        initializeRankingsListView();
        initializeUserRanking();
    }

	private void initializeUIControls()
	{
		backButtonContainer = findViewById(R.id.backButtonContainer);
		userRankingTextView = (TextView)findViewById(R.id.userRankingTextView);
		backButtonImageView = (ImageView)findViewById(R.id.backButtonImageView);
		backButtonContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}

		});
		
		shareButtonImageView = (ImageView) findViewById(R.id.shareButtonImageView);
//		shareButtonImageView.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				shareFacebookMessage();
//			}
//
//		});
	}

	/* commenting this out for now. this is for sharing on facebook */
//	private void shareFacebookMessage()
//	{
//		String playStoreLinkUrl = getResources().getString(R.string.play_store_link);
//		String shareMessage = String.format("I got %S cheese, try snatching them from me!", Integer.toString(userCheeseCount));
//		Bundle params = new Bundle();
//		params.putString("caption", "caption");
//		params.putString("message", shareMessage);
//		params.putString("link", playStoreLinkUrl);
//
//		Request request = new Request(Session.getActiveSession(), "me/feed", params, HttpMethod.POST);
//		request.setCallback(new Request.Callback() {
//		    @Override
//		    public void onCompleted(Response response) {
//		        if (response.getError() == null) {
//		            // Tell the user success!
//		        }
//		    }
//		});
//		request.executeAsync();
//	}

	private void populateUserRanking(int ranking, User currentUser)
	{
		userRankingTextView.setText(getOrdinal(ranking));

		if (ranking > 10)
		{
			View userGreaterThanTenView = findViewById(R.id.userGreaterThanTenRanking);
			userGreaterThanTenView.setVisibility(View.VISIBLE);
			TextView userNameTextView = (TextView)userGreaterThanTenView.findViewById(R.id.playerNameTextview);
			TextView userCheeseCountTextView = (TextView)userGreaterThanTenView.findViewById(R.id.cheeseCountTextView);
            ImageView userImageView = (ImageView)userGreaterThanTenView.findViewById(R.id.playerImageView);

			/* load user image view */
			String imageUrl = currentUser.getProfilePicUrl() + "?type=normal";
			Transformation circleTransform = new CircleTransform();
            Picasso.with(this).load(imageUrl)
            .transform(circleTransform)
            .into(userImageView);

            /* load user name and cheese count */
            userNameTextView.setText(currentUser.getFirstName());
            String cheeseCountText = "x " + currentUser.getCheeseCount();
            userCheeseCountTextView.setText(cheeseCountText);
		}
	}

    private void initializeUserRanking(){
        FirebaseProxy.getUserRanking(currentUserFacebookId, new FirebaseProxy.UserRankingCallback() {
            @Override
            public void userRankingRetrieved(Integer rank, User currentUser) {
                if (rank == null) {
                    Toast.makeText(RankingsActivity.this, getString(R.string.get_user_ranking_failed_message), Toast.LENGTH_LONG).show();
                } else {
                    populateUserRanking(rank, currentUser);
                }
            }
        });
    }

    private String getOrdinal(int num)
    {
        String[] suffix = {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        int m = num % 100;
        return Integer.toString(num) + suffix[(m > 10 && m < 20) ? 0 : (m % 10)];
    }

//    FirebaseProxy.doCheeseTheft(victim, new FirebaseProxy.CheeseTheftActionCallback() {
//        @Override
//        public void cheeseTheftPerformed(boolean isSuccess) {
//            if (isSuccess) {
//                //Only update theft history, IF theft success...
//                FirebaseProxy.insertTheftHistory(victim.getFacebookId());
//            }
//        }
//    });
	private void initializeRankingsListView()
	{
		rankingsListView= (RecyclerView)findViewById( R.id.rankingsListView );
        Query topTenUsersQuery = mFirebaseRef.child("users").orderByChild("cheeseCount").limitToLast(10);
        RecyclerViewLinearLayoutManager llm = new RecyclerViewLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rankingsListView.setLayoutManager(llm);
        rankingsListAdapter = new RankingsListAdapter( this, topTenUsersQuery,currentUserFacebookId);
		rankingsListView.setAdapter( rankingsListAdapter );
	}

	
}
