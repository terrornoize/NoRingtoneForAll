package com.noringtoneforall

/** Pure decision logic, kept free of Android types so it can be unit-tested on the JVM. */
object CallPolicy {

    /**
     * @param isContact returns true/false when the lookup succeeded, or null when it could not be
     *   performed (missing permission, provider error). An unknown result never silences: it is
     *   better to ring for a stranger than to miss a call from a contact.
     */
    fun shouldSilence(
        enabled: Boolean,
        incoming: Boolean,
        number: String?,
        isContact: (String) -> Boolean?,
    ): Boolean {
        if (!enabled || !incoming) return false
        val n = number?.trim().orEmpty()
        if (n.isEmpty()) return true // hidden / private / unknown caller
        return isContact(n) == false
    }
}
