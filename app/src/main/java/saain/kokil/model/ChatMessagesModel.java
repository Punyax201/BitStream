package saain.kokil.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import saain.kokil.persistence.ChatMessageCursorWrapper;
import saain.kokil.persistence.DatabaseBack;

public class ChatMessagesModel {


    private static ChatMessagesModel schatMessagesModel;
    private Context context;
    private static final String LOGTAG="ChatMessagesModel";

    List<ChatMessage> messages;

    //DATABASESQLiteDatabase
    private SQLiteDatabase database;

    public static ChatMessagesModel get(Context context) {
        if (schatMessagesModel == null) {
            schatMessagesModel = new ChatMessagesModel(context);
        }

        return schatMessagesModel;
    }

    private ChatMessagesModel(Context context) {
        this.context = context;
        database= DatabaseBack.getInstance(context).getWritableDatabase();
    }

    public List<ChatMessage> getMessages(String counterpartJID){
        List<ChatMessage> messages=new ArrayList<>();

        ChatMessageCursorWrapper cursorWrapper=queryMessages("jid= ?",new    String[] {counterpartJID});

        try{
            cursorWrapper.moveToFirst();
            while(!cursorWrapper.isAfterLast()){
                messages.add(cursorWrapper.getChatMessage());
                cursorWrapper.moveToNext();
            }
        }
        finally {
            cursorWrapper.close();
        }
        return messages;
    }

    public boolean addMessage(ChatMessage message){

        ContentValues contentValues=message.getContentValue();
        if(database.insert(ChatMessage.TABLE_NAME,null,contentValues)==-1){
            return false;
        }
        else{
            ChatModel.get(context).updateLastMessageDetails(message);
            return true;
        }

    }

    //DELETE MESSAGE

    public boolean deleteMessage(int uid){
        int value=database.delete(ChatMessage.TABLE_NAME,ChatMessage.Cols.CHAT_UNIQUE_ID+"=?",new String[]{String.valueOf(uid)});

        if(value==1){
            Log.d(LOGTAG,"DELETED Message");
            return true;
        }
        else
            Log.d(LOGTAG,"Failed to DELETE Message");

        return false;
    }

    private ChatMessageCursorWrapper queryMessages(String where,String[] args){
        Cursor cursor=database.query(ChatMessage.TABLE_NAME,null,where,args,null,null,null);

        return new ChatMessageCursorWrapper(cursor);
    }
}
