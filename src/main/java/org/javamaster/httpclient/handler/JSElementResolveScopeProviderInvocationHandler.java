package org.javamaster.httpclient.handler;

import com.google.common.collect.Lists;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import one.util.streamex.StreamEx;
import org.javamaster.httpclient.jsPlugin.support.HttpRequestHandlerApiDefinitionFilesHolder;
import org.javamaster.httpclient.jsPlugin.support.JavaScript;
import org.javamaster.httpclient.psi.HttpResponseScript;
import org.javamaster.httpclient.psi.HttpScriptBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author yudong
 */
public class JSElementResolveScopeProviderInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if (method.getReturnType() != GlobalSearchScope.class) {
            return method.invoke(o, objects);
        }

        return getElementResolveScope((PsiElement) objects[0]);
    }

    public @Nullable GlobalSearchScope getElementResolveScope(PsiElement element) {
        Project project = element.getProject();
        PsiLanguageInjectionHost injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(element);
        if (!(injectionHost instanceof HttpScriptBody)) {
            return null;
        }

        List<VirtualFile> virtualFiles = Lists.newArrayList();

        HttpRequestHandlerApiDefinitionFilesHolder filesHolder = HttpRequestHandlerApiDefinitionFilesHolder.INSTANCE;

        List<VirtualFile> vFiles;
        if (insideResponseHandler(element)) {
            vFiles = getLibraryFiles(
                    filesHolder.getCommonLibraryFilePointer(),
                    filesHolder.getResponseLibraryFilePointer(),
                    filesHolder.getDynamicVariablesFilePointer()
            );

        } else {
            vFiles = getLibraryFiles(
                    filesHolder.getCommonLibraryFilePointer(),
                    filesHolder.getCryptoLibraryFilePointer(),
                    filesHolder.getPreRequestLibraryFilePointer(),
                    filesHolder.getDynamicVariablesFilePointer()
            );
        }

        virtualFiles.addAll(vFiles);

        virtualFiles.addAll(JavaScript.INSTANCE.getCoreJsStubLib());

        return GlobalSearchScope.filesScope(project, virtualFiles);
    }

    private static List<VirtualFile> getLibraryFiles(VirtualFilePointer... filePointers) {
        return StreamEx.of(filePointers).map(VirtualFilePointer::getFile).nonNull().toList();
    }

    private static boolean insideResponseHandler(@NotNull PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, HttpResponseScript.class) != null;
    }

}
