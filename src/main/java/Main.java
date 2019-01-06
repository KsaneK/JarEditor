import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(LoggerFormatter.class.getName());
        logger.setUseParentHandlers(false);
        LoggerFormatter formatter = new LoggerFormatter();
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        logger.addHandler(handler);
        logger.info("Starting JAR editor.");
        new JarEditor("Jar Editor");
    }
}
