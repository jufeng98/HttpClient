package org.javamaster.httpclient.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HttpPsiUtils {
    public static @Nullable PsiElement getNextSiblingByType(@Nullable PsiElement element,
                                                            @NotNull IElementType type, boolean strict) {
        PsiElement sibling = element != null && strict ? element.getNextSibling() : element;
        while (sibling != null && !isOfType(sibling, type)) {
            sibling = sibling.getNextSibling();
        }
        return sibling;
    }

    public static @Nullable PsiElement getPrevSiblingByType(@Nullable PsiElement element,
                                                            @NotNull IElementType type, boolean strict) {
        PsiElement sibling = element != null && strict ? element.getPrevSibling() : element;
        while (sibling != null && !isOfType(sibling, type)) {
            sibling = sibling.getPrevSibling();
        }
        return sibling;
    }

    public static boolean isOfType(@NotNull PsiElement element, @NotNull IElementType type) {
        ASTNode node = element.getNode();
        return node != null && node.getElementType() == type;
    }

    public static boolean isOfTypes(@NotNull PsiElement element, @NotNull TokenSet tokenSet) {
        for (IElementType type : tokenSet.getTypes()) {
            boolean ofType = isOfType(element, type);
            if (ofType) {
                return true;
            }
        }

        return false;
    }

    public static List<TextRange> collectVariablesRangesInMessageBody(String body) {
        List<TextRange> result = new ArrayList<>();

        int closeBraceIndex;
        for (int index = 0; index < body.length(); index = closeBraceIndex + 2) {
            int openBraceIndex = body.indexOf("{{", index);
            closeBraceIndex = body.indexOf("}}", openBraceIndex);
            if (openBraceIndex < 0 || closeBraceIndex < 0) {
                break;
            }

            TextRange range = TextRange.create(openBraceIndex, closeBraceIndex + 2);
            if (!range.isEmpty() && !StringUtil.isEmptyOrSpaces(body.substring(openBraceIndex + 2, closeBraceIndex))) {
                result.add(range);
            }
        }

        return result;
    }
}
