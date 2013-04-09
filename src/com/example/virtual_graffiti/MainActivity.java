package com.example.virtual_graffiti;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MainActivity extends Activity {

	private ListView msgListView;
	private List<String> msgList = new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Parse stuff
		Parse.initialize(this, "f09jNI2OB82MUvz0iHP8X8ZkKFgjnsC03IGHW240", "s5BkfxQRp75PUNjlKRIew7XieumzS0CcZMBdeIwF");
		ParseAnalytics.trackAppOpened(getIntent());
		
		// Location lib stuff
		LocationLibrary.initialiseLibrary(getBaseContext(), "com.example.virtual-graffiti");
		
		// set up the msgListView
		msgListView = (ListView)findViewById(R.id.listViewMessages);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, msgList);
		msgListView.setAdapter(adapter);
		
		// Start a new thread to get the messages
		new Thread(new Runnable() {
			public void run() {
				getMessages();
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void submitMessage(View view) {
		// get user's coordinates
		ParseGeoPoint geoPoint = new ParseGeoPoint();
		LocationInfo latestInfo = new LocationInfo(getBaseContext());
		geoPoint.setLatitude(latestInfo.lastLat);
		geoPoint.setLongitude(latestInfo.lastLong);
		TextView tvLatLng = (TextView)findViewById(R.id.textViewLatLng);
		tvLatLng.setText("Lat:" + latestInfo.lastLat + "\nLng: " + latestInfo.lastLong);
		
		// get user's message
		EditText etMessage = (EditText)findViewById(R.id.editTextInput);
		String message = etMessage.getText().toString();
		
		// Ignore if the input is blank
		if (message.length() == 0) return;
		
		// create and store geomessage
		ParseObject geoMessage = new ParseObject("GeoMessage");
		geoMessage.put("geoPoint", geoPoint);
		geoMessage.put("message", message);
		geoMessage.saveInBackground();
		
		// clear input
		etMessage.setText("");
		
		/*// put message in GeoMessage viewer
		EditText etMessageViewer = (EditText)findViewById(R.id.editTextMessages);
		etMessageViewer.getText().insert(0, message + "\n");*/
	}
	
	public void getMessages(View view) {
		getMessages();
	}
	
	public void getMessages() {
		// Make 10 attempts to get the users location
		LocationInfo userLoc = new LocationInfo(getBaseContext());;
		for (int i = 0; i < 10; ++i)
		{
			// check if lat is valid
			if (userLoc.lastLat < -90   || userLoc.lastLat > 90 ||
				userLoc.lastLong < -180 || userLoc.lastLong > 180)
			{
				if (i == 10)
				{
					displayError("Could not get location");
					return;
				}
			}
			userLoc = new LocationInfo(getBaseContext());
		}
		
		// Got a valid location
		ParseGeoPoint geoPoint = new ParseGeoPoint();
		geoPoint.setLatitude(userLoc.lastLat);
		geoPoint.setLongitude(userLoc.lastLong);
		
		// Display the lat/lng
		final TextView tvLatLng = (TextView)findViewById(R.id.textViewLatLng);
		final LocationInfo displayLoc = userLoc;
		tvLatLng.post(new Runnable() {
			public void run() {
				tvLatLng.setText("Lat:" + displayLoc.lastLat + "\nLng: " + displayLoc.lastLong);
			}
		});
		
		// Get messages within 50 meters of the current location
		ParseQuery query = new ParseQuery("GeoMessage");
		query.whereWithinKilometers("geoPoint", geoPoint, 0.05);
		query.findInBackground(new FindCallback() {
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) {
					displayGeoMessages(objects);
				} else {
					displayError(e.getMessage());
				}
			}
		});
	}
	
	public void displayGeoMessages(List<ParseObject> messages) {
		msgList.clear();
		for (ParseObject msg : messages) {
			msgList.add(msg.getString("message"));
		}
		adapter.notifyDataSetChanged();
	}
	
	public void displayError(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Error");
		builder.setMessage(msg);
		builder.setNeutralButton("Close", null);
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}


}

