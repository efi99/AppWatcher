package com.anod.appwatcher.details

import android.accounts.Account
import android.os.AsyncTask
import androidx.lifecycle.*
import com.android.volley.VolleyError
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.Application
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.utils.map
import com.anod.appwatcher.utils.switchMap
import finsky.api.model.DfeDetails
import finsky.api.model.DfeModel
import finsky.api.model.Document
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.livedata.OneTimeObserver
import info.anodsplace.framework.os.BackgroundTask
import info.anodsplace.playstore.DetailsEndpoint
import info.anodsplace.playstore.PlayStoreEndpoint

typealias TagMenuItem = Pair<Tag,Boolean>

sealed class ChangelogLoadState
object LocalComplete : ChangelogLoadState()
class RemoteComplete(val error: Boolean): ChangelogLoadState()
object Complete : ChangelogLoadState()

class DetailsViewModel(application: android.app.Application) : AndroidViewModel(application), PlayStoreEndpoint.Listener {

    val context: ApplicationContext
        get() = ApplicationContext(getApplication())

    val provide: AppComponent
        get() = Application.provide(context)

    var detailsUrl = ""
    var appId = MutableLiveData<String>("")
    var rowId: Int = -1
    var isNewApp: Boolean = false

    val app: MutableLiveData<App> = MutableLiveData()

    val account: Account? by lazy {
        Application.provide(application).prefs.account
    }
    private val detailsEndpoint: DetailsEndpoint by lazy {
        val account = this.account ?: Account("empty", "empty")
        DetailsEndpoint(application, provide.requestQueue, provide.deviceInfo, account, detailsUrl)
    }
    var authToken = ""
    var localChangelog: List<AppChange> = emptyList()
    val tagsMenuItems: LiveData<List<TagMenuItem>> = appId.switchMap { appId ->
        if (appId.isEmpty()) {
            return@switchMap MutableLiveData(emptyList<TagMenuItem>())
        }
        return@switchMap provide.database.tags().observe().switchMap { tags ->
            return@switchMap provide.database.appTags().forApp(appId).map { appTags ->
                val appTagsList = appTags.map { it.tagId }
                tags.map { TagMenuItem(it, appTagsList.contains(it.id)) }
            }
        }
    }

    val changelogState = MutableLiveData<ChangelogLoadState>()

    val document: Document?
        get() = detailsEndpoint.document

    var recentChange = AppChange(appId.value!!, 0, "", "", "")

    override fun onCleared() {
        detailsEndpoint.listener = null
    }

    fun loadApp() {
        if (appId.value!!.isEmpty()) {
            app.value = null
            return
        }

        if (rowId == -1) {
            val localApp = context.packageManager.packageToApp(-1, appId.value!!)
            isNewApp = true
            AppLog.i("Show details for unwatched $appId")
            app.value = localApp
        } else {
            isNewApp = false
            AppLog.i("Show details for watched $appId")
            BackgroundTask(object : BackgroundTask.Worker<Int, App?>(rowId) {
                override fun run(param: Int): App? {
                    val appsTable = Application.provide(context).database.apps()
                    return appsTable.loadAppRow(rowId)
                }

                override fun finished(result: App?) {
                    app.value = result
                }

            }).execute()
        }
    }

    fun loadLocalChangelog() {
        if (appId.value!!.isBlank()) {
            this.updateChangelogState(LocalComplete)
            return
        }
        val changes = Application.provide(context).database.changelog().ofApp(appId.value!!)
        changes.observeForever(OneTimeObserver(changes, Observer {
            this.localChangelog = it ?: emptyList()
            this.updateChangelogState(LocalComplete)
        }))
    }

    fun loadRemoteChangelog() {
        if (this.authToken.isEmpty()) {
            this.updateChangelogState(RemoteComplete(true))
        } else {
            detailsEndpoint.authToken = this.authToken
            detailsEndpoint.listener = this
            detailsEndpoint.startAsync()
        }
    }

    override fun onDataChanged(data: DfeModel) {
        val details = data as DfeDetails
        val appDetails = details.document?.appDetails
        if (appDetails != null) {
            recentChange = AppChange(appId.value!!, appDetails.versionCode, appDetails.versionString, appDetails.recentChangesHtml
                    ?: "", appDetails.uploadDate)
        }
        this.updateChangelogState(RemoteComplete(false))
    }

    private fun updateChangelogState(state: ChangelogLoadState) {
        when (state) {
            is LocalComplete -> {
                if (this.changelogState.value is RemoteComplete || this.changelogState.value is Complete) {
                    this.changelogState.value = Complete
                } else {
                    this.changelogState.value = state
                }
            }
            is RemoteComplete -> {
                if (this.changelogState.value is LocalComplete || this.changelogState.value is Complete) {
                    this.changelogState.value = Complete
                } else {
                    this.changelogState.value = state
                }
            }
        }
    }

    override fun onErrorResponse(error: VolleyError) {
        AppLog.e("Cannot fetch details for $appId - $error")
        this.updateChangelogState(RemoteComplete(true))
    }

    fun changeTag(tagId: Int, checked: Boolean)  {
        BackgroundTask(object : BackgroundTask.Worker<Boolean, Boolean>(checked) {
            override fun run(param: Boolean): Boolean {
                if (checked) {
                    return provide.database.appTags().delete(tagId, appId.value!!) > 0
                }

                return AppTagsTable.Queries.insert(tagId, appId.value!!, provide.database) != -1L
            }

            override fun finished(result: Boolean) {

            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }
}