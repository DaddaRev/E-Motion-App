package com.example.emotionapp

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

class ControlsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Force this Fragment in landscape orientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_controls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as MainActivity
        val device = mainActivity.deviceHCMAC
        val outputStream = mainActivity.OutputStream

        val slideBarUpDown: SeekBar = view.findViewById(R.id.seekBar)
        val slideBarRightLeft: SeekBar = view.findViewById(R.id.seekBar2)
        val waterButton: Button = view.findViewById(R.id.button)        // Water action trigger button
        val cleanButton: Button = view.findViewById(R.id.button2)       // Clean action trigger button
        val stopButton: Button = view.findViewById(R.id.stopButon)      // Stop the servo button
        val weatherButton: Button = view.findViewById(R.id.meteoButton)   // Check meteo for next days button
        val progressBattery :ProgressBar = view.findViewById(R.id.batteryLevel) // Battery level

        val navController = findNavController()

        //Listener for the water button
        waterButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val stringToSend: String = "W\n"
                mainActivity.writeBytes(stringToSend.toByteArray())
            }
        })

        //Listener for the clean button
        cleanButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val stringToSend: String = "C\n"
                mainActivity.writeBytes(stringToSend.toByteArray())
            }
        })

        //Listener for the stop Servo button
        stopButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val stringToSend: String = "S\n"
                mainActivity.writeBytes(stringToSend.toByteArray())
            }
        })

        //Listener for meteo button
        weatherButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //Tells the BT module that user is on meteo interface --> Not using controls
                val stringToSend: String = "M\n"
                mainActivity.writeBytes(stringToSend.toByteArray())
                val action = ControlsFragmentDirections.actionControlsFragmentToMeteoFragment()
                navController.navigate(action)
            }
        })

        // Listener for the up/down slide bar
        slideBarUpDown.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val stringToSend: String = "V$progress\n"
                mainActivity.writeBytes(stringToSend.toByteArray())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something when tracking starts, if needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seekBar.progress = 50  //Returning in the middle when user stops moving
            }
        })

        // Listener for the up/down slide bar
        slideBarRightLeft.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val stringToSend: String = "H$progress\n"
                mainActivity.writeBytes(stringToSend.toByteArray())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something when tracking starts, if needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seekBar.progress = 50  //Returning in the middle when user stops moving
            }
        })


    }

    override fun onDestroy(){
        super.onDestroy()
        //Reset the orientation for the next fragments
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

}