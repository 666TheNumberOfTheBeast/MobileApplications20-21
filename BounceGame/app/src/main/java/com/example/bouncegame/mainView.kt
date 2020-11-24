package com.example.bouncegame

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs


class mainView(context: Context?) : View(context), View.OnTouchListener {

    private var previous = System.currentTimeMillis()

    // Ball variables
    private val radius = 100f
    private var cx = radius
    private var cy = radius

    // Velocity components
    private var vx = 200f
    private var vy = 400f

    // Variable to detect when the user touchs the screen
    private var touched = false
    // How close the finger needs to be for touching the ball
    private var howClose = 1.2f

    // Shader matrix
    private val mx = Matrix()

    private val ballPainter = Paint().apply {
        shader = RadialGradient(0f, 0f, radius, Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP)
    }

    // Finger coordinates
    private var fx = 0f
    private var fy = 0f

    init {
        setOnTouchListener(this)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        fun updateBallPosition() {
            // Compute time difference
            val now = System.currentTimeMillis()
            val dt = now - previous
            previous = now


            // Invert the velocity if the left edge has been reached and it's the 1st frame that detects the event
            if (cx - radius < 0 && vx < 0)
                vx *= -1
            // Else invert the velocity if the right edge has been reached and it's the 1st frame that detects the event
            else if (cx + radius > width && vx > 0)
                vx *= -1

            // Invert the velocity if the top edge has been reached and it's the 1st frame that detects the event
            if (cy - radius < 0 && vy < 0)
                vy *= -1
            // Else invert the velocity if the bottom edge has been reached and it's the 1st frame that detects the event
            else if (cy + radius > height && vy > 0)
                vy *= -1


            // Check if the user touches the screen
            if (touched) {

                //Log.d("finger", "fx: " + fx + "\tfy: " + fy)
                //Log.d("finger", "vx: " + vx + "\tvy: " + vy)
                //Log.d("finger", "cy-radius: " + (cy-radius) + "\tcy+radius: " + (cy+radius))

                // Check if the finger X coordinate is close to the ball
                if (abs(fx - cx) <= radius) {
                    //Log.d("finger", "finger x is near")

                    if (abs(fy - cy) <= radius*howClose) {
                        //Log.d("finger", "finger y is near")

                        if (cy - radius < fy && vy < 0) {
                            //Log.d("finger", "finger is over the ball")
                            vy *= -1
                        }
                        else if (cy + radius < fy && vy > 0) {
                            //Log.d("finger", "finger is below the ball")
                            vy *= -1
                        }
                    }

                }
            }


            // Calculate new ball center
            cx += vx * dt / 1000
            cy += vy * dt / 1000

            // Shader offset
            val offx = 2*cx / width -1f
            val offy = 2*cx / height -1f

            //Log.d("ball", "cx: " + cx + "\tcy: " + cy)
            //Log.d("ball", "offx: " + offx + "\toffy: " + offy)
            //Log.d("bal",  "" + (cx + radius*offx) + "\t: " + (cy + radius*offy))

            // Set translation to the matrix for the shader
            mx.setTranslate(cx + 0.6f*radius*offx, cy + 0.4f*radius*offy)
            ballPainter.shader.setLocalMatrix(mx)

            canvas?.drawCircle(cx, cy, radius, ballPainter)
            invalidate()
        }

        updateBallPosition()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                // Set the finger coordinates
                fx = event.x
                fy = event.y
                touched = true
            }
            MotionEvent.ACTION_UP -> {
                // Reset the finger coordinates
                fx = 0f
                fy = 0f
                touched = false
            }
        }


        return true
    }
}