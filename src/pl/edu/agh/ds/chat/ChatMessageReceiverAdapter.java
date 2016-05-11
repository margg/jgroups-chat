package pl.edu.agh.ds.chat;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

public class ChatMessageReceiverAdapter extends ReceiverAdapter {

    @Override
    public void viewAccepted(View newView) {
        super.viewAccepted(newView);
        System.out.println("** Channel view changed: " + newView);
    }

    @Override
    public void receive(Message msg) {
        String line = msg.getSrc() + ": " + msg.getObject();
        System.out.println(line);
    }
}
