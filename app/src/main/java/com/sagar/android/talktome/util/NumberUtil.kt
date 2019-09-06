package com.sagar.android.talktome.util

object NumberUtil {

    fun numberToWord(number: String): String {
        var numberToOperate = number
        numberToOperate = numberToOperate.replace("th", "", ignoreCase = true)
        numberToOperate = numberToOperate.replace("st", "", ignoreCase = true)
        numberToOperate = numberToOperate.replace("nd", "", ignoreCase = true)

        return NumberToWords.convert(numberToOperate.toLong())
    }
}