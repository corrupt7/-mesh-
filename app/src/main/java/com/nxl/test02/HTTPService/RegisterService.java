package com.nxl.test02.HTTPService;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RegisterService {
    @POST("userRegister")
    Call<ResponseBody> post(@Body RequestBody user);
}
