/*
 */
package mamachine;

import java.time.Instant;
import java.util.Date;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author maria
 */
public class MaLog {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static enum LEVELS {
        VERBOSE(ANSI_WHITE),
        DATA(ANSI_GREEN),
        INFO(ANSI_CYAN),
        WARNING(ANSI_YELLOW),
        ERROR(ANSI_RED);
        String color;

        private LEVELS(String s) {
            color = s;
        }

        public String getColor() {
            return color;
        }
    };
    private boolean logOn = true;
    private LEVELS level = LEVELS.INFO;
    JTextArea jta = null;
    Class cl;

    public MaLog(Class c) {
        cl = c;
    }

    public MaLog echo(String s) {
        return echo(LEVELS.INFO, s);
    }

    public MaLog echo(LEVELS l, String s) {
        if (!isLogOn()) {
            return this;
        }
        if (getLevel().ordinal() > l.ordinal()) {
            return this;
        }
        final String s1 = cl.getCanonicalName()+" "
                + Date.from(Instant.now())
                + " LEVEL " + l.toString() + ":" 
                + System.getProperty("line.separator")
                + l.getColor() + s + System.getProperty("line.separator")
                + ANSI_RESET;

        if (jta == null) {
            System.out.print(s1);
        } else if (!Thread.currentThread().isInterrupted()) {
            SwingUtilities.invokeLater(() -> {
                if (jta != null) {
                    jta.append(s1);
                }
            });
        }
        return this;
    }

    public MaLog setJta(JTextArea jta) {
        this.jta = jta;
        return this;
    }

    public LEVELS getLevel() {
        return level;
    }

    public MaLog setLevel(LEVELS level) {
        this.level = level;
        return this;
    }

    public void setLogOn(boolean logOn) {
        this.logOn = logOn;
    }

    public boolean isLogOn() {
        return logOn;
    }

}
