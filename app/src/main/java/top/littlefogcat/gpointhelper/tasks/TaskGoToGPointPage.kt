package top.littlefogcat.gpointhelper.tasks

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import top.littlefogcat.gpointhelper.ID_BOTTOM_TAB
import top.littlefogcat.gpointhelper.TXT_G_POINT
import top.littlefogcat.gpointhelper.utils.Logger
import top.littlefogcat.gpointhelper.utils.findAccessibilityNodeInfoByIdAndText
import top.littlefogcat.gpointhelper.utils.performClick

class TaskGoToGPointPage(service: AccessibilityService) : Task(service) {
    private lateinit var mService: AccessibilityService
    override fun onRunning() {
        Logger.d(TAG, "TaskGoToGPointPage开始执行...")
        if (!this::mService.isInitialized) {
            mService = getService() ?: return
        }
        var txtGP: AccessibilityNodeInfo? = null
        val counter = CountDown(3)
        while (counter.getCount() > 0) {
            txtGP = findAccessibilityNodeInfoByIdAndText(mService.rootInActiveWindow, ID_BOTTOM_TAB, TXT_G_POINT)
            if (txtGP != null) {
                break
            }
            Logger.d(TAG, "没有找到G分按钮，重试...")
            sleep(3000)
            counter.countDown()
        }
        if (txtGP == null) {
            Logger.e(TAG, "没有找到G分按钮，任务失败。")
            setStatus(STATUS_ERROR, "没有找到G分按钮，任务失败。")
            return
        }
        Logger.i(TAG, "执行跳转到G分界面任务")
        txtGP.parent?.performClick()
    }

    override fun beforeRun() {
//        closeDialogs()
    }
}
