package com.example.project5

import android.util.Log
import java.util.TimerTask

class GameTimerTask : TimerTask {
    private lateinit var activity : MainActivity

    constructor(activity : MainActivity) {
        this.activity = activity
    }

    override fun run() {
        //update model
        activity.updateModel()

        //update View
        activity.updateView()

    }

}