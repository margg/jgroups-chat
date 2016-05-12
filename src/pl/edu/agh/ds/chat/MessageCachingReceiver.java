package pl.edu.agh.ds.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageCachingReceiver {


    private Map<String, List<String>> cache = new ConcurrentHashMap<>();
    private String nickname;

    public MessageCachingReceiver(String nickname) {
        this.nickname = nickname;
    }

    public List<String> getAndRemoveLastMessages(String channelName) {
        List<String> messages = new ArrayList<>();
        if (cache.containsKey(channelName)) {
            messages = cache.get(channelName);
            cache.get(channelName).clear();
        }
        return messages;
    }

    public void putMessage(String channelName, String message, String src) {
//        if (cache.containsKey(channelName)) {
//            cache.get(channelName).add(message);
//        } else {
//            ArrayList<String> messages = new ArrayList<>();
//            messages.add(message);
//            cache.put(channelName, messages);
//        }
        if (!src.equals(nickname)) {
            System.out.println(channelName + "> " + src + ": " + message);
        }
    }
}
