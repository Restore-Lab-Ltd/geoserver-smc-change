package co.nz.restorelab.smc;


import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.wps.gs.GeoServerProcess;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@DescribeProcess(title = "CalculateChange", description = "Calculates the change in soil moisture content.")
public class CalculateChangeProcess implements GeoServerProcess {

    private final Catalog catalog;

    public CalculateChangeProcess(Catalog catalog) {
        this.catalog = catalog;
    }

    @DescribeResult(name = "SMC Change", description = "Returns the change in soil moisture content.")
    public String execute(
            @DescribeParameter(name = "Start Timestamp First", description = "Starting timestamp for the first flight.")
            String startDateFirst,
            @DescribeParameter(name = "End Timestamp First", description = "Ending timestamp for the first flight.")
            String endDateFirst,
            @DescribeParameter(name = "Start Timestamp Second", description = "Starting timestamp for the second flight.")
            String startDateSecond,
            @DescribeParameter(name = "End Timestamp Second", description = "Ending timestamp for the second flight.")
            String endDateSecond
    ) throws Exception {
        // Validate first flight dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateLocal = LocalDateTime.parse(startDateFirst, formatter);
        LocalDateTime endDateLocal = LocalDateTime.parse(endDateFirst, formatter);

        LocalDateTime startDateSecondLocal = LocalDateTime.parse(startDateSecond, formatter);
        LocalDateTime endDateSecondLocal = LocalDateTime.parse(endDateSecond, formatter);
        if (startDateLocal.isAfter(endDateLocal) || startDateSecondLocal.isAfter(endDateSecondLocal)) {
            throw new IllegalArgumentException("Start must be before the end date.");
        }
        boolean validRange = endDateLocal.isBefore(startDateSecondLocal) || endDateLocal.equals(startDateSecondLocal)
                || endDateSecondLocal.isBefore(startDateLocal) || endDateSecondLocal.equals(startDateLocal);

        if (!validRange) {
            throw new IllegalArgumentException("Must be no overlap between the first flight range and second flight range.");
        }

        // Validates first and second do not overlap

        FeatureTypeInfo featureType = catalog.getFeatureTypeByName("tiger:poly_landmarks");

        SimpleFeatureSource featureSource = (SimpleFeatureSource) featureType.getFeatureSource(null, null);

        try (SimpleFeatureIterator featureIterator = featureSource.getFeatures().features()) {
            while (featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();
                System.out.println(feature.getAttribute("LANAME"));
            }
        }

        return "Hello, this is testing" + startDateFirst;
    }
}
