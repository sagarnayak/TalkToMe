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
                                    data.sortByDescending { word -> word.frequency }
                                    wordsInDictionary.postValue(
                                        Event(data)
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
}