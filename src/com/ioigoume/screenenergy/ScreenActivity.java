package com.ioigoume.screenenergy;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

public class ScreenActivity extends Activity{
	public static final String TAG = "ScreenActivity";
	
	private ToggleButton enableToggle = null;
	private com.ioigoume.numberpicker.NumberPicker redRegulator = null;
	private com.ioigoume.numberpicker.NumberPicker blueRegulator = null;
	private com.ioigoume.numberpicker.NumberPicker greenRegulator = null;
	//private com.ioigoume.numberpicker.NumberPicker contrastRegulator = null;
	private com.ioigoume.numberpicker.NumberPicker brightnessRegulator = null;
	private com.ioigoume.numberpicker.NumberPicker alphaRegulator = null;
	
	private int red = 0;
	private int green = 0;
	private int blue = 0;
	//private int contrast = 0;
	private int brightness = 0;
	private int alpha = 0;
	
	private EditText redText = null;
	private EditText blueText = null;
	private EditText greenText = null;
	//private EditText contrastText = null;
	private EditText brightnessText = null;
	private EditText alphaText = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screen);
		Log.v(TAG, "onCreate");
		/**
		 * RETRIEVE ITEMS BY ID
		 */
		// Screen Colors Regulation Objects
		redRegulator = (com.ioigoume.numberpicker.NumberPicker)findViewById(R.id.PickerRed);
		blueRegulator = (com.ioigoume.numberpicker.NumberPicker)findViewById(R.id.PickerBlue);
		greenRegulator = (com.ioigoume.numberpicker.NumberPicker)findViewById(R.id.PickerGreen);
		//contrastRegulator = (com.ioigoume.numberpicker.NumberPicker)findViewById(R.id.PickerContrast);
		brightnessRegulator = (com.ioigoume.numberpicker.NumberPicker)findViewById(R.id.PickerBrightness);
		alphaRegulator = (com.ioigoume.numberpicker.NumberPicker)findViewById(R.id.PickerAlpha);
	
		// Text Editor included in the Regulation Object
		redText = (EditText)redRegulator.findViewById(R.string.text_id);
		blueText = (EditText)blueRegulator.findViewById(R.string.text_id);
		greenText = (EditText)greenRegulator.findViewById(R.string.text_id);
		//contrastText = (EditText)contrastRegulator.findViewById(R.string.text_id);
		brightnessText = (EditText)brightnessRegulator.findViewById(R.string.text_id);
		alphaText = (EditText)alphaRegulator.findViewById(R.string.text_id);
		
		
		// Buttons
		enableToggle = (ToggleButton)findViewById(R.id.toggleBtn_Enable);
				
		/**
		 * ADD EVENT LISTENER TO THE OBJECTS RETRIEVED
		 */
		// Text Change Listener for the Text Editors
		redText.addTextChangedListener(redTextWatcher);
		blueText.addTextChangedListener(blueTextWatcher);
		greenText.addTextChangedListener(greenTextWatcher);
		//contrastText.addTextChangedListener(contrastTextWatcher);
		brightnessText.addTextChangedListener(brightnessTextWatcher);
		alphaText.addTextChangedListener(alphaTextWatcher);
		
		
		// Listeners
		enableToggle.setOnClickListener(toggleListener);	
		
		
	}

	
	@Override
	protected void onPause() {
		super.onPause();
		Log.v(TAG, "onPause");
		
		// Edit and commit
		savePreferences(getString(R.string.filename_prefs), getString(R.string.key_red), Integer.toString(redRegulator.getValue()));
		savePreferences(getString(R.string.filename_prefs), getString(R.string.key_green), Integer.toString(greenRegulator.getValue()));
		savePreferences(getString(R.string.filename_prefs), getString(R.string.key_blue), Integer.toString(blueRegulator.getValue()));
		savePreferences(getString(R.string.filename_prefs), getString(R.string.key_brightness), Integer.toString(brightnessRegulator.getValue()));
		savePreferences(getString(R.string.filename_prefs), getString(R.string.key_alpha), Integer.toString(alphaRegulator.getValue()));
	}


	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
		
		/**
		 * PREFERENCE OBJECT
		 */
		SharedPreferences myprefs = getApplicationContext().getSharedPreferences(getString(R.string.filename_prefs), MODE_PRIVATE);
		
		// Retrieve values from shared prefs file
		red = Integer.parseInt(myprefs.getString(getString(R.string.key_red), getString(R.string.red_defaultvalue)));
		Log.i(TAG, "red: " + String.valueOf(red));
		
		blue = Integer.parseInt(myprefs.getString(getString(R.string.key_blue), getString(R.string.blue_defaultvalue)));
		Log.i(TAG, "blue: " + String.valueOf(blue));
		
		green = Integer.parseInt(myprefs.getString(getString(R.string.key_green), getString(R.string.green_defaultvalue)));
		Log.i(TAG, "green: " + String.valueOf(green));
		
		brightness = Integer.parseInt(myprefs.getString(getString(R.string.key_brightness), getString(R.string.brightness_defaultvalue)));
		Log.i(TAG, "brightness: " + String.valueOf(brightness));
		
		alpha = Integer.parseInt(myprefs.getString(getString(R.string.key_alpha), getString(R.string.alpha_defaultvalue)));
		Log.i(TAG, "alpha: " + String.valueOf(alpha));
		
		redRegulator.setValue(red);
		greenRegulator.setValue(green);
		blueRegulator.setValue(blue);
		brightnessRegulator.setValue(brightness);
		alphaRegulator.setValue(alpha);
		
		if(isServiceRunning(".BackgroundService_HUD")){
			Log.i(TAG,"Service Running");
			enableToggle.setChecked(true);
		}else{
			Log.i(TAG,"Service Not Running");
			enableToggle.setChecked(false);
		}
	}


	@Override
	protected void onStart() {
		super.onStart();
		Log.v(TAG, "onStart");
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.v(TAG,"onDestroy");
	}


	/**
	 * LISTENER FUNCTIONS
	 */
	View.OnClickListener toggleListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(ScreenActivity.this, BackgroundService_HUD.class);
			intent.addFlags(Service.START_STICKY);
			intent.addFlags(Service.BIND_AUTO_CREATE);
			
			if(enableToggle.isChecked()){
				Log.i(TAG, "Start Service");
				startService(intent);
			}else {
				Log.i(TAG, "Stop Service");
				stopService(intent);
			}
			
		}
	};
	
	
	/**
	 * TEXT CHANGE LISTENERS CLASSES
	 */
	// Red EditText
	TextWatcher redTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {}
		
		@Override
		public void afterTextChanged(Editable s) {
			// Send the changed value
			Intent redint = new Intent();
			redint.setAction("com.ioigoume.action.red");
			redint.putExtra("red", s.toString());
			sendBroadcast(redint);
			
			// Store the changed value
			savePreferences(getString(R.string.filename_prefs), getString(R.string.key_red), Integer.toString(redRegulator.getValue()));
		}
	};
	
	
		// Blue EditText
		TextWatcher blueTextWatcher = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				// Send the changed value
				Intent blueint = new Intent();
				blueint.setAction("com.ioigoume.action.blue");
				blueint.putExtra("blue", s.toString());
				sendBroadcast(blueint);		
				
				// Store the changed value
				savePreferences(getString(R.string.filename_prefs), getString(R.string.key_blue), Integer.toString(blueRegulator.getValue()));
			}
		};
		
		// Green EditText
		TextWatcher greenTextWatcher = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				// Send the changed value
				Intent greenint = new Intent();
				greenint.setAction("com.ioigoume.action.green");
				greenint.putExtra("green", s.toString());
				sendBroadcast(greenint);	
				
				// Store the changed value
				savePreferences(getString(R.string.filename_prefs), getString(R.string.key_green), Integer.toString(greenRegulator.getValue()));
			}
		};
		
		/*
		// Contrast EditText
		TextWatcher contrastTextWatcher = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				// Send the changed value
				Intent contrastint = new Intent();
				contrastint.setAction("com.ioigoume.action.contrast");
				contrastint.putExtra("contrast", s.toString());
				sendBroadcast(contrastint);
				
				// Store the changed value
				SharedPreferences.Editor myeditor = myprefs.edit();
				myeditor.putString(getString(R.string.key_contrast), s.toString());
				myeditor.commit();
			}
		};*/
		
		// Brightness EditText
		TextWatcher brightnessTextWatcher = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				// Send the changed value
				Intent brightnessint = new Intent();
				brightnessint.setAction("com.ioigoume.action.brightness");
				brightnessint.putExtra("brightness", s.toString());
				sendBroadcast(brightnessint);
				
				// Store the changed value
				savePreferences(getString(R.string.filename_prefs), getString(R.string.key_brightness), Integer.toString(brightnessRegulator.getValue()));
			}
		};
		
		
		// Alpha EditText
		TextWatcher alphaTextWatcher = new TextWatcher() {
					
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
				
			@Override
			public void afterTextChanged(Editable s) {
				// Send the changed value
				Intent alphaint = new Intent();
				alphaint.setAction("com.ioigoume.action.alpha");
				alphaint.putExtra("alpha", s.toString());
				sendBroadcast(alphaint);
						
				// Store the changed value
				savePreferences(getString(R.string.filename_prefs), getString(R.string.key_alpha), Integer.toString(alphaRegulator.getValue()));
			}
		};
		
		// --- SERVICE CHECK CONTROL USING THE SYSTMEM
		// Check if the service is running
		public boolean isServiceRunning(String serviceName) {
			boolean serviceRunning = false;
			ActivityManager am = (ActivityManager) ScreenActivity.this.getSystemService(ACTIVITY_SERVICE);
			List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);
			Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
			while (i.hasNext()) {
				ActivityManager.RunningServiceInfo runningServiceInfo = (ActivityManager.RunningServiceInfo) i.next();
				if (runningServiceInfo.service.getShortClassName().equals(serviceName)) {
					serviceRunning = true;
				}
			}
			return serviceRunning;
		}
		
		/**
		 * SAVE SHARED PREFERENCES
		 * @param filename
		 * @param key
		 * @param value
		 * @return
		 */
		private boolean savePreferences(String filename, String key, String value){
			try{
				SharedPreferences myprefs = getApplicationContext().getSharedPreferences(filename, MODE_PRIVATE);
				SharedPreferences.Editor myeditor = myprefs.edit();
				
				// Edit and commit
				myeditor.putString(key, value);
				myeditor.commit();
				return true;
			}catch(Exception e){
				return false;
			}
			
			
		}
}
