package com.shenhua.oneread

import android.app.Application
import cn.bmob.v3.Bmob
import cn.bmob.v3.BmobConfig

/**
 * Created by shenhua on 2017-08-16-0016.
 * Email shenhuanet@126.com
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Bmob.initialize(BmobConfig.Builder(this)
                .setApplicationId(Constants.BMOB_KEY)
                .setConnectTimeout(10000)
                .build())
    }
}