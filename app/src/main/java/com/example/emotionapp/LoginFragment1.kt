package com.example.emotionapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs


class LoginFragment1 : Fragment() {


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
        val button: Button = view.findViewById(R.id.loginButton)

        val navController = findNavController()

        val mainActivity = activity as MainActivity
        val device = mainActivity.deviceHCMAC
        val outputStream = mainActivity.OutputStream

        //Listener for the first button --> Wrong Answer
        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (device == null) {
                    val textView: TextView = view.findViewById(R.id.textView)
                    textView.text = "Device not found, pair the device and restart the application"
                }else{
                    val action = LoginFragment1Directions.actionLoginFragment1ToControlsFragment()
                    navController.navigate(action)
                }
            }
        })

    }

}