package assignment1.client;

import assignment1.server.Circle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.List;

public class Client extends JFrame {

    private BufferedReader reader;
    private PrintWriter writer;

    private JTextField amountField;
    private JInternalFrame internalFrame;
    private BallCanvas ballCanvas;

    private HashMap<Integer, Circle> circles = new HashMap<>();

    public Client(String host, int port) {

        // JFrame
        setBounds(100, 100, 700, 300);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        setVisible(true);

        // JPanel
        JPanel panel = new JPanel();
        panel.setVisible(true);
        JLabel Label = new JLabel("Amount of Ball");
        panel.add(Label);
        amountField = new JTextField();
        panel.add(amountField);
        amountField.setColumns(15);

        getContentPane().add(panel, BorderLayout.PAGE_START);

        // Internal Frame
        internalFrame = new JInternalFrame("Battle ball"); // assign internalFrame here
        internalFrame.setBounds(10, 55, 700, 200);
        getContentPane().add(internalFrame);
        internalFrame.getContentPane().setLayout(null);
        internalFrame.setVisible(true);

        ballCanvas = new BallCanvas();
        ballCanvas.setBounds(0, 0, 700, 200);
        internalFrame.add(ballCanvas);

        try {
            Socket socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            registerWindowMoveListener();
            registerCanvasClickListener();

            System.out.println("Connected to server: " + socket);
            sendRelocateCommand();
            new SocketReadingThread().start();

            new java.util.Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    amountField.setText(Integer.toString(circles.size()));
                    ballCanvas.repaint();
                }
            }, 100L, 20L);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class BallCanvas extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            for (Circle circle : circles.values()) {
                if (circle.getX() < Client.this.getX() + getX() ||
                        circle.getY() < Client.this.getY() + getY() ||
                        circle.getX() > Client.this.getX() + getWidth() + getX() ||
                        circle.getY() > Client.this.getY() + getHeight() + getY()) continue;
                g2d.setColor(circle.getColor());
                g2d.drawString("Test", 0, 0);
                g2d.fillOval(circle.getX() - Client.this.getX() + getX(), circle.getY() - Client.this.getY() + getY(),
                        circle.getDiameter(), circle.getDiameter());
            }
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

    private void handleInboundMessage(String message) {
        List<String> tokens = Arrays.stream(message.split(" ")).toList();
        if (tokens.size() == 0) return;

        String command = tokens.get(0);
        if (command.equals("render")) {
            if (tokens.size() != 8) return;
            int id = Integer.parseInt(tokens.get(1));
            int x = Integer.parseInt(tokens.get(2)), y = Integer.parseInt(tokens.get(3)),
                d = Integer.parseInt(tokens.get(4)), r = Integer.parseInt(tokens.get(5)),
                g = Integer.parseInt(tokens.get(6)), b = Integer.parseInt(tokens.get(7));
            if (circles.containsKey(id)) {
                circles.get(id).relocate(x, y);
            } else {
                // Let Circle be data storage here, we will ignore velocity.
                circles.put(id, new Circle(x, y, d, new Color(r, g, b)));
            }
        }

        if (command.equals("kill")) {
            if (tokens.size() != 2) return;
            int id = Integer.parseInt(tokens.get(1));
            if (!circles.containsKey(id)) return;
            circles.remove(id);
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    private void sendRelocateCommand() {
        sendMessage("relocate " + String.format("%d %d %d %d",
                getX() + ballCanvas.getX(),
                getY() + ballCanvas.getY(),
                getX() + ballCanvas.getX() + ballCanvas.getWidth(),
                getY() + ballCanvas.getY() + ballCanvas.getHeight()
        ));
    }

    private void sendSpawnCommand(int x, int y) {
        sendMessage("spawn " + String.format("%d %d",
                x, y));
    }

    private void registerWindowMoveListener() {
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                sendRelocateCommand();
            }
        });
    }

    private void registerCanvasClickListener() {
        ballCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    sendSpawnCommand(e.getXOnScreen(), e.getYOnScreen());
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    sendMessage("remove");
                }
            }
        });
    }

    public static void main(String[] args) {
        new Client("localhost", 12345);
    }

}
