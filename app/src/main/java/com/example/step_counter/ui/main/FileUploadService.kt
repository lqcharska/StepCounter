package com.example.step_counter.ui.main

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface FileUploadService {
    @Multipart
    @POST("get_acc_data/")
    fun upload(
        @Part file: MultipartBody.Part?
    ): retrofit2.Call<RequestBody>
}