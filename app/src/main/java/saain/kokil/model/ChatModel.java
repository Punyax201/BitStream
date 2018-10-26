package saain.kokil.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;

import saain.kokil.persistence.ChatCursorWrapper;
import saain.kokil.persistence.DatabaseBack;

public class ChatModel {

    private static final String LOGTAG="ChatModel";

    private static ChatModel sChatsModel;
    private Context mContext;
    private SQLiteDatabase database;

    public static ChatModel get(Context c){
        if(sChatsModel==null){
            sChatsModel=new ChatModel(c);
        }

        return sChatsModel;
    }

    private ChatModel(Context c){
        mContext=c;
        database= DatabaseBack.getInstance(mContext).getWritableDatabase();
    }

    //GET CHATS LIST

    public List<Chat> getChats(){
        List<Chat> chats=new ArrayList<>();
        ChatCursorWrapper cursor=queryChats(null,null);

        try {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                chats.add(cursor.getChat());
                Log.d(LOGTAG,"chats........"+String.valueOf(cursor.getChat().getPersistID()));
                cursor.moveToNext();
            }
        }
        finally {
            cursor.close();
        }
        return chats;

    }

    //GET CHAT LIST BY JID

    public List<Chat> getChatsByJID(String jid){
        List<Chat> chats=new ArrayList<>();
        ChatCursorWrapper cursorWrapper=queryChats(Chat.Cols.CONTACT_JID+"=?",new String[]{jid});

        try{
            cursorWrapper.moveToFirst();
            while(!cursorWrapper.isAfterLast()){
                chats.add(cursorWrapper.getChat());

                cursorWrapper.moveToNext();
            }

        }
        finally {
            cursorWrapper.close();
        }
        return chats;
    }

    //Add Chat

    public boolean addChat(Chat chat){
        ContentValues values=chat.getContentValue();
        Log.d(LOGTAG,"----------------------------------CONTENT VALUES-------------------"+values.toString()+values);
        if(database.insert(Chat.TABLE_NAME,null,values)==-1){
            return false;
        }
        else
            return true;
    }

    public boolean deleteChat(int uid){
        int value=database.delete(Chat.TABLE_NAME,Chat.Cols.CHAT_UNIQUE_ID+"=?",new String[]{String.valueOf(uid)});
        Log.d(LOGTAG,"Trying to delete..."+String.valueOf(uid));
        if(value==1){
            Log.d(LOGTAG,"Message Deleted");
            return true;
        }
        else {
            Log.d(LOGTAG,"Failed to delete Message");
            return false;
        }
    }

    public boolean deleteChat(Chat chat){
        Log.d(LOGTAG,"Trying to delete "+chat.getPersistID());
        return deleteChat(chat.getPersistID());
    }

    //CursorWrapper

    private ChatCursorWrapper queryChats(String where,String[] args){
        Cursor cursor=database.query(Chat.TABLE_NAME,null,where,args,null,null,null);
        return new ChatCursorWrapper(cursor);
    }

    //Update Last Method

    public boolean updateLastMessageDetails(ChatMessage message){
        List<Chat> chats=getChatsByJID(message.getContacJid());
        if(!chats.isEmpty()){
            Chat chat=chats.get(0);
            chat.setLastMessageTime(message.getTimestamp());
            chat.setLastmessage(message.getMessage());

            ContentValues values=chat.getContentValue();

            int update=database.update(Chat.TABLE_NAME,values,Chat.Cols.CHAT_UNIQUE_ID+"=?",new String[]{String.valueOf(chat.getPersistID())});

            if(update==1){
                Log.d(LOGTAG,"ChatMessage Updated");
                return true;
            }
            else {
                Log.d(LOGTAG,"ChatMessage Update failed "+values.toString());
                return false;
            }
        }
        return false;
    }

}
