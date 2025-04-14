package co.nz.restorelab.smc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

public class CalculateChangeProcessTest {
    private CalculateChangeProcess process;
    @BeforeEach
    void setUp() {
        process = new CalculateChangeProcess();
    }

    void runTestGood(String startDate, String endData) {
        String result = process.execute(startDate, endData);
        assertEquals("Hello, this is testing2025-02-12 12:02:02", result);
    }

    void runTestFail(String startDate, String endDate) {
        assertThrows(DateTimeParseException.class, () -> process.execute(startDate, endDate));
    }

    @Test void testGoodDate() {runTestGood("2025-02-12 12:02:02", "2025-02-13 12:02:02");}
    @Test void testBadGate() {runTestFail("2025 02 02 02:02:02", "2025 02 02 02:02:02");}
}
