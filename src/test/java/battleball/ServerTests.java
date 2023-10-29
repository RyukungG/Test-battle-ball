package battleball;

import battleball.client.HeadlessClient;
import battleball.server.Server;
import battleball.server.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
