package com.mycheez.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.URI;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


public class User implements Parcelable{
	private int cheeseCount;
	private Date createdAt;
	private String fName;
	private String[] friends;
	private Boolean isOnline;
	private String lName;
	private URI profilePicUrl;
	private Date updatedAt;
	private String facebookId;


	private User(Parcel in)
	{
		this.facebookId = in.readString();
		this.cheeseCount = in.readInt();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(facebookId);
		dest.writeInt(this.cheeseCount);
	}

	private void readFromParcel(Parcel in) {
		facebookId = in.readString();
		cheeseCount = in.readInt();
	}

	public static final Creator<User> CREATOR = new Creator<User>() {

		@Override
		public User createFromParcel(Parcel source) {
			return new User(source);
		}

		@Override
		public User[] newArray(int size) {
			return new User[size];
		}
	};

}
