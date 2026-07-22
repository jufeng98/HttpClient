package org.javamaster.httpclient.scan.support

import com.google.common.collect.Maps
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import org.javamaster.httpclient.logger.HttpRequestLogger
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Supplier

/**
 * @author yudong
 */
class ControllerPsiTreeChangePreprocessor : Thread("controllerPsiTreeChangePreprocessorThread"),
    PsiTreeChangePreprocessor {
    private val tasks = TaskConcurrentMap()

    init {
        setDaemon(true)
        priority = MIN_PRIORITY

        start()
    }

    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        val psiJavaFile = event.file as? PsiJavaFile ?: return

        val project = psiJavaFile.project
        val dumbService = project.getService(DumbService::class.java)
        if (dumbService.isDumb) {
            return
        }

        val code = event.code
        if (code != PsiTreeChangeEventImpl.PsiEventType.CHILDREN_CHANGED) return

        if (tasks.size() > 60) {
            HttpRequestLogger.logWarn("任务堆积数量已超 60, 直接清除")

            tasks.clear()

            return
        }

        val psiRunnable = PsiRunnable(psiJavaFile, project)

        if (tasks.contains(psiRunnable)) {
            tasks.remove(psiRunnable)
        }

        tasks.add(psiRunnable)
    }

    override fun run() {
        while (true) {
            TimeUnit.SECONDS.sleep(2)

            val runnable = tasks.take()

            runnable.run()
        }
    }

    private class TaskConcurrentMap {
        private val map = Maps.newHashMap<String, Runnable>()

        private val lock = ReentrantLock()
        private val notEmpty = lock.newCondition()

        fun size(): Int {
            return map.size
        }

        fun clear() {
            computeInLock {
                map.clear()
            }
        }

        fun contains(element: Runnable): Boolean {
            val psiRunnable = element as PsiRunnable

            return computeInLock {
                map.contains(psiRunnable.psiJavaFile.virtualFile.path)
            }
        }

        fun remove(element: Runnable): Boolean {
            val psiRunnable = element as PsiRunnable

            return computeInLock {
                val runnable = map.remove(psiRunnable.psiJavaFile.virtualFile.path)

                runnable != null
            }
        }

        fun add(element: Runnable): Boolean {
            val psiRunnable = element as PsiRunnable

            computeInLock {
                map.put(psiRunnable.psiJavaFile.virtualFile.path, element)

                notEmpty.signal()
            }

            return true
        }

        fun take(): Runnable {
            return computeInLock {
                var ele = pickRandomAndRemove()

                while (ele == null) {
                    notEmpty.await()

                    ele = pickRandomAndRemove()
                }

                ele
            }
        }

        private fun <T> computeInLock(supplier: Supplier<T>): T {
            lock.lock()
            try {
                return supplier.get()
            } finally {
                lock.unlock()
            }
        }

        private fun pickRandomAndRemove(): Runnable? {
            val iterator = map.values.iterator()
            if (iterator.hasNext()) {
                val runnable = iterator.next()

                iterator.remove()

                return runnable
            }

            return null
        }

    }

}