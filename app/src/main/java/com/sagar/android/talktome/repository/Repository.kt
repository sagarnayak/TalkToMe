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

/**
 * this is the repository class for the application.
 * this governs all the api call and core business logic.
 */
class Repository(
    private val apiInterface: ApiInterface,
    val logUtil: LogUtil,
    @Suppress("unused") val pref: SharedPreferences
) : SuperRepository() {

    val wordsInDictionary: MutableLiveData<Event<ArrayList<Word>>> = MutableLiveData()
    val error: MutableLiveData<Event<String>> = MutableLiveData()

    val words: ArrayList<Word> = ArrayList()

    private var partialMatchPhrase: String = ""

    fun reinitialiseVoiceRecognition() {
        partialMatchPhrase = ""
    }

    fun getDictionary() {
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
                                    val jsonObjBody = JSONObject(it.string())
                                    val data = Gson().fromJson<ArrayList<Word>>(
                                        jsonObjBody.getJSONArray("dictionary").toString(),
                                        listType
                                    )
                                    logUtil.logV("generating alternate samples for words")
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
                                        Event(
                                            getCopyOfWords()
                                        )
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

    interface VoiceMatchingCallback {
        fun foundExactMatch(words: ArrayList<Word>, newIndex: Int)
        fun foundPartialMatch()
        fun noMatchFound(mostPreferredPhrase: String)
    }

    /**
     * this is the method that checks has the core logic to match samples from voice recognition
     * and the words from server.
     * the result is sent to observer through a interface callback
     */
    fun recognizedVoice(
        voiceSamples: ArrayList<String>,
        callback: VoiceMatchingCallback
    ) {
        logUtil.logV("got recognized voice phrases : $voiceSamples")
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

        logUtil.logV("working set of words :\n $words")

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
            logUtil.logV("found exact match at $foundAtIndex")

            foundExactMatch(foundAtIndex, callback)
        } else {
            logUtil.logV("going for a full sample set exact match loop")

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
                logUtil.logV("found exact match with alternate sample set at index $foundAtIndex")

                foundExactMatch(foundAtIndex, callback)
            } else if (partialMatchPhrase == "") {
                logUtil.logV("going for a partial match loop with alternate samples")

                var foundPartialMatch = false

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
                                            foundPartialMatch = true
                                            logUtil.logV("found partial match with $partialMatchPhrase")
                                            break@loopForPartialMatchWithParsedSamples
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!foundPartialMatch) {
                    logUtil.logV("no partial match found")

                    callback.noMatchFound(
                        voiceSamples[0]
                    )
                    partialMatchPhrase = ""
                }
            } else {
                logUtil.logV("no match found")

                callback.noMatchFound(
                    voiceSamples[0]
                )
                partialMatchPhrase = ""
            }
        }
    }

    private fun foundExactMatch(foundAtIndex: Int, callback: VoiceMatchingCallback) {
        words[foundAtIndex].frequency++
        val tempWord = words[foundAtIndex]
        words.sortByDescending { it.frequency }
        val newIndex = words.indexOf(tempWord)
        callback.foundExactMatch(
            getCopyOfWords(),
            newIndex
        )
        partialMatchPhrase = ""
    }

    private fun getCopyOfWords(): ArrayList<Word> {
        val listToSend: ArrayList<Word> = ArrayList()
        for (w in words)
            listToSend.add(w.copy())

        return listToSend
    }
}