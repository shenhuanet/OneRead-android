package com.shenhua.oneread.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import cn.bmob.v3.BmobUser
import com.bumptech.glide.Glide
import com.shenhua.oneread.R
import com.shenhua.oneread.Utils
import com.shenhua.oneread.widget.FavoriteSheet
import kotlinx.android.synthetic.main.activity_setting.*
import kotlin.concurrent.thread


/**
 * Created by shenhua on 2017-08-02-0002.
 * Email shenhuanet@126.com
 */
class SettingActivity : AppCompatActivity(), View.OnClickListener {

    private var dialog: BottomSheetDialog? = null
    private var imageUrl: String? = null

    override fun onClick(p0: View?) {
        if (dialog == null) {
            dialog = BottomSheetDialog(this)
        }
        when (p0?.id) {
            R.id.userItem -> {

            }
            R.id.favoriteItem -> {
                FavoriteSheet().show(supportFragmentManager, "favorite")
            }
            R.id.broweItem -> {
                dialog!!.setContentView(R.layout.dialog_set_browe)
                dialog!!.show()
                dialog!!.window.findViewById<TextView>(R.id.item_color_black).setOnClickListener {
                    view ->
                    Utils.Config.setWebTextColor(this, "#000000")
                    dismissDialog()
                }
                dialog!!.window.findViewById<TextView>(R.id.item_color_gray).setOnClickListener {
                    view ->
                    Utils.Config.setWebTextColor(this, "#2C2C2C")
                    dismissDialog()
                }
                dialog!!.window.findViewById<TextView>(R.id.item_color_accent).setOnClickListener {
                    view ->
                    Utils.Config.setWebTextColor(this, "#9FE1A4")
                    dismissDialog()
                }
                dialog!!.window.findViewById<TextView>(R.id.item_color_blue).setOnClickListener {
                    view ->
                    Utils.Config.setWebTextColor(this, "#1296DB")
                    dismissDialog()
                }
                dialog!!.window.findViewById<TextView>(R.id.item_color_pink).setOnClickListener {
                    view ->
                    Utils.Config.setWebTextColor(this, "#DB639B")
                    dismissDialog()
                }
                dialog!!.window.findViewById<TextView>(R.id.item_color_yellow).setOnClickListener {
                    view ->
                    Utils.Config.setWebTextColor(this, "#D2CA26")
                    dismissDialog()
                }
            }
            R.id.picItem -> {
                dialog!!.setContentView(R.layout.dialog_set_pic)
                dialog!!.show()
                dialog!!.window.findViewById<TextView>(R.id.download_launcher).setOnClickListener {
                    view ->
                    imageUrl = Utils.Config.getLauncherImage(this)
                    checkPermissions()
                    dismissDialog()
                }
                dialog!!.window.findViewById<TextView>(R.id.download_background).setOnClickListener {
                    view ->
                    imageUrl = Utils.Config.getBgImage(this)
                    checkPermissions()
                    dismissDialog()
                }
            }
            R.id.cleanItem -> {
                Utils.FileUtil.clearAllCache(this)
                tv_set_cache.text = Utils.FileUtil.getTotalCacheSize(this)
            }
            R.id.aboutItem -> {
                dialog!!.setContentView(R.layout.dialog_set_about)
                dialog!!.show()
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Array(1, { android.Manifest.permission.WRITE_EXTERNAL_STORAGE }),
                    100)
        } else {
            savePic()
        }
    }

    private fun savePic() {
        thread {
            try {
                val bitmap = Glide.with(this).load(imageUrl!!).asBitmap()
                        .into(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL,
                                com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                        .get()
                if (bitmap != null) {
                    Utils.FileUtil.saveBitmapToSDCard(this, bitmap,
                            Utils.FileUtil.getFileName(imageUrl!!)!!,
                            "oneRead", true)
                    runOnUiThread { Utils.toast(this, getString(R.string.save_pic_success)) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread { Utils.toast(this, getString(R.string.save_pic_error)) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 21) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.statusBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.activity_setting)

        var bitmap = BitmapFactory.decodeResource(resources, R.drawable.bg_3)
        bitmap = Utils.blurBitmap(bitmap, 25.0f, this);
        image_blur.setImageBitmap(bitmap)
        setListener(userItem, favoriteItem, broweItem, picItem, cleanItem, aboutItem)
        rl_user.setOnClickListener { _ ->
            if (Utils.UserHelper.isLogin(this)) {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle(getString(R.string.string_message))
                dialog.setMessage(getString(R.string.string_had_login))
                dialog.setNegativeButton(getString(R.string.string_cancel), null)
                dialog.setPositiveButton(getString(R.string.string_sure)) { p0, p1 ->
                    BmobUser.logOut()
                    Utils.UserHelper.logout(this@SettingActivity)
                    onResume()
                }
                dialog.show()
            } else {
                startActivity(Intent().setClass(this, LoginActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        tv_set_cache.text = Utils.FileUtil.getTotalCacheSize(this)
        if (Utils.UserHelper.isLogin(this)) {
            iv_status.setImageResource(R.drawable.ic_online)
        } else {
            iv_status.setImageResource(R.drawable.ic_offline)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                savePic()
            } else {
                Utils.toast(this, getString(R.string.please_permission))
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun dismissDialog() {
        if (dialog!!.isShowing)
            dialog!!.dismiss()
    }

    fun setListener(vararg views: View) {
        for (view in views) {
            view.setOnClickListener(this)
        }
    }
}