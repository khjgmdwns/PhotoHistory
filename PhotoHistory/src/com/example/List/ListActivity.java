package com.example.List;

import java.util.ArrayList;
import java.util.List;
import com.example.Gallery.GalleryMain;
import com.example.photohistory.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ListActivity extends Activity {
	private List<ThemeInfo> tList;
	private ListView listViewThemeInfo;
	private ThemeInfoListAdapter tilAdapter;
	private Context ctx;
	private List<String> readDes;
	public List<String> readID;
	public List<String> readDeg;
	
	String[] projection = { MediaStore.Images.Media._ID,
			MediaStore.Images.Media.DESCRIPTION,
			MediaStore.Images.Media.ORIENTATION};// 테마필드

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_activity);
		ctx = this;

		tList = new ArrayList<ThemeInfo>();

		SharedPreferences pref = getSharedPreferences("pref",
				Activity.MODE_PRIVATE);
		int size = pref.getInt("Size", 0);

		tList.add(new ThemeInfo("All", "전체사진"));

		for (int i = 1; i < size; i++) {
			String a = pref.getString("Name" + "" + i, "");
			String b = pref.getString("tMemos" + "" + i, "");

			tList.add(new ThemeInfo(a, b));
		}

		UpdateList();

		listViewThemeInfo = (ListView) findViewById(R.id.theme_list);
		tilAdapter = new ThemeInfoListAdapter(this, ctx, R.layout.theme_row,
				tList);
		listViewThemeInfo.setAdapter(tilAdapter);

		// Click event for single list row
		listViewThemeInfo.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ThemeInfo o = (ThemeInfo) parent.getItemAtPosition(position);

				Intent in = new Intent(ListActivity.this, GalleryMain.class);

				in.putExtra("TNAME", o.getName());
				startActivity(in);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		UpdateList();
		
		tilAdapter.notifyDataSetChanged();
	}

	public void setOtherID(String tname) {
		String selection = Images.Media.DESCRIPTION + " like ?";
		String[] selectionArgs = { "%*" + tname + "*%" };

		Cursor Cursor = getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
				selection, selectionArgs,
				MediaStore.Images.Media.DATE_ADDED + " desc ");
		
		int IDCol = Cursor.getColumnIndex(MediaStore.Images.Media._ID);
		int DEGCol = Cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
		if (Cursor.getCount() > 0) {
			
			Cursor.moveToPosition(0);
			readID.add(Cursor.getString(IDCol));
			
		} else {
			readID.add("");
		}
		Cursor.close();

	}

	public void setFirstID() {
		readID = new ArrayList<String>();// 초기화
		readDeg = new ArrayList<String>();
		
		Cursor allCursor = getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
				null, MediaStore.Images.Media.DATE_ADDED + " desc ");
		int allIDCol = allCursor.getColumnIndex(MediaStore.Images.Media._ID);
		int allDEGCol = allCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
		allCursor.moveToPosition(0);
		readID.add(allCursor.getString(allIDCol));
		readDeg.add(allCursor.getString(allDEGCol));
		
		allCursor.close();
	}

	private void findTheme(String tName) {// 테마필드 검색

		Cursor Cursor;

		String selection = Images.Media.DESCRIPTION + " like ?";
		String[] selectionArgs = { "%*" + tName + "*%" };

		readID = new ArrayList<String>();
		readDes = new ArrayList<String>();
		
		Cursor = getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
				selection, selectionArgs,
				MediaStore.Images.Media.DATE_ADDED + " desc ");

		if (Cursor != null && Cursor.getCount() > 0) {
			// 컬럼 인덱스
			int IDCol = Cursor.getColumnIndex(MediaStore.Images.Media._ID);
			int DesCol = Cursor
					.getColumnIndex(MediaStore.Images.Media.DESCRIPTION);

			// id = Integer.valueOf(IDCol);// 형변환

			// 커서에서 이미지의 ID와 경로명을 가져와서 ThumbImageInfo 모델 클래스를 생성해서
			// 리스트에 더해준다.
			while (Cursor.moveToNext()) {

				readID.add(Cursor.getString(IDCol));
				readDes.add(Cursor.getString(DesCol));

			}
		}

	}

	private void ReNameTheme(String bName, String aName) {
		ContentResolver resolver = getContentResolver();
		ContentValues content = new ContentValues(1);

		for (int i = 0; i < readID.size(); i++) {

			String selection = Images.Media._ID + " = ?";
			String[] selectionArgs = { readID.get(i) };

			String str = readDes.get(i).replace("*" + bName + "*", aName); // *테마명*
																			// 를
																			// 지워준다

			content.put(MediaStore.Images.Media.DESCRIPTION, str);
			int rs = resolver.update(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content,
					selection, selectionArgs);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		SharedPreferences pref = getSharedPreferences("pref",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putInt("Size", tList.size());

		for (int i = 0; i < tList.size(); i++) {
			editor.putString("Name" + "" + i, tList.get(i).getName());
			editor.putString("Memo" + "" + i, tList.get(i).getMemo());
		}
		editor.commit();
	}

	public boolean onCreateOptionsMenu(Menu menu) {// 액션바 설정
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_ac, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {// 앤션바 클릭 이벤트

		switch (item.getItemId()) {

		case R.id.addTheme: // 기존테마
			final LinearLayout linear = (LinearLayout) View.inflate(this,
					R.layout.theme_add, null);

			Builder ad = new AlertDialog.Builder(this);

			ad.setTitle("테마 정보를 입력하세요.")
					.setView(linear)
					.setPositiveButton("확인",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									EditText et_tName = (EditText) linear
											.findViewById(R.id.tName);
									EditText et_tMemo = (EditText) linear
											.findViewById(R.id.tMemo);

									@SuppressWarnings("unused")
									boolean check = false;
									for (int i = 0; i < tList.size(); i++) {
										Log.i("Ltest", "" + i);
										if (et_tName.getText().toString()
												.equals(tList.get(i).getName())) {// 테마
											// 이름중복
											check = true;
											Log.i("Ltest", "중복");
											break;
										}
									}
									if (check)// 테마이름중복
										Toast.makeText(getBaseContext(),
												"생성 실패: 테마이름 중복",
												Toast.LENGTH_SHORT).show();
									else if (et_tName.getText().toString()
											.equals("")) {
										// 테마명을 입력하지 않은경우
										Toast.makeText(getBaseContext(),
												"생성 실패: 테마이름 미입력",
												Toast.LENGTH_SHORT).show();
									}

									// 정상입력의 경우
									else {
										tList.add(new ThemeInfo(et_tName
												.getText().toString(), et_tMemo
												.getText().toString()));
										
										UpdateList();
										tilAdapter.notifyDataSetChanged();
									}

								}
							})
					.setNegativeButton("취소",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).show();

			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	public void ResetInfo(final int position) {

		final String bName = tList.get(position).getName();// 변경전 테마이름
		final String bMemo = tList.get(position).getMemo();
		final LinearLayout linear = (LinearLayout) View.inflate(this,
				R.layout.theme_add, null);

		final EditText et_tName = (EditText) linear.findViewById(R.id.tName);
		final EditText et_tMemo = (EditText) linear.findViewById(R.id.tMemo);

		et_tName.setText(bName);
		et_tMemo.setText(bMemo);
		Builder ad = new AlertDialog.Builder(this);

		ad.setTitle("테마 정보를 수정하세요.")
				.setView(linear)
				.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						@SuppressWarnings("unused")
						boolean check = false;
						for (int i = 0; i < tList.size(); i++) {
							Log.i("Ltest", "" + i);
							if (et_tName.getText().toString()
									.equals(tList.get(i).getName())) {// 테마 중복				
									if(!tList.get(i).getName().equals(bName))
										check = true;
								Log.i("Ltest", "중복");
								break;
							}
						}
						if (check)// 테마이름중복
							Toast.makeText(getBaseContext(), "생성 실패: 테마이름 중복",
									Toast.LENGTH_SHORT).show();
						else if (et_tName.getText().toString().equals("")) {
							// 테마명을 입력하지 않은경우
							Toast.makeText(getBaseContext(), "생성 실패: 테마이름 미입력",
									Toast.LENGTH_SHORT).show();
						}

						// 정상입력의 경우
						else {
							tList.get(position).setName(
									et_tName.getText().toString());
							tList.get(position).setMemo(
									et_tMemo.getText().toString());
							tilAdapter.notifyDataSetChanged();

							findTheme(bName);
							ReNameTheme(bName, "*"
									+ et_tName.getText().toString() + "*");
							UpdateList();
						}

					}
				})
				.setNegativeButton("취소", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).show();

	}

	public void delete(int position) {
		String str = tList.get(position).getName();
		findTheme(str);
		ReNameTheme(str, "");

		tList.remove(position);
		tilAdapter.notifyDataSetChanged();

	}

	public void addImage(int position) {
		Intent in = new Intent(ListActivity.this, AddThemeImages.class);
		in.putExtra("TNAME", tList.get(position).getName());
		startActivity(in);
	}

	public Bitmap getBitmap(String id) {

		int int_id = Integer.valueOf(id);// 형변환

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = false;
		options.inSampleSize = 4;

		Bitmap bmp = MediaStore.Images.Thumbnails.getThumbnail(
				// 섬네일 이미지를 가져와서 저장
				getApplicationContext().getContentResolver(), int_id,
				MediaStore.Images.Thumbnails.MICRO_KIND, options);
		System.gc();
		return bmp;
	}
	public void UpdateList(){
		setFirstID();
		for (int i = 1; i < tList.size(); i++)
			setOtherID(tList.get(i).getName());
	}
}