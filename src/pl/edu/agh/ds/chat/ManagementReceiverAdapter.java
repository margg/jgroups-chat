package pl.edu.agh.ds.chat;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatAction;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatAction.ActionType;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatState;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagementReceiverAdapter extends ReceiverAdapter {

    private Chat chat;

    public ManagementReceiverAdapter(Chat chat) {
        this.chat = chat;
    }

    @Override
    public void viewAccepted(View newView) {
        super.viewAccepted(newView);
        System.out.println("** Cluster view changed: " + newView);
        List<String> currentMemberNames = new ArrayList<>();
        for (Address member : newView.getMembers()) {
            currentMemberNames.add(member.toString());
        }
        chat.updateUsers(currentMemberNames);
    }

    @Override
    public void receive(Message msg) {
        try {
            ChatAction action = ChatAction.parseFrom(msg.getBuffer());
            if (action.getAction() == ActionType.JOIN) {
                chat.userJoined(action.getNickname(), action.getChannel());
            } else if (action.getAction() == ActionType.LEAVE) {
                chat.userLeft(action.getNickname(), action.getChannel());
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        List<ChatAction> actionList = new ArrayList<>();
        Map<String, List<String>> chatState = chat.getChatState();

        for (String channel : chatState.keySet()) {
            List<String> users = chatState.get(channel);
            for (String user : users) {
                actionList.add(ChatAction.newBuilder()
                        .setNickname(user)
                        .setChannel(channel)
                        .setAction(ActionType.JOIN)
                        .build());
            }
        }
        byte[] state = ChatState.newBuilder().addAllState(actionList).build().toByteArray();
        Util.objectToStream(state, new DataOutputStream(output));
    }

    @Override
    public void setState(InputStream input) throws Exception {
        Map<String, List<String>> newChatState = new HashMap<>();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
            System.out.println("bytes read: " + nRead);
        }

        ChatState state = ChatState.parseFrom(buffer.toByteArray());

        for (ChatAction action : state.getStateList()) {
            String channel = action.getChannel();
            String nickname = action.getNickname();
            if (action.getAction() == ActionType.JOIN) {
                if (newChatState.containsKey(channel)) {
                    newChatState.get(channel).add(nickname);
                } else {
                    ArrayList<String> users = new ArrayList<>();
                    users.add(nickname);
                    newChatState.put(channel, users);
                }
            }

        }
        chat.updateState(newChatState);
    }
}
