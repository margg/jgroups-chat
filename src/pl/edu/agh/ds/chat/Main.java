package pl.edu.agh.ds.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Nickname: ");
            String nickname = in.readLine();
            MessageCachingReceiver receiver = new MessageCachingReceiver(nickname);
            Chat chat = new Chat(nickname, receiver);

            printHelp();
            printChannelState(chat);

//            String activeChannel = "No active channel.";

            while (true) {
                System.out.print("> ");
                System.out.flush();
                String line = in.readLine();
                if (line.equals("help")) {
                    printHelp();
                    System.out.println();
                } else if (line.equals("show")) {
                    printChannelState(chat);
//                    System.out.println("Active channel: " + activeChannel);
                    System.out.println();
                }/* else if (line.startsWith("active ")) {
                    String channelName = line.split(" ")[1];
                    activeChannel = channelName;
                    System.out.println("Switched to channel " + channelName);

                    List<String> lastMessages = receiver.getAndRemoveLastMessages(channelName);
                    for (String message : lastMessages) {
                        System.out.println(activeChannel + ">> " + message);
                    }

                }*/ else if (line.startsWith("connect ")) {
                    String channelName = line.split(" ")[1];
                    chat.connectToChannel(channelName);
                    System.out.println("Connected to channel " + channelName);

                } else if (line.startsWith("exit")) {
                    break;
                } else {
                    String channelName = line.split(" ")[0];
                    if (channelName.matches("\\d+")) {
                        chat.sendMessage(line.substring(channelName.length()), channelName);
                    } else {
                        System.out.println("Please provide a valid channel number separated with a whitespace before the message.");
                    }
                }

//            System.out.println(chatState);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void printHelp() {
        System.out.println("Type:");
        System.out.println("\thelp         \tto view this help");
        System.out.println("\tshow         \tto show current chat state");
        System.out.println("\tconnect <n>  \tto connect to channel <n> (n between 1 and 200)");
//        System.out.println("\tactive <n> \tto switch to chat on channel <n>");
        System.out.println("\t<n> <message>\tto send message to channel <n>");
        System.out.println("\texit         \tto exit application");
    }

    private static void printChannelState(Chat chat) {
        Map<String, List<String>> chatState = chat.getChatState();
        System.out.println("All channels:\n");
        for (String channel : chatState.keySet()) {
            System.out.print(channel + ": [");

            for (String user : chatState.get(channel)) {
                System.out.print(user + " ");
            }
            System.out.println("]");
        }
    }
}
