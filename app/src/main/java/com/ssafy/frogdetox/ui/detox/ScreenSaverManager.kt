package com.ssafy.frogdetox.ui.detox

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.ssafy.frogdetox.common.getTodayInMillis
import com.ssafy.frogdetox.service.receiver.ScreenSaverReceiver
import java.util.Calendar
import kotlin.math.min

object ScreenSaverManager{
    @SuppressLint("ScheduleExactAlarm")
    fun setScreenSaverAlarm(context: Context, hour: Int?, minute : Int?){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ScreenSaverReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 100001, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        Calendar.getInstance().apply {
            if (hour != null && minute != null) {

                // 지금보다 늦으면 다음 날로 설정
                if(com.ssafy.frogdetox.common.getTimeInMillis(hour, minute) < getTodayInMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                    Log.d("싸피", "날짜 하루 늘렸고")
                }

                set(Calendar.HOUR_OF_DAY, hour)

                set(Calendar.MINUTE, minute)

                set(Calendar.SECOND,0)

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                Log.d("싸피", "setScreenSaverAlarm: time $hour $minute 에 설정했쇼")
            }
        }
    }

    fun cancelScreenSaverAlarm(context: Context){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ScreenSaverReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 100001, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}