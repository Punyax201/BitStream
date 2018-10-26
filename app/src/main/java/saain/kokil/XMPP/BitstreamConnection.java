package saain.kokil.XMPP;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.Collection;
import java.util.List;

import de.measite.minidns.record.A;
import saain.kokil.ChatListActivity;
import saain.kokil.Constants;
import saain.kokil.Encryption.Encryptor;
import saain.kokil.R;
import saain.kokil.model.Chat;
import saain.kokil.model.ChatMessage;
import saain.kokil.model.ChatMessagesModel;
import saain.kokil.model.ChatModel;
import saain.kokil.model.ChatStateChangedListener;
import saain.kokil.model.Contact;
import saain.kokil.model.ContactModel;

public class BitstreamConnection implements ConnectionListener,SubscribeListener,RosterListener {

    private static final String LOGTAG="XMPP Connection";
    private final Context ApplicationContext;
    private String username;
    private String password;
    private String serviceName;

    private XMPPTCPConnection connection;
    private ConnectionState connectionState;

    private PingManager pingManager;
    private ChatManager chatManager;

    //Notification
    private Notification.Builder builder;
    private NotificationManager notificationManager;
    private int notification_id;
    private RemoteViews remoteViews;
    private Roster roster;
    private ChatStateListener chatStateListener;


    //Connection State
    public static enum ConnectionState{
        OFFLINE,CONNECTING,ONLINE
    }

    public ConnectionState getConnectionState(){
        return connectionState;
    }

    public void setConnectionState(ConnectionState connectionState){
        this.connectionState=connectionState;
    }

    public String getConnectionStateString(){
        switch (connectionState){
            case OFFLINE:
                return "Offline";
            case ONLINE:
                return "Online";
            case CONNECTING:
                return "Connecting";

            default:
                return "Offline";
        }
    }

    private void updateActivitiesOfConnectionStateChange(ConnectionState mconnectionState){
        ConnectionState connectionState1=mconnectionState;
        String status;

        switch (mconnectionState){
            case OFFLINE:
                status="Offline";
            case CONNECTING:
                status="Connecting";
            case ONLINE:
                status="Online";

            default:
                status="Offline";
                break;
        }

        Intent i=new Intent(Constants.BroadcastMessages.UI_CONNECTION_STATUS_CHANGE_FLAG);
        i.putExtra(Constants.UI_CONNECTION_STATUS_CHANGE,status);
        i.setPackage(ApplicationContext.getPackageName());
        ApplicationContext.sendBroadcast(i);
    }

    public BitstreamConnection(Context applicationContext) {
        Log.d(LOGTAG,"Connecting....");
        ApplicationContext = applicationContext;


    }

