package com.shenhua.oneread.fab

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import com.shenhua.oneread.R

class FloatingActionButtonPlus @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {

    private var mSwitchFabRotateVal = 45f
    private var mAnimationDuration: Int = 0

    private var mPosition: Int = 0
    private var mAnimation: Int = 0
    private var mBackgroundColor: Int = 0
    private var mFabColor: ColorStateList? = null
    private var mIcon: Drawable? = null

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    private var mSwitchFab: FloatingActionButton? = null
    private var mBackView: View? = null

    private var mStatus: Boolean = false
    /**
     * 返回当前主Fab的显示状态

     * @return 显示的时候返回true，隐藏的时候返回false
     */
    var switchFabDisplayState = true
        private set
    private var mFirstEnter = true

    private var mOnItemClickListener: OnItemClickListener? = null
    private var mOnSwitchFabClickListener: OnSwitchFabClickListener? = null

    interface OnSwitchFabClickListener {
        fun onClick()
    }

    /**
     * 设置主Fab的点击时间，该点击事件只在展开前会响应

     * @param onSwitchFabClickListener 主Fab的点击事件接口
     */
    fun setOnSwitchFabClickListener(onSwitchFabClickListener: OnSwitchFabClickListener) {
        mOnSwitchFabClickListener = onSwitchFabClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(tagView: FabTagLayout, position: Int)
    }

