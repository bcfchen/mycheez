package com.mycheez.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mycheez.R;
import com.mycheez.application.MyCheezApplication;
import com.mycheez.enums.UpdateType;
import com.mycheez.util.AnimationHandler;
import com.mycheez.util.AuthenticationHelper;
import com.mycheez.util.CircularImageView;

public class TheftActivity extends Activity {
	CircularImageView userProfileImageView;
	TextView userCheeseTextView;
	ImageView refreshImageView;
	ImageView rankingsImageView;
	private AnimationHandler animationHandler;
	private UpdateType updateType;
	private AuthenticationHelper authHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_theft);
		Bundle extras = getIntent().getExtras();
		String authUid = extras.getString("authenticationUid");
		authHelper = new AuthenticationHelper(authUid);
		initializeUtilities();
		initializeUIControls();
		updateType = UpdateType.LOGIN;
	}

	private void initializeUtilities() {
		this.animationHandler = new AnimationHandler(this);
	}

	@Override
	public void onBackPressed() {
		    finish();
            super.onBackPressed();
    }



	/* set display properties for user */
	private void populateUserView()
    {
		/* create adapter for user view */
		userCheeseTextView = (TextView) findViewById(R.id.cheeseCountTextView);
		userProfileImageView = (CircularImageView) findViewById(R.id.userProfileImageView);

		/* set display values via adapter */
//		userViewAdapter.setUser(userViewModel);
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
