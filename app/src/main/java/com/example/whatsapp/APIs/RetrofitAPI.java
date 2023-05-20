package com.example.whatsapp.APIs;

import com.example.whatsapp.API_Models.BrainShopMsgModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface RetrofitAPI {

    @GET
    Call<BrainShopMsgModel> getMessage(@Url String url);

}
