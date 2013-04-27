package com.example.virtual_graffiti;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

public class MainActivity extends Activity {

	private ListView msgListView;
	private List<String> msgList = new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private Handler uiHandler = new Handler();
	private DbHelper dbHelper = new DbHelper();
	private LocationInfo userLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle("Virtual Graffiti");
		
		// Parse stuff
		Parse.initialize(this, "f09jNI2OB82MUvz0iHP8X8ZkKFgjnsC03IGHW240", "s5BkfxQRp75PUNjlKRIew7XieumzS0CcZMBdeIwF");
		ParseAnalytics.trackAppOpened(getIntent());

		// Location lib stuff
		LocationLibrary.initialiseLibrary(this, "com.example.virtual-graffiti");
		userLocation = new LocationInfo(this);
		
		// set up the msgListView
		msgListView = (ListView)findViewById(R.id.listViewMessages);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, msgList);
		msgListView.setAdapter(adapter);
		
		if (uiHandler == null) displayError("uiHandler");
		else if (msgList == null) displayError("msgList");
		else if (adapter == null) displayError("adapter");
		else if (this == null) displayError("this");
		else if (getIntent() == null) displayError("getIntent()");
		else dbHelper.initialize(this, getIntent(), uiHandler, msgList, adapter);
		
		// Start a new thread to get the messages
		loadListView(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void switchToMapView(View view)
	{
		Intent myIntent = new Intent(this, ActivityMapView.class);
//		myIntent.putExtra("key", value); //Optional parameters
		this.startActivity(myIntent);
	}
	
	public void doSubmit(View view)
	{
		// get user's message
		final EditText etMessage = (EditText)findViewById(R.id.editTextInput);
		final String msg = etMessage.getText().toString();
		
		if (msg.replaceAll("\\s", "").equals("")) return;
		
		// get the user's coords
		userLocation.refresh(this);
		
		// push the message to the cloud
		boolean statusOK = dbHelper.push(msg, userLocation.lastLat, userLocation.lastLong);
		
		if (!statusOK)
		{
			displayError("Failed to submit message");
			return;
		}
		
		etMessage.setText("");
		msgList.add(0, msg);
		adapter.notifyDataSetChanged();

        final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		msgListView.smoothScrollToPosition(0);
	}
	
	public void loadListView(View view) {
		// Get the user's location
		userLocation.refresh(this);
		dbHelper.pull(userLocation);

		// Display the lat/lng
		final TextView tvLatLng = (TextView)findViewById(R.id.textViewLatLng);
		tvLatLng.setText("Lat:" + userLocation.lastLat + "\nLng: " + userLocation.lastLong);
		

	}
	
	public void displayGeoMessages(List<ParseObject> messages) {
		msgList.clear();
		for (ParseObject msg : messages) {
			msgList.add(msg.getString("message"));
		}
		adapter.notifyDataSetChanged();
	}
	
	public void displayError(final String msg) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Error");
		builder.setMessage(msg);
		builder.setNeutralButton("Close", null);
		
		AlertDialog errDialog = builder.create();
		errDialog.show();
	}
}

