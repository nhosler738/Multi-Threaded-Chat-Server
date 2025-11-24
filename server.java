import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;



class server {

    
    public static void main(String[] args) {
        ServerSocket server = null;
        int clientIdCounter = 0;

        try {
            // server listening on port 1234 at localhost
            server = new ServerSocket(1234);
            server.setReuseAddress(true);

            // running infinite loop for getting client requests
            while (true) {
                Socket clientSocket = server.accept();
                int thisClientID = clientIdCounter++;

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
        finally {
            if (server != null) {
                try {
                    server.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ClientHandler implements Runnable {

        private final Socket clientSocket; 
        private final int clientSocketID;
        private final String clientSocketIDString;
    
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
            PrintWriter out = null;
            BufferedReader in = null;

            try {
                // get outputstream from client
                out = new PrintWriter(
                    clientSocket.getOutputStream(), true
                );
                // get inputstream from client
                in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream())
                );

                // send all client socket information here 
                out.println(clientSocketIDString);

                String line; 
                while ((line = in.readLine()) != null) {
                    // writing the received message from client 
                    System.out.println("Sent from " + clientSocketIDString + ": " + line);
                }

                // Client disconnects from server 
                System.out.println(clientSocketIDString + " disconnected");


            }
            catch (IOException e) {
                e.printStackTrace();
            }
            // after, close i/o streams and client socket 
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

