package com.sagar.android.talktome.ui.mainactivity.adapter

import androidx.recyclerview.widget.DiffUtil
import com.sagar.android.talktome.model.Word

class DiffUtil(private val oldList: ArrayList<Word>, private val newList: ArrayList<Word>) :
    DiffUtil.Callback() {

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

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return newList[newItemPosition]
    }
}