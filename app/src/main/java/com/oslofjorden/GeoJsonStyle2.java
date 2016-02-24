package com.oslofjorden;

interface GeoJsonStyle2 {

    /**
     * Gets the type of geometries this style can be applied to
     *
     * @return type of geometries this style can be applied to
     */
    public String[] getGeometryType();

    public boolean isVisible();

    public void setVisible(boolean visible);

}
