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

class registerFragment : Fragment() {

    val myCouroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registerButton: Button = view.findViewById(R.id.loginButtonR)
        val usernameText : EditText = view.findViewById(R.id.usernameTextR)
        val emailText: EditText = view.findViewById(R.id.emailTextR)
        val passwordText : EditText = view.findViewById(R.id.passwordTextR)

        val mainActivity = activity as MainActivity

        val navController = findNavController()

        //Listener for the register button --> Register Fragment
        registerButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val textView: TextView = view.findViewById(R.id.textViewR)

                if(usernameText.text.isNotBlank() && emailText.text.isNotBlank() && passwordText.text.isNotBlank())
                {
                    //User Registration Handler
                    myCouroutineScope.launch {
                        try{
                            val username = usernameText.text
                            val email = emailText.text
                            val password = passwordText.text
                            val response: Response<RegisterResponse> =
                                mainActivity.customService.registerUser(
                                    username.toString(),
                                    email.toString(),
                                    password.toString()
                                )

                            if (response.isSuccessful) {
                                //User has been successfully registered
                                withContext(Dispatchers.Main) {
                                    val action =
                                        registerFragmentDirections.actionRegisterFragmentToLoginFragment1()
                                    navController.navigate(action)
                                }
                            } else {
                                textView.text = "Username already exist! "
                                textView.setTextColor(Color.RED)
                            }
                        }catch (e: SocketTimeoutException) {
                            withContext(Dispatchers.Main) {
                                textView.text = "Server is currently offline or not reacheble"
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                textView.text = "\nAn error occurred: ${e.message}"
                            }
                        }
                    }
                }else{
                    textView.text = "Fill al the fields!"
                    textView.setTextColor(Color.RED)
                }
            }
        })
    }

}