package com.shenhua.oneread.ui

import android.app.ProgressDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.LogInListener
import cn.bmob.v3.listener.UpdateListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.shenhua.oneread.R
import com.shenhua.oneread.Utils
import com.shenhua.oneread.bean.User
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.view_login.*
import kotlin.concurrent.thread

/**
 * Created by shenhua on 2017-08-11-0011.
 * Email shenhuanet@126.com
 */
class LoginActivity : AppCompatActivity() {

    private var fragmentLists = ArrayList<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 21) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.statusBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.activity_login)
        try {
            var bitmap = BitmapFactory.decodeResource(resources, R.drawable.bg_3)
            bitmap = Utils.blurBitmap(bitmap, 25.0f, this);
            image_blur.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Glide.with(this).load(R.drawable.bg_3).apply(RequestOptions.bitmapTransform(CenterCrop())).into(image_blur);
        }
        tablayout.setupWithViewPager(viewpager)
        fragmentLists.add(Login())
        fragmentLists.add(Register())
        fragmentLists.add(Forgot())
        viewpager.adapter = PagerAdapter(this, supportFragmentManager, fragmentLists)
    }

    /**
     * 忘记密码
     */
    class Forgot : Fragment() {
        var rootView: View? = null
        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            if (rootView == null)
                rootView = inflater!!.inflate(R.layout.view_login, container, false)
            val parent = rootView!!.parent
            if (parent != null) {
                (parent as ViewGroup).removeView(rootView)
            }
            return rootView
        }

        override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            et_password.visibility = View.INVISIBLE
            btn_login.text = getString(R.string.string_sure)
            btn_login.setOnClickListener { _ ->
                val email = et_email_phone.text.toString().trim()
                if (!Utils.LoginUtil.checkEmail(email)) {
                    Utils.toast(context, getString(R.string.mail_error))
                } else {
                    val dialog = ProgressDialog(context)
                    dialog.setMessage(getString(R.string.operationing))
                    dialog.show()
                    BmobUser.resetPasswordByEmail(email, object : UpdateListener() {
                        override fun done(e: BmobException?) {
                            dialog.dismiss()
                            if (e == null) {
                                Utils.toast(context, String.format(getString(R.string.reset_success), email))
                            } else {
                                Utils.toast(context, getString(R.string.operation_error) + Utils.BmobUtil().parseErrorCode(e.errorCode))
                            }
                        }
                    })
                }
            }
        }
    }

    /**
     * 注册
     */
    class Register : Fragment() {
        var rootView: View? = null

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            if (rootView == null)
                rootView = inflater!!.inflate(R.layout.view_login, container, false)
            val parent = rootView!!.parent
            if (parent != null) {
                (parent as ViewGroup).removeView(rootView)
            }
            return rootView
        }

        override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            btn_login.text = getString(R.string.string_sign)
            btn_login.setOnClickListener { _ -> register() }
        }

        private fun register() {
            Utils.hideKeyboard(activity)
            val email = et_email_phone.text.toString().trim()
            val password = et_password.text.toString().trim()

            if (!Utils.LoginUtil.checkEmail(email)) {
                Utils.toast(context, getString(R.string.mail_error))
            } else if (!Utils.LoginUtil.checkPassword(password)) {
                Utils.toast(context, getString(R.string.password_error))
            } else {
                val dialog = ProgressDialog(context)
                dialog.setMessage(getString(R.string.operationing))
                dialog.show()
                thread {
                    val pass = Utils.md5(password)
                    activity.runOnUiThread {
                        Utils.BmobUtil().register(email, pass, object : Utils.BmobUtil.OnRegisterListener {
                            override fun onStart() {
                            }

                            override fun onSuccess() {
                                Utils.toast(context, getString(R.string.string_sign_success))
                                Utils.UserHelper.setUser(context, email, pass)
                                activity.finish()
                            }

                            override fun onError(msg: String) {
                                Utils.toast(context, getString(R.string.string_sign_error) + msg)
                            }

                            override fun onEnd() {
                                dialog.dismiss()
                            }
                        })
                    }
                }
            }
        }
    }

    /**
     * 登录
     */
    class Login : Fragment() {
        var rootView: View? = null

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            if (rootView == null)
                rootView = inflater!!.inflate(R.layout.view_login, container, false)
            val parent = rootView!!.parent
            if (parent != null) {
                (parent as ViewGroup).removeView(rootView)
            }
            return rootView
        }

        override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            btn_login.text = getString(R.string.string_login)
            btn_login.setOnClickListener { _ -> login(); }
        }

        private fun login() {
            Utils.hideKeyboard(activity)
            val email = et_email_phone.text.toString().trim()
            val password = et_password.text.toString().trim()

            if (!Utils.LoginUtil.checkEmail(email)) {
                Utils.toast(context, getString(R.string.mail_error))
            } else if (!Utils.LoginUtil.checkPassword(password)) {
                Utils.toast(context, getString(R.string.password_error))
            } else {
                val dialog = ProgressDialog(context)
                dialog.setMessage(getString(R.string.operationing))
                dialog.show()
                thread {
                    val pass = Utils.md5(password)
                    activity.runOnUiThread {
                        BmobUser.loginByAccount(email, pass, object : LogInListener<User>() {
                            override fun done(p0: User?, p1: BmobException?) {
                                dialog.dismiss()
                                if (p1 == null) {
                                    Utils.toast(context, getString(R.string.string_login_success))
                                    Utils.UserHelper.setUser(context, email, pass)
                                    activity.finish()
                                } else {
                                    Utils.toast(context, Utils.BmobUtil().parseErrorCode(p1.errorCode))
                                }
                            }
                        })
                    }
                }
            }
        }
    }

    /**
     * viewpager滑动适配器
     */
    inner class PagerAdapter(val context: Context, fm: FragmentManager?, val lists: List<Fragment>?) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return lists!![position]
        }

        override fun getCount(): Int {
            return lists!!.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            when (position) {
                0 -> return context.getString(R.string.string_login)
                1 -> return context.getString(R.string.string_sign)
                2 -> return context.getString(R.string.string_forget)
            }
            return super.getPageTitle(position)
        }
    }
}