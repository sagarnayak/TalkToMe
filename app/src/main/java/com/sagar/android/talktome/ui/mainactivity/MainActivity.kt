package com.sagar.android.talktome.ui.mainactivity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.sagar.android.logutilmaster.LogUtil
import com.sagar.android.talktome.R
import com.sagar.android.talktome.core.KeywordAndConstant
import com.sagar.android.talktome.databinding.ActivityMainBinding
import com.sagar.android.talktome.model.Word
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

        getData()
    }

    private fun bindToViewModel() {
        viewModel.wordsInDictionary.observe(
            this,
            Observer<Event<ArrayList<Word>>> { t ->
                t?.let {
                    if (it.shouldReadContent())
                        gotNewWords(it.getContent() ?: ArrayList())
                }
            }
        )

        viewModel.error.observe(
            this,
            Observer<Event<String>> { t ->
                t?.let {
                    if (it.shouldReadContent())
                        gotError(it.getContent() ?: KeywordAndConstant.GENERIC_ERROR)
                }
            }
        )
    }

    private fun getData() {
        showProgress()
        viewModel.getDictionary()
    }

    private fun gotNewWords(words: ArrayList<Word>) {
        hideProgress()
        logUtil.logV("got ${words.size} new words")
    }

    private fun gotError(error: String) {
        hideProgress()
        showMessageInDialog(error)
    }
}
