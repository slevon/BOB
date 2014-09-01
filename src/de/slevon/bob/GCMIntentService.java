package de.slevon.bob;

import static de.slevon.bob.CommonUtilities.TAG;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.slevon.bob.TakePhotoActivity;
import de.slevon.bob.WifiAp;
import de.slevon.gcm.R;

 
public class GCMIntentService extends IntentService  {
	public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GCMIntentService() {
        super("GcmIntentService");
        
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Roman", "Receiver onHandleIntend");
        Bundle extras = intent.getExtras();
        Log.d("Roman", extras.toString());
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                MESSAGE_TYPE_MESSAGE.equals(messageType)) {
	            Log.d(TAG, "onHandleIntent Received: " + extras.toString());
            	//Now we check the type of message:
            	String msg = extras.getString("msg");
            	if(msg == null){
            		msg="";
            	}
            	if(msg.equals("takePicture")){
	       		//We start the activtiy that takes the photo
	       		Intent i = new Intent (this, TakePhotoActivity.class);
	       		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	       		getApplication().startActivity(i);
	       		
	       		//ACHTUNG!!!
	       		//everytime we take a picture, we upload the battery stat and wifi-ap
	       		BatteryState.postLog(true);
	       		WifiAp.postWifiApState(getApplicationContext());
	       		
            	} else if( msg.equals("toggleWifiAp")){
            		//Toggle WIFI
            		WifiAp.toggleWifiApState(getApplicationContext());
            		//After this we update the current state:
            		WifiAp.postWifiApState(getApplicationContext());
            	}else if( msg.equals("getWifiAp")){
            		//send current value to webserver
            		WifiAp.postWifiApState(getApplicationContext());
	            }else if( msg.equals("sleep")){
	        		//If the teathering is off, we set the device to sleep for a given number of minutes
	            	try{
		            	if(!WifiAp.getWifiApState(getApplicationContext())){
		            		//We wait one minute before, we 
		            		String sMinutes = extras.getString("minutes");
		            		int minutes = 1;
		            		if(sMinutes != null){
		            			minutes = Integer.parseInt(sMinutes);
		            		}
		            		FlightmodeSwitcher.enableFlightmodeForMinutes(getApplicationContext(), minutes);
		            	}
	            	}catch(Exception e){
	            		//on fallback always disable flightmode
	            		FlightmodeSwitcher.disableFlightmode(getApplicationContext());
	            	}
	        	}
            }else{
            	Log.e("ROMAN","onHandleIntent: Unknown Meassage type");
            }
        }else{
        	Log.e("ROMAN","onHandleIntent: Extras are emtpy");
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("GCM Notification")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}