package com.example.Detail;

import java.util.ArrayList;
import com.example.Gallery.ImageInfo;
import com.example.photohistory.MainActivity;
import com.example.photohistory.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.support.v4.view.*;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class DetailMain extends Activity{
	private ArrayList<ImageInfo> mImageInfoList;
	private Bitmap bmp;
	private CustomPager page;
	TextView page_count;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_detail);
		setTitle("사진보기");
		
		mImageInfoList = new ArrayList<ImageInfo>();

		Intent i = getIntent();
		mImageInfoList = i.getParcelableArrayListExtra("Detail");
		
		
		ViewPager pager = (ViewPager) this.findViewById(R.id.pager);
		ViewPagerAdapter adapter = new ViewPagerAdapter(this, mImageInfoList);
        pager.setAdapter(adapter);
        
        page_count = (TextView) this.findViewById(R.id.page_count);
		page_count.setText("1/" + mImageInfoList.size());
		
		pager.setOnPageChangeListener(new OnPageChangeListener() { //아이템 변경 시 이벤트
			@Override
			public void onPageSelected(int position) {
				page_count.setText(Integer.toString(position+1) + "/" + mImageInfoList.size());// (현재 페이지 / 전체 페이지 수) 출력
			}
			@Override
			public void onPageScrollStateChanged(int position) {}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}			
		});   
	}
	
	//PagerAdapter Inner Class
	public class ViewPagerAdapter extends PagerAdapter {
		// sample image resource ids
		static final int VISIBLE = 0x00000000;
		static final int INVISIBLE = 0x00000004;
		ArrayList<ImageInfo> tImageInfoList;
		private Context mContext;

		/**
		 * Initialize
		 *
		 * @param context
		 */
		public ViewPagerAdapter(Context c, ArrayList<ImageInfo> tImageInfoList) {
			mContext = c;	
			this.tImageInfoList = tImageInfoList;
		}

		//총 페이지 수
		public int getCount() {
			return tImageInfoList.size();
		}

		//페이지 생성 및 표시
		public Object instantiateItem(View pager, int position) {
			// create a instance of the page and set data

			page = new CustomPager(mContext);
			
			String path = mImageInfoList.get(position).getData();

			BitmapFactory.Options option = new BitmapFactory.Options();

			option.inPurgeable = true;
			option.inSampleSize = 2;
			option.inTempStorage =  new byte[16*1024];
			option.inJustDecodeBounds = false;
			
			bmp = BitmapFactory.decodeFile(path, option);
			
			String tmpDeg = new String();
			tmpDeg = mImageInfoList.get(position).getDeg();

			if(tmpDeg == null){
				Log.i("gpstest", "null을 반환했음");
				tmpDeg = "0";
			}
			
			page.setImage(MainActivity.GetRotatedBitmap(bmp,
					Integer.valueOf(tmpDeg)));
			page.setText(path);

			page.setVisibility(VISIBLE);
			setProgressBarIndeterminateVisibility(false);
			
			// add to the ViewPager
			ViewPager curPager = (ViewPager) pager;
			curPager.addView(page);

			return page;			
		}

		//메모리 반환
		public void destroyItem(View pager, int position, Object view) {
			((ViewPager)pager).removeView((CustomPager)view);
			System.gc();
			Log.i("destroyItem()", "destroyItem() 호출");
		}
		
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}
		
		public void finishUpdate( View view ) {}
		public void restoreState( Parcelable p, ClassLoader c ) {}
		
		public Parcelable saveState() {
			return null;
		}
		
		public void startUpdate( View view ) {}
	}
}
