package com.example.Detail;


import com.example.photohistory.*;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class CustomPager extends LinearLayout {
	Context mContext;
	ImageView iconImage;
	TextView describe;

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

		// inflate XML layout
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.image_viewpager, this, true);
		
		iconImage = (ImageView) findViewById(R.id.iconImage);
		describe = (TextView) findViewById(R.id.describe);
	}
	
	public void setImage(Bitmap bmp) {
		iconImage.setImageBitmap(bmp);
	}
	
	public void setText(String path){
		describe.setTextColor(Color.parseColor("#ffffff"));
		//describe.setText("data : " + path);
	}

}