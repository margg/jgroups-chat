package pl.edu.agh.ds.chat;

import com.sun.javaws.exceptions.InvalidArgumentException;

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
            Chat chat = new Chat(nickname, new MessagePrinter(nickname));

            printHelp();
            printChannelState(chat);

            while (true) {
                System.out.print("> ");
                System.out.flush();
                String line = in.readLine();
                if (line.equals("help")) {
                    printHelp();
                    System.out.println();
                } else if (line.equals("show")) {
                    printChannelState(chat);
                    System.out.println();
                } else if (line.startsWith("connect ")) {
                    String channelName = line.split(" ")[1];
                    try {
                        if (Integer.valueOf(channelName) <= 200 && Integer.valueOf(channelName) >= 1) {
                            chat.connectToChannel(channelName);
                            System.out.println("Connected to channel " + channelName);
                        } else {
                            System.out.println("Invalid channel name. Must be an integer between 1 and 200.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid channel name. Must be an integer between 1 and 200.");
                    }
                } else if (line.startsWith("disconnect ")) {
                    String channelName = line.split(" ")[1];
                    try {
                        chat.disconnectFromChannel(channelName);
                    } catch (InvalidArgumentException e) {
                        System.out.println("Invalid channel name.");
                    }
                    System.out.println("Disconnected from channel " + channelName);
                } else if (line.startsWith("exit")) {
                    chat.leaveChat();
                    System.out.println("Be back!");
                    break;
                } else {
                    String channelName = line.split(" ")[0];
                    if (channelName.matches("\\d+")) {
                        chat.sendMessage(line.substring(channelName.length()), channelName);
                    } else {
                        System.out.println("Please provide a valid channel number separated with a whitespace before the message.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void printHelp() {
        System.out.println("Please type:");
        System.out.println("\thelp          \tto view this help");
        System.out.println("\tshow          \tto show current chat state");
        System.out.println("\tconnect <n>   \tto connect to channel <n> (n between 1 and 200)");
        System.out.println("\tdisconnect <n>\tto disconnect from channel <n>");
        System.out.println("\t<n> <message> \tto send message to channel <n>");
        System.out.println("\texit          \tto exit application");
    }

    private static void printChannelState(Chat chat) {
        Map<String, List<String>> chatState = chat.getChatState();
        if (!chatState.isEmpty()) {
            System.out.println("All channels:\n");
            for (String channel : chatState.keySet()) {
                System.out.print(channel + ": [ ");
                for (String user : chatState.get(channel)) {
                    System.out.print(user + " ");
                }
                System.out.println("]");
            }
        }
    }
}
