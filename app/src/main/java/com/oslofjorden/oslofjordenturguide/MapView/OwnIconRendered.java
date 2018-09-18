package com.oslofjorden.oslofjordenturguide.MapView;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.oslofjorden.oslofjordenturguide.MapView.model.Marker;

class OwnIconRendered extends DefaultClusterRenderer<Marker> {

    public OwnIconRendered(Context context, GoogleMap map,
                           ClusterManager<Marker> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(Marker item, MarkerOptions markerOptions) {
        markerOptions.icon(item.getIcon());

        super.onBeforeClusterItemRendered(item, markerOptions);
    }

}