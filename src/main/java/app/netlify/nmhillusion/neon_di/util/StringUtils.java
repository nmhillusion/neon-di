package app.netlify.nmhillusion.neon_di.util;

/**
 * date: 2022-02-02
 * <p>
 * created-by: MINGUY1
 */

public class StringUtils {
    public static boolean isBlank(Object data) {
        return null == data
                || (data instanceof String
                && 0 == String.valueOf(data).trim().length());
    }

    public static String getFirstNotBlank(Object... args) {
        for (Object argv : args) {
            if (!isBlank(argv)) {
                return String.valueOf(argv);
            }
        }
        return "";
    }
}
