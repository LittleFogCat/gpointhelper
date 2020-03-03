package top.littlefogcat.gpointhelper

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.appcompat.app.AlertDialog
import top.littlefogcat.gpointhelper.tasks.TaskManager
import top.littlefogcat.gpointhelper.utils.*

class GPointService : AccessibilityService() {

    private lateinit var mDialog: AlertDialog

    override fun onServiceConnected() {
        Log.i(TAG, "onServiceConnected: GPointService")
        TaskManager.init(this)
        mDialog = AlertDialog.Builder(this)
                .setTitle("重要提示")
                .setMessage("即将进行自动执行任务，请勿手动操作手机，以免执行失败")
                .create()
    }


    /**
     * 检测到事件。
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventType = AccessibilityEvent.eventTypeToString(event.eventType)
//        Log.v(TAG, "onAccessibilityEvent: $eventType")

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            TaskManager.checkAndRunTasks()
        }
    }

//    /**
//     * 流程：签到 -> 领周卡月卡 -> 喂猫
//     */
//    private fun startGetPoints() {
//        mHandler.postDelayed({ gotoGPointPage() }, 1500)
//        if (!mSignDone) mHandler.postDelayed({ doSign() }, 3000)
//        if (!mCardDone) {
//            mHandler.postDelayed({ gotoPointCardPage() }, 4500)
//            mHandler.postDelayed({ doGetCardPoints() }, 6000)
//        }
//    }

    /**
     * 首先检查是否在主页面，没有其他弹出框的遮盖
     */
    private fun checkIfAtMainPage() {
        val runningActivity = getRunningActivity(this)
        Logger.d(TAG, "runningActivity: $runningActivity")

        // 如果有升级按钮，那么取消升级
        val btnCancelUpdate = findAccessibilityNodeInfoByIdAndText(rootInActiveWindow, ID_CANCEL_UPDATE)
        if (btnCancelUpdate != null) {
            btnCancelUpdate.performClick()
            return
        }


        val txtGPoint = findAccessibilityNodeInfoByIdAndText(rootInActiveWindow, ID_BOTTOM_TAB, TXT_G_POINT)
        if (txtGPoint == null) { // 没有找到G分按钮，说明不在主页面

        } else {
            gotoGPointPage()
        }
    }


    /**
     * 跳转到G分界面
     */
    private fun gotoGPointPage() {
        val root = rootInActiveWindow
        val accessibilityNodeInfoList = root.findAccessibilityNodeInfosByViewId("com.tencent.tgclub:id/bottom_tab_txt")
        for (node in accessibilityNodeInfoList) {
            val cs = node.text
            if (cs is String && "G分".contentEquals(cs)) {
                Log.d(TAG, "gotoGPointPage")
                node.parent.performClick()
            }
        }
    }

    private fun doSign() {
        val root = rootInActiveWindow
        val accessibilityNodeInfoList = root.findAccessibilityNodeInfosByViewId("com.tencent.tgclub:id/new_integral_sign_btn")
        val c = accessibilityNodeInfoList.size
        if (c == 0) {
            Log.e(TAG, "doSign: 没有找到签到入口")
            return
        } else {
            for (nodeInfo in accessibilityNodeInfoList) {
                Log.d(TAG, "doSign: $nodeInfo")
            }
        }
        val btnSign = accessibilityNodeInfoList[0]
        Log.d(TAG, "doSign")
        if ("已签到" == btnSign.text.toString()) {
            showToast(applicationContext, "已签到，跳过")
        } else {
            btnSign.performClick()
        }
    }

    private fun gotoPointCardPage() {
        val root = rootInActiveWindow
        val accessibilityNodeInfoList = root.findAccessibilityNodeInfosByViewId("com.tencent.tgclub:id/gain_integral_title")
        for (nodeInfo in accessibilityNodeInfoList) {
            if (nodeInfo != null && nodeInfo.text != null && "理财礼卡" == nodeInfo.text.toString()) {
                nodeInfo.parent.performClick()
                break
            }
        }
    }

    private fun doGetCardPoints() {
        val root = rootInActiveWindow
        val c = root.childCount
        var webView: AccessibilityNodeInfo? = null
        for (i in 0 until c) {
            val child = root.getChild(i)
            //            Log.d(TAG, "doGetCardPoints: " + child);
            if (child.className.toString().endsWith("WebView")) {
                webView = child
                break
            }
        }
        if (webView == null) return
        Log.i(TAG, "doGetCardPoints: 找到webView")

        val webViewChildCount = webView.childCount
        if (webViewChildCount == 0) {
            Log.w(TAG, "doGetCardPoints: web view has no child")
            traverse(root)
            return
        } else {
            Log.i(TAG, "doGetCardPoints: $webViewChildCount")
        }
        for (i in 0 until webViewChildCount) {
            val child = webView.getChild(i)
            Log.d(TAG, "doGetCardPoints: " + child.text)
            Log.d(TAG, "doGetCardPoints: " + child.contentDescription)
        }
    }

    /**
     * 遍历所有子结点
     *
     * @param root
     */
    private fun traverse(root: AccessibilityNodeInfo) {
        val c = root.childCount
        val log = StringBuilder("traverse: id = " + root.viewIdResourceName + ", class = " + root.className + ", clickable = " + root.isClickable)
        if (root.text != null) {
            log.append(", text = ${root.text}")
        }
        if (c != 0) {
            log.append(", children = [")
            for (i in 0 until c) {
                val child = root.getChild(i)
                log.append(child.viewIdResourceName).append(", ")
            }
            log.delete(log.length - 2, log.length)
            log.append("]")
        }
        Log.d(TAG, "traverse: $log")
        for (i in 0 until c) {
            val child = root.getChild(i)
            traverse(child)
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "onInterrupt: GPointService")
    }

    companion object {
        private val TAG = GPointService::class.java.simpleName
    }
}
