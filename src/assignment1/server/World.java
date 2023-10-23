package assignment1.server;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class World {

    private int minX = 0, minY = 0, maxX = 0, maxY = 0;
    private ArrayList<Circle> circles = new ArrayList<>();
    private Random random = new Random();

    public World() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 100L, 50L); // start update timer
    }

    public Point getMinCorner() {
        return new Point(minX, minY);
    }

    public Point getMaxCorner() {
        return new Point(maxX, maxY);
    }

    public void setMinCorner(int x, int y) {
        minX = x;
        minY = y;
        if (minX < 0) {
            minX = 0;
        }
        if (minY < 0) {
            minY = 0;
        }
    }

    public void setMaxCorner(int x, int y) {
        // Don't need to limit, I think.
        maxX = x;
        maxY = y;
    }

    public void newCircle(int x, int y) {
        float r = random.nextFloat();
        float g = random.nextFloat();
        float b = random.nextFloat();
        int max = 25;
        int min = 5;
        int size = random.nextInt(max - min) + min;
        Color color = new Color(r,g,b);

        Circle circle = new Circle(x, y, size, color);
        circles.add(circle);
        System.out.println("Added ball");
    }

    public Circle removeCircle() {
        if (circles.size() <= 0) {
            System.out.println("Stack underflow");
            return null;
        }
        System.out.println("Removed ball");
        return circles.remove(0);
    }

    public ArrayList<Circle> getCircles() {
        return circles;
    }

    public void update() {
        if (circles.size() <= 0) {
            return;
        }
        // TODO: Fix this.
        for (Circle circle : circles) {
            int x = circle.getX();
            int y = circle.getY();
            if (x < this.getMinCorner().x) {
                circle.setVelocityX(Math.abs(circle.getVelocityX()));
                x = this.getMinCorner().x;
            } else if (x > this.getMaxCorner().x) {
                circle.setVelocityX(-Math.abs(circle.getVelocityX()));
                x = this.getMaxCorner().x;
            }
            if (y < this.getMinCorner().y) {
                circle.setVelocityY(Math.abs(circle.getVelocityY()));
                y = this.getMinCorner().y;
            } else if (y > this.getMaxCorner().y) {
                circle.setVelocityY(-Math.abs(circle.getVelocityY()));
                y = this.getMaxCorner().y;
            }
            circle.relocate(x + circle.getVelocityX(), y + circle.getVelocityY());
        }
    }

}
