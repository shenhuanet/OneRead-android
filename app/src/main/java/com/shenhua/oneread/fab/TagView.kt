package com.shenhua.oneread.fab

import android.content.Context
import android.support.v7.widget.CardView
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

/**
 * A label View for FloatingActionButtonPlus
 * Created by shenhua on 2017-08-01-0001.
 * Email shenhuanet@126.com
 */
class TagView(context: Context) : CardView(context) {

    private val mTextView: TextView = TextView(context)

    init {
        mTextView.setSingleLine(true)
    }

    fun setTextSize(size: Float) {
        mTextView.textSize = size
    }

    fun setTextColor(color: Int) {
        mTextView.setTextColor(color)
    }

    fun setTagText(text: String) {
        mTextView.text = text
        positionView()
    }

    private fun positionView() {
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        val l = convertDp(8)
        val r = convertDp(8)
        val t = convertDp(4)
        val b = convertDp(4)
        layoutParams.setMargins(l, t, r, b)
        addView(mTextView, layoutParams)
    }

    private fun convertDp(value: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
    }
}
