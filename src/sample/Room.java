package sample;

public class Room extends Thread {
    private ClientConnectionHandler player1;
    private ClientConnectionHandler player2;

    public Room(ClientConnectionHandler player1, ClientConnectionHandler player2) {
        super();
        this.player1 = player1;
        this.player2 = player2;
        this.start();
    }

    public void run() {
        player1.start();
        player2.start();
        sendAll("INIT");
    }

    private void sendAll(Object object) {
        player1.send(object);
        player2.send(object);
    }
}
