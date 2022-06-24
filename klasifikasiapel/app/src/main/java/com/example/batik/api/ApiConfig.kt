package com.example.batik.api


import com.example.batik.api.response.Default
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

class ApiConfig {
    companion object{
        const val BASE_URL ="http://192.168.1.39:5000/"
        const val IMAGE_URL = BASE_URL+"image/"
    }

    private fun retrofit() : Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun instance() : ApiInterface{
        return retrofit().create(ApiInterface::class.java)
    }
}

interface  ApiInterface{
    @Multipart
    @POST("api/image")
    fun upload(
        @Part imagename: MultipartBody.Part
    ) : Call<Default>
}