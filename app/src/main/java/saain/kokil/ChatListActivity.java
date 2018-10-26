package saain.kokil;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.widget.PopupMenu;
import android.widget.Toast;

import saain.kokil.XMPP.BitstreamConnection;
import saain.kokil.XMPP.BitstreamConnectionService;
import saain.kokil.model.Chat;
import saain.kokil.model.ChatModel;
import saain.kokil.model.adapters.ChatListAdapter;

public class ChatListActivity extends AppCompatActivity implements ChatListAdapter.onItemClickListener ,ChatListAdapter.onItemLongClickListener{

    private RecyclerView chatsRecyclerView;
    private FloatingActionButton newConversationButton;
    private static final String LOGTAG="Chatlist Activity";
    private Toolbar myToolbar;
    private ChatListAdapter mAdapter;
    protected static final int REQUEST=188;
    private NotificationManager notificationManager;
    private BroadcastReceiver broadcastReceiver;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);


        //Custom ToolBar

                myToolbar=findViewById(R.id.mytoolbar);
                setSupportActionBar(myToolbar);
                getSupportActionBar().setIcon(getDrawable(R.drawable.logo));
                myToolbar.setBackgroundColor(getResources().getColor(R.color.app_theme_color));

        boolean loggen_in_state= PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("xmpp_logged_in",false);

        if(!loggen_in_state){
            Log.d(LOGTAG,"Logged in state: "+loggen_in_state);
            Intent i=new Intent(ChatListActivity.this,SplashActivity.class);
            startActivity(i);
            finish();
        }
        else{
            if (!Utilities.isServiceRunning(BitstreamConnectionService.class,getApplicationContext())){
                Log.d(LOGTAG,"Service not running. Starting ....");
                Intent intent=new Intent(this,BitstreamConnectionService.class);
                startService(intent);
            }
            else {
                Log.d(LOGTAG,"Sevice is already Running");
            }
        }

        chatsRecyclerView=(RecyclerView)findViewById(R.id.chatsRecyclerView);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));


        mAdapter=new ChatListAdapter(getApplicationContext());
        chatsRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);    //CLick Listener
        mAdapter.setOnItemLongClickListener(this);  //Long Click Listener

        newConversationButton=(FloatingActionButton) findViewById(R.id.new_conversation_btn);
        newConversationButton.setBackgroundTintList(getResources().getColorStateList(R.color.warning));

        newConversationButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i=new Intent(ChatListActivity.this,ContactListActivity.class);
                startActivity(i);
            }
        });

        boolean deniedBatteryOpRequest=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("denied_battey_optimization_request",false);
        boolean user_has_gone_through_battery_optimizations=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("user_has_gone_through_battery_optimizations",false);

        Log.d(LOGTAG,"BATTERY: "+deniedBatteryOpRequest+" "+user_has_gone_through_battery_optimizations);

        if (!deniedBatteryOpRequest && !user_has_gone_through_battery_optimizations){
            Log.d(LOGTAG,"calling...............");
            requestBatteryOptimization();
        }
        else
            Log.d(LOGTAG,"BATTERy: "+deniedBatteryOpRequest+" "+user_has_gone_through_battery_optimizations);

    }

    //REQUEST BATTERY PERMISSIONS

    private void requestBatteryOptimization(){

        Log.d(LOGTAG,"requestBatteryOptimization() called");
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(ChatListActivity.this);
            builder.setTitle("Battery Optimization Request");
            builder.setMessage("Battery Optimization Permissions Required");

            builder.setPositiveButton(R.string.ignore_battery_allow, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Log.d(LOGTAG,"User clicked OK");
                    Intent intent=new Intent();
                    String packageName=getPackageName();

                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:"+packageName));
                    startActivityForResult(intent,REQUEST);
                }
            });
            builder.setNegativeButton(R.string.add_contact_cancel_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(LOGTAG,"Clicked cancel");
                    SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    preferences.edit().putBoolean("denied_battery_optimization_request",true).commit();
                    dialog.cancel();
                }
            });
            builder.show();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action=intent.getAction();
                switch (action){
                    case Constants.BroadcastMessages.UI_NEW_CHAT_ITEM:
                    mAdapter.onChatCountChange();
                    return;
                }
            }
        };

        IntentFilter filter=new IntentFilter(Constants.BroadcastMessages.UI_NEW_CHAT_ITEM);
        registerReceiver(broadcastReceiver,filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==REQUEST){
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    Intent intent=new Intent();
                    String packageName=getPackageName();
                    PowerManager powerManager=(PowerManager) getSystemService(getApplicationContext().POWER_SERVICE);

                    if (powerManager.isIgnoringBatteryOptimizations(packageName)){
                        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivity(intent);
                    }

                    SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    preferences.edit().putBoolean("user_has_gone_through_battery_optimizations",true).commit();
                }
            }
        }
        else{
            if ( requestCode == REQUEST)
            {
                Log.d(LOGTAG,"Result code is cancelled");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.activity_me_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.me){
            Intent i= new Intent(ChatListActivity.this,MeActivity.class);
            startActivity(i);

        }
        else if(item.getItemId()==R.id.logout){
            BitstreamConnectionService.getConnection().disconnect();

            Intent i= new Intent(ChatListActivity.this,SplashActivity.class);
            startActivity(i);
            finish();
        }


        return true;
    }

    @Override
    public void onItemClick(String contactJid,Chat.ContactType contactType) {

        Intent i=new Intent(ChatListActivity.this,ChatViewActivity.class);
        i.putExtra("contact_jid",contactJid);
        i.putExtra("contact_type",contactType);
        startActivity(i);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onItemLongClick(final String jid, final int uid, View anchor) {
        PopupMenu popupMenu=new PopupMenu(ChatListActivity.this,anchor, Gravity.CENTER);
        popupMenu.getMenuInflater().inflate(R.menu.chat_list_popup_menu,popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.delete_chat:
                        if(ChatModel.get(getApplicationContext()).deleteChat(uid)){
                            mAdapter.onChatCountChange();
                            Toast.makeText(ChatListActivity.this,"Chat Deleted",Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(ChatListActivity.this,"Cannot Delete Chat "+uid,Toast.LENGTH_SHORT).show();

                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }
}
