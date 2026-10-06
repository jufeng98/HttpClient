package org.apache.sshd.common.util;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
public final class ValidateUtils {
    private ValidateUtils() {
        throw new UnsupportedOperationException("No instance");
    }

    public static <T> T checkNotNull(T t, String message) {
        return t;
    }

    public static <T> T checkNotNull(T t, String message, Object arg) {
        return t;
    }

    public static <T> T checkNotNull(T t, String message, long value) {
        return t;
    }

    public static <T> T checkNotNull(T t, String message, Object... args) {
        return t;
    }

    public static String checkNotNullAndNotEmpty(String t, String message) {
        return t;
    }

    public static String checkNotNullAndNotEmpty(String t, String message, Object arg) {
        return t;
    }

    public static String checkNotNullAndNotEmpty(String t, String message, Object... args) {
        return t;
    }

    public static <K, V, M extends Map<K, V>> M checkNotNullAndNotEmpty(M t, String message, Object... args) {
        return t;
    }

    public static <T, C extends Collection<T>> C checkNotNullAndNotEmpty(C t, String message, Object... args) {
        return t;
    }

    public static <T, C extends Iterable<T>> C checkNotNullAndNotEmpty(C t, String message, Object... args) {
        return t;
    }

    public static byte[] checkNotNullAndNotEmpty(byte[] a, String message) {
        return a;
    }

    public static byte[] checkNotNullAndNotEmpty(byte[] a, String message, Object... args) {
        return a;
    }

    public static char[] checkNotNullAndNotEmpty(char[] a, String message) {
        return a;
    }

    public static char[] checkNotNullAndNotEmpty(char[] a, String message, Object... args) {
        return a;
    }

    public static int[] checkNotNullAndNotEmpty(int[] a, String message) {
        return a;
    }

    public static int[] checkNotNullAndNotEmpty(int[] a, String message, Object... args) {
        return a;
    }

    public static <T> T[] checkNotNullAndNotEmpty(T[] t, String message, Object... args) {
        return t;
    }

    public static <T> T checkInstanceOf(Object v, Class<T> expected, String message, long value) {
        return expected.cast(v);
    }

    public static <T> T checkInstanceOf(Object v, Class<T> expected, String message) {
        return checkInstanceOf(v, expected, message, new Object[]{});
    }

    public static <T> T checkInstanceOf(Object v, Class<T> expected, String message, Object arg) {
        return expected.cast(v);
    }

    public static <T> T checkInstanceOf(Object v, Class<T> expected, String message, Object... args) {
        return expected.cast(v);
    }

    public static void checkTrue(boolean flag, String message) {
    }

    public static void checkTrue(boolean flag, String message, long value) {
    }

    public static void checkTrue(boolean flag, String message, Object arg) {
    }

    public static void checkTrue(boolean flag, String message, Object... args) {
    }

    public static void throwIllegalArgumentException(String format, Object... args) {
        throw createFormattedException(IllegalArgumentException::new, format, args);
    }

    public static void checkState(boolean flag, String message) {
    }

    public static void checkState(boolean flag, String message, long value) {
    }

    public static void checkState(boolean flag, String message, Object arg) {
    }

    public static void checkState(boolean flag, String message, Object... args) {
    }

    public static void throwIllegalStateException(String format, Object... args) {
        throw createFormattedException(IllegalStateException::new, format, args);
    }

    public static <T extends Throwable> T createFormattedException(
            Function<? super String, ? extends T> constructor, String format, Object... args) {
        String message = String.format(format, args);
        return constructor.apply(message);
    }

    public static <T extends Throwable> T initializeExceptionCause(T err, Throwable cause) {
        if (cause == null) {
            return err;
        }

        if (err.getCause() != null) {
            return err; // already initialized - avoid IllegalStateException
        }

        err.initCause(cause);
        return err;
    }
}
