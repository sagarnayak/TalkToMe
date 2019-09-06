package com.sagar.android.talktome.repository.retrofit

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

/**
 * retrofit api interface
 */
interface ApiInterface {

    @GET("interview/dictionary-v2.json")
    fun getDictionary(): Observable<Response<ResponseBody>>
}