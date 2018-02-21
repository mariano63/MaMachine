/*
 */
package mamachine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author maria
 */
public class ServerMsg {

    protected JSONObject jsonServer = new JSONObject();
    protected final List<Integer> lstClients = new ArrayList(Arrays.asList(1, 2));
    protected final BlockingQueue<JSONObject> coda = new ArrayBlockingQueue(1);
    protected int idServito;
    protected MaLog l;
    {
        l = new MaLog(getClass()).setLevel(MaLog.LEVELS.VERBOSE);
    }

    protected static enum KEYS {
        CMD, PARAM, ID_SERVITO,INSTANT
    }

    /**
     * Ricevuta una stringa da Client Questo metodo creerà una classe ServerMsg
     * che verrà inserita nella coda e spedita poi al Client. Questo metodo si
     * trova alla fine della comunicazione. Server(Send) - Client(receive) -X-.
     * E il comando in coda verrà quindi spedito alla prossima richiesta di
     * socket da parte del client. I comandi alla coda possono essere multipli.
     *
     * @param sClientString
     */
    protected void ricezioneDaClient(String sClientString) {
        JSONObject jsonObject = (JSONObject) JSONValue.parse(sClientString);
        l.echo(MaLog.LEVELS.DATA, String.format("[From Client]:%s", jsonObject.toString()));
        /**
         * qui si leggono i valori dal messaggio Client
         */         
        long idServito = (Long) jsonObject.get(ClientMsg.KEYS.ID_CLIENT.name());
        String param = (String) jsonObject.get(ClientMsg.KEYS.PARAM.name());
        String cmd = (String) jsonObject.get(ClientMsg.KEYS.CMD.name());
        String time = (String) jsonObject.get(ClientMsg.KEYS.CMD.name());
        String date = (String) jsonObject.get(ClientMsg.KEYS.INSTANT.name());
        jsonServer.clear();
        //Compila tutti i campi con il default.
        setServerMsgDefault(jsonServer);
        /**
         * In base al COMMANDS del ClientMsg compila campi del ServerMsg, ed esegue vari task.
         */
        switch (COMMANDS.valueOf((String) jsonObject.get(ClientMsg.KEYS.CMD.name()))) {
            case CLIENT_STATUS:
            case CLIENT_NACK:
            case ERROR_UNKNOWN_COMMAND:
                putCmd(COMMANDS.SERVER_OR_CLIENT_IDLE, jsonServer);
                break;
            case CLIENT_ACK:
                //TODO Read from LIST of COMMANDS(Language)
                putCmd(COMMANDS.SERVER_STATUS, jsonServer);
                break;
            default:
        }
        coda.add(jsonServer);
    }
    protected void putCmd(COMMANDS cmd, JSONObject jso){
        jso.put(ServerMsg.KEYS.CMD.name(), cmd.name());        
    }
    protected void putParam(String param, JSONObject jso){
        jso.put(ServerMsg.KEYS.PARAM.name(), param);        
    }
    protected void putIdServito(int id, JSONObject jso){
        jso.put(ServerMsg.KEYS.ID_SERVITO.name(), id);        
    }
    protected void putInstantNow(JSONObject jso){
        jso.put(ServerMsg.KEYS.INSTANT.name(), Instant.now().toString());        
    }
    /**
     * Messaggio di default
     * @param obj 
     */
    protected final void setServerMsgDefault(JSONObject obj){
        putCmd(COMMANDS.SERVER_STATUS, obj);
        putParam("@", obj);
        putIdServito(idServito, obj);
        putInstantNow(obj);
    }
    /**
     *
     */
    public ServerMsg() {
    }

}
