package com.oslofjorden.oslofjordenturguide.MapView;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

public class MyMarkerOptions implements ClusterItem {
        private String title;
        private String description;
        private LatLng position;
        private BitmapDescriptor icon;


        public MyMarkerOptions(MarkerOptions myMarkerOptions) {
            this.title = myMarkerOptions.getTitle();
            this.description = myMarkerOptions.getSnippet();
            this.position = myMarkerOptions.getPosition();
            this.icon = myMarkerOptions.getIcon();

        }

        @Override
        public String getSnippet() {
            return null;
        }

        @Override
        public LatLng getPosition() {
            return position;
        }

        public String getDescription() {
            return description;
        }

        public String getTitle() {
            return title;
        }


        public BitmapDescriptor getIcon() {
            return icon;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setIcon(BitmapDescriptor icon) {
            this.icon = icon;
        }

        public void setPosition(LatLng position) {
            this.position = position;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }