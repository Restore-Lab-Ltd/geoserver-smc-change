package co.nz.restorelab.smc;


import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.wps.gs.GeoServerProcess;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.api.filter.Filter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@DescribeProcess(title = "CalculateChange", description = "Calculates the change in soil moisture content.")
public class CalculateChangeProcess implements GeoServerProcess {

    private final Catalog catalog;

    public CalculateChangeProcess(Catalog catalog) {
        this.catalog = catalog;
    }

    @DescribeResult(name = "SMC Change", description = "Returns the change in soil moisture content.")
    public SimpleFeatureCollection execute(
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

        // Validates first and second do not overlap
        boolean validRange = endDateLocal.isBefore(startDateSecondLocal) || endDateLocal.equals(startDateSecondLocal)
                || endDateSecondLocal.isBefore(startDateLocal) || endDateSecondLocal.equals(startDateLocal);

        if (!validRange) {
            throw new IllegalArgumentException("Must be no overlap between the first flight range and second flight range.");
        }

        FeatureTypeInfo featureType = catalog.getFeatureTypeByName("restore-lab:smc_testing");

        SimpleFeatureSource featureSource = (SimpleFeatureSource) featureType.getFeatureSource(null, null);

        String cqlFirstRange = String.format("utc_time BETWEEN '%s' AND '%s'", startDateFirst, endDateFirst);
        String cqlSecondRange = String.format("utc_time BETWEEN '%s' AND '%s'", startDateSecond, endDateSecond);

        Filter filterFirst = CQL.toFilter(cqlFirstRange);
        Filter filterSecond = CQL.toFilter(cqlSecondRange);

        Map<Long, Double> averagesFirst = computeAverageByGrid(featureSource, filterFirst);
        Map<Long, Double> averagesSecond = computeAverageByGrid(featureSource, filterSecond);

        Map<Long, SimpleFeature> featureMap = copyFeatures(featureSource, filterSecond);

        // Create new SimpleFeatureLayer
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.init(featureSource.getSchema());
        typeBuilder.setName("smc_change_result");
        typeBuilder.add("smc_change", Double.class);
        SimpleFeatureType newType = typeBuilder.buildFeatureType();

        DefaultFeatureCollection resultCollection = new DefaultFeatureCollection();

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(newType);

        for (Long gridId : averagesFirst.keySet()) {
            if (averagesSecond.containsKey(gridId) && featureMap.containsKey(gridId)) {
                double change = averagesSecond.get(gridId) - averagesFirst.get(gridId);
                SimpleFeature original = featureMap.get(gridId);

                builder.reset();
                for (int i=0; i < original.getAttributeCount(); i++) {
                    builder.set(i, original.getAttribute(i));
                }
                builder.set("smc_change", change);

                SimpleFeature newFeature = builder.buildFeature(null);
                resultCollection.add(newFeature);
            }
        }
        return resultCollection;

    }

    private Map<Long, SimpleFeature> copyFeatures(SimpleFeatureSource source, Filter filter) throws IOException {
        Map<Long, SimpleFeature> featureMap = new HashMap<>();

        try (SimpleFeatureIterator it = source.getFeatures(filter).features()) {
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                Long id = (Long) f.getAttribute("id");
                featureMap.put(id, f);
            }
        }
        return featureMap;
    }

    private Map<Long, Double> computeAverageByGrid(SimpleFeatureSource source, Filter filter) throws IOException {
        Map<Long, Double> totalMap = new HashMap<>();
        Map<Long, Integer> countMap = new HashMap<>();

        try (SimpleFeatureIterator features = source.getFeatures(filter).features()) {
            while (features.hasNext()) {

                SimpleFeature feature = features.next();
                Long gridId = (Long) feature.getAttribute("id");
                Double smc = (Double) feature.getAttribute("smc_average");

                totalMap.put(gridId, totalMap.getOrDefault(gridId, 0d) + smc);
                countMap.put(gridId, countMap.getOrDefault(gridId, 0) + 1);
            }
        }

        Map<Long, Double> averages = new HashMap<>();
        for (Long gridId : totalMap.keySet()) {
            averages.put(gridId, totalMap.get(gridId) / countMap.get(gridId));
        }
        return averages;
    }
}
