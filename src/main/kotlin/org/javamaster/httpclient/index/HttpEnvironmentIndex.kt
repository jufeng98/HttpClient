package org.javamaster.httpclient.index

import com.intellij.json.psi.*
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.javamaster.httpclient.index.support.HttpEnvironmentInputFilter
import org.javamaster.httpclient.index.support.HttpVariablesExternalizer
import org.javamaster.httpclient.index.support.StringDataExternalizer


class HttpEnvironmentIndex : FileBasedIndexExtension<String, MutableMap<String, String>>() {
    override fun getName(): ID<String, MutableMap<String, String>> {
        return INDEX_ID
    }

    override fun getIndexer(): DataIndexer<String, MutableMap<String, String>, FileContent> {
        return DataIndexer<String, MutableMap<String, String>, FileContent> { inputData ->
            val file = inputData.psiFile as JsonFile

            val root = file.topLevelValue as? JsonObject ?: return@DataIndexer emptyMap()

            val result: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

            for (property in root.propertyList) {
                val envValueObj = property.value

                if (envValueObj !is JsonObject) continue

                val env = property.name
                result[env] = readEnvVariables(envValueObj)
            }

            result
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): DataExternalizer<MutableMap<String, String>> {
        return HttpVariablesExternalizer(StringDataExternalizer()) { mutableMapOf() }
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return HttpEnvironmentInputFilter()
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    private fun readEnvVariables(envObj: JsonObject): MutableMap<String, String> {
        val properties = envObj.propertyList
        if (properties.isEmpty()) {
            return mutableMapOf()
        }

        val map = mutableMapOf<String, String>()

        properties.stream()
            .forEach {
                val key = it.name
                if (key.isBlank()) {
                    return@forEach
                }

                when (val innerJsonValue = it.value) {
                    is JsonStringLiteral -> {
                        map[key] = innerJsonValue.value
                    }

                    is JsonNumberLiteral -> {
                        map[key] = "" + innerJsonValue.value
                    }

                    is JsonBooleanLiteral -> {
                        map[key] = innerJsonValue.value.toString()
                    }

                    else -> {
                        map[key] = innerJsonValue?.text ?: ""
                    }
                }
            }

        return map
    }

    @Suppress("CompanionObjectInExtension")
    companion object {
        val INDEX_ID: ID<String, MutableMap<String, String>> = ID.create("http.execution.environment")
    }
}
