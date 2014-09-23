package com.example.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.List.ThemeInfo;
import com.example.photohistory.R;
import com.google.android.gms.internal.fi;

/**
 * 그리드뷰를 이용해 월별 캘린더를 만드는 방법에 대해 알 수 있습니다.
 * 
 * @author Mike
 */
public class CalendarActivity extends Activity {

	/**
	 * 월별 캘린더 뷰 객체
	 */
	CalendarMonthView monthView;

	/**
	 * 월별 캘린더 어댑터
	 */
	CalendarMonthAdapter monthViewAdapter;

	/**
	 * 월을 표시하는 텍스트뷰
	 */
	TextView monthText;

	/**
	 * 현재 연도
	 */
	int curYear;

	/**
	 * 현재 월
	 */
	int curMonth;

	/**
	 * 클릭 이벤트 이미지
	 */
	ImageView selectedThumb;
	TextView memo;
	TextView date;
	SharedPreferences pref;
	int selectedDay = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.calendar_main);

		pref = getSharedPreferences("diary", Activity.MODE_PRIVATE);
	}

	/**
	 * 월 표시 텍스트 설정
	 */
	private void setMonthText() {
		curYear = monthViewAdapter.getCurYear();
		curMonth = monthViewAdapter.getCurMonth();

		monthText.setText(curYear + "년 " + (curMonth + 1) + "월");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*
	 * @Override protected void onStop() { super.onStop();
	 * 
	 * SharedPreferences pref = getSharedPreferences("pref",
	 * Activity.MODE_PRIVATE); SharedPreferences.Editor editor = pref.edit();
	 * 
	 * editor.putInt("Size", tList.size());
	 * 
	 * for (int i = 0; i < tList.size(); i++) { editor.putString("Name" + "" +
	 * i, tList.get(i).getName()); editor.putString("Memo" + "" + i,
	 * tList.get(i).getMemo()); } editor.commit(); }
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// 월별 캘린더 뷰 객체 참조
		monthView = (CalendarMonthView) findViewById(R.id.monthView);
		int height = monthView.getHeight();
		Log.i("높이", "" + height);
		monthViewAdapter = new CalendarMonthAdapter(this);
		monthViewAdapter.setScreenHeight(height);
		monthView.setAdapter(monthViewAdapter);

		selectedThumb = (ImageView) findViewById(R.id.selectedThumb);
		memo = (TextView) findViewById(R.id.textView3);
		date = (TextView) findViewById(R.id.textView2);

		selectedThumb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(getBaseContext(), "이미지 확인", Toast.LENGTH_LONG)
						.show();
			}
		});

		memo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				final LinearLayout linear = (LinearLayout) View.inflate(
						CalendarActivity.this, R.layout.memo_add, null);

				Builder ad = new AlertDialog.Builder(CalendarActivity.this);

				ad.setTitle("메모를 입력하세요.")
						.setView(linear)
						.setPositiveButton("확인",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										
										EditText inputM = (EditText) linear
												.findViewById(R.id.inputMemo);
										
										
										SharedPreferences.Editor editor = pref
												.edit();
										String str = new String();
										str = inputM.getText().toString();

										if (selectedDay != 0) {// 있는날짜의경우
											editor.putString("" + curYear + ""
													+ (curMonth + 1) + "월 "
													+ "" + selectedDay + "일",
													str);// 영구적 저장 (키, 값)
											
											editor.commit();// 커밋
											memo.setText(str);
										} else
											Toast.makeText(getBaseContext(),
													"날짜를 선택하세요.",
													Toast.LENGTH_SHORT).show();
										
									}
								})
						.setNegativeButton("취소",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
									}
								}).show();

			}
		});

		// 리스너 설정
		monthView.setOnDataSelectionListener(new OnDataSelectionListener() {
			public void onDataSelected(AdapterView parent, View v,
					int position, long id) {
				// 현재 선택한 일자 정보 표시
				MonthItem curItem = (MonthItem) monthViewAdapter
						.getItem(position);
				selectedDay = curItem.getDay();
				Bitmap bmp;
				bmp = monthViewAdapter.getSelectedImage(selectedDay);
				selectedThumb.setImageBitmap(bmp); // 이미지 변경

				// 날짜 변경
				if (selectedDay != 0) {// 있는 날짜의 경우
					date.setVisibility(0);
					date.setText("" + (curMonth + 1) + "월 " + "" + selectedDay
							+ "일");

					String readMemo = pref.getString("" + curYear + ""
							+ (curMonth + 1) + "월 " + "" + selectedDay + "일",
							"");

					if (readMemo != null)
						memo.setText(readMemo);
				} else
					date.setVisibility(1);

			}
		});

		monthText = (TextView) findViewById(R.id.monthText);
		setMonthText();

		// 이전 월로 넘어가는 이벤트 처리
		Button monthPrevious = (Button) findViewById(R.id.monthPrevious);
		monthPrevious.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				monthViewAdapter.setPreviousMonth();
				monthViewAdapter.notifyDataSetChanged();

				setMonthText();
			}
		});

		// 다음 월로 넘어가는 이벤트 처리
		Button monthNext = (Button) findViewById(R.id.monthNext);
		monthNext.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				monthViewAdapter.setNextMonth();
				monthViewAdapter.notifyDataSetChanged();

				setMonthText();
			}
		});
		Log.i("onWindowFocusChanged", "호출");
	}
}
