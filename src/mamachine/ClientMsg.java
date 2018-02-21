/*
 */
package mamachine;

import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author maria
 */
public class ClientMsg {

    /**
     * variabili e dati
     */
    protected int idClient;             //Id Client
    protected String IP;                  //Indirizzo IP
    protected int port;                   //Numero di porta 

    protected final BlockingQueue<JSONObject> coda = new ArrayBlockingQueue(1);
    JSONObject jsonClient = new JSONObject();
    MaLog l;

    {
        l = new MaLog(getClass()).setLevel(MaLog.LEVELS.VERBOSE);
    }

    static public enum KEYS {
        CMD, PARAM, ID_CLIENT, INSTANT
    }

    /**
     * Ricevuta una stringa da server. Questo metodo creerà un comando ClientMsg
     * che verrà inserito nella coda e spedito poi al Server. Questo metodo si
     * trova esattamente in mezzo alla comunicazione Server-Client
     * Server(receive) -X- Client(send).
     *
     * @param sServerMsg
     */
    public void ricezioneDaServer(String sServerMsg) {
        jsonClient = (JSONObject) JSONValue.parse(sServerMsg);
        /**
         * qui si leggono i valori dal messaggio Server
         */
        long idServito = (Long) jsonClient.get(ServerMsg.KEYS.ID_SERVITO.name());
        String cmd = (String) jsonClient.get(ServerMsg.KEYS.CMD.name());
        String param = (String) jsonClient.get(ServerMsg.KEYS.PARAM.name());
        String date = (String) jsonClient.get(ServerMsg.KEYS.INSTANT.name());
        l.echo(MaLog.LEVELS.DATA, String.format("[From Server]:%s", jsonClient.toString()));
        /**
         * Qui si gestisce il messaggio da spedire al server
         */
        jsonClient.clear();
        setClientMsgDefault(jsonClient);
        //un SERVER_STATUS implica sempre una risposta da parte del Client con un
        //CLIENT_STATUS, anche se non matcha ID.
        //Serve per sapere se il Client è vivo.
        if (COMMANDS.valueOf(cmd) == COMMANDS.SERVER_STATUS) {
            putCmd(COMMANDS.CLIENT_STATUS, jsonClient);
            coda.add(jsonClient);
            return;
        }
        if (idServito != idClient) {
            putCmd(COMMANDS.CLIENT_NACK, jsonClient);
            putParam("**ERR: ID non coincide.", jsonClient);
            coda.add(jsonClient);
            return;
        }
        switch (COMMANDS.valueOf(cmd)) {
            case SERVER_OR_CLIENT_IDLE:
                putCmd(COMMANDS.SERVER_OR_CLIENT_IDLE, jsonClient);
                putParam("**STATUS: Client", jsonClient);
                break;
        }
        coda.add(jsonClient);
    }

    protected final void setClientMsgDefault(JSONObject obj) {
        putCmd(COMMANDS.ERROR_UNKNOWN_COMMAND, obj);
        putParam("@", obj);
        putIdClient(idClient, obj);
        putInstantNow(obj);
    }

    protected void putCmd(COMMANDS cmd, JSONObject jso) {
        jso.put(ClientMsg.KEYS.CMD.name(), cmd.name());
    }

    protected void putParam(String param, JSONObject jso) {
        jso.put(ClientMsg.KEYS.PARAM.name(), param);
    }

    protected void putIdClient(int id, JSONObject jso) {
        jso.put(ClientMsg.KEYS.ID_CLIENT.name(), idClient);
    }

    protected void putInstantNow(JSONObject jso) {
        jso.put(ClientMsg.KEYS.INSTANT.name(), Instant.now().toString());
    }

    /**
     *
     */
    public ClientMsg() {
        this(1);
    }

    /**
     *
     * @param idClient
     */
    public ClientMsg(int idClient) {
        this.idClient = idClient;
    }
}
