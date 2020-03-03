package top.littlefogcat.gpointhelper.tasks

import android.accessibilityservice.AccessibilityService
import android.app.AlarmManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import top.littlefogcat.gpointhelper.*
import top.littlefogcat.gpointhelper.tasks.TaskManager.init
import top.littlefogcat.gpointhelper.utils.Logger

/**
 * 处理任务操作的单例类。
 * 在调用之前，需要使用[init]方法进行初始化，否则会报错。
 *
 *
 */
object TaskManager {
    private var tasks: MutableList<Task> = mutableListOf()
    private lateinit var mAlarmManager: AlarmManager
    private lateinit var mPreferences: SharedPreferences

    private val TAG = TaskManager::class.java.simpleName
    private var mOnTaskFailedListener: OnTaskFailedListener? = null
    private var mRunningTask: Task? = null
    private val mHandler = Handler()
    private var mInit = false

    private val mLock = Any()

    fun init(context: Context) {
        mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        initTasks((context as AccessibilityService))
        mInit = true
    }

    private fun initTasks(service: AccessibilityService) {
        val taskGoToGP = TaskGoToGPointPage(service)
        val taskSign = TaskSign(service)
        val taskCard = TaskCard(service)
//        val taskCat = TaskCat(root)

        tasks.add(taskGoToGP)
        tasks.add(taskSign)
        tasks.add(taskCard)
//        tasks.add(taskCat)
    }

    /**
     * 执行所有需要执行的任务
     */
    @Synchronized
    fun checkAndRunTasks() {
        if (tasks.isEmpty()) {
            Logger.e(TAG, "tasks is empty")
            return
        }
        if (!mInit) {
            Logger.e(TAG, "TaskManager not init")
            return
        }
        if (mRunningTask != null) {
            Logger.v(TAG, "There is already a running task: ${mRunningTask!!::class.java.simpleName}")
            return
        }

        for (i in 0..tasks.size) {
            val task = tasks[i]
            val shouldRun = task.shouldRunTask()
            if (shouldRun) {
                Logger.d(TAG, "checkAndRunTasks: task = ${task.javaClass.simpleName}, shouldRun = $shouldRun")
                mRunningTask = task
                task.addOnTaskListener {
                    mRunningTask = null
                    if (i != tasks.size - 1) {
                        checkAndRunTasks()
                    }
                }
                task.run()
                break
            } else {
                Logger.v(TAG, "checkAndRunTasks: task = ${task.javaClass.simpleName}, shouldRun = $shouldRun")
            }
        }
    }

    //todo
    fun setOnTaskFailedListener(listener: OnTaskFailedListener) {
        mOnTaskFailedListener = listener
    }

    fun isAllTaskDone(): Boolean {
        if (tasks.isEmpty()) {
            return true
        }
        for (task in tasks) {
            if (!task.shouldRunTask()) {
                return false
            }
        }

        return true
    }

    fun addTask(taskId: String, task: Task) {
        tasks.add(task)
    }

    fun removeTask(taskId: Task) {
        tasks.remove(taskId)
    }

    fun getTasks() = tasks


    interface OnTaskFailedListener {
        fun onTaskFailed()
    }
}