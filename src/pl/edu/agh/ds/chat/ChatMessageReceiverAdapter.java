package pl.edu.agh.ds.chat;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

public class ChatMessageReceiverAdapter extends ReceiverAdapter {

    private MessageCachingReceiver receiver;
    private String channelName;

    public ChatMessageReceiverAdapter(String channelName, MessageCachingReceiver receiver) {
        this.channelName = channelName;
        this.receiver = receiver;
    }

//    @Override
//    public void viewAccepted(View newView) {
//        super.viewAccepted(newView);
//        System.out.println("** Channel view changed: " + newView);
//    }

    @Override
    public void receive(Message msg) {
        receiver.putMessage(channelName, msg.getSrc().toString(), (String) msg.getObject());
    }
}
