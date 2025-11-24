import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Client Interface Libraries
import javax.swing.*;

public class client {

    public static void main(String[] args) throws IOException {
        //String hostname = "localhost";
        //int port = 1234;

        //startApplication(hostname, port);
        
        // Testing JFrame interface
        testClientInterface();

    }

    public static JFrame createInterface(String clientIDString) {
        // Create new JFrame (main client window)
        JFrame frame = new JFrame("Client Interface - " + clientIDString);
        frame.setSize(400, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Chat area (view all clients messages)
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(chatArea);

        // Input area (for entering messages to server)
        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.add(scroll, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        
        return frame;
    }

    public static void startApplication(String hostname, int port) {
        try {
            Socket socket = connectToServer(hostname, port);
            
            // setup client input and output streams to server 
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // get client id from server clienthandler 
            String clientIDString = in.readLine();

            // start interface 
            JFrame clientInterface = createInterface(clientIDString);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
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


    // Tests
    public static void testClientInterface() {
        String testID = "Test Client ID 1";
        JFrame clientInterface = createInterface(testID);
        clientInterface.setVisible(true);
    }
    
}


