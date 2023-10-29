package battleball.server;

import java.awt.*;
import java.util.Random;

public class Circle {

    private static int nextId = 1;
    private int id;
    private int x, y;
    private Color color;
    private int diameter;
    private int velocityX;
    private int velocityY;

    public Circle()
    {
        this(0, 0, 10, Color.blue);
    }

    public Circle(int x, int y, int d, Color c)
    {
        this.id = nextId++;
        this.x = x;
        this.y = y;
        this.diameter = d;
        this.color = c;
        int min = 5;
        int max = 25;
        this.velocityX = new Random().nextInt((max-min)+1) + min;
        this.velocityY = new Random().nextInt((max-min)+1) + min;
    }

    public Circle(int x, int y, int d)
    {
        this(x, y, d, Color.blue);
    }

    public Circle(int x, int y)
    {
        this(x, y, 10, Color.blue);
    }

    public int getId() {
        return id;
    }

    public void setColor(Color c) {
        color = c;
    }

    public Color getColor() {
        return color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDiameter() {
        return diameter;
    }

    public int getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(int velocityX) {
        this.velocityX = velocityX;
    }

    public int getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(int velocityY) {
        this.velocityY = velocityY;
    }

    public void relocate(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
