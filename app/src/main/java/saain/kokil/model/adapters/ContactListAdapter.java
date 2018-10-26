package saain.kokil.model.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import saain.kokil.R;
import saain.kokil.model.Contact;
import saain.kokil.model.ContactModel;

public class ContactListAdapter extends RecyclerView.Adapter<ContactHolder> {

    public interface OnItemClickListener{
        public void onItemClick(String contactJid);
    }

    //LongClick
    public interface OnItemLongClickListener{
        public void onItemLongClick(int uid,String jid,View anchor);
    }

    private List<Contact> contacts;
    private Context context;
    private static final String LOGTAG="ContactList Adapter";
    private OnItemClickListener onItemClickListener;

    private OnItemLongClickListener onItemLongClickListener;

    public ContactListAdapter(Context context){
        contacts= ContactModel.get(context).getContactlist();
        this.context=context;
        Log.d(LOGTAG,"ContactListAdapter Size of List:"+contacts.size());
    }

    public OnItemClickListener getmOnItemClickListener(){
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public OnItemLongClickListener getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.contact_list_item,parent,false);

        return new ContactHolder(view,this);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactHolder holder, int position) {
        Contact contact=contacts.get(position);
        holder.bindContact(contact);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void onContactCountChange(){
        contacts=ContactModel.get(context).getContactlist();
        notifyDataSetChanged();
    }
}
class ContactHolder extends RecyclerView.ViewHolder{

    private TextView jidText;
    private TextView subscription;
    private Contact mcontact;
    private ImageView profile;
    private ContactListAdapter madapter;
    public static final String LOGTAG="ContactList Adapter";

    public ContactHolder(final View itemView, final ContactListAdapter adapter) {
        super(itemView);

        madapter=adapter;

        jidText=(TextView)itemView.findViewById(R.id.contact_jid_string);
        subscription=(TextView)itemView.findViewById(R.id.suscription_type);
        profile=(ImageView)itemView.findViewById(R.id.profile_contact);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOGTAG,"User Clicked on Contact Item");
                ContactListAdapter.OnItemClickListener listener=madapter.getmOnItemClickListener();

                if(listener!=null){
                    Log.d(LOGTAG,"Calling Listener...");
                    listener.onItemClick(jidText.getText().toString());
                }
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ContactListAdapter.OnItemLongClickListener listener=adapter.getOnItemLongClickListener();
                if(listener!=null){
                    adapter.getOnItemLongClickListener().onItemLongClick(mcontact.getPersistID(),mcontact.getJid(),itemView);
                    return true;
                }
                return false;
            }
        });
    }

    public void bindContact(Contact c){
        mcontact=c;

        if(mcontact==null){
            return;
        }
        else{
            jidText.setText(mcontact.getJid());
            Log.d("CONTACT JID:",mcontact.getJid());
            subscription.setText("NONE NONE");
            profile.setImageResource(R.drawable.ic_profile);
        }
    }
}
