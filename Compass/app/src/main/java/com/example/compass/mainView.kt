package com.example.compass

import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withMatrix

class mainView(context: Context?) : View(context), SensorEventListener2 {

    private var converted = false
    private lateinit var compass:Bitmap

    // Graphics matrices
    private var compassMatrix = Matrix()
    private var triangleMatrix = Matrix()

    private val triangleVertices = floatArrayOf(
        -100f,0f,
        100f, 0f,
        0f, 200f)

    private val triangleColors = intArrayOf(
        Color.RED,
        Color.RED,
        Color.RED)

    private val trianglePaint = Paint().apply {}

    // Sensors values
    private var lastAcceleration = FloatArray(3)
    private var lastMagnetic     = FloatArray(3)

    // Orientation and rotation matrices
    private val mRotationMatrix = FloatArray(9)
    private val mOrientation    = FloatArray(9)

    // Yaw angle for rotating the bitmap
    private var yaw = 0f

    init {
        val sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )

        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Convert the image once
        if (!converted) {
            converted = true
            compass = ResourcesCompat.getDrawable(resources, R.drawable.ic_compass, null)?.toBitmap(width, width)!!
        }

        // Center the bitmap in portrait mode
        compassMatrix.setTranslate(0f, (height-width)/2f)
        // Scale the bitmap
        compassMatrix.postScale(0.7f, 0.7f, width/2f, height/2f)
        // Rotate the bitmap based on the yaw angle
        compassMatrix.postRotate(-yaw, width/2f, height/2f)

        canvas?.withMatrix (compassMatrix) {
            drawBitmap(compass, 0f, 0f, null)
        }

        // Set triangle position
        triangleMatrix.setTranslate(width/2f, 50f)

        canvas?.withMatrix (triangleMatrix) {
            drawVertices(
                Canvas.VertexMode.TRIANGLES,
                triangleVertices.size,
                triangleVertices,
                0,
                null,
                0,
                triangleColors,
                0,
                null,
                0,
                0,
                trianglePaint
            )
        }

    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onFlushCompleted(sensor: Sensor?) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER  -> lastAcceleration = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> lastMagnetic     = event.values.clone()
        }

        SensorManager.getRotationMatrix(mRotationMatrix, null, lastAcceleration, lastMagnetic)
        SensorManager.getOrientation(mRotationMatrix, mOrientation)

        // Convert the yaw angle to degrees
        yaw = (mOrientation[0] * 180f / Math.PI).toFloat()

        invalidate()
    }

}