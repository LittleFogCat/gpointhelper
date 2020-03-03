package top.littlefogcat.gpointhelper.utils

import android.util.Log

object Logger {
    private var mEnabled = false
    private var mLocalEnabled = false
    private var mRemoteEnabled = false

    private var mLocalFile: String? = null
    private var mRemoteUrl: String? = null

    fun v(TAG: String, msg: String) {
        Log.v(TAG, msg)
    }

    fun d(TAG: String, msg: String) {
        Log.d(TAG, msg)
    }

    fun i(TAG: String, msg: String) {
        Log.i(TAG, msg)
    }

    fun w(TAG: String, msg: String) {
        Log.w(TAG, msg)
    }

    fun e(TAG: String, msg: String) {
        Log.e(TAG, msg)
    }

    fun setEnable(enable: Boolean) {
        mEnabled = enable
    }

    fun setLocalEnabled(enable: Boolean, file: String) {
        mLocalEnabled = enable
        mLocalFile = file
    }

    fun setRemoteEnabled(enable: Boolean, url: String) {
        mRemoteEnabled = enable
        mRemoteUrl = url
    }
}