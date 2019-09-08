package com.example.controller

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import java.io.IOException
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.abs

private const val SERVICE_ID = "00001101-0000-1000-8000-00805f9b34fb" //SPP(Serial Port Profile) UUID
private const val SERVICE_ADDRESS = "00:14:03:06:8F:E7" // DSD TECH HC-05 BT ADDRESS

private const val TAG = "TESTING --->"

class MainActivity2 : AppCompatActivity() {

	private var btSocket: BluetoothSocket? = null

	private var btAdapter: BluetoothAdapter? = null

	private var connectThread: ConnectThread? = null

	enum class Side {
		LEFT, RIGHT
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main_2)

		val leftUp: Button = findViewById(R.id.left_up)
		val leftStop: Button = findViewById(R.id.left_stop)
		val leftDown: Button = findViewById(R.id.left_down)
		val rightUp: Button = findViewById(R.id.right_up)
		val rightStop: Button = findViewById(R.id.right_stop)
		val rightDown: Button = findViewById(R.id.right_down)

		leftUp.setOnClickListener {
			writeValue(Side.LEFT, 3)
		}
		leftStop.setOnClickListener {
			writeValue(Side.LEFT, 0)
		}
		leftDown.setOnClickListener {
			writeValue(Side.LEFT, -3)
		}
		rightUp.setOnClickListener {
			writeValue(Side.RIGHT, 3)
		}
		rightStop.setOnClickListener {
			writeValue(Side.RIGHT, 0)
		}
		rightDown.setOnClickListener {
			writeValue(Side.RIGHT, -3)
		}

//		val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//		registerReceiver(receiver, filter)
//
		btAdapter = BluetoothAdapter.getDefaultAdapter()

//		btAdapter?.bondedDevices?.forEach { device ->
//			val deviceName = device.name
//			val deviceAddress = device.address
//
//			log("deviceName: $deviceName, deviceAddress: $deviceAddress")
//		}

		val btDevice = btAdapter?.getRemoteDevice(SERVICE_ADDRESS)

		if (btAdapter == null) {
			log("Bluetooth not available")
		} else {
			if (btAdapter?.isEnabled == false) {
				val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
				startActivityForResult(enableIntent, 3)
			} else {
				connectThread = ConnectThread(btDevice!!)
				connectThread?.start()
			}
		}

//		if (btSocket != null) {
//			try {
//				val out = btSocket?.outputStream
//				out?.write("hello\r\n".toByteArray())
//			} catch (e: IOException) {
//				throw RuntimeException()
//			}
//		}
	}

	override fun onDestroy() {
		connectThread?.cancel()

		super.onDestroy()
	}

	private inner class ConnectThread(device: BluetoothDevice) : Thread() {
		private val thisSocket: BluetoothSocket
		private val thisDevice: BluetoothDevice = device

		init {
			val tmp: BluetoothSocket?
			try {
				tmp = thisDevice.createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_ID))
			} catch (e: IOException) {
				throw RuntimeException()
			}
			thisSocket = tmp
		}

		override fun run() {
			btAdapter?.cancelDiscovery()

			try {
				thisSocket.connect()
				Log.d("TESTING", "Connected to the module")
			} catch (connectException: IOException) {
				try {
					thisSocket.close()
				} catch (closeException: IOException) {
					Log.e("TESTING", "Can't close socket")
				}
				return
			}

			btSocket = thisSocket
		}

		fun cancel() {
			try {
				thisSocket.close()
			} catch (e: IOException) {
				Log.e("TESTING", "Can't close socket")
			}
		}
	}

	private val receiver = object : BroadcastReceiver() {

		override fun onReceive(context: Context, intent: Intent) {
			when (intent.action) {
				BluetoothDevice.ACTION_FOUND -> {
					val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
					val deviceName = device.name
					val deviceHardwareAddress = device.address

					log(deviceName)
					log(deviceHardwareAddress)
				}
			}
		}

	}

//    fun isBluetoothEnabled(): Boolean {
//        var ret = false
//        if (btAdapter?.isEnabled == true) {
//            ret = true;
//        }
//        return ret
//    }

	/*
	 *
	 * @param side is either Side.LEFT or Side.RIGHT
	 * @param value what should be sent, must be in range [-255, 255].
	 *          Forward if positive, backward otherwise
	 *
	 * Data is represented as a byte array in the following format:
	 *
	 * | 0  | 1  | 2  | 3  |
	 * | xx | xx | xx | xx |
	 *
	 * [0]: side
	 *      left -> 0
	 *      right -> 1
	 * [1]: direction
	 *      forward -> 0
	 *      backward -> 1
	 * [2:3]: value/speed
	 *      in range [0, 255]
	 */
	private fun writeValue(side: Side, value: Int) {
		assert(value >= -255 || value <= 255)

		val sideByte: Byte = if (side == Side.LEFT) 0 else 1
		val directionByte: Byte = if (value > 0) 0 else 1
		val valueByteArray = ByteBuffer.allocate(4).putInt(abs(value)).array()

		val byteArray = ByteArray(4)
		byteArray[0] = sideByte
		byteArray[1] = directionByte
		byteArray[2] = valueByteArray[2]
		byteArray[3] = valueByteArray[3]

		btSocket?.outputStream?.write(byteArray)

		logHex(byteArray)
	}

	fun log(message: String) {
		Log.d(TAG, message)
	}

	private fun logHex(byteArray: ByteArray) {
		log(format(byteArray))
	}

	private fun format(byteArray: ByteArray): String {
		val stringBuffer = StringBuffer()
		for (b in byteArray) {
			stringBuffer.append(String.format("%02X", b))
		}
		return stringBuffer.toString()
	}

}
