package com.example.project5

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.View

class GameView : View {
    private lateinit var paint : Paint
    private lateinit var brickBreaker : BrickBreaker

    constructor(context : Context, width : Int, height : Int) : super(context) {
        paint = Paint()
        paint.strokeWidth = 20f
        paint.isAntiAlias = true

        brickBreaker = BrickBreaker(context,
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels,
            10, 0.5f)
        brickBreaker.setDeltaTime(60)
    }

    override fun onDraw(canvas : Canvas) {
        super.onDraw(canvas)

        if (brickBreaker.isGameOver()) {
            paint.color = Color.BLACK
            paint.textSize = 50f
            canvas.drawText("${brickBreaker.getTotalHit()} bricks hit, " +
                    "${brickBreaker.getTotalLeft()} bricks left",
                50f, 700f, paint)

            //Log.w("MainActivity", "Best score: ${brickBreaker.getBestScore()}")
            canvas.drawText("Best score: ${brickBreaker.getBestScore()}", 50f, 800f, paint)

            if (brickBreaker.getTotalHit() >= brickBreaker.getBestScore()) {
                canvas.drawText("New best score !!", 50f, 900f, paint)
            }
        }

        //draw circle (ball)
        paint.color = Color.BLACK
        canvas.drawCircle(brickBreaker.getBallCenter().x.toFloat(),
            brickBreaker.getBallCenter().y.toFloat(), brickBreaker.getBallRadius().toFloat(), paint)

        //draw line (paddle)
        paint.color = Color.BLACK
        canvas.drawLine(brickBreaker.getPaddleStartPosX(),brickBreaker.getPaddleStartPosY(),
            brickBreaker.getPaddleStopPosX(),brickBreaker.getPaddleStopPosY(), paint)

        //draw rects (bricks)
        var width : Int = brickBreaker.getBrickWidth()
        var height : Int = brickBreaker.getBrickHeight()

        //draw bricks (4 rows, 6 columns)
        for (row in 0..4 - 1) {
            for (col in 0..6 - 1) {
                if (brickBreaker.brickBroken(row, col)) {
                    continue // Skip broken bricks
                }
                paint.color = COLORS[(row + col) % 2]
                val left = col * width.toFloat()
                val top = row * height.toFloat()
                val right = left + width
                val bottom = top + height
                canvas.drawRect(left, top, right, bottom, paint)
            }
        }
    }

    fun getGame() : BrickBreaker {
        return brickBreaker
    }

    companion object {
        private var COLORS: Array<Int> = arrayOf(Color.CYAN, Color.MAGENTA)
    }

}