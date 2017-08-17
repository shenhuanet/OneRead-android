package com.shenhua.oneread.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.shenhua.oneread.R
import com.shenhua.oneread.Utils
import com.shenhua.oneread.bean.Favorite
import com.shenhua.oneread.bean.User
import com.shenhua.oneread.ui.MainActivity
import kotlinx.android.synthetic.main.dialog_favorite.*


/**
 * Created by shenhua on 2017-08-04-0004.
 * Email shenhuanet@126.com
 */
class FavoriteSheet : BottomSheetDialogFragment() {

    private var rootView: View? = null
    private var favorites: ArrayList<Favorite>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater!!.inflate(R.layout.dialog_favorite, container, false)
        }
        val parent = rootView!!.parent
        if (parent != null) {
            parent as ViewGroup
            parent.removeView(rootView)
        }
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = FAdapter(context)

        if (!Utils.UserHelper.isLogin(context)) {
            Utils.toast(context, getString(R.string.please_login))
            return
        }

        tv_empty.visibility = View.VISIBLE
        tv_empty.text = getString(R.string.string_loading)
        val query = BmobQuery<Favorite>();
        query.addWhereEqualTo("user", BmobUser.getCurrentUser(User::class.java))
        query.findObjects(object : FindListener<Favorite>() {
            override fun done(result: MutableList<Favorite>?, e: BmobException?) {
                if (e == null) {
                    favorites = result as ArrayList<Favorite>
                    if (favorites != null && favorites!!.size > 0) {
                        tv_empty.visibility = View.INVISIBLE
                        recyclerView.visibility = View.VISIBLE
                        recyclerView.adapter?.notifyDataSetChanged()
                        tv_empty.text = getString(R.string.data_null)
                    }
                } else {
                    tv_empty.text = getString(R.string.data_load_error)
                    Utils.toast(context, getString(R.string.favorite_error) + e.message)
                }
            }
        })
    }

    inner class FAdapter(var context: Context) : RecyclerView.Adapter<VHolder>() {

        override fun onBindViewHolder(holder: VHolder?, position: Int) {
            val item = this@FavoriteSheet.favorites
            holder!!.title!!.text = item!![position].articleTitle
            holder.auth!!.text = item[position].articleAuth
            holder.digest!!.text = item[position].articleDigest

            holder.itemView.setOnClickListener { _ ->
                val date = item[position].articleDate
                startActivity(Intent(context, MainActivity::class.java).putExtra("date", date))
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false)
            return VHolder(view)
        }

        override fun getItemCount(): Int {
            return this@FavoriteSheet.favorites?.size ?: 0
        }
    }

    class VHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var title = itemView?.findViewById<TextView>(R.id.item_title)
        var auth = itemView?.findViewById<TextView>(R.id.item_auth)
        var digest = itemView?.findViewById<TextView>(R.id.item_digest)
    }
}

