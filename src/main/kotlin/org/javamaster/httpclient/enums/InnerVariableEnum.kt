package org.javamaster.httpclient.enums

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.util.system.OS
import io.ktor.http.*
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateFormatUtils
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.RandomStringUtils
import org.javamaster.httpclient.utils.StreamUtils
import org.javamaster.httpclient.utils.VirtualFileUtils
import org.mozilla.javascript.Context
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

enum class InnerVariableEnum(val methodName: String) {
    RANDOM_ALPHABETIC("\$random.alphabetic") {
        override fun typeText(): String {
            return NlsBundle.nls("alphabetic.desc", methodName)
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
            return NlsBundle.nls("alphanumeric.desc", methodName)
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
    RANDOM_HEXADECIMAL("\$random.hexadecimal") {
        override fun typeText(): String {
            return NlsBundle.nls("hexadecimal.desc", methodName)
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is Int) {
                throw IllegalArgumentException("$methodName has wrong arguments.${typeText()}")
            }

            val max = args[0] as Int
            return ThreadLocalRandom.current().nextInt(max).toString(16).uppercase()
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    RANDOM_NUMBER("\$random.numeric") {
        override fun typeText(): String {
            return NlsBundle.nls("numeric.desc", methodName)
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
            return NlsBundle.nls("uuid.desc")
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return UUID.randomUUID().toString().replace("-", "")
        }
    },
    RANDOM_INT("\$randomInt") {
        override fun typeText(): String {
            return NlsBundle.nls("randomInt.desc")
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return ThreadLocalRandom.current().nextInt(0, 1000).toString()
        }
    },
    RANDOM_INTEGER("\$random.integer") {
        override fun typeText(): String {
            return NlsBundle.nls("integer.desc", methodName)
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 2 || args[0] !is Int || args[1] !is Int) {
                throw IllegalArgumentException("$methodName has wrong arguments.${typeText()}")
            }

            val start = args[0] as Int
            val end = args[1] as Int
            return ThreadLocalRandom.current().nextInt(start, end).toString()
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    RANDOM_FLOAT("\$random.float") {
        override fun typeText(): String {
            return NlsBundle.nls("float.desc", methodName)
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 2 || args[0] !is Int || args[1] !is Int) {
                throw IllegalArgumentException("$methodName has wrong arguments.${typeText()}")
            }

            val start = args[0] as Int
            val end = args[1] as Int
            return ThreadLocalRandom.current().nextFloat(start.toFloat(), end.toFloat()).toString()
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    TIMESTAMP("\$timestamp") {
        override fun typeText(): String {
            return NlsBundle.nls("timestamp.desc")
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return System.currentTimeMillis().toString()
        }
    },
    ISO_TIMESTAMP("\$isoTimestamp") {
        override fun typeText(): String {
            return NlsBundle.nls("isoTimestamp.desc")
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return DateFormatUtils.format(Date(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getDefault())
        }
    },
    DATETIME("\$datetime") {
        override fun typeText(): String {
            return NlsBundle.nls("datetime.desc")
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return DateFormatUtils.format(Date(), "yyyy-MM-dd HH:mm:ss", TimeZone.getDefault())
        }
    },
    TIMESTAMP_DATE("\$timestampDate") {
        override fun typeText(): String {
            return NlsBundle.nls("timestampDate.desc", methodName)
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is Int) {
                throw IllegalArgumentException("$methodName has wrong arguments.${typeText()}")
            }

            val count = args[0] as Int

            val seconds = LocalDateTime.of(LocalDate.now().plusDays(count.toLong()), LocalTime.of(0, 0, 0))
                .toEpochSecond(ZoneOffset.of("+08:00")) * 1000
            return seconds.toString()
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    IMAGE_TO_BASE64("\$imageToBase64") {
        override fun typeText(): String {
            return NlsBundle.nls("imageToBase64.desc", methodName)
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is String) {
                throw IllegalArgumentException("$methodName has wrong arguments.${typeText()}")
            }

            val path = args[0] as String
            val filePath = HttpUtils.constructFilePath(path, httpFileParentPath)
            val file = File(filePath)

            val bytes = VirtualFileUtils.readNewestBytes(file)
            if (bytes.isEmpty()) {
                return ""
            }

            return Base64.getEncoder().encodeToString(bytes)
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    READ_STRING("\$readString") {
        override fun typeText(): String {
            return NlsBundle.nls("readString.desc", methodName)
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
            return NlsBundle.nls("address.desc")
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().address().fullAddress()
        }
    },
    RANDOM_BOOL("\$random.bool") {
        override fun typeText(): String {
            return NlsBundle.nls("bool.desc")
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().bool().bool().toString()
        }
    },
    RANDOM_NAME("\$random.name") {
        override fun typeText(): String {
            return NlsBundle.nls("name.desc")
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().name().name()
        }
    },
    RANDOM_BOOK_TITLE("\$random.book.title") {
        override fun typeText(): String {
            return NlsBundle.nls("book.desc")
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().book().title()
        }
    },
    RANDOM_APP_NAME("\$random.app.name") {
        override fun typeText(): String {
            return NlsBundle.nls("app.desc")
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().app().name()
        }
    },
    RANDOM_COMPANY_NAME("\$random.company.name") {
        override fun typeText(): String {
            return NlsBundle.nls("company.desc")
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().company().name()
        }
    },
    RANDOM_HERO_NAME("\$random.hero.name") {
        override fun typeText(): String {
            return NlsBundle.nls("hero.desc")
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().superhero().name()
        }
    },
    RANDOM_NATION_NAME("\$random.nation.name") {
        override fun typeText(): String {
            return NlsBundle.nls("nation.desc")
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().nation().capitalCity()
        }
    },
    RANDOM_UNIVERSITY_NAME("\$random.university.name") {
        override fun typeText(): String {
            return NlsBundle.nls("university.desc")
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().university().name()
        }
    },
    PICK("\$random.pick") {
        override fun typeText(): String {
            return NlsBundle.nls("pick.desc", methodName, methodName)
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
    REPEAT("\$repeat") {
        override fun typeText(): String {
            return NlsBundle.nls("repeat.desc", methodName)
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 2 || args[0] !is String || args[1] !is Int) {
                throw IllegalArgumentException("$methodName has wrong arguments.${typeText()}")
            }

            val str = args[0] as String
            val times = args[1] as Int

            return StringUtils.repeat(str, times)
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    EVAL("\$eval") {
        override fun typeText(): String {
            return NlsBundle.nls("eval.desc", methodName)
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is String) {
                throw IllegalArgumentException("$methodName has wrong arguments.${typeText()}")
            }

            val context = Context.enter()

            context.use {
                val scriptableObject = it.initStandardObjects()
                val res = it.evaluateString(scriptableObject, args[0] as String, "dummy.js", 1, null)
                return res.toString()
            }
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    EXEC("\$exec") {
        override fun typeText(): String {
            return NlsBundle.nls("exec.desc", methodName)
        }

        override fun exec(httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is String) {
                throw IllegalArgumentException("$methodName has wrong arguments.${typeText()}")
            }

            val command = if (OS.CURRENT == OS.Windows) {
                "cmd /c " + args[0]
            } else {
                args[0] as String
            }

            @Suppress("DEPRECATION")
            val process = Runtime.getRuntime().exec(command)
            process.waitFor(3, TimeUnit.SECONDS)

            var msg = StreamUtils.copyToStringClose(process.inputStream, Charset.forName("GBK")).escapeIfNeeded()

            msg = if (msg != "") msg else StreamUtils.copyToStringClose(process.errorStream, StandardCharsets.UTF_8)

            msg = msg.escapeIfNeeded().substring(1, msg.length - 1).replace("\\", "\\\\")

            return msg
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    MVN_TARGET("\$mvnTarget") {
        override fun typeText(): String {
            return NlsBundle.nls("mvnTarget.desc")
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
            return NlsBundle.nls("projectRoot.desc")
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
            return NlsBundle.nls("historyFolder.desc")
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
