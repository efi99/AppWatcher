package com.anod.appwatcher.model

import android.content.Context
import android.database.Cursor
import android.support.v4.content.CursorLoader
import android.text.TextUtils
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.model.schema.AppTagsTable
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.database.FilterCursor
import info.anodsplace.framework.database.NullCursor
import java.util.*

/**
 * @author alex
 * *
 * @date 8/11/13
 */
open class AppListCursorLoader(context: Context,
                               protected val titleFilter: String,
                               sortOrder: String,
                               private val cursorFilter: AppListFilter,
                               tag: Tag?)
    : CursorLoader(context, DbContentProvider.appsContentUri(tag), AppListTable.projection, null, null, sortOrder) {

    constructor(context: Context, titleFilter: String, sortId: Int, cursorFilter: AppListFilter, tag: Tag?)
            : this(context, titleFilter, createSortOrder(sortId), cursorFilter, tag)

    init {
        val selc = ArrayList<String>(3)
        val args = ArrayList<String>(3)

        selc.add(AppListTable.Columns.status + " != ?")
        args.add(AppInfoMetadata.STATUS_DELETED.toString())

        if (tag != null) {
            selc.add(AppTagsTable.TableColumns.tagId + " = ?")
            args.add(tag.id.toString())
            selc.add(AppTagsTable.TableColumns.appId + " = " + AppListTable.TableColumns.appId)
        }

        if (!TextUtils.isEmpty(titleFilter)) {
            selc.add(AppListTable.Columns.title + " LIKE ?")
            args.add("%$titleFilter%")
        }

        val selection = TextUtils.join(" AND ", selc)
        val selectionArgs = args.toTypedArray()

        setSelection(selection)
        setSelectionArgs(selectionArgs)
    }

    override fun loadInBackground(): Cursor {
        val cr = super.loadInBackground() ?: NullCursor()

        cursorFilter.resetNewCount()
        return AppListCursor(FilterCursor(cr, cursorFilter))
    }

    val newCount: Int
        get() = cursorFilter.newCount

    val updatableCount: Int
        get() = cursorFilter.updatableNewCount

    val recentlyUpdatedCount: Int
        get() = cursorFilter.recentlyUpdatedCount

    companion object {
        private fun createSortOrder(sortId: Int): String {
            val filter = ArrayList<String>()
            filter.add(AppListTable.Columns.status + " DESC")
            filter.add(AppListTable.Columns.recentFlag + " DESC")
            when (sortId) {
                Preferences.SORT_NAME_DESC -> filter.add(AppListTable.Columns.title + " COLLATE NOCASE DESC")
                Preferences.SORT_DATE_ASC -> filter.add(AppListTable.Columns.uploadTimestamp + " ASC")
                Preferences.SORT_DATE_DESC -> filter.add(AppListTable.Columns.uploadTimestamp + " DESC")
                else -> filter.add(AppListTable.Columns.title + " COLLATE NOCASE ASC")
            }
            return TextUtils.join(", ", filter)
        }
    }
}
