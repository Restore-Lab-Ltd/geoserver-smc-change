package co.nz.restorelab.smc;


import org.geoserver.wps.gs.GeoServerProcess;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@DescribeProcess(title = "CalculateChange", description = "Calculates the change in soil moisture content.")
public class CalculateChangeProcess implements GeoServerProcess {

    @DescribeResult(name = "SMC Change", description = "Returns the change in soil moisture content.")
    public String execute(
            @DescribeParameter(name = "Start Timestamp", description = "Starting timestamp for the calculation")
            String startDate,
            @DescribeParameter(name = "End Timestamp", description = "Ending timestamp for the calculation")
            String endDate
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime test = LocalDateTime.parse(startDate, formatter);
        System.out.println(test);

        return "Hello, this is testing" + startDate;
    }
}
