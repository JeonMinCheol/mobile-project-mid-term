package com.example.mobilewebproject2;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Post {
    @Multipart
    @POST("/v1/object-detection/yolov5s")
    Call<ReceiveDTO> sendData(
            @Part("title") RequestBody title,
            @Part("text") RequestBody text,
            @Part MultipartBody.Part imageFile
    );
}
