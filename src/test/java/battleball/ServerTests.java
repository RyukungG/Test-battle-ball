package battleball;

import battleball.client.HeadlessClient;
import battleball.server.Server;
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

}
