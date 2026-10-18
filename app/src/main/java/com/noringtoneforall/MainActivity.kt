package com.noringtoneforall

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import java.util.Date

class MainActivity : Activity() {

    private lateinit var prefs: Prefs
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = dp(16)
            setPadding(pad, pad, pad, pad)
        }
        setContentView(ScrollView(this).apply {
            fitsSystemWindows = true
            addView(content)
        })
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        render()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        render()
    }

    private fun render() {
        content.removeAllViews()

        content.addView(TextView(this).apply {
            text = getString(R.string.intro)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setPadding(0, 0, 0, dp(16))
        })

        content.addView(Switch(this).apply {
            text = getString(R.string.filter_enabled)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            isChecked = prefs.enabled
            setPadding(0, dp(8), 0, dp(8))
            setOnCheckedChangeListener { _, checked -> prefs.enabled = checked }
        })

        header(R.string.setup)
        statusRow(R.string.role_label, hasRole(), R.string.role_action) { requestRole() }
        statusRow(R.string.contacts_label, granted(Manifest.permission.READ_CONTACTS), R.string.grant_action) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQ_PERMS)
        }
        statusRow(R.string.battery_label, ignoresBatteryOptimizations(), R.string.battery_action) {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName"))
            )
        }

        header(R.string.recent_silenced)
        val log = prefs.silencedLog
        if (log.isEmpty()) {
            content.addView(TextView(this).apply { text = getString(R.string.none_yet) })
        } else {
            for ((at, number) in log) {
                content.addView(TextView(this).apply {
                    val shown = number.ifEmpty { getString(R.string.hidden_number) }
                    val time = DateFormat.getMediumDateFormat(context).format(Date(at)) + " " +
                        DateFormat.getTimeFormat(context).format(Date(at))
                    text = "$time  ·  $shown"
                    setPadding(0, dp(4), 0, dp(4))
                })
            }
            content.addView(Button(this).apply {
                text = getString(R.string.clear_log)
                setOnClickListener {
                    prefs.clearLog()
                    render()
                }
            })
        }
    }

    private fun header(textRes: Int) {
        content.addView(TextView(this).apply {
            text = getString(textRes)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            isAllCaps = true
            alpha = 0.7f
            setPadding(0, dp(24), 0, dp(8))
        })
    }

    private fun statusRow(labelRes: Int, ok: Boolean, actionRes: Int, action: () -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(4), 0, dp(4))
        }
        row.addView(TextView(this).apply {
            text = (if (ok) "✅  " else "❌  ") + getString(labelRes)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        row.addView(Button(this).apply {
            text = getString(actionRes)
            visibility = if (ok) View.GONE else View.VISIBLE
            setOnClickListener { action() }
        })
        content.addView(row)
    }

    private fun hasRole(): Boolean = getSystemService(RoleManager::class.java).isRoleHeld(RoleManager.ROLE_CALL_SCREENING)

    private fun requestRole() {
        val rm = getSystemService(RoleManager::class.java)
        if (rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            @Suppress("DEPRECATION")
            startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING), REQ_ROLE)
        } else {
            startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        }
    }

    private fun granted(permission: String) = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun ignoresBatteryOptimizations() =
        getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName)

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    companion object {
        private const val REQ_ROLE = 1
        private const val REQ_PERMS = 2
    }
}
