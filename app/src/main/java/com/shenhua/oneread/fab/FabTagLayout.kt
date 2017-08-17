package com.shenhua.oneread.fab

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.shenhua.oneread.R

/**
 * include the FloatingActionButton and TagView {@Link com.shenhua.onereading.fab.TagView}
 * Created by shenhua on 2017-08-01-0001.
 * Email shenhuanet@126.com
 */
class FabTagLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {

    private var mTagText: String? = null
    private var mTagView: TagView? = null
    private var mOrientation = TO_RIGHT

    /*这个变量表示是单独使用，还是在FloatingActionButtonPlus中使用，以通过不同的方式获得LayoutParams*/
    private var mScene: Boolean = false

    private var mTagOnClickListener: TagOnClickListener? = null
    private var mFabOnClickListener: FabOnClickListener? = null

    interface TagOnClickListener {
        fun onClick()
    }

    interface FabOnClickListener {
        fun onClick()
    }

    fun setTagOnClickListener(onClickListener: TagOnClickListener) {
        mTagOnClickListener = onClickListener
    }

    fun setFabOnClickListener(onClickListener: FabOnClickListener) {
        mFabOnClickListener = onClickListener
    }

    init {
        getAttributes(context, attrs!!)
        settingsView(context)
    }

    private fun getAttributes(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FabTagLayout)
        mTagText = typedArray.getString(R.styleable.FabTagLayout_tagText)
        typedArray.recycle()
    }

    private fun settingsView(context: Context) {
        mTagView = TagView(context)
        mTagView!!.setTagText(mTagText!!)
        addView(mTagView)
    }

    /**
     * 设置tag显示的文字

     * @param text 显示的文字
     */
    fun setTagText(text: String) {
        mTagView!!.setTagText(text)
    }

    /**
     * 改变标标签的显示位置
     * 通过 TO_RIGHT or TO_LEFT来判断

     * @param orientation FloatingActionButton所处的方向
     */
    fun setOrientation(orientation: Int) {
        mOrientation = orientation
        invalidate()
    }

    /**
     * 设置使用场景，以通过不同方式获取LayoutParams

     * @param scene 表示是在FloatingActionButton中被使用还是单独被使用
     */
    fun setScene(scene: Boolean) {
        mScene = scene
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val ChildCount = childCount

        for (i in 0..ChildCount - 1) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val groupWidth = getChildAt(0).measuredWidth + getChildAt(1).measuredWidth + convertDp(24 + 8 + 8)
        val groupHeight = Math.max(getChildAt(0).measuredHeight, getChildAt(1).measuredHeight) + convertDp(12)

        val params: ViewGroup.MarginLayoutParams

        if (mScene) {
            params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        } else {
            params = layoutParams as ViewGroup.MarginLayoutParams
        }

        params.width = groupWidth
        params.height = groupHeight
        layoutParams = params

        val tagView = getChildAt(0)
        val FabView = getChildAt(1) as View

        val fabWidth = FabView.measuredWidth
        val fabHeight = FabView.measuredHeight
        val tagWidth = tagView.measuredWidth
        val tagHeight = tagView.measuredHeight

        var fl = 0
        val ft = 0
        var fr = 0
        var fb = 0
        var tl = 0
        var tt = 0
        var tr = 0
        var tb = 0

        when (mOrientation) {
            TO_RIGHT -> {
                /*FAB*/
                fl = tagWidth + convertDp(16)
                //                ft = convertDp(4);
                fr = fl + fabWidth
                fb = ft + fabHeight

                /*TAG*/
                tl = convertDp(8)
                tt = (fabHeight - tagHeight) / 2
                tr = tl + tagWidth
                tb = tt + tagHeight
            }
            TO_LEFT -> {
                /*FAB*/
                fl = convertDp(24)
                //                ft = convertDp(4);
                fr = fl + fabWidth
                fb = ft + fabHeight

                /*TAG*/
                tl = convertDp(32) + fabWidth
                tt = (fabHeight - tagHeight) / 2
                tr = tl + tagWidth
                tb = tt + tagHeight
            }
        }

        FabView.layout(fl, ft, fr, fb)
        tagView.layout(tl, tt, tr, tb)

        bindEvents(tagView, FabView)
    }

    private fun bindEvents(tagView: View, fabView: View) {
        tagView.setOnClickListener {
            if (mTagOnClickListener != null) {
                mTagOnClickListener!!.onClick()
            }
        }

        fabView.setOnClickListener {
            if (mFabOnClickListener != null) {
                mFabOnClickListener!!.onClick()
            }
        }
    }

    private fun convertDp(value: Int): Int {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
        } else {
            return 0
        }
    }

    companion object {
        val TO_RIGHT = 0
        val TO_LEFT = 1
    }
}
