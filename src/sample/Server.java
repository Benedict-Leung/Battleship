package sample;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    protected Socket clientSocket = null;
    protected ServerSocket serverSocket = null;

    public static int SERVER_PORT = 16789;

    public ClientConnectionHandler waitingClient;
    public int numClients = 0;

    public Server() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Listening to port: " + SERVER_PORT);

            while (true) {
                clientSocket = serverSocket.accept();
                ClientConnectionHandler client = new ClientConnectionHandler(clientSocket);
                numClients++;

                if (numClients % 2 == 0) {
                    try {
                        waitingClient.out.writeObject("Connect");
                        Room room = new Room(waitingClient, client);
                        System.out.println("Starting room");
                    } catch (Exception e) {
                        System.out.println("User Disconnected");
                        waitingClient = client;
                        numClients--;
                    }
                } else {
                    waitingClient = client;
                }
            }
        } catch (IOException e) {
            System.err.println("IOException while creating server connection");
        }
    }

    public static void main(String[] args) {
        Server app = new Server();
    }
}
