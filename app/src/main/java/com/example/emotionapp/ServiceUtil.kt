//Davide Reverberi E-MotionApp

package com.example.emotionapp

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * LoginResponse is a data class representing the response received after a login attempt.
 **/
@JsonClass(generateAdapter = true)
data class LoginResponse(
    val message: String
)

/**
 * RegisterResponse is a data class representing the response received after a register attempt.
 **/
@JsonClass(generateAdapter = true)
data class RegisterResponse(
    val message: String
)

/**
 * UserCredentials is a data class representing the user's login credentials.
 **/
@JsonClass(generateAdapter = false)
data class UserCredentials(
    val username: String,
    val password: String
)

/**
 * UserProfile is a data class representing the user's registration data.
 **/
@JsonClass(generateAdapter = false)
data class UserProfile(
    val username: String,
    val email: String,
    val password: String
)

/**
 * WeatherReport is a data class representing the response received after a weather attempt.
 **/
@JsonClass(generateAdapter = false)
data class WeatherReport(
    val message1: String,
    val message2: String
)

/**
 * WebServiceAPI is an interface defining the API endpoints for the web service.
 */
interface WebServiceAPI {

    //Fetches the weather report for a specified city.
    @GET("weather")
    suspend fun getWeather(@Query("city") city: String): Response<WeatherReport>

    //Registers a new user with the provided user profile.
    @POST("register")
    suspend fun registerUser(@Body userProfile: UserProfile): Response<RegisterResponse>

    //Logs in a user with the provided credentials.
    @POST("login")
    suspend fun loginUser(@Body userCredentials: UserCredentials): Response<LoginResponse>
}