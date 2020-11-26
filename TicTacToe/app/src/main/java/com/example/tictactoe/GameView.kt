package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

import android.os.Handler
import android.os.Looper

import android.util.Log
import android.widget.Toast

import android.view.MotionEvent
import android.view.View

import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import com.example.tictactoe.configuration.Configuration.Companion.ID
import com.example.tictactoe.configuration.Configuration.Companion.MULTIPLAYER
import com.example.tictactoe.configuration.Configuration.Companion.NEW_MOVE
import com.example.tictactoe.configuration.Configuration.Companion.POLLING
import com.example.tictactoe.configuration.Configuration.Companion.URL
import com.example.tictactoe.configuration.Configuration.Companion.POLLING_PERIOD

import kotlin.math.min

class GameView(context: Context?) : View(context), View.OnTouchListener {

    // Paint for the grid
    private val gridPaint = Paint().apply{
        style=Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth=3f
    }

    // Paint for the marks
    private val mPaint = Paint().apply{
        style=Paint.Style.STROKE
        color = Color.BLUE
        strokeWidth=3f
    }

    // Grid horizontal and vertical lines
    private val hlines = 2
    private val vlines = 2

    // Size of each cell of the grid
    private var dx = 0f
    private var dy = 0f

    // Define game constants
    private val UNDEFINED = -1
    private val PLAYER1   =  0
    private val PLAYER2   =  1

    // Init winner and grid cells matrix
    private var winner = UNDEFINED
    private var cells = Array(hlines+1) { IntArray(vlines+1) {UNDEFINED} }

    // Variables for the 2 players version
    private val queue = Volley.newRequestQueue(context)

    init {
        setOnTouchListener(this)

        // Check if multiplayer mode
        if (MULTIPLAYER)
            poll()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        Toast.makeText(context, "id: " + ID, Toast.LENGTH_SHORT).show()

        // Get the size of each cell of the grid
        dx = width/(vlines + 1f)
        dy = height/(hlines + 1f)

        // Draw rows of the grid
        for (row in 1..hlines) {
            val y = dy*row
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
        }

        // Draw cols of the grid
        for (col in 1..vlines) {
            val x = dx*col
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
        }


        // Function to draw O
        fun drawO(row: Int, col:Int) {
            val halfCellX = dx/2
            val halfCellY = dy/2

            canvas.drawCircle(col*dx + halfCellX, row*dy + halfCellY, min(halfCellX, halfCellY), mPaint)
        }

        // Function to draw X
        fun drawX(row: Int, col:Int) {
            val cellX = col*dx
            val cellY = row*dy

            canvas.drawLine(cellX + dx/5, cellY + dy/5, cellX + dx * 4/5, cellY + dy * 4/5, mPaint)
            canvas.drawLine(cellX + dx/5, cellY + dy * 4/5, cellX + dx * 4/5, cellY + dy/5, mPaint)
        }

        // Draw marks
        for (row in 0..hlines)
            for (col in 0..vlines) {
                //Log.d("draw marks", "cells[" + row + "][" + col + "]: " + cells[row][col])

                if (cells[row][col] == PLAYER1)         drawO(row, col)
                else if (cells[row][col] == PLAYER2)    drawX(row, col)
            }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (dx == 0f || dy == 0f)   return false

        when(event?.action) {
            // Get the clicked cell
            MotionEvent.ACTION_DOWN -> {
                // Start a new game if the player tap the screen and the game is finished
                if (winner != UNDEFINED) {
                    newGame()
                    return true
                }

                val row = (event.y / dy).toInt()
                val col = (event.x / dx).toInt()

                val res = setO(row, col)
                if (res == false) {
                    Toast.makeText(context, "You cannot overwrite a mark", Toast.LENGTH_SHORT).show()
                    return false
                }

                if (MULTIPLAYER)
                    postMove(row, col)

                checkWinner()

                if (!MULTIPLAYER && winner == UNDEFINED)
                    // Reply to player move
                    moveIA()
                else if (winner == PLAYER1)
                    Toast.makeText(context, "Win", Toast.LENGTH_SHORT).show()
            }
        }

        return true
    }

    // Set O mark in the grid cell (row, col)
    private fun setO(row: Int, col:Int): Boolean {
        if (row < 0 || col < 0 || row > hlines || col > vlines || cells[row][col] != UNDEFINED)   return false

        //Toast.makeText(context, "O drawn at cell (" + row + "," + col + ")", Toast.LENGTH_SHORT).show()
        cells[row][col] = PLAYER1
        invalidate()

        return true
    }

