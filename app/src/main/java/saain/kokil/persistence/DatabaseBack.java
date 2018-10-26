package saain.kokil.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import saain.kokil.model.Chat;
import saain.kokil.model.ChatMessage;
import saain.kokil.model.Contact;

public class DatabaseBack extends SQLiteOpenHelper {

    private static final String LOGTAG="DatabaseBack";
    private static DatabaseBack instance=null;
    private static final String DB_NAME="bitstream_db";
    private static final int DB_VERSION=2;

    //SQL STRINGS

    //Create Chat List Table
    private static String CREATE_CHAT_LIST="CREATE TABLE "+ Chat.TABLE_NAME+"("+
            Chat.Cols.CHAT_UNIQUE_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
            Chat.Cols.CONTACT_TYPE+" TEXT,"+
            Chat.Cols.CONTACT_JID+" TEXT,"+
            Chat.Cols.LAST_MESSAGE+" TEXT,"+
            Chat.Cols.UNREAD_COUNT+" NUMBER,"+
            Chat.Cols.LAST_TIME_STAMP+" NUMBER"
            +")";

    //Create Contact List Table
    private static String CREATE_CONTACT_LIST="CREATE TABLE "+ Contact.TABLE_NAME+"("+Contact.Cols.CONTACT_UNIQUE_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
            Contact.Cols.SUBSCRIPTION_TYPE+" TEXT,"+
            Contact.Cols.CONTACT_JID+" TEXT,"+
            Contact.Cols.PROFILE_IMAGE_PATH+" TEXT,"+
            Contact.Cols.ONLINE_STATUS+" NUMBER DEFAULT 0,"+
            Contact.Cols.PENDING_STATUS_FROM+" NUMBER DEFAULT 0,"+
            Contact.Cols.PENDING_STATUS_TO+" NUMBER DEFAULT 0"+
            ")";

    //Create Chat Message List Table

    private static String CREATE_CHAT_MESSAGE="CREATE TABLE "+ ChatMessage.TABLE_NAME+" ("+ChatMessage.Cols.CHAT_UNIQUE_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
            ChatMessage.Cols.MESSAGE+" TEXT,"+
            ChatMessage.Cols.MESSAGE_TYPE+" TEXT,"+
            ChatMessage.Cols.TIME_STAMP+" NUMBER,"+
            ChatMessage.Cols.CONTACT_JID+" TEXT"+")";

    /*******************************/

    private DatabaseBack(Context context) {
        super(context,DB_NAME,null,DB_VERSION);
    }

    public static synchronized DatabaseBack getInstance(Context context){
        Log.d(LOGTAG,"Getting DB Instance...");
        if(instance==null){
            instance=new DatabaseBack(context);
        }
        return instance;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOGTAG,"Creating Tables..");
        db.execSQL(CREATE_CHAT_LIST);
        db.execSQL(CREATE_CONTACT_LIST);
        db.execSQL(CREATE_CHAT_MESSAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion<2 && newVersion >=2){
            Log.d(LOGTAG,"Upgrading to v 2.0");
            db.execSQL("ALTER TABLE "+Contact.TABLE_NAME+" ADD COLUMN "+Contact.Cols.PENDING_STATUS_TO+" NUMBER DEFAULT 0");
            db.execSQL("ALTER TABLE "+Contact.TABLE_NAME+" ADD COLUMN "+Contact.Cols.PENDING_STATUS_FROM+" NUMBER DEFAULT 0");
            db.execSQL("ALTER TABLE "+Contact.TABLE_NAME+" ADD COLUMN "+Contact.Cols.ONLINE_STATUS+" NUMBER DEFAULT 0");
        }
    }
}
