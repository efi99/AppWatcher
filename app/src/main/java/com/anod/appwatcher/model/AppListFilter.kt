package com.anod.appwatcher.model

import com.anod.appwatcher.database.entities.AppListItem
import info.anodsplace.framework.content.InstalledApps

/**
 * @author alex
 * *
 * @date 8/4/14.
 */

interface CountableFilter {
    val newCount: Int
    val updatableNewCount: Int
    val recentlyUpdatedCount: Int
}

interface AppListFilter: CountableFilter {

    fun filterRecord(item: AppListItem): Boolean

    class None : AppListFilter {
        override fun filterRecord(item: AppListItem): Boolean {
            return false
        }

        override val newCount: Int = 0
        override val updatableNewCount: Int = 0
        override val recentlyUpdatedCount: Int = 0
    }
}

class AppListFilterInclusion(private val inclusion: Inclusion, private val installedApps: InstalledApps) : AppListFilter {

    interface Inclusion {
        fun include(versionCode: Int, installedInfo: InstalledApps.Info): Boolean
    }

    class All : Inclusion {
        override fun include(versionCode: Int, installedInfo: InstalledApps.Info): Boolean {
            return true
        }
    }

    class Installed : Inclusion {
        override fun include(versionCode: Int, installedInfo: InstalledApps.Info): Boolean {
            return installedInfo.isInstalled
        }
    }

    class Uninstalled : Inclusion {
        override fun include(versionCode: Int, installedInfo: InstalledApps.Info): Boolean {
            return !installedInfo.isInstalled
        }
    }

    class Updatable : Inclusion {
        override fun include(versionCode: Int, installedInfo: InstalledApps.Info): Boolean {
            return installedInfo.isInstalled && installedInfo.isUpdatable(versionCode)
        }
    }

    override var newCount: Int = 0
        private set
    override var updatableNewCount: Int = 0
        private set
    override var recentlyUpdatedCount: Int = 0
        private set

    override fun filterRecord(item: AppListItem): Boolean {
        val packageName = item.app.packageName
        val status = item.app.status
        val versionCode = item.app.versionNumber

        val installedInfo = installedApps.packageInfo(packageName)

        if (!inclusion.include(versionCode, installedInfo)) {
            return true
        }

        if (status == AppInfoMetadata.STATUS_UPDATED) {
            newCount++
            if (installedInfo.isUpdatable(versionCode)) {
                updatableNewCount++
            }
        } else if (status == AppInfoMetadata.STATUS_NORMAL) {
            if (item.recentFlag) {
                recentlyUpdatedCount++
            }
        }
        return false
    }
}