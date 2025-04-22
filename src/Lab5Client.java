import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;


public class Lab5Client {


    static Socket socket = null;
    static PrintWriter out = null;
    static BufferedReader in = null;
    static JTextArea textArea;

    public static void main(String[] args) {

        // Creates main frame
        JFrame frame = new JFrame("Simple Chat Client");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Creates the main text area
        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        textArea.setEditable(false);

        // Create Parent Panel
        JPanel parentPanel = new JPanel();
        parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.Y_AXIS));

        // Add the Message panel
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new FlowLayout());
        JButton sendButton = new JButton("Send");
        sendButton.setEnabled(false);
        JTextArea messageGui = new JTextArea(null, 1, 30);
        messageGui.setEditable(false);

        messagePanel.add(messageGui);
        messagePanel.add(sendButton);


        // Add the Connection panel
        JPanel connectionPanel = new JPanel();
        messagePanel.setLayout(new FlowLayout());

        JLabel serverLabel = new JLabel("Server: ");
        JTextArea serverField = new JTextArea("localhost", 1, 16);

        JLabel portLabel = new JLabel("Port: ");
        JTextArea portField = new JTextArea("7225",1, 5);

        JButton connectButton = new JButton("Connect");

        // Add components to Panel
        connectionPanel.add(serverLabel);
        connectionPanel.add(serverField);
        connectionPanel.add(portLabel);
        connectionPanel.add(portField);
        connectionPanel.add(connectButton);

        // Add child panels to parent panel
        parentPanel.add(messagePanel);
        parentPanel.add(connectionPanel);

        // Add the parent panel to the frame
        frame.add(parentPanel, BorderLayout.SOUTH);

        // Sets the frame to visible
        frame.setVisible(true);

        // Get the port and address from the user
        final int PORT = Integer.parseInt(portField.getText()); // DEFAULT = "7225"
        final String ADDRESS = serverField.getText(); // DEFAULT = "localhost"

        // Connect to the server given an address and a port number
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // Connect to the server
                try {
                    socket = new Socket(ADDRESS, PORT);


                    out = new PrintWriter(socket.getOutputStream(), true); // Init 'out'

                    in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Init 'in'


                    System.out.println("Connected to " + socket.getInetAddress().getHostName());
                }
                catch (UnknownHostException UHe) {
                    System.err.println("Could not connect to " + ADDRESS + ":" + PORT);
                }
                catch (IOException IOe) {
                    System.err.println("Server not responding at " + ADDRESS + ":" + PORT);
                }


                // Create a thread that will listen to the server for broadcasted messages
                Thread listenThread = new Thread(() -> {
                    String serverMessage;
                    try {
                        while ((serverMessage = in.readLine()) != null) {
                            textArea.append(serverMessage + "\n");
                            System.out.println("Server: " + serverMessage);
                        }
                    } catch (IOException IOe) {
                        System.err.println("Error receiving from server: " + IOe.getMessage());
                    }
                });

                listenThread.setDaemon(true); // Marks this thread as a daemon thread
                listenThread.start();


                sendButton.setEnabled(true); // Send button gets enabled
                messageGui.setEditable(true); // Message text field becomes editable
                portField.setEditable(false); // Port Text field becomes UNeditable
                serverField.setEditable(false); // Server Text field becomes UNeditable
                connectButton.setEnabled(false); // Connect button is disabled



            }

        });


        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // Sends the text to the server so that it may be broadcast
                String message = messageGui.getText();
                if(!message.isEmpty()) {
                    out.println(message);
                    messageGui.setText("");

                }


            }
        });

    }

}
