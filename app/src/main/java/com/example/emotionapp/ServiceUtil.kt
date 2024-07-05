package com.example.emotionapp

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val message: String
)

@JsonClass(generateAdapter = true)
data class RegisterResponse(
    val message: String
)

@JsonClass(generateAdapter = false)
data class UserCredentials(
    val username: String,
    val password: String
)

@JsonClass(generateAdapter = false)
data class UserProfile(
    val username: String,
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = false)
data class WeatherReport(
    val message1: String,
    val message2: String
)

interface WebServiceAPI {

    @GET("weather")
    suspend fun getWeather(@Query("city") city: String): Response<WeatherReport>

    @POST("register")
    suspend fun registerUser(@Body userProfile: UserProfile): Response<RegisterResponse>

    @POST("login")
    suspend fun loginUser(@Body userCredentials: UserCredentials): Response<LoginResponse>
}