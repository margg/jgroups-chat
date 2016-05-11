package pl.edu.agh.ds.chat;

public class Main {

    public static void main(String[] args) {

        try {
            Chat chat = new Chat("marg");
            chat.connectToChannel("2");

            chat.sendMessage("hi", "2");

            while (true) {}
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
