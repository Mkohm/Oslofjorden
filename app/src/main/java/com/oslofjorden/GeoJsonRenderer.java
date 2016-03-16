package com.oslofjorden;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * Renders GeoJsonFeature objects onto the GoogleMap as Marker, Polyline and Polygon objects. Also
 * removes GeoJsonFeature objects and redraws features when updated.
 */
/* package */ public class GeoJsonRenderer implements Observer {

    private final static int POLYGON_OUTER_COORDINATE_INDEX = 0;

    private final static int POLYGON_INNER_COORDINATE_INDEX = 1;

    private final static Object FEATURE_NOT_ON_MAP = null;

    /**
     * Value is a Marker, Polyline, Polygon or an array of these that have been created from the
     * corresponding key
     */
    private final HashMap<GeoJsonFeature2, Object> mFeatures;

    private final GeoJsonPointStyle2 mDefaultPointStyle;

    private final GeoJsonLineStringStyle2 mDefaultLineStringStyle;

    private final GeoJsonPolygonStyle2 mDefaultPolygonStyle;

    private boolean mLayerOnMap;

    private GoogleMap mMap;





    /**
     * Creates a new GeoJsonRender object
     *
     * @param map map to place GeoJsonFeature objects on
     */
    /* package */ GeoJsonRenderer(GoogleMap map, HashMap<GeoJsonFeature2, Object> features) {
        mMap = map;
        mFeatures = features;
        mLayerOnMap = false;
        mDefaultPointStyle = new GeoJsonPointStyle2();
        mDefaultLineStringStyle = new GeoJsonLineStringStyle2();
        mDefaultPolygonStyle = new GeoJsonPolygonStyle2();

        // Add default styles to features
        for (GeoJsonFeature2 feature : getFeatures()) {
            setFeatureDefaultStyles(feature);
        }
    }

    /**
     * Given a Marker, Polyline, Polygon or an array of these and removes it from the map
     *
     * @param mapObject map object or array of map objects to remove from the map
     */
    private static void removeFromMap(Object mapObject) {
        if (mapObject instanceof Marker) {
            ((Marker) mapObject).remove();
        } else if (mapObject instanceof Polyline) {
            ((Polyline) mapObject).remove();
        } else if (mapObject instanceof Polygon) {
            ((Polygon) mapObject).remove();
        } else if (mapObject instanceof ArrayList) {
            for (Object mapObjectElement : (ArrayList) mapObject) {
                removeFromMap(mapObjectElement);
            }
        }
    }

    /* package */ boolean isLayerOnMap() {
        return mLayerOnMap;
    }

    /**
     * Gets the GoogleMap that GeoJsonFeature objects are being placed on
     *
     * @return GoogleMap
     */
    /* package */ GoogleMap getMap() {
        return mMap;
    }

    /**
     * Changes the map that GeoJsonFeature objects are being drawn onto. Existing objects are
     * removed from the previous map and drawn onto the new map.
     *
     * @param map GoogleMap to place GeoJsonFeature objects on
     */
    /* package */ void setMap(GoogleMap map) {
        for (GeoJsonFeature2 feature : getFeatures()) {
            redrawFeatureToMap(feature, map);
        }
    }

    /**
     * Adds all of the stored features in the layer onto the map if the layer is not already on the
     * map.
     */
    /* package */ void addLayerToMap() {
        if (!mLayerOnMap) {
            mLayerOnMap = true;
            for (GeoJsonFeature2 feature : getFeatures()) {
                addFeature(feature);
            }
        }
    }

    /**
     * Gets a set containing GeoJsonFeatures
     *
     * @return set containing GeoJsonFeatures
     */
    /* package */ Set<GeoJsonFeature2> getFeatures() {
        return mFeatures.keySet();
    }

    /**
     * Checks for each style in the feature and adds a default style if none is applied
     *
     * @param feature feature to apply default styles to
     */
    private void setFeatureDefaultStyles(GeoJsonFeature2 feature) {
        if (feature.getPointStyle() == null) {
            feature.setPointStyle(mDefaultPointStyle);
        }
        if (feature.getLineStringStyle() == null) {
            feature.setLineStringStyle(mDefaultLineStringStyle);
        }
        if (feature.getPolygonStyle() == null) {
            feature.setPolygonStyle(mDefaultPolygonStyle);
        }
    }

    /**
     * Adds a new GeoJsonFeature to the map if its geometry property is not null.
     *
     * @param feature feature to add to the map
     */
    /* package */ void addFeature(GeoJsonFeature2 feature) {

        Object mapObject = FEATURE_NOT_ON_MAP;
        setFeatureDefaultStyles(feature);
        if (mLayerOnMap) {
            feature.addObserver(this);

            if (mFeatures.containsKey(feature)) {
                // Remove current map objects before adding new ones
                removeFromMap(mFeatures.get(feature));
            }

            if (feature.hasGeometry()) {
                // Create new map object
                mapObject = addFeatureToMap(feature, feature.getGeometry());
            }
        }
        mFeatures.put(feature, mapObject);
    }

    /**
     * Removes all GeoJsonFeature objects stored in the mFeatures hashmap from the map
     */
    /* package */ void removeLayerFromMap() {
        if (mLayerOnMap) {
            for (GeoJsonFeature2 feature : mFeatures.keySet()) {
                removeFromMap(mFeatures.get(feature));

                feature.deleteObserver(this);
            }
            mLayerOnMap = false;
        }
    }

    /**
     * Removes a GeoJsonFeature from the map if its geometry property is not null
     *
     * @param feature feature to remove from map
     */
    /* package */ void removeFeature(GeoJsonFeature2 feature) {
        // Check if given feature is stored
        if (mFeatures.containsKey(feature)) {
            removeFromMap(mFeatures.remove(feature));
            feature.deleteObserver(this);
        }
    }

    /**
     * Gets the default style used to render GeoJsonPoints
     *
     * @return default style used to render GeoJsonPoints
     */
    /* package */ GeoJsonPointStyle2 getDefaultPointStyle() {
        return mDefaultPointStyle;
    }

    /**
     * Gets the default style used to render GeoJsonLineStrings
     *
     * @return default style used to render GeoJsonLineStrings
     */
    /* package */ GeoJsonLineStringStyle2 getDefaultLineStringStyle() {
        return mDefaultLineStringStyle;
    }

    /**
     * Gets the default style used to render GeoJsonPolygons
     *
     * @return default style used to render GeoJsonPolygons
     */
    /* package */ GeoJsonPolygonStyle2 getDefaultPolygonStyle() {
        return mDefaultPolygonStyle;
    }

    /**
     * Adds a new object onto the map using the GeoJsonGeometry for the coordinates and the
     * GeoJsonFeature for the styles.
     *
     * @param feature  feature to get geometry style
     * @param geometry geometry to add to the map
     */
    private Object addFeatureToMap(GeoJsonFeature2 feature, GeoJsonGeometry2 geometry) {
        String geometryType = geometry.getType();
        if (geometryType.equals("Point")) {
            return addPointToMap(feature.getPointStyle(), (GeoJsonPoint2) geometry);
        } else if (geometryType.equals("LineString")) {
            return addLineStringToMap(feature.getLineStringStyle(),
                    (GeoJsonLineString2) geometry);
        } else if (geometryType.equals("Polygon")) {
            return addPolygonToMap(feature.getPolygonStyle(),
                    (GeoJsonPolygon2) geometry);
        } else if (geometryType.equals("MultiPoint")) {
            return addMultiPointToMap(feature.getPointStyle(),
                    (GeoJsonMultiPoint2) geometry);
        } else if (geometryType.equals("MultiLineString")) {
            return addMultiLineStringToMap(feature.getLineStringStyle(),
                    ((GeoJsonMultiLineString2) geometry));
        } else if (geometryType.equals("MultiPolygon")) {
            return addMultiPolygonToMap(feature.getPolygonStyle(),
                    ((GeoJsonMultiPolygon2) geometry));
        } else if (geometryType.equals("GeometryCollection")) {
            return addGeometryCollectionToMap(feature,
                    ((GeoJsonGeometryCollection2) geometry).getGeometries());
        }
        return null;
    }

    /**
     * Adds a GeoJsonPoint to the map as a Marker
     *
     * @param pointStyle contains relevant styling properties for the Marker
     * @param point      contains coordinates for the Marker
     * @return Marker object created from the given GeoJsonPoint
     */
    private Marker addPointToMap(GeoJsonPointStyle2 pointStyle, GeoJsonPoint2 point) {
        MarkerOptions markerOptions = pointStyle.toMarkerOptions();
        markerOptions.position(point.getCoordinates());

        MapsActivity.markersReadyToAdd.add(markerOptions);
        //return mMap.addMarker(markerOptions);
        return null;
    }

    /**
     * Adds all GeoJsonPoint objects in GeoJsonMultiPoint to the map as multiple Markers
     *
     * @param pointStyle contains relevant styling properties for the Markers
     * @param multiPoint contains an array of GeoJsonPoints
     * @return array of Markers that have been added to the map
     */
    private ArrayList<Marker> addMultiPointToMap(GeoJsonPointStyle2 pointStyle,
                                                 GeoJsonMultiPoint2 multiPoint) {
        ArrayList<Marker> markers = new ArrayList<Marker>();
        for (GeoJsonPoint2 geoJsonPoint : multiPoint.getPoints()) {
            markers.add(addPointToMap(pointStyle, geoJsonPoint));
        }
        return markers;
    }

    /**
     * Adds a GeoJsonLineString to the map as a Polyline
     *
     * @param lineStringStyle contains relevant styling properties for the Polyline
     * @param lineString      contains coordinates for the Polyline
     * @return Polyline object created from given GeoJsonLineString
     */
    private Polyline addLineStringToMap(GeoJsonLineStringStyle2 lineStringStyle,
                                        GeoJsonLineString2 lineString) {
        PolylineOptions polylineOptions = lineStringStyle.toPolylineOptions();
        // Add coordinates


        //Putter description og name inn i et array som så puttes inn i hashmappet
        String[] arrayElements = new String[2];

        String description = MapsActivity.descriptionList.get(MapsActivity.indexInDescriptionList);
        String name = MapsActivity.nameList.get(MapsActivity.indexInNameList);

        arrayElements[0] = description;
        arrayElements[1] = name;


        MapsActivity.kyststiInfoMap.put(lineString.getCoordinates(), arrayElements);
        MapsActivity.indexInDescriptionList++;
        MapsActivity.indexInNameList++;


        //This is one polyline, adds all the coordinates
        polylineOptions.addAll(lineString.getCoordinates());


        //Adds the correct coloring according to the description
        setKyststiColor(polylineOptions, description);


        //Adds the customized polyline to the list
        MapsActivity.polylinesReadyToAdd.add(polylineOptions);
        //return mMap.addPolyline(polylineOptions);
        return null;
    }

    private void setKyststiColor(PolylineOptions polylineOptions, String description) {
        if (description != null) {
            if (isSykkelvei(description)) {
                polylineOptions.color(Color.GREEN);
            } else if (isFerge(description)) {
                polylineOptions.color(Color.parseColor("#980009"));
            } else if (isVanskeligKyststi(description)) {
                polylineOptions.color(Color.RED);
            } else {
                polylineOptions.color(Color.BLUE);
            }
        }
    }

    private boolean isSykkelvei(String description) {
        return description.contains("Sykkelvei") || description.contains("sykkelvei");
    }

    private boolean isFerge(String description) {
        return (description.contains("Ferge") || description.contains("ferge")) && !description.contains("fergeleie");
    }

    private boolean isVanskeligKyststi(String description) {
        return description.contains("Vanskelig") || description.contains("vanskelig");
    }

    /**
     * Adds all GeoJsonLineString objects in the GeoJsonMultiLineString to the map as multiple
     * Polylines
     *
     * @param lineStringStyle contains relevant styling properties for the Polylines
     * @param multiLineString contains an array of GeoJsonLineStrings
     * @return array of Polylines that have been added to the map
     */
    private ArrayList<Polyline> addMultiLineStringToMap(GeoJsonLineStringStyle2 lineStringStyle,
                                                        GeoJsonMultiLineString2 multiLineString) {
        ArrayList<Polyline> polylines = new ArrayList<Polyline>();
        for (GeoJsonLineString2 geoJsonLineString : multiLineString.getLineStrings()) {
            polylines.add(addLineStringToMap(lineStringStyle, geoJsonLineString));
        }
        return polylines;
    }

    /**
     * Adds a GeoJsonPolygon to the map as a Polygon
     *
     * @param polygonStyle contains relevant styling properties for the Polygon
     * @param polygon      contains coordinates for the Polygon
     * @return Polygon object created from given GeoJsonPolygon
     */
    private Polygon addPolygonToMap(GeoJsonPolygonStyle2 polygonStyle, GeoJsonPolygon2 polygon) {
        PolygonOptions polygonOptions = polygonStyle.toPolygonOptions();
        // First array of coordinates are the outline
        polygonOptions.addAll(polygon.getCoordinates().get(POLYGON_OUTER_COORDINATE_INDEX));
        // Following arrays are holes
        for (int i = POLYGON_INNER_COORDINATE_INDEX; i < polygon.getCoordinates().size();
             i++) {
            polygonOptions.addHole(polygon.getCoordinates().get(i));
        }
        return mMap.addPolygon(polygonOptions);
    }

    /**
     * Adds all GeoJsonPolygon in the GeoJsonMultiPolygon to the map as multiple Polygons
     *
     * @param polygonStyle contains relevant styling properties for the Polygons
     * @param multiPolygon contains an array of GeoJsonPolygons
     * @return array of Polygons that have been added to the map
     */
    private ArrayList<Polygon> addMultiPolygonToMap(GeoJsonPolygonStyle2 polygonStyle,
                                                    GeoJsonMultiPolygon2 multiPolygon) {
        ArrayList<Polygon> polygons = new ArrayList<Polygon>();
        for (GeoJsonPolygon2 geoJsonPolygon : multiPolygon.getPolygons()) {
            polygons.add(addPolygonToMap(polygonStyle, geoJsonPolygon));
        }
        return polygons;
    }

    /**
     * Adds all GeoJsonGeometry objects stored in the GeoJsonGeometryCollection onto the map.
     * Supports recursive GeometryCollections.
     *
     * @param feature           contains relevant styling properties for the GeoJsonGeometry inside
     *                          the GeoJsonGeometryCollection
     * @param geoJsonGeometries contains an array of GeoJsonGeometry objects
     * @return array of Marker, Polyline, Polygons that have been added to the map
     */
    private ArrayList<Object> addGeometryCollectionToMap(GeoJsonFeature2 feature,
                                                         List<GeoJsonGeometry2> geoJsonGeometries) {
        ArrayList<Object> geometries = new ArrayList<Object>();
        for (GeoJsonGeometry2 geometry : geoJsonGeometries) {
            geometries.add(addFeatureToMap(feature, geometry));
        }
        return geometries;
    }

    /**
     * Redraws a given GeoJsonFeature onto the map. The map object is obtained from the mFeatures
     * hashmap and it is removed and added.
     *
     * @param feature feature to redraw onto the map
     */
    private void redrawFeatureToMap(GeoJsonFeature2 feature) {
        redrawFeatureToMap(feature, mMap);
    }

    private void redrawFeatureToMap(GeoJsonFeature2 feature, GoogleMap map) {
        removeFromMap(mFeatures.get(feature));
        mFeatures.put(feature, FEATURE_NOT_ON_MAP);
        mMap = map;
        if (map != null && feature.hasGeometry()) {
            mFeatures.put(feature, addFeatureToMap(feature, feature.getGeometry()));
        }
    }

    /**
     * Update is called if the developer sets a style or geometry in a GeoJsonFeature object
     *
     * @param observable GeoJsonFeature object
     * @param data       null, no extra argument is passed through the notifyObservers method
     */
    public void update(Observable observable, Object data) {
        if (observable instanceof GeoJsonFeature2) {
            GeoJsonFeature2 feature = ((GeoJsonFeature2) observable);
            boolean featureIsOnMap = mFeatures.get(feature) != FEATURE_NOT_ON_MAP;
            if (featureIsOnMap && feature.hasGeometry()) {
                // Checks if the feature has been added to the map and its geometry is not null
                // TODO: change this so that we don't add and remove
                redrawFeatureToMap(feature);
            } else if (featureIsOnMap && !feature.hasGeometry()) {
                // Checks if feature is on map and geometry is null
                removeFromMap(mFeatures.get(feature));
                mFeatures.put(feature, FEATURE_NOT_ON_MAP);
            } else if (!featureIsOnMap && feature.hasGeometry()) {
                // Checks if the feature isn't on the map and geometry is not null
                addFeature(feature);
            }
        }
    }
}
