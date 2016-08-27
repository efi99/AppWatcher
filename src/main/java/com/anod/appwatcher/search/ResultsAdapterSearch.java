package com.anod.appwatcher.search;

import android.content.Context;

import com.anod.appwatcher.market.SearchEndpoint;
import com.anod.appwatcher.model.AddWatchAppHandler;
import com.google.android.finsky.api.model.Document;

/**
 * @author algavris
 * @date 26/08/2016.
 */

public class ResultsAdapterSearch extends ResultsAdapter {
    private final SearchEndpoint mSearchEngine;

    public ResultsAdapterSearch(Context context, SearchEndpoint searchEngine, AddWatchAppHandler newAppHandler) {
        super(context, newAppHandler);
        mSearchEngine = searchEngine;
    }

    @Override
    Document getDocument(int position) {
        return mSearchEngine.getData().getItem(position, false);
    }

    @Override
    public int getItemCount() {
        return mSearchEngine.getCount();
    }

}
