package org.javamaster.httpclient.parser;

import com.intellij.lang.*;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.javamaster.httpclient.CookieLanguage;
import org.javamaster.httpclient.psi.CookieTypes;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class CookieParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(CookieLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new CookieAdapter();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new CookieParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return TokenSet.create(CookieTypes.LINE_COMMENT);
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return CookieTypes.Factory.createElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new CookieFile(viewProvider);
    }
}
