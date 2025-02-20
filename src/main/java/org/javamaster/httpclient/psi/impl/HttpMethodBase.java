package org.javamaster.httpclient.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HttpMethodBase extends HttpCompositeElementImpl implements UserDataHolder {
    private final UserDataHolderBase userDataHolderBase = new UserDataHolderBase();

    public HttpMethodBase(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return userDataHolderBase.getUserData(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        userDataHolderBase.putUserData(key, value);
    }
}
