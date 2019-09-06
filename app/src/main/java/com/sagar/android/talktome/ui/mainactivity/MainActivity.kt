package com.sagar.android.talktome.ui.mainactivity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.sagar.android.logutilmaster.LogUtil
import com.sagar.android.talktome.R
import com.sagar.android.talktome.databinding.ActivityMainBinding
import com.sagar.android.talktome.model.Word
import com.sagar.android.talktome.ui.mainactivity.adapter.WordsListAdapter
import com.sagar.android.talktome.util.Event
import com.sagar.android.talktome.util.SuperActivity
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class MainActivity : SuperActivity(), KodeinAware {

    override val kodein: Kodein by kodein()

    private val viewModelProvider: ViewModelProvider by instance()
    private val logUtil: LogUtil by instance()
    private lateinit var viewModel: ViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: WordsListAdapter
    private lateinit var words: ArrayList<Word>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        binding.context = this

        setSupportActionBar(binding.toolbar)

        viewModel = androidx.lifecycle.ViewModelProvider(this, viewModelProvider)
            .get(ViewModel::class.java)

        bindToViewModel()

        setUpSwipeRefreshLayout()

        setUpList()

        getData()
    }

    private fun bindToViewModel() {
        viewModel.wordsInDictionary.observe(
            this,
            Observer<Event<ArrayList<Word>>> { t ->
                t?.let {
                    if (it.shouldReadContent()) {
                        it.getContent()?.let { words ->
                            gotNewWords(words)
                        }
                    }
                }
            }
        )

        viewModel.error.observe(
            this,
            Observer<Event<String>> { t ->
                t?.let {
                    if (it.shouldReadContent()) {
                        it.getContent()?.let { error -> gotError(error) }
                    }
                }
            }
        )
    }

    private fun setUpSwipeRefreshLayout() {
        binding.contentMain.swipeRefreshLayout.setOnRefreshListener { viewModel.getDictionary() }
    }

    private fun setUpList() {
        binding.contentMain.recyclerView.layoutAnimation =
            android.view.animation.AnimationUtils.loadLayoutAnimation(
                this,
                R.anim.layout_animation_slide_from_bottom
            )
        val layoutManager = object : LinearLayoutManager(this) {
            override fun supportsPredictiveItemAnimations(): Boolean {
                return true
            }
        }
        binding.contentMain.recyclerView.layoutManager = layoutManager
        binding.contentMain.recyclerView.itemAnimator = DefaultItemAnimator()
        words = ArrayList()
        adapter = WordsListAdapter(words)
        binding.contentMain.recyclerView.adapter = adapter
    }

    private fun getData() {
        showProgress()
        viewModel.getDictionary()
    }

    private fun gotNewWords(words: ArrayList<Word>) {
        hideProgress()
        binding.contentMain.swipeRefreshLayout.isRefreshing = false
        this.words.clear()
        this.words.addAll(words)
        adapter.notifyDataSetChanged()
        binding.contentMain.recyclerView.scheduleLayoutAnimation()
    }

    private fun gotError(error: String) {
        hideProgress()
        binding.contentMain.swipeRefreshLayout.isRefreshing = false
        showMessageInDialog(error)
    }

    private fun isCurrentListViewItemVisible(position: Int): Boolean {
        val layoutManager = binding.contentMain.recyclerView.layoutManager as LinearLayoutManager
        val first = layoutManager.findFirstVisibleItemPosition()
        val last = layoutManager.findLastVisibleItemPosition()
        return position in first..last
    }

    private fun showHighLightAnimationOnList(position: Int) {
        Handler().postDelayed(
            {
                if (isCurrentListViewItemVisible(position)) {
                    (
                            binding.contentMain.recyclerView.findViewHolderForLayoutPosition(
                                position
                            )
                                    as WordsListAdapter.ViewHolder
                            ).highLight()
                }
            },
            1000
        )
    }

    fun startSpeechRecognizer(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivityForResult(
            Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            ),
            123
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            data?.let {
                it.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { result ->
                    recognizedSpeech(result[0])

                    for (
                    word in result
                    ) {
                        Toast.makeText(
                            this,
                            word,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun recognizedSpeech(result: String) {
        var initialPositionOfRecognizedWord: Int = -1
        for (word in words) {
            if (word.word.equals(ignoreCase = true, other = result)) {
                initialPositionOfRecognizedWord = words.indexOf(word)
                break
            }
        }
        if (initialPositionOfRecognizedWord != -1) {
            words[initialPositionOfRecognizedWord].frequency++
            adapter.notifyItemChanged(initialPositionOfRecognizedWord)
            words.sortByDescending { it.frequency }
            var newPosition = -1
            for (word in words) {
                if (word.word.equals(ignoreCase = true, other = result)) {
                    newPosition = words.indexOf(word)
                    break
                }
            }
            if (!isCurrentListViewItemVisible(newPosition)) {
                binding.contentMain.recyclerView.smoothScrollToPosition(newPosition)
            }
            adapter.notifyItemMoved(
                initialPositionOfRecognizedWord,
                newPosition
            )

            showHighLightAnimationOnList(newPosition)
        } else {
            showMessageInDialog(
                "$result \n is not present in the Dictionary"
            )
        }
    }
}
