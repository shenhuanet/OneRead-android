package com.shenhua.oneread.ui

import android.animation.ObjectAnimator
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.shenhua.oneread.Constants
import com.shenhua.oneread.R
import com.shenhua.oneread.Utils
import com.shenhua.oneread.bean.Article
import com.shenhua.oneread.bean.Favorite
import com.shenhua.oneread.bean.User
import com.shenhua.oneread.fab.FabTagLayout
import com.shenhua.oneread.fab.FloatingActionButtonPlus
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_menu.*
import org.jsoup.Jsoup
import java.io.File
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private var article: Article? = null
    private var imageUrl: String = ""
    private var isLoaded: Boolean = false
    private val flag = "article"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 21) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.statusBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.activity_main)
        webview.setBackgroundColor(Color.parseColor("#00000000"))
        webview.clearCache(false)
        webview.settings.defaultTextEncodingName = "UTF-8"
        fabs.setOnItemClickListener(object : FloatingActionButtonPlus.OnItemClickListener {
            override fun onItemClick(tagView: FabTagLayout, position: Int) {
                when (position) {
                    0 -> {
                        startActivity(Intent().setClass(this@MainActivity, SettingActivity::class.java))
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    }
                    1 -> {
                        if (Utils.UserHelper.isLogin(this@MainActivity)) favorite()
                        else {
                            Utils.toast(this@MainActivity, getString(R.string.please_login))
                            startActivity(Intent().setClass(this@MainActivity, LoginActivity::class.java))
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        }
                    }
                    2 -> start(1, "")
                }
            }
        })
        val date = intent.getStringExtra("date")
        if (date == null) start(0, "") else {
            fabs.visibility = View.GONE
            start(2, date)
        }
    }

    private fun start(int: Int, date: String) {
        isLoaded = false
        var str = StringBuilder().append(Constants().url(this)).append(Constants().fixEnd()).toString();
        str = Constants().rep(str)
        val url = when (int) {
            0 -> StringBuilder().append(str).append("/").append(flag).append(File.separator)
                    .append(today()).toString()
            1 -> StringBuilder().append(str).append("/").append(flag).append(File.separator)
                    .append(random()).toString()
            2 -> {
                StringBuilder().append(str).append("/").append(flag).append(File.separator)
                        .append("day?dev=1").append("&date=" + date).toString()
            }
            else -> "null"
        }
        imageUrl = getRandomImage()
        Glide.with(this).load(imageUrl).crossFade(2000).into(image_bg)
        image_bg.postDelayed({
            ObjectAnimator.ofFloat(image_load, "alpha", 0.0f, 1.0f).setDuration(1000).start()
            image_load.visibility = View.VISIBLE
            try {
                startGetArticle(url)
            } catch (e: Exception) {
                Utils.toast(this, getString(R.string.data_error))
            }
        }, 1800)
    }

    private fun favorite() {
        if (!isLoaded) {
            return
        }
        val favorite = Favorite()
        val user = BmobUser.getCurrentUser(User::class.java)
        favorite.user = user
        favorite.articleTitle = article!!.getData().title
        favorite.articleAuth = article!!.getData().author
        favorite.articleDate = article!!.getData().date!!.curr
        favorite.articleDigest = article!!.getData().digest
        val dialog = ProgressDialog(this)
        dialog.setMessage(getString(R.string.operationing))
        dialog.show()
        // 查询该用户是否已收藏
        val query = BmobQuery<Favorite>();
        query.addWhereEqualTo("user", user)
        query.findObjects(object : FindListener<Favorite>() {
            override fun done(favorites: MutableList<Favorite>?, e: BmobException?) {
                if (e == null) {
                    favorites!!.forEach {
                        if (favorite == it) {// 已收藏
                            dialog.dismiss()
                            Utils.toast(this@MainActivity, "该文章已收藏")
                            return
                        }
                    }
                    favorite.save(object : SaveListener<String>() {
                        override fun done(p0: String?, p1: BmobException?) {
                            dialog.dismiss()
                            if (p1 == null) {
                                Utils.toast(this@MainActivity, "收藏成功")
                            } else {
                                Utils.toast(this@MainActivity, "收藏失败")
                            }
                        }
                    })
                } else {
                    dialog.dismiss()
                    Utils.toast(this@MainActivity, getString(R.string.string_error_again))
                }
            }
        })
    }

    private fun startGetArticle(url: String) {
        var result: String;
        thread {
            try {
                val res = Jsoup.connect(url)
                        .header("Content-Type", "application/json;charset=utf-8")
                        .timeout(10000).ignoreContentType(true).execute()
                result = res.body()
            } catch (e: Exception) {
                result = "null"
            }
            article = Gson().fromJson<Article>(result, Article::class.java)
            var content = article!!.getData().content
            val color = Utils.Config.getWebTextColor(this)
            var html = Constants.HtmlString.HTML_TITLE
            html = html.replace(html.substring(html.indexOf("#"), html.indexOf("#") + 7), color)
            content = html + article!!.getData().title +
                    Constants.HtmlString.HTML_TITLE_AUTH + article!!.getData().author +
                    Constants.HtmlString.HTML_TITLE_END + content +
                    Constants.HtmlString.HTML_END
            runOnUiThread {
                webview.loadDataWithBaseURL(null, content,
                        "text/html;charset=UTF-8", null, null)
                webview.scrollTo(0, 0)
                isLoaded = true
                dataLoaded()
            }
        }
    }

    private fun dataLoaded() {
        ObjectAnimator.ofFloat(layout_content, "alpha", 0.0f, 1.0f).setDuration(1500).start()
        layout_content.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(image_load, "alpha", 1.0f, 0.0f).setDuration(1200).start()
    }

    private fun getRandomImage(): String {
        val img = Constants().imgU(Constants().imgU1("yi"))
        Utils.Config.setBgImage(this, img)
        return img
    }

    external fun today(): String

    external fun random(): String

    companion object {
        init {
            System.loadLibrary("core")
        }
    }
}
