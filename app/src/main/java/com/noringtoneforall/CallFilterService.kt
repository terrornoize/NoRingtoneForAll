package com.noringtoneforall

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

/**
 * Invoked by Telecom for every incoming call (on some ROMs only for numbers not in contacts).
 * Unknown callers are silenced (no ringtone, no vibration) but never rejected: the call still
 * shows on screen, can be answered and lands in the call log as usual.
 */
class CallFilterService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val prefs = Prefs(this)
        val incoming = callDetails.callDirection == Call.Details.DIRECTION_INCOMING
        val number = callDetails.handle?.schemeSpecificPart

        val silence = try {
            CallPolicy.shouldSilence(prefs.enabled, incoming, number) { ContactLookup.isContact(this, it) }
        } catch (e: Exception) {
            Log.e(TAG, "Screening failed, letting the call ring", e)
            false
        }

        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSilenceCall(silence)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()
        respondToCall(callDetails, response)

        Log.i(TAG, "Incoming=$incoming silenced=$silence")
        if (silence) {
            prefs.recordSilenced(number)
            TrayService.refresh(this)
        }
    }

    companion object {
        private const val TAG = "NoRingtoneForAll"
    }
}
