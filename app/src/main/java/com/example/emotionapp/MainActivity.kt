package com.example.emotionapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val MY_UUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")  //Standard UUID for HT-06
    val context: Context = this     //Needed for the Thread execution
    var deviceHCMAC : BluetoothDevice? = null       //Device of interest
    var OutputStream : OutputStream? = null         //Communication Stream with the HT device
    var bluetoothSocket: BluetoothSocket? = null    //Communication Socket Stream

    // Service related variables:
    lateinit var customService : WebService
    private var mBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService(). */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected (className: ComponentName, service: IBinder) {
            // We've bound to service, cast the IBinder and get service instance.
            val binder = service as MyBinder
            customService = binder.getService()
            mBound = true
        }
        override fun onServiceDisconnected (arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Getting the Bluetooth adapter and the Bluetooth manager
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

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
            Log.e("cnt", "Device Name --> $deviceName, Device MAC --> $deviceHardwareAddress")
            if (deviceName == "HC-06") {
                deviceHCMAC = device  //Address of interest
                println("HC-06 MAC: --> ${deviceHCMAC!!.address}")
            }
        }

        //  Eventually, you can start discovery of other devices here, see:
        //  https://developer.android.com/develop/connectivity/bluetooth/find-bluetooth-devices#java


        //Connect the HC-06 device in a separate thread
        class ConnectThread(device: BluetoothDevice) : Thread() {

            val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
                device.createRfcommSocketToServiceRecord(MY_UUID)
            }

            public override fun run() {
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
                bluetoothSocket = mmSocket
            }

        }
        //Starting the thread if the HT-06 has been found
        if (deviceHCMAC != null)
        {
            val connectThread = ConnectThread(deviceHCMAC!!)
            connectThread.start()

        }else
        {
            Toast.makeText(this, "HC Device is not found", Toast.LENGTH_LONG).show()
        }

    }

    // Call this function to send data to the remote device.
    fun writeBytes(bytes: ByteArray) {
        try {
            if (OutputStream == null){
                Log.d("error", "OutputStream is null --> Can't communicate")
            }
            OutputStream?.write(bytes)   //Writing the Bytes in the stream
        } catch (e: IOException) {
            Log.e("error", "Error occurred when sending data", e)
        }
    }

    override fun onStart() {
        super.onStart()
        //Bind to local WebService
        val boundIntent = Intent(this, WebService::class.java)
        bindService(boundIntent,connection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket?.close()
                Log.d("message", "Connection closed")
            } catch (e: IOException) {
                Log.d("message", "Error while closing the connection", e)
            }
        }
    }
}