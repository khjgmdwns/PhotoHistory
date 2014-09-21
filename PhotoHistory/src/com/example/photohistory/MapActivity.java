package com.example.photohistory;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.example.Gallery.ImageInfo;
import com.example.photohistory.R;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.internal.go;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.model.internal.f;
import Utils.ImageViewHolder;
import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.*;
import android.location.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class MapActivity extends FragmentActivity implements LocationListener {
	Marker cMarker;
	private boolean first = true;
	private GoogleMap googleMap;
	private LocationManager locationManager;// 위치 매니저
	private String provider;
	private ArrayList<ImageInfo> mImageInfoList;
	private PolylineOptions po;
	private Polyline pl;
	private ArrayList<Marker> markers;
	boolean locationTag = true; // 위치 한번만 가져오기 위해

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork()
				.penaltyLog().build());

		mImageInfoList = new ArrayList<ImageInfo>();
		Intent in = getIntent();
		mImageInfoList = in.getParcelableArrayListExtra("Map");
		setContentView(R.layout.map_main);
		markers = new ArrayList<Marker>();
		init();

	}

	private Double ConvertFormat(int type, String a) {
		String str= "";
		
		if (type == 0)
			str = a.replace("GPSLatitude : ", "");
		else if (type == 1)
			str = a.replace("GPSLongitude : ", "");
		
		Log.i("theme", str);
		StringTokenizer Token = new StringTokenizer(str, ",");
		
		String Do;
		String Bun;
		String Cho;
				
		Do = Token.nextToken();
		StringTokenizer tokenDo = new StringTokenizer(Do, "/");
		
		Bun = Token.nextToken();
		StringTokenizer tokenBun = new StringTokenizer(Bun, "/");
		
		Cho = Token.nextToken();
		StringTokenizer tokenCho = new StringTokenizer(Cho, "/");
		Double res= 0.;
		
		res = Double.valueOf(tokenDo.nextToken()) / Double.valueOf(tokenDo.nextToken());
		res += Double.valueOf(tokenBun.nextToken()) / Double.valueOf(tokenBun.nextToken()) * 60;
		res += Double.valueOf(tokenCho.nextToken()) / Double.valueOf(tokenCho.nextToken()) *3600;
		
		Log.i("theme", ""+res);
		
		return res;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {// 위치설정
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 0:
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			provider = locationManager.getBestProvider(criteria, true);

			if (provider != null) { // 사용자가 위치설정 동의 했을때
				locationManager.requestLocationUpdates(provider, 1L, 2F,
						MapActivity.this);
				setUpMapIfNeeded();
			}
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);// 위치정보 업데이트중단
	}

	void init() {
		GooglePlayServicesUtil.isGooglePlayServicesAvailable(MapActivity.this);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, true);

		if (provider == null) { // 위치정보 설정이 안되어 있으면 설정하는 엑티비티로 이동합니다
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

			dialogBuilder.setTitle("위치서비스 동의");
			dialogBuilder.setMessage("위치서비스 사용을 동의합니까?");
			dialogBuilder.setNegativeButton("예",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivityForResult(
									new Intent(
											android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
									0);
						}
					});
			dialogBuilder.setPositiveButton("아니오", null);

			dialogBuilder.show();
		}

		else { // 위치 정보 설정이 되어 있으면 현재위치를 받아옵니다
			locationManager.requestLocationUpdates(provider, 1, 1,
					MapActivity.this);
			setUpMapIfNeeded();
		}

	}

	private void setUpMapIfNeeded() {
		if (googleMap == null)// 구글맵 인스턴스가 null인 경우
			googleMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();// xml과 연결

		if (googleMap != null)
			setUpMap();// 맵 생성

	}

	private void setUpMap() {// 맵을 생성하는 부분
		googleMap.setMyLocationEnabled(true);
		googleMap.getMyLocation();
		po = new PolylineOptions();
		po.color(Color.RED);
		for (int i = 0; i < mImageInfoList.size(); i++) {
			if ((mImageInfoList.get(i).getLat() != null)
					&& !mImageInfoList.get(i).getLat()
							.contains("GPSLatitude : null")) {
				Double a = ConvertFormat(0, mImageInfoList.get(i).getLat());
				Double b = ConvertFormat(1, mImageInfoList.get(i).getLng());

				po.add(new LatLng(a, b));

				addMarker(a, b, mImageInfoList.get(i).getId(), mImageInfoList
						.get(i).getDate(), mImageInfoList.get(i).getDeg());
			}
		}

		LatLng StartPoint = new LatLng(36.738884, 127.902832);// 시작위치

		if (first) {
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
					StartPoint, 6.5f));
			first = false;

		}

		googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			public boolean onMarkerClick(Marker marker) {
				if (cMarker != null)
					cMarker.setIcon(BitmapDescriptorFactory
							.fromResource(R.drawable.green2));

				cMarker = marker;
				marker.showInfoWindow();
				marker.setIcon(BitmapDescriptorFactory
						.fromResource(R.drawable.pk));
				return false;
			}
		});

		googleMap.setOnMapClickListener(new OnMapClickListener() {

			public void onMapClick(LatLng point) {
				if (cMarker != null)
					cMarker.setIcon(BitmapDescriptorFactory
							.fromResource(R.drawable.green2));
			}
		});
		googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				// TODO Auto-generated method stub

				if (LoadImageCheck(marker.getPosition().latitude,
						marker.getPosition().longitude))// 로드뷰 이미지가 존재하면
					ShowStreetView(marker.getPosition().latitude,
							marker.getPosition().longitude);// 로드뷰 실행
				else
					Toast.makeText(getBaseContext(), "로드뷰 이미지가 존재하지 않는 지역입니다.",
							Toast.LENGTH_SHORT).show();
			}
		});

		googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {

			// Use default InfoWindow frame
			@Override
			public View getInfoWindow(Marker arg0) {
				return null;
			}

			// Defines the contents of the InfoWindow
			@Override
			public View getInfoContents(Marker arg0) {

				// Getting view from the layout file info_window_layout
				int deg = 0;

				View v = getLayoutInflater()
						.inflate(R.layout.marker_info, null);

				ImageView img = (ImageView) v.findViewById(R.id.imageView1);

				TextView addr = (TextView) v.findViewById(R.id.tv_addr);
				TextView date = (TextView) v.findViewById(R.id.tv1);

				StringTokenizer st = new StringTokenizer(arg0.getSnippet(), "*");
				while (st.hasMoreTokens()) {
					date.setText(st.nextToken());
					deg = Integer.valueOf(st.nextToken());
				}

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inDither = false;
				options.inSampleSize = 4;

				int id = Integer.valueOf(arg0.getTitle());// 형변환
				Bitmap bmp = MediaStore.Images.Thumbnails.getThumbnail(
						// 섬네일 이미지를 가져와서 저장
						getApplicationContext().getContentResolver(), id,
						MediaStore.Images.Thumbnails.MICRO_KIND, options);

				img.setImageBitmap(MainActivity.GetRotatedBitmap(bmp, deg));

				return v;

			}
		});

	}

	@Override
	public void onBackPressed() {
		this.finish();
	}

	private void addMarker(Double lat, Double lng, String MakerName,
			String date, String deg) {
		// TODO Auto-generated method stub
		Marker mk = googleMap.addMarker(new MarkerOptions()
				.position(new LatLng(lat, lng))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.green2))
				.title(MakerName).snippet(date + "*" + deg));

		markers.add(mk);
	}

	void setAnimate(Double lat, Double lng) {
		googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,
				lng)));
		LatLng PYPoint = new LatLng(lat, lng);
		googleMap.animateCamera(CameraUpdateFactory
				.newLatLngZoom(PYPoint, 6.5f));// 확대축소비율
	}

	void setMove(Double lat, Double lng) { // 효과없이 카메라 이동, 일단 안씀
		googleMap.moveCamera(CameraUpdateFactory
				.newLatLng(new LatLng(lat, lng)));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		switch (item.getItemId()) {
		case R.id.addline:
			float[] results = new float[3];
			Location location = new Location("start");
			location.distanceBetween(39.03922, 125.76252, 35.1717241,
					129.0718241, results);

			String msg = String.format("%.2f", results[0] / 1000);
			Toast.makeText(getBaseContext(), "거리: " + "16.5km",
					Toast.LENGTH_SHORT).show();
			pl = googleMap.addPolyline(po);
			pl.setWidth(5.0f);

			return true;
		}
		/*
		 * googleMap.setMapType(2);// 위성
		 * 
		 * googleMap.setMapType(3);// 지형
		 * 
		 * } else if (id == 8) { float[] results = new float[3]; Location
		 * location = new Location("start"); location.distanceBetween(39.03922,
		 * 125.76252, 35.1717241, 129.0718241, results);
		 * 
		 * String msg = String.format("%.2f", results[0] / 1000);
		 * Toast.makeText(getBaseContext(), "거리: " + msg + "km",
		 * Toast.LENGTH_SHORT).show(); return true; } else if (id == 9) {
		 * 
		 * pl = googleMap.addPolyline(po); pl.setWidth(5.0f);
		 * 
		 * return true; }
		 */
		return super.onOptionsItemSelected(item);
	}

	private void ShowStreetView(double lat, double lng) {// 스트리트뷰

		Intent streetViewActivity = new Intent(
				android.content.Intent.ACTION_VIEW,
				Uri.parse("google.streetview:cbll=" + lat + "," + lng
						+ "&cbp=1,90,,1,-5.27&mz=21"));
		startActivity(streetViewActivity);
	}

	public boolean LoadImageCheck(double lat, double lng) {
		String url = "http://maps.google.com/cbk?output=xml&ll=" + lat + ","
				+ lng;
		String panoID = "";
		try {
			InputStream is = (InputStream) new URL(url).getContent();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(is, "utf-8");
			int eventType = xpp.getEventType();

			boolean isItemTag = false;

			String tagName = "";

			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					tagName = xpp.getName();

					if (tagName.equals("data_properties")) {
						panoID = xpp.getAttributeValue(null, "pano_id");

						Log.i("dubug",
								"xpp.getAttributeValue: "
										+ xpp.getAttributeValue(null, "pano_id"));
						break;
					}

				} else if (eventType == XmlPullParser.TEXT) {

				} else if (eventType == XmlPullParser.END_TAG) {

				}

				eventType = xpp.next();
			}
		}

		catch (Exception e) {
			Log.i("dubug", "안됨");
		}

		url = "http://cbk0.google.com/cbk?output=tile&panoid=" + panoID
				+ "&zoom=3&x=1&y=1";

		try {
			InputStream is = (InputStream) new URL(url).getContent();

			Drawable d = Drawable.createFromStream(is, "src name");

			Log.d("dubug", "img Sucess");
			Log.d("dubug", "url: " + url.toString());
			Log.d("dubug", "is: " + is.toString());

			return true;
		} catch (Exception e) {
			Log.d("dubug", "img failed" + e.toString());
			return false;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if (locationTag) {// 한번만 위치를 가져오기 위해서 tag
			Log.d("myLog", "onLocationChanged: !!" + "onLocationChanged!!");

			double lat = location.getLatitude();
			double lng = location.getLongitude();

			Toast.makeText(MapActivity.this, "위도: " + lat + " 경도: " + lng,
					Toast.LENGTH_SHORT).show();
			locationTag = false;
		}// end if
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

}