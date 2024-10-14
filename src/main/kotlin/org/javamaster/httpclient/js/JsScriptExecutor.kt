package org.javamaster.httpclient.js

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.javamaster.httpclient.utils.HttpUtils.gson
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import javax.script.Invocable
import javax.script.ScriptEngine

/**
 * 执行请求的前置js和后置js脚本
 *
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class JsScriptExecutor {
    private var engine: ScriptEngine = NashornScriptEngineFactory().getScriptEngine("Nashorn")

    init {
        engine.eval(
            """
                client = {
                  fullMsg: '',
                  log: function(msg) {
                    this.fullMsg = this.fullMsg + '# ' + msg + '\r\n';
                  },
                  global: {
                    dataHolder: {},
                    isEmpty: function() {
                      return Object.keys(this.dataHolder).length === 0;
                    },
                    get: function(key) {
                      return this.dataHolder[key] !== undefined ? this.dataHolder[key] : null;
                    },
                    clear: function(key) {
                      return delete this.dataHolder[key];
                    },
                    clearAll: function() {
                      this.dataHolder = {};
                      return true;
                    },
                    set: function(key, val) {
                      this.dataHolder[key] = val;
                      client.log(key + ' 已设置为(global): ' + val);
                    },
                  },
                  test: function(successMsg, assertCallback) {
                    var success = assertCallback();                    
                    if(success === true || success === undefined) {                   
                        this.log(successMsg);
                    }
                  },
                  assert: function(success, failMsg) {
                    if(!success) {                      
                      this.log("断言失败: " + failMsg);
                    }
                    return success;
                  }                
                }
                function getLog() {
                    var tmp = client.fullMsg;
                    client.fullMsg = '';
                    return tmp;
                }
                function getGlobalVariable(key) {
                    return client.global.get(key);
                }
                function getRequestVariable(key) {
                    return request.variables.get(key);
                }
        """.trimIndent()
        )
    }

    fun prepareJsRequestObj() {
        val js = """
            var request = {
              variables: {
                dataHolder: {},
                get: function(key) {
                  return this.dataHolder[key] !== undefined ? this.dataHolder[key] : null;
                },
                set: function(key, val) {
                  this.dataHolder[key] = val;
                  client.log(key + ' 已设置为(request): ' + val);
                },
              }
            };
            """.trimIndent()
        engine.eval(js)
    }

    fun clearJsRequestObj() {
        val js = """
              request = null;
            """.trimIndent()
        engine.eval(js)
    }

    fun evalJsBeforeRequest(beforeJsScripts: List<String>): List<String> {
        if (beforeJsScripts.isEmpty()) {
            return arrayListOf()
        }

        val resList = mutableListOf("# 前置js执行结果:\r\n")

        val list = beforeJsScripts
            .map { evalJs(it) }
            .filter { it.isNotEmpty() }

        resList.addAll(list)
        return resList
    }

    fun evalJsAfterRequest(
        jsScript: String?,
        response: HttpResponse<ByteArray>,
        resPair: Pair<String, ByteArray>,
    ): String? {
        if (jsScript == null) {
            return null
        }

        val headerJsonStr = gson.toJson(response.headers().map())

        val body: String
        if (resPair.first == "json") {
            val bytes = resPair.second
            val jsonStr = String(bytes, StandardCharsets.UTF_8)
            body = jsonStr
        } else {
            body = "'{}'"
        }

        val js = """
              response = {
                status: ${response.statusCode()},
                body: $body,
                headers: $headerJsonStr
              };
              $jsScript;
            """.trimIndent()

        return evalJs(js)
    }

    private fun evalJs(jsScript: String?): String {
        try {
            engine.eval(jsScript)
        } catch (e: Exception) {
            return "# " + e.message + "\r\n"
        }

        val invocable = engine as Invocable
        return invocable.invokeFunction("getLog") as String
    }

    fun getRequestVariable(key: String): String? {
        val invocable = engine as Invocable
        return invocable.invokeFunction("getRequestVariable", key) as String?
    }

    fun getGlobalVariable(key: String): String? {
        val invocable = engine as Invocable
        return invocable.invokeFunction("getGlobalVariable", key) as String?
    }

    companion object {
        fun getService(project: Project): JsScriptExecutor {
            return project.getService(JsScriptExecutor::class.java)
        }
    }
}
