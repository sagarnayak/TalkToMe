package com.sagar.android.talktome

import com.sagar.android.talktome.util.NumberUtil
import org.junit.Assert
import org.junit.Test

class NumberUtilTest {

    @Test
    fun assertDoesHaveNumberInString() {
        Assert.assertTrue(
            NumberUtil.doesHaveDigit("2nd")
        )
    }

    @Test
    fun assertDoesNotHaveNumberInString() {
        Assert.assertFalse(
            NumberUtil.doesHaveDigit("test")
        )
    }

    @Test
    fun testOrdinal() {
        Assert.assertEquals(
            "second",
            NumberUtil.toOrdinal("2")
        )
    }

    @Test
    fun assertConvertNumberToWords() {
        var found = false
        val result = NumberUtil.convertToWordRepresentation("2nd street")

        for (res in result) {
            if (res.equals("second street", true)) {
                found = true
                break
            }
        }

        Assert.assertEquals(
            result.toString(),
            true,
            found
        )
    }
}