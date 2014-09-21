package com.example.Gallery;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import com.example.Detail.DetailMain;
import com.example.photohistory.MainActivity;
import com.example.photohistory.MapActivity;
import com.example.photohistory.R;
import com.google.android.gms.maps.model.LatLng;
import Utils.ImageViewHolder;
import Utils.ThumbImageInfo;
import android.app.*;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.*;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;

public class GalleryMain extends Activity implements
		GridView.OnItemClickListener, ListView.OnScrollListener {
	boolean mBusy = false;
	int width, height;
	String ThemeName;
	boolean existChecked = false; // 체크된버튼이 하나라도 있을경우 true
	boolean GPSbtn = false;
	ProgressDialog mLoagindDialog; // 다이얼로그
	GridView mGvImageList; // 섬네일 이미지 뷰
	ImageAdapter mListAdapter; // 체크이미지 뷰
	
	ArrayList<ThumbImageInfo> mThumbImageInfoList; // 섬네일에 대한 정보 <id,
													// bmp,checkedState>

	ArrayList<String> CheckList; // 체크된 박스목록 임시저장공간
	ArrayList<String> CheckedPath; // 체크된 이미지 경로 저장공간
	ArrayList<String> CheckedDes; // 체크된 이미지 Description 저장공간
	ArrayList<ImageInfo> mImageInfoList; // ImageInfo 저장공간

	String[] tList;// 테마 리스트
	Cursor TmpCursor;// CheckList 커서

	Builder ad;// 기존 테마리스트 빌더
	// 다이얼로그 부분
	private static final int DIALOG_SINGLE_CHOICE = 0;
	private static final int DIALOG_DLETE = 1;

	// Select 하고자 하는 컬럼
	String[] projection = {
			MediaStore.Images.Media._ID, // ID, 식별자
			MediaStore.Images.Media.DATE_TAKEN,
			MediaStore.Images.Media.DATA,// 이미지정보,
											// 이미지
											// 경로
			MediaStore.Images.Media.LATITUDE,// 위도
			MediaStore.Images.Media.ORIENTATION,
			MediaStore.Images.Media.DESCRIPTION };// 테마필드
	
	String[] Checked_ID;// 체크된 id리스트를 배열로 변환하기위한 공간

	int WhichButton = 0;// 리스트 목록의 눌려진 번호

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_list_view);

		ThemeName = new String();

		Intent i = getIntent();
		ThemeName = i.getStringExtra("TNAME");

		ActionBar actionBar = getActionBar();
		actionBar.setTitle(ThemeName);

		mThumbImageInfoList = new ArrayList<ThumbImageInfo>();
		mGvImageList = (GridView) findViewById(R.id.gvImageList);
		mGvImageList.setOnItemClickListener(this);
		mGvImageList.setOnScrollListener(this);
		new DoFindImageList().execute(); // 이미지로딩 관련 작업

		DisplayMetrics displayMetrics = new DisplayMetrics();
		Display display = getWindowManager().getDefaultDisplay();
		display.getMetrics(displayMetrics);
		width = (displayMetrics.widthPixels / 3)
				- (displayMetrics.widthPixels / 50);
		height = (displayMetrics.widthPixels / 4)
				+ (displayMetrics.widthPixels / 20);
		
		setButton();

	}

	public void setButton() {
		final ToggleButton tBtn = (ToggleButton) findViewById(R.id.selectAll);
		Button okBtn = (Button) findViewById(R.id.btnSelect);

		tBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (tBtn.isChecked()) {
					for (int i = 0; i < mThumbImageInfoList.size(); i++) {
						mThumbImageInfoList.get(i).setCheckedState(true);
						mListAdapter.notifyDataSetChanged();
					}
				} else {
					for (int i = 0; i < mThumbImageInfoList.size(); i++) {
						mThumbImageInfoList.get(i).setCheckedState(false);
						mListAdapter.notifyDataSetChanged();
					}
				}

			}
		});

		okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (CheckCheck(0)) { // 체크박스 확인
					DetailCheckedImage();
				} else
					Toast.makeText(getBaseContext(), "선택된 이미지가 없음",
							Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void DetailCheckedImage() {
		Intent in = new Intent(this, DetailMain.class);
		in.putParcelableArrayListExtra("Detail", mImageInfoList);
		startActivity(in);
	}

	public void deleteCheckedImage() {// 파일 삭제 메소드

		File file = null;
		for (int i = 0; i < CheckedPath.size(); i++) {
			file = new File(CheckedPath.get(i));

			if (file.exists()) {// 파일이 존재하면 삭제
				file.delete();
				// 물리적인 파일이 삭제되었지만 ContentProvider가 목록은 유지하고 있기 때문에 갤러리 상에는 빈
				// 썸네일이 표시 됨
				// 따라서 Provider 목록의 해당 이미지 컬럼까지 삭제 함
				String selection = Images.Media.DATA + " = ?";
				String[] selectionArgs = { CheckedPath.get(i) };
				int count2 = getContentResolver().delete(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						selection, selectionArgs);
				Log.i("Delete", file.getPath() + " 파일 삭제 완료");
			}
		}
	}

	public void RegisterTheme(String tList) {

		ContentResolver resolver = getContentResolver();
		ContentValues content = new ContentValues(1);

		for (int i = 0; i < CheckList.size(); i++) {

			String selection = Images.Media._ID + " = ?";
			String[] selectionArgs = { CheckList.get(i) };

			String str = CheckedDes.get(i) + "*" + tList + "*"; // *는 구분자
			// 가나 테마와 가나다 테마가있을경우 like 연산으로 가려낼수 없기 때문에 *를 추가하여 저장
			content.put(MediaStore.Images.Media.DESCRIPTION, str);
			int rs = resolver.update(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content,
					selection, selectionArgs);
		}
		Log.i("theme", "테마등록완료");
	}

	private boolean CheckCheck(int sel) {
		boolean NoneChecked = false;// false = 체크된 항목이 하나도 없을 경우

		switch (sel) {
		case 0:// 확인, 마커(상세정보)
			mImageInfoList = new ArrayList<ImageInfo>();

			for (int i = 0; i < mThumbImageInfoList.size(); i++) {
				if (mThumbImageInfoList.get(i).getCheckedState()) {// 체크 on이면
					mImageInfoList.add(new ImageInfo(mThumbImageInfoList.get(i)
							.getData(), mThumbImageInfoList.get(i).getlat(),
							mThumbImageInfoList.get(i).getlng(),
							mThumbImageInfoList.get(i).getId(),
							mThumbImageInfoList.get(i).getDate(),
							mThumbImageInfoList.get(i).getDeg()));
					NoneChecked = true;
				}
			}

			break;
		case 1:// 테마등록(id)
			CheckList = new ArrayList<String>();// 체크된 이미지의 id가 저장될 공간
			CheckedDes = new ArrayList<String>();// 체크된 이미지의 description이 저장될 공간
			for (int i = 0; i < mThumbImageInfoList.size(); i++) {
				if (mThumbImageInfoList.get(i).getCheckedState()) {// 체크 on이면
					CheckList.add(mThumbImageInfoList.get(i).getId());
					CheckedDes.add(mThumbImageInfoList.get(i).getDes());
					NoneChecked = true;
				}
			}
			break;
		case 2: // 삭제(경로)
			CheckedPath = new ArrayList<String>();// 파일경로 저장공간
			for (int i = 0; i < mThumbImageInfoList.size(); i++) {
				if (mThumbImageInfoList.get(i).getCheckedState()) {// 체크 on이면
					CheckedPath.add(mThumbImageInfoList.get(i).getData());
					NoneChecked = true;
				}
			}
			break;
		}

		if (!NoneChecked)
			return false;

		return true;
	}

	private long findThumbList() {// 이미지 받아오는부분
		long returnValue = 0;

		Cursor imageCursor;

		if (ThemeName.equals("All")) {
			imageCursor = getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
					null, null, MediaStore.Images.Media.DATE_ADDED + " desc ");
		}

		else {
			Log.i("ts2", ThemeName);
			String selection = Images.Media.DESCRIPTION + " like ?";
			String[] selectionArgs = { "%*" + ThemeName + "*%" };

			imageCursor = getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
					selection, selectionArgs,
					MediaStore.Images.Media.DATE_ADDED + " desc ");
		}

		if (imageCursor != null && imageCursor.getCount() > 0) {
			// 컬럼 인덱스
			int imageIDCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media._ID);
			int imageDesCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media.DESCRIPTION);
			int imageDataCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media.DATA);
			int imageLatCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media.LATITUDE);
			int imageDateCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
			int imageDegCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media.ORIENTATION);

			// 커서에서 이미지의 ID와 경로명을 가져와서 ThumbImageInfo 모델 클래스를 생성해서
			// 리스트에 더해준다.
			while (imageCursor.moveToNext()) {
				ThumbImageInfo thumbInfo = new ThumbImageInfo();

				thumbInfo.setId(imageCursor.getString(imageIDCol));// 커서로 받아온 Id
				thumbInfo.setDes(imageCursor.getString(imageDesCol));
				thumbInfo.setData(imageCursor.getString(imageDataCol));

				Calendar cal = new GregorianCalendar();
				Date d = new Date();
				cal.setTimeInMillis(Long.valueOf(imageCursor
						.getString(imageDateCol)));
				d = cal.getTime();
				DateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd");
				String temp = sdFormat.format(d);

				thumbInfo.setDate(temp);
				thumbInfo.setlat(imageCursor.getString(imageLatCol));
				thumbInfo.setCheckedState(false);// 체크상태 초기화
				thumbInfo.setDeg(imageCursor.getString(imageDegCol));
				mThumbImageInfoList.add(thumbInfo);

				returnValue++;
				Log.i("ts2", "" + returnValue);
			}
		}

		imageCursor.close();

		for (int i = 0; i < mThumbImageInfoList.size(); i++) {
			if (mThumbImageInfoList.get(i).getlat() != null)
				try {
					ExifInterface exif = new ExifInterface(mThumbImageInfoList
							.get(i).getData());
					String tmplatitude = getTagString(
							ExifInterface.TAG_GPS_LATITUDE, exif);
					String tmplongtude = getTagString(
							ExifInterface.TAG_GPS_LONGITUDE, exif);

					mThumbImageInfoList.get(i).setlat(tmplatitude);
					mThumbImageInfoList.get(i).setlng(tmplongtude);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return returnValue;
	}

	private String getTagString(String tag, ExifInterface exif) {
		// TODO Auto-generated method stub
		return (tag + " : " + exif.getAttribute(tag) + "\n");
	}

	private void ViewGPS() {// 위치정보 사진만 보기

		for (int i = 0; i < mThumbImageInfoList.size(); i++) {
			if ((mThumbImageInfoList.get(i).getlat() == null)
					|| mThumbImageInfoList.get(i).getlat()
							.contains("GPSLatitude : null")) {
				mThumbImageInfoList.remove(i);
				i--;
			}
		}

	}

	// 화면에 이미지들을 뿌려준다.
	private void updateUI() {
		mListAdapter = new ImageAdapter(this, R.layout.image_cell,
				mThumbImageInfoList);
		mGvImageList.setAdapter(mListAdapter);
	}

	// 아이템 체크시 현재 체크상태를 가져와서 반대로 변경(true -> false, false -> true)시키고
	// 그 결과를 다시 ArrayList의 같은 위치에 담아준다
	// 그리고 어댑터의 notifyDataSetChanged() 메서드를 호출하면 리스트가 현재 보이는
	// 부분의 화면을 다시 그리기 시작하는데(getView 호출) 이러면서 변경된 체크상태를
	// 파악하여 체크박스에 체크/언체크를 처리한다.
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		ImageAdapter adapter = (ImageAdapter) arg0.getAdapter();
	    ThumbImageInfo rowData = (ThumbImageInfo)adapter.getItem(position);
	    boolean curCheckState = rowData.getCheckedState();
	    
	    rowData.setCheckedState(!curCheckState);
	    
	    mThumbImageInfoList.get(position).setCheckedState(!curCheckState);
	    adapter.notifyDataSetChanged();
	    Log.i("in402", "확인1");
	}

	private class ImageAdapter extends BaseAdapter {
		static final int VISIBLE = 0x00000000;
		static final int INVISIBLE = 0x00000004;
		private Context mContext;
		private int mCellLayout;
		private LayoutInflater mLiInflater;
		private ArrayList<ThumbImageInfo> mThumbImageInfoList;

		@SuppressWarnings("unchecked")
		public ImageAdapter(Context c, int cellLayout,
				ArrayList<ThumbImageInfo> thumbImageInfoList) {
			mContext = c;
			mCellLayout = cellLayout;
			mThumbImageInfoList = thumbImageInfoList;
			mLiInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		public int getCount() {
			return mThumbImageInfoList.size();
		}

		public Object getItem(int position) {
			return mThumbImageInfoList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		@SuppressWarnings("unchecked")
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageViewHolder holder = new ImageViewHolder();

			if (convertView == null) {
				convertView = mLiInflater.inflate(mCellLayout, parent, false);

				holder.ivImage = (ImageView) convertView
						.findViewById(R.id.ivImage);
				holder.chkImage = (CheckBox) convertView
						.findViewById(R.id.chkImage);
				holder.gpsImage = (ImageView) convertView
						.findViewById(R.id.gpsimage);
	
				convertView.setLayoutParams(new GridView.LayoutParams(width,
						height));

				convertView.setTag(holder);
			} else {
				holder = (ImageViewHolder) convertView.getTag();
			}

			if (((ThumbImageInfo) mThumbImageInfoList.get(position))
					.getCheckedState()) {
				holder.chkImage.setChecked(true);
				existChecked = true;
			} else
				holder.chkImage.setChecked(false);

			String tmp = new String();
			tmp = mThumbImageInfoList.get(position).getlat();

			if (tmp == null) {
				holder.gpsImage.setVisibility(INVISIBLE);

			}

			else if ((tmp.contains("GPSLatitude : null"))) {
				holder.gpsImage.setVisibility(INVISIBLE);
			} else {
				holder.gpsImage.setVisibility(VISIBLE);
			}

			if (!mBusy) {
				try {

					int id = Integer.valueOf(mThumbImageInfoList.get(position)
							.getId());// 형변환

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inDither = false;
					options.inSampleSize = 4;
					options.inPurgeable = true;
					options.inJustDecodeBounds = false;
					options.inTempStorage = new byte[16 * 1024];

					Bitmap bmp = MediaStore.Images.Thumbnails.getThumbnail(
							// 섬네일 이미지를 가져와서 저장
							getApplicationContext().getContentResolver(), id,
							MediaStore.Images.Thumbnails.MICRO_KIND, options);

					holder.ivImage.setVisibility(VISIBLE);
					setProgressBarIndeterminateVisibility(false);

					String tmpDeg = new String();
					tmpDeg = mThumbImageInfoList.get(position).getDeg();

					if (tmpDeg == null) {
						tmpDeg = "0";
					}

					holder.ivImage.setImageBitmap(MainActivity
							.GetRotatedBitmap(bmp, Integer.valueOf(tmpDeg)));

				} catch (Exception e) {
					e.printStackTrace();
					setProgressBarIndeterminateVisibility(false);
				}
			} else {
				setProgressBarIndeterminateVisibility(true);
				holder.ivImage.setVisibility(INVISIBLE);
			}

			return convertView;
		}
	}

	private class DoFindImageList extends AsyncTask<String, Integer, Long> {// 이미지
																			// 로딩관련
		@Override
		protected void onPreExecute() {
			mLoagindDialog = ProgressDialog.show(GalleryMain.this, null,
					"로딩중...", true, true);
			super.onPreExecute();
		}

		@Override
		protected Long doInBackground(String... arg0) {
			long returnValue = 0;
			returnValue = findThumbList();
			return returnValue;
		}

		@Override
		protected void onPostExecute(Long result) {
			updateUI();
			mLoagindDialog.dismiss();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gallerymenu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {// 메뉴 클릭 이벤트

		switch (item.getItemId()) {
		case R.id.view_gps:

			if (!GPSbtn) {
				ViewGPS();
				mListAdapter.notifyDataSetChanged();
				GPSbtn = true;
			} else {
				mThumbImageInfoList.clear();
				findThumbList();
				mListAdapter.notifyDataSetChanged();
				GPSbtn = false;
			}

			return true;
		case R.id.theme: // 테마
			if (CheckCheck(1)) { // 체크박스 확인
				showDialog(DIALOG_SINGLE_CHOICE);
				Log.i("in404", "쇼 다이얼로그 호출");
			} else
				Toast.makeText(getBaseContext(), "선택된 이미지가 없음",
						Toast.LENGTH_SHORT).show();
			return true;

		case R.id.marker:
			if (CheckCheck(0)) { // 체크박스 확인
				Intent in = new Intent(this, MapActivity.class);
				in.putParcelableArrayListExtra("Map", mImageInfoList);
				startActivity(in);
			} else
				Toast.makeText(getBaseContext(), "선택된 이미지가 없음",
						Toast.LENGTH_SHORT).show();

			return true;

		case R.id.del:
			if (CheckCheck(2)) { // 체크박스 확인
				if (ThemeName.equals("All"))
					showDialog(DIALOG_DLETE);
				else
					RemoveImage();
			} else
				Toast.makeText(getBaseContext(), "선택된 이미지가 없음",
						Toast.LENGTH_SHORT).show();

			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	public void readList() {

		SharedPreferences pref = getSharedPreferences("pref",
				Activity.MODE_PRIVATE);
		int size = pref.getInt("Size", 0);

		tList = new String[size - 1];

		for (int i = 0; i < size - 1; i++) {
			String a = pref.getString("Name" + "" + (i + 1), "");// all 테마는 목록에서
																	// 나타내지 않기위함
			tList[i] = a;
		}
	}

	private void RemoveImage() {
		ContentResolver resolver = getContentResolver();
		ContentValues content = new ContentValues(1);

		for (int i = 0; i < CheckList.size(); i++) {

			String selection = Images.Media._ID + " = ?";
			String[] selectionArgs = { CheckList.get(i) };

			String str = CheckList.get(i).replace("*" + ThemeName + "*", ""); // *테마명*
																				// 를
																				// 지워준다

			content.put(MediaStore.Images.Media.DESCRIPTION, str);
			int rs = resolver.update(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content,
					selection, selectionArgs);
		}

		// 화면 업데이트하는 부분
		mThumbImageInfoList.clear();
		// // 리스트 비우기
		findThumbList();// 이미지 다시 채우고
		mListAdapter.notifyDataSetChanged(); // 어댑터에 변경알림
		Toast.makeText(getBaseContext(), "목록에서 제거됨", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {

		switch (id) {
		case DIALOG_SINGLE_CHOICE:
			removeDialog(DIALOG_SINGLE_CHOICE);
			break;
		}
		super.onPrepareDialog(id, dialog);
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_SINGLE_CHOICE:
			readList();
			ad = new AlertDialog.Builder(GalleryMain.this);
			ad.setTitle("테마목록 선택")
					.setSingleChoiceItems(tList, 0,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									WhichButton = whichButton;
								}
							})
					.setPositiveButton("확인",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									Toast.makeText(getBaseContext(), "테마등록 완료",
											Toast.LENGTH_SHORT).show();
									/* User clicked Yes so do some stuff */
									RegisterTheme(tList[WhichButton]);
								}
							})
					.setNegativeButton("취소",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									/* User clicked No so do some stuff */
								}
							});
			return ad.create();

		case DIALOG_DLETE:
			ad = new AlertDialog.Builder(GalleryMain.this);
			ad.setTitle("사진을 삭제하겠습니까?")
					.setPositiveButton("확인",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									deleteCheckedImage();

									// 화면 업데이트하는 부분
									mThumbImageInfoList.clear();
									// // 리스트 비우기
									findThumbList();// 이미지 다시 채우고
									mListAdapter.notifyDataSetChanged(); // 어댑터에
																			// 변경알림
									Toast.makeText(getBaseContext(), "삭제완료",
											Toast.LENGTH_SHORT).show();

								}
							})
					.setNegativeButton("취소",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							}).setMessage("확인 버튼을 누르면 사진파일이 삭제됩니다.");
			return ad.create();

		}// end switch
		return null;
	}

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}

	// 스크롤 상태를 판단한다.
	// 스크롤 상태가 IDLE 인 경우(mBusy == false)에만 이미지 어댑터의 getView에서
	// 이미지들을 출력한다.
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_IDLE:
			mBusy = false;
			mListAdapter.notifyDataSetChanged();
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			mBusy = true;
			break;
		case OnScrollListener.SCROLL_STATE_FLING:
			mBusy = true;
			break;
		}
	}

	@Override
	public void onBackPressed() {
		this.finish();
	}

}