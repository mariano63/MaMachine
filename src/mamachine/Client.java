/*
 */
package mamachine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import mamachine.MaLog.LEVELS;

/**
 *
 * @author maria
 */
public class Client extends ClientMsg{

    /**
     * dati classe
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
     
    /**
     * Classe Thread client Loop infinito, se il Server non risponde attence
     * qualche secondo e riprova Se il Server spedisce un comando con u id
     * diverso dal client viene generato un messaggio su coda di CLIENT_STATUS
     */
    private class ThClient implements Runnable {

        private final int SECONDI_ATTESA_TRA_2_LISTENING = 3;//Secondi riconnessione

        public ThClient() {
            //Inizializza messaggio default
//            coda.add(Proto.COMMANDS.CLIENT_STATUS.name() + ", Port:" + port + " idClient:" + idClient);
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try (Socket cs = new Socket(IP, port); DataInputStream dis = new DataInputStream(cs.getInputStream())) {
                    //read from Server
                    ricezioneDaServer(dis.readUTF());
                    //write to Server
                    try (DataOutputStream dos = new DataOutputStream(cs.getOutputStream())) {
                        dos.writeUTF(coda.poll().toString());
                    }
                } catch (IOException ex) {
                    if (ex instanceof ConnectException) {
                        l.echo(LEVELS.INFO, "Server not found!");
                    } else {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    Thread.sleep(SECONDI_ATTESA_TRA_2_LISTENING * 1000);
                } catch (InterruptedException ex) {
                    l.echo(LEVELS.INFO, "Client interrupted!");
                    return;
                }
            }
            l.echo(LEVELS.INFO, "Thread Client[] terminated!");
        }
    }

    public Client begin() {
        executor.execute(new ThClient());
        return this;
    }

    public void end() {
        l.echo(MaLog.LEVELS.VERBOSE, "Performing some shutdown cleanup...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        l.echo(MaLog.LEVELS.VERBOSE, "Done cleaning");
    }

    public boolean isTerminated() {
        return executor.isTerminated();
    }
    
    public Client() {
        this("127.0.0.1", 6363, 1);
    }
    public Client(String ip, Integer port, Integer idClient) {
            this.IP = ip;
            this.port = port;
            this.idClient = idClient;
    }

    static public void main(String[] args) throws InterruptedException {
        Client c = new Client("127.0.0.1", 6565, 1).begin();
        while (!c.isTerminated()) {
            Thread.sleep(15000);
//            Client.getInstance().end();
        }
    }
}
