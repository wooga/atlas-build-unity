package wooga.gradle.build.unity.internal

import java.util.concurrent.Callable

class PropertyUtils {
    static String convertToString(Object value) {
        if (!value) {
            return null
        }

        if (value instanceof Callable) {
            value = ((Callable) value).call()
        }

        value.toString()
    }

}
