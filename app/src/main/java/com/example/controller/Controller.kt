package com.example.controller

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

private const val SERVICE_ID = "00001101-0000-1000-8000-00805f9b34fb" //SPP(Serial Port Profile) UUID
private const val SERVICE_ADDRESS = "00:14:03:06:8F:E7" // DSD TECH HC-05 BT ADDRESS
//private const val SERVICE_ADDRESS = "00:14:03:06:8F:93" // DSD TECH HC-05 BT ADDRESS

class Controller(private val bluetoothAdapter: BluetoothAdapter, private val logger: Logger) {

	private var bluetoothSocket: BluetoothSocket? = null
	private var commandWriter: CommandWriter? = null

	private var outputStream: OutputStream? = null
	private var inputStream: InputStream? = null

	fun start() {
		if (bluetoothAdapter.isEnabled) {
			val connectThread = ConnectThread(this, logger)
			connectThread.start()
		} else {
//				val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//				startActivityForResult(enableIntent, 3)
		}
	}

	fun writeValue(side: Side, value: Int) {
		commandWriter?.writeValue(side, value)
	}

	class ConnectThread(val controller: Controller, val logger: Logger) : Thread() {

		override fun run() {
			val bluetoothSocket: BluetoothSocket?
			try {
				bluetoothSocket = controller
					.bluetoothAdapter
					.getRemoteDevice(SERVICE_ADDRESS)
					.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SERVICE_ID))
			} catch (e: IOException) {
				throw RuntimeException(e)
			}

			controller.bluetoothAdapter.cancelDiscovery()

			try {
				bluetoothSocket.connect()
				controller.bluetoothSocket = bluetoothSocket
				controller.outputStream = bluetoothSocket.outputStream
				controller.inputStream = bluetoothSocket.inputStream
				controller.commandWriter = CommandWriter(bluetoothSocket.outputStream, logger)
				InputStreamReaderThread(bluetoothSocket.inputStream, logger).start()
				logger.log("Connected to the module")
			} catch (connectException: IOException) {
				logger.log("Error occurred: " + connectException.printStackTrace())
				try {
					bluetoothSocket.close()
				} catch (closeException: IOException) {
					logger.log("Can't close socket")
				}
				return
			}
		}

//		fun cancel() {
//			try {
//				thisSocket.close()
//			} catch (e: IOException) {
//				Log.e("TESTING", "Can't close socket")
//			}
//		}
	}

	class InputStreamReaderThread(val inputStream: InputStream, val logger: Logger) : Thread() {

		override fun run() {
			while (true) {
				val data = inputStream.read()
				logger.log(data.toString())
			}
		}

	}

}
