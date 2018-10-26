package saain.kokil.persistence;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

import saain.kokil.model.Chat;

public class ChatCursorWrapper extends CursorWrapper {

    private static final String LOGTAG="ChatCursorWrapper";
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public ChatCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Chat getChat(){

        String jid=getString(getColumnIndex(Chat.Cols.CONTACT_JID));
        String contactType=getString(getColumnIndex(Chat.Cols.CONTACT_TYPE));
        String lastMessage=getString(getColumnIndex(Chat.Cols.LAST_MESSAGE));
        long unreadCount=getLong(getColumnIndex(Chat.Cols.UNREAD_COUNT));
        long lastMessageTime=getLong(getColumnIndex(Chat.Cols.LAST_TIME_STAMP));
        int uid=getInt(getColumnIndex(Chat.Cols.CHAT_UNIQUE_ID));

        Log.d(LOGTAG,"Chat Message Received with ID:"+uid);

        Chat.ContactType chatType = null;

        if(contactType.equals("GROUP")){
            chatType=Chat.ContactType.GROUP;
        }
        else if(contactType.equals("STRANGER")){
            chatType=Chat.ContactType.STRANGER;
        }
        else{
            chatType=Chat.ContactType.ONE_ON_ONE;
        }

        Chat chat=new Chat(jid,lastMessage,chatType,lastMessageTime,unreadCount);
        chat.setPersistID(uid);
        return chat;
    }
}
