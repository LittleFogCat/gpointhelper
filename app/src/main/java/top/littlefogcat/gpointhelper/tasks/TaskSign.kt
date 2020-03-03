package top.littlefogcat.gpointhelper.tasks

import android.accessibilityservice.AccessibilityService
import top.littlefogcat.gpointhelper.ID_DIALOG_OK
import top.littlefogcat.gpointhelper.ID_SIGN
import top.littlefogcat.gpointhelper.utils.Logger
import top.littlefogcat.gpointhelper.utils.findAccessibilityNodeInfoByIdAndText
import top.littlefogcat.gpointhelper.utils.performClick

class TaskSign(service: AccessibilityService) : Task(service) {
    companion object {
        const val TAG = "TaskSign"
    }
private lateinit var mService:AccessibilityService
    override fun onRunning() {
        Logger.d(TAG, "--------- 签到任务执行中 ---------")
        if (!this::mService.isInitialized) {
            mService = getService() ?: return
        }
        val btnSign = findAccessibilityNodeInfoByIdAndText(mService.rootInActiveWindow, ID_SIGN)
        if (btnSign == null) {
            Logger.w(TAG, "doSign: 没有找到签到入口")
            return
        }

        if ("已签到" == btnSign.text) {
            Logger.i(TAG, "已签到，跳过")
        } else {
            Logger.i(TAG, "执行签到")
            btnSign.performClick()
            sleep(3000)
            val btnOK = findAccessibilityNodeInfoByIdAndText(mService.rootInActiveWindow, ID_DIALOG_OK, "确定")
            btnOK?.performClick()
            sleep(1000)
        }
    }
}