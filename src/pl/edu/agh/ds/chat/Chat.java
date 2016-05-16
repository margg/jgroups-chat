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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Chat {

    private static final String CHANNEL_BASE_MCAST_ADDRESS = "230.0.0.";
    private static final String MANAGEMENT_CHANNEL_NAME = "ChatManagement321123";

    private final JChannel managementChannel;
    private final Map<String, JChannel> userChannels;
    private final Map<String, List<String>> chatState;

    private final String nickname;
    private MessagePrinter messagePrinter;

    public Chat(String nickname, MessagePrinter messagePrinter) throws Exception {
        this.nickname = nickname;
        this.messagePrinter = messagePrinter;
        this.userChannels = new HashMap<>();
        this.chatState = new ConcurrentHashMap<>();
        this.managementChannel = initMgmtChannel();
    }

    public void connectToChannel(String channelName) throws Exception {
        JChannel channel = initChannel(channelName, new ChatMessageReceiverAdapter(channelName, messagePrinter),
                CHANNEL_BASE_MCAST_ADDRESS + channelName);
        userChannels.put(channelName, channel);

        managementChannel.send(createActionMessage(nickname, channelName, ActionType.JOIN));
    }

    public void disconnectFromChannel(String channelName) throws Exception {
        if (userChannels.containsKey(channelName)) {
            userChannels.get(channelName).close();
            userChannels.remove(channelName);

            managementChannel.send(createActionMessage(nickname, channelName, ActionType.LEAVE));
        } else {
            throw new IllegalArgumentException("Disconnecting from not connected channel.");
        }
    }

    public void sendMessage(String message, String channelName) throws Exception {
        if (userChannels.containsKey(channelName)) {
            JChannel channel = userChannels.get(channelName);
            Message msg = new Message();
            msg.setBuffer(message.getBytes());
            channel.send(msg);
        } else {
            connectToChannel(channelName);
            sendMessage(message, channelName);
        }
    }

    public void leaveChat() throws Exception {
        for (String userChannel : userChannels.keySet()) {
            managementChannel.send(createActionMessage(nickname, userChannel, ActionType.LEAVE));
            userChannels.get(userChannel).close();
        }
        managementChannel.close();
    }

    public Map<String, List<String>> getChatState() {
        return Collections.unmodifiableMap(chatState);
    }

    public void updateState(Map<String, List<String>> newChatState) {
        this.chatState.clear();
        this.chatState.putAll(newChatState);
    }

    public void updateUsers(List<String> currentUserNames) {
        for (List<String> channelUsers : chatState.values()) {
            channelUsers.retainAll(currentUserNames);
        }
        for (Map.Entry<String, List<String>> stateEntry : chatState.entrySet()) {
            if (stateEntry.getValue().isEmpty()) {
                chatState.remove(stateEntry.getKey());
            }
        }
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

        Message msg = new Message();
        msg.setBuffer(action.toByteArray());
        return msg;
    }

//    private Message createByteArrayMessage(Object messageObj) {
//
//        return msg;
//    }

    private JChannel initMgmtChannel() throws Exception {
        JChannel channel = createBaseManagementChannel();
        channel.setName(nickname);
        channel.setReceiver(new ManagementReceiverAdapter(this));
        channel.connect(MANAGEMENT_CHANNEL_NAME);
        channel.getState(null, 10000);
        return channel;
    }

    private JChannel initChannel(String channelName, Receiver receiver, String multicastAddress) throws Exception {
        JChannel channel = createBaseChannel(multicastAddress);
        channel.setName(nickname);
        channel.setReceiver(receiver);
        channel.connect(channelName);
        return channel;
    }

    private JChannel createBaseChannel(String multicastAddress) throws Exception {
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
        stack.init();
        return channel;
    }

    private JChannel createBaseManagementChannel() throws Exception {
        JChannel channel = new JChannel(false);
        ProtocolStack stack = new ProtocolStack();
        channel.setProtocolStack(stack);
        stack.addProtocol(new UDP())
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
        stack.init();
        return channel;
    }
}
