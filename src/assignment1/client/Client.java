package assignment1.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class Client {

    private BufferedReader reader;
    private PrintWriter writer;

    public Client(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to server: " + socket);
            new SocketReadingThread().start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class SocketReadingThread extends Thread {
        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    handleInboundMessage(message);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public abstract void handleInboundMessage(String message);

    public void sendMessage(String message) {
        writer.println(message);
    }

}