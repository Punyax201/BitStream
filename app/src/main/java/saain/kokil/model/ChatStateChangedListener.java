package saain.kokil.model;

import android.util.Log;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateListener;

public class ChatStateChangedListener implements ChatStateListener {

    public static final String LOGTAG="CHATSTATE LIETENER";

    @Override
    public void stateChanged(Chat chat, ChatState state, Message message) {
        Log.d(LOGTAG,"Chat STate changed");
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        Log.d(LOGTAG,"CHAT STATE CHANGEDDD");
    }
}
