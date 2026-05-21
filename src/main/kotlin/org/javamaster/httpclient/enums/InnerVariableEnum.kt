package org.javamaster.httpclient.enums

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.util.system.OS
import io.ktor.http.*
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateFormatUtils
import org.apache.commons.lang3.time.DateUtils
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.RandomStringUtils
import org.javamaster.httpclient.utils.StreamUtils
import org.javamaster.httpclient.utils.VirtualFileUtils
import org.mozilla.javascript.Context
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.*
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit


enum class InnerVariableEnum(val methodName: String) {
    RANDOM_ALPHABETIC("\$random.alphabetic") {
        override fun typeText(): String {
            return nls("alphabetic.desc", methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is Int) {
                throw IllegalArgumentException(nls("method.wrong.args", methodName, typeText()))
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
            return nls("alphanumeric.desc", methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is Int) {
                throw IllegalArgumentException(nls("method.wrong.args", methodName, typeText()))
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
            return nls("hexadecimal.desc", methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is Int) {
                throw IllegalArgumentException(nls("method.wrong.args", methodName, typeText()))
            }

            val length = args[0] as Int
            return RandomStringUtils.random(length, "0123456789ABCDEF")
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    RANDOM_NUMERIC("\$random.numeric") {
        override fun typeText(): String {
            return nls("numeric.desc", methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is Int) {
                throw IllegalArgumentException(nls("method.wrong.args", methodName, typeText()))
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
            return nls("uuid.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return UUID.randomUUID().toString()
        }
    },
    RANDOM_INT("\$randomInt") {
        override fun typeText(): String {
            return nls("randomInt.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return ThreadLocalRandom.current().nextInt(0, 1000).toString()
        }
    },
    RANDOM_INTEGER("\$random.integer") {
        override fun typeText(): String {
            return nls("integer.desc", methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 2 || args[0] !is Int || args[1] !is Int) {
                return ThreadLocalRandom.current().nextInt(0, 1000).toString()
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
            return nls("float.desc", methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 2 || args[0] !is Int || args[1] !is Int) {
                return ThreadLocalRandom.current().nextFloat(0F, 1000F).toString()
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
            return nls("timestamp.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return System.currentTimeMillis().toString()
        }
    },
    TIMESTAMP_FULL("\$timestampFull") {
        override fun typeText(): String {
            return nls("timestampFull.desc", methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 3 || args[0] !is Int || args[1] !is Int || args[2] !is Int) {
                throw IllegalArgumentException(nls("method.wrong.args", methodName, typeText()))
            }

            val day = args[0] as Int
            val hour = args[1] as Int
            val minute = args[2] as Int

            val now = LocalDateTime.now().plusDays(day.toLong())
            val localDateTime = LocalDateTime.of(now.year, now.month, now.dayOfMonth, hour, minute, 0)
            val zonedDateTime = localDateTime.atZone(ZoneId.systemDefault())
            val instant = zonedDateTime.toInstant()
            return instant.toEpochMilli().toString()
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    ISO_TIMESTAMP("\$isoTimestamp") {
        override fun typeText(): String {
            return nls("isoTimestamp.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return DateFormatUtils.format(Date(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getDefault())
        }
    },
    DATETIME("\$datetime") {
        override fun typeText(): String {
            return nls("datetime.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return DateFormatUtils.format(Date(), "yyyy-MM-dd HH:mm:ss", TimeZone.getDefault())
        }
    },
    TIMESTAMP_DATE("\$timestampDate") {
        override fun typeText(): String {
            return nls("timestampDate.desc", methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is Int) {
                throw IllegalArgumentException(nls("method.wrong.args", methodName, typeText()))
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
    MY_UUID("\$uuid") {
        override fun typeText(): String {
            return "生成 uuid"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return UUID.randomUUID().toString()
        }
    },
    DATE("\$date") {
        override fun typeText(): String {
            return nls("date.desc", methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.isEmpty() || args[0] !is Int) {
                throw IllegalArgumentException(nls("method.wrong.args", methodName, typeText()))
            }

            if (args.size > 1 && args[1] !is String) {
                throw IllegalArgumentException(nls("method.wrong.args", methodName, typeText()))
            }

            val count = args[0] as Int
            val pattern = if (args.size > 1) {
                args[1] as String
            } else {
                "yyyy-MM-dd"
            }

            val date = DateUtils.addDays(Date(), count)

            return DateFormatUtils.format(date, pattern)
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    IMAGE_TO_BASE64("\$imageToBase64") {
        override fun typeText(): String {
            return nls("imageToBase64.desc", methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is String) {
                throw IllegalArgumentException(nls("method.wrong.args", methodName, typeText()))
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
    FILE_TO_BASE64("\$fileToBase64") {
        override fun typeText(): String {
            return IMAGE_TO_BASE64.typeText()
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return IMAGE_TO_BASE64.exec(variableName, httpFileParentPath, *args)
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return IMAGE_TO_BASE64.insertHandler()
        }
    },
    READ_STRING("\$readString") {
        override fun typeText(): String {
            return nls("readString.desc", methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is String) {
                throw IllegalArgumentException(nls("method.wrong.args", methodName, typeText()))
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
    RANDOM_ADDRESS("\$random.address") {
        override fun typeText(): String {
            return nls("address.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_ANCIENT("\$random.ancient") {
        override fun typeText(): String {
            return "生成 ancient"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_BEER("\$random.beer") {
        override fun typeText(): String {
            return "生成 beer"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_BOOL("\$random.bool") {
        override fun typeText(): String {
            return nls("bool.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_BOOK("\$random.book") {
        override fun typeText(): String {
            return nls("book.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_BUSINESS("\$random.business") {
        override fun typeText(): String {
            return "生成 business"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_CHUCK_NORRIS("\$random.chuckNorris") {
        override fun typeText(): String {
            return "生成 chuck norris"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_CODE("\$random.code") {
        override fun typeText(): String {
            return "生成 code"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_EMAIL("\$random.email") {
        override fun typeText(): String {
            return "生成 email"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return RandomStringUtils.faker().internet().emailAddress()
        }
    },
    RANDOM_COLOR("\$random.color") {
        override fun typeText(): String {
            return "生成 color"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_COMMERCE("\$random.commerce") {
        override fun typeText(): String {
            return "生成 commerce"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_COMPANY("\$random.company") {
        override fun typeText(): String {
            return nls("company.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_CRYPTO("\$random.crypto") {
        override fun typeText(): String {
            return "生成 crypto"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_DATE_AND_TIME("\$random.dateAndTime") {
        override fun typeText(): String {
            return "生成 date and time"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName.replace("dateAndTime", "date"), *args)
        }
    },
    RANDOM_EDUCATOR("\$random.educator") {
        override fun typeText(): String {
            return "生成 educator"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_FINANCE("\$random.finance") {
        override fun typeText(): String {
            return "生成 finance"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_HACKER("\$random.hacker") {
        override fun typeText(): String {
            return "生成 hacker"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_ID_NUMBER("\$random.idNumber") {
        override fun typeText(): String {
            return "生成 id number"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_LOREM("\$random.lorem") {
        override fun typeText(): String {
            return "生成 lorem"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_NAME("\$random.name") {
        override fun typeText(): String {
            return nls("name.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_APP("\$random.app") {
        override fun typeText(): String {
            return nls("app.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_OPTIONS("\$random.options") {
        override fun typeText(): String {
            return "生成 options"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_PHONE_NUMBER("\$random.phoneNumber") {
        override fun typeText(): String {
            return "生成 phone number"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_SHAKESPEARE("\$random.shakespeare") {
        override fun typeText(): String {
            return "生成 shakespeare"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_SUPER_HERO("\$random.superhero") {
        override fun typeText(): String {
            return nls("hero.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_NATION("\$random.nation") {
        override fun typeText(): String {
            return nls("nation.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_UNIVERSITY("\$random.university") {
        override fun typeText(): String {
            return nls("university.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_INTERNET("\$random.internet") {
        override fun typeText(): String {
            return "生成 internet 相关"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_ANIMAL("\$random.animal") {
        override fun typeText(): String {
            return "生成 animal"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_NUMBER("\$random.number") {
        override fun typeText(): String {
            return "生成范围内数字,用法${methodName}(10, 1000)"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 2 || args[0] !is Int || args[1] !is Int) {
                return RandomStringUtils.faker().number().numberBetween(0, 1000).toString()
            }

            val start = args[0] as Int
            val end = args[1] as Int

            return RandomStringUtils.faker().number().numberBetween(start, end).toString()
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    RANDOM_TEAM("\$random.team") {
        override fun typeText(): String {
            return "生成 team"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    RANDOM_PROGRAMMING_LANGUAGE("\$random.programmingLanguage") {
        override fun typeText(): String {
            return "生成 programmingLanguage"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return callFakerMethod(variableName, *args)
        }
    },
    PICK("\$random.pick") {
        override fun typeText(): String {
            return nls("pick.desc", methodName, methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
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
            return nls("repeat.desc", methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 2 || args[0] !is String || args[1] !is Int) {
                throw IllegalArgumentException(nls("method.wrong.args", methodName, typeText()))
            }

            val str = args[0] as String
            val times = args[1] as Int

            return StringUtils.repeat(str, times)
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    TO_JSON_STR("\$toJsonStr") {
        override fun typeText(): String {
            this.methodName
            return "将对象序列化为JSON,用法:${methodName}('name'),name为js全局或请求变量名"
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
           throw UnsupportedOperationException()
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    EVAL("\$eval") {
        override fun typeText(): String {
            return nls("eval.desc", methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is String) {
                throw IllegalArgumentException(nls("method.wrong.args", methodName, typeText()))
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
            return nls("exec.desc", methodName)
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            if (args.size != 1 || args[0] !is String) {
                throw IllegalArgumentException(nls("method.wrong.args", methodName, typeText()))
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
            return nls("mvnTarget.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return exec("", ProjectUtil.getActiveProject() ?: return "") ?: return ""
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
            return nls("projectRoot.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return ProjectUtil.getActiveProject()?.basePath ?: ""
        }

        override fun exec(httpFileParentPath: String, project: Project): String? {
            return project.basePath
        }
    },
    HISTORY_FOLDER("\$historyFolder") {
        override fun typeText(): String {
            return nls("historyFolder.desc")
        }

        override fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String {
            return exec("", ProjectUtil.getActiveProject() ?: return "") ?: return ""
        }

        override fun exec(httpFileParentPath: String, project: Project): String? {
            val basePath = project.basePath ?: return null

            return "$basePath/.idea/httpClient"
        }
    },
    ;

    abstract fun typeText(): String

    abstract fun exec(variableName: String, httpFileParentPath: String, vararg args: Any): String

    open fun exec(httpFileParentPath: String, project: Project): String? {
        return null
    }

    open fun insertHandler(): InsertHandler<LookupElement>? {
        return null
    }

    companion object {
        fun isFolderEnum(innerVariableEnum: InnerVariableEnum?): Boolean {
            innerVariableEnum ?: return false
            return innerVariableEnum == HISTORY_FOLDER
                    || innerVariableEnum == PROJECT_ROOT
                    || innerVariableEnum == MVN_TARGET
        }

        fun getEnum(variable: String): InnerVariableEnum? {
            return InnerVariableEnum.entries
                .filter {
                    variable.startsWith(it.methodName)
                }
                .maxByOrNull { it.methodName.length }
        }

        private fun callFakerMethod(variableName: String, vararg args: Any): String {
            val split = variableName.split(".")
            val methodName = split[1]

            val faker = RandomStringUtils.faker()

            val method = faker.javaClass.getDeclaredMethod(methodName)
            method.isAccessible = true
            val targetObj = method.invoke(faker)

            if (split.size == 3) {
                val declaredMethod = targetObj.javaClass.getDeclaredMethod(split[2])
                declaredMethod.isAccessible = true
                return "" + declaredMethod.invoke(targetObj)
            }

            if (args.size != 1 || args[0] !is String) {
                val declaredMethod = targetObj.javaClass.declaredMethods[0]
                declaredMethod.isAccessible = true
                return "" + declaredMethod.invoke(targetObj)
            }

            val declaredMethod = targetObj.javaClass.getDeclaredMethod(args[0] as String)
            declaredMethod.isAccessible = true
            return "" + declaredMethod.invoke(targetObj)
        }
    }
}
