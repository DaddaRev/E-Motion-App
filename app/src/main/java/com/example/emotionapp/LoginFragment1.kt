//Davide Reverberi E-MotionApp

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
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 *
 * LoginFragment1 is the Fragment that handles the login functionality of an application.
 * This class provides a user interface for entering login credentials and managing user authentication.
 * It also includes navigation to the registration fragment.
 *
 **/
class LoginFragment1 : Fragment() {

    val myCouroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loginButton: Button = view.findViewById(R.id.loginButtonR)
        val registerButton: Button = view.findViewById(R.id.button3)
        val usernameText :EditText = view.findViewById(R.id.usernameTextR)
        val passwordText :EditText = view.findViewById(R.id.passwordTextR)

        val mainActivity = activity as MainActivity
        val device = mainActivity.deviceHCMAC

        val navController = findNavController()

        //Listener for the login button
        loginButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val textView: TextView = view.findViewById(R.id.textViewR)

                // The following code is specific for the test version with a physical device
                // in which it is possible to navigate to the bluetooth controls interface
                // only if the connection with HC-05 device is done.
                // Change the below boolean comparison to an inequality if you are in the emulator test mode. (device != null)

                if (device == null) {
                    textView.text = "\nDevice not found, pair the device and restart the application"

                }else{   // The device is connected --> User login

                    //User Login Handler
                    myCouroutineScope.launch {
                        try{
                            val username = usernameText.text
                            val password = passwordText.text
                            val response : Response<LoginResponse> = mainActivity.customService.loginUser(username.toString(), password.toString())

                            if (response.isSuccessful){
                                //User has entered the right credentials --> Access to controls panel
                                withContext(Dispatchers.Main) {
                                    val action = LoginFragment1Directions.actionLoginFragment1ToControlsFragment()
                                    navController.navigate(action)
                                }
                            }else{
                                textView.text = "\nInvalid User or Password! "
                                textView.setTextColor(Color.RED)
                            }
                        }catch (e: SocketTimeoutException) {
                            withContext(Dispatchers.Main) {
                                textView.text = "\nServer is currently offline or not reacheble"
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                textView.text = "\nAn error occurred: ${e.message}"
                            }
                        }

                    }
                }
            }
        })

        //Listener for the register button --> Register Fragment
        registerButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val action = LoginFragment1Directions.actionLoginFragment1ToRegisterFragment()
                navController.navigate(action)
            }
        })
    }

}