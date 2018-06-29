package org.acheron.sample.bluez;

public final class BufferUtils {

    private BufferUtils() {
    }

    public static String dump(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format(" %02X", b));
        }
        return builder.toString();
    }

}
