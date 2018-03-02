package com.anod.appwatcher.upgrade

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.widget.Toast
import com.anod.appwatcher.R
import com.anod.appwatcher.SettingsActivity
import com.anod.appwatcher.backup.gdrive.GDriveSignIn
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.sync.ManualSyncService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import info.anodsplace.framework.app.ApplicationContext

/**
 * @author algavris
 * @date 02-Mar-18
 */
class UpgradeRefresh(val prefs: Preferences,val activity: Activity) : UpgradeTask {
    override fun onUpgrade(upgrade: UpgradeCheck.Result) {

        val googleAccount = GoogleSignIn.getLastSignedInAccount(activity)
        if (prefs.isDriveSyncEnabled && googleAccount == null) {
            Toast.makeText(activity, activity.getString(R.string.refresh_gdrive_mesage), Toast.LENGTH_LONG).show()
            GDriveSignIn(activity, object : GDriveSignIn.Listener {
                override fun onGDriveLoginSuccess(googleSignInAccount: GoogleSignInAccount) {
                    requestRefresh()
                }

                override fun onGDriveLoginError(errorCode: Int) {
                    val settingActivity = Intent(activity, SettingsActivity::class.java)
                    settingActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    GDriveSignIn.showResolutionNotification(
                            PendingIntent.getActivity(activity, 0, settingActivity, 0), ApplicationContext(activity))
                }
            }).signIn()
        } else {
            requestRefresh()
        }
    }

    private fun requestRefresh() {
        ManualSyncService.startActionSync(activity)
    }
}