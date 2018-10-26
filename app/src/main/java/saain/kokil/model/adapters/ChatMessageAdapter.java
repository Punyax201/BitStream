package saain.kokil.model.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.List;

import saain.kokil.R;
import saain.kokil.Utilities;
import saain.kokil.XMPP.BitstreamConnection;
import saain.kokil.XMPP.BitstreamConnectionService;
import saain.kokil.model.ChatMessage;
import saain.kokil.model.ChatMessagesModel;
import saain.kokil.model.ChatModel;
import saain.kokil.model.ChatStateChangedListener;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageViewHolder>{





    //To Scroll on Message Send/Receive

    public interface OnInformRecyclerViewToScrollDownListener{
        public void onInformRecyclerViewToScrollDown(int size);
    }

    ////////////

    //On LONG CLICK
    public interface OnItemLongClickListener{
        public void onItemLongClick(int uid, View anchor);
    }


    private static final int SENT=1;
    private static final int RECEIVED=2;
    private static final String LOGTAG="ChatMessageAdapter";
    private List<ChatMessage> chatMessageList;
    private LayoutInflater layoutInflater;
    private Context context;
    private String contactJid;
    private OnInformRecyclerViewToScrollDownListener onInformRecyclerViewToScrollDownListener;
    private OnItemLongClickListener onItemLongClickListener;


    //GETTER SETTER FOR LONGCLICK LISTENER


    public OnItemLongClickListener getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnInformRecyclerViewToScrollDownListener(OnInformRecyclerViewToScrollDownListener onInformRecyclerViewToScrollDownListener) {
        this.onInformRecyclerViewToScrollDownListener = onInformRecyclerViewToScrollDownListener;
    }

    public ChatMessageAdapter(Context context, String contactJid){
        this.layoutInflater=LayoutInflater.from(context);
        this.contactJid=contactJid;
        this.context=context;

        chatMessageList= ChatMessagesModel.get(context).getMessages(contactJid);
        Log.d(LOGTAG,"Getting chat messages for: "+contactJid);
    }



    public void informRecyclerViewToScrollDown(){
        onInformRecyclerViewToScrollDownListener.onInformRecyclerViewToScrollDown(chatMessageList.size());
        Log.d(LOGTAG,"Size of list: "+chatMessageList.size());
    }



    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View ItemView1;

        if(viewType==SENT){
            ItemView1=layoutInflater.inflate(R.layout.chat_message_sent,parent,false);

            return  new ChatMessageViewHolder(ItemView1,this);
        }
        else if(viewType==RECEIVED){
            ItemView1=layoutInflater.inflate(R.layout.chat_message_received,parent,false);

            return  new ChatMessageViewHolder(ItemView1,this);
        }
        else{
            ItemView1=layoutInflater.inflate(R.layout.chat_message_sent,parent,false);

            return  new ChatMessageViewHolder(ItemView1,this);
        }
    }

    @Override
    public void onBindViewHolder(ChatMessageViewHolder holder, int position) {
        ChatMessage message=chatMessageList.get(position);
        holder.bindChat(message);
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage.Type messageType=chatMessageList.get(position).getMessageType();

        if(messageType==ChatMessage.Type.SENT){
            return SENT;
        }
        else{
            return RECEIVED;
        }
    }

    public void onMessageAdd() {
        chatMessageList= ChatMessagesModel.get(context).getMessages(contactJid);
        notifyDataSetChanged();
        informRecyclerViewToScrollDown();
    }
}

class ChatMessageViewHolder extends RecyclerView.ViewHolder{

    private TextView messageBody,messageTimeStamp;
    private ImageView profile;
    private ChatMessage chatMessage;

    public ChatMessageViewHolder(final View itemView, final ChatMessageAdapter adapter) {
        super(itemView);

        messageBody=(TextView)itemView.findViewById(R.id.text_message_body);
        messageTimeStamp=(TextView) itemView.findViewById(R.id.text_message_timestamp);
        profile=(ImageView)itemView.findViewById(R.id.profile);

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            ChatMessageAdapter.OnItemLongClickListener longClickListener=adapter.getOnItemLongClickListener();
            @Override
            public boolean onLongClick(View v) {
                longClickListener.onItemLongClick(chatMessage.getPersistID(),itemView);
                return true;
            }
        });


    }

    public void bindChat(ChatMessage chatMessage){
        this.chatMessage=chatMessage;
        messageBody.setText(chatMessage.getMessage());
        messageTimeStamp.setText(Utilities.getFormattedTime(chatMessage.getTimestamp()));
        profile.setImageResource(R.drawable.user);

        ChatMessage.Type type=chatMessage.getType();


    }
}
