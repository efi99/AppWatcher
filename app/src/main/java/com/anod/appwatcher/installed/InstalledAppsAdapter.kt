package com.anod.appwatcher.installed

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.utils.SingleLiveEvent
import com.anod.appwatcher.watchlist.AppViewHolderBase
import com.anod.appwatcher.watchlist.AppViewHolderResourceProvider
import com.anod.appwatcher.watchlist.WishListAction
import info.anodsplace.framework.content.InstalledPackage

/**
 * @author alex
 * *
 * @date 2015-08-30
 */
open class InstalledAppsAdapter(
        private val itemViewType: Int,
        protected val context: Context,
        private val packageManager: PackageManager,
        private val dataProvider: AppViewHolderResourceProvider,
        protected val action: SingleLiveEvent<WishListAction>)
    : RecyclerView.Adapter<AppViewHolderBase<AppListItem>>() {

    var installedPackages: List<InstalledPackage> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    internal val iconLoader: PicassoAppIcon = Application.provide(context).iconLoader

    override fun getItemCount(): Int {
        return this.installedPackages.size
    }

    override fun getItemViewType(position: Int) = itemViewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolderBase<AppListItem> {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_app, parent, false)
        return InstalledAppViewHolder(v, dataProvider, iconLoader, action)
    }

    override fun onBindViewHolder(holder: AppViewHolderBase<AppListItem>, position: Int) {
        val installedPackage = installedPackages[position]
        val app = packageManager.packageToApp(-1, installedPackage.packageName)
        holder.bind(AppListItem(app, "", noNewDetails = false, recentFlag = false))
    }

}
