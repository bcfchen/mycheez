package com.mycheez.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.mycheez.R;
import com.mycheez.adapter.PlayersListAdapter;
import com.mycheez.adapter.UserViewAdapter;
import com.mycheez.application.MyCheezApplication;
import com.mycheez.enums.UpdateType;
import com.mycheez.firebase.FirebaseProxy;
import com.mycheez.model.User;
import com.mycheez.util.AnimationHandler;
import com.mycheez.util.CircularImageView;

public class TheftActivity extends Activity {
	CircularImageView userProfileImageView;
	TextView userCheeseTextView;
	ImageView refreshImageView;
	ImageView rankingsImageView;
    private RecyclerView playersList;
    private PlayersListAdapter playersListAdapter;
	private AnimationHandler animationHandler;
	private UpdateType updateType;
	private Firebase mFirebaseRef;
	private String TAG = "theftActivity";
    private UserViewAdapter userViewAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_theft);
		Bundle extras = getIntent().getExtras();
		String facebookId = extras.getString("facebookId");
		initializeUtilities();
		initializeUIControls();
		setupUserFirebaseBindings(facebookId);
        initializePlayersList();
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

	/* setup firebase binding to update user data */
	private void setupUserFirebaseBindings(final String authUid){
		mFirebaseRef = MyCheezApplication.getMyCheezFirebaseRef();
		// make single call to retrieve info for current user once
		FirebaseProxy.getUserData(authUid, new FirebaseProxy.GetUserDataCallback() {
            @Override
            public void userDataRetrieved(User user) {
                if (user == null) {
                    Toast.makeText(TheftActivity.this, getString(R.string.get_user_failed_message), Toast.LENGTH_LONG).show();
                } else {
                    userViewAdapter.setUser(user);
                }
            }
        });

		// bind cheese count
		FirebaseProxy.getUserCheeseCount(authUid, new FirebaseProxy.GetUserCheeseCountCallback() {
            @Override
            public void userCheeseCountRetrieved(Integer cheeseCount) {
                if (cheeseCount == null) {
                    Toast.makeText(TheftActivity.this, getString(R.string.get_user_cheese_failed_message), Toast.LENGTH_LONG).show();
                } else {
                    userViewAdapter.setCheeseCount(cheeseCount);
				}
			}
		});
	}

    
	private void initializeUIControls()
	{
		initializeUserView();
		initializeImageButtons();
	}

	private void initializeUserView(){
		/* create adapter for user view */
		userCheeseTextView = (TextView) findViewById(R.id.cheeseCountTextView);
		userProfileImageView = (CircularImageView) findViewById(R.id.userProfileImageView);
		userViewAdapter = new UserViewAdapter(this, userCheeseTextView, userProfileImageView);
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
		rankingsImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent(TheftActivity.this, RankingsActivity.class);
//				updateType = UpdateType.NOUPDATE;
//				startActivity(intent);
			}

		});
	}

    private void initializePlayersList(){
        playersList= ( RecyclerView )findViewById( R.id.playersList );
        Query playersQuery = mFirebaseRef.child("users");
        LinearLayoutManager llm = new LinearLayoutManager(this);
        playersList.setLayoutManager(llm);
        playersListAdapter = new PlayersListAdapter(this, playersQuery);
        playersList.setAdapter( playersListAdapter );
    }

	@Override
	public void onDestroy() {
		System.out.println("Called destory...");
		MyCheezApplication.setActivityisStopping();
		super.onDestroy();
	}

    @Override
    public void onStop() {
        super.onStop();
        playersListAdapter.cleanup();
    }

}
