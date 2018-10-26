package saain.kokil.XMPP;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

import saain.kokil.Constants;

public class BitstreamConnectionService extends Service {

    private static final String LOGTAG="Connection Service";
    private boolean active;
    private Thread mThread;
    private Handler mHandler;
    private static BitstreamConnection connection;

    public static BitstreamConnection getConnection() {
        return connection;
    }

    public BitstreamConnectionService(){

    }

    public void initConnection(){
        Log.d(LOGTAG,"initConnection()");
        if(connection==null){
            connection=new BitstreamConnection(this);
        }
        try {
            connection.connect();
        } catch (Exception e) {
            Log.d(LOGTAG,"Error..........."+e.getMessage());
            Intent i=new Intent(Constants.BroadcastMessages.UI_CONNECTION_ERROR);
            i.setPackage(getApplicationContext().getPackageName());
            getApplication().sendBroadcast(i);

            Log.d(LOGTAG,"Error Broadcast Sent");

            boolean logged_in_state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("xmpp_logged_in",false);
            if(!logged_in_state){
                Log.d(LOGTAG,"Logged in state:"+logged_in_state+" calling stopSelf()");
                stopSelf();
            }
            else {
                Log.d(LOGTAG,"Logged in state:"+logged_in_state);
            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(LOGTAG,"Service Created");
        super.onCreate();

        ServerPingWithAlarmManager.onCreate(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOGTAG,"Service Start Command");
        start();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ServerPingWithAlarmManager.onDestroy();
        stop();
    }

    public void start(){
        Log.d(LOGTAG,"Start() called...."+active);
        if(!active){
            active=true;
            if(mThread==null || !mThread.isAlive()){
                mThread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        mHandler=new Handler();
                        initConnection();
                        Looper.loop();
                    }
                });
                mThread.start();
            }
        }
    }

    public void stop(){
        Log.d(LOGTAG,"Stop() called.....");
        active=false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(connection!=null){
                    connection.disconnect();
                }
            }
        });
    }
}
