package com.example.Gallery;

import com.google.android.gms.internal.de;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class ImageInfo implements Parcelable {
	String Data;
	String Date;
	String Id;
	String Deg;
	String Size;
	Double Lat;
	Double Lng;

	public ImageInfo(String data, Double lat, Double lng, String id,
			String date, String deg, String size) {
		this.Data = data;
		this.Lat = lat;
		this.Lng = lng;
		this.Id = id;
		this.Date = date;
		this.Deg = deg;
		this.Size = size;
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

	public Double getLat() {
		return Lat;
	}

	public Double getLng() {
		return Lng;
	}

	public String getId() {
		return Id;
	}

	public String getDeg() {
		return Deg;
	}

	public String getSize() {
		return Size;
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
		dest.writeString(Id);
		dest.writeString(Date);
		dest.writeString(Deg);
		dest.writeString(Size);
		dest.writeDouble(Lat);
		dest.writeDouble(Lng);
	}

	private void readFromParcel(Parcel in) {
		Data = in.readString();
		Id = in.readString();
		Date = in.readString();
		Deg = in.readString();
		Size = in.readString();
		Lat = in.readDouble();
		Lng = in.readDouble();
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
