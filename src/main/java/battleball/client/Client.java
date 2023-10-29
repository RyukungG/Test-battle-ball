package battleball.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public abstract class Client {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread thread;

    public Client(String host, int port) {
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to server: " + socket);
            thread = new SocketReadingThread();
            thread.start();
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
            } catch (SocketException ignored) {

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public abstract void handleInboundMessage(String message);

    public void sendMessage(String message) {
        writer.println(message);
    }

    public void stop() {
        thread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}