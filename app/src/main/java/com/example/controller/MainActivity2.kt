package com.example.controller

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity2 : AppCompatActivity() {

	private lateinit var controller: Controller;

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_main_2)

		controller = Controller(BluetoothAdapter.getDefaultAdapter())

		val leftUp: Button = findViewById(R.id.left_up)
		val leftStop: Button = findViewById(R.id.left_stop)
		val leftDown: Button = findViewById(R.id.left_down)
		val rightUp: Button = findViewById(R.id.right_up)
		val rightStop: Button = findViewById(R.id.right_stop)
		val rightDown: Button = findViewById(R.id.right_down)

		leftUp.setOnClickListener {
			controller.writeValue(Side.LEFT, 3)
		}
		leftStop.setOnClickListener {
			controller.writeValue(Side.LEFT, 0)
		}
		leftDown.setOnClickListener {
			controller.writeValue(Side.LEFT, -3)
		}
		rightUp.setOnClickListener {
			controller.writeValue(Side.RIGHT, 3)
		}
		rightStop.setOnClickListener {
			controller.writeValue(Side.RIGHT, 0)
		}
		rightDown.setOnClickListener {
			controller.writeValue(Side.RIGHT, -3)
		}

		controller.start();
	}

	override fun onDestroy() {
		controller.stop();

		super.onDestroy()
	}

}
