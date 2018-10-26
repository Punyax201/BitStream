package saain.kokil.model;

import android.content.ContentValues;

import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

public class ChatMessage {

    private String message;
    private long timestamp;
    private String contacJid;
    private Type type;
    private int persistID;
    public static final String TABLE_NAME="chatmessage";
    //Cols Inner class

    public static final class Cols{
        public static final String CHAT_UNIQUE_ID="id";
        public static final String CONTACT_JID="jid";
        public static final String MESSAGE_TYPE="Type";
        public static final String MESSAGE="msg";
        public static final String TIME_STAMP="ltstamp";
    }

    public int getPersistID() {
        return persistID;
    }

    public void setPersistID(int persistID) {
        this.persistID = persistID;
    }

    ///////////////

    public ChatMessage(String message, long timestamp, Type type, String contacJid){
        this.contacJid=contacJid;
        this.message=message;
        this.timestamp=timestamp;
        this.type=type;
    }


    public enum Type{
        SENT,RECEIVED
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getContacJid() {
        return contacJid;
    }

    public String getMessage(){
        return message;
    }

    public Type getMessageType(){
        return type;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getTypeStringValue(Type type){
        if (type==Type.SENT){
            return "SENT";
        }
        else
            return "RECEIVED";
    }

    public ContentValues getContentValue(){
        ContentValues values=new ContentValues();
        values.put(Cols.MESSAGE,message);
        values.put(Cols.MESSAGE_TYPE,getTypeStringValue(type));
        values.put(Cols.TIME_STAMP,timestamp);
        values.put(Cols.CONTACT_JID,contacJid);

        return values;
    }
}
