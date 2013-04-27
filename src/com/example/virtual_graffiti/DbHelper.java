package com.example.virtual_graffiti;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.ArrayAdapter;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class DbHelper {
	
	Handler ui;
	Context mContext;
	List<String> mMsgList;
	ArrayAdapter<String> mAdapter;
	
	public void initialize(Context c, Intent i, Handler uiHandler, List<String> list, ArrayAdapter<String> a) {
		ui = uiHandler;
		mContext = c;
		mMsgList = list;
		mAdapter = a;
	}
	
	public boolean push(String msg, double lat, double lng) {
		// get user's coordinates
		ParseGeoPoint geoPoint = new ParseGeoPoint();
		
		geoPoint.setLatitude(lat);
		geoPoint.setLongitude(lng);
		
		// Ignore if the input is blank
		if (msg.length() == 0) return true;
		
		// create and store geomessage
		ParseObject geoMessage = new ParseObject("GeoMessage");
		geoMessage.put("geoPoint", geoPoint);
		geoMessage.put("message", msg);
		geoMessage.saveInBackground();
		
		return true;
	}
	
	public void pull(LocationInfo loc) {
		ParseGeoPoint geoPoint = new ParseGeoPoint();
		geoPoint.setLatitude(loc.lastLat);
		geoPoint.setLongitude(loc.lastLong);
		
		// Get messages within 50 meters of the current location
		ParseQuery query = new ParseQuery("GeoMessage");
		query.whereWithinKilometers("geoPoint", geoPoint, 0.05);
		query.findInBackground(new FindCallback() {
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) {
					mMsgList.clear();
					for (ParseObject msg : objects) {
						mMsgList.add(msg.getString("message"));
					}
					mAdapter.notifyDataSetChanged();
				} else {
					displayError(e.getMessage());
				}
			}
		});
	}
	private void displayError(final String msg) {
		ui.post(new Runnable() {
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("Error");
				builder.setMessage(msg);
				builder.setNeutralButton("Close", null);
				
				AlertDialog errDialog = builder.create();
				errDialog.show();
			}
		});
	}
	
	
}
