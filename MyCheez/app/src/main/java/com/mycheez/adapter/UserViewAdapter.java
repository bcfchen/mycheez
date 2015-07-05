//package com.mycheez.adapter;
//
//import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Transformation;
//import com.mycheez.application.MyCheezApplication;
//import com.mycheez.util.CircleTransform;
//import com.mycheez.viewmodel.PlayerViewModel;
//
//import android.app.Activity;
//import android.util.Log;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//public class UserViewAdapter
//{
//	TextView userCheeseTextView;
//	ImageView userProfileImageView;
//	Activity activity;
//
//	public UserViewAdapter(Activity activity, View userCheeseTextView, View userProfileImageView)
//	{
//		this.activity = activity;
//		this.userCheeseTextView = (TextView) userCheeseTextView;
//		this.userProfileImageView = (ImageView) userProfileImageView;
//	}
//
//	public void setCheeseCount(int cheeseCount)
//	{
//		userCheeseTextView.setText("x " + Integer.toString(cheeseCount));
//	}
//
//	public void setImageString(String imageString)
//	{
//        Transformation circleTransform = new CircleTransform();
//        try
//        {
//        	Picasso.with(activity).load(imageString)
//        	//.resize(userProfileImageView.getWidth(), userProfileImageView.getHeight())
//        	.fit()
//        	        	.centerCrop()
//        	//.transform(circleTransform)
//        	.into(userProfileImageView);
//        }
//        catch (Exception ex)
//        {
//        	Log.e(MyCheezApplication.LOG_TAG, "Cannot set user profile image. Error is: " + ex.toString());
//        }
//	}
//
//	public void setUser(PlayerViewModel user)
//	{
//		setCheeseCount(user.getCheese());
//		setImageString(user.getImageString());
//	}
//
//}