    /**
     * 设置每一item的点击事件，每一个item中的Fab的Tag均会会响应这同一个事件

     * @param itemClickListener 每一个item的点击事件接口
     */
    fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
        mOnItemClickListener = itemClickListener
    }

    init {
        getAttributes(context, attrs!!)
        settingsView(context)
    }

    private fun getAttributes(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButtonPlus)
        mPosition = typedArray.getInt(R.styleable.FloatingActionButtonPlus_position, POS_RIGHT_BOTTOM)
        mAnimation = typedArray.getInt(R.styleable.FloatingActionButtonPlus_animationMode, ANIM_SCALE)
        mBackgroundColor = typedArray.getColor(R.styleable.FloatingActionButtonPlus_mBackgroundColor, 0xf2ffffff.toInt())
        mFabColor = typedArray.getColorStateList(R.styleable.FloatingActionButtonPlus_switchFabColor)
        mIcon = typedArray.getDrawable(R.styleable.FloatingActionButtonPlus_switchFabIcon)
        mAnimationDuration = typedArray.getInt(R.styleable.FloatingActionButtonPlus_animationDuration, 150)

        typedArray.recycle()
    }

    private fun settingsView(context: Context) {
        val backView = View(context)
        backView.setBackgroundColor(mBackgroundColor)
        backView.alpha = 0f
        addView(backView)

        mSwitchFab = FloatingActionButton(context)
        mSwitchFab!!.backgroundTintList = mFabColor
        mSwitchFab!!.setImageDrawable(mIcon)
        addView(mSwitchFab)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mStatus
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val ChildCount = childCount
        for (i in 0..ChildCount - 1) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec)
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (mFirstEnter) {
            val layoutParams = layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = measuredWidth
            layoutParams.height = measuredHeight - 1
            setLayoutParams(layoutParams)

            mFirstEnter = false
        }

        Log.d("FloatingActionButtonPlu", measuredWidth.toString() + " " + measuredHeight)

        if (changed) {
            layoutSwitchFab()
            layoutBackView()

            val childCount = childCount
            var i = 0
            val j = childCount - 2
            while (i < j) {
                val childView = getChildAt(i + 2) as FabTagLayout
                mWidth = childView.layoutParams.width
                mHeight = childView.layoutParams.height
                childView.setScene(true)
                childView.visibility = View.INVISIBLE

                val childWidth = childView.measuredWidth
                val childHeight = childView.measuredHeight

                val mFabHeight: Int
                val supportMargin: Int
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    mFabHeight = mSwitchFab!!.measuredHeight + dp2px(20)
                    supportMargin = 0
                } else {
                    mFabHeight = mSwitchFab!!.measuredHeight
                    supportMargin = dp2px(8)
                }

                var fl = 0 + supportMargin
                var ft = 0

                when (mPosition) {
                    POS_LEFT_BOTTOM -> {
                        childView.setOrientation(FabTagLayout.TO_LEFT)
                        ft = measuredHeight - (mFabHeight + childHeight * (i + 1))
                    }
                    POS_LEFT_TOP -> {
                        childView.setOrientation(FabTagLayout.TO_LEFT)
                        ft = mFabHeight + childHeight * i
                    }
                    POS_RIGHT_TOP -> {
                        ft = mFabHeight + childHeight * i
                        fl = measuredWidth - childWidth - supportMargin
                    }
                    POS_RIGHT_BOTTOM -> {
                        ft = measuredHeight - (mFabHeight + childHeight * (i + 1))
                        fl = measuredWidth - childWidth - supportMargin
                    }
                }

                childView.layout(fl, ft, fl + childWidth, ft + childHeight)
                bindChildEvents(childView, i)
                prepareAnim(childView, i)
                i++
            }
        }
    }

    /**
     * 布局完了之后要准备动画，设置好每个View在第一次动画开始前的初始值

     * @param childView
     * *
     * @param i
     */
    private fun prepareAnim(childView: FabTagLayout, i: Int) {
        when (mAnimation) {
            ANIM_BOUNCE -> childView.translationY = 50f
            ANIM_SCALE -> {
                childView.scaleX = 0f
                childView.scaleY = 0f
            }
        }
    }

    private fun bindChildEvents(childView: FabTagLayout, position: Int) {
        childView.setFabOnClickListener(object : FabTagLayout.FabOnClickListener {
            override fun onClick() {
                rotateSwitchFab()
                showBackground()
                changeStatus()
                closeItems()

                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(childView, position)
                }
            }

        })

        childView.setTagOnClickListener(object : FabTagLayout.TagOnClickListener {
            override fun onClick() {
                rotateSwitchFab()
                showBackground()
                changeStatus()
                closeItems()

                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(childView, position)
                }
            }
        })
    }

    private fun layoutSwitchFab() {
        var l: Int
        var t: Int

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            l = dp2px(16)
            t = dp2px(16)
        } else {
            l = 0
            t = 0
        }

        val width = mSwitchFab!!.measuredWidth
        val height = mSwitchFab!!.measuredHeight

        when (mPosition) {
            POS_LEFT_BOTTOM -> t = measuredHeight - height - t
            POS_RIGHT_TOP -> l = measuredWidth - width - l
            POS_RIGHT_BOTTOM -> {
                l = measuredWidth - width - l
                t = measuredHeight - height - t
            }
        }

        mSwitchFab!!.layout(l, t, l + width, t + height)

        bindSwitchFabEvent()
    }

    private fun layoutBackView() {
        mBackView = getChildAt(0)
        mBackView!!.layout(0, 0, measuredWidth, measuredHeight)
    }

    private fun bindSwitchFabEvent() {
        mSwitchFab!!.setOnClickListener {
            rotateSwitchFab()
            showBackground()
            changeStatus()

            if (mStatus) {
                openItems()
                if (mOnSwitchFabClickListener != null) {
                    mOnSwitchFabClickListener!!.onClick()
                }
            } else {
                closeItems()
            }
        }
    }

    private fun openItems() {
        when (mAnimation) {
            ANIM_BOUNCE -> bounce()
            ANIM_FADE -> fade()
            ANIM_SCALE -> scale()
        }
    }


    private fun scale() {
        for (i in 2..childCount - 1) {
            val view = getChildAt(i)
            view.visibility = View.VISIBLE
            val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f)
            val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f)
            val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(scaleX, scaleY, alpha)
            animatorSet.duration = mAnimationDuration.toLong()
            animatorSet.startDelay = (i * 40).toLong()
            animatorSet.interpolator = OvershootInterpolator()
            animatorSet.start()
        }
    }

    private fun fade() {
        for (i in 2..childCount - 1) {
            val view = getChildAt(i)
            view.visibility = View.VISIBLE
            val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            alpha.duration = mAnimationDuration.toLong()
            alpha.startDelay = (i * 40).toLong()
            alpha.interpolator = OvershootInterpolator()
            alpha.start()
        }
    }

    private fun bounce() {
        for (i in 2..childCount - 1) {
            val view = getChildAt(i)
            view.visibility = View.VISIBLE
            val translationY = ObjectAnimator.ofFloat(view, "translationY", 50f, 0f)
            val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(translationY, alpha)
            animatorSet.duration = mAnimationDuration.toLong()
            animatorSet.interpolator = BounceInterpolator()
            animatorSet.start()
        }
    }

    private fun closeItems() {
        for (i in 2..childCount - 1) {
            val alpha = ObjectAnimator.ofFloat(getChildAt(i), "alpha", 1f, 0f)
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(alpha)
            animatorSet.duration = mAnimationDuration.toLong()
            animatorSet.start()

            hideChild(animatorSet, getChildAt(i))
        }
    }


    @SuppressLint("ObjectAnimatorBinding")
    private fun showBackground() {
        val backAlpha = if (mStatus)
            ObjectAnimator.ofFloat(mBackView, "alpha", 0.9f, 0f)
        else
            ObjectAnimator.ofFloat(mBackView, "alpha", 0f, 0.9f)
        backAlpha.duration = 150
        backAlpha.start()
    }

    private fun changeStatus() {
        mStatus = if (mStatus) false else true
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun rotateSwitchFab() {
        val animator = if (mStatus)
            ObjectAnimator.ofFloat(mSwitchFab, "rotation", mSwitchFabRotateVal, 0f)
        else
            ObjectAnimator.ofFloat(mSwitchFab, "rotation", 0f, mSwitchFabRotateVal)
        animator.duration = 150
        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    private fun hideChild(animatorSet: AnimatorSet, childView: View) {
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                childView.visibility = View.INVISIBLE
            }
        })
    }

    /**
     * 设置动画持续时间

     * @param duration 动画持续时间，毫秒值
     */
    fun setAnimationDuration(duration: Int) {
        mAnimationDuration = duration
    }

    /**
     * 设置主Fab在被点击的时候旋转的角度

     * @param val 主Fab旋转时的度数
     */
    fun setRotateValues(`val`: Float) {
        mSwitchFabRotateVal = `val`
    }

    /**
     * 设置主Fab的背景颜色

     * @param color 颜色 是一个ColorStateList对象
     */
    fun setSwitchFabColor(color: ColorStateList) {
        mSwitchFab!!.backgroundTintList = color
    }

    /**
     * 设置主Fab的Icon图片

     * @param icon 主Fab的Icon图片，Drawable对象
     */
    fun setContentIcon(icon: Drawable) {
        mSwitchFab!!.setImageDrawable(icon)
    }

    /**
     * 设置item展开的动画（Animation），可选值有
     * FloatingActionButtonPlus.ANIM_BOUNCE
     * FloatingActionButtonPlus.ANIM_FADE
     * FloatingActionButtonPlus.ANIM_SCALE
     * FloatingActionButtonPlus.ANIM_ZHIHU

     * @param animationMode 动画模式
     */
    fun setAnimation(animationMode: Int) {
        mAnimation = animationMode
    }

    /**
     * 设置在屏幕中的位置，可选值有
     * FloatingActionButtonPlus.POS_LEFT_TOP
     * FloatingActionButtonPlus.POS_LEFT_BOTTOM
     * FloatingActionButtonPlus.POS_RIGHT_BOTTOM
     * FloatingActionButtonPlus.POS_RIGHT_TOP

     * @param position Fab所处的位置
     */
    fun setPosition(position: Int) {
        mPosition = position
    }

    /**
     * 隐藏FloatingActionButtonPlus
     */
    @SuppressLint("ObjectAnimatorBinding")
    fun hideFab() {
        if (switchFabDisplayState) {
            val scaleX = ObjectAnimator.ofFloat(mSwitchFab, "scaleX", 1f, 0f)
            val scaleY = ObjectAnimator.ofFloat(mSwitchFab, "scaleY", 1f, 0f)
            val alpha = ObjectAnimator.ofFloat(mSwitchFab, "alpha", 1f, 0f)
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(scaleX, scaleY, alpha)
            animatorSet.duration = 300
            animatorSet.interpolator = OvershootInterpolator()
            animatorSet.start()

            hideChild(animatorSet, mSwitchFab!!)
            switchFabDisplayState = false
        }
    }

    /**
     * 显示FloatingActionButtonPlus
     */
    @SuppressLint("ObjectAnimatorBinding")
    fun showFab() {
        if (!switchFabDisplayState) {
            mSwitchFab!!.visibility = View.VISIBLE

            val scaleX = ObjectAnimator.ofFloat(mSwitchFab, "scaleX", 0f, 1f)
            val scaleY = ObjectAnimator.ofFloat(mSwitchFab, "scaleY", 0f, 1f)
            val alpha = ObjectAnimator.ofFloat(mSwitchFab, "alpha", 0f, 1f)
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(scaleX, scaleY, alpha)
            animatorSet.duration = 300
            animatorSet.interpolator = OvershootInterpolator()
            animatorSet.start()
            switchFabDisplayState = true
        }
    }

    /**
     * 将数值转换为DP

     * @param value
     * *
     * @return
     */
    private fun dp2px(value: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
    }

    companion object {
        val POS_LEFT_TOP = 0
        val POS_LEFT_BOTTOM = 1
        val POS_RIGHT_TOP = 2
        val POS_RIGHT_BOTTOM = 3

        val ANIM_FADE = 0
        val ANIM_SCALE = 1
        val ANIM_BOUNCE = 2
        val ANIM_ZHIHU = 3
    }

}
