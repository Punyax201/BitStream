package saain.kokil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import saain.kokil.XMPP.BitstreamConnection;
import saain.kokil.XMPP.BitstreamConnectionService;

public class MeActivity extends AppCompatActivity {


    private TextView connectionStatus;
    private BroadcastReceiver broadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        BitstreamConnection connection=BitstreamConnectionService.getConnection();
        connectionStatus=(TextView) findViewById(R.id.connection_status);
        //Connection Status
        String status;
        if(connection!=null){
            status=connection.getConnectionStateString();
            connectionStatus.setText(status);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action=intent.getAction();
                switch (action){
                    case Constants.BroadcastMessages.UI_CONNECTION_STATUS_CHANGE_FLAG:
                        String status=intent.getStringExtra(Constants.UI_CONNECTION_STATUS_CHANGE);
                        connectionStatus.setText(status);
                        break;
                }
            }
        };
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constants.BroadcastMessages.UI_CONNECTION_STATUS_CHANGE_FLAG);
        this.registerReceiver(broadcastReceiver,filter);
    }
}
