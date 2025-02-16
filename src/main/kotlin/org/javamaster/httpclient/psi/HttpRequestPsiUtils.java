package org.javamaster.httpclient.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HttpRequestPsiUtils {
    public static @Nullable PsiElement getNextSiblingByType(@Nullable PsiElement element, @NotNull IElementType type, boolean strict) {
        PsiElement sibling;
        sibling = element != null && strict ? element.getNextSibling() : element;
        while (sibling != null && !isOfType(sibling, type)) {
            sibling = sibling.getNextSibling();
        }
        return sibling;
    }

    public static HttpRequestBlock @NotNull [] getRequestBlocks(@NotNull PsiFile file) {
        HttpRequestBlock[] blocks = PsiTreeUtil.getChildrenOfType(file, HttpRequestBlock.class);
        return blocks == null ? new HttpRequestBlock[0] : blocks;
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
}
