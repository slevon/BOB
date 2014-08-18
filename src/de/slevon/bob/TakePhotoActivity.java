package de.slevon.bob;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.slevon.gcm.R;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class TakePhotoActivity extends Activity implements SurfaceHolder.Callback, OnClickListener {

	 private static final String TAG = "CameraTest";
	    Camera mCamera;
	    boolean mPreviewRunning = false; 
	    Timer myTimer;
	    Button but;
	    String mPhotoFilename;
	    final String mFilePath="/sdcard/cam_shots/"; 
	    
	    ProgressDialog mUploadDialog = null;

		PowerManager pm;
		WakeLock wl;
		KeyguardManager km;
		KeyguardLock kl;
		
	    public void onCreate(Bundle icicle) 
	    {
	        super.onCreate(icicle);

	        /////////////////////////////////////////////////////////////////
	        // unlock screen
	        /////////////////////////////////////////////////////////////////
	        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	        km=(KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
	        kl=km.newKeyguardLock("INFO");
	        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.ON_AFTER_RELEASE, "INFO");
	        wl.acquire(); //wake up the screen
	        kl.disableKeyguard();// dismiss the keyguard
	        /////////////////////////////////////////////////////////////////
	        
	        getWindow().setFormat(PixelFormat.TRANSLUCENT);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        setContentView(R.layout.activity_take_photo);
	        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
	        mSurfaceView.setOnClickListener(this);
	        mSurfaceHolder = mSurfaceView.getHolder();
	        mSurfaceHolder.addCallback(this);
	        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	        
	        Button but=(Button) findViewById(R.id.btnCapturePicture);
	        but.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mCamera.takePicture(null, null, mPictureCallback);
					Log.d("ROMAN","Clicked");
				}
			});

	      // Take photo after 5 Seconds;
	      //
	       final ScheduledExecutorService worker= 
	        		  Executors.newSingleThreadScheduledExecutor();	
	        		 
		  Runnable task = new Runnable() {
		    public void run() {
		    	mCamera.takePicture(null, null, mPictureCallback);
				Log.d("ROMAN","Taken");
		    }
		  };
		  worker.schedule(task, 5, TimeUnit.SECONDS);
	        
	    }

	    @Override
	    protected void onRestoreInstanceState(Bundle savedInstanceState) 
	    {
	        super.onRestoreInstanceState(savedInstanceState);
	    }


	    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

	        @Override
	        public void onPictureTaken(byte[] data, Camera camera) {
	            // TODO Auto-generated method stub
	            if (data != null) 
	            {

	            	//TimeZone tz = TimeZone.getTimeZone("UTC");
	            	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
	            	//df.setTimeZone(tz);
	            	String nowAsISO = df.format(new Date());
	            	
	            	mPhotoFilename=nowAsISO+".jpg";
	            	
	                mCamera.stopPreview();
	                mPreviewRunning = false;
	                mCamera.release();

	                FileOutputStream outStream = null;
	                try{
	                	File f= new File(mFilePath);
	                	f.mkdirs(); //create path if not exists
	                	
	                    outStream = new FileOutputStream(mFilePath+mPhotoFilename);
	                    outStream.write(data);
	                    outStream.close();
	                    
	                } catch (FileNotFoundException e){
	                    Log.d("CAMERA", e.getMessage());
	                } catch (IOException e){
	                    Log.d("CAMERA", e.getMessage());
	                }
	                //StoreByteImage(mContext, imageData, 50,"ImageName");
	                //setResult(FOTO_MODE, mIntent);

	                mUploadDialog = ProgressDialog.show(TakePhotoActivity.this, "", "Uploading file...", true);
		              //  setResult(585);
		                new Thread(new Runnable() {
		                    public void run() {           
		                       
		                         uploadFile(mFilePath+mPhotoFilename);
		                                                  
		                    }
		                  }).start(); 
	            } 
	            
	        }
	    };


	 
	    protected void onSaveInstanceState(Bundle outState) 
	    {
	        super.onSaveInstanceState(outState);
	    }

	    protected void onStop() 
	    {
	        Log.e(TAG, "onStop");
	        super.onStop();
	    }
	    @Override
	    protected void onPause() {
	        // TODO Auto-generated method stub
	        super.onPause();
	        if(wl.isHeld()){
	        	wl.release(); //when the activiy pauses, we should realse the wakelock
	        }
	    }

	    @Override
	    protected void onResume() {
	        // TODO Auto-generated method stub
	        super.onResume();
	        wl.acquire();//must call this!
	    }

	    public void surfaceCreated(SurfaceHolder holder) 
	    {
	        Log.e(TAG, "surfaceCreated");
	        mCamera = Camera.open();
	        mCamera.setDisplayOrientation(90);
	        //////////////////////////////////////////////////////////////////
	        //Get the resolutons
	        Camera.Parameters params = mCamera.getParameters();
	        // Check what resolutions are supported by your camera
	        List<Size> sizes = params.getSupportedPictureSizes();
	        // Iterate through all available resolutions and choose one.
	        // The chosen resolution will be stored in mSize.
	        Size mSize=sizes.get(0);
	        for (Size size : sizes) {
	            Log.i(TAG, "Available resolution: "+size.width+" "+size.height);
	            if (size.width > mSize.width || size.height > mSize.height) {//find the largest on
	                mSize = size;
	                break;
	            }
	        }
	         
	        Log.i(TAG, "Chosen resolution: "+mSize.width+" "+mSize.height);
	        params.setPictureSize(mSize.width, mSize.height);
	        ///////////////////////////////////////////////////////////////////

	        //////////////////////////////////////////////////////////////////
	        // set autofocus
	        List<String> focusModes = params.getSupportedFocusModes();
	        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
	        {
	        	params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
	        }
	       
	        ///////////////////////////////////////////////////////////////////
	        //////////////////////////////////////////////////////////////////
	        // set whitebalance
	        params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
	        ///////////////////////////////////////////////////////////////////
	        //////////////////////////////////////////////////////////////////
	        // set rotation
	        ///////////////////////////////////////////////////////////////////
	        //Not working, we rotate the bitmap
	        
	        
	        mCamera.setParameters(params);
	    }

	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	        Log.e(TAG, "surfaceChanged");

	        // XXX stopPreview() will crash if preview is not running
	        if (mPreviewRunning) 
	        {
	            mCamera.stopPreview();
	        }

	        Camera.Parameters p = mCamera.getParameters();

	        List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();

	        Camera.Size previewSize = previewSizes.get(3);
	        p.setPreviewSize(previewSize.width, previewSize.height);


	        mCamera.setParameters(p);
	        try 
	        {
	            mCamera.setPreviewDisplay(holder);
	        } 
	        catch (Exception e) 
	        {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        mCamera.startPreview();
	        mPreviewRunning = true;

	    // THIS IS THE CODE THAT BREAKS IT. IS THERE ANY OTHER WAY TO DO THIS??? ********
	       // mCamera.takePicture(null, null, mPictureCallback);
	    }

	    @Override
	    public void surfaceDestroyed(SurfaceHolder holder) {
	        Log.e(TAG, "surfaceDestroyed");
//	        mCamera.stopPreview();
//	        mPreviewRunning = false;
	        mCamera.release();
	    }

	    private SurfaceView mSurfaceView;
	    private SurfaceHolder mSurfaceHolder; 

	    @Override
	    public void onClick(View v) {
	        // TODO Auto-generated method stub
	        mCamera.takePicture(null, mPictureCallback, mPictureCallback);
	    }
	    
	  
	    
	    public int uploadFile(String sourceFileUri) {
	           
	          final String fileName = sourceFileUri;
	          int serverResponseCode=-1;
	          HttpURLConnection conn = null;
	          DataOutputStream dos = null; 
	          DataInputStream dis =null;
	          String lineEnd = "\r\n";
	          String twoHyphens = "--";
	          String boundary = "***************";
	          int bytesRead, bytesAvailable, bufferSize;
	          byte[] buffer;
	          int maxBufferSize = 1 * 1024 * 1024;
	          File sourceFile = new File(sourceFileUri);
	           
	          if (!sourceFile.isFile()) {
	               
	               mUploadDialog.dismiss();
	                
	               Log.e("uploadFile", "Source File not exist :"
	                                   +fileName);
	               CommonUtilities.appendLog("Source File not exist :"
	            		   +fileName );
	                
	               runOnUiThread(new Runnable() {
	                   public void run() {
	                     Toast.makeText(getApplicationContext(),"Source File not exist :"
	                               +fileName,Toast.LENGTH_LONG ).show();
	                   }
	               });
	          }
	          else
	          {
	               try {
	                    
	                     // open a URL connection to the Servlet
	                   FileInputStream fileInputStream = new FileInputStream(sourceFile);
	                   URL url = new URL(CommonUtilities.UPLOAD_SERVER_URL);
	                    
	                   // Open a HTTP  connection to  the URL
	                   conn = (HttpURLConnection) url.openConnection();
	                   conn.setDoInput(true); // Allow Inputs
	                   conn.setDoOutput(true); // Allow Outputs
	                   conn.setUseCaches(false); // Don't use a Cached Copy
	                   conn.setRequestMethod("POST");
	                   conn.setRequestProperty("Connection", "Keep-Alive");
	                   conn.setRequestProperty("ENCTYPE", "multipart/form-data");
	                   conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
	                   conn.setRequestProperty("uploaded_file", fileName);
	                   dos = new DataOutputStream(conn.getOutputStream());
	                   dos.writeBytes(twoHyphens + boundary + lineEnd);
	                   dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
	                                             + fileName + "\"" + lineEnd);
	                   dos.writeBytes(lineEnd);
	                   // create a buffer of  maximum size
	                   bytesAvailable = fileInputStream.available();         
	                   bufferSize = Math.min(bytesAvailable, maxBufferSize);
	                   buffer = new byte[bufferSize];          
	                   // read file and write it into form...
	                   bytesRead = fileInputStream.read(buffer, 0, bufferSize); 	                      
	                   while (bytesRead > 0) {	                        
	                     dos.write(buffer, 0, bufferSize);
	                     bytesAvailable = fileInputStream.available();
	                     bufferSize = Math.min(bytesAvailable, maxBufferSize);
	                     bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
	                      
	                    }	          
	                   // send multipart form data necesssary after file data...
	                   dos.writeBytes(lineEnd);
	                   dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	          
	                   // Responses from the server (code and message)
	                   serverResponseCode = conn.getResponseCode();
	                   final String serverResponseMessage = conn.getResponseMessage();
	                   
	                   Log.i("uploadFile", "HTTP Response is : "
	                           + serverResponseMessage + ": " + serverResponseCode);    
	                   //get the content of the retuend data:
	                   dis = new DataInputStream(conn.getInputStream());
	                   final ByteArrayOutputStream bo = new ByteArrayOutputStream();
	                   buffer = new byte[1024];
	                   dis.read(buffer); // Read from Buffer.
	                   bo.write(buffer); // Write Into Buffer.
	                   
	                   CommonUtilities.appendLog("Server said: "+bo.toString().trim());
	                   
	                   if(serverResponseCode == 200){
	                        
	                       runOnUiThread(new Runnable() {
	                            public void run() {
	                                 
	                                String msg = "File Upload Completed.\n\n See uploaded file here: "
	                                              +" "+CommonUtilities.UPLOAD_SERVER_URL+"\n\nFilename: "
	                                              +fileName+ "\n\nServers answer: "+ bo.toString();
	                                 
	                                
	                                Toast.makeText(TakePhotoActivity.this, "File Upload Complete.",
	                                             Toast.LENGTH_SHORT).show();
	                            }
	                        });               
	                   }   
	                    
	                   //close the streams //
	                   fileInputStream.close();
	                   dos.flush();
	                   dos.close();
	                 
	                     
	              } catch (MalformedURLException ex) {
	                   
	                  mUploadDialog.dismiss(); 
	                  CommonUtilities.appendLog("MalformedURLException Exception : check script url.");
	                   
	                  Log.e("Upload file to server", "error: " + ex.getMessage(), ex); 
	              } catch (Exception e) {
	                   
	            	  mUploadDialog.dismiss(); 
	                  CommonUtilities.appendLog("Got Exception : "+e.getMessage());
	                  Log.e("Upload file to server Exception", "Exception : "
	                                                   + e.getMessage(), e); 
	              }
	           } 
	          
	          mUploadDialog.dismiss();
	          
	          if(wl.isHeld()){
		        	wl.release(); //when the activiy pauses, we should realse the wakelock
		        }
	          finish();
	          return serverResponseCode;
	          
	         }
}
