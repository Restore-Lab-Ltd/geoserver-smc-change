package co.nz.restorelab.smc;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;

import org.geotools.api.filter.Filter;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CalculateChangeProcessIntegrationTests {

    private CalculateChangeProcess process;

    @BeforeEach
    public void setUp() throws Exception {
        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();
        for (int i = 0; i < 10; i++) {
            featureCollection.add(createFeature());
        }

        SimpleFeatureSource mockFeatureSource = mock(SimpleFeatureSource.class);
        when(mockFeatureSource.getFeatures(any(Filter.class))).thenReturn(featureCollection);

        FeatureTypeInfo mockFeatureTypeInfo = mock(FeatureTypeInfo.class);
        doReturn(mockFeatureSource).when(mockFeatureTypeInfo).getFeatureSource(null, null);

        Catalog mockCatalog = mock(Catalog.class);
        when(mockCatalog.getFeatureTypeByName("restore-lab:smc_testing")).thenReturn(mockFeatureTypeInfo);

        process = new CalculateChangeProcess(mockCatalog);
    }

//    @Test public void testGood() throws Exception {
//        SimpleFeatureCollection result = process.execute("2023-01-01 00:00:00", "2023-01-02 00:00:00",
//                "2024-01-01 00:00:00", "2024-01-02 00:00:00");
////        assertEquals("1, 0.000\n", result);
//    }

    @Test public void testBadFirstDate() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> process.execute("2024-01-01 00:00:00",
                "2023-01-02 00:00:00",
                "2024-01-01 00:00:00",
                "2023-01-02 00:00:00"));
        assertEquals("Start must be before the end date.", exception.getMessage());
    }

    @Test public void testBadSecondDate() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> process.execute("2023-01-01 00:00:00",
                "2023-01-02 00:00:00",
                "2024-01-01 00:00:00",
                "2023-01-02 00:00:00"));
        assertEquals("Start must be before the end date.", exception.getMessage());
    }

    @Test public void testMalformedDate() {
        assertThrows(DateTimeParseException.class, () -> process.execute("2023 01 01 00 00 00",
                "2023-01-02 00:00:00",
                "2024-01-01 00:00:00",
                "2023-01-02 00:00:00"));
    }

    @Test public void testInvalidDateRangeOverlap() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> process.execute("2023-01-01 00:00:00",
                "2023-01-02 00:00:00",
                "2023-01-01 00:00:00",
                "2023-01-02 00:00:00"));
        assertEquals("Must be no overlap between the first flight range and second flight range.", exception.getMessage());
    }

//    private SimpleFeatureCollection createValidFeatureCollection(DefaultFeatureCollection originalCollection) {
//        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
//        typeBuilder.init(originalCollection.getSchema());
//        typeBuilder.setName("smc_change_result");
//        typeBuilder.add("smc_change", Double.class);
//
//        Defa
//    }

    private SimpleFeature createFeature() {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("smc_testing");
        typeBuilder.add("smc_average", Double.class);
        typeBuilder.add("id", Long.class);
        SimpleFeatureType featureType = typeBuilder.buildFeatureType();

        // Creating a fake feature
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
        featureBuilder.add(1d);
        featureBuilder.add(1L);
        return featureBuilder.buildFeature("fid.1");
    }
}