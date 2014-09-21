package Utils;

import java.io.*;
import java.util.*;

import android.content.*;
import android.location.*;
import android.widget.*;

import com.google.android.gms.maps.model.*;

public class Geocoding {
	String sLocationInfo = "";
	Context context;

	public Geocoding(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	public String getAddress(LatLng latlng) {
		try {
			Geocoder gc = new Geocoder(context, Locale.KOREA);
			List<Address> addresses = gc.getFromLocation(latlng.latitude,
					latlng.longitude, 1);
			if (addresses != null) {
				Address addr = addresses.get(0);
				sLocationInfo = String.format("%s %s %s", addr.getAdminArea(), addr.getLocality(), addr.getThoroughfare());

			}// end if
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			sLocationInfo = "위치를 받아올 수 없음";
		}

		return sLocationInfo;
	}
}