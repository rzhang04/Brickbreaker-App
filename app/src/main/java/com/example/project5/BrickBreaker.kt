package com.example.project5

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.graphics.Rect

class BrickBreaker {
    private var deltaTime = 0 //in milliSeconds

    //bricks
    private lateinit var bricks: Array<Array<Rect?>>
    private var brickWidth = 0
    private var brickHeight = 0
    private lateinit var brickBroken: Array<Array<Boolean>>
    
    //ball
    private var ballCenter : Point? = Point(800, 500)
    private var ballRadius = 0
    private var ballAngle = 0f
    private var ballSpeed = 0f
    private var ballIsMoving : Boolean = false

    //paddle
    private var paddleStartPosX = 200f
    private var paddleStartPosY = 2200f
    private var paddleStopPosX = 380f
    private var paddleStopPosY = 2200f

    private var totalHit = 0
    private var totalLeft = 24
    private var bestScore = 0
    private var isGameOver = false

    constructor(context : Context,
                screenWidth : Int, screenHeight : Int,
                newBallRadius: Int, newBallSpeed: Float) {
        var pref : SharedPreferences =
            context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
        bestScore = pref.getInt(BEST_SCORE, 0)

        brickWidth = screenWidth / 6
        brickHeight = screenHeight / 24
        bricks = Array<Array<Rect?>>(4, {i -> Array<Rect?>(6, {j ->
            val left = j * brickWidth
            val top = i * brickHeight
            val right = left + brickWidth
            val bottom = top + brickHeight
            Rect(left, top, right, bottom)})})
        brickBroken = Array<Array<Boolean>> (4, {i -> Array<Boolean>(6, {j -> false})})

        setBallRadius(newBallRadius)
        setBallSpeed(newBallSpeed)
        ballAngle = 45f //starting ball angle
        ballIsMoving = false

    }

    fun setDeltaTime(newDeltaTime: Int) {
        if (newDeltaTime > 0)
            deltaTime = newDeltaTime
    }

    fun getTotalHit() : Int {
        return totalHit
    }

    fun getTotalLeft() : Int {
        return totalLeft
    }

    fun getBestScore() : Int {
        return bestScore
    }

    //brick related methods below
    fun getBrickWidth() : Int {
        return brickWidth
    }

    fun getBrickHeight() : Int {
        return brickHeight
    }

    //ball related methods below
    fun getBallCenter() : Point {
        return ballCenter!!
    }
    fun getBallRadius() : Int {
        return ballRadius
    }

    fun setBallRadius(newBallRadius: Int) {
        if (newBallRadius > 0)
            ballRadius = newBallRadius
    }

    fun setBallSpeed(newBallSpeed: Float) {
        if (newBallSpeed > 0)
            ballSpeed = newBallSpeed
    }

    fun moveBall() {
        ballCenter!!.x -= (ballSpeed * Math.cos(ballAngle.toDouble()) * deltaTime).toInt()
        ballCenter!!.y += (ballSpeed * Math.sin(ballAngle.toDouble()) * deltaTime).toInt()
    }

    fun ballHitWall(screenWidth: Int) : Boolean {
        //left, right, top are walls, bottom is not
        val isBeyondLeft = ballCenter!!.x - ballRadius < 0
        val isBeyondRight = ballCenter!!.x + ballRadius > screenWidth
        val isBeyondTop = ballCenter!!.y - ballRadius < 0

        return isBeyondLeft || isBeyondRight || isBeyondTop
    }

    fun bounceOffWall(screenWidth : Int) {
        //ball hit the left or right wall
        if (ballCenter!!.x - ballRadius < 0 || ballCenter!!.x + ballRadius > screenWidth) {
            ballAngle = (Math.PI - ballAngle.toDouble()).toFloat()
        }

        //ball hit top wall
        if (ballCenter!!.y - ballRadius < 0) {
            ballAngle = -ballAngle
        }
    }

    fun ballHitPaddle() : Boolean {
        val paddleRect = Rect(
            getPaddleStartPosX().toInt(),
            getPaddleStartPosY().toInt(),
            getPaddleStopPosX().toInt(),
            (getPaddleStopPosY() + ballRadius).toInt() //ball padding
        )

        val ballRect = Rect(
            ballCenter!!.x - ballRadius,
            ballCenter!!.y - ballRadius,
            ballCenter!!.x + ballRadius,
            ballCenter!!.y + ballRadius
        )
        return Rect.intersects(paddleRect, ballRect)
    }

