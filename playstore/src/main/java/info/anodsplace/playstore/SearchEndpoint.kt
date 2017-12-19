package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import com.android.volley.RequestQueue

import finsky.api.DfeUtils
import finsky.api.model.DfeModel
import finsky.api.model.DfeSearch

/**
 * @author alex
 * *
 * @date 2015-02-21
 */
class SearchEndpoint(context: Context, requestQueue: RequestQueue, deviceInfoProvider: DeviceInfoProvider, account: Account, val query: String, private val autoLoadNextPage: Boolean)
        : PlayStoreEndpointBase(context, requestQueue, deviceInfoProvider, account) {

    var searchData: DfeSearch?
        get() = data as? DfeSearch
        set(value) {
            this.data = value
        }

    override fun reset() {
        searchData?.resetItems()
        super.reset()
    }

    val count: Int
        get() = searchData?.count ?: 0

    override fun executeAsync() {
        searchData?.startLoadItems()
    }

    override fun executeSync() {
        throw UnsupportedOperationException("Not implemented")
    }

    override fun createDfeModel(): DfeModel {
        return DfeSearch(dfeApi, query, autoLoadNextPage, AppDetailsFilter.predicate)
    }
}