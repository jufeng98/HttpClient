package test;

import com.cool.request.utils.StreamUtils;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.mock.MockApplication;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.impl.ProgressManagerImpl;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.tree.IElementType;
import org.javamaster.httpclient.HttpLanguage;
import org.javamaster.httpclient.parser.HttpAdapter;
import org.javamaster.httpclient.parser.HttpParserDefinition;
import org.javamaster.httpclient.psi.HttpMyJsonValue;
import org.javamaster.httpclient.psi.impl.TextVariableLazyFileElement;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LexerTest {

    @Test
    public void testLexer() throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("test.http");
        String str = StreamUtils.copyToString(stream, StandardCharsets.UTF_8);

        HttpAdapter httpAdapter = new HttpAdapter();
        httpAdapter.start(str);

        while (true) {
            IElementType tokenType = httpAdapter.getTokenType();
            System.out.println(tokenType);

            httpAdapter.advance();
            if (tokenType == null) {
                break;
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void initApplication() {
        MockApplication application = new MockApplication(Disposer.newDisposable()) {
            {
                registerService(ProgressManager.class, ProgressManagerImpl.class);
            }
        };
        ApplicationManager.setApplication(application);
        LanguageParserDefinitions.INSTANCE.addExplicitExtension(HttpLanguage.INSTANCE, new HttpParserDefinition());
    }

    @Test
    public void testLexer1() {
        initApplication();

        String str = "this is a {{$requestName}} and {{age}} good.";

        HttpMyJsonValue element = TextVariableLazyFileElement.Companion.parse(str);
        System.out.println(element.getVariableList());

        str = "{{requestName}}";
        element = TextVariableLazyFileElement.Companion.parse(str);
        System.out.println(element.getVariableList());
    }

}
