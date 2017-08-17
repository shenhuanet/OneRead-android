package com.shenhua.oneread.bean

import cn.bmob.v3.BmobObject

/**
 * Created by shenhua on 2017-08-10-0010.
 * Email shenhuanet@126.com
 */
class Favorite : BmobObject() {

    var user: User? = null
    var articleDate: String? = null
    var articleTitle: String? = null
    var articleAuth: String? = null
    var articleDigest: String? = null

    override fun equals(other: Any?): Boolean {
        val obj = other as Favorite
        return obj.articleTitle!! == articleTitle &&
                obj.articleAuth!! == articleAuth &&
                obj.articleDate!! == articleDate
    }
}
