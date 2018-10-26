package saain.kokil.model.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.List;

import saain.kokil.R;
import saain.kokil.Utilities;
import saain.kokil.model.Chat;
import saain.kokil.model.ChatModel;

public class ChatListAdapter extends RecyclerView.Adapter<ChatHolder> {

     public interface onItemClickListener{
         public void onItemClick(String contactJid,Chat.ContactType contactType);
     }

     public interface onItemLongClickListener{
         public void onItemLongClick(String jid,int uid,View anchor);
     }

    List<Chat> chats;
     private static onItemClickListener onItemClickListener;
     private static onItemLongClickListener onItemLongClickListener;
     private Context context;

    public ChatListAdapter(Context context){
        this.chats= ChatModel.get(context).getChats();
        this.context=context;
    }


    //Interface Getters and Setters

    public static onItemClickListener getOnItemClickListener(){
        return onItemClickListener;
    }

    public void setOnItemClickListener(onItemClickListener onItemClickListener){
        this.onItemClickListener=onItemClickListener;
    }

    public static ChatListAdapter.onItemLongClickListener getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    public static void setOnItemLongClickListener(ChatListAdapter.onItemLongClickListener onItemLongClickListener) {
        ChatListAdapter.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.chat_lists_item,parent,false);

        return new ChatHolder(view,this);
    }

    @Override
    public void onBindViewHolder(ChatHolder holder, int position) {
        Chat chat=chats.get(position);
        holder.bindChat(chat);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public void onChatCountChange(){
        chats=ChatModel.get(context).getChats();
        notifyDataSetChanged();
    }
}

class ChatHolder extends RecyclerView.ViewHolder{

    private static final String LOGTAG="ChatHolder";
    private TextView contact;
    private TextView messageAbstractTextView;
    private TextView timestamp;
    private ImageView profile;
    private Chat mChat;
    private ChatListAdapter chatListAdapter;

    public ChatHolder(final View itemView, ChatListAdapter adapter) {
        super(itemView);

        contact=(TextView)itemView.findViewById(R.id.contact_jid);
        messageAbstractTextView=(TextView)itemView.findViewById(R.id.message_abstract);
        timestamp=(TextView)itemView.findViewById(R.id.text_message_timestamp);
        profile=(ImageView)itemView.findViewById(R.id.profile);
        chatListAdapter=adapter;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOGTAG,"Clicked in Rec View");
                ChatListAdapter.onItemClickListener listener=ChatListAdapter.getOnItemClickListener();

                if(listener!=null){
                    listener.onItemClick(contact.getText().toString(),mChat.getContactType());
                }
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v) {
                ChatListAdapter.onItemLongClickListener longClickListener=chatListAdapter.getOnItemLongClickListener();
                if(longClickListener!=null){
                    longClickListener.onItemLongClick(mChat.getJid(),mChat.getPersistID(),itemView);
                    return true;
                }
                return false;
            }
        });
    }

    public void bindChat(Chat c){
        mChat=c;
        contact.setText(c.getJid());
        messageAbstractTextView.setText(c.getLastmessage());
        profile.setImageResource(R.drawable.user);
        timestamp.setText(Utilities.getFormattedTime(c.getLastMessageTime()));
    }
}