package com.example.Gallery;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class ImageInfo implements Parcelable {
	String Data;
	String Date;
	String Lat;
	String Lng;
	String Id;
	String Deg;

	public ImageInfo(String data, String lat, String lng, String id,
			String date, String deg) {
		this.Data = data;
		this.Lat = lat;
		this.Lng = lng;
		this.Id = id;
		this.Date = date;
		this.Deg = deg;
	}

	public ImageInfo(Parcel in) {
		// TODO Auto-generated constructor stub
		readFromParcel(in);

	}

	public String getData() {
		return Data;
	}

	public String getDate() {
		return Date;
	}

	public String getLat() {
		return Lat;
	}

	public String getLng() {
		return Lng;
	}

	public String getId() {
		return Id;
	}

	public String getDeg() {
		return Deg;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(Data);
		dest.writeString(Lat);
		dest.writeString(Lng);
		dest.writeString(Id);
		dest.writeString(Date);
		dest.writeString(Deg);
	}

	private void readFromParcel(Parcel in) {
		Data = in.readString();
		Lat = in.readString();
		Lng = in.readString();
		Id = in.readString();
		Date = in.readString();
		Deg = in.readString();
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public ImageInfo createFromParcel(Parcel in) {
			return new ImageInfo(in);
		}

		public ImageInfo[] newArray(int size) {
			return new ImageInfo[size];
		}
	};

}
