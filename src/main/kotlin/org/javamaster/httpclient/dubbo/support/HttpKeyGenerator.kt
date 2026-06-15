package org.javamaster.httpclient.dubbo.support

import com.alibaba.dubbo.config.ReferenceConfig
import com.alibaba.dubbo.config.utils.ReferenceConfigCache
import org.apache.commons.lang3.StringUtils

/**
 * @author yudong
 */
object HttpKeyGenerator : ReferenceConfigCache.KeyGenerator {

    override fun generateKey(referenceConfig: ReferenceConfig<*>): String {
        val ret = StringBuilder()
        ret.append(referenceConfig.getInterface())
        ret.append(":").append(referenceConfig.timeout)

        val version = referenceConfig.getVersion()
        if (StringUtils.isNotBlank(version)) {
            ret.append(":").append(version)
        }

        val group = referenceConfig.group
        if (StringUtils.isNotBlank(group)) {
            ret.append(":").append(group)
        }

        val url = referenceConfig.url
        if (StringUtils.isNotBlank(url)) {
            ret.append(":").append(url)
        } else {
            ret.append(":").append(referenceConfig.registry.address)
            ret.append(":").append(referenceConfig.registry.timeout)
        }

        return ret.toString()
    }

}