package com.shenhua.oneread.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.TranslateAnimation

/**
 * Created by shenhua on 2017-08-03-0003.
 * Email shenhuanet@126.com
 */
class ElasticScrollView @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : NestedScrollView(context, attrs, defStyleAttr) {

    private var childView: View? = null
    private var distance: Float = 0.toFloat()
    private var normal = Rect()

    init {
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    override fun onFinishInflate() {
        if (childCount > 0) {
            childView = getChildAt(0)
            Handler().postDelayed({ childView!!.minimumHeight = height + 2 }, TIME.toLong())
        }
        super.onFinishInflate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> distance = ev.y
            MotionEvent.ACTION_MOVE -> {
                val oldY = distance
                val newY = ev.y
                val deltaY = (oldY - newY).toInt() / SIZE
                distance = newY
                if (needMove) {
                    if (normal.isEmpty) {
                        normal.set(childView!!.left, childView!!.top, childView!!.right, childView!!.bottom)
                        return super.onTouchEvent(ev)
                    }
                    val yy = childView!!.top - deltaY
                    childView!!.layout(childView!!.left, yy, childView!!.right, childView!!.bottom - deltaY)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (needAnim) anim()
            }
        }
        return super.onTouchEvent(ev)
    }

    private fun anim() {
        val ta = TranslateAnimation(0f, 0f, childView!!.top.toFloat(), normal.top.toFloat())
        ta.duration = TIME.toLong()
        childView!!.startAnimation(ta)

        childView!!.layout(normal.left, normal.top, normal.right, normal.bottom)
        normal.setEmpty()
    }

    val needAnim: Boolean
        get() = !normal.isEmpty

    val needMove: Boolean get() {
        val offset = childView!!.measuredHeight - height
        return (scrollY == 0 || scrollY == offset)
    }

    fun setSize(size: Int) {
        SIZE = size
        invalidate()
    }

    companion object {
        private var SIZE = 3
        private var TIME = 300
    }

}
