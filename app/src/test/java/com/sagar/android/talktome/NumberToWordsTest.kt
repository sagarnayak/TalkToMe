package com.sagar.android.talktome

import com.sagar.android.talktome.util.NumberToWords
import org.junit.Assert
import org.junit.Test

class NumberToWordsTest {

    @Test
    fun assert_convertLessThenHundred() {
        Assert.assertEquals(
            "successfully converted 4 to four",
            "four",
            NumberToWords.convert(4L)
        )
    }
}