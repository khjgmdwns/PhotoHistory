package com.example.Detail;

import com.example.photohistory.*;
import com.google.android.gms.maps.model.LatLng;

import Utils.Geocoding;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class CustomPager extends LinearLayout {
	Context mContext;
	Geocoding geo;
	ImageView iconImage;
	TextView date, size, addr;
	Double Lat, Lng;

	public CustomPager(Context context) {
		super(context);

		init(context);
	}

	public CustomPager(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	private void init(Context context) {
		mContext = context;
		geo = new Geocoding(mContext);

		// inflate XML layout
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.image_viewpager, this, true);

		iconImage = (ImageView) findViewById(R.id.iconImage);
		date = (TextView) findViewById(R.id.photho_date);
		size = (TextView) findViewById(R.id.photho_size);
		addr = (TextView) findViewById(R.id.photho_addr);
	}

	public void setImage(Bitmap bmp) {
		iconImage.setImageBitmap(bmp);
	}

	public void setText() {
		date.setTextColor(Color.parseColor("#ffffff"));
		size.setTextColor(Color.parseColor("#ffffff"));
		addr.setTextColor(Color.parseColor("#ffffff"));
	}

	public void setAddr() {
		addr.setText("위치:"+geo.getAddress(new LatLng(Lat, Lng)));
	}

	public void setDate(String path) {
		date.setText("날짜: "+path);
	}

	public void setSize(String path) {
		Double db = Double.valueOf(path)/100000;
		size.setText("크기: "+""+db+"Mb");
	}

	public void setLat(Double path) {
		Lat = path;
	}

	public void setLng(Double path) {
		Lng = path;
	}

}