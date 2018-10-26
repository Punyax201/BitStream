package saain.kokil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.jivesoftware.smackx.chatstates.ChatStateListener;

import saain.kokil.Encryption.Encryptor;
import saain.kokil.XMPP.BitstreamConnection;
import saain.kokil.XMPP.BitstreamConnectionService;
import saain.kokil.model.Chat;
import saain.kokil.model.ChatMessage;
import saain.kokil.model.ChatMessagesModel;
import saain.kokil.model.ChatModel;
import saain.kokil.model.Contact;
import saain.kokil.model.ContactModel;
import saain.kokil.model.adapters.ChatMessageAdapter;

public class ChatViewActivity extends AppCompatActivity implements ChatMessageAdapter.OnInformRecyclerViewToScrollDownListener,ChatMessageAdapter.OnItemLongClickListener {

    private static final String LOGTAG="ChatView Activity";
    RecyclerView chatMessagesRecyclerView;
    EditText sendText;
    ImageButton sendButton;
    private BroadcastReceiver broadcastReceiver;
    ChatMessageAdapter adapter;
    private String counterpartJid;

    //snackbar
    private View snackbar;
    private View snackbarStranger;
    private TextView snackbarActionAccept;
    private TextView SnackbarActionReject;
    private TextView snackbarAddStrangerContact;
    private TextView snackbarBlock;

