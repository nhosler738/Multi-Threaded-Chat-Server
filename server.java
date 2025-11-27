import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;



class server {

    // list of all client output streams (clients connected to the server)
    private static final List<PrintWriter> clientOutputs = new CopyOnWriteArrayList<>();
    // variable to track number of client connections (thread safe)
    private static final AtomicInteger numberOfClientsConnected = new AtomicInteger(0);
    // only one session active at a time 
    private static boolean loggingSessionActive = false;
    private static PrintWriter logWriter;
    private static Path logDirectory;
    private static int sessionNumber = 0;
    public static void main(String[] args) {
        

        try (ServerSocket server = new ServerSocket(1234)) {
            System.out.println("Server running on port: " + server.getLocalPort());

            // check for log dir 
            checkForLogDir();
    

            // running infinite loop for getting client requests
            while (true) {
                Socket clientSocket = server.accept();
            
                ClientHandler clientHandler = new ClientHandler(clientSocket);

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

    public static void checkForLogDir() {
        try {
            // Get server dir 
            String serverDir = System.getProperty("user.dir");

            // build path to client_log dir
            Path logDir = Paths.get(serverDir, "client_log");

            logDirectory = logDir;

            // create directory if doesn't already exist
            if (!Files.exists(logDir)) {
                Files.createDirectory(logDir);
                System.out.println("client_log directory created");
            } else {
                System.out.println("client_log already exists");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        
        
    }


    private static synchronized void startLogSessionIfNeeded() {
        if (!loggingSessionActive) {
            loggingSessionActive = true;
            sessionNumber++;

            String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            
            String filename = "log_session" + sessionNumber + "_" + timestamp + ".txt";
            try {
                logWriter = new PrintWriter(Files.newBufferedWriter(logDirectory.resolve(filename)));
                System.out.println("Logging session started: " + filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static synchronized void log(String clientName, String message, String ip) {
        if (!loggingSessionActive || logWriter == null) return;

        String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        logWriter.println(clientName + " | " + message + " | " + ip + " | " + timestamp);
        logWriter.flush();

    }

    private static synchronized void endLogSessionIfNeeded() {
        if (numberOfClientsConnected.get() == 0 && loggingSessionActive) {
            System.out.println("Logging session ended");
            loggingSessionActive = false;

            if (logWriter != null) {
                logWriter.close();
                logWriter = null;
            }
        }
    }

    
    private static class ClientHandler implements Runnable {

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

                String username = in.readLine();
                setClientChatName(username);

                // send all client socket information here 
                out.println(clientSocketIDString);

                numberOfClientsConnected.incrementAndGet();
                startLogSessionIfNeeded();
                String ip = clientSocket.getInetAddress().getHostAddress();
                log(clientSocketIDString, "joined server", ip);

                // Get intial timestamp on join
                String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                server.broadcast("[" + timestamp + "] " + clientSocketIDString + " has joined the server");

                String line; 
                while ((line = in.readLine()) != null) {
                    // update timestamp
                    timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    String fullMessage = "[" + timestamp + "] " + clientSocketIDString + ": " + line;
                    // log user messages
                    log(clientSocketIDString, line, ip);

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
                    String disconnectTimeStamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    server.broadcast("[" + disconnectTimeStamp + "] " + clientSocketIDString + " has left the server");

                    String ip = clientSocket.getInetAddress().getHostAddress();
                    log(clientSocketIDString, "left server", ip);

                    // decrement client connection variable
                    numberOfClientsConnected.decrementAndGet();
                    endLogSessionIfNeeded();
                    
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

