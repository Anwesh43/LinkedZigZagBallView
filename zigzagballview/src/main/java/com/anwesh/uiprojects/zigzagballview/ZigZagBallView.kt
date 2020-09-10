package com.anwesh.uiprojects.zigzagballview

/**
 * Created by anweshmishra on 11/09/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.app.Activity
import android.content.Context

val colors : Array<Int> = arrayOf(
        "#3F51B5",
        "#F44336",
        "#03A9F4",
        "#9C27B0",
        "#FFC107"
).map({Color.parseColor(it)}).toTypedArray()
val divs : Int = 3
val parts : Int = 2 + divs
val scGap : Float = 0.02f / parts
val rFactor : Float = 8.9f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
