package pl.edu.agh.ds.chat;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatAction;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatAction.ActionType;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagemementReceiverAdapter extends ReceiverAdapter {

    private Chat chat;

    public ManagemementReceiverAdapter(Chat chat) {
        this.chat = chat;
    }

    @Override
    public void viewAccepted(View newView) {

        //TODO

        super.viewAccepted(newView);
        System.out.println("** Cluster view changed: " + newView);
    }

    @Override
    public void receive(Message msg) {
        ChatAction action = (ChatAction) msg.getObject();
        if (action.getAction() == ActionType.JOIN) {
            chat.userJoined(action.getNickname(), action.getChannel());
        } else if (action.getAction() == ActionType.LEAVE) {
            chat.userLeft(action.getNickname(), action.getChannel());
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
        ChatState state = ChatState.newBuilder().addAllState(actionList).build();

        Util.objectToStream(state, new DataOutputStream(output));
    }

    @Override
    public void setState(InputStream input) throws Exception {
        Map<String, List<String>> newChatState = new HashMap<>();
        ChatState state = (ChatState) Util.objectFromStream(new DataInputStream(input));

        for (ChatAction action : state.getStateList()) {
            String channel = action.getChannel();
            String nickname = action.getNickname();
//            if (action.getAction() == ActionType.JOIN) {
                if (newChatState.containsKey(channel)) {
                    newChatState.get(channel).add(nickname);
                } else {
                    ArrayList<String> users = new ArrayList<>();
                    users.add(nickname);
                    newChatState.put(channel, users);
                }
//            }

        }
        chat.updateState(newChatState);
    }

}
