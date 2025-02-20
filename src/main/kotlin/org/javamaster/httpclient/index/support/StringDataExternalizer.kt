package org.javamaster.httpclient.index.support

import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.IOUtil
import java.io.DataInput
import java.io.DataOutput

class StringDataExternalizer : DataExternalizer<String> {
    override fun save(out: DataOutput, value: String) {
        IOUtil.writeUTF(out, value)
    }

    override fun read(`in`: DataInput): String {
        return IOUtil.readUTF(`in`)
    }
}
