package com.example.project5

import android.graphics.Rect
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import java.util.Timer

class MainActivity : AppCompatActivity() {
    private lateinit var gameView : GameView
    private lateinit var detector : GestureDetector

    private lateinit var pool : SoundPool
    private var paddleSoundId : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var poolBuilder : SoundPool.Builder = SoundPool.Builder()
        pool = poolBuilder.build()
        paddleSoundId = pool.load(this, R.raw.hit, 1)
    }

    fun playSound(id : Int) {
        pool.play(id, 2.0f, 2.0f, 0, 0, 1.0f)
    }

    fun updateModel() {
        var brickBreaker : BrickBreaker = gameView.getGame()
        var screenWidth : Int = resources.displayMetrics.widthPixels
        var screenHeight : Int = resources.displayMetrics.heightPixels

        //move ball
        if(brickBreaker.isBallMoving()) {
            brickBreaker.moveBall()
        }

        //ball bounce off wall
        if(brickBreaker.ballHitWall(screenWidth)) {
            Log.w("MainActivity", "ballhitWall")
            brickBreaker.bounceOffWall(screenWidth)
        }

        //ball bounce off paddle
        if(brickBreaker.ballHitPaddle()) {
            Log.w("MainActivity", "ballhitPaddle")
            brickBreaker.bounceOffPaddle()
            playSound(paddleSoundId)
        }

        //ball hits brick
        var brickIndices : Pair<Int, Int>? = brickBreaker.ballHitBrick()
        if(brickIndices != null) {
            brickBreaker.ballBrokeBrick(brickIndices.first, brickIndices.second)
        }

        //ball offscreen
        if (brickBreaker.ballOffScreen(screenHeight)) {
            //Log.w("MainActivity", "game over")
            brickBreaker.setPreferences(this)

            //ignore this method, this was for testing persistent data
//            brickBreaker.reset(this)
        }
    }

    fun updateView() {
        gameView.postInvalidate()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        buildViewByCode()
    }

    fun buildViewByCode() {
        var width : Int = resources.displayMetrics.widthPixels
        var height : Int = resources.displayMetrics.heightPixels
        var rectangle : Rect = Rect(0,0,0,0)

        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        var statusBar : Int = rectangle.top

        gameView = GameView(this, width, height - statusBar)
        setContentView(gameView)

        var handler : TouchHandler = TouchHandler()
        detector = GestureDetector(this, handler)
        detector.setOnDoubleTapListener(handler)

        var timer : Timer = Timer()
        var task : GameTimerTask = GameTimerTask(this)
        timer.schedule(task, 0, 100)
    }

    fun updatePaddle(e: MotionEvent) {
        val brickBreaker: BrickBreaker = gameView.getGame()
        val paddleWidth = brickBreaker.getPaddleStopPosX() - brickBreaker.getPaddleStartPosX()

        //center aligns with touch position
        val newStartX = e.x - (paddleWidth / 2)
        val newStopX = e.x + (paddleWidth / 2)

        //paddle stays in screen
        val screenWidth = resources.displayMetrics.widthPixels
        if (newStartX >= 0 && newStopX <= screenWidth) {
            brickBreaker.setPaddleStartPosX(newStartX)
            brickBreaker.setPaddleStopPosX(newStopX)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            detector.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }

    inner class TouchHandler : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            var brickBreaker : BrickBreaker = gameView.getGame()
            if(!brickBreaker.isBallMoving()) {
                brickBreaker.gameStarted()
            }
            return super.onSingleTapConfirmed(e)
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            updatePaddle(e2)
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }
}