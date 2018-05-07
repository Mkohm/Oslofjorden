package com.oslofjorden.oslofjordenturguide.MapView;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

class OwnIconRendered extends DefaultClusterRenderer<MarkerData> {

    public OwnIconRendered(Context context, GoogleMap map, ClusterManager<MarkerData> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(MarkerData item, MarkerOptions markerOptions) {
        markerOptions.icon(item.getIcon());

        super.onBeforeClusterItemRendered(item, markerOptions);
    }

}