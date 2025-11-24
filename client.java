import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.*;

public class client {

    public static void main(String[] args) throws IOException {
        String hostname = "localhost";
        int port = 1234;
        
        Socket socket = connectToServer(hostname, port);
        
        // setup I/O streams between client and server 
        if (socket != null) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // receive all client socket information here from server 
            String clientIDString = in.readLine();

            // start interface with client info from server 
            JFrame clientInterface = createInterface(clientIDString);

            
        }

    }

    public static JFrame createInterface(String clientIDString) {
        // Create new JFrame (main client window)
        JFrame frame = new JFrame("Client Interface - " + clientIDString);
        frame.setSize(400, 500);
        
        return frame;
    }


    public static Socket connectToServer(String hostname, int port) {
        // establish connection by providing host and port number 
        try {
            Socket socket = new Socket(hostname, port);
            return socket;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
}


