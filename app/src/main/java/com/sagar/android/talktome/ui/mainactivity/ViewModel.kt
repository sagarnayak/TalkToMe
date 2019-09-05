package com.sagar.android.talktome.ui.mainactivity

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.sagar.android.talktome.model.Word
import com.sagar.android.talktome.repository.Repository
import com.sagar.android.talktome.util.Event

class ViewModel(private val repository: Repository) : ViewModel() {

    val wordsInDictionary: MediatorLiveData<Event<ArrayList<Word>>> = MediatorLiveData()
    val error: MediatorLiveData<Event<String>> = MediatorLiveData()

    init {
        bindToRepo()
    }

    fun getDictionary() {
        repository.getDictionary()
    }

    private fun bindToRepo() {
        wordsInDictionary.addSource(
            repository.wordsInDictionary
        ) { t -> wordsInDictionary.postValue(t) }

        error.addSource(
            repository.error
        ) { t -> error.postValue(t) }
    }
}