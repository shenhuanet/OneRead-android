package com.shenhua.oneread

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.LogInListener
import cn.bmob.v3.listener.SaveListener
import com.shenhua.oneread.bean.User
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.security.MessageDigest
import java.util.regex.Pattern

/**
 * Created by shenhua on 2017-08-02-0002.
 * Email shenhuanet@126.com
 */
object Utils {

    object UserHelper {

        fun setUser(context: Context, username: String, password: String) {
            context.getSharedPreferences("user", Context.MODE_PRIVATE).edit()
                    .putString("username", username)
                    .putString("password", password)
                    .apply()
        }

        fun logout(context: Context) {
            context.getSharedPreferences("user", Context.MODE_PRIVATE).edit().clear().apply()
        }

        fun isLogin(context: Context): Boolean {
            val result = context.getSharedPreferences("user", Context.MODE_PRIVATE)
                    .getString("username", "")
            return !TextUtils.isEmpty(result)
        }
    }

    object Config {
        fun setWebTextColor(context: Context, color: String) {
            set(context, "web_color", color)
        }

        fun getWebTextColor(context: Context): String {
            return context.getSharedPreferences("config", Context.MODE_PRIVATE)
                    .getString("web_color", "#000000")
        }

        fun setLauncherImage(context: Context, url: String) {
            set(context, "launcher_url", url)
        }

        fun getLauncherImage(context: Context): String {
            return getUrl(context, "launcher_url")
        }

        fun setBgImage(context: Context, url: String) {
            set(context, "bg_url", url)
        }

        fun getBgImage(context: Context): String {
            return getUrl(context, "bg_url")
        }

        private fun set(context: Context, key: String, value: String) {
            context.getSharedPreferences("config", Context.MODE_PRIVATE).edit()
                    .putString(key, value)
                    .apply()
        }

        private fun getUrl(context: Context, key: String): String {
            return context.getSharedPreferences("config", Context.MODE_PRIVATE)
                    .getString(key, "")
        }
    }

    object FileUtil {
        /**
         * 获取所有缓存大小

         * @param context 上下文
         * *
         * @return string
         * *
         * @throws Exception
         */
        @Throws(Exception::class)
        fun getTotalCacheSize(context: Context): String {
            var cacheSize = getFolderSize(context.cacheDir)
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                cacheSize += getFolderSize(context.externalCacheDir)
            }
            return getFormatSize(cacheSize.toDouble())
        }

