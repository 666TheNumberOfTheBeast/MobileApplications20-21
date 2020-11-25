package com.example.cannongame

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap

class mainView(context: Context?) : View(context), View.OnTouchListener {

    private var converted = false
    private lateinit var cannon: Bitmap

    private val cannonScale = 0.3f

    // Graphics cannon matrix
    private var cannonMatrix = Matrix()
    private var ang = 0f

    // Cannon ball graphics variables
    private val radius = 50f
    private var cx = 0f
    private var cy = 0f

    private val ballPainter = Paint().apply {
        shader = RadialGradient(0f, 0f, radius, Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP)
    }

    // Graphics shader matrix
    private var shaderMatrix = Matrix()

    // Variables for handling the shooting
    private var fired = false
    private var previous = 0L

    // Cannon ball velocity components
    private var vx = 0f
    private var vy = 0f

    // World constants
    private val speed = 450f //1000f // m/s
    private val g = 200f     //9.81f // m/s^2

    // Finger coordinates
    private var fx = 0.0
    private var fy = 0.0

    init {
        setOnTouchListener(this)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Convert the image once
        if (!converted) {
            converted = true
            cannon = ResourcesCompat.getDrawable(resources, R.drawable.ic_cannon, null)?.toBitmap(height, height)!!
        }

        // Scale the bitmap
        cannonMatrix.setScale(cannonScale, cannonScale)

        // Set translation values
        val dx = 30f
        val dy = height.toFloat() - cannon.height*cannonScale

        // Put the bitmap in the bottom left in landscape mode
        cannonMatrix.postTranslate(dx, dy)

        // Rotate the bitmap based on the angle
        cannonMatrix.postRotate(-ang, dx, dy)

        canvas?.drawBitmap(cannon, cannonMatrix, null)

        // Check if the cannon has fired
        if (!fired)     return

        // Check if the cannon ball is outside the screen
        if (cx - radius > width || cy - radius > height) {
            fired = false
            return
        }

        // Compute time difference
        val now = System.currentTimeMillis()
        val dt = now - previous
        previous = now

        // Calculate new ball center
        // Greater values at right
        cx += vx * dt / 1000f

        // Greater values at bottom
        //cy += 0.5f * g * dt/1000f * dt/1000f - vy * dt / 1000f
        cy -= vy * dt / 1000f

        // Update Y velocity
        vy -= g * dt / 1000f

        Log.d("ball", "cx: " + cx + "\tcy: " + cy)

        // Set translation to the matrix for the shader
        shaderMatrix.setTranslate(cx - 0.3f*radius, cy - 0.3f*radius)
        ballPainter.shader.setLocalMatrix(shaderMatrix)

        canvas?.drawCircle(cx, cy, radius, ballPainter)
        invalidate()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                // Get user click coordinates
                fx = event.x.toDouble()
                fy = height.toDouble() - event.y.toDouble()

                // Get the angle in radiant
                val rad = Math.atan2(fy, fx)

                // Convert radiant to degrees and clamp max value
                ang = Math.toDegrees(rad).toFloat() / 2
                //Log.d("angle", "ang: " + ang)

                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                fired = true
                previous = System.currentTimeMillis()

                // Calculate cosine of the angle
                val cos = Math.cos( Math.toRadians(ang.toDouble()) ).toFloat()

                // Calculate cannon ball center
                cx = (cannon.width*cannonScale + radius) * cos
                cy = (height.toFloat() - cannon.height*cannonScale) * cos

                //Log.d("ball", "cx: " + cx + "\tcy: " + cy)

                // Set velocity components of the cannon ball
                vx = speed * cos
                vy = speed * Math.sin( Math.toRadians(ang.toDouble()) ).toFloat()

                invalidate()
            }
        }

        return true
    }
}