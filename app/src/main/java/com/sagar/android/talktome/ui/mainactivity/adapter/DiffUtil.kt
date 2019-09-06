package com.sagar.android.talktome.ui.mainactivity.adapter

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.sagar.android.talktome.model.Word

class DiffUtil(private val oldList: ArrayList<Word>, private val newList: ArrayList<Word>) :
    DiffUtil.Callback() {

    init {
        val first = oldList.size
        val last = newList.size
        Log.i("Dggb", "Dbdfbdsfb $first $last")
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].word == newList[newItemPosition].word
    }

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldWord = oldList[oldItemPosition]
        val newWord = newList[newItemPosition]

        return (
                oldWord.word == newWord.word &&
                        oldWord.frequency == newWord.frequency
                )
    }
}