    public void connect() throws Exception {

        connectionState=ConnectionState.CONNECTING;
        updateActivitiesOfConnectionStateChange(ConnectionState.CONNECTING);
        getCredentials();

        XMPPTCPConnectionConfiguration configuration= null;
        try {
            configuration = XMPPTCPConnectionConfiguration.builder().setXmppDomain(serviceName).setHost(serviceName).setResource("Bitstream+").setKeystoreType(null)
                    .setSendPresence(true)
                    .setDebuggerEnabled(true)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                    .setCompressionEnabled(true).build();
        } catch (XmppStringprepException e) {
            Log.d(LOGTAG,"Error............."+e.getMessage());
        }

        SmackConfiguration.DEBUG=true;
        XMPPTCPConnection.setUseStreamManagementDefault(true);

        connection=new XMPPTCPConnection(configuration);
        connection.setUseStreamManagement(true);
        connection.setUseStreamManagementResumption(true);
        connection.setPreferredResumptionTime(10);
        connection.addConnectionListener(this);


        roster=Roster.getInstanceFor(connection);
        roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
        roster.addSubscribeListener(this);
        roster.addRosterListener(this);

        chatManager=ChatManager.getInstanceFor(connection);

        //Incoming message Listener

        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, org.jivesoftware.smack.chat2.Chat chat) {

                chatStateListener=new ChatStateListener() {
                    @Override
                    public void stateChanged(org.jivesoftware.smack.chat.Chat chat, ChatState state, Message message) {

                    }

                    @Override
                    public void processMessage(org.jivesoftware.smack.chat.Chat chat, Message message) {

                    }
                };

                Log.d(LOGTAG,"Message Body: "+message.getBody()+ " from: "+message.getFrom());

                String source=message.getFrom().toString();
                String decrypted_body= Encryptor.decrypt("Bar12345Bar12345","RandomInitVector",message.getBody());
                String contactJid="";
                if(source.contains("/")){
                    contactJid=source.split("/")[0];

                }
                else{
                    contactJid=source;
                }

                ChatMessagesModel.get(ApplicationContext).addMessage(new ChatMessage(decrypted_body,System.currentTimeMillis(),ChatMessage.Type.RECEIVED,contactJid));

                //Add Chat for stranger
                if(ContactModel.get(ApplicationContext).isStranger(contactJid)){
                    List<Chat> chats=ChatModel.get(ApplicationContext).getChatsByJID(contactJid);

                    if(chats.size()==0){
                        Chat chatRoster=new Chat(contactJid,decrypted_body, Chat.ContactType.STRANGER,System.currentTimeMillis(),1);
                        ChatModel.get(ApplicationContext).addChat(chatRoster);

                        Intent intent=new Intent(Constants.BroadcastMessages.UI_NEW_CHAT_ITEM);
                        intent.setPackage(ApplicationContext.getPackageName());
                        ApplicationContext.sendBroadcast(intent);
                    }
                }
                else{

                }

                Intent intent=new Intent(Constants.BroadcastMessages.UI_NEW_MESSAGE_FLAG);
                intent.setPackage(ApplicationContext.getPackageName());
                ApplicationContext.sendBroadcast(intent);

                sendNotification(message.getFrom().toString(),decrypted_body);
            }
        });



        //Instantiate Ping Manager
        ServerPingWithAlarmManager.getInstanceFor(connection).setEnabled(true);
        pingManager=PingManager.getInstanceFor(connection);
        pingManager.setPingInterval(20);

        connection.connect();
        connection.login(username,password);
        syncContactListWithRemoteRoster();



    }



    public void syncContactListWithRemoteRoster(){
        Log.d(LOGTAG,"Roster syncing...");

        Collection<RosterEntry> entries=getRosterEntries();

        for (RosterEntry entry:entries){
            RosterPacket.ItemType itemType=entry.getType();
            Log.d(LOGTAG,"Entry "+entry.getJid().toString()+" has Subscription "+entry.getType());

            List<String> contacts=ContactModel.get(ApplicationContext).getContactsJidString();

            if((!contacts.contains(entry.getJid().toString())) && (itemType!=RosterPacket.ItemType.none)){

                if(ContactModel.get(ApplicationContext).addContact(new Contact(entry.getJid().toString(),rosterItemTypeToContactSubscriptionType(itemType)))){
                    Log.d(LOGTAG,"New Contact added from Entry with JID "+entry.getJid().toString());
                }
                else {

                }
            }

            if((contacts.contains(entry.getJid().toString()))){
                Contact.SubscriptionStatus subscriptionStatus=rosterItemTypeToContactSubscriptionType(itemType);
                boolean isSubscriptionPending=entry.isSubscriptionPending();
                Contact contact=ContactModel.get(ApplicationContext).getContactByJidString(entry.getJid().toString());
                contact.setPendingTo(isSubscriptionPending);
                contact.setSubStatus(subscriptionStatus);
                ContactModel.get(ApplicationContext).updateContactSubscription(contact);
            }
        }
    }

    public Collection<RosterEntry> getRosterEntries(){
        Collection<RosterEntry> entries=roster.getEntries();
        Log.d(LOGTAG,"Found "+entries.size()+" contacts in Roster");
        return entries;
    }

    //NOTIFY
    public void sendNotification(String title,String content){
        Log.d(LOGTAG,"Notification Sent");
        new Notifier().execute(title,content);
    }

    public void disconnect(){
        Log.d(LOGTAG,"Disconnecting...");
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(ApplicationContext);
        preferences.edit().putBoolean("xmpp_logged_in",false).commit();

        if(connection!=null){
            connection.disconnect();
        }
    }

    public void sendMessage(String body,String Tojid){
        Log.d(LOGTAG,"Sending msg to "+Tojid);
        EntityBareJid jid=null;

        //Encrypt
        String messageBody=Encryptor.encrypt("Bar12345Bar12345","RandomInitVector",body);
        Log.d(LOGTAG,"text: "+body+" Encrypted: "+messageBody+" Decrypted: "+Encryptor.decrypt("Bar12345Bar12345","RandomInitVector",messageBody));


        try{
            jid= JidCreate.entityBareFrom(Tojid);
        }
        catch (XmppStringprepException e){

        }

        org.jivesoftware.smack.chat2.Chat chat=chatManager.chatWith(jid);
        try{
            Message message=new Message(jid,Message.Type.chat);
            message.setBody(messageBody);
            chat.send(messageBody);

            ChatMessagesModel.get(ApplicationContext).addMessage(new ChatMessage(body,System.currentTimeMillis(),ChatMessage.Type.SENT,Tojid));
        }
        catch (SmackException.NotConnectedException |InterruptedException e){
            Log.d(LOGTAG,e.getMessage());
        }

    }

    //SUBSCRIBE/UNSUBSCRIBE

    public boolean subscribe(String contact){
        Jid jidTo=null;
        try{
            jidTo=JidCreate.from(contact);
        }
        catch (XmppStringprepException e){
            return false;
        }

        Presence presence=new Presence(jidTo,Presence.Type.subscribe);
        if(sendPresence(presence)){
            return true;
        }
        else
            return false;
    }

    //SUBSCRIBED

    public boolean subscribed(String contact){
        Jid jidTo=null;
        try{
            jidTo=JidCreate.from(contact);
        }
        catch (XmppStringprepException e){
            return false;
        }
        Presence presence=new Presence(jidTo,Presence.Type.subscribed);
        sendPresence(presence);

        return true;
    }

    //UNSUBSCRIBE

    public boolean unsubscribe(String contact){
        Jid jidTo=null;
        try{
            jidTo=JidCreate.from(contact);
        }
        catch (XmppStringprepException e){
            return false;
        }

        Presence presence=new Presence(jidTo,Presence.Type.unsubscribe);
        if(sendPresence(presence)){
            return true;
        }
        else
            return false;
    }

    public boolean unsubscribed(String contact){
        Jid jidTo=null;
        try{
            jidTo=JidCreate.from(contact);
        }
        catch (XmppStringprepException e){
            return false;
        }

        Presence presence=new Presence(jidTo,Presence.Type.unsubscribed);
        if(sendPresence(presence)){
            return true;
        }
        else
            return false;
    }
    //Send Presence
    public boolean sendPresence(Presence presence){
        if(connection!=null){
            try {
                connection.sendStanza(presence);
            }
            catch (SmackException.NotConnectedException e){
                return false;
            }
            catch (InterruptedException e){
                return false;
            }
            return true;
        }
        return false;
    }



    public void getCredentials(){
        String jid= PreferenceManager.getDefaultSharedPreferences(ApplicationContext).getString("xmpp_jid",null);
        password=PreferenceManager.getDefaultSharedPreferences(ApplicationContext).getString("xmpp_password",null);

        if(jid!=null){
            username=jid.split("@")[0];
            serviceName=jid.split("@")[1];
        }
        else{
            username="";
            serviceName="";
        }
    }

    @Override
    public void connected(XMPPConnection connection) {
        Log.d(LOGTAG,"Connected");
        connectionState=ConnectionState.CONNECTING;
        updateActivitiesOfConnectionStateChange(ConnectionState.CONNECTING);

    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.d(LOGTAG,"Authenticated");
        connectionState=ConnectionState.ONLINE;
        updateActivitiesOfConnectionStateChange(ConnectionState.ONLINE);
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(ApplicationContext);
        preferences.edit().putBoolean("xmpp_logged_in",true).commit();

        Intent i=new Intent(Constants.BroadcastMessages.UI_AUTHENTICATED);
        i.setPackage(ApplicationContext.getPackageName());
        ApplicationContext.sendBroadcast(i);
        Log.d(LOGTAG,"Authentication Broadcast sent ");
    }

    @Override
    public void connectionClosed() {
        Log.d(LOGTAG,"Connection Closed");
        connectionState=ConnectionState.OFFLINE;
        updateActivitiesOfConnectionStateChange(ConnectionState.OFFLINE);
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.d(LOGTAG,"Closed on Error");
        connectionState=ConnectionState.OFFLINE;
        updateActivitiesOfConnectionStateChange(ConnectionState.OFFLINE);
    }

    @Override
    public void reconnectionSuccessful() {
        Log.d(LOGTAG,"Reconnection Successful");
        connectionState=ConnectionState.ONLINE;
        updateActivitiesOfConnectionStateChange(ConnectionState.ONLINE);
    }

    @Override
    public void reconnectingIn(int seconds) {
        Log.d(LOGTAG,"Reconnecting in..."+seconds);
        connectionState=ConnectionState.CONNECTING;
        updateActivitiesOfConnectionStateChange(ConnectionState.CONNECTING);
    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.d(LOGTAG,"Reconnection Failed");
        connectionState=ConnectionState.OFFLINE;
        updateActivitiesOfConnectionStateChange(ConnectionState.OFFLINE);
    }

    //SUBSCRIPTION

    @Override
    public SubscribeAnswer processSubscribe(Jid from, Presence subscribeRequest) {

        Log.d(LOGTAG,"----SUBSCRIBTION ATTEMPT---");
        Log.d(LOGTAG,"FROM:"+from.toString()+" Presence Type:"+subscribeRequest.getType().toString());

        if(!ContactModel.get(ApplicationContext).isStranger(from.toString())){
            Contact contact=ContactModel.get(ApplicationContext).getContactByJidString(from.toString());
            contact.setPendingFrom(true);
            ContactModel.get(ApplicationContext).updateContactSubscription(contact);
        }
        else{
            //Create a new STRANGER Contact
            Log.d(LOGTAG,"Encountered a Stranger");
            List<Chat> chats= ChatModel.get(ApplicationContext).getChatsByJID(from.toString());
            if (chats.size()==0){
                if(ChatModel.get(ApplicationContext).addChat(new Chat(from.toString(),"NEW CHAT!", Chat.ContactType.STRANGER,System.currentTimeMillis(),1))){
                    Log.d(LOGTAG,"New Contact added!");
                }
            }
        }

        return null;
    }

    //ADD CONTACT TO ROSTER

    public boolean addContactToRoster(String contactjid){
        Jid jid;
        try{
            jid=JidCreate.from(contactjid);
        }
        catch (XmppStringprepException e){
            return false;
        }

        try{
            roster.createEntry(jid.asBareJid(),"",null);
        }
        catch (Exception e){
            return false;
        }

        return true;
    }

    public boolean removeRosterEntry(String contactJID){
        Jid jid;
        try {
            jid=JidCreate.from(contactJID);
        }
        catch (XmppStringprepException e){
            return false;
        }

        RosterEntry entry=roster.getEntry(jid.asBareJid());
        try{
            roster.removeEntry(entry);
        }
        catch (Exception e){
            return false;
        }

        return true;
    }

    //ROSTER LISTENER
    @Override
    public void entriesAdded(Collection<Jid> addresses) {

        for (Jid jid:addresses){
            RosterEntry rosterEntry=roster.getEntry(jid.asBareJid());
            RosterPacket.ItemType itemType=rosterEntry.getType();
            boolean isSibscriptionPending=rosterEntry.isSubscriptionPending();

            //Get All Contacts
            List<String> contacts=ContactModel.get(ApplicationContext).getContactsJidString();

            //Add New Roster Entries
            if((!contacts.contains(rosterEntry.getJid().toString())) && (itemType!=RosterPacket.ItemType.none)){

                Contact contact=new Contact(rosterEntry.getJid().toString(),rosterItemTypeToContactSubscriptionType(itemType));
                contact.setPendingTo(isSibscriptionPending);

                if(ContactModel.get(ApplicationContext).addContact(contact)){
                    Log.d(LOGTAG,"New Contact Added to Roster! JID:"+rosterEntry.getJid().toString());
                }
                else {
                    Log.d(LOGTAG,"Contact Add Failed");
                }
            }

            //Update already existing entries if necessary

            if((contacts.contains(rosterEntry.getJid().toString()))){
                Contact.SubscriptionStatus subscriptionStatus=rosterItemTypeToContactSubscriptionType(itemType);
                Contact contact=ContactModel.get(ApplicationContext).getContactByJidString(rosterEntry.getJid().toString());

                contact.setPendingTo(isSibscriptionPending);
                contact.setSubStatus(subscriptionStatus);
                ContactModel.get(ApplicationContext).updateContactSubscription(contact);
            }
        }
    }

    private Contact.SubscriptionStatus rosterItemTypeToContactSubscriptionType(RosterPacket.ItemType itemType){
        if(itemType==RosterPacket.ItemType.none){
            return Contact.SubscriptionStatus.NONE;
        }
        else if(itemType==RosterPacket.ItemType.from){
            return Contact.SubscriptionStatus.FROM;
        }
        else if(itemType==RosterPacket.ItemType.to){
            return Contact.SubscriptionStatus.TO;
        }
        else
            return Contact.SubscriptionStatus.BOTH;
    }

    @Override
    public void entriesUpdated(Collection<Jid> addresses) {

        for (Jid jid:addresses){
            RosterEntry rosterEntry=roster.getEntry(jid.asBareJid());
            RosterPacket.ItemType itemType=rosterEntry.getType();
            boolean isSubscriptionPending=rosterEntry.isSubscriptionPending();

            List<String> contacts=ContactModel.get(ApplicationContext).getContactsJidString();

            //Update already existing entries if necessary
            if((contacts.contains(rosterEntry.getJid().toString()))){
                Contact.SubscriptionStatus subscriptionStatus=rosterItemTypeToContactSubscriptionType(itemType);
                Contact contact=ContactModel.get(ApplicationContext).getContactByJidString(rosterEntry.getJid().toString());
                contact.setPendingTo(isSubscriptionPending);
                contact.setSubStatus(subscriptionStatus);
                ContactModel.get(ApplicationContext).updateContactSubscription(contact);
            }
        }
    }

    @Override
    public void entriesDeleted(Collection<Jid> addresses) {

        for (Jid jid:addresses){
            if(!ContactModel.get(ApplicationContext).isStranger(jid.toString())){
                Contact contact=ContactModel.get(ApplicationContext).getContactByJidString(jid.toString());
                if(ContactModel.get(ApplicationContext).deleteContact(contact.getPersistID())){
                    Log.d(LOGTAG,"Contact "+jid+" deleted from DB");
                }
            }
        }
    }

    @Override
    public void presenceChanged(Presence presence) {

        Log.d(LOGTAG,"PresenceChange() called. Presence:"+presence.toString());

        Presence mPresence=roster.getPresence(presence.getFrom().asBareJid());
        Contact contact=ContactModel.get(ApplicationContext).getContactByJidString(presence.getFrom().asBareJid().toString());

        if(mPresence.isAvailable() && (!mPresence.isAway())){
            contact.setOnlineStatus(true);
        }
        else {
            contact.setOnlineStatus(false);
        }

        ContactModel.get(ApplicationContext).updateContactSubscription(contact);

        Intent intent=new Intent(Constants.BroadcastMessages.UI_ONLINE_STATUS_CHANGED);
        intent.putExtra(Constants.ONLINE_STATUS_CHANGE_CONTACT,presence.getFrom().asBareJid().toString());
        intent.setPackage(ApplicationContext.getPackageName());
        ApplicationContext.sendBroadcast(intent);
    }



    class Notifier extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String...params) {
            String title=params[0];
            String content=params[1];

            createNotification(title,content, ApplicationContext);

            return null;

        }

        private void createNotification(String title,String c,Context context){

            int notificationID=1;
            String channelID="bitstream_channel_1";
            String name="Bitstream Channel";
            int importance=NotificationManager.IMPORTANCE_HIGH;

            if(Build.VERSION.SDK_INT<Build.VERSION_CODES.O) {

                Log.d(LOGTAG,"ANDROID N or smaller");
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification.Builder builder = new Notification.Builder(context).setSmallIcon(R.drawable.bit).setContentTitle(title).setContentText(c).setAutoCancel(true);

                notificationManager.notify(notificationID, builder.build());
            }
            else {
                Log.d(LOGTAG,"ANDROID OREO or Greater");

                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel=new NotificationChannel(channelID,name,importance);
                notificationManager.createNotificationChannel(channel);
                Notification.Builder builder = new Notification.Builder(context).setSmallIcon(R.drawable.bit).setContentTitle(title).setContentText(c).setAutoCancel(true).setChannelId(channelID);
                notificationManager.notify(notificationID, builder.build());

            }
        }

    }
}

