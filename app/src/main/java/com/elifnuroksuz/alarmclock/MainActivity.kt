package com.elifnuroksuz.alarmclock

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.elifnuroksuz.alarmclock.databinding.ActivityMainBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var timePicker: MaterialTimePicker
    private var calendar: Calendar? = null
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateCurrentTime()
            handler.postDelayed(this, 1000) // Her saniye çalıştır
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()
        startClock() // Saat güncellemeyi başlat

        // Bildirim iznini kontrol et
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        binding.selectTime.setOnClickListener {
            timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Alarm Time")
                .build()

            timePicker.show(supportFragmentManager, "androidknowledge")
            timePicker.addOnPositiveButtonClickListener {
                val hour = timePicker.hour
                val minute = timePicker.minute
                binding.selectTime.text = if (hour >= 12) {
                    String.format("%02d:%02dPM", if (hour > 12) hour - 12 else hour, minute)
                } else {
                    String.format("%02d:%02dAM", hour, minute)
                }

                calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                Log.d("MainActivity", "Selected time: ${calendar?.time}")
            }
        }

        binding.button.setOnClickListener {
            calendar?.let {
                alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, AlarmReceiver::class.java)
                pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0, // Benzersiz requestCode
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    it.timeInMillis,
                    AlarmManager.INTERVAL_DAY, // Günlük tekrar
                    pendingIntent
                )
                Log.d("MainActivity", "Alarm set for: ${it.time}")
                Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show()
            }
        }

        binding.button2.setOnClickListener {
            val intent = Intent(this, AlarmReceiver::class.java)
            pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (::alarmManager.isInitialized.not()) {
                alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            }
            alarmManager.cancel(pendingIntent)
            Log.d("MainActivity", "Alarm canceled")
            Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startClock() {
        handler.post(updateTimeRunnable)
    }

    private fun updateCurrentTime() {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val currentTime = timeFormat.format(Calendar.getInstance().time)
        val currentDate = dateFormat.format(Calendar.getInstance().time)
        binding.currentTimeTextView.text = "Current Time: $currentTime\nDate: $currentDate"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable) // Aktivite yok olduğunda zamanlayıcıyı durdur
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Channel"
            val descriptionText = "Channel for Alarm Manager"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("androidknowledge", name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d("MainActivity", "Notification channel created")
        }
    }
}
