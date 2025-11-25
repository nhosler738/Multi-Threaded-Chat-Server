import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;



class server {

    // list of all client output streams (clients connected to the server)
    private static final List<PrintWriter> clientOutputs = new CopyOnWriteArrayList<>();
    private static int clientIDCounter = 0;
    
    public static void main(String[] args) {
        

        try (ServerSocket server = new ServerSocket(1234)) {
            System.out.println("Server running on port: " + server.getLocalPort());

            // running infinite loop for getting client requests
            while (true) {
                Socket clientSocket = server.accept();
                int thisClientID = clientIDCounter++;

                ClientHandler clientHandler = new ClientHandler(clientSocket, thisClientID);

                // Display that new client has connected to server with its ID number
                System.out.println("New client connected: " + clientHandler.getClientIDString());
                
                // This thread will handle the new client separately 
                new Thread(clientHandler).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast message to all connected clients 
    public static void broadcast(String message) {
        for (PrintWriter writer : clientOutputs) {
            writer.println(message);
            writer.flush();
        }
    }

    private static class ClientHandler implements Runnable {

        private final Socket clientSocket; 
        private final int clientSocketID;
        private final String clientSocketIDString;
        private PrintWriter out;
        private BufferedReader in;
    
        // Constructor 
        public ClientHandler(Socket socket, int clientID) {
            this.clientSocket = socket;
            this.clientSocketID = clientID;
            this.clientSocketIDString = initClientIDString();
        }

        public String initClientIDString() {
            return "Client" + this.clientSocketID;
        }

        public String getClientIDString() {
            return clientSocketIDString;
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

                // add the clients output stream to the list of all client output streams
                clientOutputs.add(out);


                // send all client socket information here 
                out.println(clientSocketIDString);

                String line; 
                while ((line = in.readLine()) != null) {
                    String fullMessage = clientSocketIDString + ": " + line;
                    // *** log message in log.txt file for future ***
                    System.out.println(fullMessage);

                    // broadcast to all clients
                    server.broadcast(fullMessage);
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
                    clientOutputs.remove(out);
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
}

