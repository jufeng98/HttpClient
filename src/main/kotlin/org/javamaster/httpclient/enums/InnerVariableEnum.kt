package org.javamaster.httpclient.enums

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.readBytes
import com.intellij.openapi.vfs.readText
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.RandomStringUtils
import java.io.File
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.regex.Pattern

enum class InnerVariableEnum(val methodName: String) {
    RANDOM_ALPHABETIC("\$random.alphabetic") {
        override fun typeText(): String {
            return "用法:$methodName(8)"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            val count = patternNotNumber.matcher(variable).replaceAll("")
            return RandomStringUtils.randomAlphabetic(count.toInt())
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    RANDOM_ALPHA_NUMERIC("\$random.alphanumeric") {
        override fun typeText(): String {
            return "用法:$methodName(8)"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            val count = patternNotNumber.matcher(variable).replaceAll("")
            return RandomStringUtils.randomAlphanumeric(count.toInt())
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    RANDOM_NUMBER("\$random.numeric") {
        override fun typeText(): String {
            return "用法:$methodName(8)"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            val count = patternNotNumber.matcher(variable).replaceAll("")
            return RandomStringUtils.randomNumeric(count.toInt())
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    RANDOM_UUID("\$random.uuid") {
        override fun typeText(): String {
            return "调用 UUID.randomUUID() 并去掉其-"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            return UUID.randomUUID().toString().replace("-", "")
        }
    },
    RANDOM_INT("\$randomInt") {
        override fun typeText(): String {
            return "生成 [0,1000) 范围数字"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            return ThreadLocalRandom.current().nextInt(0, 1000).toString()
        }
    },
    RANDOM_INTEGER("\$random.integer") {
        override fun typeText(): String {
            return "用法:$methodName(0,80)"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            val split = variable.split(",")
            val start = patternNotNumber.matcher(split[0]).replaceAll("").toInt()
            val end = patternNotNumber.matcher(split[1]).replaceAll("").toInt()
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

        override fun exec(variable: String, httpFileParentPath: String): String {
            return System.currentTimeMillis().toString()
        }
    },
    IMAGE_TO_BASE64("\$imageToBase64") {
        override fun typeText(): String {
            return "用法:$methodName(图片的绝对或相对路径)"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            val imagePath = variable.substring(methodName.length + 1, variable.length - 1)
            val filePath = HttpUtils.constructFilePath(imagePath, httpFileParentPath)
            val file = File(filePath)
            if (!file.exists()) {
                throw IllegalArgumentException("文件${file.absoluteFile.normalize().absolutePath}不存在!")
            }

            if (file.isDirectory) {
                throw IllegalArgumentException("${file.absoluteFile.normalize().absolutePath}不是文件!")
            }

            val virtualFile = VfsUtil.findFileByIoFile(file, true)!!
            val bytes = virtualFile.readBytes()

            return Base64.getEncoder().encodeToString(bytes)
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    READ_STRING("\$readString") {
        override fun typeText(): String {
            return "用法:$methodName(图片的绝对或相对路径)"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            val path = variable.substring(methodName.length + 1, variable.length - 1)
            val filePath = HttpUtils.constructFilePath(path, httpFileParentPath)
            val file = File(filePath)
            if (!file.exists()) {
                throw IllegalArgumentException("文件${file.absoluteFile.normalize().absolutePath}不存在!")
            }

            if (file.isDirectory) {
                throw IllegalArgumentException("${file.absoluteFile.normalize().absolutePath}不是文件!")
            }

            val virtualFile = VfsUtil.findFileByIoFile(file, true)!!
            return virtualFile.readText()
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    RANDOM_ADDRESS("\$random.address.full") {
        override fun typeText(): String {
            return "生成完整地址"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            return RandomStringUtils.faker().address().fullAddress()
        }
    },
    RANDOM_BOOL("\$random.bool") {
        override fun typeText(): String {
            return "生成布尔值"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            return RandomStringUtils.faker().bool().bool().toString()
        }
    },
    RANDOM_NAME("\$random.name") {
        override fun typeText(): String {
            return "生成用户名"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            return RandomStringUtils.faker().name().name()
        }
    },
    RANDOM_BOOK_TITLE("\$random.book.title") {
        override fun typeText(): String {
            return "生成标题"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            return RandomStringUtils.faker().book().title()
        }
    },
    RANDOM_APP_NAME("\$random.app.name") {
        override fun typeText(): String {
            return "生成app名称"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            return RandomStringUtils.faker().app().name()
        }
    },
    RANDOM_COMPANY_NAME("\$random.company.name") {
        override fun typeText(): String {
            return "生成公司名称"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            return RandomStringUtils.faker().company().name()
        }
    },
    RANDOM_HERO_NAME("\$random.hero.name") {
        override fun typeText(): String {
            return "生成hero名称"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            return RandomStringUtils.faker().superhero().name()
        }
    },
    RANDOM_NATION_NAME("\$random.nation.name") {
        override fun typeText(): String {
            return "生成国家首都"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            return RandomStringUtils.faker().nation().capitalCity()
        }
    },
    RANDOM_UNIVERSITY_NAME("\$random.university.name") {
        override fun typeText(): String {
            return "生成大学"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            return RandomStringUtils.faker().university().name()
        }
    },
    PICK("\$random.pick") {
        override fun typeText(): String {
            return "从给定的选项中随机挑选一个，用法:$methodName(23, 46) 或 $methodName('Jack', 'Rose')"
        }

        override fun exec(variable: String, httpFileParentPath: String): String {
            val paramsStr = variable.substring(methodName.length + 1, variable.length - 1)

            val split = paramsStr.split(",").stream()
                .map { it.trim() }
                .toList()

            val value = split[RandomStringUtils.RANDOM.nextInt(split.size)]
            if (value.startsWith("'")) {
                return value.substring(1, value.length - 1)
            }

            return value
        }

        override fun insertHandler(): InsertHandler<LookupElement>? {
            return ParenthesesInsertHandler.WITH_PARAMETERS
        }
    },
    ;

    val patternNotNumber: Pattern = Pattern.compile("\\D")

    abstract fun typeText(): String

    abstract fun exec(variable: String, httpFileParentPath: String): String

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

        fun getEnum(variable: String): InnerVariableEnum? {
            val name = variable.split("(")[0]
            return map[name]
        }
    }
}
