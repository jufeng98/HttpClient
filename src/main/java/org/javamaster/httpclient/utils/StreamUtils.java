package org.javamaster.httpclient.utils;

import com.alibaba.dubbo.common.utils.Assert;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {

    /**
     * The default buffer size used why copying bytes.
     */
    public static final int BUFFER_SIZE = 4096;

    /**
     * Copy the contents of the given InputStream into a new byte array.
     * Leaves the stream open when done.
     * @param in the stream to copy from (may be {@code null} or empty)
     * @return the new byte array that has been copied to (possibly empty)
     * @throws IOException in case of I/O errors
     */
    public static byte[] copyToByteArray(@Nullable InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        copy(in, out);
        return out.toByteArray();
    }


    /**
     * Copy the contents of the given InputStream to the given OutputStream.
     * Leaves both streams open when done.
     *
     * @param in  the InputStream to copy from
     * @param out the OutputStream to copy to
     * @throws IOException in case of I/O errors
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        Assert.notNull(in, "No InputStream specified");
        Assert.notNull(out, "No OutputStream specified");

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
    }

}
