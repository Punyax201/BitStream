package saain.kokil;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import saain.kokil.XMPP.BitstreamConnection;
import saain.kokil.XMPP.BitstreamConnectionService;
import saain.kokil.model.Contact;
import saain.kokil.model.ContactModel;

public class ContactDetailsActivity extends AppCompatActivity {

    private static final String LOGTAG="ContactDetails Activity";
    private String contactJID;
    private ImageView profileImage;
    private CheckBox fromCheckbox;
    private CheckBox toCheckbox;
    private Context mApplicationContext;
    private TextView pendingFrom;
    private TextView pendingTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        mApplicationContext=getApplicationContext();

        Intent intent=getIntent();
        contactJID=intent.getStringExtra("contact_jid");
        setTitle(contactJID);



        pendingFrom=(TextView) findViewById(R.id.pending_from);
        pendingTo=(TextView) findViewById(R.id.pending_to);

        fromCheckbox=(CheckBox) findViewById(R.id.them_to_me);
        fromCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fromCheckbox.isChecked()){

                }
                else {
                    if(BitstreamConnectionService.getConnection().unsubscribed(contactJID)){
                        Toast.makeText(mApplicationContext,"Stopped sending Presence Update to "+contactJID,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        toCheckbox=(CheckBox) findViewById(R.id.me_to_tem);
        toCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toCheckbox.isChecked()){
                    if(BitstreamConnectionService.getConnection().subscribe(contactJID)){
                        Toast.makeText(mApplicationContext,"Subscription Request sent to "+contactJID,Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    if(BitstreamConnectionService.getConnection().unsubscribe(contactJID)){
                        Toast.makeText(mApplicationContext,"Successfully Unsubscribed to "+contactJID,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        if(!ContactModel.get(getApplicationContext()).isStranger(contactJID)) {
            Contact contact= ContactModel.get(getApplicationContext()).getContactByJidString(contactJID);
            Contact.SubscriptionStatus subscriptionStatus=contact.getSubStatus();


            if (subscriptionStatus == Contact.SubscriptionStatus.NONE) {
                fromCheckbox.setEnabled(false);
                fromCheckbox.setChecked(false);
                toCheckbox.setChecked(false);
            } else if (subscriptionStatus == Contact.SubscriptionStatus.FROM) {
                fromCheckbox.setEnabled(true);
                fromCheckbox.setChecked(true);
                toCheckbox.setChecked(false);
            } else if (subscriptionStatus == Contact.SubscriptionStatus.TO) {
                fromCheckbox.setEnabled(false);
                fromCheckbox.setChecked(false);
                toCheckbox.setChecked(true);
            } else {
                fromCheckbox.setEnabled(true);
                fromCheckbox.setChecked(true);
                toCheckbox.setChecked(true);
            }

            if (contact.isPendingFrom()) {
                pendingFrom.setVisibility(View.VISIBLE);
            } else {
                pendingFrom.setVisibility(View.GONE);
            }

            if (contact.isPendingTo()) {
                pendingTo.setVisibility(View.VISIBLE);
            } else {
                pendingTo.setVisibility(View.GONE);
            }
        }

        else {
            fromCheckbox.setEnabled(false);
            fromCheckbox.setChecked(false);
            toCheckbox.setChecked(false);
            toCheckbox.setEnabled(true);
        }
    }
}
