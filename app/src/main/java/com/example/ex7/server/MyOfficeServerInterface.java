package com.example.ex7.server;

import org.json.JSONObject;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MyOfficeServerInterface {

    @GET("/images/all/")
    Call<AllImagesResponse> getAllImages();

    @GET("/users/{user_name}/token/")
    Call<TokenResponse> getToken(@Path("user_name") String userName);

    @GET("/user/")
    Call<UserResponse> getUser(@Header("Authorization") String userToken);

    @Headers({"Content-Type: application/json"})
    @POST("/user/edit/")
    Call<UserResponse> setPrettyName(@Header("Authorization") String userToken, @Body SetUserPrettyNameRequest request);

    @Headers({"Content-Type: application/json"})
    @POST("/user/edit/")
    Call<UserResponse> setImageUrl(@Header("Authorization") String userToken, @Body SetUserImageRequest request);

}



/*

TODO saving the user token to sp!

 server endpoint:
https://hujipostpc2019.pythonanywhere.com/

 methods to have:
 * connectivity check A - users/0
 * connectivity check B - todos/0
 * getUser(user_id)
 * getAllTodos(user_id)
 * putNewTodo(todoId)

 */

