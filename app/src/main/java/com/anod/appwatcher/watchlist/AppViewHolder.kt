package com.anod.appwatcher.watchlist

import android.view.View
import android.widget.ImageView
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.details.AppDetailsView
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.utils.SingleLiveEvent
import info.anodsplace.framework.view.setOnSafeClickListener

open class AppViewHolder(
        itemView: View,
        resourceProvider: ResourceProvider,
        iconLoader: PicassoAppIcon,
        private val action: SingleLiveEvent<WishListAction>)
    : AppViewHolderBase<AppListItem>(itemView, resourceProvider, iconLoader) {

    private var item: AppListItem? = null
    private val icon: ImageView = itemView.findViewById(R.id.icon)
    private val detailsView: AppDetailsView = AppDetailsView(itemView, resourceProvider)

    open val isLocalApp: Boolean
        get() = false

    init {
        itemView.findViewById<View>(R.id.content).setOnSafeClickListener {
            action.value = ItemClick(this.item!!.app)
        }
    }

    override fun recycle() {
        itemView.findViewById<View>(R.id.content).setOnClickListener(null)
    }

    override fun bind(item: AppListItem) {
        this.item = item

        this.detailsView.fillDetails(item.app, item.recentFlag, item.changeDetails
                ?: "", item.noNewDetails, isLocalApp)
        iconLoader.loadAppIntoImageView(item.app, this.icon, R.drawable.ic_app_icon_placeholder)
    }

    override fun placeholder() {
        // TODO
    }

}