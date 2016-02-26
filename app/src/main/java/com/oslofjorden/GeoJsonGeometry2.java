package com.oslofjorden;

/**
 * Represents a GeoJSON geometry object. Note that only the first two elements in each position are
 * considered. Altitude and any further values are not considered.
 */
public interface GeoJsonGeometry2 {

    /**
     * Gets the type of geometry. The type of geometry conforms to the GeoJSON 'type'
     * specification.
     *
     * @return type of geometry
     */
    public String getType();
}
