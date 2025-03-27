package org.javamaster.httpclient.enums

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.RandomStringUtils
import org.javamaster.httpclient.utils.VirtualFileUtils
import java.io.File
import java.util.*
import java.util.concurrent.ThreadLocalRandom

enum class InnerVariableEnum(val methodName: String) {
    RANDOM_ALPHABETIC("\$random.alphabetic") {
        override fun typeText(): String {
            return "用法:$methodName(8)"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is Int) {
                throw IllegalArgumentException("$methodName has wrong arguments.${typeText()}")
            }

            val count = args[0] as Int
            return RandomStringUtils.randomAlphabetic(count)
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    RANDOM_ALPHA_NUMERIC("\$random.alphanumeric") {
        override fun typeText(): String {
            return "用法:$methodName(8)"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is Int) {
                throw IllegalArgumentException("$methodName has wrong arguments.${typeText()}")
            }

            val count = args[0] as Int
            return RandomStringUtils.randomAlphanumeric(count)
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    RANDOM_NUMBER("\$random.numeric") {
        override fun typeText(): String {
            return "用法:$methodName(8)"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is Int) {
                throw IllegalArgumentException("$methodName has wrong arguments.${typeText()}")
            }

            val count = args[0] as Int
            return RandomStringUtils.randomNumeric(count)
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    RANDOM_UUID("\$random.uuid") {
        override fun typeText(): String {
            return "调用 UUID.randomUUID() 并去掉其-"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return UUID.randomUUID().toString().replace("-", "")
        }
    },
    RANDOM_INT("\$randomInt") {
        override fun typeText(): String {
            return "生成 [0, 1000) 范围数字"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return ThreadLocalRandom.current().nextInt(0, 1000).toString()
        }
    },
    RANDOM_INTEGER("\$random.integer") {
        override fun typeText(): String {
            return "用法:$methodName(0, 80)"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 2 || args[0] !is Int || args[1] !is Int) {
                throw IllegalArgumentException("$methodName has wrong arguments.${typeText()}")
            }

            val start = args[0] as Int
            val end = args[1] as Int
            return (start + ThreadLocalRandom.current().nextInt(0, end - start)).toString()
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    TIMESTAMP("\$timestamp") {
        override fun typeText(): String {
            return "时间戳"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return System.currentTimeMillis().toString()
        }
    },
    IMAGE_TO_BASE64("\$imageToBase64") {
        override fun typeText(): String {
            return "用法:$methodName('图片的绝对或相对路径')"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is String) {
                throw IllegalArgumentException("$methodName has wrong arguments.${typeText()}")
            }

            val path = args[0] as String
            val filePath = HttpUtils.constructFilePath(path, httpFileParentPath)
            val file = File(filePath)

            val bytes = VirtualFileUtils.readNewestBytes(file)

            return Base64.getEncoder().encodeToString(bytes)
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    READ_STRING("\$readString") {
        override fun typeText(): String {
            return "用法:$methodName('图片的绝对或相对路径')"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is String) {
                throw IllegalArgumentException("$methodName has wrong arguments.${typeText()}")
            }

            val path = args[0] as String
            val filePath = HttpUtils.constructFilePath(path, httpFileParentPath)
            val file = File(filePath)

            return VirtualFileUtils.readNewestContent(file)
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    RANDOM_ADDRESS("\$random.address.full") {
        override fun typeText(): String {
            return "生成完整地址"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().address().fullAddress()
        }
    },
    RANDOM_BOOL("\$random.bool") {
        override fun typeText(): String {
            return "生成布尔值"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().bool().bool().toString()
        }
    },
    RANDOM_NAME("\$random.name") {
        override fun typeText(): String {
            return "生成用户名"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().name().name()
        }
    },
    RANDOM_BOOK_TITLE("\$random.book.title") {
        override fun typeText(): String {
            return "生成标题"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().book().title()
        }
    },
    RANDOM_APP_NAME("\$random.app.name") {
        override fun typeText(): String {
            return "生成app名称"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().app().name()
        }
    },
    RANDOM_COMPANY_NAME("\$random.company.name") {
        override fun typeText(): String {
            return "生成公司名称"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().company().name()
        }
    },
    RANDOM_HERO_NAME("\$random.hero.name") {
        override fun typeText(): String {
            return "生成hero名称"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().superhero().name()
        }
    },
    RANDOM_NATION_NAME("\$random.nation.name") {
        override fun typeText(): String {
            return "生成国家首都"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().nation().capitalCity()
        }
    },
    RANDOM_UNIVERSITY_NAME("\$random.university.name") {
        override fun typeText(): String {
            return "生成大学"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().university().name()
        }
    },
    PICK("\$random.pick") {
        override fun typeText(): String {
            return "从给定的选项中随机挑选一个，用法:$methodName(23, 46) 或 $methodName('Jack', 'Rose')"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.isEmpty()) {
                throw IllegalArgumentException("$methodName must to past arguments.${typeText()}")
            }

            return args[RandomStringUtils.RANDOM.nextInt(args.size)].toString()
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    MVN_TARGET("\$mvnTarget") {
        override fun typeText(): String {
            return "指向模块的 src/target"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            throw UnsupportedOperationException()
        }

        override fun exec(httpFileParentPath: String, project: Project): String? {
            val triple = HttpEditorTopForm.getTriple(project) ?: return null

            val module = triple.third ?: return null

            val dirPath = ModuleUtil.getModuleDirPath(module)

            return "$dirPath/target"
        }
    },
    PROJECT_ROOT("\$projectRoot") {
        override fun typeText(): String {
            return "指向项目根目录"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            throw UnsupportedOperationException()
        }

        override fun exec(httpFileParentPath: String, project: Project): String? {
            return project.basePath
        }
    },
    HISTORY_FOLDER("\$historyFolder") {
        override fun typeText(): String {
            return "指向 .idea/httpClient"
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            throw UnsupportedOperationException()
        }

        override fun exec(httpFileParentPath: String, project: Project): String? {
            val basePath = project.basePath ?: return null

            return "$basePath/.idea/httpClient"
        }
    },
    ;

    abstract fun typeText(): String

    abstract fun exec(httpFileParentPath: String, vararg args: Any): String

    open fun exec(httpFileParentPath: String, project: Project): String? {
        return null
    }

    open fun insertHandler(): InsertHandler<LookupElement>? {
        return null
    }

    companion object {
        private val map by lazy {
            val map = mutableMapOf<String, InnerVariableEnum>()
            InnerVariableEnum.entries
                .forEach {
                    map[it.methodName] = it
                }
            return@lazy map
        }

        fun isFolderEnum(innerVariableEnum: InnerVariableEnum?): Boolean {
            innerVariableEnum ?: return false
            return innerVariableEnum == HISTORY_FOLDER
                    || innerVariableEnum == PROJECT_ROOT
                    || innerVariableEnum == MVN_TARGET
        }

        fun getEnum(variable: String): InnerVariableEnum? {
            return map[variable]
        }
    }
}
