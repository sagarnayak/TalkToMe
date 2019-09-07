package com.sagar.android.talktome.ui.mainactivity.adapter

import android.animation.Animator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sagar.android.talktome.databinding.WordsListItemBinding
import com.sagar.android.talktome.model.Word

class WordsListAdapter(private val words: ArrayList<Word>) :
    RecyclerView.Adapter<WordsListAdapter.ViewHolder>() {

    fun updateData(newWords: ArrayList<Word>) {
        val diffUtil = DiffUtil(words, newWords)
        val diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(diffUtil)

        words.clear()
        words.addAll(newWords)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            WordsListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return words.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(words[position])
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty() && payloads[0] is Word) {
            val word = payloads[0] as Word
            holder.setWord(word.word)
            holder.setFrequency(word.frequency)
        } else {
            holder.bind(words[position])
        }
    }

    inner class ViewHolder(private val wordsListItemBinding: WordsListItemBinding) :
        RecyclerView.ViewHolder(wordsListItemBinding.root) {

        fun bind(word: Word) {
            setWord(word = word.word)
            setFrequency(word.frequency)
            wordsListItemBinding.lottieAnimation.addAnimatorListener(
                object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {

                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        wordsListItemBinding.lottieAnimation.progress = 0f
                        wordsListItemBinding.lottieAnimation.visibility = View.GONE
                    }

                    override fun onAnimationCancel(p0: Animator?) {

                    }

                    override fun onAnimationStart(p0: Animator?) {

                    }

                }
            )
        }

        fun setWord(word: String) {
            wordsListItemBinding.textViewWord.text = word
        }

        fun setFrequency(frequency: Int) {
            wordsListItemBinding.textViewFrequency.text = frequency.toString()
        }

        fun highLight() {
            wordsListItemBinding.lottieAnimation.visibility = View.VISIBLE
            wordsListItemBinding.lottieAnimation.playAnimation()
        }
    }
}