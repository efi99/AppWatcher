package finsky.api.model

import finsky.api.DfeUtils
import finsky.protos.nano.Messages

abstract class ContainerList<T> constructor(
        url: String,
        autoLoadNextPage: Boolean,
        private val filter: FilterPredicate?
    ) : PaginatedList<T, Document>(url, autoLoadNextPage) {


    override fun getItemsFromResponse(wrapper: Messages.Response.ResponseWrapper): Array<Document> {
        val payload = payload(wrapper)
        val doc = DfeUtils.getRootDoc(payload) ?: return arrayOf()

        val docs = doc.child?.map { Document(it) } ?: listOf()
        if (filter == null) {
            return docs.toTypedArray()
        }

        val list = docs.filter(filter)
        return list.toTypedArray()
    }

    override fun getNextPageUrl(wrapper: Messages.Response.ResponseWrapper): String? {
        val payload = payload(wrapper)
        val doc = DfeUtils.getRootDoc(payload) ?: return null

        val containerMetadata = doc.containerMetadata
        var nextPageUrl: String? = null
        if (containerMetadata != null) {
            nextPageUrl = doc.containerMetadata.nextPageUrl
        }
        return nextPageUrl
    }

    private fun payload(wrapper: Messages.Response.ResponseWrapper): Messages.Response.Payload {
        val payload = wrapper.payload
        return if (wrapper.preFetch.isNotEmpty() && (payload.searchResponse != null && payload.searchResponse.doc.isEmpty() || payload.listResponse != null && payload.listResponse.doc.isEmpty())) {
            wrapper.preFetch[0].response.payload
        } else payload
    }

}