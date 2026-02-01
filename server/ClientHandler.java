package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ClientHandler implements Runnable {

    private final Socket clientSocket; 
    private String clientSocketIDString;
    private PrintWriter out;
    private BufferedReader in;
    
    // Constructor 
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.clientSocketIDString = "";
    }

    public void setClientChatName(String username) {
        this.clientSocketIDString = username;
    }

    public String getClientIDString() {
        return clientSocketIDString;
    }

    public PrintWriter getWriter() {
        return out;
    }
    
    public void run() {
        try {
            // get outputstream from client
            out = new PrintWriter(
                clientSocket.getOutputStream(), true
            );
            // get inputstream from client
            in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
            );

            // init command handler for new client
            CommandHandler cmdHandler = new CommandHandler(this);

            // add the clients output stream to the list of all client output streams
            server.getClientOutputsList().add(out);

            String username = in.readLine();
            setClientChatName(username);

            // send all client socket information here 
            out.println(clientSocketIDString);


            server.getNumberOfConnectedClients().incrementAndGet();
            server.startLogSessionIfNeeded();
            String ip = clientSocket.getInetAddress().getHostAddress();
            server.log(clientSocketIDString, "joined server", ip);

            // Get intial timestamp on join
            String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            server.broadcast("[" + timestamp + "] " + clientSocketIDString + " has joined the server");

            String line; 
            while ((line = in.readLine()) != null) {
                // check for command request
                if(line.startsWith("/")) {
                    cmdHandler.handle(line);
                } else {
                    // update timestamp
                    timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    String fullMessage = "[" + timestamp + "] " + clientSocketIDString + ": " + line;
                    // log user messages
                    server.log(clientSocketIDString, line, ip);

                    // broadcast to all clients
                    server.broadcast(fullMessage);
                }
                    
            }

            // Client disconnects from server 
            System.out.println(clientSocketIDString + " disconnected");


        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // after client disconnects, close i/o streams and client socket 
        // remove client output stream from clientOutputs list 
        finally {
            try {
                server.getClientOutputsList().remove(out);
                String disconnectTimeStamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                server.broadcast("[" + disconnectTimeStamp + "] " + clientSocketIDString + " has left the server");

                String ip = clientSocket.getInetAddress().getHostAddress();
                server.log(clientSocketIDString, "left server", ip);

                // decrement client connection variable
                server.getNumberOfConnectedClients().decrementAndGet();
                server.endLogSessionIfNeeded();
                    
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                clientSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
