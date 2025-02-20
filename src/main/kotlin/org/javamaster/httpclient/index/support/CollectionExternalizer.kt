package org.javamaster.httpclient.index.support

import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.DataInputOutputUtil
import java.io.DataInput
import java.io.DataOutput

open class CollectionExternalizer<T, C : MutableMap<T, T>>(
    private val elementExternalizer: DataExternalizer<T>,
    private val collectionFactory: () -> C,
) : DataExternalizer<C> {
    override fun save(out: DataOutput, value: C) {
        DataInputOutputUtil.writeINT(out, value.size)

        value.entries.forEach {
            elementExternalizer.save(out, it.key)
            elementExternalizer.save(out, it.value)
        }
    }

    override fun read(`in`: DataInput): C {
        val size = DataInputOutputUtil.readINT(`in`)

        val value = collectionFactory.invoke()

        for (i in 0..<size) {
            val k = elementExternalizer.read(`in`)
            val v = elementExternalizer.read(`in`)
            value[k] = v
        }

        return value
    }

}
