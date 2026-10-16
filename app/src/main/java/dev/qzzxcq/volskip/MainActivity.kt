package dev.qzzxcq.volskip

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (20 * resources.displayMetrics.density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
        }

        root.addView(TextView(this).apply {
            text = "VolSkip"
            textSize = 26f
        })
        root.addView(TextView(this).apply {
            text = "Удержание Vol+ — следующий трек\nУдержание Vol− — предыдущий\nКороткое нажатие — громкость"
            textSize = 15f
            setPadding(0, pad / 2, 0, pad)
        })

        status = TextView(this).apply { textSize = 15f }
        root.addView(status)

        root.addView(Button(this).apply {
            text = "Открыть настройки спец. возможностей"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })

        val delayLabel = TextView(this).apply { setPadding(0, pad, 0, 0) }
        fun updateLabel(ms: Long) { delayLabel.text = "Время удержания: $ms мс" }
        updateLabel(Prefs.longPressMs(this))
        root.addView(delayLabel)

        root.addView(SeekBar(this).apply {
            max = 900 // 200..1100 мс
            progress = (Prefs.longPressMs(this@MainActivity) - 200).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    val ms = 200L + p
                    updateLabel(ms)
                    if (fromUser) Prefs.setLongPressMs(this@MainActivity, ms)
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        })

        root.addView(Switch(this).apply {
            text = "Вибрация при переключении"
            isChecked = Prefs.vibrate(this@MainActivity)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, pad, 0, 0)
            setOnCheckedChangeListener { _, v -> Prefs.setVibrate(this@MainActivity, v) }
        })

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.contains("$packageName/") == true
        status.text = if (enabled) "✅ Сервис включён" else "❌ Сервис выключен — включи его в настройках"
    }
}
