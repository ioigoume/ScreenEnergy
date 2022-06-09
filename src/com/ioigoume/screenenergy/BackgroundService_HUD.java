package com.ioigoume.screenenergy;

import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

public class BackgroundService_HUD extends Service {
	private static final String TAG = "BackgroundService";
	
	// Service variables
	private NotificationManager notificationMgr = null;		//	NOTIFICATION MANAGER
	private PowerManager pm = null;							// 	POWER - NOT SLEEP
	private WakeLock myWakeLock = null;						//	POWER - NOT SLEEP
	
	private ServiceBroadCastReceiver receiver = null;		// 	BROADCAST RECEIVER OBJECT
	private MyIntentFilter myIntent = null;
	private WindowManager wm = null;						//	WINDOW MANAGER
	private WindowManager.LayoutParams params = null;		//	PARAMETERS
	
	HUDView mView = null;
	
	private int red = 0;
	private int green = 0;
	private int blue = 0;
	//private float contrast = 0;
	private float brightness = 0;
	private int	alpha = 0;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// /////////// CALL MY SERVICES

		// Power management service
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// Notification manager Service
		notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				
		///////////////////////////////////
		//	INITIALIZE THE POWER CONTROL //
		///////////////////////////////////
		myWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "call_lock");
		
				
		//////////////////////////////////////////////////
		// INITIALIZE THE BROADCAST RECEIVER - OBSERVER	//
		//////////////////////////////////////////////////
		// Broadcast Receiver
		receiver = new ServiceBroadCastReceiver();
		// Intent Filter
		myIntent = new MyIntentFilter();        
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		displayNotificationMessage("Remove Screen Adjustments");
        try
        {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mView);
            mView = null;
        }catch(NullPointerException e){
        	Log.e(TAG,"myView-Ln:81:Null");
        }catch(IllegalStateException e){
        	Log.e(TAG,"myView-Ln:81:Illegal State Exception");
        }catch(Exception e){
        	Log.e(TAG,"myView-Ln:81:" + e.getMessage());
        }
		
		/**
		 *  RELEASE THE NON SLEEP PHONE STATE MODE
		 */
		if (myWakeLock.isHeld())
			myWakeLock.release();
		/**
		 * UREGISTER RECEIVER
		 */	
		if(receiver != null)
			this.unregisterReceiver(receiver);
		
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		// Register the broadcast receiver
		this.registerReceiver(receiver, myIntent);
		
		// Do not let the device go to sleeping mode
		Log.d(TAG, "onStart - Do not let me sleep");
		myWakeLock.acquire();
		
		/**
		 * PREFERENCE OBJECT
		 */
		SharedPreferences myprefs = getApplicationContext().getSharedPreferences(getString(R.string.filename_prefs), MODE_PRIVATE);
		
		red = Integer.parseInt(myprefs.getString(getString(R.string.key_red), getString(R.string.red_defaultvalue)));
		blue = Integer.parseInt(myprefs.getString(getString(R.string.key_blue), getString(R.string.blue_defaultvalue)));
		green = Integer.parseInt(myprefs.getString(getString(R.string.key_green), getString(R.string.green_defaultvalue)));
		//contrast = Integer.parseInt(myprefs.getString(getString(R.string.key_contrast), getString(R.string.contrast_defaultvalue)))/100f;
		brightness = Integer.parseInt(myprefs.getString(getString(R.string.key_brightness), getString(R.string.brightness_defaultvalue)))/100f;
		alpha = Integer.parseInt(myprefs.getString(getString(R.string.key_alpha), getString(R.string.alpha_defaultvalue)));
			
		
		/**
		 * CREATE MY VIEW
		 */ 
		     
		// Create the view after the colors have been retrieved
		mView = new HUDView(this);
        params = new WindowManager.LayoutParams();
        params.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
        			   WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
        			   WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
        			   WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        params.format = PixelFormat.TRANSLUCENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.TOP;
            
        if(brightness < 0.1f) brightness = 0.1f;
        params.screenBrightness = brightness;
       
        params.setTitle("Load Average");
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(mView, params);
        
        displayNotificationMessage("Screen Adjustment Added");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	/**
	 *  --- SERVICE CHECK CONTROL USING THE SYSTMEM
	 * @param serviceName
	 * @return boolean
	 * Check if the service is running
	 */
	public boolean isServiceRunning(String serviceName) {
		boolean serviceRunning = false;
		ActivityManager am = (ActivityManager) BackgroundService_HUD.this.getSystemService(ACTIVITY_SERVICE);
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
	 * 
	 * @param message : the message to be displayed as a notification
	 */
	private void displayNotificationMessage(String message) {
		Notification notify = new Notification(android.R.drawable.stat_notify_chat, message, System.currentTimeMillis());
		notify.flags = Notification.FLAG_AUTO_CANCEL;
		notify.icon = R.drawable.ic_launcher_not;
		
		// The service is not running
		if(!isServiceRunning(("." + BackgroundService_HUD.class.getSimpleName()).trim())){
			Intent start = new Intent();
			start.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			// Notification that does not redirect to other Activities
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, start, PendingIntent.FLAG_UPDATE_CURRENT);
			notify.setLatestEventInfo(BackgroundService_HUD.this, "Nitalb - ScreenEnergy", message, contentIntent);
			notificationMgr.notify(R.string.app_notification_id, notify);
		}else{	// The service is running
			Intent start = new Intent(BackgroundService_HUD.this, ScreenActivity.class);
			start.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			// Notification that redirects to another Activity
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, start, PendingIntent.FLAG_UPDATE_CURRENT);
			notify.setLatestEventInfo(this, "Nitalb - ScreenEnergy", message, contentIntent);
			
			notificationMgr.notify(R.string.app_notification_id, notify);
		}
	}	
	
	/**
	 * Creates the customized camvas
	 * @author ioigoume
	 *
	 */
	class HUDView extends ViewGroup {
	    //private Paint mLoadPaint;

	    public HUDView(Context context) {
	        super(context);
	        //mLoadPaint = new Paint();
	        //mLoadPaint.setAntiAlias(true);
	        //mLoadPaint.setTextSize(10);
	        //mLoadPaint.setARGB(100, 255, 0, 0);
	    }

	    @Override
	    protected void onDraw(Canvas canvas) {
	        super.onDraw(canvas);
	        //canvas.drawText("Hello World", 15, 15, mLoadPaint);
	        canvas.drawARGB(alpha, red, green, blue);
	    }

	    @Override
		protected void drawableStateChanged() {
			super.drawableStateChanged();
		}

		@Override
	    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
	    }

	    @Override
	    public boolean onTouchEvent(MotionEvent event) {
	        //return super.onTouchEvent(event);
	        return true;
	    }
	}
	
	/**
	 * Broadcast Receiver
	 * @author ioigoume
	 *
	 */
	private class ServiceBroadCastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action != null && !action.isEmpty()){
				if(action.equals("com.ioigoume.action.red")){
					String redval = intent.getStringExtra("red");
					red = Integer.parseInt(redval);
					mView.postInvalidate();
					Log.i(TAG, Integer.toString(red));
				}
				if(action.equals("com.ioigoume.action.blue")){
					String blueval = intent.getStringExtra("blue");
					blue = Integer.parseInt(blueval);
					mView.postInvalidate();
					Log.i(TAG, Integer.toString(blue));
				}
				if(action.equals("com.ioigoume.action.green")){
					String greenval = intent.getStringExtra("green");
					green = Integer.parseInt(greenval);
					mView.postInvalidate();
					Log.i(TAG, Integer.toString(green));
				}
				/*if(action.equals("com.ioigoume.action.contrast")){
					String contrastval = intent.getStringExtra("contrast");
					contrast = Integer.parseInt(contrastval);
					
					mView.postInvalidate();
					Log.i(TAG, Float.toString(contrast));
				}*/
				if(action.equals("com.ioigoume.action.brightness")){
					String brightnessval = intent.getStringExtra("brightness");
					brightness = Float.parseFloat(brightnessval)/100f;
					if(brightness < 0.1f)
						brightness = 0.1f;
					params.screenBrightness = brightness;
				    wm.updateViewLayout(mView, params);
				    mView.postInvalidate();
					
					Log.i(TAG, Float.toString(brightness));
				}
				if(action.equals("com.ioigoume.action.alpha")){
					String alphaval = intent.getStringExtra("alpha");
					alpha = Integer.parseInt(alphaval);
				    mView.postInvalidate();
					
					Log.i(TAG, String.valueOf(alpha));
				}
			}
		}
		
		
	}
	
	
	/**
	 * 
	 * @author menios junior
	 *
	 */
	private class MyIntentFilter extends IntentFilter{
		MyIntentFilter(){
			this.addAction("com.ioigoume.action.red");
			this.addAction("com.ioigoume.action.green");
			this.addAction("com.ioigoume.action.blue");
			//this.addAction("com.ioigoume.action.contrast");
			this.addAction("com.ioigoume.action.brightness");
			this.addAction("com.ioigoume.action.alpha");
			
		}
	}
}
