/*
 */
package mamachine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author maria
 */
public class Server extends ServerMsg{

    private int port;
    private ServerSocket ss;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    class thServer implements Runnable {

        public thServer() throws IOException {
            ss = new ServerSocket(port);
        }

        @Override
        public void run() {
            l.echo(MaLog.LEVELS.VERBOSE, "Server start...");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    l.echo(MaLog.LEVELS.VERBOSE, "Wait client...");
                    ss.setSoTimeout(15000);
                    Socket socket = ss.accept();
                    try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                        //write to Client
                        jsonServer = coda.poll();
                        if (jsonServer == null) {
                            jsonServer = new JSONObject();
                            setServerMsgDefault(jsonServer);
                        }
                        dos.writeUTF(jsonServer.toString());
                        //read from Client and manage the queue
                        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                            ricezioneDaClient((String) dis.readUTF());
                        }
                    }
                } catch (IOException ex) {
                    if (ex instanceof SocketTimeoutException) {
                        l.echo(MaLog.LEVELS.VERBOSE, "Server accept() timeout!");
                    } else {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            l.echo(MaLog.LEVELS.INFO, "Server interrupted!");
            try {
                ss.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Server begin() {
        try {
            executor.execute(new thServer());
        } catch (IOException ex) {
            l.echo(MaLog.LEVELS.ERROR, "Server impossibilitato allo start!\n" + ex.getMessage());
        }
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

    public int getPort() {
        return port;
    }
    
    public Server(int port, int... clients) {
        super();
        this.port = port;
        this.lstClients.clear();
        for (int cl : clients) {
            this.lstClients.add(cl);
        }
        l.echo(MaLog.LEVELS.INFO, String.format(
                "Create Server on port:%d, listening Clients:%s", port, lstClients.toString()));
        this.idServito = lstClients.get(0);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Server s = new Server(6565, 1, 2).begin();
        while (!s.isTerminated()) {
            Thread.sleep(15000);
//            Server.getInstance().end();
        };
    }
}
