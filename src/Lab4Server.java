import java.io.*;
import java.net.*;
import java.lang.Runnable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;


public class Lab4Server {


    private static int clientCounter = 0; // Used to count how many clients are connected and for stylistic purposes
    private static final int PORT = 7225;

    // Hashmap to check connected clients, useful for broadcast function
    private static ConcurrentHashMap <String, ClientHandler> clients = new ConcurrentHashMap<>();

    // Queue using Linked List implementation to create a message history for each client that connects
    private static Queue<String> chatHistory = new LinkedList<>();

    public static void main(String[] args) throws IOException {


        // Init server and listen on port for client connections
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Connect Client to Server
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());
                clientCounter++; // Increment Client Counter
                String clientName = "Client " + clientCounter;

                // Pass Client socket and the Client name to the clientHandler
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientName);


                // Create and start a new thread for each client that joins the server
                clients.put(clientName, clientHandler);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();


            }
        }
        catch (IOException e) {
            System.err.println(e);
        }

    }

    // Broadcasts a message to all connected clients
    private static void broadcast(String message, ClientHandler sender) {
        for(Map.Entry<String, ClientHandler> entry : clients.entrySet() ) {
            entry.getValue().sendMessage(message);

            System.out.println("Sent to " + sender + ": " + message);
        }

    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final String clientName;
        private BufferedReader in;
        private PrintWriter out;

        // Constructor that init variables and connect in and out
        public ClientHandler(Socket clientSocket, String clientName) {

            System.out.println("Client " + clientName + " connected");

            this.clientSocket = clientSocket;
            this.clientName = clientName;

            try {
                in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

                out = new PrintWriter(
                        clientSocket.getOutputStream(), true);
            }
            catch (IOException e) {
                System.err.println(e);
            }

            for(Object item : chatHistory) {
                out.println(item); // Sends chat history to the connected client
                System.out.println("Sent to " + clientName + ": " + item);
            }

        }

        // Overrides the run method to receive input from the client and broadcast it to all connected clients
        @Override
        public void run() {

            try {
                String receivedLine;

                while ((receivedLine = in.readLine()) != null) {

                    System.out.println("Received from " + clientName +  ": " + receivedLine);
                    String formattedMessage = clientName + ": " + receivedLine;
                    broadcast(formattedMessage, this); // sends message and the ClientHandler object
                    chatHistory.add(formattedMessage); // Adds formatted message to the universal queue

                }
            }
            catch (IOException e) {
                System.err.println(e);
            }
            finally {
                try {
                    clientSocket.close(); // Close the client socket
                    clients.remove(clientName); // Remove client from the clients HashMap
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }

        public void sendMessage(String message) {
            out.println(message); // Sends a message to the client
        }
    }
}

/* NOTES */

// Server program will need to be multithreaded in order to support multiple clients at one time
// When a client connects, we will need to create a new thread

// Important to isolate each client thread connection to prevent the race condition problem

// Sequence
// Create a server socket to listen on a specified port
    // Connect a client to the serversocket to create two-way communication
    // Spawn a thread to handle the client, call an instance of the client handler
// Continue the previous two as the server continues to listen
// Ensure that when a client closes a connection that all resources are closed



