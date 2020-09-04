// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.SingleLiveEvent
import info.anodsplace.framework.view.setOnSafeClickListener


class EmptyViewHolder(itemView: View, action: SingleLiveEvent<WishListAction>) : RecyclerView.ViewHolder(itemView), BindableViewHolder<Void?> {

    init {
        itemView.findViewById<Button>(R.id.button1).setOnSafeClickListener {
            action.value = EmptyButton(1)
        }
        itemView.findViewById<Button>(R.id.button2).setOnSafeClickListener {
            action.value = EmptyButton(2)
        }
        itemView.findViewById<Button>(R.id.button3).setOnSafeClickListener {
            action.value = EmptyButton(3)
        }
    }

    override fun bind(item: Void?) {

    }

    override fun placeholder() {
    }
}