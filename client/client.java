package client;

// Java Networking Libraries
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Client Interface Libraries
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;



public class client {

    static class ClientStartGUI {
        JFrame frame;
        JTextField hostnameField;
        JTextField portField;
        JTextField clientUsername;
        JButton sendButton;


        String hostname;
        String port;
        String chatUsername;

        ClientStartGUI(JFrame frame, JTextField hostnameField,
        JTextField portField, JButton sendButton, JTextField clientUsername) { 
            this.frame = frame;
            this.hostnameField = hostnameField;
            this.portField = portField;
            this.sendButton = sendButton;
            this.clientUsername = clientUsername;
        }

        public void addListeners() {
            sendButton.addActionListener(e -> {
                this.hostname = hostnameField.getText();
                this.port = portField.getText();
                this.chatUsername = clientUsername.getText();

                // start client connection 
                startCommunicationWithServer(hostname, Integer.parseInt(port), chatUsername);
            });
        }
    }
 
   

    static class ClientChatGUI {
        JFrame frame;
        JTextArea chatArea;
        JTextField inputField;
        JButton sendButton;

        ClientChatGUI(JFrame frame, JTextArea chatArea, JTextField inputField, JButton sendButton) {
            this.frame = frame;
            this.chatArea = chatArea;
            this.inputField = inputField;
            this.sendButton = sendButton;
        }
    }

    
    public static void main(String[] args) throws IOException {
        

        // start gui 
        ClientStartGUI startFrame = createStartGUI();
        startFrame.addListeners();
        startFrame.frame.setVisible(true);


        

        
        
    }

    public static ClientStartGUI createStartGUI() {
        JFrame frame = new JFrame("Start chatting");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Input fields
        JTextField hostname = new JTextField("Hostname");
        JTextField port = new JTextField("Port");
        JTextField username = new JTextField("Username");
        JButton enterButton = new JButton("Enter");

        // Panel for input fields
        JPanel inputPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        inputPanel.add(hostname);
        inputPanel.add(port);
        inputPanel.add(username);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(enterButton, BorderLayout.SOUTH);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);

        return new ClientStartGUI(frame, hostname, port, enterButton, username);
    }


    public static ClientChatGUI createChatGUI(String clientIDString) {
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

        
        return new ClientChatGUI(frame, chatArea, inputField, sendButton);
    }
    
    

    

    

    public static void startCommunicationWithServer(String hostname, int port, String username) {
        try {
            Socket socket = connectToServer(hostname, port);
            
            // setup client input and output streams to server 
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // send preferred chat username to server 
            out.println(username);

            // get client id from server clienthandler 
            String clientIDString = in.readLine();

            // start interface 
            ClientChatGUI clientInterface = createChatGUI(clientIDString);

            // Setup listeners 
            
            // send button 
            clientInterface.sendButton.addActionListener(e -> {
                String text = clientInterface.inputField.getText();
                clientInterface.inputField.setText("");
                out.println(text);
            });

            // if client presses enter instead of clicking send button
            clientInterface.inputField.addActionListener(e -> {
                clientInterface.sendButton.doClick();
            });
            
            // listener for server messages (other clients messages)
            new Thread(() -> {
                try {
                    String msg;

                    // read messages continuously
                    while ((msg = in.readLine()) != null) {
                        clientInterface.chatArea.append(msg + "\n");
                    } 

                    // If loop exits: server closed the connection
                    clientInterface.chatArea.append("Disconnected from server\n");

                } catch (IOException ex) {
                    clientInterface.chatArea.append("Connection error\n");
                } 
            }).start();




            // show the GUI
            clientInterface.frame.setVisible(true);



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


    
}


