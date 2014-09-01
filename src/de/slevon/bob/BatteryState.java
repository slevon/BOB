package de.slevon.bob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;


public class BatteryState extends Service{
	
	final static String logpath="sdcard/battery-log.txt";
	final static  String url="http://slevon.de/huette/camera/battery_state.php";
	private final String REMINDER_BUNDLE = "BatteryBundle"; 
	
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
		 Log.d("ROMAN", "BatteryService started");
		 
		 logCurrentState(getApplicationContext());
	}
	
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
    	 Log.d("ROMAN", "Battery Service stopped");
        super.onDestroy();
    }
	
	
	public static float getLevel(Context context){
		  
        Intent batteryIntent = context.registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        
        
        float percent = ((float)level/(float)scale) * 100.0f;
        
        Log.d("BATBAT","Fill:"+percent);
        
        return percent;
        
	}
	
	public static boolean isCharging(Context context){
		  Intent batteryIntent = context.registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		  int charging = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
	      boolean isCharing =false;
	      
	      if(charging == BatteryManager.BATTERY_STATUS_CHARGING){
	    	  isCharing = true;
	      }  
		  Log.d("BATBAT","Status:"+isCharing);
	        
		  return isCharing;
		
	}
	
	public static void logCurrentState(Context context)
	{       
	   File logFile = new File(logpath);
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   try
	   {
		   	//BufferedWriter for performance, true to set append to file flag
		   	BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
	      	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      		String nowAsISO = df.format(new Date());
      		buf.append(nowAsISO+"\t"+getLevel(context)+"\t"+isCharging(context)+"\t"+FlightmodeSwitcher.isFlightmodeOn(context));
      		buf.newLine();
      		buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}
	/*
	 *Sends the data to the Server
	 */
	public static void postLog(boolean clear){
		 	Log.d("BATBAT","Post");
		 	File fl = new File(logpath);
		    FileInputStream fin;
		    String ret="";
			try {
				fin = new FileInputStream(fl);
				ret = convertStreamToString(fin);
				fin.close();
				//Make sure you close all streams.
			}catch (IOException e) {
					e.printStackTrace();   
			}catch (Exception e) {
				e.printStackTrace();
			}
		    if (clear){
		    	fl.delete();
		    }
		    ///////////////////////
		    //Upload
		    //////////////////////
		    //Post it to the server
			HttpClient httpC = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			
			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>(1);
				params.add(new BasicNameValuePair("battery_values", ret));

				httpPost.setEntity(new UrlEncodedFormEntity(params));
				HttpResponse response = httpC.execute(httpPost);
				Log.d("ROMAN postWifiApState",response.toString());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private static String convertStreamToString(InputStream is) throws Exception {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line).append("\n");
	    }
	    reader.close();
	    return sb.toString();
	}

	

}
