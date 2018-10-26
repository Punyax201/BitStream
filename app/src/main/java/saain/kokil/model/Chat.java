package saain.kokil.model;

import android.content.ContentValues;

public class Chat {
    private String jid;
    private String lastmessage;
    private long lastMessageTime;
    private int persistID;
    private long unreadCount;
    private ContactType contactType;


    public static final String TABLE_NAME="chats";

    public static final class Cols{
        public static final String CHAT_UNIQUE_ID="id";
        public static final String CONTACT_JID="jid";
        public static final String CONTACT_TYPE="contactType";
        public static final String LAST_MESSAGE="lmsg";
        public static final String UNREAD_COUNT="ucount";
        public static final String LAST_TIME_STAMP="ltstamp";
    }

    public Chat(String jid,String lastmessage,ContactType contactType,long timeStamp,long unreadCount){
        this.jid=jid;
        this.lastmessage=lastmessage;
        this.contactType=contactType;
        this.lastMessageTime=timeStamp;
        this.unreadCount=unreadCount;
    }
    public String getJid(){
        return jid;
    }
    public String getLastmessage(){
        return lastmessage;
    }

    public enum ContactType{
        ONE_ON_ONE,GROUP,STRANGER
    }

    public String getTypeStringValue(ContactType type){
        if(type==ContactType.ONE_ON_ONE){
            return "ONE_ON_ONE";
        }
        else if(type==ContactType.GROUP){
            return "GROUP";
        }
        else
            return "STRANGER";

    }

    //Getters and Setters


    public void setLastmessage(String lastmessage) {
        this.lastmessage = lastmessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getPersistID() {
        return persistID;
    }

    public void setPersistID(int persistID) {
        this.persistID = persistID;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void setContentType(ContactType type){
        this.contactType=type;
    }

    public ContactType getContactType() {
        return contactType;
    }

    public ContentValues getContentValue(){
        ContentValues values=new ContentValues();
        values.put(Cols.CONTACT_JID,jid);
        values.put(Cols.CONTACT_TYPE,getTypeStringValue(contactType));
        values.put(Cols.LAST_TIME_STAMP,lastMessageTime);
        values.put(Cols.LAST_MESSAGE,lastmessage);
        values.put(Cols.UNREAD_COUNT,unreadCount);

        return values;
    }
}