    // Set X mark in the grid cell (row, col)
    private fun setX(row: Int, col:Int): Boolean {
        if (row < 0 || col < 0 || row > hlines || col > vlines || cells[row][col] != UNDEFINED)   return false

        //Toast.makeText(context, "X drawn at cell (" + row + "," + col + ")", Toast.LENGTH_SHORT).show()
        cells[row][col] = PLAYER2
        invalidate()

        return true
    }

    // Check winner and set the winner variable if any
    private fun checkWinner() {
        // Check rows
        for (row in 0..hlines) {
            var tris = 0

            for (col in 0 until vlines) {
                //Log.d("check rows", "cells[" + row + "][" + col + "]: " + cells[row][col] + "\tcells[" + row + "][" + (col+1) + "]: " + cells[row][col+1])

                if (cells[row][col] == UNDEFINED)               break
                else if (cells[row][col] != cells[row][col+1])  break
                else                                            tris++
            }

            if (tris == hlines) {
                winner = cells[row][0]
                return
            }
        }

        // Check cols
        for (col in 0..vlines) {
            var tris = 0

            for (row in 0 until hlines) {
                //Log.d("check rows", "cells[" + row + "][" + col + "]: " + cells[row][col] + "\tcells[" + (row+1) + "][" + col + "]: " + cells[row][col+1])

                if (cells[row][col] == UNDEFINED)               break
                else if (cells[row][col] != cells[row+1][col])  break
                else                                            tris++
            }

            if (tris == hlines) {
                winner = cells[0][col]
                return
            }
        }


        // Check diagonal (0,0), (1,1), (2,2)
        var tris = 0
        for (row in 0 until hlines) {
            val col = row
            //Log.d("check rows", "cells[" + row + "][" + col + "]: " + cells[row][col] + "\tcells[" + (row+1) + "][" + (col+1) + "]: " + cells[row][col+1])

            if (cells[row][col] == UNDEFINED)                 break
            else if (cells[row][col] != cells[row+1][col+1])  break
            else                                              tris++
        }

        if (tris == hlines) {
            winner = cells[0][0]
            return
        }


        // Check diagonal (2,0), (1,1), (0,2)
        tris = 0
        var col = 0
        for (row in hlines downTo 1) {

            if (cells[row][col] == UNDEFINED)                 break
            else if (cells[row][col] != cells[row-1][col+1])  break
            else                                              tris++

            col++
        }

        if (tris == hlines) {
            winner = cells[hlines][0]
            return
        }
    }

    private fun moveIA() {
        // Find first free cell
        for (row in 0..hlines)
            for (col in 0..vlines) {
                if (cells[row][col] == UNDEFINED) {
                    setX(row, col)

                    checkWinner()
                    if (winner == PLAYER2)
                        Toast.makeText(context, "Lose", Toast.LENGTH_SHORT).show()

                    return
                }
            }
    }

    // Reset game variables
    private fun newGame() {
        winner = UNDEFINED

        for (row in 0..hlines)
            for (col in 0..vlines)
                cells[row][col] = UNDEFINED

        invalidate()
    }


    // 2 players version functions

    // Get adversary move from the server
    private fun getMove() {
        // Request a JSON response from the provided URL
        val stringRequest = JsonObjectRequest(
            Request.Method.GET,
            URL + "req=" + POLLING + "&who=" + ID,
            null,
            { response ->
                Log.d("getMove", response.toString())

                // Get last adversary move
                val move = response.getInt("move")

                // Map the unroll in the matrix
                val row = move / (hlines+1)
                val col = move % (hlines+1)

                // Set adversary move
                setX(row, col)

                checkWinner()
                if (winner == PLAYER2)
                    Toast.makeText(context, "Lose", Toast.LENGTH_SHORT).show()
            },
            { error -> Log.d("getMove", error.toString()) }
        )

        // Add the request to the RequestQueue
        queue.add(stringRequest)
    }

    // Post move to the server
    private fun postMove(row: Int, col: Int) {
        // Unroll the matrix in an array index
        val move = (hlines + 1) * row + col

        // Request a JSON response from the provided URL
        val stringRequest = JsonObjectRequest(
            Request.Method.POST,
            URL + "req=" + NEW_MOVE + "&who=" + ID + "&move=" + move,
            null,
            { response -> Log.d("postMove", response.toString()) },
            { error -> Log.d("postMove", error.toString()) }
        )

        // Add the request to the RequestQueue
        queue.add(stringRequest)
    }

    // Continuos polling
    private fun poll() {
        getMove()
        Handler(Looper.myLooper()!!).postDelayed({ poll() }, POLLING_PERIOD)
    }

}