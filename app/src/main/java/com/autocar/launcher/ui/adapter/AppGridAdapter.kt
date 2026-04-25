/**
 * 应用网格适配器
 * 用于在网格中显示应用图标
 */
package com.autocar.launcher.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.autocar.launcher.R
import com.autocar.launcher.data.model.AppInfo

class AppGridAdapter(
    private val context: Context,
    private val appList: List<AppInfo>,
    private val onAppClick: (AppInfo, Int) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = appList.size

    override fun getItem(position: Int): AppInfo = appList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_app_icon, parent, false)
            holder = ViewHolder()
            holder.ivIcon = view.findViewById(R.id.ivAppIcon)
            holder.tvName = view.findViewById(R.id.tvAppName)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val app = appList[position]
        holder.ivIcon?.setImageDrawable(app.icon)
        holder.tvName?.text = app.appName
        holder.tvName?.visibility = View.VISIBLE

        view.setOnClickListener {
            onAppClick(app, position)
        }

        return view
    }

    private class ViewHolder {
        var ivIcon: ImageView? = null
        var tvName: TextView? = null
    }
}
