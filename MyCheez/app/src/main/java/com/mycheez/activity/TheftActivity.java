package com.mycheez.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.mycheez.R;
import com.mycheez.adapter.DividerItemDecoration;
import com.mycheez.adapter.HistoryListAdapter;
import com.mycheez.adapter.PlayersListAdapter;
import com.mycheez.adapter.UserViewAdapter;
import com.mycheez.application.MyCheezApplication;
import com.mycheez.enums.CheeseCountChangeType;
import com.mycheez.firebase.FirebaseProxy;
import com.mycheez.model.User;
import com.mycheez.util.AnimationHandler;
import com.mycheez.util.CircularImageView;
import com.mycheez.util.SharedPreferencesService;

import java.util.HashMap;
import java.util.Map;

public class TheftActivity extends Activity {
	CircularImageView userProfileImageView;
	TextView userCheeseTextView;
	ImageView notificationImageView;
	ImageView rankingsImageView;
    private RecyclerView playersList;
	private RecyclerView historyList;
    private HistoryListAdapter historyListAdapter;
	private AnimationHandler animationHandler;
	private Firebase mFirebaseRef;
	private String TAG = "theftActivity";
    private UserViewAdapter userViewAdapter;
    private String currentUserFacebookId;
    private SharedPreferencesService sharedPreferencesService;
    private Map<String, Integer> playerListScrollLocationMap = new HashMap<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_theft);
        initializeUtilities();
        currentUserFacebookId = sharedPreferencesService.getUserIdToSharedPreferences();

        initializeUIControls();
        setupUserFirebaseBindings();
        initializePlayersList();
        initializeTheftHistoryList();
	}

	private void initializeUtilities() {
		this.animationHandler = new AnimationHandler(this);
        sharedPreferencesService = new SharedPreferencesService(this);
	}

	@Override
	public void onBackPressed() {
		finish();
		super.onBackPressed();
	}

	/* setup firebase binding to update user data */
	private void setupUserFirebaseBindings(){
		mFirebaseRef = MyCheezApplication.getMyCheezFirebaseRef();
		// make single call to retrieve info for current user once
        FirebaseProxy.getUserData(currentUserFacebookId, new FirebaseProxy.UserDataCallback() {
            @Override
            public void userDataRetrieved(User user) {
                if (user == null) {
                    Toast.makeText(TheftActivity.this, getString(R.string.get_user_failed_message), Toast.LENGTH_LONG).show();
                } else {
                    MyCheezApplication.setCurrentUser(user);
                    userViewAdapter.setUser(user);

                    // setup playerlist data after getting latest data
                    int dividerLocation = user.getFriends().size();
                    playersList.addItemDecoration(new DividerItemDecoration(TheftActivity.this, R.drawable.divider, false, false, dividerLocation));
                    PlayersListAdapter playersListAdapter = new PlayersListAdapter(TheftActivity.this, user, playerListScrollLocationMap);
                    playersList.setAdapter(playersListAdapter);


                }
            }
        });

        setUpCheeseCountListener();
    }


    private void initializeUIControls() {
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
	private void initializeImageButtons() {
		/* hook up refresh button to fetch data from Parse and populate views */
        notificationImageView = (ImageView) findViewById(R.id.notificationSettings);
        setNotificationIconState();

        notificationImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                sharedPreferencesService.toggleNotificationSetting();
                setNotificationIconState();
			}

		});

		/* hook up rankings button to fetch ranking info from Parse and populate views */
		rankingsImageView = (ImageView)findViewById(R.id.rankingImageView);
		rankingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TheftActivity.this, RankingsActivity.class);
                try {
                    intent.putExtra("facebookId", currentUserFacebookId);
                    startActivity(intent);
                } catch (Exception ex) {
                    Log.e(TAG, "Error launching rankings", ex);
                }
            }

        });
	}

    private void setNotificationIconState(){
        boolean state = sharedPreferencesService.getNotificationSetting();
        if (state){
            notificationImageView.setImageResource(R.drawable.notification_on_white);
        } else {
            notificationImageView.setImageResource(R.drawable.notification_off_white);
        }
    }

    private void initializePlayersList() {
        playersList = (RecyclerView) findViewById(R.id.playersList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        playersList.setLayoutManager(llm);
    }

	private void initializeTheftHistoryList(){
		historyList = ( RecyclerView )findViewById( R.id.historyList);
        Query historyQuery = mFirebaseRef.child("history").child(currentUserFacebookId).orderByKey().limitToLast(5);
        LinearLayoutManager llm = new org.solovyev.android.views.llm.LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		historyList.setLayoutManager(llm);
		historyListAdapter = new HistoryListAdapter(this, historyQuery, new PlayerListScroller() {
            @Override
            public void scrollPlayerList(String thiefId) {
                Integer location = playerListScrollLocationMap.get(thiefId);
                if(location != null) {
                    playersList.smoothScrollToPosition(location);
                }
            }
        });
		historyList.setAdapter(historyListAdapter);
    }

    private void setUpCheeseCountListener() {
        // bind cheese count
        FirebaseProxy.getUserCheeseCount(currentUserFacebookId, new FirebaseProxy.UserCheeseCountCallback() {
            @Override
            public void userCheeseCountRetrieved(Integer cheeseCount) {
                if (cheeseCount == null) {
                    Toast.makeText(TheftActivity.this, getString(R.string.get_user_cheese_failed_message), Toast.LENGTH_LONG).show();
                } else {
                    Integer oldCheeseCount = MyCheezApplication.getCurrentUser() != null ?
                            MyCheezApplication.getCurrentUser().getCheeseCount() : null;
                    CheeseCountChangeType cheeseCountChangeType = getChangeType(oldCheeseCount, cheeseCount);

                    /* null protect this. needed when we kill the app, then
                     * launch from push notification
                     */
                    if (MyCheezApplication.getCurrentUser() != null) {
                        MyCheezApplication.getCurrentUser().setCheeseCount(cheeseCount);
                    }

                    userViewAdapter.setCheeseCount(cheeseCount);
                    animationHandler.handleUserCheeseCountChanged(cheeseCountChangeType, userCheeseTextView);
                }
            }
        });
    }

    public CheeseCountChangeType getChangeType(Integer oldCheeseCount, Integer newCheeseCount){
        if (oldCheeseCount == null){
            return null;
        } else if (newCheeseCount >= oldCheeseCount){
            return CheeseCountChangeType.STEAL;
        } else if (newCheeseCount < oldCheeseCount){
            return CheeseCountChangeType.STOLEN;
        } else {
            return CheeseCountChangeType.NO_CHANGE;
        }
    }

	public void onCheeseTheft(View friendImageClicked, final User victim, ImageView movedCheeseImg){

		Log.i(TAG, "Victim is : " + victim);
    	/* display animation and start cheese theft async process */
		animationHandler.animateCheeseTheft(friendImageClicked, movedCheeseImg, userProfileImageView);

        // Performing actual steal...
        FirebaseProxy.doCheeseTheft(victim, new FirebaseProxy.CheeseTheftActionCallback() {
            @Override
            public void cheeseTheftPerformed(boolean isSuccess) {
                if (isSuccess) {
                    //Only update theft history, IF theft success...
                    FirebaseProxy.insertTheftHistory(victim.getFacebookId());
                }
            }
        });


	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "Theft is destroyed");
		MyCheezApplication.setActivityisStopping();
		super.onDestroy();
	}

    @Override
    public void onPause() {
		super.onPause();
		MyCheezApplication.getMyCheezFirebaseRef().getApp().goOffline();
    }


    @Override
    public void onStart(){
        super.onStart();
        AuthData authData = MyCheezApplication.getMyCheezFirebaseRef().getAuth();
        if(authData == null){
            Intent newIntent = this.getPackageManager().getLaunchIntentForPackage(this.getPackageName());
            this.startActivity(newIntent);
            finish();
        }else {
            MyCheezApplication.getMyCheezFirebaseRef().getApp().goOnline();
        }
    }

    public interface PlayerListScroller {

        void scrollPlayerList(String thiefId);

    }

}
