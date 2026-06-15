package org.javamaster.httpclient.listener

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import org.javamaster.httpclient.dubbo.DubboRequest
import org.javamaster.httpclient.logger.HttpRequestLogger.logWarn

/**
 * @author yudong
 */
class HttpProjectManagerListener : ProjectManagerListener {

    override fun projectClosing(project: Project) {
        try {
            DubboRequest.referenceConfigCache.destroyAll()
        } catch (t: Throwable) {
            logWarn("销毁 dubbo 出错", t)
        }
    }

}