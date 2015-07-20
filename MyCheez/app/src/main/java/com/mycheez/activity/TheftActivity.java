package com.mycheez.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.mycheez.R;
import com.mycheez.adapter.UserViewAdapter;
import com.mycheez.application.MyCheezApplication;
import com.mycheez.enums.UpdateType;
import com.mycheez.model.User;
import com.mycheez.util.AnimationHandler;
import com.mycheez.util.CircularImageView;

public class TheftActivity extends Activity {
	CircularImageView userProfileImageView;
	TextView userCheeseTextView;
	ImageView refreshImageView;
	ImageView rankingsImageView;
	private AnimationHandler animationHandler;
	private UpdateType updateType;
	private Firebase mFirebaseRef;
	private User currentUser;
	private String TAG = "theftActivity";
    private UserViewAdapter userViewAdapter;
    private ObjectMapper mapper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_theft);
		Bundle extras = getIntent().getExtras();
		String facebookId = extras.getString("facebookId");
		initializeUtilities();
		initializeUIControls();
		setupFirebaseBindings(facebookId);
		updateType = UpdateType.LOGIN;
	}

	private void initializeUtilities() {
        mapper = new ObjectMapper();
		this.animationHandler = new AnimationHandler(this);
	}

	@Override
	public void onBackPressed() {
		    finish();
            super.onBackPressed();
    }

	/* setup firebase binding to update user data */
	private void setupFirebaseBindings(final String authUid){
		mFirebaseRef = MyCheezApplication.getRootFirebaseRef();
		// setup current user binding
		mFirebaseRef.child("users").child(authUid).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				Log.i(TAG, "users changed in Firebase");
				currentUser = mapper.convertValue(snapshot.getValue(), User.class);
				populateUserView();
			}

			@Override
			public void onCancelled(FirebaseError error) {
			}
		});
	}

	/* set display properties for user */
	private void populateUserView()
    {
		/* create adapter for user view */
		userCheeseTextView = (TextView) findViewById(R.id.cheeseCountTextView);
		userProfileImageView = (CircularImageView) findViewById(R.id.userProfileImageView);
        userViewAdapter = new UserViewAdapter(this, userCheeseTextView, userProfileImageView);

		/* set display values via adapter */
        userViewAdapter.setUser(currentUser);
    }


	private void initializeUIControls()
	{
		initializeImageButtons();
	}

	/* hook up image button clicks */
	private void initializeImageButtons()
	{
		/* hook up refresh button to fetch data from Parse and populate views */
		refreshImageView = (ImageView)findViewById(R.id.refreshImageView);
		refreshImageView.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
			}

		});

		/* hook up rankings button to fetch ranking info from Parse and populate views */
		rankingsImageView = (ImageView)findViewById(R.id.rankingImageView);
		rankingsImageView.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent(TheftActivity.this, RankingsActivity.class);
//				updateType = UpdateType.NOUPDATE;
//				startActivity(intent);
				}

		});
	}



	@Override
	public void onDestroy() {
		System.out.println("Called destory...");
		MyCheezApplication.setActivityisStopping();
		super.onDestroy();

	}

}
