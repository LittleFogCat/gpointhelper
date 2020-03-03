package top.littlefogcat.gpointhelper

private class TaskState {

    private var mLastExecTime = 0L
    private var mTomorrowMillis = 0L

    private var mSignDone = false
    private var mCardDone = false
    private var mCatDone = false

    fun isAllTaskDone(): Boolean {
        if (mLastExecTime == 0L || mTomorrowMillis == 0L) {
            return false
        }

        val current = System.currentTimeMillis()
        if (current >= mTomorrowMillis) {
            return false
        }

        return mSignDone && mCardDone && mCatDone
    }



}