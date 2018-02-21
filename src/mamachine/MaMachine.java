/*
 */
package mamachine;

import java.io.IOException;

/**
 *
 * @author maria
 */
public class MaMachine {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        new Server(6363,1,2).begin();
        new Client().begin();
    }
    
}
