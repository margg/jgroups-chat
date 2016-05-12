package pl.edu.agh.ds.chat;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatAction;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatAction.ActionType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Chat {

    private static final String MGMT_CHANNEL_MCAST_ADDRESS = "228.8.8.8";
    private static final String CHANNEL_BASE_MCAST_ADDRESS = "230.0.0.";
    private static final String MANAGEMENT_CHANNEL_NAME = "ChannelManagement321123";

    private final Map<String, JChannel> userChannels;
    private final JChannel managementChannel;
    private final Map<String, List<String>> chatState;
    private final String nickname;
    private MessageCachingReceiver messageCachingReceiver;

    public Chat(String nickname, MessageCachingReceiver messageCachingReceiver) throws Exception {
        this.nickname = nickname;
        this.messageCachingReceiver = messageCachingReceiver;
        this.userChannels = new HashMap<>();
        this.chatState = new ConcurrentHashMap<>();
        this.managementChannel = initMgmtChannel();
    }

    public void connectToChannel(String channelName) {
        try {
            JChannel channel =
                    initChannel(channelName, new ChatMessageReceiverAdapter(channelName, messageCachingReceiver), CHANNEL_BASE_MCAST_ADDRESS + channelName);
            userChannels.put(channelName, channel);

            managementChannel.send(createActionMessage(nickname, channelName, ActionType.JOIN));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message, String channelName) throws Exception {
        JChannel channel = userChannels.get(channelName);
        Message msg = createObjectMessage(message);
        channel.send(msg);
    }

    public Map<String, List<String>> getChatState() {
        return Collections.unmodifiableMap(chatState);
    }

    public void updateState(Map<String, List<String>> newChatState) {
        this.chatState.clear();
        this.chatState.putAll(newChatState);
    }

    public void userJoined(String nickname, String channel) {
        if (this.chatState.containsKey(channel)) {
            chatState.get(channel).add(nickname);
        } else {
            ArrayList<String> users = new ArrayList<>();
            users.add(nickname);
            chatState.put(channel, users);
        }
    }

    public void userLeft(String nickname, String channel) {
        if (this.chatState.containsKey(channel)) {
            chatState.get(channel).remove(nickname);
        }
    }

    private Message createActionMessage(String nick, String channelName, ActionType actionType) {
        ChatAction action = ChatAction.newBuilder()
                .setNickname(nick)
                .setChannel(channelName)
                .setAction(actionType)
                .build();
        return createObjectMessage(action);
    }

    private Message createObjectMessage(Object messageObj) {
        Message msg = new Message();
        msg.setObject(messageObj);
        return msg;
    }

    private JChannel initMgmtChannel() throws Exception {
        JChannel channel =
                initChannel(MANAGEMENT_CHANNEL_NAME, new ManagemementReceiverAdapter(this), MGMT_CHANNEL_MCAST_ADDRESS);
        channel.getState(null, 10000);
        return channel;
    }

    private JChannel initChannel(String channelName, Receiver receiver, String multicastAddress) {
        JChannel channel = null;
        try {
            channel = createBaseChannel(multicastAddress);
            channel.setName(nickname);
            channel.setReceiver(receiver);
            channel.connect(channelName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }

    private JChannel createBaseChannel(String multicastAddress) throws UnknownHostException {
        JChannel channel = new JChannel(false);
        ProtocolStack stack = new ProtocolStack();
        channel.setProtocolStack(stack);
        stack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName(multicastAddress)))
                .addProtocol(new PING())
                .addProtocol(new MERGE2())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK())
                .addProtocol(new UNICAST2())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2())
                .addProtocol(new STATE_TRANSFER())
                .addProtocol(new FLUSH());
        try {
            stack.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }
}
