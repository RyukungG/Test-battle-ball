package battleball;

import battleball.client.HeadlessClient;
import battleball.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Point;

public class ServerTests {

    Server server;

    final String HOST = "localhost";
    final int PORT = 12346;

    @BeforeEach
    void setUp() {
        this.server = new Server(PORT);
        System.out.println(this.server);
    }

    @AfterEach
    void tearDown() {
        this.server.shutdown();
        this.server = null;
    }

    @Test
    public void testConnection() {
        Assertions.assertEquals(0, server.getConnectingClients().size());
        HeadlessClient client = new HeadlessClient(HOST, PORT);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } // Not a good solution, but it works for now.

        Assertions.assertEquals(1, server.getConnectingClients().size());
        client.stop();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(0, server.getConnectingClients().size());
    }

    @Test
    public void testRelocateOneWindow() {
        HeadlessClient client = new HeadlessClient(HOST, PORT);
        client.sendRelocateCommand(0,0,100,100);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        {
            Assertions.assertEquals(0,server.getWorld().getMinCorner().x);
            Assertions.assertEquals(0,server.getWorld().getMinCorner().y);
            Assertions.assertEquals(100,server.getWorld().getMaxCorner().x);
            Assertions.assertEquals(100,server.getWorld().getMaxCorner().y);
        }

    }

    @Test
    public void testRelocateNWindow() {
        HeadlessClient client = new HeadlessClient(HOST, PORT);
        HeadlessClient client2 = new HeadlessClient(HOST, PORT);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        client.sendRelocateCommand(0, 0, 100, 100);

        client2.sendRelocateCommand(100, 100, 200, 200);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(new Point(0, 0), server.getWorld().getMinCorner());
        Assertions.assertEquals(new Point(200, 200), server.getWorld().getMaxCorner());
    }

    // TODO: Test case for one ball. (Demo)

    @Test
    public void testSpawnNBall() {
        final int n = 10;
        HeadlessClient client = new HeadlessClient(HOST, PORT);
        int initialCircleCount = server.getWorld().getCircles().size();

        // Spawn n balls using the client
        for (int i = 0; i < n; i++) {
            client.sendSpawnCommand(10 + i, 10 + i); // Adjust coordinates as needed
            try {
                Thread.sleep(500); // Short delay to ensure messages are processed in order. Adjust if needed.
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // After all spawn commands, check the count of circles in the server's world
        int finalCircleCount = server.getWorld().getCircles().size();
        Assertions.assertEquals(initialCircleCount + n, finalCircleCount, "The server should have " + n + " more circles after spawning.");
        client.stop();
    }

    @Test
    public void testRemoveOneBall()
    {
        HeadlessClient client = new HeadlessClient(HOST, PORT);
        client.sendRelocateCommand(0,0,500,500);

        client.sendSpawnCommand(250,250);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(1, server.getWorld().getCircles().size());

        client.sendKillCommand();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(0, server.getWorld().getCircles().size());

        client.stop();
    }
    
    @Test
    public void testRemoveNBall() {
        final int spawn = 10;
        final int remove = 5;

        HeadlessClient client = new HeadlessClient(HOST, PORT);

        for (int i = 0; i<spawn; i++) {
            client.sendSpawnCommand(10 * i, 10 * i);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Assertions.assertEquals(spawn, server.getWorld().getCircles().size());

        for (int i = 0; i<remove; i++) {
            client.sendKillCommand();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Assertions.assertEquals(5, server.getWorld().getCircles().size());
    }
}
