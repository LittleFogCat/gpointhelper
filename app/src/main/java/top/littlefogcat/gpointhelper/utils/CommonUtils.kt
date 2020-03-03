package top.littlefogcat.gpointhelper.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

fun showToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

/**
 * 辅助创建dialog
 */
fun makeDialog(context: Context,
               title: String,
               content: String = "",
               positiveButtonText: String = "确定",
               positiveButtonClickListener: DialogInterface.OnClickListener? = null,
               cancelable: Boolean = true
) = AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(content)
        .setPositiveButton(positiveButtonText, positiveButtonClickListener)
        .setCancelable(cancelable)
        .create()

/**
 * 跳转到第三方app
 */
fun launchThirdPartyApp(context: Context, packageName: String, notFoundHandler: () -> Unit) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    if (intent == null) {
        notFoundHandler()
        return
    }
    context.startActivity(intent)
}

@SuppressLint("NewApi")
fun getRunningActivity(context: Context): String {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val taskInfo = activityManager.getRunningTasks(1)
    val topActivity = taskInfo[0].topActivity
    return topActivity?.className ?: ""
}
