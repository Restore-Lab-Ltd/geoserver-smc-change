package co.nz.restorelab;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.*;

import java.util.HashMap;
import java.util.Map;

public class GridCalculator {
    private final double cellSize;
    private final GeometryFactory geometryFactory;
    private final Envelope gridEnvelope;

    public GridCalculator(double cellSize) {
        this.cellSize = cellSize;
        this.gridEnvelope = new Envelope(165.0, 180.0, -48.0, -33.0);
        this.geometryFactory = JTSFactoryFinder.getGeometryFactory();
    }

    public Map<GridCell, Double> aggregate(SimpleFeatureCollection features) {
        double minX = gridEnvelope.getMinX();
        double maxX = gridEnvelope.getMaxX();
        double minY = gridEnvelope.getMinY();
        double maxY = gridEnvelope.getMaxY();


        Map<GridCell, Double> counts = new HashMap<>();

        try (SimpleFeatureIterator featureIterator = features.features()) {
            while (featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();
                Geometry geom = (Geometry) feature.getDefaultGeometry();
                if (geom == null) continue;

                Envelope geomEnv = geom.getEnvelopeInternal();
                int colStart = (int) Math.floor((geomEnv.getMinX() - minX) / cellSize);
                int colEnd = (int) Math.floor((geomEnv.getMaxX() - minX) / cellSize);
                int rowStart = (int) Math.floor((geomEnv.getMinY() - minY) / cellSize);
                int rowEnd = (int) Math.floor((geomEnv.getMaxY() - minY) / cellSize);

                for (int col = colStart; col <= colEnd; col++) {
                    for (int row = rowStart; row <= rowEnd; row++) {
                        double cellMinX = minX + col * cellSize;
                        double cellMinY = minY + row * cellSize;
                        Polygon cellPolygon = createCell(cellMinX, cellMinY, cellSize);

                        if (geom.intersects(cellPolygon)) {
                            GridCell cell = new GridCell(col, row, cellPolygon);
                            counts.put(cell, counts.getOrDefault(cell, 0.0) + 1.0);
                        }
                    }
                }
            }
        }
        return counts;
    }

    public SimpleFeatureType getResultFeatureType() {
        SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName("gridcell");
        featureTypeBuilder.add("geometry", Polygon.class);
        featureTypeBuilder.add("value", Double.class);
        return featureTypeBuilder.buildFeatureType();
    }

    private Polygon createCell(double minX, double minY, double size) {
        Coordinate[] coords = new Coordinate[]{
                new Coordinate(minX, minY),
                new Coordinate(minX + size, minY),
                new Coordinate(minX + size, minY + size),
                new Coordinate(minX, minY + size),
                new Coordinate(minX, minY),
        };
        LinearRing ring = geometryFactory.createLinearRing(coords);
        return geometryFactory.createPolygon(ring);
    }
}
