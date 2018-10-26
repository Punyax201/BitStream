package saain.kokil.persistence;

import android.database.Cursor;
import android.database.CursorWrapper;

import saain.kokil.model.Contact;

public class ContactCursorWrapper extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public ContactCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Contact getContact(){
        String subscriptionTypeString=getString(getColumnIndex(Contact.Cols.SUBSCRIPTION_TYPE));
        String jid=getString(getColumnIndex(Contact.Cols.CONTACT_JID));
        int contactUID=getInt(getColumnIndex(Contact.Cols.CONTACT_UNIQUE_ID));
        String profileImage=getString(getColumnIndex(Contact.Cols.PROFILE_IMAGE_PATH));

        //ROSTER

        int pendingFromInt=getInt(getColumnIndex(Contact.Cols.PENDING_STATUS_FROM));
        int pendingToInt=getInt(getColumnIndex(Contact.Cols.PENDING_STATUS_TO));
        int onlineStatusInt=getInt(getColumnIndex(Contact.Cols.ONLINE_STATUS));

        Contact.SubscriptionStatus subscriptionStatus=null;

        if(subscriptionTypeString.equals("NONE")){
            subscriptionStatus=Contact.SubscriptionStatus.NONE;
        }
        else if(subscriptionTypeString.equals("FROM")){
            subscriptionStatus=Contact.SubscriptionStatus.FROM;
        }

        else if(subscriptionTypeString.equals("TO")){
            subscriptionStatus=Contact.SubscriptionStatus.TO;
        }

        else if(subscriptionTypeString.equals("BOTH")){
            subscriptionStatus=Contact.SubscriptionStatus.BOTH;
        }

        Contact contact=new Contact(jid,subscriptionStatus);
        contact.setPersistID(contactUID);
        contact.setProfileImagePath(profileImage);
        contact.setPendingFrom((pendingFromInt==0)?false:true);
        contact.setPendingTo((pendingToInt==0)?false:true);
        contact.setOnlineStatus((onlineStatusInt==0)?false:true);

        return contact;
    }
}
