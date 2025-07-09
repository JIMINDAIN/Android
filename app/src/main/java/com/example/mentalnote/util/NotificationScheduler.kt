package com.example.mentalnote.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.os.Build
import android.media.AudioAttributes
import android.net.Uri
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.example.mentalnote.R
import com.example.mentalnote.USER_BED_TIME
import com.example.mentalnote.USER_WORK_END_TIME
import com.example.mentalnote.dataStore
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import android.graphics.BitmapFactory

class NotificationScheduler {

    companion object {
        const val CHANNEL_ID = "mental_note_channel"
        const val CHANNEL_NAME = "Mental Note Reminders"
        const val NOTIFICATION_ID_WORK_END = 1
        const val NOTIFICATION_ID_BED_TIME = 2

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) // 기본 알람 소리 사용

                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for Mental Note app"
                    setSound(soundUri, audioAttributes) // 알림 소리 설정
                }
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        suspend fun scheduleNotifications(context: Context) {
            val prefs = context.dataStore.data.first()
            val workEndTime = prefs[USER_WORK_END_TIME]
            val bedTime = prefs[USER_BED_TIME]

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            workEndTime?.let { time ->
                val parts = time.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toInt()
                    val minute = parts[1].toInt()
                    scheduleAlarm(context, alarmManager, hour, minute, NOTIFICATION_ID_WORK_END, "퇴근 시간입니다! 오늘 하루는 어떠셨나요?", "오늘의 감정을 기록해 보세요.")
                }
            }

            bedTime?.let { time ->
                val parts = time.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toInt()
                    val minute = parts[1].toInt()
                    Log.d("NotificationScheduler", "Scheduling Bed Time Alarm for $hour:$minute")
                    scheduleAlarm(context, alarmManager, hour, minute, NOTIFICATION_ID_BED_TIME, "잠자리에 들 시간입니다!", "오늘의 감정을 정리하고 편안한 밤을 보내세요.")
                }
            }
        }

        fun cancelNotifications(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val workEndIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(context, NOTIFICATION_ID_WORK_END, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
            alarmManager.cancel(workEndIntent)

            val bedTimeIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(context, NOTIFICATION_ID_BED_TIME, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
            alarmManager.cancel(bedTimeIntent)
        }

        private fun scheduleAlarm(context: Context, alarmManager: AlarmManager, hour: Int, minute: Int, notificationId: Int, title: String, message: String) {
            val calendar: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1) // If time is already passed for today, schedule for tomorrow
                }
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            Log.d("NotificationScheduler", "Scheduling alarm for ID $notificationId at ${sdf.format(calendar.time)} (millis: ${calendar.timeInMillis})")

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("notificationId", notificationId)
                putExtra("title", title)
                putExtra("message", message)
            }
            val pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notificationId", 0)
        val title = intent.getStringExtra("title") ?: "알림"
        val message = intent.getStringExtra("message") ?: "새로운 알림이 있습니다."
        Log.d("AlarmReceiver", "Alarm received! ID: $notificationId, Title: $title")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 알림 클릭 시 앱으로 이동하는 Intent 설정
        val mainActivityIntent = Intent(context, com.example.mentalnote.MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.mindnote_logo) // 작은 아이콘 (상단 바)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.mindnote_logo)) // 큰 아이콘 (알림 내용 옆)
            .setContentIntent(contentPendingIntent) // 알림 클릭 시 앱으로 이동

        notificationManager.notify(notificationId, builder.build())
    }
}