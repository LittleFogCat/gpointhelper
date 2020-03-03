package top.littlefogcat.gpointhelper.tasks

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import top.littlefogcat.gpointhelper.*
import top.littlefogcat.gpointhelper.utils.*

class TaskCard(mService: AccessibilityService) : Task(mService) {
    companion object {
        const val TAG = "TaskCard"
        const val PAGE_UNKNOWN = -1
        const val PAGE_GP = 0
        const val PAGE_CARD = 1
        const val PAGE_OTHER = 2
    }

    init {
        setAsync(true)
    }

    private var mCurrentPage = PAGE_UNKNOWN
    private var mCountDown = CountDown(3)

    private lateinit var mService: AccessibilityService

    override fun onRunning() {
//        Logger.v(TAG, "onRunning: ")
        if (!this::mService.isInitialized) {
            mService = getService() ?: return
        }
        // 先检查当前页面
        mCurrentPage = checkCurrentPage()
        when (mCurrentPage) {
            PAGE_GP -> {
                Log.d(TAG, "onRunning: PAGE_GP")
                // 在G分界面，点击理财礼卡图标进入
                val btnCard = findAccessibilityNodeInfoByIdAndText(mService.rootInActiveWindow, ID_BTN_CARD, TXT_CARD_TITLE)
                if (btnCard == null) {
                    Log.d(TAG, "onRunning: btnCard == null")
                    mService.dispatchGesture(500, 500, 1500, 500)
                    sleep(1000)
                }
                btnCard?.parent?.performClick()
                sleep(3000)
                onRunning()
            }
            PAGE_CARD -> {
                Log.d(TAG, "onRunning: PAGE_CARD")
                // 在理财礼卡界面
                sleep(3000) // 等待3秒加载网页
                val loadSuccess = isLoadSuccess()
                if (!loadSuccess && mCountDown.getCount() > 0) { // 加载失败，返回重试
                    Log.d(TAG, "onRunning: 加载失败，重试...")
                    mCountDown.countDown()
                    performClickAccessibilityButton(mService.rootInActiveWindow, ID_WEB_VIEW_BACK)
                } else if (!loadSuccess) { // 加载失败，重试次数超出
                    Log.w(TAG, "onRunning: 加载失败，退出执行")
                    mCountDown.restore()
                    finish()
                } else { // 加载成功
                    receivePoints()
                }
            }
            PAGE_OTHER -> {

                Log.d(TAG, "onRunning: PAGE_OTHER")
            }
            PAGE_UNKNOWN -> {
                Log.d(TAG, "onRunning: PAGE_UNKNOWN")
                finish()
            }
        }
    }

    override fun afterRun() {
        super.afterRun()
        mCountDown.restore()
    }

    /**
     * 检查当前所处的界面
     */
    private fun checkCurrentPage(): Int {
        // 在理财礼卡界面
        val webViewTitle = findAccessibilityNodeInfoByIdAndText(mService.rootInActiveWindow, ID_WEB_VIEW_TITLE, TXT_CARD_TITLE)
        if (webViewTitle != null) {
            return PAGE_CARD
        }

        // 在签到界面
        val btnSign = findAccessibilityNodeInfoByIdAndText(mService.rootInActiveWindow, ID_SIGN)
        if (btnSign != null) {
            return PAGE_GP
        }

        // 在除签到以外的其他界面
        val gpBottomTab = findAccessibilityNodeInfoByIdAndText(mService.rootInActiveWindow, ID_BOTTOM_TAB, TXT_G_POINT)
        if (gpBottomTab != null) {
            return PAGE_OTHER
        }

        return PAGE_UNKNOWN
    }

    private var mNodeList = mutableListOf<AccessibilityNodeInfo>()
    /**
     * 判断网页是否加载完毕
     */
    private fun isLoadSuccess(): Boolean {
        Log.d(TAG, "isLoadSuccess: 1")

        val root = mService.rootInActiveWindow
        fun traverse(node: AccessibilityNodeInfo) {
            Log.d(TAG, "traverse: $node")
            mNodeList.add(node)
            for (i in 0..node.childCount) {
                traverse(node.getChild(i))
            }
        }
        traverse(root)

        Log.d(TAG, "isLoadSuccess: $mNodeList")
        var textView: AccessibilityNodeInfo? = null
        for (node in mNodeList) {
            if (node.text != null && node.text.startsWith("我的G分")) {
                textView = node
            }
        }

        Log.d(TAG, "isLoadSuccess: 3 $textView")
        if (textView?.text != "我的G分：0") {
            return true
        }
        return false
    }

    private fun receivePoints() {
        Log.d(TAG, "receivePoints: ")
        val listCanGet = mService.rootInActiveWindow.findAccessibilityNodeInfosByText("领取收益")
        val listGot = mService.rootInActiveWindow.findAccessibilityNodeInfosByText("已领取")

        for (nodeInfo in listCanGet) {
            nodeInfo.performClick()
            sleep(3000)
            performClickAccessibilityButton(mService.rootInActiveWindow, null, "确定")
            sleep(3000)
        }

        val bought = listCanGet.size + listGot.size // 已经买了的卡数量
        if (bought == 2) return // 最多买2张卡

        var points = 0
        traverse(mService.rootInActiveWindow) { v ->
            val view = v as AccessibilityNodeInfo
            val len = "我的G分：".length
            points = view.text.substring(len + 1).toInt()
        }
        val prices = intArrayOf(20, 80, 300, 600)
        // todo buy cards
    }

}