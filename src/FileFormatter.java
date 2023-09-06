import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import static java.lang.System.err;
import static java.lang.System.out;

public class FileFormatter extends Formatter {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSS");

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append("> [");
        sb.append(dateFormat.format(new Date(record.getMillis())));
        sb.append("] ");
        sb.append(record.getLevel().getName());
        sb.append(": ");
        sb.append(formatMessage(record));
        sb.append(System.getProperty("line.separator"));
        return sb.toString();
    }
}
