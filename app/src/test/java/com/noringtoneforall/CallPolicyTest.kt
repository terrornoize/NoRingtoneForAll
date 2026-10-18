package com.noringtoneforall

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallPolicyTest {

    private val contacts = setOf("+393331234567")
    private val lookup: (String) -> Boolean? = { it in contacts }

    @Test
    fun contactRings() {
        assertFalse(CallPolicy.shouldSilence(true, true, "+393331234567", lookup))
    }

    @Test
    fun unknownNumberIsSilenced() {
        assertTrue(CallPolicy.shouldSilence(true, true, "+390000000000", lookup))
    }

    @Test
    fun hiddenNumberIsSilenced() {
        assertTrue(CallPolicy.shouldSilence(true, true, null, lookup))
        assertTrue(CallPolicy.shouldSilence(true, true, "  ", lookup))
    }

    @Test
    fun disabledNeverSilences() {
        assertFalse(CallPolicy.shouldSilence(false, true, "+390000000000", lookup))
        assertFalse(CallPolicy.shouldSilence(false, true, null, lookup))
    }

    @Test
    fun outgoingNeverSilenced() {
        assertFalse(CallPolicy.shouldSilence(true, false, "+390000000000", lookup))
    }

    @Test
    fun failedLookupRings() {
        assertFalse(CallPolicy.shouldSilence(true, true, "+390000000000") { null })
    }

    @Test
    fun numberIsTrimmedBeforeLookup() {
        assertFalse(CallPolicy.shouldSilence(true, true, " +393331234567 ", lookup))
    }
}
