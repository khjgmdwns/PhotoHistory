package com.example.List;

import java.util.ArrayList;
import com.example.Gallery.GalleryMain;
import com.example.photohistory.MainActivity;
import com.example.photohistory.R;
import Utils.ImageViewHolder;
import Utils.ThumbImageInfo;
import android.app.*;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class AddThemeImages extends Activity implements
		GridView.OnItemClickListener {
	String ThemeName;
	ProgressDialog mLoagindDialog; // 다이얼로그
	GridView mGvImageList; // 섬네일 이미지 뷰
	ImageAdapter mListAdapter; // 체크이미지 뷰
	ImageAdapter adapter; // 체크이미지 어뎁터
	ArrayList<ThumbImageInfo> mThumbImageInfoList; // 섬네일에 대한 정보 <id,
													// bmp,checkedState>
	ArrayList<String> CheckList; // 체크된 박스목록 임시저장공간
	ArrayList<String> CheckedDes; // 체크된 이미지 Description 저장공간

	Cursor TmpCursor;// CheckList 커서

	// 다이얼로그 부분
	private static final int DIALOG_SINGLE_CHOICE = 0;

	// Select 하고자 하는 컬럼
	String[] projection = { MediaStore.Images.Media._ID, // ID, 식별자
			MediaStore.Images.Media.DATA,// 이미지정보, 이미지 경로
			MediaStore.Images.Media.LATITUDE,// 위도
			MediaStore.Images.Media.LONGITUDE,// 경도
			MediaStore.Images.Media.DESCRIPTION,
			MediaStore.Images.Media.ORIENTATION};// 테마필드
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
		actionBar.setTitle("[" + ThemeName + "] 사진추가");

		mThumbImageInfoList = new ArrayList<ThumbImageInfo>();
		mGvImageList = (GridView) findViewById(R.id.gvImageList);
		mGvImageList.setOnItemClickListener(this);
		new DoFindImageList().execute(); // 이미지로딩 관련 작업

		setButton();
	}

	public void setButton() {
		final ToggleButton tBtn = (ToggleButton) findViewById(R.id.selectAll);
		Button okBtn = (Button) findViewById(R.id.btnSelect);
		okBtn.setText("추가");
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
				if (CheckCheck()) { // 체크박스 확인
					RegisterTheme(ThemeName);
					finish();
					Intent in = new Intent(AddThemeImages.this,
							GalleryMain.class);
					in.putExtra("TNAME", ThemeName);
					startActivity(in);
				} else
					Toast.makeText(getBaseContext(), "선택된 이미지가 없음",
							Toast.LENGTH_SHORT).show();
			}
		});
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

	private boolean CheckCheck() {
		boolean NoneChecked = false;// false = 체크된 항목이 하나도 없을 경우
		CheckList = new ArrayList<String>();// 체크된 이미지의 id가 저장될 공간
		CheckedDes = new ArrayList<String>();// 체크된 이미지의 description이 저장될 공간

		for (int i = 0; i < mThumbImageInfoList.size(); i++) {
			if (mThumbImageInfoList.get(i).getCheckedState()) {// 체크 on이면
				CheckList.add(mThumbImageInfoList.get(i).getId());
				CheckedDes.add(mThumbImageInfoList.get(i).getDes());

				NoneChecked = true;
			}
		}

		if (!NoneChecked)
			return false;

		Checked_ID = CheckList.toArray(new String[CheckList.size()]);// query
		// 를위해서
		// 배열로
		// 변환

		// SelectionArgs 사용이 안되서 하드코딩하기 위한 부분
		String t = Checked_ID[0];

		for (int i = 1; i < Checked_ID.length; i++) {
			t = t + "," + Checked_ID[i];
		}// id 넘김

		TmpCursor = getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
				"_id in (" + t + ")", null, null);

		Log.i("ts2", "생존");

		return true;
	}

	private long findThumbList() {// 이미지 받아오는부분
		long returnValue = 0;

		Cursor imageCursor;

		imageCursor = getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
				null, MediaStore.Images.Media.DATE_ADDED + " desc ");

		if (imageCursor != null && imageCursor.getCount() > 0) {
			// 컬럼 인덱스
			int imageIDCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media._ID);
			int imageDesCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media.DESCRIPTION);
			int imageDegCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
			// 커서에서 이미지의 ID와 경로명을 가져와서 ThumbImageInfo 모델 클래스를 생성해서
			// 리스트에 더해준다.
			while (imageCursor.moveToNext()) {
				ThumbImageInfo thumbInfo = new ThumbImageInfo();

				thumbInfo.setId(imageCursor.getString(imageIDCol));// 커서로 받아온 Id
				thumbInfo.setDes(imageCursor.getString(imageDesCol));
				thumbInfo.setCheckedState(false);// 체크상태 초기화

				mThumbImageInfoList.add(thumbInfo);
				returnValue++;
				Log.i("ts2", "" + returnValue);
			}
		}
		imageCursor.close();
		return returnValue;
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
		adapter = (ImageAdapter) arg0.getAdapter();
		ThumbImageInfo rowData = (ThumbImageInfo) adapter.getItem(position);
		boolean curCheckState = rowData.getCheckedState();

		rowData.setCheckedState(!curCheckState);

		mThumbImageInfoList.set(position, rowData);
		adapter.notifyDataSetChanged();
	}

	private class ImageAdapter extends BaseAdapter {
		static final int VISIBLE = 0x00000000;
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
			if (convertView == null) {
				convertView = mLiInflater.inflate(mCellLayout, parent, false);
				ImageViewHolder holder = new ImageViewHolder();

				holder.ivImage = (ImageView) convertView
						.findViewById(R.id.ivImage);
				holder.chkImage = (CheckBox) convertView
						.findViewById(R.id.chkImage);

				DisplayMetrics displayMetrics = new DisplayMetrics();
				Display display = getWindowManager().getDefaultDisplay();
				display.getMetrics(displayMetrics);
				int width = (displayMetrics.widthPixels / 3)
						- (displayMetrics.widthPixels / 50);
				int height = (displayMetrics.widthPixels / 4)
						+ (displayMetrics.widthPixels / 20);
				convertView.setLayoutParams(new GridView.LayoutParams(width,
						height));

				convertView.setTag(holder);
			}

			final ImageViewHolder holder = (ImageViewHolder) convertView
					.getTag();

			if (((ThumbImageInfo) mThumbImageInfoList.get(position))
					.getCheckedState()) {
				holder.chkImage.setChecked(true);
			} else
				holder.chkImage.setChecked(false);

			int id = Integer.valueOf(mThumbImageInfoList.get(position).getId());// 형변환

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDither = false;
			options.inSampleSize = 4;

			Bitmap bmp = MediaStore.Images.Thumbnails.getThumbnail(
					// 섬네일 이미지를 가져와서 저장
					getApplicationContext().getContentResolver(), id,
					MediaStore.Images.Thumbnails.MICRO_KIND, options);//

			holder.ivImage.setVisibility(VISIBLE);

			setProgressBarIndeterminateVisibility(false);


			String tmpDeg = new String();
			tmpDeg = mThumbImageInfoList.get(position).getDeg();

			if(tmpDeg == null){
				Log.i("gpstest", "null을 반환했음");
				tmpDeg = "0";
			}
			
			holder.ivImage.setImageBitmap(MainActivity.GetRotatedBitmap(bmp,
					Integer.valueOf(tmpDeg)));
			return convertView;
		}
	}

	private class DoFindImageList extends AsyncTask<String, Integer, Long> {// 이미지
																			// 로딩관련
		@Override
		protected void onPreExecute() {
			mLoagindDialog = ProgressDialog.show(AddThemeImages.this, null,
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
	public void onBackPressed() {
		this.finish();
	}

}