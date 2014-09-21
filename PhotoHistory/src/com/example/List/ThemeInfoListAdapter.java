package com.example.List;

import java.util.List;

import com.example.Gallery.GalleryMain;
import com.example.photohistory.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ThemeInfoListAdapter extends ArrayAdapter<ThemeInfo> {
	private int resource;
	private LayoutInflater inflater;
	private Context context;
	private ListActivity mainAct;

	public ThemeInfoListAdapter(ListActivity mainAct, Context ctx,
			int resourceId, List<ThemeInfo> objects) {
		super(ctx, resourceId, objects);
		this.mainAct = mainAct;
		resource = resourceId;
		inflater = LayoutInflater.from(ctx);
		context = ctx;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		convertView = (RelativeLayout) inflater.inflate(resource, null);

		ThemeInfo tInfo = getItem(position);
		TextView tv_Name = (TextView) convertView.findViewById(R.id.tvName);
		tv_Name.setText(tInfo.getName());

		TextView tv_Memo = (TextView) convertView.findViewById(R.id.tvMemo);
		tv_Memo.setText(tInfo.getMemo());

		ImageView tImage = (ImageView) convertView.findViewById(R.id.Image);

		Button pbtn = (Button) convertView.findViewById(R.id.pup_btn);

		if (position == 0) {
			pbtn.setVisibility(View.INVISIBLE);
		}
		pbtn.setTag(position);// 리스트뷰 이벤트와 버튼이벤트를 따로 처리하기 위함
		pbtn.setFocusable(false);

		pbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				PopupMenu popup = new PopupMenu(context, v);
				MenuInflater inflater = popup.getMenuInflater();
				Menu menu = popup.getMenu();

				inflater.inflate(R.menu.popupmenu, menu);
				// popup.inflate(R.menu.popupmenutestmenu);
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						// TODO Auto-generated method stub
						switch (item.getItemId()) {
						case R.id.popup_reInfo:
							mainAct.ResetInfo(position);
							mainAct.UpdateList();
							break;
						case R.id.popup_upload:
							mainAct.addImage(position);
							mainAct.UpdateList();
							break;
						case R.id.popup_del:
							mainAct.delete(position);
							mainAct.UpdateList();
							break;
						}
						return false;
					}
				});
				popup.show();
			}

		});

		String rId = mainAct.readID.get(position);
		Log.i("tsts", "확인 pos:" + "" + position);
		if (rId == "")
			tImage.setImageResource(R.drawable.no_img);
		else
			tImage.setImageBitmap(mainAct.getBitmap(rId));

		return convertView;
	}
}
