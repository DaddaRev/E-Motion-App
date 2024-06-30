package com.example.emotionapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val MY_UUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")  //Standard UUID for HT-06
    val context: Context = this     //Needed for the Thread execution
    var deviceHCMAC : BluetoothDevice? = null   //Device of interest
    var OutputStream : OutputStream? = null     //Communication Stream with the HT device

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Getting the Bluetooth adapter and the Bluetooth manager
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.d("Message", "Device doesn't support Bluetooth")
        }

        //Checking if the bluetooth permission is granted or not
        if (bluetoothAdapter?.isEnabled == false) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // If not granted, permission is requested to the user
                if (Build.VERSION.SDK_INT > 31) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT),
                        100
                    )
                    return
                }
            }
        }

        //Permission granted --> Getting all the paired devices
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            println("Device Name --> $deviceName, Device MAC --> $deviceHardwareAddress")
            if (deviceName == "HC-06") {
                deviceHCMAC = device  //Address of interest
                println("HC-06 MAC: --> ${deviceHCMAC!!.address}")
            }
        }

        //  Eventually, you can start discovery of other devices here, see:
        //  https://developer.android.com/develop/connectivity/bluetooth/find-bluetooth-devices#java


        /*
        //Connect the HC-06 device in a separate thread
        class ConnectThread(device: BluetoothDevice) : Thread() {

            public val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
                device.createRfcommSocketToServiceRecord(MY_UUID)
            }

            override fun run() {
                //Checking if the bluetooth permission is granted or not
                if (bluetoothAdapter?.isEnabled == false) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // If not granted, permission is requested to the user
                        if (Build.VERSION.SDK_INT > 31) {
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT),
                                100
                            )
                            return
                        }
                    }
                }
                // Cancel discovery because it otherwise slows down the connection.
                bluetoothAdapter?.cancelDiscovery()
                mmSocket?.connect()
                OutputStream = mmSocket?.outputStream
            }

            // Closes the client socket and causes the thread to finish.
            fun cancel() {
                try {
                    mmSocket?.close()
                } catch (e: IOException) {
                    Log.e("message", "Could not close the client socket", e)
                }
            }
        }

        val connectThread = ConnectThread(deviceHCMAC!!)

        //Starting the thread if the HT-06 has been found
        if (deviceHCMAC != null) {

            connectThread.start()

            //Perform App

            connectThread.cancel()

        }else{
            Toast.makeText(this, "HC Device is not found", Toast.LENGTH_LONG).show()
        }
        */
    }

    override fun onDestroy() {
        super.onDestroy()
    }



}