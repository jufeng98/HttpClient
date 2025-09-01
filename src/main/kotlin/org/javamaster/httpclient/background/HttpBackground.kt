package org.javamaster.httpclient.background

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.util.application
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * @author yudong
 */
class HttpBackground<T> {
    private var resultConsumer: Consumer<T?>? = null
    private var exceptionConsumer: Consumer<Exception>? = null

    fun finishOnUiThread(resultConsumer: Consumer<T?>): HttpBackground<T> {
        this.resultConsumer = resultConsumer

        return this
    }

    fun exceptionallyOnUiThread(exceptionConsumer: Consumer<Exception>): HttpBackground<T> {
        this.exceptionConsumer = exceptionConsumer

        return this
    }

    companion object {

        fun <T> runInBackgroundReadActionAsync(supplier: Supplier<T?>): HttpBackground<T> {
            val background = HttpBackground<T>()

            application.executeOnPooledThread {
                runReadAction {
                    try {
                        val result = supplier.get()

                        runInEdt {
                            try {
                                background.resultConsumer!!.accept(result)
                            } catch (ex: Exception) {
                                ex.printStackTrace()

                                background.exceptionConsumer!!.accept(ex)
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()

                        runInEdt {
                            background.exceptionConsumer!!.accept(ex)
                        }
                    }
                }
            }

            return background
        }
    }

}
