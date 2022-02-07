package app.netlify.nmhillusion.ground_injection.util.log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * date: 2022-02-02
 * <p>
 * created-by: MINGUY1
 */

public class LogHelper {
    private static final Map<Class<?>, PiLogger> logFactory = new ConcurrentHashMap<>();

    public static PiLogger getLog(Object thisToLog) {
        final Class<?> classToLog = null == thisToLog ? LogHelper.class : thisToLog.getClass();

        if (logFactory.containsKey(classToLog)) {
            return logFactory.get(classToLog);
        } else {
            final PiLogger piLogger = new PiLogger(classToLog);
            logFactory.put(classToLog, piLogger);
            return piLogger;
        }
    }
}
