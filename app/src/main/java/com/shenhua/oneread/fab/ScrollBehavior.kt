package com.shenhua.oneread.fab

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View

/**
 * used !! don't delete
 *
 * Created by shenhua on 2017-08-01-0001.
 * Email shenhuanet@126.com
 */
class ScrollBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<View>(context, attrs) {

    var mActionButtonPlus: FloatingActionButtonPlus? = null

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, directTargetChild: View, target: View, nestedScrollAxes: Int): Boolean {
        return nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, dx: Int, dy: Int, consumed: IntArray) {
        if (mActionButtonPlus == null) {
            mActionButtonPlus = child as FloatingActionButtonPlus
        }

        if (dy > 10) {
            mActionButtonPlus!!.hideFab()
        } else if (dy < -10) {
            mActionButtonPlus!!.showFab()
        }
    }

    override fun layoutDependsOn(parent: CoordinatorLayout?, child: View?, dependency: View?): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View?, dependency: View?): Boolean {
        val translationY = Math.min(0f, dependency!!.translationY - dependency.height)
        child!!.translationY = translationY
        return true
    }
}