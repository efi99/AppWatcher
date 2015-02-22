package com.google.android.finsky.api.model;

import com.android.volley.Response;
import com.google.android.finsky.api.DfeApi;
import com.google.android.finsky.protos.Details;
import com.google.android.finsky.protos.DocumentV2;

public class DfeDetails extends DfeModel implements Response.Listener<Details.DetailsResponse>
{
    private Details.DetailsResponse mDetailsResponse;
    private final String mDetailsUrl;
    private DfeApi mDfeApi;

    public DfeDetails(final DfeApi dfeApi, final String url) {
        super();
        mDfeApi = dfeApi;
        mDetailsUrl = url;
    }
    
    public void start() {
        mDfeApi.getDetails(mDetailsUrl, false, false, this, this);
    }

    public Details.DiscoveryBadge[] getDiscoveryBadges() {
        if (this.mDetailsResponse == null) {
            return null;
        }
        return this.mDetailsResponse.discoveryBadge;
    }
    
    public Document getDocument() {
        if (this.mDetailsResponse == null || this.mDetailsResponse.docV2 == null) {
            return null;
        }
        return new Document(this.mDetailsResponse.docV2);
    }
    
    public String getFooterHtml() {
        if (this.mDetailsResponse == null || this.mDetailsResponse.footerHtml.length() == 0) {
            return null;
        }
        return this.mDetailsResponse.footerHtml;
    }
    
    public byte[] getServerLogsCookie() {
        if (this.mDetailsResponse == null || this.mDetailsResponse.serverLogsCookie.length == 0) {
            return null;
        }
        return this.mDetailsResponse.serverLogsCookie;
    }
    
    public DocumentV2.Review getUserReview() {
        if (this.mDetailsResponse == null || this.mDetailsResponse.userReview == null) {
            return null;
        }
        return this.mDetailsResponse.userReview;
    }
    
    @Override
    public boolean isReady() {
        return this.mDetailsResponse != null;
    }
    
    public void onResponse(final Details.DetailsResponse mDetailsResponse) {
        this.mDetailsResponse = mDetailsResponse;
        this.notifyDataSetChanged();
    }
}
