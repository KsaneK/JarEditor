import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class LoggerFormatter extends Formatter {
    private static final DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss.SSS");
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();
        if (record.getLevel() == Level.INFO) builder.append(ANSI_GREEN);
        else builder.append(ANSI_RED);
        builder.append(String.format("[%c] ", record.getLevel().getName().charAt(0)));
        builder.append(ANSI_BLUE);
        builder.append(dateFormat.format(new Date(record.getMillis())));
        builder.append(ANSI_PURPLE);
        builder.append(String.format(" [%s.%s] ", record.getSourceClassName(), record.getSourceMethodName()));
        builder.append(ANSI_WHITE);
        builder.append(" ");
        builder.append(formatMessage(record));
        builder.append("\n");
        return builder.toString();
    }
}
