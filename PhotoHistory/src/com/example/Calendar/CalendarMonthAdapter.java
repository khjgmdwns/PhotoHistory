package com.example.Calendar;

import java.util.Calendar;

import android.content.Context;
import android.graphics.*;
import android.support.v7.internal.view.menu.MenuView.ItemView;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

// 출처 - Do it! 안드로이드 앱 프로그래밍 전면개정판(젤리빈 4.2.2판)

/**
 * 어댑터 객체 정의
 * 
 * @author Mike
 *
 */
public class CalendarMonthAdapter extends BaseAdapter {

	public static final String TAG = "CalendarMonthAdapter";
	
	Context mContext;
	
	public static int oddColor = Color.rgb(225, 225, 225);
	public static int headColor = Color.rgb(12, 32, 158);
	
	private int selectedPosition = -1;
	
	private MonthItem[] items;
	
	private int countColumn = 7;
	
	int mStartDay;
	int startDay;
	int curYear;
	int curMonth;
	
	int firstDay;
	int lastDay;
	
	int screenHeight;
	
	Calendar mCalendar;
	MonthItemView itemView;
	
	boolean recreateItems = false;
	
	public CalendarMonthAdapter(Context context) {
		super();

		mContext = context;
		
		init();
	}
	
	public CalendarMonthAdapter(Context context, AttributeSet attrs) {
		super();

		mContext = context;
		
		init();
	}

	private void init() {
		items = new MonthItem[7 * 6];

		mCalendar = Calendar.getInstance();
		recalculate();
		resetDayNumbers();
		
	}
	
	public void recalculate() {

		// set to the first day of the month
		mCalendar.set(Calendar.DAY_OF_MONTH, 1);
		
		// get week day
		int dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
		firstDay = getFirstDay(dayOfWeek);
		Log.d(TAG, "firstDay : " + firstDay);
		
		mStartDay = mCalendar.getFirstDayOfWeek();
		curYear = mCalendar.get(Calendar.YEAR);
		curMonth = mCalendar.get(Calendar.MONTH);
		lastDay = getMonthLastDay(curYear, curMonth);
		
		Log.d(TAG, "curYear : " + curYear + ", curMonth : " + curMonth + ", lastDay : " + lastDay);
		
		int diff = mStartDay - Calendar.SUNDAY - 1;
        startDay = getFirstDayOfWeek();
		Log.d(TAG, "mStartDay : " + mStartDay + ", startDay : " + startDay);
		
	}
	
	public void setPreviousMonth() {
		mCalendar.add(Calendar.MONTH, -1);
        recalculate();
        
        resetDayNumbers();
        selectedPosition = -1;
	}
	
	public void setNextMonth() {
		mCalendar.add(Calendar.MONTH, 1);
        recalculate();
        
        resetDayNumbers();
        selectedPosition = -1;
	}
	
	public void resetDayNumbers() {
		for (int i = 0; i < 42; i++) {
			// calculate day number
			int dayNumber = (i+1) - firstDay;
			if (dayNumber < 1 || dayNumber > lastDay) {
				dayNumber = 0;
			}
			
	        // save as a data item
	        items[i] = new MonthItem(dayNumber);
		}
	}
	
	private int getFirstDay(int dayOfWeek) {
		int result = 0;
		if (dayOfWeek == Calendar.SUNDAY) {
			result = 0;
		} else if (dayOfWeek == Calendar.MONDAY) {
			result = 1;
		} else if (dayOfWeek == Calendar.TUESDAY) {
			result = 2;
		} else if (dayOfWeek == Calendar.WEDNESDAY) {
			result = 3;
		} else if (dayOfWeek == Calendar.THURSDAY) {
			result = 4;
		} else if (dayOfWeek == Calendar.FRIDAY) {
			result = 5;
		} else if (dayOfWeek == Calendar.SATURDAY) {
			result = 6;
		}
		
		return result;
	}
	
	
	public int getCurYear() {
		return curYear;
	}
	
	public int getCurMonth() {
		return curMonth;
	}
	
	
	public int getNumColumns() {
		return 7;
	}

	public int getCount() {
		return 7 * 6;
	}

	public Object getItem(int position) {
		return items[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.d(TAG, "getView(" + position + ") called.");

		itemView = new MonthItemView(mContext, getCurYear(), getCurMonth());
		
		// create a params
		GridView.LayoutParams params = new GridView.LayoutParams(
				GridView.LayoutParams.MATCH_PARENT,
				getScreenHeight()/6);
		
		// calculate row and column
		int rowIndex = position / countColumn;
		int columnIndex = position % countColumn;
		
		//Log.d(TAG, "Index : " + rowIndex + ", " + columnIndex);

		// set item data and properties
		itemView.setItem(items[position]);
		itemView.setLayoutParams(params);
		itemView.setPadding(0, 0, 0, 0);
		
		// set properties
		itemView.setGravity(Gravity.LEFT);
		
		if (columnIndex == 0) {
			itemView.setTextColor(Color.RED);
		} else if (columnIndex == 6) {
			itemView.setTextColor(Color.BLUE);
		} else {
			itemView.setTextColor(Color.BLACK);
		}
		
		// set background color
		if (position == getSelectedPosition()) {
        	itemView.setBackgroundColor(Color.YELLOW);
        } else {
        	itemView.setBackgroundColor(Color.WHITE);
        }
        

        
        
		return itemView;
	}

	
    /**
     * Get first day of week as android.text.format.Time constant.
     * @return the first day of week in android.text.format.Time
     */
    public static int getFirstDayOfWeek() {
        int startDay = Calendar.getInstance().getFirstDayOfWeek();
        if (startDay == Calendar.SATURDAY) {
            return Time.SATURDAY;
        } else if (startDay == Calendar.MONDAY) {
            return Time.MONDAY;
        } else {
            return Time.SUNDAY;
        }
    }
 
	
    /**
     * get day count for each month
     * 
     * @param year
     * @param month
     * @return
     */
    private int getMonthLastDay(int year, int month){
    	switch (month) {
 	   		case 0:
      		case 2:
      		case 4:
      		case 6:
      		case 7:
      		case 9:
      		case 11:
      			return (31);

      		case 3:
      		case 5:
      		case 8:
      		case 10:
      			return (30);

      		default:
      			if(((year%4==0)&&(year%100!=0)) || (year%400==0) ) {
      				return (29);   // 2월 윤년계산
      			} else { 
      				return (28);
      			}
 	   	}
 	}
    
    
    
	
	
	
	
	
	/**
	 * set selected row
	 * 
	 * @param selectedRow
	 */
	public void setSelectedPosition(int selectedPosition) {
		this.selectedPosition = selectedPosition;
	}

	/**
	 * get selected row
	 * 
	 * @return
	 */
	public int getSelectedPosition() {
		return selectedPosition;
	}
	public void setScreenHeight(int screenHeight){//화면 크기 세팅
		this.screenHeight = screenHeight-30;
	}
	public int getScreenHeight(){//화면 크기 가져오기
		return screenHeight;
	}
	public Bitmap getSelectedImage(int position){
		Log.i("getSelectedImage()", "ok");
		return itemView.getImage(position);
	}
}
