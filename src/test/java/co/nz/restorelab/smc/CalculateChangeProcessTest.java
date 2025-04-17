package co.nz.restorelab.smc;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.*;

public class CalculateChangeProcessTest {

    private CalculateChangeProcess process;

    @BeforeEach
    public void setUp() throws Exception {
        SimpleFeature feature = createFeature();
        DefaultFeatureCollection test = new DefaultFeatureCollection();
        test.add(feature);

        SimpleFeatureSource mockFeatureSource = mock(SimpleFeatureSource.class);
        when(mockFeatureSource.getFeatures()).thenReturn(test);

        FeatureTypeInfo mockFeatureTypeInfo = mock(FeatureTypeInfo.class);
        doReturn(mockFeatureSource).when(mockFeatureTypeInfo).getFeatureSource(null, null);

        Catalog mockCatalog = mock(Catalog.class);
        when(mockCatalog.getFeatureTypeByName("tiger:poly_landmarks")).thenReturn(mockFeatureTypeInfo);

        process = new CalculateChangeProcess(mockCatalog);
    }

    @Test public void testGood() throws Exception {
        String result = process.execute("2023-01-01 00:00:00", "2023-01-02 00:00:00",
                "2024-01-01 00:00:00", "2024-01-02 00:00:00");
        assertTrue(result.contains("Hello, this is testing"));
    }

    @Test public void testBadFirstDate() {
        assertThrows(IllegalArgumentException.class, () -> process.execute("2024-01-01 00:00:00",
                "2023-01-02 00:00:00",
                "2024-01-01 00:00:00",
                "2023-01-02 00:00:00"));
    }

    @Test public void testBadSecondDate() {
        assertThrows(IllegalArgumentException.class, () -> process.execute("2023-01-01 00:00:00",
                "2023-01-02 00:00:00",
                "2024-01-01 00:00:00",
                "2023-01-02 00:00:00"));
    }

    @Test public void testMalformedDate() {
        assertThrows(DateTimeParseException.class, () -> process.execute("2023 01 01 00 00 00",
                "2023-01-02 00:00:00",
                "2024-01-01 00:00:00",
                "2023-01-02 00:00:00"));
    }

    public SimpleFeature createFeature() {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("poly_landmarks");
        typeBuilder.add("LANAME", String.class);
        SimpleFeatureType featureType = typeBuilder.buildFeatureType();

        // Creating a fake feature
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
        featureBuilder.add("Test County");
        return featureBuilder.buildFeature("fid.1");
    }
}