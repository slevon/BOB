package de.slevon.bob;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class FlightmodeSwitcher  extends Service {
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onStart(Intent intent, int startId) {
	    handleStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    handleStart(intent, startId);
	    return START_STICKY;
	}
	
	public void handleStart(Intent intent, int startId){
		//Startup  code herecontext
		 Log.d("ROMAN", "Flightmode service started");
		 //When the service ist triggered, we disable the flightmode, 
		 // this is usually called by the Alarmmanger
		 disableFlightmode(getApplicationContext());
	}
	
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
    	 Log.d("ROMAN", "Flightmode Service stopped");
        super.onDestroy();
    }
    //////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    public static boolean isFlightmodeOn(Context context){
    	boolean state = ((Settings.System.getInt(context.getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0) == 1)?true:false);
    	Log.d("ROMAN","isFlightmodeOn Air Plane Mode is "+(state?"on":"off"));
    	return state;
    }
    public static void setFlightmode(Context context,boolean state){
    	
    		int stateVal =state ? 1: 0;
    		// 0= OFF
    		// 1= NO
    		Settings.System.putInt(context.getContentResolver(),Settings.System.AIRPLANE_MODE_ON, stateVal);
    		Log.d("ROMAN","Air Plane Mode is "+(state?"on":"off"));//Displaying a Message
    		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);//creating intent and Specifying action for AIRPLANE mode.
            intent.putExtra("state", state);//indicate the "state" of airplane mode is changed to OFF
            context.sendBroadcast(intent);//Broadcasting and Intent
    }
    
    public static void disableFlightmode(Context context){
    	setFlightmode(context, false);
    }
    public static void enableFlightmode(Context context){
    	setFlightmode(context, true);
    }
    
    //This function disables the flightmode for a given time:
    public static void enableFlightmodeForMinutes(Context context, int minutes){
    	//switch off now
    	enableFlightmode(context);
    	
    	//enquqe for switching on:
		/////////////////////////////
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, minutes);
		
		Intent flightIntent = new Intent(context, FlightmodeSwitcher.class);
		PendingIntent pintent = PendingIntent.getService(context, 0, flightIntent, 0);
	
		AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),  pintent);
		
		Log.d("ROMAN", "Switched off for "+minutes);
    }
    
    
    
    
}


