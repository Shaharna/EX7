package com.example.ex7.server;

import com.example.ex7.data.Ticket;
import com.example.ex7.data.User;

import org.json.JSONObject;

import java.util.List;

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

    @GET("/users/0")
    Call<User> connectivityCheck();

    @GET("/users/{user_name}/token/")
    Call<TokenResponse> getToken(@Path("user_name") String userName);

    //@Headers("Authorization: token {user_token}")
    @GET("/user/")
    Call<UserResponse> getUser(@Header("Authorization") String userToken);


    @GET("/todos")
    Call<List<Ticket>> getAllTicketsForUser(@Query("user_id") String userId);


    // the result will be the ticket as was created in the server (e.g. change in the "id" field, etc)
    @POST("/todos")
    Call<Ticket> insertNewTicket(@Body Ticket ticket);

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

