package server;

import java.io.PrintWriter;

public class CommandHandler {
    /*
    Commands that are visible only to the client calling them:
        - HELP
        - USERS
        - TIME
    */

    enum Commands {
        HELP,
        USERS,
        NICK,
        TIME,
        AFK
    }

    
    private PrintWriter clientOutStream;
    private ClientHandler client;

    public CommandHandler(ClientHandler client) {
        this.client = client;
        this.clientOutStream = client.getWriter();
        
    }

    public void handle(String message) {
        // remove "/" from message
        String withoutSlash = message.substring(1); // remove leading "/"

        // split by space into command + args
        String[] parts = withoutSlash.split(" ", 2); // max two parts

        String command = parts[0].toUpperCase();
        String args = "";
        if (parts.length > 1) {
            args = parts[1].trim();
        }

        // convert string command to enum
        Commands cmd;
        try {
            cmd = Commands.valueOf(command);
        } catch (IllegalArgumentException e) {
            sendMessage("Unknown command: " + command);
            return;
        }

        // run the command functionality
        switch (cmd) {
            case HELP:
                sendMessage("Available commands: /help, /users, /nick, /time, /afk");
                break;
            case USERS:
                break;
            case NICK:
                // verify there are two parts (command + arg)
                if (args.isEmpty()) {
                    sendMessage("Usage: /nick <new name>");
                } else {
                    setNewUsername(args);
                    sendMessage("Username changed to: " + args);
                }
                break;
        }
        
    

        
    }

    public void setNewUsername(String newName) {
        client.setClientChatName(newName);
    }

    public void sendMessage(String message) {
        clientOutStream.println(message);
        clientOutStream.flush();
    }
}