package com.sagar.android.talktome.model

data class Word(
    val word: String,
    var frequency: Int,
    var alternateSamples: ArrayList<String>
) {

    override fun equals(other: Any?): Boolean {
        if (other is Word) {
            return (
                    other.word == word &&
                            other.frequency == frequency
                    )
        }
        return false
    }

    override fun hashCode(): Int {
        return word.hashCode() * frequency
    }
}