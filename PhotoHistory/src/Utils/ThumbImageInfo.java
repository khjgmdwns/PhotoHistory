package Utils;

import android.util.Log;

public class ThumbImageInfo {
	private String id;
	private String des;
	private String data;
	private String lat;
	private String lng;
	private String date;
	private String deg;
	private String size;
	
	private boolean checkedState;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getlat() {
		return lat;
	}

	public void setlat(String lat) {
		this.lat = lat;
	}

	public String getlng() {
		return lng;
	}

	public void setlng(String lng) {
		this.lng = lng;
	}

	public String getDeg() {
		return deg;
	}

	public void setDeg(String deg) {
		this.deg = deg;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
	
	public boolean getCheckedState() {
		return checkedState;
	}

	public void setCheckedState(boolean checkedState) {
		this.checkedState = checkedState;
	}
}
