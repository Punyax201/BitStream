package saain.kokil;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.List;
import java.util.zip.Inflater;

import saain.kokil.XMPP.BitstreamConnection;
import saain.kokil.XMPP.BitstreamConnectionService;
import saain.kokil.model.Chat;
import saain.kokil.model.ChatModel;
import saain.kokil.model.Contact;
import saain.kokil.model.ContactModel;
import saain.kokil.model.adapters.ContactListAdapter;

public class ContactListActivity extends AppCompatActivity implements ContactListAdapter.OnItemClickListener,ContactListAdapter.OnItemLongClickListener{

    private RecyclerView contactListRecyclerView;
    private static final String LOGTAG="ContactList Activity";
    ContactListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.new_contact_button);
        fab.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOGTAG,"Add BTN Clicked..........");
                addContact();
            }
        });

        contactListRecyclerView=(RecyclerView)findViewById(R.id.contact_list_recycler_view);
        contactListRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        adapter=new ContactListAdapter(getApplicationContext());
        contactListRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
    }

    private void addContact(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Jabber ID");

        final EditText input=new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(ContactModel.get(getApplicationContext()).addContact(new Contact(input.getText().toString(),Contact.SubscriptionStatus.NONE))){
                    Log.d(LOGTAG,"Contact Added");
                    adapter.onContactCountChange();
                }
                else {
                    Log.d(LOGTAG,"Failed to add Contact");
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    public void onItemClick(String contactJid) {
        Log.d(LOGTAG,"Chat Message received with Time Stamp:"+Utilities.getFormattedTime(System.currentTimeMillis()));
        List<Chat> chats= ChatModel.get(getApplicationContext()).getChatsByJID(contactJid);
        if(chats.size()==0){
            Chat chat=new Chat(contactJid,"",Chat.ContactType.ONE_ON_ONE,System.currentTimeMillis(),0);
            ChatModel.get(getApplicationContext()).addChat(chat);

            Intent intent=new Intent(ContactListActivity.this,ChatViewActivity.class);
            intent.putExtra("contact_jid",contactJid);
            startActivity(intent);
            finish();
        }
        else {
            Intent intent=new Intent(ContactListActivity.this,ChatViewActivity.class);
            intent.putExtra("contact_jid",contactJid);
            startActivity(intent);
            finish();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onItemLongClick(final int uid,final String jid, View anchor) {
        PopupMenu popupMenu=new PopupMenu(ContactListActivity.this,anchor, Gravity.CENTER);
        popupMenu.getMenuInflater().inflate(R.menu.contact_list_popup_menu,popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.delete_contact){
                    if(ContactModel.get(getApplicationContext()).deleteContact(uid)){
                        adapter.onContactCountChange();

                        if(BitstreamConnectionService.getConnection().removeRosterEntry(jid)){
                            Log.d(LOGTAG,jid+" deleted from Roster");
                            Toast.makeText(ContactListActivity.this,"Contact Deleted",Toast.LENGTH_SHORT).show();
                        }

                    }

                }

                else if(item.getItemId()==R.id.contact_details){
                    Intent intent=new Intent(ContactListActivity.this,ContactDetailsActivity.class);
                    intent.putExtra("contact_jid",jid);
                    startActivity(intent);
                }
                return true;
            }
        });
        popupMenu.show();
    }
}
