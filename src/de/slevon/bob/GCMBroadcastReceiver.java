package de.slevon.bob;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
 
public class GCMBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    	//////////////////////////////
    	// First; Start the GCM
    	/////////////////////////////
    	Log.d("ROMAN","GCMBroadcastReceiver onReceive");
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                GCMIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    	//////////////////////////////
    	// Second; Start the Battery Logger
    	/////////////////////////////
        Calendar cal = Calendar.getInstance();

		Intent battIntent = new Intent(context.getApplicationContext(), BatteryState.class);
		PendingIntent pintent = PendingIntent.getService(context, 0, battIntent, 0);

		AlarmManager alarm = (AlarmManager)context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		// Start every 30 seconds
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 60*60*1000, pintent); 
		//End start on regluar bases
		//And log at the time of the boot:
		BatteryState.logCurrentState(context.getApplicationContext());
        
    }
}