package saain.kokil.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import saain.kokil.persistence.ContactCursorWrapper;
import saain.kokil.persistence.DatabaseBack;

public class ContactModel {

    private static final String LOGTAG="Contact Model";
    private static ContactModel scontactModel;
    private Context context;


    //Database
    private SQLiteDatabase database;

    public static ContactModel get(Context context){
        if(scontactModel==null){
            scontactModel=new ContactModel(context);
        }
        return scontactModel;
    }

    private ContactModel(Context context){
        this.context=context;
        database= DatabaseBack.getInstance(context).getWritableDatabase();
    }

    public List<Contact> getContactlist() {
        List<Contact> contacts=new ArrayList<>();
        ContactCursorWrapper cursorWrapper=  queryContacts(null,null);

        try{
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()){
                contacts.add(cursorWrapper.getContact());
                cursorWrapper.moveToNext();
            }
        }
        finally {
            cursorWrapper.close();
        }
        return contacts;
    }

    //GET CONTACT BY JID
    public Contact getContactByJidString(String jid){
        List<Contact> contacts=getContactlist();
        List<String> stringJids=new ArrayList<>();

        Contact mcontact=null;

        for (Contact contact:contacts){
            if (contact.getJid().equals(jid)){
                mcontact=contact;
            }
        }

        return mcontact;
    }

    public List<String> getContactsJidString(){
        List<Contact> contacts=getContactlist();
        List<String> stringJids=new ArrayList<>();

        for (Contact contact:contacts){
            stringJids.add(contact.getJid());
        }

        return stringJids;
    }

    //CHECK IF CONTACT IS STRANGER

    public boolean isStranger(String contact){
        List<String> contacts=getContactsJidString();
        return !contacts.contains(contact);
    }

    private ContactCursorWrapper queryContacts(String whereClause, String[] whereArgs){
        Cursor cursor=database.query(Contact.TABLE_NAME,null,whereClause,whereArgs,null,null,null);
        return new ContactCursorWrapper(cursor);
    }

    public boolean addContact(Contact contact){
        ContentValues values=contact.getContentValue();
        if((database.insert(Contact.TABLE_NAME,null,values)==-1)){
            return false;
        }
        else
            return true;
    }

    //UPDATE SUBSCRIPTION

    public boolean updateContactSubscription(Contact contact){
        Contact mContact=contact;
        String jidString=contact.getJid();

        ContentValues contentValues=contact.getContentValue();

        int row=database.update(Contact.TABLE_NAME,contentValues,"jid=?",new String[]{jidString});
        Log.d(LOGTAG,row+" rows Affected");

        if(row!=0){
            Log.d(LOGTAG,"DB update successful");
            return true;
        }

        return false;
    }

    //UPDATE SUBSCRIPTION ON SUBSCRIBED

    public void updateContactSubscriptionOnSendSubscribed(String contact){
        Contact contact1=getContactByJidString(contact);
        contact1.setPendingFrom(false);
        updateContactSubscription(contact1);
    }


    public boolean deleteContact(int uid){
        int value=database.delete(Contact.TABLE_NAME,Contact.Cols.CONTACT_UNIQUE_ID+"=?",new String[] {String.valueOf(uid)});
        if(value==1){
            Log.d(LOGTAG,"Deleted Record");
            return true;
        }
        else{
            Log.d(LOGTAG,"Could not delete record");
            return false;
        }
    }
}
