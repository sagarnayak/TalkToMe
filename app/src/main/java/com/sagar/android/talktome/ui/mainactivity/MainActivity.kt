package com.sagar.android.talktome.ui.mainactivity

import android.os.Bundle
import android.os.Handler
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

        Handler().postDelayed(
            {
                val wordTemp = words[2]
                words.removeAt(2)
                words.add(1, wordTemp)

                adapter.notifyItemMoved(2, 1)

                showHighLightAnimationOnList(1)
            },
            5000
        )

        Handler().postDelayed(
            {
                val wordTemp = words[2]
                words.removeAt(2)
                words.add(1, wordTemp)

                adapter.notifyItemMoved(2, 1)

                showHighLightAnimationOnList(1)
            },
            10000
        )
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
        words.sortByDescending { it.frequency }
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
}
