
// Java Networking Libraries
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

// Client Interface Libraries
import javax.swing.*;
import java.awt.BorderLayout;



public class client {

    static class ClientGUI {
        JFrame frame;
        JTextArea chatArea;
        JTextField inputField;
        JButton sendButton;

        ClientGUI(JFrame frame, JTextArea chatArea, JTextField inputField, JButton sendButton) {
            this.frame = frame;
            this.chatArea = chatArea;
            this.inputField = inputField;
            this.sendButton = sendButton;
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter server hostname:");
        String hostname = sc.nextLine();
        System.out.println("Enter server port number:");
        int port = sc.nextInt();

        // start client application
        startApplication(hostname, port);

        sc.close();
        
    }

    public static ClientGUI createInterface(String clientIDString) {
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

        
        return new ClientGUI(frame, chatArea, inputField, sendButton);
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
            ClientGUI clientInterface = createInterface(clientIDString);

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