    fun bounceOffPaddle() {
        ballAngle = -ballAngle
    }

    fun ballHitBrick() : Pair<Int, Int>? {
        for (i in 0 .. bricks.size - 1) {
            for (j in 0 .. bricks[i].size - 1) {
                val brick = bricks[i][j]

                //skip if brick is already broken
                if (brickBroken[i][j]) {
                    continue
                }

                //intersection of brick and ball
                if (Rect.intersects(brick!!, Rect(
                        ballCenter!!.x - ballRadius,
                        ballCenter!!.y - ballRadius,
                        ballCenter!!.x + ballRadius,
                        ballCenter!!.y + ballRadius))) {
                    return Pair(i, j)
                }
            }
        }
        //no collision
        return null
    }

    fun brickBroken(i : Int?, j : Int?) : Boolean {
        if (i == null || j == null) {
            return false
        }
        if (brickBroken[i][j]) {
            return true
        } else {
            return false
        }
    }

    fun ballBrokeBrick(i : Int, j : Int) {
        brickBroken[i][j] = true
        totalHit++
        totalLeft--

        //get the brick rectangle
        val brick = bricks[i][j]
        if (brick == null) {
            return
        }

        //get the ball's bounding box
        val ballRect = Rect(
            ballCenter!!.x - ballRadius,
            ballCenter!!.y - ballRadius,
            ballCenter!!.x + ballRadius,
            ballCenter!!.y + ballRadius
        )

        //check where ball hit brick
        val brickLeft = brick.left
        val brickRight = brick.right
        val brickTop = brick.top
        val brickBottom = brick.bottom

        //ball hit the left or right side of the brick
        if (ballRect.right > brickLeft && ballRect.left < brickRight) {
            if (ballRect.top < brickBottom && ballRect.bottom > brickTop) {
                ballAngle = (Math.PI - ballAngle.toDouble()).toFloat()
            }
        }

        //ball hit the top or bottom side of the brick
        if (ballRect.bottom > brickTop && ballRect.top < brickBottom) {
            if (ballRect.left < brickRight && ballRect.right > brickLeft) {
                ballAngle = -ballAngle //reverse the vertical direction
            }
        }
    }

    fun ballOffScreen(screenHeight: Int) : Boolean {
        val isBeyondBottom = ballCenter!!.y + ballRadius > screenHeight
        if (isBeyondBottom) {
            setGameOver()
        }
        return isBeyondBottom
    }

    fun isBallMoving(): Boolean {
        return ballIsMoving
    }

    //paddle related methods
    fun getPaddleStartPosX() : Float {
        return paddleStartPosX
    }

    fun getPaddleStartPosY() : Float {
        return paddleStartPosY
    }

    fun getPaddleStopPosX() : Float {
        return paddleStopPosX
    }

    fun getPaddleStopPosY() : Float {
        return paddleStopPosY
    }

    fun setPaddleStartPosX(startPosX : Float) {
        paddleStartPosX = startPosX
    }

    fun setPaddleStopPosX(stopPosX : Float) {
        paddleStopPosX = stopPosX
    }

    //game related methods below
    fun gameStarted() {
        ballIsMoving = true
    }

    fun isGameOver(): Boolean {
        return isGameOver
    }

    fun setGameOver() {
        isGameOver = true
        ballIsMoving = false
    }

//I use this to test my code, please ignore!!
//    fun reset(context : Context) {
//        val pref: SharedPreferences = context.getSharedPreferences(
//            context.packageName + "_preferences", Context.MODE_PRIVATE
//        )
//        val editor: SharedPreferences.Editor = pref.edit()
//        editor.clear()
//        editor.commit()
//    }

    fun setPreferences(context: Context) {
        var pref : SharedPreferences =
            context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
        var editor : SharedPreferences.Editor = pref.edit()

        //check if totalhit is better than current best
        if (totalHit >= bestScore) {
            bestScore = totalHit
        }
        editor.putInt(BEST_SCORE, bestScore)
        editor.commit()
    }

    companion object {
        private const val BEST_SCORE : String = "score"
    }

}