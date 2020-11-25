package com.example.analogclock

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.graphics.withMatrix

class mainView(context: Context?) : View(context) {

    private val r = Rect()
    private val blackPaint = Paint().apply {
        style       = Paint.Style.STROKE
        color       = Color.BLACK
        strokeWidth = 3f
        textSize    = 80f
    }

    private val redPaint = Paint().apply {
        style       = Paint.Style.STROKE
        color       = Color.RED
        strokeWidth = 3f
    }

    private val secondsMatrix = Matrix()
    private val minutesMatrix = Matrix()
    private val hoursMatrix   = Matrix()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Draw the clock frame
        val cx = width/2f
        val cy = height/2f
        val radius = width/2f
        canvas?.drawCircle(cx, cy, radius, blackPaint)

        // Draw the clock numbers
        blackPaint.getTextBounds("12", 0, 1, r)
        val offX = (r.right - r.left)/2
        //val offY = (r.bottom - r.top)/2f

        /*for (i in 0 until 12) {
            val x1 = 1.1*radius*Math.cos(i*2.0*Math.PI/12)+cx
            val y1 = -1.1*radius*Math.sin(i*2.0*Math.PI/12)+cy

            val x2 = 0.9*radius*Math.cos(i*2.0*Math.PI/12)+cx
            val y2 = -0.9*radius*Math.sin(i*2.0*Math.PI/12)+cy
            canvas?.drawLine(x1.toFloat(),y1.toFloat(),x2.toFloat(),y2.toFloat(), blackPaint)
        }*/

        for (i in 1..12) {
            var x = 0.9 * radius * Math.cos(i*2.0*Math.PI/12 - Math.PI/2) + cx
            var y = 0.9 * radius * Math.sin(i*2.0*Math.PI/12 - Math.PI/2) + cy

            if (i < 10)
                x -= 2.0*offX
            else
                x -= 4.0*offX

            canvas?.drawText(i.toString(), x.toFloat(), y.toFloat(), blackPaint)
        }


        // Get current ms
        val now  = System.currentTimeMillis()

        // Convert current ms into s
        var sec = now / 1000.0

        // Convert current s into min
        var minute = sec / 60.0

        // Convert current min into h
        var hour = minute / 60.0

        // Get the second in the current minute => [0, 59]
        sec %= 60

        // Get the minute in the current hour => [0, 59]
        minute %= 60

        // Get the hour in the current day => [0, 11]
        //hour %= 24
        hour = hour%12 +1

        // Map current second to an angle
        var ang = sec * 6
        //Log.d("angle", "sec: " + sec + "\tangle: " + ang)

        secondsMatrix.setTranslate(cx, cy)
        secondsMatrix.postRotate(ang.toFloat(), cx, cy)

        canvas?.withMatrix (secondsMatrix) {
            drawLine(0f, 0f, 0f, -radius*0.9f, redPaint)
        }

        // Map current minute to an angle
        ang = minute * 6
        //Log.d("angle", "minute: " + minute + "\tangle: " + ang)

        minutesMatrix.setTranslate(cx, cy)
        minutesMatrix.postRotate(ang.toFloat(), cx, cy)

        canvas?.withMatrix (minutesMatrix) {
            drawLine(0f, 0f, 0f, -radius*0.9f, blackPaint)
        }

        // Map current hour to an angle
        ang = hour * 30     // 360/12 = 30
        //Log.d("angle", "hour: " + hour + "\tangle: " + ang)

        hoursMatrix.setTranslate(cx, cy)
        hoursMatrix.postRotate(ang.toFloat(), cx, cy)

        canvas?.withMatrix (hoursMatrix) {
            drawLine(0f, 0f, 0f, -radius*0.5f, blackPaint)
        }

        invalidate()
    }

}