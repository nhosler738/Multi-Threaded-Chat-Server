package server;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
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
            // print server lan address 
            returnLanAddress();
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

    public static void returnLanAddress() {

        try {
            // get lan ip address of server socket 
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                
                // skip loopback (localhost) and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        if (ip.startsWith("192.") || ip.startsWith("10.")) {
                            System.out.println("LAN IP: " + ip);
                        }
                    }
                }
            }

            
            

        } catch (SocketException e) {
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


    static synchronized void startLogSessionIfNeeded() {
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

    static synchronized void log(String clientName, String message, String ip) {
        if (!loggingSessionActive || logWriter == null) return;

        String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        logWriter.println(clientName + " | " + message + " | " + ip + " | " + timestamp);
        logWriter.flush();

    }

    static synchronized void endLogSessionIfNeeded() {
        if (numberOfClientsConnected.get() == 0 && loggingSessionActive) {
            System.out.println("Logging session ended");
            loggingSessionActive = false;

            if (logWriter != null) {
                logWriter.close();
                logWriter = null;
            }
        }
    }

    // getters for ClientHandler.java
    public static List<PrintWriter> getClientOutputsList() {
        return clientOutputs;
    }

    public static AtomicInteger getNumberOfConnectedClients() {
        return numberOfClientsConnected;
    }

   
}

