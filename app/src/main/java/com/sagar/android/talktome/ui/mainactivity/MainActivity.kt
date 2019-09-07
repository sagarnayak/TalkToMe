package com.sagar.android.talktome.ui.mainactivity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.sagar.android.talktome.R
import com.sagar.android.talktome.databinding.ActivityMainBinding
import com.sagar.android.talktome.model.Word
import com.sagar.android.talktome.repository.Repository
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
    private lateinit var viewModel: ViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: WordsListAdapter

    companion object {
        const val REQUEST_VOICE_RECOGNITION = 123
    }

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
        adapter = WordsListAdapter(ArrayList())
        binding.contentMain.recyclerView.adapter = adapter
    }

    private fun getData() {
        showProgress()
        viewModel.getDictionary()
    }

    private fun gotNewWords(words: ArrayList<Word>) {
        hideProgress()
        binding.contentMain.swipeRefreshLayout.isRefreshing = false
        adapter.updateData(words)
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
        startVoiceRecognitionIntent()
    }

    private fun startVoiceRecognitionIntent(promptMessage: String? = null) {
        hideActionButton()
        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        )
        promptMessage?.let {
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, it)
        }
        startActivityForResult(intent, REQUEST_VOICE_RECOGNITION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_VOICE_RECOGNITION && resultCode == Activity.RESULT_OK) {
            data?.let {
                it.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { result ->
                    recognizedSpeech(result)
                }
            }
        } else {
            showActionButton()

            viewModel.reinitialiseVoiceRecognition()
        }
    }

    private fun recognizedSpeech(result: ArrayList<String>) {
        showProgress()
        viewModel.recognizedSpeech(
            result,
            object : Repository.VoiceMatchingCallback {
                override fun foundExactMatch(words: ArrayList<Word>, newIndex: Int) {
                    hideProgress()
                    if (!isCurrentListViewItemVisible(newIndex)) {
                        binding.contentMain.recyclerView.smoothScrollToPosition(
                            whichPositionToScrollTo(newIndex)
                        )
                        Handler().postDelayed(
                            {
                                updateWordsAndHighLight(words, newIndex)
                            },
                            1000
                        )
                    } else {
                        updateWordsAndHighLight(words, newIndex)
                    }
                }

                override fun foundPartialMatch() {
                    hideProgress()
                    startVoiceRecognitionIntent("Please say that again. we do not have full match for your input")
                }

                override fun noMatchFound(mostPreferredPhrase: String) {
                    hideProgress()
                    showMessageInDialog(
                        "$mostPreferredPhrase \n is not present in the Dictionary"
                    )
                    showActionButton()
                }

            }
        )
    }

    private fun hideActionButton() {
        binding.fab.hide()
    }

    private fun showActionButton() {
        binding.fab.show()
    }

    private fun updateWordsAndHighLight(words: ArrayList<Word>, newIndex: Int) {
        adapter.updateData(words)
        showHighLightAnimationOnList(newIndex)
        Handler().postDelayed(
            {
                showActionButton()
            },
            4000
        )
    }

    private fun whichPositionToScrollTo(newIndex: Int): Int {
        val center = adapter.itemCount / 2
        var toScroll = 0
        if (newIndex < center) {
            toScroll = newIndex - 2
            if (toScroll < 0) {
                toScroll = newIndex - 1
                if (toScroll < 0)
                    toScroll = 0
            }
        } else if (newIndex > center) {
            toScroll = newIndex + 2
            if (toScroll > adapter.itemCount - 1) {
                toScroll = newIndex + 1
                if (toScroll > adapter.itemCount - 1) {
                    toScroll = adapter.itemCount - 1
                }
            }
        }

        return toScroll
    }
}
