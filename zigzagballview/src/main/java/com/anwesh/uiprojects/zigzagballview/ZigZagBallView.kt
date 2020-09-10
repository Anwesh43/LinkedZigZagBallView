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
val rFactor : Float = 13.9f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.max(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawZigZagBall(scale : Float, w : Float, h : Float, paint : Paint) {
    val scFirst : Float = scale.divideScale(0, parts)
    val scLast : Float = scale.divideScale(parts - 1, parts)
    val r : Float = Math.min(w, h) / rFactor
    val gap : Float = w
    save()
    translate(0f, h / 2)
    for (i in 0..1) {
        var x : Float = 0f
        var y : Float = 0f
        save()
        scale(1f, 1f - 2 * i)
        for (j in 1..divs) {
            val scj : Float = scale.divideScale(j, parts)
            x += gap * scj
            y -= gap * scj.sinify()
        }
        drawCircle(x, y, r * (scFirst - scLast), paint)
        restore()
    }
    restore()
}

fun Canvas.drawZZBNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    drawZigZagBall(scale, w, h, paint)
}

class ZigZagBallView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class ZZBNode(var i : Int, val state : State = State()) {

        private var next : ZZBNode? = null
        private var prev : ZZBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = ZZBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawZZBNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : ZZBNode {
            var curr : ZZBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class ZigZagBall(var i : Int) {

        private var curr : ZZBNode = ZZBNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : ZigZagBallView) {

        private val animator : Animator  = Animator(view)
        private val zzb : ZigZagBall = ZigZagBall(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            zzb.draw(canvas, paint)
            animator.animate {
                zzb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            zzb.startUpdating {
                animator.start()
            }
        }
    }
}