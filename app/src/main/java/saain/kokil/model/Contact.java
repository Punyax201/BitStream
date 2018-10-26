package saain.kokil.model;

import android.content.ContentValues;

public class Contact {

    private String Jid;
    private SubscriptionStatus subStatus;
    private String profileImagePath;
    private int persistID;

    boolean pendingTo;
    boolean pendingFrom;
    boolean onlineStatus;

    public static final String TABLE_NAME="contacts";

    public static final class Cols{
        public static final String CONTACT_UNIQUE_ID="c";
        public static final String CONTACT_JID="jid";
        public static final String SUBSCRIPTION_TYPE="type";
        public static final String PROFILE_IMAGE_PATH="path";

        public static final String PENDING_STATUS_TO="pendingTo";
        public static final String PENDING_STATUS_FROM="pendingFrom";
        public static final String ONLINE_STATUS="OnlineStatus";
    }

    public ContentValues getContentValue(){
        ContentValues values=new ContentValues();
        values.put(Cols.CONTACT_JID,Jid);
        values.put(Cols.SUBSCRIPTION_TYPE,getTypeStringValue(subStatus));
        values.put(Cols.PROFILE_IMAGE_PATH,profileImagePath);

        int pendingFromInt=(pendingFrom)?1:0;
        int pendingToInt=(pendingTo)?1:0;
        int onlineStatusInt=(onlineStatus)?1:0;

        values.put(Cols.PENDING_STATUS_FROM,pendingFromInt);
        values.put(Cols.PENDING_STATUS_TO,pendingToInt);
        values.put(Cols.ONLINE_STATUS,onlineStatusInt);

        return values;
    }

    public String getTypeStringValue(SubscriptionStatus type){
        if(type==SubscriptionStatus.FROM){
            return "FROM";
        }
        else if(type==SubscriptionStatus.TO){
            return "TO";
        }
        else if(type==SubscriptionStatus.BOTH){
            return "BOTH";
        }
        else if(type==SubscriptionStatus.NONE){
            return "NONE";
        }
        else{
            return "N/A";
        }
    }

    public enum SubscriptionStatus{
        NONE,FROM,TO,BOTH
    }

    public Contact(String jid,SubscriptionStatus status){
        this.Jid=jid;
        this.subStatus=status;
        this.profileImagePath="none";
        this.pendingFrom=false;
        this.pendingTo=false;
        this.onlineStatus=false;
    }

    public String getJid() {
        return Jid;
    }

    public void setJid(String jid) {
        Jid = jid;
    }

    public SubscriptionStatus getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(SubscriptionStatus subStatus) {
        this.subStatus = subStatus;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public int getPersistID() {
        return persistID;
    }

    public void setPersistID(int persistID) {
        this.persistID = persistID;
    }

    public boolean isPendingTo() {
        return pendingTo;
    }

    public void setPendingTo(boolean pendingTo) {
        this.pendingTo = pendingTo;
    }

    public boolean isPendingFrom() {
        return pendingFrom;
    }

    public void setPendingFrom(boolean pendingFrom) {
        this.pendingFrom = pendingFrom;
    }

    public boolean isOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }
}
