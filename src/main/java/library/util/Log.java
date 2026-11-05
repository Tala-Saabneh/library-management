package library.util;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Log {
private static final boolean DEBUG = Boolean.getBoolean("library.debug");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private Log() {
    }

    public static void info(String message) {
    print("INFO ", message);
    }

    public static void debug(String message) 
    {
        if (DEBUG)
        {
            print("DEBUG", message);
        }
    }
    private static void print(String level,String message)
    {
        System.out.println(LocalTime.now().format(TIME) + " [" + Thread.currentThread().getName()+ "] " + level + " " + message);
    }
}
