package dev.qzzxcq.volskip

import android.content.Context

object Prefs {
    private const val FILE = "volskip"
    private fun sp(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun longPressMs(c: Context): Long = sp(c).getLong("long_ms", 450L)
    fun setLongPressMs(c: Context, v: Long) = sp(c).edit().putLong("long_ms", v).apply()

    fun vibrate(c: Context): Boolean = sp(c).getBoolean("vibrate", true)
    fun setVibrate(c: Context, v: Boolean) = sp(c).edit().putBoolean("vibrate", v).apply()
}
