package org.javamaster.httpclient.runconfig

import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.javamaster.httpclient.utils.HttpUtils.getTargetHttpMethod
import org.jdom.Element

/**
 * @author yudong
 */
class HttpRunConfiguration(
    project: Project,
    httpConfigurationFactory: HttpConfigurationFactory,
    name: String?,
) :
    RunConfigurationBase<HttpRunConfiguration>(
        project,
        httpConfigurationFactory,
        name
    ) {
    private val envKey = "env"
    private val pathKey = "httpFilePath"

    var httpFilePath: String = ""
    var env: String = ""


    override fun checkConfiguration() {
        getTargetHttpMethod(httpFilePath, this.name, project)
            ?: throw RuntimeConfigurationError("Tip:无法找到对应请求!")
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return HttpRunProfileState(project, environment, httpFilePath, env)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return HttpSettingsEditor(env, httpFilePath)
    }

    override fun writeExternal(element: Element) {
        val envEle = Element(envKey)
        envEle.text = env
        element.addContent(envEle)

        val pathEle = Element(pathKey)
        pathEle.text = httpFilePath
        element.addContent(pathEle)

        super.writeExternal(element)
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)

        env = element.getChild(envKey)?.text ?: ""
        httpFilePath = element.getChild(pathKey)?.text ?: ""
    }
}
