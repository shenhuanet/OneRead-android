package com.shenhua.oneread.bean

/**
 * Created by shenhua on 2017-08-01-0001.
 * Email shenhuanet@126.com
 */
class Article {

    private var data: DataBean? = null

    fun getData(): DataBean {
        return data!!
    }

    fun setData(data: DataBean) {
        this.data = data
    }

    class DataBean {

        var date: DateBean? = null
        var author: String? = null
        var title: String? = null
        var digest: String? = null
        var content: String? = null
        var wc: Int = 0

        class DateBean {

            var curr: String? = null
            var prev: String? = null
            var next: String? = null
        }
    }

}
