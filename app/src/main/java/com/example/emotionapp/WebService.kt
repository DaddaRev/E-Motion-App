//Davide Reverberi E-MotionApp

package com.example.emotionapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 *
 * WebService is a Service class that provides methods for interacting with a web service API.
 * It uses Retrofit for making HTTP requests and Moshi for JSON serialization/deserialization.
 *
 */
class WebService : Service() {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Configure OkHttpClient with custom timeout settings --> Registration process may require several seconds
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
        .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
        .build()

    //If not in the emulator version of the code (Emulator: " http://10.0.2.2:5000 "),
    // change this below URL with the IP of the server Device (Device: " http://<Server IP address>:5000 ")

    val retrofit = Retrofit.Builder()
        .baseUrl(" http://10.0.2.2:5000 ")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()

    val service = retrofit.create(WebServiceAPI::class.java)

    // Called when a client binds to the service.
    private val binder = MyBinder(this)
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    /** Method for clients. */
    //Sending to the client the result of the login operation
    suspend fun loginUser(username :String, password :String) :Response<LoginResponse>{
        return service.loginUser(UserCredentials(username, password))  //POST call
    }

    //Sending to the client the result of the register operation
    suspend fun registerUser(username :String, email: String, password :String) :Response<RegisterResponse>{
        return service.registerUser(UserProfile(username, email, password))  //POST call
    }

    suspend fun getWeather(city :String) :Response<WeatherReport>{
        return service.getWeather(city) //GET call
    }
}