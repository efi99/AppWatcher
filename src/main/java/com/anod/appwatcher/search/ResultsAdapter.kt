package com.anod.appwatcher.search

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.anod.appwatcher.R
import com.anod.appwatcher.model.WatchAppList
import finsky.api.model.Document
import finsky.protos.nano.Messages
import com.squareup.picasso.Picasso
import info.anodsplace.appwatcher.framework.InstalledApps
import info.anodsplace.appwatcher.framework.ThemeCompat

/**
 *  @author alex
 *  @date 6/3/2017
 */

abstract class ResultsAdapter(
        private val context: Context,
        private val watchAppList: WatchAppList): RecyclerView.Adapter<ResultsAppViewHolder>() {

    private val colorBgDisabled = ThemeCompat.getColor(context, R.attr.inactiveRow)
    private val colorBgNormal = ThemeCompat.getColor(context, R.attr.colorItemBackground)
    private val installedAppsProvider = InstalledApps.MemoryCache(InstalledApps.PackageManager(context.packageManager))

    val isEmpty: Boolean
        get() = this.itemCount == 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsAppViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_market_app, parent, false)
        return ResultsAppViewHolder(view, watchAppList)
    }

    abstract fun getDocument(position: Int): Document

    override fun onBindViewHolder(holder: ResultsAppViewHolder, position: Int) {
        val doc = getDocument(position)

        val app = doc.appDetails
        val uploadDate = app.uploadDate
        val packageName = app.packageName

        holder.doc = doc
        holder.title.text = doc.title
        holder.details.text = doc.creator
        holder.updated.text = uploadDate

        if (watchAppList.contains(packageName)) {
            holder.row.setBackgroundColor(colorBgDisabled)
        } else {
            holder.row.setBackgroundColor(colorBgNormal)
        }

        val imageUrl = doc.iconUrl

        Picasso.with().load(imageUrl)
                .placeholder(R.drawable.ic_notifications_black_24dp)
                .into(holder.icon)

        val isInstalled = installedAppsProvider.getInfo(packageName).isInstalled
        if (isInstalled) {
            holder.price.setText(R.string.installed)
        } else {
            val offer = doc.getOffer(Messages.Common.Offer.TYPE_1)
            if (offer == null) {
                holder.price.text = ""
            } else if (offer.micros.toInt() == 0) {
                holder.price.setText(R.string.free)
            } else {
                holder.price.text = offer.formattedAmount
            }
        }
    }
}
