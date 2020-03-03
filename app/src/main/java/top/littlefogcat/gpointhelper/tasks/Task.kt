package top.littlefogcat.gpointhelper.tasks

import android.accessibilityservice.AccessibilityService
import android.os.Looper
import top.littlefogcat.gpointhelper.ID_IMAGE_DIALOG_CLOSE
import top.littlefogcat.gpointhelper.ID_DIALOG_OK
import top.littlefogcat.gpointhelper.utils.Logger
import top.littlefogcat.gpointhelper.utils.performClickAccessibilityButton
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.Executors

/**
 * 任务基类。传入一个[Runnable]对象，执行任务相关的一些操作。
 * @see [TaskManager]
 */
abstract class Task(service: AccessibilityService) {

    companion object {
        const val TAG = "Task"

        const val STATUS_NORMAL = 0
        const val STATUS_ERROR = 1
    }

    private val mServiceRef = WeakReference<AccessibilityService>(service)

    private var mLastExecTime = 0L // 最后一次执行任务的时间
    /**
     * 任务完成状态的到期时间。任务完成之后，会进入一段时间的有效期，直到到期时间。
     * 在这个时间之前，任务不会再次执行。
     *
     * 例如，对于每日0点刷新的任务来说，这个时间一般是执行完毕之后的第二天0点。
     *
     * 通过重写[calExpireTime]来定义更新有效期的方式。
     */
    protected var mExpireTime = 0L

    /**
     * 任务结束的回调。
     */
    private var mOnTaskListeners: MutableList<OnTaskListener> = mutableListOf()

    private val mThread = Executors.newSingleThreadScheduledExecutor()

    /**
     * 表示是否异步执行任务。
     * 如果为false，表示同步执行，会阻塞当前线程，并且会使[OnTaskListener.onFinished]
     * 回调失效。
     * 如果为true，则会在新线程中执行任务，同时通过[OnTaskListener.onFinished]
     * 来返回执行结果。
     */
    private var mIsAsync = true

    private var mStatus = STATUS_NORMAL
    private var mStatusMsg: String? = null

    /**
     * 是否执行任务
     */
    open fun shouldRunTask(): Boolean {
        if (mLastExecTime == 0L || mExpireTime == 0L) {
            return true
        }
        val current = System.currentTimeMillis()
        return current >= mExpireTime
    }

    /**
     * 执行任务。对外只用调用这个方法即可。
     */
    fun run() {
        if (mIsAsync) {
            mThread.execute {
                beforeRun()
                onRunning()
                afterRun()
            }
        } else {
            beforeRun()
            onRunning()
            afterRun()
        }
    }


    /**
     * 任务执行之前的操作
     */
    protected open fun beforeRun() {
    }

    /**
     * 实际任务执行，重写此方法
     */
    abstract fun onRunning()

    /**
     * 任务执行完毕后的操作。
     */
    protected open fun afterRun() {
        mLastExecTime = System.currentTimeMillis()
        mExpireTime = calExpireTime()
        if (mOnTaskListeners.size != 0) {
            for (listener in mOnTaskListeners) {
                listener.onFinished()
            }
        }
        if (mStatus != STATUS_NORMAL) {
            Logger.e(javaClass.simpleName, mStatusMsg.toString())
        }
    }

    /**
     * 结束任务
     */
    protected fun finish(success: Boolean = false, message: String? = null) {
        if (success) {
            afterRun()
        }
    }

    /**
     * 在任务执行完毕之后，会进入一定时间的有效期。在过期时间之前的有效期内，任务不会再次执行。
     * 默认为第二天0点。重写这个方法修改过期时间。
     */
    protected open fun calExpireTime(): Long {
        val tomorrow = Calendar.getInstance()
        tomorrow.set(Calendar.HOUR_OF_DAY, 0)
        tomorrow.set(Calendar.MINUTE, 0)
        tomorrow.set(Calendar.SECOND, 0)
        tomorrow.set(Calendar.MILLISECOND, 0)
        tomorrow.add(Calendar.DATE, 1)
        return tomorrow.timeInMillis
    }


    fun getExpireTime() = mExpireTime

    fun addOnTaskListener(listener: () -> Unit) {
        val l = object : OnTaskListener {
            override fun onFinished() {
                listener()
            }
        }
        mOnTaskListeners.add(l)
    }

    fun addOnTaskListener(onTaskListener: OnTaskListener) {
        mOnTaskListeners.add(onTaskListener)
    }

    fun removeOnTaskListener(onTaskListener: OnTaskListener) {
        mOnTaskListeners.remove(onTaskListener)
    }

    fun clearOnTaskListener() {
        mOnTaskListeners.clear()
    }

    /**
     * 预估执行时间，单位：秒
     */
    open fun getEstimatedExecTime(): Int {
        return 30
    }

    /**
     * 调用该方法来设置任务是否异步执行。
     *
     * 如果为false，表示同步执行，会阻塞当前线程。
     * 如果为true，则会在新线程中执行任务，同时通过[OnTaskListener.onFinished]
     * 来返回执行结果。
     */
    open fun setAsync(async: Boolean) {
        mIsAsync = async
    }

    fun setStatus(status: Int, msg: String? = null) {
        mStatus = status
        mStatusMsg = msg
    }

//    /**
//     * 将一系列的子任务添加到待办事项中，需要调用[run]执行
//     */
//    protected fun addSubTasks(tasks: List<Action>) {
//        mSubTasks.clear()
//        mSubTasks.addAll(tasks)
//    }

    protected fun getService() = mServiceRef.get()

    protected fun closeDialogs() {
        Logger.d(TAG, "closeDialogs")
        val root = getService()?.rootInActiveWindow ?: return
        performClickAccessibilityButton(root, ID_DIALOG_OK)
//        sleep(200)
        performClickAccessibilityButton(root, ID_IMAGE_DIALOG_CLOSE)
        sleep(200)
    }

    /**
     * 同[Thread.sleep]
     */
    protected fun sleep(millis: Long) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            throw RuntimeException("This method cannot be called on main thread.")
        }
        Thread.sleep(millis)
    }

    interface OnTaskListener {
        fun onFinished()
    }

    protected class CountDown(private var count: Int) {
        private val origCount = count

        fun countDown() {
            count--
        }

        fun getCount() = count

        fun restore() {
            count = origCount
        }
    }
}