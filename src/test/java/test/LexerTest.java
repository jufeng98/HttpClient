package test;

import com.cool.request.utils.StreamUtils;
import com.intellij.psi.tree.IElementType;
import org.javamaster.httpclient.parser.HttpAdapter;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LexerTest {

    @Test
    public void testLexer() throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("test.http");
        String str = StreamUtils.copyToString(stream, StandardCharsets.UTF_8);

        HttpAdapter httpAdapter = new HttpAdapter();
        httpAdapter.start(str, 0, str.length());

        while (true) {
            httpAdapter.advance();
            IElementType tokenType = httpAdapter.getTokenType();
            System.out.println(tokenType);
            if (tokenType == null) {
                break;
            }
        }
    }
    
}
