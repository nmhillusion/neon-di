package app.netlify.nmhillusion.ground_injection.util.log;

import java.util.logging.Logger;

/**
 * date: 2022-02-02
 * <p>
 * created-by: MINGUY1
 */

public class PiLogger {
    private final Logger logger;

    public PiLogger(Class<?> clazzToLog) {
        this.logger = Logger.getLogger(clazzToLog.getName());
    }

    public void info(Object logMessage) {
        logger.info(String.valueOf(logMessage));
    }

    public void log(Object logMessage) {
        logger.info(String.valueOf(logMessage));
    }

    public void warn(Object logMessage) {
        logger.info(String.valueOf(logMessage));
    }

    public void error(Object logMessage) {
        logger.info(String.valueOf(logMessage));
    }
}
