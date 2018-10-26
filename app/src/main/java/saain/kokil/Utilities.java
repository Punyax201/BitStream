package saain.kokil;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.sql.Time;
import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

public class Utilities {

    public static boolean isServiceRunning(Class<?> serviceClass, Context context){
        ActivityManager manager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo:manager.getRunningServices(Integer.MAX_VALUE)){
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        return networkInfo!=null && networkInfo.isConnected();
    }

    public static String getFormattedTime(long timestamp){

        android.text.format.DateFormat dateFormat=new android.text.format.DateFormat();
        long oneDayinMillis= TimeUnit.DAYS.toMillis(1);
        long timeDiff=System.currentTimeMillis()-timestamp;

        return timeDiff<oneDayinMillis? dateFormat.format("hh:mm a",timestamp).toString():
                dateFormat.format("dd MMM-hh:mm a",timestamp).toString();
    }
}
