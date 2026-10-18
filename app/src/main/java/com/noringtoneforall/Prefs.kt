package com.noringtoneforall

import android.content.Context

class Prefs(context: Context) {

    private val sp = context.applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = sp.getBoolean(KEY_ENABLED, true)
        set(value) = sp.edit().putBoolean(KEY_ENABLED, value).apply()

    val silencedCount: Int
        get() = sp.getInt(KEY_COUNT, 0)

    /** Most recent first; each entry is "epochMillis|number" (number empty when hidden). */
    val silencedLog: List<Pair<Long, String>>
        get() = sp.getString(KEY_LOG, "").orEmpty()
            .lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val sep = line.indexOf('|')
                if (sep < 0) null else line.substring(0, sep).toLongOrNull()?.let { it to line.substring(sep + 1) }
            }
            .toList()

    @Synchronized
    fun recordSilenced(number: String?, at: Long = System.currentTimeMillis()) {
        val entry = "$at|${number.orEmpty().replace("|", "").replace("\n", "")}"
        val log = (listOf(entry) + sp.getString(KEY_LOG, "").orEmpty().lines().filter { it.isNotBlank() })
            .take(MAX_LOG)
        sp.edit()
            .putString(KEY_LOG, log.joinToString("\n"))
            .putInt(KEY_COUNT, silencedCount + 1)
            .apply()
    }

    fun clearLog() {
        sp.edit().remove(KEY_LOG).remove(KEY_COUNT).apply()
    }

    companion object {
        private const val KEY_ENABLED = "enabled"
        private const val KEY_LOG = "silenced_log"
        private const val KEY_COUNT = "silenced_count"
        private const val MAX_LOG = 30
    }
}
