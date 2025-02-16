package org.javamaster.httpclient.index.support

import com.intellij.util.io.DataExternalizer

class HttpVariablesExternalizer(
    elementExternalizer: DataExternalizer<String>,
    collectionFactory: () -> MutableMap<String, String>,
) : CollectionExternalizer<String, MutableMap<String, String>>(
    elementExternalizer, collectionFactory
)
