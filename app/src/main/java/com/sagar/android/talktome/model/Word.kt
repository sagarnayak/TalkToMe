package com.sagar.android.talktome.model

data class Word(
    val word: String,
    var frequency: Int,
    var alternateSamples: ArrayList<String>
)