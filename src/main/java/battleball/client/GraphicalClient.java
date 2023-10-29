package battleball.client;

import battleball.server.Circle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

public class GraphicalClient extends Client {

    public JFrame frame;
    private JTextField amountField;
    private JInternalFrame internalFrame;
    private BallCanvas ballCanvas;

    private HashMap<Integer, Circle> circles = new HashMap<>();

    public GraphicalClient(String host, int port) {
        super(host, port);
        frame = new JFrame();

        // JFrame
        frame.setBounds(100, 100, 700, 300);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.setVisible(true);

        // JPanel
        JPanel panel = new JPanel();
        panel.setVisible(true);
        JLabel Label = new JLabel("Amount of Ball");
        panel.add(Label);
        amountField = new JTextField();
        panel.add(amountField);
        amountField.setColumns(15);

        frame.getContentPane().add(panel, BorderLayout.PAGE_START);

        // Internal Frame
        internalFrame = new JInternalFrame("Battle ball"); // assign internalFrame here
        internalFrame.setBounds(10, 55, 700, 200);
        frame.getContentPane().add(internalFrame);
        internalFrame.getContentPane().setLayout(null);
        internalFrame.setVisible(true);

        ballCanvas = new BallCanvas();
        ballCanvas.setBounds(0, 0, 700, 200);
        internalFrame.add(ballCanvas);

        registerWindowMoveListener();
        registerCanvasClickListener();
        sendRelocateCommand();
        new java.util.Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                amountField.setText(Integer.toString(circles.size()));
                ballCanvas.repaint();
            }
        }, 100L, 20L);

    }

    private class BallCanvas extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            for (Circle circle : circles.values()) {
                if (circle.getX() < GraphicalClient.this.frame.getX() + getX() ||
                        circle.getY() < GraphicalClient.this.frame.getY() + getY() ||
                        circle.getX() > GraphicalClient.this.frame.getX() + getWidth() + getX() ||
                        circle.getY() > GraphicalClient.this.frame.getY() + getHeight() + getY()) continue;
                g2d.setColor(circle.getColor());
                g2d.drawString("Test", 0, 0);
                g2d.fillOval(circle.getX() - GraphicalClient.this.frame.getX() + getX(), circle.getY() - GraphicalClient.this.frame.getY() + getY(),
                        circle.getDiameter(), circle.getDiameter());
            }
        }
    }

    public void handleInboundMessage(String message) {
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

    private void sendRelocateCommand() {
        sendMessage("relocate " + String.format("%d %d %d %d",
                frame.getX() + ballCanvas.getX(),
                frame.getY() + ballCanvas.getY(),
                frame.getX() + ballCanvas.getX() + ballCanvas.getWidth(),
                frame.getY() + ballCanvas.getY() + ballCanvas.getHeight()
        ));
    }

    private void sendSpawnCommand(int x, int y) {
        sendMessage("spawn " + String.format("%d %d",
                x, y));
    }

    private void registerWindowMoveListener() {
        frame.addComponentListener(new ComponentAdapter() {
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
        new GraphicalClient("localhost", 12345);
    }

}
