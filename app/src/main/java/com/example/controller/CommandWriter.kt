package com.example.controller

import java.io.OutputStream
import java.nio.ByteBuffer
import kotlin.math.abs

class CommandWriter(private val outputStream: OutputStream, private val logger: Logger) {

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

		outputStream.write(byteArray)

		logHex(byteArray)
	}

	private fun logHex(byteArray: ByteArray) {
		logger.log(format(byteArray))
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
