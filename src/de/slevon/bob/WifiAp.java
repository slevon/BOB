package de.slevon.bob;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiAp {

	final static  String url="http://slevon.de/huette/camera/wifi_ap_state.php";
	
	static boolean getWifiApState(Context context){
        Log.d("ROMAN","WifiAp:");
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        Method method = null;
		//get method
        try {
			method = wifiMan.getClass().getDeclaredMethod("isWifiApEnabled");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
        
		method.setAccessible(true);
        //try to excute it
		boolean isOn = false;
        try {
        	isOn =(Boolean)method.invoke(wifiMan);
			Log.d("ROMAN","WifiAp:"+isOn);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return isOn;
	}
	
	static void toggleWifiApState(Context context){
		WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		boolean currentState = getWifiApState(context);
		
        Method[] methods = wifiMan.getClass().getDeclaredMethods();
        for(Method meth :methods){
        	//Log.d("Roman",meth.getName());
        	if(meth.getName().equals("setWifiApEnabled")){
        		try{
        			meth.invoke(wifiMan, null,!currentState);
        		}catch (Exception e) {
					e.printStackTrace();
				}
        	}
        }
	}
	
	static void postWifiApState(Context context){
		Log.d("ROMAN","WifiApPost");
		//get current state
		boolean isOn = getWifiApState(context);
		//Post it to the server
		HttpClient httpC = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>(1);
			params.add(new BasicNameValuePair("wifi_ap_state", String.valueOf(isOn)));

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
	
}
