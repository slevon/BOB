package de.slevon.bob;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
 
public final class CommonUtilities {
     
    // give your server registration url here
    static final String REGISTER_SERVER_URL = "http://slevon.de/huette/camera/register.php";
 
    // Google project id
    static final String SENDER_ID = "449474277547";
 
    
    public static int APPVERSION;
    
    /**
     * Tag used on log messages.
     */
    static final String TAG = "ROMAN";
 
    static final String DISPLAY_MESSAGE_ACTION =
            "de.slevon.bob.DISPLAY_MESSAGE";
 
    static final String EXTRA_MESSAGE = "message";
    
    static final String UPLOAD_SERVER_URL="http://slevon.de/huette/upload.php";
 
    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
    
    public static void appendLog(String text)
    {       
       File logFile = new File("/sdcard/cam_shots/camera.log");
       if (!logFile.exists())
       {
          try
          {
        	 logFile.mkdirs();
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
          buf.append(text);
          buf.newLine();
          buf.close();
       }
       catch (IOException e)
       {
          // TODO Auto-generated catch block
          e.printStackTrace();
       }
    }
}
