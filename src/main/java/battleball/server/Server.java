package battleball.server;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.List;

public class Server {

    private World world;
    private ServerSocket socket;
    private Map<Integer, ClientHandler> connectingClients;
    private int nextClientId = 1;

    public Map<Integer, ClientHandler> getConnectingClients() {
        return connectingClients;
    }

    public int getNextClientId() {
        return nextClientId;
    }

    public World getWorld() {
        return world;
    }

    public ServerSocket getSocket() {
        return socket;
    }

    public Server(int port) {
        try {
            world = new World();
            socket = new ServerSocket(port);
            connectingClients = new HashMap<>();
            System.out.println("Server started on port " + port);
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    broadcastCirclesPositions();
                }
            }, 100L, 20L);
            new Thread(this::listen).start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void listen() {
        while (true) {
            try {
                Socket clientSocket = socket.accept();
                System.out.println("Accepted client connection: " + clientSocket);
                ClientHandler handler = new ClientHandler(clientSocket);
                connectingClients.put(handler.getId(), handler);
                new Thread(handler).start();
            } catch (SocketException ignored) {
            } catch (IOException ex) {
                ex.printStackTrace();;
            }
        }
    }

    // TODO: Broadcast position of circles
    private synchronized void broadcastCirclesPositions() {
        if (connectingClients.size() == 0) {
            // Don't waste processing
            return;
        }
        world.getCircles().forEach(c -> {
            int id = c.getId();
            int x = c.getX(), y = c.getY();
            int d = c.getDiameter();
            Color color = c.getColor();
            int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
            // send command to client, "render x y d r g b"
            for (Map.Entry<Integer, ClientHandler> entry : connectingClients.entrySet()) {
                entry.getValue().sendMessage("render " + String.format(
                        "%d %d %d %d %d %d %d",
                        id, x, y, d, r, g, b));
            }
        });
    }

    private synchronized void broadcastRemoveCircle(Circle circle) {
        if (connectingClients.size() == 0) {
            // Don't waste processing
            return;
        }
        for (Map.Entry<Integer, ClientHandler> entry : connectingClients.entrySet()) {
            entry.getValue().sendMessage("kill " + circle.getId());
        }
    }

    private synchronized void updateWorldCorners() {
        if (connectingClients.size() == 0) {
            world.setMinCorner(0, 0);
            world.setMaxCorner(0, 0);
            return;
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = 0, maxY = 0;
        for(Map.Entry<Integer, ClientHandler> entry : connectingClients.entrySet()) {
            if (entry.getValue().getMinCorner().x < minX) {
                minX = entry.getValue().getMinCorner().x;
            }
            if (entry.getValue().getMinCorner().y < minY) {
                minY = entry.getValue().getMinCorner().y;
            }
            if (entry.getValue().getMaxCorner().x > maxX) {
                maxX = entry.getValue().getMaxCorner().x;
            }
            if (entry.getValue().getMaxCorner().y > maxY) {
                maxY = entry.getValue().getMaxCorner().y;
            }
        }
        world.setMinCorner(minX, minY);
        world.setMaxCorner(maxX, maxY);
        System.out.println("World Boundary: " + world.getMinCorner() + ", " + world.getMaxCorner());
    }

    private synchronized void removeClient(int id) {
        ClientHandler handler = connectingClients.get(id);
        if (handler != null) {
            connectingClients.remove(id);
        }
        updateWorldCorners();
    }

    private class ClientHandler implements Runnable {

        private int minX = 0, minY = 0, maxX = 0, maxY = 0;
        private Socket clientSocket;
        private BufferedReader reader;
        private PrintWriter writer;
        private int id;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
                id = nextClientId++;
                System.out.println("Assigned ID " + id + " to client " + clientSocket);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("Client " + getId() + ": " + message);
                    handleInboundMessage(message);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    reader.close();
                    writer.close();
                    clientSocket.close();
                    removeClient(getId());
                    System.out.println("Client " + clientSocket + " disconnected");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void sendMessage(String message) {
            writer.println(message);
        }

        public int getId() {
            return this.id;
        }

        public Point getMinCorner() {
            return new Point(minX, minY);
        }

        public Point getMaxCorner() {
            return new Point(maxX, maxY);
        }

        private void handleInboundMessage(String message) {
            // Since we are using Strings to message between client-server, we need to address a standard for messaging.

            List<String> tokens = Arrays.stream(message.split(" ")).toList();
            if (tokens.size() == 0) return;

            String command = tokens.get(0);

            if (command.equals("spawn")) {
                // ex. "spawn 100 100" -> spawn ball at 100,100

                if (tokens.size() != 3) return;
                int x = Integer.parseInt(tokens.get(1));
                int y = Integer.parseInt(tokens.get(2));
                invokeSpawnBall(x, y);
            }

            if (command.equals("remove")) {
                if (tokens.size() != 1) return;
                invokeRemoveBall();
            }

            if (command.equals("relocate")) {
                // ex. "relocate 100 100 700 400" -> client has relocated to corner(100, 100) going to corner(700, 400)
                // the above is a sample case for window size of 600x300

                if (tokens.size() != 5) return;
                int minX = Integer.parseInt(tokens.get(1));
                int minY = Integer.parseInt(tokens.get(2));
                int maxX = Integer.parseInt(tokens.get(3));
                int maxY = Integer.parseInt(tokens.get(4));
                invokeRelocateWindow(minX, minY, maxX, maxY);
            }
        }

        private void invokeSpawnBall(int x, int y) {
            world.newCircle(x, y);
            System.out.println("Spawned new circle at " + x + "," + y);
        }

        private void invokeRemoveBall() {
            Circle circle = world.removeCircle();
            if (circle != null) {
                broadcastRemoveCircle(circle);
                System.out.println("Removed circle " + circle);
            }
        }

        private void invokeRelocateWindow(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;

            // Minimum must be set
            if (this.minX < 0) {
                this.minX = 0;
            }
            if (this.minY < 0) {
                this.minY = 0;
            }
            updateWorldCorners();
        }

    }

    public void shutdown() {
        connectingClients.clear();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server(12345);
    }

}
