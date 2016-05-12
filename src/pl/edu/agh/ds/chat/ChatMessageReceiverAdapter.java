package pl.edu.agh.ds.chat;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

public class ChatMessageReceiverAdapter extends ReceiverAdapter {

    private MessagePrinter receiver;
    private String channelName;

    public ChatMessageReceiverAdapter(String channelName, MessagePrinter receiver) {
        this.channelName = channelName;
        this.receiver = receiver;
    }

    @Override
    public void receive(Message msg) {
        receiver.putMessage(channelName, msg.getSrc().toString(), (String) msg.getObject());
    }
}
