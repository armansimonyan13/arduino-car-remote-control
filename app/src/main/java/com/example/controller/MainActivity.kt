package com.example.controller

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

	private lateinit var controller: Controller;

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_main)

		controller = Controller(BluetoothAdapter.getDefaultAdapter(), Logger())

		val sliderLeft: Slider = findViewById(R.id.left)
		val sliderRight: Slider = findViewById(R.id.right)

		sliderLeft.setOnSlideListener(object : Slider.OnSlideListener {
			override fun onSlided(value: Int) {
				controller.writeValue(Side.LEFT, value)
			}
		})
		sliderRight.setOnSlideListener(object : Slider.OnSlideListener {
			override fun onSlided(value: Int) {
				controller.writeValue(Side.RIGHT, value)
			}
		})

		controller.start()
	}

	override fun onDestroy() {
//		controller.stop()

		super.onDestroy()
	}

}
