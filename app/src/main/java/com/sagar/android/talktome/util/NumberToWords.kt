package com.sagar.android.talktome.util

import java.text.DecimalFormat

@Suppress("MemberVisibilityCanBePrivate", "unused")
object NumberToWords {

    val tensNames = arrayListOf(
        "",
        " ten",
        " twenty",
        " thirty",
        " forty",
        " fifty",
        " sixty",
        " seventy",
        " eighty",
        " ninety"
    )

    val numNames = arrayListOf(
        "",
        " one",
        " two",
        " three",
        " four",
        " five",
        " six",
        " seven",
        " eight",
        " nine",
        " ten",
        " eleven",
        " twelve",
        " thirteen",
        " fourteen",
        " fifteen",
        " sixteen",
        " seventeen",
        " eighteen",
        " nineteen"
    )

    private fun convertLessThanOneThousand(number: Int): String {
        var numberToOperate = number
        var soFar: String

        if (numberToOperate % 100 < 20) {
            soFar = numNames[numberToOperate % 100]
            numberToOperate /= 100
        } else {
            soFar = numNames[numberToOperate % 10]
            numberToOperate /= 10

            soFar = tensNames[numberToOperate % 10] + soFar
            numberToOperate /= 10
        }
        if (numberToOperate == 0)
            return soFar
        return numNames[numberToOperate] + " hundred" + soFar
    }

    fun convert(number: Long): String {

        if (number == 0L) {
            return "zero"
        }

        val sNumber: String

        // pad with "0"
        val mask = "000000000000"
        val df = DecimalFormat(mask)
        sNumber = df.format(number)

        // XXXnnnnnnnnn
        val billions = Integer.parseInt(sNumber.substring(0, 3))
        // nnnXXXnnnnnn
        val millions = Integer.parseInt(sNumber.substring(3, 6))
        // nnnnnnXXXnnn
        val hundredThousands = Integer.parseInt(sNumber.substring(6, 9))
        // nnnnnnnnnXXX
        val thousands = Integer.parseInt(sNumber.substring(9, 12))

        val tradBillions: String
        tradBillions = when (billions) {
            0 -> ""
            1 -> convertLessThanOneThousand(billions) + " billion "
            else -> convertLessThanOneThousand(billions) + " billion "
        }
        var result = tradBillions

        val tradMillions: String
        tradMillions = when (millions) {
            0 -> ""
            1 -> convertLessThanOneThousand(millions) + " million "
            else -> convertLessThanOneThousand(millions) + " million "
        }
        result += tradMillions

        val tradHundredThousands: String
        tradHundredThousands = when (hundredThousands) {
            0 -> ""
            1 -> "one thousand "
            else -> convertLessThanOneThousand(hundredThousands) + " thousand "
        }
        result += tradHundredThousands

        val tradThousand: String
        tradThousand = convertLessThanOneThousand(thousands)
        result += tradThousand

        // remove extra spaces!
        return result.replace("^\\s+".toRegex(), "").replace("\\b\\s{2,}\\b".toRegex(), " ")
    }
}