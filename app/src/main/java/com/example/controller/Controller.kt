package com.example.controller

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.abs

private const val TAG = "TESTING --->"

private const val SERVICE_ID = "00001101-0000-1000-8000-00805f9b34fb" //SPP(Serial Port Profile) UUID
private const val SERVICE_ADDRESS = "00:14:03:06:8F:E7" // DSD TECH HC-05 BT ADDRESS

class Controller(val btAdapter: BluetoothAdapter) {

	private var connectThread: ConnectThread? = null

	private var btSocket: BluetoothSocket? = null

	fun start() {
		val btDevice = btAdapter?.getRemoteDevice(SERVICE_ADDRESS)

		if (btAdapter == null) {
			log("Bluetooth not available")
		} else {
			if (btAdapter?.isEnabled == false) {
//				val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//				startActivityForResult(enableIntent, 3)
			} else {
				connectThread = ConnectThread(btDevice!!)
				connectThread?.start()
			}
		}
	}

	fun stop() {
		connectThread?.cancel()
	}

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
	fun writeValue(side: Side, value: Int) {
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

	private fun logHex(byteArray: ByteArray) {
		log(format(byteArray))
	}

	fun log(message: String) {
		Log.d(TAG, message)
	}

	private fun format(byteArray: ByteArray): String {
		val stringBuffer = StringBuffer()
		for (b in byteArray) {
			stringBuffer.append(String.format("%02X", b))
		}
		return stringBuffer.toString()
	}

}

enum class Side {
	LEFT, RIGHT
}

