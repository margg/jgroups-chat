package pl.edu.agh.ds.chat;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos;

public class ChatMessageReceiverAdapter extends ReceiverAdapter {

    private MessagePrinter receiver;
    private String channelName;

    public ChatMessageReceiverAdapter(String channelName, MessagePrinter receiver) {
        this.channelName = channelName;
        this.receiver = receiver;
    }

    @Override
    public void receive(Message msg) {
        try {
            ChatOperationProtos.ChatMessage chatMessage = ChatOperationProtos.ChatMessage.parseFrom(msg.getBuffer());
            receiver.putMessage(channelName, msg.getSrc().toString(), chatMessage.getMessage());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
