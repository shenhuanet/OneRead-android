package com.shenhua.oneread.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bumptech.glide.Glide
import com.shenhua.oneread.R
import com.shenhua.oneread.Utils
import kotlinx.android.synthetic.main.activity_splash.*
import org.jsoup.Jsoup
import kotlin.concurrent.thread

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        image_wall.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        thread {
            var url: String
            try {
                val res = Jsoup.connect("http://guolin.tech/api/bing_pic")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .timeout(10000).ignoreContentType(true).execute()
                url = res.body()
            } catch (e: Exception) {
                url = "http://cn.bing.com/az/hprichbg/rb/HydricHammock_ZH-CN7896164965_1920x1080.jpg"
            }
            Utils.Config.setLauncherImage(this, url)
            runOnUiThread {
                Glide.with(this).load(url).into(image_wall)
                image_logo.postDelayed({ startAnim() }, 2000)
            }
        }
    }

    private fun goHome() {
        startActivity(Intent().setClass(this, MainActivity::class.java))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun startAnim() {
        image_logo.visibility = View.VISIBLE
        val obj = ObjectAnimator.ofFloat(image_logo, "alpha", 0f, 1.0f)
        obj.duration = 1000
        obj.start()
        obj.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                tv_title.visibility = View.VISIBLE
                ObjectAnimator.ofFloat(tv_title, "translationY", -30f, 0f).start()
                image_logo.postDelayed({ goHome() }, 1000)
            }
        })
    }

}
