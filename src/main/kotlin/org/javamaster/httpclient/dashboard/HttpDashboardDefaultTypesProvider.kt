package org.javamaster.httpclient.dashboard

import com.intellij.execution.dashboard.RunDashboardDefaultTypesProvider
import com.intellij.openapi.project.Project
import com.intellij.util.SmartList
import org.javamaster.httpclient.utils.HttpUtils

class HttpDashboardDefaultTypesProvider : RunDashboardDefaultTypesProvider {
    override fun getDefaultTypeIds(project: Project): MutableCollection<String> {
        return SmartList(HttpUtils.HTTP_TYPE_ID)
    }
}
