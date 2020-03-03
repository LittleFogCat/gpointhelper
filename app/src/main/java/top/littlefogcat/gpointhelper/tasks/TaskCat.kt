package top.littlefogcat.gpointhelper.tasks

import android.accessibilityservice.AccessibilityService

class TaskCat(service: AccessibilityService) :  Task(service)  {
    override fun onRunning() {
        // TODO not implemented
    }

    companion object {
        const val TAG = "TaskCat"
    }
}