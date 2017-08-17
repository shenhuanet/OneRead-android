package com.shenhua.oneread

import android.content.Context

/**
 * Created by shenhua on 2017-08-01-0001.
 * Email shenhuanet@126.com
 */
class Constants {

    object HtmlString {
        val HTML_TITLE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
                "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "<style>body{padding-top:40px} p{color:#000000}</style>" +
                "</head>\n" +
                "<body>\n" +
                "<h3><p align=\"center\">"
        val HTML_TITLE_AUTH = "</p></h3>\n<p align=\"center\">"
        val HTML_TITLE_END = "</p>\n<div style=\"text-indent:2em;\">\n"
        val HTML_END = "\n</div></body>\n" + "</html>"
    }

    companion object {

        val BMOB_KEY = "9d5f498a08bb012422f220e3750d5346"

        init {
            System.loadLibrary("core")
        }
    }

    external fun url(context: Context): String

    external fun fixEnd(): String

    external fun rep(string: String): String

    external fun imgU(string: String): String

    external fun imgU1(string: String): String
}
