package com.example.controller

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class Slider @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

	interface OnSlideListener {
		fun onSlided(value: Int)
	}

	private var onSlideListener: OnSlideListener = object : OnSlideListener {
		override fun onSlided(value: Int) {
			// Do nothing
		}
	}

	val thumbPaint = Paint()
	val barPaint = Paint()

	val thumbRadius = 80F
	val barRadius = 60F

	var cx = 0F
	var cy = 0F

	var previousValue = 0
	var value = 0

	init {
		thumbPaint.color = Color.BLUE
		thumbPaint.isAntiAlias = true
		barPaint.color = Color.GRAY
		barPaint.isAntiAlias = true
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)

		val mode = MeasureSpec.getMode(widthMeasureSpec)

		setMeasuredDimension(MeasureSpec.makeMeasureSpec((2 * thumbRadius).toInt(), mode), heightMeasureSpec)
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		val width = this.width.toFloat()
		val height = this.height.toFloat()

		val left = thumbRadius - barRadius
		var top = 0F
		val right = left + 2 * barRadius
		var bottom = 2 * barRadius
		var oval = RectF(left, top, right, bottom)

		var startAngle = 180F
		val sweepAngle = 180F
		val useCenter = false

		canvas.drawArc(oval, startAngle, sweepAngle, useCenter, barPaint)

		val top2 = height - barRadius * 2
		val bottom2 = height
		oval = RectF(left, top2, right, bottom2)

		startAngle = 0F

		canvas.drawArc(oval, startAngle, sweepAngle, useCenter, barPaint)

		canvas.drawRect(
			/* left = */ left,
			/* top = */ bottom - barRadius,
			/* right = */ right,
			/* bottom = */ top2 + barRadius,
			/* paint = */ barPaint
		)

		cx = width / 2F
		if (!isPressed) {
			cy = height / 2F
		}
		val radius = 80F

		canvas.drawCircle(cx, cy, radius, thumbPaint)
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {

		val action = event.actionMasked

		when (action) {
			MotionEvent.ACTION_DOWN -> {
//				log("Down")
				isPressed = true
				cy = event.y
			}
			MotionEvent.ACTION_MOVE -> {
//				log("Move")
				cy = event.y
			}
			MotionEvent.ACTION_UP -> {
//				log("Up")
				isPressed = false
			}
		}
		normalize()
		invalidate()

		return true
	}

	private fun normalize() {
		if (cy < thumbRadius) cy = thumbRadius
		if (cy > height - thumbRadius) cy = height - thumbRadius

		val bottom = height - thumbRadius
		val top = thumbRadius

		/*
		 * center    -> value = 0
		 * bottom    -> value = -255
		 * top       -> value = 255
		 */

		val m = (bottom - top) / 2
		val center = top + m
		val b = (-255 * center) / m
		val k = 255 / m
		previousValue = value
		value = (k * cy + b).toInt()

//		log("top: $top")
//		log("bottom: $bottom")
//		log("m: $m")
//		log("center: $center")
//		log("b: $b")
//		log("k: $k")
//		log("cy: $cy")
//		log("value: $value")

		if (previousValue != value) {
			onSlideListener.onSlided(value)
		}
	}

	fun setOnSlideListener(onSlideListener: OnSlideListener) {
		this.onSlideListener = onSlideListener
	}

	private fun log(message: String) {
		Log.d("Slider->", message)
	}

}