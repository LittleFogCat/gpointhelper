package top.littlefogcat.gpointhelper

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import top.littlefogcat.gpointhelper.utils.isAccessibilitySettingsOn
import top.littlefogcat.gpointhelper.utils.launchThirdPartyApp
import top.littlefogcat.gpointhelper.utils.makeDialog
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mDialog: AlertDialog? = null
    private var mDialogNoAppFound: AlertDialog? = null
    private var mDialogTimePicker: TimePickerDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAccessibilityOn()
        initViews()
    }

    private fun initViews() {
        // 跳转到心悦app
        btnOpen.setOnClickListener {
            launchThirdPartyApp(this, TG_PACKAGE_NAME) {
                mDialogNoAppFound = makeDialog(this, "错误", "没有找到心悦俱乐部APP")
                mDialogNoAppFound?.show()
            }
        }

        cbEnableAutoSign.setOnCheckedChangeListener { v, checked ->
            if (checked) {
                val onTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                    enableAutoSign(hour, minute)
                }
                mDialogTimePicker = TimePickerDialog(this, onTimeSetListener, 0, 0, true)
                mDialogTimePicker?.show()
            } else {
                disableAutoSign()
            }
        }

    }

    /**
     * 开启自动签到。设置一个闹钟，每日定时开启心悦俱乐部app。
     */
    private fun enableAutoSign(hour: Int, minute: Int) {
        val pendingIntent = PendingIntent.getActivity(this, ALARM_REQ_CODE,
                packageManager.getLaunchIntentForPackage(TG_PACKAGE_NAME),
                PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC, nextExecTime(hour, minute), ONE_DAY, pendingIntent)
    }

    private fun disableAutoSign() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(PendingIntent.getActivity(this, ALARM_REQ_CODE,
                packageManager.getLaunchIntentForPackage(TG_PACKAGE_NAME),
                PendingIntent.FLAG_UPDATE_CURRENT))
    }

    private fun nextExecTime(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (cal.before(now)) {
            cal.add(Calendar.DATE, 1)
        }
        return cal.timeInMillis
    }

    /**
     * 检查是否开启辅助功能，没有开启就跳转到设置页面
     */
    private fun checkAccessibilityOn() {
        if (!isAccessibilitySettingsOn(this, GPointService::class.java)) {
            mDialog = makeDialog(this,
                    "需要打开辅助功能",
                    "点击确定，在设置中找到\"G分助手\"，打开辅助功能",
                    "确定",
                    DialogInterface.OnClickListener { _, _ -> startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    false)
            mDialog!!.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeDialogs()
    }

    private fun closeDialogs() {
        if (mDialog != null && mDialog!!.isShowing) {
            mDialog!!.dismiss()
        }
        if (mDialogNoAppFound != null && mDialogNoAppFound!!.isShowing) {
            mDialogNoAppFound!!.dismiss()
        }
        if (mDialogTimePicker != null && mDialogTimePicker!!.isShowing) {
            mDialogTimePicker!!.dismiss()
        }
    }

}
