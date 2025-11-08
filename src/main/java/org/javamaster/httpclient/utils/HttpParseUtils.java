package org.javamaster.httpclient.utils;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.impl.PsiBuilderFactoryImpl;
import com.intellij.mock.MockApplication;
import com.intellij.mock.MockProject;
import com.intellij.mock.MockPsiManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.impl.ProgressManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.DummyHolderViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.javamaster.httpclient.HttpLanguage;
import org.javamaster.httpclient.parser.HttpParserDefinition;
import org.jetbrains.annotations.NotNull;

public class HttpParseUtils {
    private static final Disposable disposable = Disposer.newDisposable();
    @SuppressWarnings("UnstableApiUsage")
    private static final Project project = new MockProject(null, disposable);
    private static final MockApplication application = new MockApplication(disposable);
    private static final HttpParserDefinition parserDefinition = new HttpParserDefinition();
    private static final MockPsiManager psiManager = new MockPsiManager(project);
    private static final DummyHolderViewProvider viewProvider = new DummyHolderViewProvider(psiManager) {
        @Override
        public @NotNull Language getBaseLanguage() {
            return HttpLanguage.INSTANCE;
        }

        @Override
        public String toString() {
            return "";
        }
    };

    static {
        application.registerService(PsiBuilderFactory.class, PsiBuilderFactoryImpl.class);
        application.registerService(ProgressManager.class, ProgressManagerImpl.class);

        ApplicationManager.setApplication(application, disposable);

        LanguageParserDefinitions.INSTANCE.addExplicitExtension(HttpLanguage.INSTANCE, parserDefinition);
    }

    public static PsiElement parse(String str) {
        IElementType type = new IFileElementType(HttpLanguage.INSTANCE);

        FileElement holder = new FileElement(type, str);

        holder.setPsi(parserDefinition.createFile(viewProvider));

        TreeElement firstChildNode = holder.getFirstChildNode();

        return firstChildNode.getPsi();
    }
}