    private Chat.ContactType contactType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_view);

        //Get counterpart JID
        Intent intent=getIntent();
        counterpartJid=intent.getStringExtra("contact_jid");
        contactType=(Chat.ContactType)intent.getSerializableExtra("contact_type");
        setTitle(counterpartJid);

        chatMessagesRecyclerView=(RecyclerView)findViewById(R.id.chatsRecyclerView);
        chatMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        adapter=new ChatMessageAdapter(getApplicationContext(),counterpartJid);
        chatMessagesRecyclerView.setAdapter(adapter);
        adapter.setOnInformRecyclerViewToScrollDownListener(this);
        adapter.setOnItemLongClickListener(this);



        sendText=(EditText)findViewById(R.id.textinput);
        sendButton=(ImageButton)findViewById(R.id.textSendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //  ChatMessagesModel.get(getApplicationContext()).addMessage(new ChatMessage(sendText.getText().toString(),System.currentTimeMillis(),ChatMessage.Type.SENT,"pk@ss.com"));
                BitstreamConnectionService.getConnection().sendMessage(sendText.getText().toString(),counterpartJid);
                sendText.setText("");
                adapter.onMessageAdd();
            }
        });

        Contact contactCheck=ContactModel.get(getApplicationContext()).getContactByJidString(counterpartJid);




        snackbar=findViewById(R.id.snackbar);
        snackbarStranger=findViewById(R.id.snackbar_stranger);

        if (!ContactModel.get(getApplicationContext()).isStranger(counterpartJid)){

           if(contactCheck.isOnlineStatus()){
               sendButton.setImageDrawable(ContextCompat.getDrawable(ChatViewActivity.this,R.drawable.online_send_btn));
        }
        else {
            sendButton.setImageDrawable(ContextCompat.getDrawable(ChatViewActivity.this,R.drawable.offline_send_btn));
        }

            snackbarStranger.setVisibility(View.GONE);
            Log.d(LOGTAG,counterpartJid+" is not a stranger");
            Contact contact=ContactModel.get(this).getContactByJidString(counterpartJid);

            if(contact.isPendingFrom()){
                int paddingBottom=getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_normal);
                chatMessagesRecyclerView.setPadding(0,0,0,paddingBottom);
                snackbar.setVisibility(View.VISIBLE);
            }
            else {
                int paddingBottom=getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_normal);
                chatMessagesRecyclerView.setPadding(0,0,0,paddingBottom);
                snackbar.setVisibility(View.GONE);
            }
        }
        else{
            if(contactType== Chat.ContactType.STRANGER){
                int paddingBottom=getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_huge);
                chatMessagesRecyclerView.setPadding(0,0,0,paddingBottom);
                snackbar.setVisibility(View.GONE);
                snackbarStranger.setVisibility(View.VISIBLE);
            }
            else{
                int paddingBottom=getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_huge);
                chatMessagesRecyclerView.setPadding(0,0,0,paddingBottom);
                snackbar.setVisibility(View.VISIBLE);
                snackbarStranger.setVisibility(View.GONE);

            }
        }
        snackbarActionAccept=(TextView)findViewById(R.id.snackbar_action_accept);
        snackbarActionAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContactModel.get(getApplicationContext()).isStranger(counterpartJid)){
                    if(ContactModel.get(getApplicationContext()).addContact(new Contact(counterpartJid,Contact.SubscriptionStatus.NONE))){
                        Log.d(LOGTAG,"Stranger contact added");
                    }
                }

                if(BitstreamConnectionService.getConnection().subscribed(counterpartJid)){
                    ContactModel.get(getApplicationContext()).updateContactSubscriptionOnSendSubscribed(counterpartJid);
                    Toast.makeText(ChatViewActivity.this,"Subscription From: "+counterpartJid,Toast.LENGTH_SHORT).show();
                }
                snackbar.setVisibility(View.GONE);
            }
        });

        SnackbarActionReject=(TextView)findViewById(R.id.snackbar_action_deny);
        SnackbarActionReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOGTAG,"Subscription Rejected from "+counterpartJid);

                if(BitstreamConnectionService.getConnection().unsubscribed(counterpartJid)){

                    ContactModel.get(getApplicationContext()).updateContactSubscriptionOnSendSubscribed(counterpartJid);
                    Toast.makeText(ChatViewActivity.this,"Subscription Denied",Toast.LENGTH_SHORT);

                }
                snackbar.setVisibility(View.GONE);
            }
        });

        snackbarAddStrangerContact=(TextView)findViewById(R.id.snackbar_action_accept_stranger);
        snackbarAddStrangerContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContactModel.get(getApplicationContext()).addContact(new Contact(counterpartJid,Contact.SubscriptionStatus.NONE))){
                    if(BitstreamConnectionService.getConnection().addContactToRoster(counterpartJid)){
                        snackbarStranger.setVisibility(View.GONE);
                        StyleableToast styleableToast=new StyleableToast(getApplicationContext(),counterpartJid+" added to Contacts",Toast.LENGTH_SHORT);
                        styleableToast.setIcon(R.drawable.emoji_smiling);
                        styleableToast.show();
                    }
                }
            }
        });

        snackbarBlock=(TextView)findViewById(R.id.snackbar_action_deny_stranger);
        snackbarBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(ChatViewActivity.this,"We don't like Blocking...",Toast.LENGTH_SHORT).show();
                StyleableToast styleableToast=new StyleableToast(getApplicationContext(),"We don't like Blocking...",Toast.LENGTH_SHORT);
                styleableToast.setIcon(R.drawable.emoji);
                styleableToast.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.contact_details_chat_view){
            Intent intent=new Intent(ChatViewActivity.this,ContactDetailsActivity.class);
            intent.putExtra("contact_jid",counterpartJid);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.informRecyclerViewToScrollDown();
        broadcastReceiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action=intent.getAction();
                switch (action){
                    case Constants.BroadcastMessages.UI_NEW_MESSAGE_FLAG:
                        adapter.onMessageAdd();
                        return;
                    case Constants.BroadcastMessages.UI_ONLINE_STATUS_CHANGED:
                        String contactJid=intent.getStringExtra(Constants.ONLINE_STATUS_CHANGE_CONTACT);

                        if(counterpartJid.equals(contactJid)){
                            Contact contact=ContactModel.get(getApplicationContext()).getContactByJidString(contactJid);
                            if(!contact.isOnlineStatus()){
                                sendButton.setImageDrawable(ContextCompat.getDrawable(ChatViewActivity.this,R.drawable.online_send_btn));
                            }
                            else {
                                sendButton.setImageDrawable(ContextCompat.getDrawable(ChatViewActivity.this,R.drawable.offline_send_btn));

                            }
                        }
                }
            }
        };
        IntentFilter filter=new IntentFilter(Constants.BroadcastMessages.UI_NEW_MESSAGE_FLAG);
        filter.addAction(Constants.BroadcastMessages.UI_ONLINE_STATUS_CHANGED);
        registerReceiver(broadcastReceiver,filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.activity_chat_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onInformRecyclerViewToScrollDown(int size) {
        chatMessagesRecyclerView.scrollToPosition(size-1);
    }

    //LONG CLICK
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onItemLongClick(final int uid, View anchor) {
        PopupMenu popupMenu=new PopupMenu(ChatViewActivity.this,anchor, Gravity.CENTER);
        popupMenu.getMenuInflater().inflate(R.menu.chat_view_popup_menu,popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.delete_message:
                        if(ChatMessagesModel.get(getApplicationContext()).deleteMessage(uid)){
                            Toast.makeText(ChatViewActivity.this,"Message Deleted",Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(ChatViewActivity.this,"Cannot Delete Message "+uid,Toast.LENGTH_SHORT).show();

                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

}
