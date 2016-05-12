package pl.edu.agh.ds.chat;

public class MessagePrinter {

    private String nickname;

    public MessagePrinter(String nickname) {
        this.nickname = nickname;
    }

    public void putMessage(String channelName, String src, String message) {
        if (!src.equals(nickname)) {
            System.out.println(channelName + "> " + src + ": " + message);
        }
    }
}
