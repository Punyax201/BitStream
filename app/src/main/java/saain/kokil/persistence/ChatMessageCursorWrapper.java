package saain.kokil.persistence;

import android.database.Cursor;
import android.database.CursorWrapper;

import saain.kokil.model.ChatMessage;
import saain.kokil.model.ChatModel;

public class ChatMessageCursorWrapper extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public ChatMessageCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public ChatMessage getChatMessage(){
        String message=getString(getColumnIndex(ChatMessage.Cols.MESSAGE));
        long timestamp=getLong(getColumnIndex(ChatMessage.Cols.TIME_STAMP));
        String messageType=getString(getColumnIndex(ChatMessage.Cols.MESSAGE_TYPE));
        String counterpartJID=getString(getColumnIndex(ChatMessage.Cols.CONTACT_JID));
        int uid=getInt(getColumnIndex(ChatMessage.Cols.CHAT_UNIQUE_ID));

        ChatMessage.Type chatmessageType=null;

        if(messageType.equals("SENT")){
            chatmessageType=ChatMessage.Type.SENT;
        }
        else
            chatmessageType=ChatMessage.Type.RECEIVED;

        ChatMessage chatMessage=new ChatMessage(message,timestamp,chatmessageType,counterpartJID);
        chatMessage.setPersistID(uid);

        return chatMessage;
    }
}
