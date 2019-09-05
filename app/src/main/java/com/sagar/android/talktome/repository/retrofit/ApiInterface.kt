package com.sagar.android.talktome.repository.retrofit

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface ApiInterface {

    @GET("/")
    fun getDictionary(): Observable<Response<ResponseBody>>
}