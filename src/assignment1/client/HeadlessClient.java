package assignment1.client;

public class HeadlessClient extends Client {

    public HeadlessClient(String host, int port) {
        super(host, port);
    }

    @Override
    public void handleInboundMessage(String message) {
        return; // skip inbound messages
    }

    public void sendSpawnCommand(int x, int y) {
        sendMessage("spawn " + String.format("%d %d",
                x, y));
    }

    public void sendRelocateCommand(int x0, int y0, int x1, int y1) {
        sendMessage("relocate " + String.format("%d %d %d %d",
                x0, y0, x1, y1));
    }

    public void sendKillCommand() {
        // removes the first circle found in world
        sendMessage("remove");
    }

}
