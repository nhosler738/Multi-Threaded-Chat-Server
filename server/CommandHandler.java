package server;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CommandHandler {
    /*
    Commands that are visible only to the client calling them:
        - HELP
        - USERS
        - TIME

    Commands displayed to everyone:
        - AFK
        - BACK
        - NICK
    */

    enum Commands {
        HELP,
        USERS,
        NICK,
        TIME,
        AFK,
        BACK
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
                if (!args.isEmpty()) {
                    sendMessage("Usage: /help");
                    
                } else {
                    sendMessage("Available commands: /help, /users, /nick, /time, /afk");
                }
                break;

            // args for later: /users online, afk
            case USERS:
                if (!args.isEmpty()) {
                    sendMessage("Usage: /users");
                } else {

                }
                break;
            case NICK:
                // verify there are two parts (command + arg)
                if (args.isEmpty()) {
                    sendMessage("Usage: /nick <new name>");
                } else {
                    changeUsername(client, args);
                }
                break;
            case AFK:
                if (!args.isEmpty()) {
                    sendMessage("Usage: /afk");
                } else {

                }
                break;
            case BACK:
                if (!args.isEmpty()) {
                    sendMessage("Usage: /back");
                } else {

                }
                break;
            case TIME:
                if (!args.isEmpty()) {
                    sendMessage("Usage: /time");
                } else {

                }
                break;

            
        }
        
    

        
    }


    public void changeUsername(ClientHandler client, String newName) {
        String oldName = client.getClientIDString();

        // try to insert new key atomically
        if (server.getClients().putIfAbsent(newName, client) == null) {
            // success: remove old key 
            server.getClients().remove(oldName);
            client.setClientChatName(newName);
            sendMessage("Username successfully changed: " + newName);
        } else {
            sendMessage("Username: " + newName + " already exists");
        }
    }

    public void users(ClientHandler client) {
        List<String> listOfConnectedUsers = new ArrayList<>();

        if (server.getClients().size() == 1) {
            sendMessage("No other users currently active...");
            return;
        }

        for (String clientUsername : server.getClients().keySet()) {
            // skip user executing this command (only show other users)
            if (clientUsername.equals(client.getClientIDString())) {
                continue;
            } 
            // add users username to print 
            listOfConnectedUsers.add(clientUsername);
        }

        // print output of all connected users
        String output = "Connected Users: ";
        String lastName = listOfConnectedUsers.getLast();

        for (String name : listOfConnectedUsers) {

            if (name.equals(lastName)) {
                output += name;
            } else {
                output += name + ", ";
            }

            
            
        }

        sendMessage(output);
        
        
    }


    public void sendMessage(String message) {
        clientOutStream.println(message);
        clientOutStream.flush();
    }


}