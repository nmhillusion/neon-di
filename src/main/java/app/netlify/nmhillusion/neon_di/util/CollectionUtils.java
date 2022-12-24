package app.netlify.nmhillusion.neon_di.util;

import java.util.Collection;
import java.util.Map;

/**
 * date: 2022-02-04
 * <p>
 * created-by: MINGUY1
 */

public class CollectionUtils {
    public static boolean isEmpty(Collection<?> collection) {
        return null == collection || collection.isEmpty();
    }

    public static boolean isEmpty(Object[] collection) {
        return null == collection || 0 == collection.length;
    }

    public static boolean isEmpty(Map<?, ?> collection) {
        return null == collection || collection.isEmpty();
    }
}
