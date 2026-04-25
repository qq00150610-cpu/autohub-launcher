/**
 * 应用网格适配器
 * 用于在网格中显示应用图标
 */
package com.autocar.launcher.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autocar.launcher.R
import com.autocar.launcher.data.model.AppInfo

class AppGridAdapter(
    private val context: Context,
    private val appList: List<AppInfo>,
    private val onAppClick: (AppInfo, Int) -> Unit
) : RecyclerView.Adapter<AppGridAdapter.ViewHolder>() {

    private var onItemLongClickListener: ((AppInfo, Int) -> Boolean)? = null

    fun setOnItemLongClickListener(listener: (AppInfo, Int) -> Boolean) {
        onItemLongClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_app_icon, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = appList[position]
        holder.ivIcon?.setImageDrawable(app.icon)
        holder.tvName?.text = app.appName
        holder.tvName?.visibility = View.VISIBLE

        holder.itemView.setOnClickListener {
            onAppClick(app, position)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClickListener?.invoke(app, position) ?: false
        }
    }

    override fun getItemCount(): Int = appList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivIcon: ImageView? = itemView.findViewById(R.id.ivAppIcon)
        var tvName: TextView? = itemView.findViewById(R.id.tvAppName)
    }
}
