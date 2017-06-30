package com.anod.appwatcher.backup

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.anod.appwatcher.R
import com.anod.appwatcher.backup.gdrive.ApiClientAsyncTask
import com.anod.appwatcher.backup.gdrive.SyncConnectedWorker
import com.anod.appwatcher.backup.gdrive.SyncTask
import com.anod.appwatcher.backup.gdrive.UploadConnectedWorker
import com.anod.appwatcher.utils.GooglePlayServices
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive

/**
 * @author alex
 * *
 * @date 1/19/14
 */
class GDriveSync(private val context: Context): SyncTask.Listener, GooglePlayServices.Listener {

    var listener: Listener? = null
    val playServices = GooglePlayServices(context, this)

    val isSupported: Boolean
        get() = playServices.isSupported

    val playServiceStatusText: String
        get() = playServices.errorCodeText


    fun showResolutionNotification(resolution: PendingIntent) {
        val builder = NotificationCompat.Builder(context)
        builder
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_stat_update)
                .setContentTitle(context.getString(R.string.google_drive_sync_failed))
                .setContentText(context.getString(R.string.user_action_required))
                .setContentIntent(resolution)

        val notification = builder.build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    interface Listener {
        fun onGDriveConnect()
        fun onGDriveSyncProgress()
        fun onGDriveSyncStart()
        fun onGDriveSyncFinish()
        fun onGDriveError()
    }

    fun connect() {
        playServices.connect()
    }

    fun sync() {
        listener?.onGDriveSyncStart()
        if (!playServices.isConnected) {
            playServices.connectWithAction(ACTION_SYNC)
        } else {
            SyncTask(context, this, createGoogleApiClientBuilder().build()).execute()
        }
    }

    @Throws(Exception::class)
    fun syncLocked() {
        if (!playServices.isConnected) {
            playServices.connectLocked()
        }
        val worker = SyncConnectedWorker(context, playServices.googleApiClient)
        worker.doSyncInBackground()
    }

    @Throws(Exception::class)
    fun uploadLocked() {
        if (!playServices.isConnected) {
            playServices.connectLocked()
        }
        val worker = UploadConnectedWorker(context, playServices.googleApiClient)
        worker.doUploadInBackground()
    }

    override fun createGoogleApiClientBuilder(): GoogleApiClient.Builder {
        return GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
    }

    override fun onConnectAction(action: Int) {
        listener?.onGDriveConnect()
        if (action == ACTION_SYNC) {
            SyncTask(context, this, createGoogleApiClientBuilder().build()).execute()
        }
    }

    override fun onConnectionError() {
        listener?.onGDriveError()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        playServices.onActivityResult(requestCode, resultCode, data)
    }


    override fun onResult(result: ApiClientAsyncTask.Result) {
        if (result.status) {
            listener?.onGDriveSyncFinish()
        } else {
            Toast.makeText(context, result.ex?.message ?: "Error", Toast.LENGTH_SHORT).show()
            listener?.onGDriveError()
        }
    }

    companion object {
        private val ACTION_SYNC = 2
        private val NOTIFICATION_ID = 2
    }
}