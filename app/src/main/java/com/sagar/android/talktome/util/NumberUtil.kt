package com.sagar.android.talktome.util

import com.ibm.icu.text.RuleBasedNumberFormat
import java.util.*
import kotlin.collections.ArrayList

object NumberUtil {

    fun doesHaveDigit(input: String): Boolean {
        return input.matches(Regex(".*\\d.*"))
    }

    fun convertToWordRepresentation(input: String): ArrayList<String> {
        val words = input.split(" ")

        val result:ArrayList<String> = ArrayList()
        var resultInWords = ""
        var resultOrdinal = ""

        for (wd in words) {
            resultInWords += if (doesHaveDigit(wd))
                " " + numberToWord(sanitiseNumber(wd))
            else
                " $wd"

            resultOrdinal += if (doesHaveDigit(wd))
                " " + toOrdinal(sanitiseNumber(wd))
            else
                " $wd"
        }

        result.add(resultInWords.trim())
        result.add(resultOrdinal.trim())
        return result
    }

    fun sanitiseNumber(input: String): String {
        var numberToOperate = input
        numberToOperate = numberToOperate.replace("th", "", ignoreCase = true)
        numberToOperate = numberToOperate.replace("st", "", ignoreCase = true)
        numberToOperate = numberToOperate.replace("nd", "", ignoreCase = true)

        return numberToOperate
    }

    fun numberToWord(number: String): String {
        return NumberToWords.convert(number.toLong())
    }

    fun toOrdinal(input: String): String {
        val nf = RuleBasedNumberFormat(
            Locale.ENGLISH,
            RuleBasedNumberFormat.SPELLOUT
        )

        return nf.format(input.toLong(), "%spellout-ordinal")
    }
}