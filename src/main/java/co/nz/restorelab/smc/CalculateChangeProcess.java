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
            @DescribeParameter(name = "Start Timestamp", description = "Starting timestamp for the calculation")
            String startDate,
            @DescribeParameter(name = "End Timestamp", description = "Ending timestamp for the calculation")
            String endDate
    ) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateLocal = LocalDateTime.parse(startDate, formatter);
        LocalDateTime endDateLocal = LocalDateTime.parse(endDate, formatter);
        if (!startDateLocal.isBefore(endDateLocal)) {
            throw new IllegalArgumentException("End data must be after the start date!");
        }

        FeatureTypeInfo featureType = catalog.getFeatureTypeByName("tiger:poly_landmarks");

        SimpleFeatureSource featureSource = (SimpleFeatureSource) featureType.getFeatureSource(null, null);

        try (SimpleFeatureIterator featureIterator = featureSource.getFeatures().features()) {
            while (featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();
                System.out.println(feature.getAttribute("LANAME"));
            }
        }

        System.out.println(featureSource);

        return "Hello, this is testing" + startDate;
    }
}
