//package co.nz.restorelab.smc;
//
//import org.geoserver.catalog.Catalog;
//import org.geoserver.catalog.FeatureTypeInfo;
//import org.geotools.api.data.FeatureSource;
//import org.geotools.api.data.SimpleFeatureSource;
//import org.geotools.api.feature.simple.SimpleFeature;
//import org.geotools.api.feature.simple.SimpleFeatureType;
//import org.geotools.data.simple.SimpleFeatureCollection;
//import org.geotools.feature.DefaultFeatureCollection;
//import org.geotools.feature.simple.SimpleFeatureBuilder;
//import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.time.format.DateTimeParseException;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
//public class CalculateChangeProcessTest {
//
//    @Mock private Catalog catalog;
//    @Mock private FeatureTypeInfo featureTypeInfo;
//    @Mock private FeatureSource featureSource;
//    @InjectMocks private CalculateChangeProcess process;
//
//    private SimpleFeatureCollection fakeFeatureCollection;
//
//    @BeforeEach
//    void setUp() throws Exception {
//        MockitoAnnotations.openMocks(this);
//        process = new CalculateChangeProcess(catalog);
//
//        // Create fake feature type
//        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
//        typeBuilder.setName("TestType");
//        typeBuilder.add("smc_average", Double.class);
//        SimpleFeatureType featureType = typeBuilder.buildFeatureType();
//
//        // Fake features
//        DefaultFeatureCollection collection = new DefaultFeatureCollection(null, featureType);
//        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
//        collection.add(builder.buildFeature(null, new Object[]{10.0}));
//        collection.add(builder.buildFeature(null, new Object[]{20.0}));
//        fakeFeatureCollection = collection;
//
//        when(catalog.getFeatureTypeByName("testLayer")).thenReturn(featureTypeInfo);
//        when(featureTypeInfo.getFeatureSource(any(), any())).thenReturn(featureSource);
//
//    }
//
//    void runTestGood(String startDate, String endData) throws Exception {
//        String result = process.execute(startDate, endData);
//        assertEquals("Hello, this is testing2025-02-12 12:02:02", result);
//    }
//
//    void runTestFail(String startDate, String endDate) {
//        assertThrows(DateTimeParseException.class, () -> process.execute(startDate, endDate));
//    }
//
//    @Test void testGoodDate() throws Exception {runTestGood("2025-02-12 12:02:02", "2025-02-13 12:02:02");}
//    @Test void testBadDate() {runTestFail("2025 02 02 02:02:02", "2025 02 02 02:02:02");}
//}
