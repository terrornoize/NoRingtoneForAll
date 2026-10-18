package com.noringtoneforall

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract.PhoneLookup
import android.util.Log

object ContactLookup {

    /** True if [number] matches a saved contact, false if not, null if the lookup is impossible. */
    fun isContact(context: Context, number: String): Boolean? {
        if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        // PhoneLookup normalises formats (+39 prefix, spaces, dashes...) the same way the dialer does.
        val uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        return try {
            context.contentResolver.query(uri, arrayOf(PhoneLookup._ID), null, null, null)
                ?.use { it.count > 0 }
        } catch (e: Exception) {
            Log.w(TAG, "Contact lookup failed", e)
            null
        }
    }

    private const val TAG = "NoRingtoneForAll"
}
