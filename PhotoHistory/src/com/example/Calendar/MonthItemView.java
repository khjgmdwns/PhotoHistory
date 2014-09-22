package com.example.Calendar;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.example.photohistory.*;
import com.google.android.gms.internal.co;

import Utils.ThumbImageInfo;
import android.R.drawable;
import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.media.ExifInterface;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.format.Time;
import android.util.*;
import android.view.*;
import android.widget.*;

/**
 * 일자에 표시하는 텍스트뷰 정의
 * 
 * @author Mike
 */
public class MonthItemView extends LinearLayout {
	static final int VISIBLE = 0x00000000;
	static final int INVISIBLE = 0x00000004;
	String tmpID;
	String tmpDEG;
	String tmpPATH;
	TextView dayText;
	ImageView calendarThumb;	
	Context context;
	int year, month;
	private MonthItem item;

	String[] projection = { MediaStore.Images.Media._ID,
			MediaStore.Images.Media.DATE_TAKEN,
			MediaStore.Images.Media.ORIENTATION,
			MediaStore.Images.Media.DATA};// 테마필드

	public MonthItemView(Context context, int year, int month) {
		super(context);
		this.context = context;
		this.year = year;
		this.month = month;
		init(context);
	}

	public MonthItemView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	private void init(Context context) {
		setBackgroundColor(Color.WHITE);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.calendar_monthview, this, true);

		dayText = (TextView) findViewById(R.id.dayText);
		calendarThumb = (ImageView) findViewById(R.id.calendarThumb);
	}

	public MonthItem getItem() {
		return item;
	}

	public boolean findDayImage(int day) {

		String where = MediaStore.Images.Media.DATE_TAKEN + " >="
				+ getThisMonthInMillis(year, month, day, 0) + " AND "
				+ MediaStore.Images.Media.DATE_TAKEN + " < "
				+ getThisMonthInMillis(year, month, day, 23);

		Cursor cr = context.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
				where, null, MediaStore.Images.Media.DATE_ADDED + " desc ");
		
		if (cr != null && cr.getCount() > 0) {
			//Log.i("cal556", "cursor 사이즈 " + "" + cr.getCount());
			int IDCol = cr.getColumnIndex(MediaStore.Images.Media._ID);
			int ORCol = cr.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
			int PATHCol = cr.getColumnIndex(MediaStore.Images.Media.DATA);
			cr.moveToPosition(0);
			tmpID = cr.getString(IDCol);
			tmpDEG = cr.getString(ORCol);
			tmpPATH = cr.getString(PATHCol);
			if(tmpDEG==null){
				tmpDEG = "0";
			}
			Log.i("tmp", "tmp : " + tmpID + " / " + tmpDEG + " / " + tmpPATH);
			cr.close();
			return true;
		} else {
			//Log.i("cal556", "cursor 사이즈 0.");
			cr.close();
			return false;
		}
	}

	private long getThisMonthInMillis(int year, int month, int day, int hour) {

		//Log.i("cal556", "" + year + "" + month + "" + day);
		Calendar searchCalendar = Calendar.getInstance();
		searchCalendar.set(Calendar.YEAR, year);		
		searchCalendar.set(Calendar.MONTH, month);
		searchCalendar.set(Calendar.DAY_OF_MONTH, day);
		searchCalendar.set(Calendar.HOUR_OF_DAY, hour);
		return searchCalendar.getTimeInMillis();
	}

	public void setItem(MonthItem item) {
		this.item = item;

		int day = item.getDay();
		if (day != 0) {
			dayText.setText(String.valueOf(day));

			if (findDayImage(day)) {
				int int_id = Integer.valueOf(tmpID);// 형변환
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inDither = false;
				options.inSampleSize = 4;

				Bitmap bmp = MediaStore.Images.Thumbnails.getThumbnail(
						// 섬네일 이미지를 가져와서 저장
						context.getContentResolver(), int_id,
						MediaStore.Images.Thumbnails.MICRO_KIND, options);
				calendarThumb.setImageBitmap(MainActivity.GetRotatedBitmap(bmp,Integer.valueOf(tmpDEG)));
				calendarThumb.setVisibility(VISIBLE);

			} else
				calendarThumb.setVisibility(INVISIBLE);

		} else {
			dayText.setText("");
			//calendarThumb.setVisibility(INVISIBLE);
		}

	}

	public void setTextColor(int color) {
		dayText.setTextColor(color);
	}
	
	public Bitmap getImage(int day){
		//해당 날짜의 이미지를 Bitmap으로 가져옴
		Bitmap bmp;
		if (findDayImage(day)) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDither = false;
			options.inSampleSize = 4;
			options.inPurgeable = true;
			options.inJustDecodeBounds = false;
			options.inTempStorage = new byte[16 * 1024];
			
			/*int int_id = Integer.valueOf(tmpID);// 형변환
			bmp = MediaStore.Images.Thumbnails.getThumbnail(
				// 섬네일 이미지를 가져와서 저장
				context.getContentResolver(), int_id,
				MediaStore.Images.Thumbnails.MICRO_KIND, options);*/
			
			bmp = BitmapFactory.decodeFile(tmpPATH, options);
			bmp = MainActivity.GetRotatedBitmap(bmp,Integer.valueOf(tmpDEG));
			
			return bmp;
		}
		else{
			return null;
		}
	}
}
