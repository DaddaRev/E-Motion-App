package com.example.emotionapp

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import retrofit2.Response
import java.net.SocketTimeoutException

class MeteoFragment : Fragment() {

    val myCouroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meteo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val searchButton :Button = view.findViewById(R.id.wheaterSearchButton)
        val errorText :TextView = view.findViewById(R.id.cityTextEntered)
        val cityText :EditText = view.findViewById(R.id.cityText)
        val threeHoursText :TextView = view.findViewById(R.id.nextHoursText)
        val tomorrowText :TextView = view.findViewById(R.id.tomorrowText)
        val errorText2 :TextView = view.findViewById(R.id.textView5)

        val navController = findNavController()

        val mainActivity = activity as MainActivity

        //Listener for the login button
        searchButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                if(cityText.text.isNotBlank()) {

                    //User Login Handler
                    myCouroutineScope.launch {
                        try {
                            val city = cityText.text
                            val response: Response<WeatherReport> =
                                mainActivity.customService.getWeather(city.toString())

                            if (response.isSuccessful) {
                                val weatherResponse: WeatherReport? = response.body()
                                threeHoursText.text = "Waiting.."
                                tomorrowText.text = "Waiting.."


                                if (weatherResponse != null) {
                                    threeHoursText.text = weatherResponse.message1
                                    tomorrowText.text = weatherResponse.message2
                                    errorText.text = "Enter your city"
                                } else {
                                    errorText.text = "Report error"  // response body error
                                    errorText.setTextColor(Color.RED)
                                }
                            } else {
                                errorText.text = "Invalid City"
                                errorText.setTextColor(Color.RED)
                            }
                        } catch (e: SocketTimeoutException) {
                            withContext(Dispatchers.Main) {
                                errorText2.text = "\nServer is currently offline or not reacheble"
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                errorText2.text = "\nError: ${e.message}"
                            }
                        }
                    }
                }else{
                    errorText.text = "Invalid city"
                    errorText.setTextColor(Color.RED)
                }
            }

        })
    }

}