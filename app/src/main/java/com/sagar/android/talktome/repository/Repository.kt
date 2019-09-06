package com.sagar.android.talktome.repository

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sagar.android.logutilmaster.LogUtil
import com.sagar.android.talktome.core.KeywordAndConstant
import com.sagar.android.talktome.model.Word
import com.sagar.android.talktome.repository.retrofit.ApiInterface
import com.sagar.android.talktome.util.Event
import com.sagar.android.talktome.util.NumberUtil
import com.sagar.android.talktome.util.StatusCode
import com.sagar.android.talktome.util.SuperRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

class Repository(
    val apiInterface: ApiInterface,
    val logUtil: LogUtil,
    val pref: SharedPreferences
) : SuperRepository() {

    public val wordsInDictionary: MutableLiveData<Event<ArrayList<Word>>> = MutableLiveData()
    public val error: MutableLiveData<Event<String>> = MutableLiveData()

    public val words: ArrayList<Word> = ArrayList()

    private var partialMatchPhrase: String = ""

    public fun getDictionary() {
        apiInterface.getDictionary()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                object : io.reactivex.Observer<Response<ResponseBody>> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(t: Response<ResponseBody>) {
                        when (t.code()) {
                            StatusCode.OK.code -> {
                                t.body()?.let {
                                    val listType = object : TypeToken<ArrayList<Word>>() {}.type
                                    val jsonObjBody: JSONObject = JSONObject(it.string())
                                    val data = Gson().fromJson<ArrayList<Word>>(
                                        jsonObjBody.getJSONArray("dictionary").toString(),
                                        listType
                                    )
                                    for (word in data) {
                                        word.alternateSamples = ArrayList()
                                        word.alternateSamples.addAll(
                                            NumberUtil.convertToWordRepresentation(word.word)
                                        )

                                        logUtil.logV("${word.word} = ${word.alternateSamples}")
                                    }
                                    data.sortByDescending { word -> word.frequency }
                                    words.clear()
                                    words.addAll(data)
                                    wordsInDictionary.postValue(
                                        Event(words)
                                    )
                                } ?: run {
                                    t.errorBody()?.let {
                                        error.postValue(
                                            Event(
                                                getErrorMessage(it)
                                            )
                                        )
                                    } ?: run {
                                        error.postValue(
                                            Event(
                                                KeywordAndConstant.GENERIC_ERROR
                                            )
                                        )
                                    }
                                }
                            }
                            else -> {
                                error.postValue(
                                    Event(
                                        KeywordAndConstant.GENERIC_ERROR
                                    )
                                )
                            }
                        }
                    }

                    override fun onError(e: Throwable) {
                        error.postValue(
                            Event(
                                getErrorMessage(e)
                            )
                        )
                    }

                }
            )
    }

    public interface NumberMatchingCallback {
        fun foundExactMatch(word: Word, initialPosition: Int, finalPosition: Int)
        fun foundPartialMatch()
        fun noMatchFound(mostPreferredPhrase: String)
    }

    public fun recognizedVoice(
        voiceSamples: ArrayList<String>,
        callback: NumberMatchingCallback
    ) {
        val wordsToWorkOn: ArrayList<Word> = ArrayList()
        if (partialMatchPhrase != "") {
            for (word in words) {
                alternateWordSampleLoop@ for (alternateSample in word.alternateSamples) {
                    if (alternateSample.contains(partialMatchPhrase, true)) {
                        wordsToWorkOn.add(word)
                        break@alternateWordSampleLoop
                    }
                }
            }
        } else {
            wordsToWorkOn.addAll(words)
        }

        var foundAtIndex = -1

        for (voiceSample in voiceSamples) {
            for (word in wordsToWorkOn) {
                if (word.word.equals(voiceSample, true)) {
                    foundAtIndex = words.indexOf(word)
                    break
                }
            }
        }

        if (foundAtIndex != -1) {
            words[foundAtIndex].frequency++
            val tempWord = words[foundAtIndex]
            words.sortByDescending { it.frequency }
            val newIndex = words.indexOf(tempWord)
            callback.foundExactMatch(
                tempWord,
                foundAtIndex,
                newIndex
            )
            partialMatchPhrase = ""
        } else {
            loopForFullMatchWithParsedSamples@ for (voiceSample in voiceSamples) {
                val parsedVoiceSamples = NumberUtil.convertToWordRepresentation(voiceSample)
                for (parsedVoiceSample in parsedVoiceSamples) {
                    for (word in wordsToWorkOn) {
                        for (parsedWord in word.alternateSamples) {
                            if (parsedWord.equals(parsedVoiceSample, true)) {
                                foundAtIndex = words.indexOf(word)
                                break@loopForFullMatchWithParsedSamples
                            }
                        }
                    }
                }
            }

            if (foundAtIndex != -1) {
                words[foundAtIndex].frequency++
                val tempWord = words[foundAtIndex]
                words.sortByDescending { it.frequency }
                val newIndex = words.indexOf(tempWord)
                callback.foundExactMatch(
                    tempWord,
                    foundAtIndex,
                    newIndex
                )
                partialMatchPhrase = ""
            } else if (partialMatchPhrase == "") {
                loopForPartialMatchWithParsedSamples@ for (voiceSample in voiceSamples) {
                    val parsedVoiceSamples = NumberUtil.convertToWordRepresentation(voiceSample)
                    for (parsedVoiceSample in parsedVoiceSamples) {
                        for (word in wordsToWorkOn) {
                            for (parsedWord in word.alternateSamples) {
                                val wordsInVoiceSample = voiceSample.split(" ")
                                val wordsInWord = parsedWord.split(" ")
                                for (wordInVoiceSample in wordsInVoiceSample) {
                                    for (wordInWord in wordsInWord) {
                                        if (wordInVoiceSample.equals(wordInWord, true)) {
                                            partialMatchPhrase = wordInWord
                                            callback.foundPartialMatch()
                                            break@loopForPartialMatchWithParsedSamples
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                callback.noMatchFound(
                    voiceSamples[0]
                )
                partialMatchPhrase = ""
            }
        }
    }
}