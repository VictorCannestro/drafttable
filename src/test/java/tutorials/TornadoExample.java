package tutorials;

import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.rows.Row;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cannestro.drafttable.core.options.Item.*;
import static com.cannestro.drafttable.core.options.Items.*;
import static com.cannestro.drafttable.core.options.SortingOrderType.*;
import static java.time.Month.*;
import static org.hamcrest.Matchers.*;


/**
 * @author Victor Cannestro
 */
public class TornadoExample {

    public static void main(String[] args) {
        DraftTable tornadoes = FlexibleDraftTable.create()
                .fromCSV().at(Path.of("csv/tornadoes_1950-2014.csv"))
                .transform("Injuries", (String injuries) -> (int) Double.parseDouble(injuries))
                .transform("Fatalities", (String fatalities) -> (int) Double.parseDouble(fatalities))
                .transform("Start Lat", (String lat) -> Double.parseDouble(lat))
                .transform("Start Lon", (String lon) -> Double.parseDouble(lon))
                .transform("Scale", (String scale) -> Double.parseDouble(scale))
                .transform("Length", (String length) -> Double.parseDouble(length))
                .transform("Width", (String width) -> Double.parseDouble(width));

        // Viewing metadata examples
        System.out.println("Column Names: " + tornadoes.columnNames());
        System.out.println(tornadoes.shape());
        System.out.println("Not of type Double: " +
                tornadoes.whereColumnType(is(not(Double.class))).columnNames()
        );
        tornadoes.write().structure();
        System.out.println();

        // Viewing data examples
        tornadoes.top(2).write().prettyPrint();
        System.out.println();

        tornadoes.bottom(2).write().prettyPrint();
        System.out.println();

        tornadoes.randomDraw(3).write().prettyPrint();
        System.out.println();

        tornadoes.select("Injuries").write().describe();

        // Mapping example 1: melt columns
        tornadoes = tornadoes.melt("Date", "Time", into("DateTime"), (String date, String time) -> LocalDate.parse(date).atTime(LocalTime.parse(time)));

        // Mapping example 2: Drop columns
        System.out.println("Using dropColumns: " +
                tornadoes.drop(named("State No", "Start Lat", "Start Lon")).columnNames()
        );
        System.out.println("Using dropAllExcept: " +
                tornadoes.dropAllExcept(these("State", "Time", "Scale", "DateTime", "Injuries", "Fatalities", "Length", "Width")).columnNames()
        );

        // Mapping example 3: derive new column
        Function<String, String> censusBureauDivisions = (String state) -> switch (state) {
            case "CT", "ME", "MA", "NH", "RI", "VT" -> "New England";
            case "NJ", "NY", "PA" -> "Middle Atlantic";
            case "IL", "IN", "MI", "OH", "WI" -> "East North Central";
            case "IA", "KS", "MN", "MO", "NE", "ND", "SD" -> "West North Central";
            case "DE", "FL", "GA", "MD", "NC", "SC", "VA", "WV", "DC" -> "South Atlantic";
            case "AL", "KY", "MS", "TN" -> "East South Central";
            case "AR", "LA", "OK", "TX" -> "West South Central";
            case "AZ", "CO", "ID", "MT", "NV", "NM", "UT", "WY" -> "Mountain";
            case "WA", "OR", "CA" -> "Pacific";
            default ->  null;
        };
        tornadoes = tornadoes.deriveFrom("State", as("Region"), censusBureauDivisions);
        System.out.println("Should contain new column 'Region': " + tornadoes.columnNames());
        System.out.println();

        tornadoes.select("Region")
                .group()
                .byValueCounts(DESCENDING)
                .write()
                .prettyPrint();
        System.out.println();

        // Mapping example 4: gather subset into new column
        record PhysicalMeasurements (double Scale, double Length, double Width) {}
        tornadoes.gatherInto(PhysicalMeasurements.class, as("Measurements"), using("Scale", "Length", "Width"))
                 .top(2)
                 .write()
                 .toJsonString();

        tornadoes.gatherInto(PhysicalMeasurements.class, as("Measurements"), using("Scale", "Length", "Width"))
                 .select("Measurements")
                 .group().byValueCounts(DESCENDING)
                 .where("Value", PhysicalMeasurements::Scale, greaterThanOrEqualTo(0.0))
                 .where("Count", greaterThan(1000L))
                 .write().prettyPrint();

        System.out.println(
                tornadoes.gatherInto(PhysicalMeasurements.class, as("Measurements"), using("Scale", "Length", "Width"))
                        .top(1)
                        .where("Fatalities", is(-1))
                        .select(these("DateTime", "Measurements"))
                        .write()
                        .toJsonString()
        );

        List<PhysicalMeasurements> physicalMeasurements = tornadoes.gatherInto(PhysicalMeasurements.class, as("Measurements"), using("Scale", "Length", "Width"))
                .select("Measurements")
                .values();
        System.out.println(physicalMeasurements.get(0));
        System.out.println();

        // Filtering example 1
        System.out.println("Tornadoes before 1951: " +
                tornadoes.where("DateTime", lessThan(LocalDate.of(1951, 1, 1).atStartOfDay())).rowCount()
        );

        // Filtering example 2
        record Dimension(Double length, Double width) {}
        record Coordinate(Double lat, Double lon) {}
        record TornadoPathInfo(Coordinate coordinate, Dimension dimension) {}
        tornadoes.where("Fatalities", greaterThan(0))
                 .where("DateTime", LocalDateTime::getMonth, is(APRIL))
                 .melt("Length", "Width", as("Dimension"), Dimension::new)
                 .melt("Start Lat", "Start Lon", into("Coordinate"), Coordinate::new)
                 .melt("Coordinate", "Dimension",  into("PathInfo"), TornadoPathInfo::new)
                 .where("PathInfo", (TornadoPathInfo pathInfo) -> (pathInfo.dimension().length() > 10 || pathInfo.dimension().width() > 300) && (pathInfo.coordinate().lat() > 30.0 && pathInfo.coordinate().lat() < 40.0), is(true))
                 .select(using("State", "DateTime"));

        // Filtering to answer questions
        System.out.println(
                "Mid-Atlantic tornadoes: " + tornadoes.where("Region", is("Middle Atlantic")).rowCount()
        );
        System.out.println(
                "Tornadoes not in the Mid-Atlantic in the 1990's: " + tornadoes.where("Region", is(not("Middle Atlantic"))).where("DateTime", LocalDateTime::getYear, allOf(greaterThan(1989), lessThan(2000))).rowCount()
        );
        System.out.println(
                "Tornadoes in 2010 rated F3 or higher: " + tornadoes.where("DateTime", LocalDateTime::getYear, is(2010)).where("Scale", greaterThanOrEqualTo(3.0)).rowCount()
        );
        System.out.println();


        // Sorting and Ordering data
        tornadoes.orderBy(using("Fatalities", "DateTime"), DESCENDING)
                 .top(4)
                 .write()
                 .prettyPrint();
        System.out.println();

        tornadoes.orderBy(Comparator.comparing((Row row) -> ((LocalDateTime) row.valueOf("DateTime")).getDayOfMonth()))
                .top(3)
                .write()
                .prettyPrint();
        System.out.println();

        // Exporting to output formats
        System.out.println("To JSON output: " +
                tornadoes.where("DateTime", LocalDateTime::getYear, is(2010))
                         .where("Scale", greaterThanOrEqualTo(3.0))
                         .top(1)
                         .write()
                         .toJsonString()
        );
        System.out.println();

        // Putting it all together
        Function<LocalDateTime, Boolean> isInSummer = (LocalDateTime date) -> (date.getMonth().equals(JUNE) && date.getDayOfMonth() >= 21)
                                                                           || (List.of(JULY, AUGUST).contains(date.getMonth()))
                                                                           || (date.getMonth().equals(SEPTEMBER) && date.getDayOfMonth() < 22);
        tornadoes.where("DateTime", isInSummer, is(true))
                 .select("DateTime")
                 .orderBy(ASCENDING)
                 .top(10)
                 .write()
                 .prettyPrint();
        System.out.println();

        tornadoes.introspect(df -> FlexibleDraftTable.create().emptyDraftTable()
                        .append(df.where("DateTime", LocalDateTime::getMonth, is(JUNE))
                                 .where("DateTime", LocalDateTime::getDayOfMonth, greaterThanOrEqualTo(21)))
                        .append(df.where("DateTime", LocalDateTime::getMonth, in(List.of(JULY, AUGUST))))
                        .append(df.where("DateTime", LocalDateTime::getMonth, is(SEPTEMBER))
                                 .where("DateTime", LocalDateTime::getDayOfMonth, lessThan(22))))
                 .select("Scale")
                 .orderBy(ASCENDING)
                 .write()
                 .describe();
        System.out.println();

        record PlaceInTime(String place, LocalDateTime localDateTime) {}
        tornadoes.where("DateTime", LocalDateTime::getYear, allOf(greaterThan(1999), lessThan(2010)))
                .melt("Region", "DateTime", into("PlaceInTime"), PlaceInTime::new)
                .select("PlaceInTime")
                .group()
                .by((PlaceInTime pit) -> pit.localDateTime().getYear(), Collectors.groupingBy(PlaceInTime::place, Collectors.counting()))
                .write()
                .prettyPrint();

    }

}
