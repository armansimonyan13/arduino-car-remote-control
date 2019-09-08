package com.example.controller

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import java.io.IOException
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.util.*

private val SERVICE_ID = "00001101-0000-1000-8000-00805f9b34fb" //SPP(Serial Port Profile) UUID
private val SERVICE_ADDRESS = "00:14:03:06:8F:E7" // DSD TECH HC-05 BT ADDRESS
//val SERVICE_ADDRESS = "20:13:09:23:01:69" // HC-05 BT ADDRESS

private val TAG = "TESTING --->"

class MainActivity : AppCompatActivity() {

	private var btSocket: BluetoothSocket? = null

	private var btAdapter: BluetoothAdapter? = null

	private var isOn: Boolean = false

	private var connectThread: ConnectThread? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val button: Button = findViewById(R.id.button)
		button.setOnClickListener {
			if (isOn) {
				btSocket?.outputStream?.write("0".toByteArray())
				isOn = false
			} else {
				btSocket?.outputStream?.write("1".toByteArray())
				isOn = true
			}
		}

		val sliderLeft: Slider = findViewById(R.id.left)
		val sliderRight: Slider = findViewById(R.id.right)

		val textLeft: TextView = findViewById(R.id.text_left)
		val textRight: TextView = findViewById(R.id.text_right)

		sliderLeft.setOnSlideListener(object : Slider.OnSlideListener {
			override fun onSlided(value: Int) {
				writeValue("l", value, textLeft)
			}
		})
		sliderRight.setOnSlideListener(object : Slider.OnSlideListener {
			override fun onSlided(value: Int) {
				writeValue("r", value, textRight)
			}
		})

		val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
		registerReceiver(receiver, filter)

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

		if (btSocket != null) {
			try {
				val out = btSocket?.outputStream
				out?.write("hello\r\n".toByteArray())
			} catch (e: IOException) {
				throw RuntimeException()
			}
		}
	}

	override fun onDestroy() {
		connectThread?.cancel()

		super.onDestroy()
	}

	private inner class ConnectThread(device: BluetoothDevice) : Thread() {
		private val thisSocket: BluetoothSocket
		private val thisDevice: BluetoothDevice = device

		init {
			var tmp: BluetoothSocket? = null
			try {
				tmp = thisDevice.createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_ID))
//				tmp = thisDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SERVICE_ID))
//				tmp = createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_ID), thisDevice)
			} catch (e: IOException) {
				throw RuntimeException()
			}
			thisSocket = tmp
		}

		override fun run() {
			btAdapter?.cancelDiscovery()

			try {
				thisSocket.connect()
				Log.d("TESTING", "Connected to shit")
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
			val action = intent.action
			when (action) {
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

//    fun createRfcommSocketToServiceRecord(uuid: UUID, device: BluetoothDevice): BluetoothSocket {
//        if (!isBluetoothEnabled()) {
//            Log.e(TAG, "Bluetooth is not enabled");
//            throw IOException()
//        }
//
//        val klass = BluetoothSocket::class.java
//        val constuctor = klass.getDeclaredConstructor(
//            Int::class.java,
//            Int::class.java,
//            Boolean::class.java,
//            Boolean::class.java,
//            BluetoothDevice::class.java,
//            Int::class.java,
//            ParcelUuid::class.java
//        )
//	    constuctor.isAccessible = true
//
//        return constuctor.newInstance(
//                BluetoothSocket.TYPE_RFCOMM,
//                -1,
//                true,
//                true,
//                device,
//                1,
//                ParcelUuid(uuid)
//            )
//    }
//
//    fun isBluetoothEnabled(): Boolean {
//        var ret = false
//        if (btAdapter?.isEnabled == true) {
//            ret = true;
//        }
//        return ret
//    }

	/*
	 *
	 * @param side is either "l" or "r"
	 * @param value what should be sent
	 * @param textView the view which should display the value
	 *
	 */
	fun writeValue(side: String, value: Int, textView: TextView) {
		btSocket?.outputStream?.write(side.toByteArray())
		val ba = ByteBuffer.allocate(4).putInt(value).array()
		logHex(ba)
		btSocket?.outputStream?.write(ba)
		textView.text = format(ba)
	}

	fun log(message: String) {
		Log.d(TAG, message)
	}

	fun logHex(byteArray: ByteArray) {
		log(format(byteArray))
	}

	fun format(byteArray: ByteArray): String {
		val stringBuffer = StringBuffer()
		for (b in byteArray) {
			stringBuffer.append(String.format("%02X", b))
		}
		return stringBuffer.toString()
	}

}