        /**
         * 清理所有缓存

         * @param context 上下文
         */
        fun clearAllCache(context: Context) {
            deleteDir(context.cacheDir)
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                deleteDir(context.externalCacheDir)
            }
        }

        private fun deleteDir(dir: File?): Boolean {
            if (dir != null && dir.isDirectory()) {
                val children = dir.list()
                for (aChildren in children) {
                    val success = deleteDir(File(dir, aChildren))
                    if (!success) {
                        return false
                    }
                }
            }
            assert(dir != null)
            return dir!!.delete()
        }

        /**
         * 获取文件

         * @param file 文件  Context.getExternalFilesDir() --> SDCard/Android/data/应用包名/files/目录，一般放一些长时间保存的数据
         * *             Context.getExternalCacheDir() --> SDCard/Android/data/应用包名/cache/目录，一般存放临时缓存数据
         * *
         * @return string
         * *
         * @throws Exception
         */
        @Throws(Exception::class)
        private fun getFolderSize(file: File): Long {
            var size: Long = 0
            try {
                val fileList = file.listFiles()
                for (aFileList in fileList) {
                    // 如果下面还有文件
                    if (aFileList.isDirectory()) {
                        size = size + getFolderSize(aFileList)
                    } else {
                        size = size + aFileList.length()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return size
        }

        /**
         * 格式化单位

         * @param size 大小
         * *
         * @return string
         */
        private fun getFormatSize(size: Double): String {
            val kiloByte = size / 1024
            if (kiloByte < 1) {
                return "0KB"
            }
            val megaByte = kiloByte / 1024
            if (megaByte < 1) {
                val result1 = BigDecimal(java.lang.Double.toString(kiloByte))
                return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB"
            }
            val gigaByte = megaByte / 1024
            if (gigaByte < 1) {
                val result2 = BigDecimal(java.lang.Double.toString(megaByte))
                return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB"
            }
            val teraBytes = gigaByte / 1024
            if (teraBytes < 1) {
                val result3 = BigDecimal(java.lang.Double.toString(gigaByte))
                return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB"
            }
            val result4 = BigDecimal(teraBytes)
            return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB"
        }

        @Throws(Exception::class)
        fun saveBitmapToSDCard(context: Context, bitmap: Bitmap?, title: String, dirName: String, shouldRefreshGallery: Boolean): String {
            val dir = File(Environment.getExternalStorageDirectory(), dirName)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, title + ".jpg")
            if (!file.exists()) file.createNewFile()
            val fileOutputStream = FileOutputStream(file)
            if (bitmap == null) throw Exception("bitmap is null")
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            if (shouldRefreshGallery)
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + dirName + file.absolutePath)))
            return file.absolutePath
        }

        fun getFileName(pathAndName: String): String? {
            val start = pathAndName.lastIndexOf("/")
            val end = pathAndName.lastIndexOf(".")
            if (start != -1 && end != -1) {
                return pathAndName.substring(start + 1, end)
            } else {
                return null
            }
        }
    }

    object LoginUtil {

        fun checkEmail(email: String): Boolean {
            val regex = "^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w{2,3}){1,3})$"
            return Pattern.matches(regex, email)
        }

        fun checkPassword(password: String): Boolean {
            if (password.length < 6 || password.length > 32 || TextUtils.isEmpty(password)) {
                return false
            }
            return true
        }
    }

    class BmobUtil {
        fun register(email: String, password: String, listener: OnRegisterListener?) {
            listener?.onStart()
            val bmobuser = BmobUser()
            bmobuser.username = email
            bmobuser.setPassword(password)
            bmobuser.email = email
            bmobuser.signUp(object : SaveListener<User>() {
                override fun done(user: User?, e: BmobException?) {
                    if (e == null) {
                        BmobUser.loginByAccount(email, password, object : LogInListener<User>() {
                            override fun done(p0: User?, p1: BmobException?) {
                                listener?.onEnd()
                                if (p1 == null) {
                                    listener?.onSuccess()
                                } else {
                                    listener?.onError(parseErrorCode(p1.errorCode))
                                }
                            }
                        })
                    } else {
                        listener?.onEnd()
                        listener?.onError(parseErrorCode(e.errorCode))
                    }
                }
            })
        }

        interface OnRegisterListener {
            fun onStart()
            fun onSuccess()
            fun onError(msg: String)
            fun onEnd()
        }

        fun parseErrorCode(code: Int): String {
            return when (code) {
                9010 -> "网络超时"
                9016 -> "无网络连接，请检查您的手机网络."
                108 -> "用户名或密码错误"
                202 -> "用户名已经存在"
                203 -> "该邮箱已经注册"
                205 -> "用户不存在"
                209 -> "该手机号码已经存在"
                210 -> "旧密码不正确"
                else -> {
                    "操作失败"
                }
            }
        }
    }

    /**
     * 高斯模糊
     */
    fun blurBitmap(bitmap: Bitmap, radius: Float, context: Context): Bitmap {
        val rs = RenderScript.create(context)
        val allocation = Allocation.createFromBitmap(rs, bitmap)
        val t = allocation.type
        val blurredAllocation = Allocation.createTyped(rs, t)
        val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        blurScript.setRadius(radius)
        blurScript.setInput(allocation)
        blurScript.forEach(blurredAllocation)
        blurredAllocation.copyTo(bitmap)
        allocation.destroy()
        blurredAllocation.destroy()
        blurScript.destroy()
        t.destroy()
        rs.destroy()
        return bitmap
    }

    /**
     * MD5加密类
     */
    fun md5(s: String): String {
        val instance: MessageDigest = MessageDigest.getInstance("MD5")
        val digest: ByteArray = instance.digest(s.toByteArray())
        val sb: StringBuffer = StringBuffer()
        for (b in digest) {
            val i: Int = b.toInt() and 0xff
            var hexString = Integer.toHexString(i)
            if (hexString.length < 2) {
                hexString = "0" + hexString
            }
            sb.append(hexString)
        }
        return sb.toString()
    }

    fun toast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * 隐藏软键盘
     * @param context context
     */
    fun hideKeyboard(context: Activity) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive && context.currentFocus != null) {
            if (context.currentFocus.windowToken != null) {
                imm.hideSoftInputFromWindow(context.currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }
}
