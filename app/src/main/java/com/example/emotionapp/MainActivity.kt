//Davide Reverberi E-MotionApp

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
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 *
 * Main activity of the entire application which deals with all the operations necessary for requesting permissions,
 * pairing and connecting with Bluetooth devices; writing and reading data on the Bluetooth communication stream and
 * managing the binding with the service to communicate with the server.
 *
 */
class MainActivity : AppCompatActivity() {

    private val MY_UUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")  //Standard UUID for HT-05
    val context: Context = this     //Needed for the Thread execution
    var deviceHCMAC : BluetoothDevice? = null       //Device of interest
    var OutputStream : OutputStream? = null         //Output communication Stream with the HT device
    var InputStream :InputStream? = null            //Input communication Stream with the HT device
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
            Log.d("cnt", "Device doesn't support Bluetooth")
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
            Log.d("cnt", "Device Name --> $deviceName, Device MAC --> $deviceHardwareAddress")
            if (deviceName == "HC-05") {
                deviceHCMAC = device  //Address of interest
                Log.d("cnt","HC-05 MAC: --> ${deviceHCMAC!!.address}")
            }
        }

        //  Eventually, you can start discovery other devices here, see:
        //  https://developer.android.com/develop/connectivity/bluetooth/find-bluetooth-devices#java


        //Connect the HC-05 device in a separate thread
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

                bluetoothAdapter?.cancelDiscovery()    // Cancel discovery because it otherwise slows down the connection.
                mmSocket?.connect()                         // Connect the BT device
                OutputStream = mmSocket?.outputStream       // Getting the output stream
                InputStream = mmSocket?.inputStream         // Getting the input stream
                bluetoothSocket = mmSocket                  // Getting the socket
            }

        }
        //Starting the connection thread if the HT-05 has been found
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
            //Thread.sleep(50)
        } catch (e: IOException) {
            Log.e("error", "Error occurred when sending data", e)
        }
    }

    // Call this function to read data from the remote device,
    // Not used in this prototype version of the application.
    fun readBytes():ByteArray? {
        try{
            return InputStream?.readBytes()
        } catch (e: IOException) {
            Log.e("error", "Error occurred when receiving data", e)
        }
        val errorString :String = "Error"
        return errorString.toByteArray()
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