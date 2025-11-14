# Exploring the Tornado Dataset
*Author(s): Victor Cannestro*

*Note: Special thanks to the TableSaw project. Their documentation provided the inspiration for this document. Our goal
here is to showcase the DraftTable library and provide a basis of comparison for users interested in adopting Java 
here is to showcase the DraftTable library and provide a basis of comparison for users interested in adopting Java 
dataframe capabilities.*

## Introduction
Here we'll explore the [tornado dataset](https://www.ncei.noaa.gov/access/monitoring/tornadoes/) maintained by
the NOAA. These tornado statistics are limited to the contiguous U.S. and are provided by the Storm Prediction
Center (SPC). In doing so we’ll cover a variety of functionality provided by this library, including:
- Reading/writing from/to input/output sources (CSV, JSON, etc.)
- Viewing table metadata
- Peaking at the `top(n)` or `bottom(n)` rows
- Generating descriptive statistics on columns
- Performing mapping operations over columns
- Adding and removing columns
- Filtering rows
- Sorting
- Grouping operations

The data used in this tutorial can be found in the `test/resources/csv` folder.

## Reading a CSV file
Here we read in the CSV file of tornado data directly and specify mapping rules for several columns. Our data is comma 
delimited (`','`) with rows separated by new lines (`'\n'`). Alternatively, `.txt` or `.tsv` formats could have also 
been used. By default, CSV data is read in as a `String`. To prepare our dataset to perform numerical operations, we'll 
need to parse any columns we're interested in operating on to a more fitting data type.

```java
Path filePath = Path.of("csv/tornadoes_1950-2014.csv");
DraftTable tornadoes = FlexibleDraftTable.create().fromCsv().at(filePath)
        .transform("Injuries", (String injuries) -> (int) Double.parseDouble(injuries))
        .transform("Fatalities", (String fatalities) -> (int) Double.parseDouble(fatalities))
        .transform("Start Lat", (String lat) -> Double.parseDouble(lat))
        .transform("Start Lon", (String lon) -> Double.parseDouble(lon))
        .transform("Scale", (String scale) -> Double.parseDouble(scale))
        .transform("Length", (String length) -> Double.parseDouble(length))
        .transform("Width", (String width) -> Double.parseDouble(width));
```

Column names in this `DraftTable` will appear exactly as they do in the CSV.

### Bean-Based Reading
Alternatively, we could pass in a user defined [Data](https://projectlombok.org/features/Data) class that 
`implements CsvBean` to bind CSV column names to corresponding fields in the mapping class, and, also, 
`implements Mappable` to be eligible to be converted into a `DraftTable`. The DraftTable library uses OpenCSV for these
underlying processing operations. See [OpenCSV's documentation](https://opencsv.sourceforge.net/#reading_into_beans) for
more. Using this bean-based reading approach, the pipeline we defined earlier would change to something like the 
following:
```java
Path filePath = Path.of("csv/tornadoes_1950-2014.csv");
CsvParsingOptions csvOptions = CustomizableParsingOptions.builder().type(TornadoDataBean.class).build();
FlexibleDraftTable.create().fromCsv().at(filePath, csvOptions);
``` 
or 
```java
FlexibleDraftTable.create().fromCsv(DefaultCsvLoader.class).load(filePath.toFile(), TornadoDataBean.class);
``` 
where `TornadoDataBean.class` defines the bindings of column names to field names and data types, specifies required
columns vs optional columns, etc. When using this approach, *the key names specified in `asMap()` will become the column
names* of the `DraftTable`, *not the CSV column names*. 

<details>
    <summary> Expand to see <b>TornadoDataBean.class</b> </summary>
  
```java
    @Data
    public class TornadoDataBean implements CsvBean, Mappable {
    
        @CsvBindByName(column = "Date") @CsvDate("yyyy-MM-dd") private LocalDate date;
        @CsvBindByName(column = "Time") @CsvDate("HH:mm:ss") private LocalTime time;
        @CsvBindByName(column = "State") private String state;
        @CsvBindByName(column = "State No") private String stateNumber;
        @CsvBindByName(column = "Scale") private double scale;
        @CsvBindByName(column = "Injuries") private double injuries;
        @CsvBindByName(column = "Fatalities") private double fatalities;
        @CsvBindByName(column = "Start Lat") private double startLat;
        @CsvBindByName(column = "Start Lon") private double startLon;
        @CsvBindByName(column = "Length") private double length;
        @CsvBindByName(column = "Width") private double width;
    
        @Override
        public Map<String, ?> asMap() {
            return MapBuilder.with()
                    .entry("date", date)
                    .entry("time", time)
                    .entry("state", state)
                    .entry("stateNumber", stateNumber)
                    .entry("scale", scale)
                    .entry("injuries", injuries)
                    .entry("fatalities", fatalities)
                    .entry("startLat", startLat)
                    .entry("startLon", startLon)
                    .entry("length", length)
                    .entry("width", width)
                    .asMap();
        }
    }
```
</details>


This is the recommended approach for datasets with many columns and a variety of data types since it scales.

### Advanced Reading
Outside these built-in capabilities, users may also integrate their own CSV parsers with the `DraftTable` library. All
that's required is to define a class that `implements CsvLoader`. Suppose we defined 
`public class MyCustomCsvLoader implements CsvLoader`. To access the methods of this custom loader in the pipeline we
just need to write:
```java
FlexibleDraftTable.create().fromCsv(MyCustomCsvLoader.class)
```


## Viewing table data
### Viewing the metadata
Let's start exploring the dataset by displaying the column names for reference:
```java
System.out.println(tornadoes.columnNames())
```
```
[State, Time, Scale, State No, Date, Injuries, Fatalities, Start Lat, Start Lon, Length, Width]
```

The `shape()` method displays the row and column counts:
```java
System.out.println(tornadoes.shape());
```
```
59944 rows X 11 columns
```

We can also filter columns by data type to understand the type distribution. For instance, to get a list of all columns
that are not of type `Double` we could use:
```java
System.out.println(
    tornadoes.whereColumnType(is(not(Double.class))).columnNames()
);
```
```
[State, Time, Scale, State No, Date, Injuries, Fatalities]
```

To generate a more comprehensive breakdown of the type distribution and count of any `null` values present on a
per-column basis we can invoke:
```java
tornadoes.write().structure();
```
```
             tornadoes_1950-2014              
==============================================
| ColumnName |       Type        | NullCount |
==============================================
|       Date |  java.lang.String |       0.0 |
| Fatalities | java.lang.Integer |       0.0 |
|   Injuries | java.lang.Integer |       0.0 |
|     Length |  java.lang.Double |       0.0 |
|      Scale |  java.lang.Double |       0.0 |
|  Start Lat |  java.lang.Double |       0.0 |
|  Start Lon |  java.lang.Double |       0.0 |
|      State |  java.lang.String |       0.0 |
|   State No |  java.lang.String |       0.0 |
|       Time |  java.lang.String |       0.0 |
|      Width |  java.lang.Double |       0.0 |
```

### The data at a glance

The `top(n)` method returns a new `DraftTable` containing the first `n` rows, up to the total row count.
```java
tornadoes.top(2).write().prettyPrint();
```
```
                                                 tornadoes_1950-2014                                                 
=====================================================================================================================
| Start Lon | Length | State | Fatalities |   Time   | Scale | State No | Width |    Date    | Injuries | Start Lat |
=====================================================================================================================
|    -90.22 |    6.2 |    MO |          0 | 11:00:00 |   3.0 |      1.0 | 150.0 | 1950-01-03 |        3 |     38.77 |
|    -90.12 |    3.3 |    IL |          0 | 11:10:00 |   3.0 |      1.0 | 100.0 | 1950-01-03 |        0 |     38.82 |
```

Analogously, the `bottom(n)` method returns a new `DraftTable` containing the last `n` rows, up to the total row count.
```java
tornadoes.bottom(2).write().prettyPrint();
```
```
                                                 tornadoes_1950-2014                                                 
=====================================================================================================================
| Start Lon | Length | State | Fatalities |   Time   | Scale | State No | Width |    Date    | Injuries | Start Lat |
=====================================================================================================================
|   -93.979 |   1.98 |    TX |          0 | 14:35:00 |   1.0 |      0.0 | 150.0 | 2014-12-27 |        0 |    30.864 |
|  -83.2833 |   0.74 |    GA |          0 | 10:26:00 |   2.0 |      0.0 | 180.0 | 2014-12-29 |        9 |   30.8157 |
```

To get a random uniform sample up to size `n`, up to the total row count, from the dataset, we can call:
```java
tornadoes.randomDraw(3).write().prettyPrint();
```
```
                                                 tornadoes_1950-2014                                                 
=====================================================================================================================
| Start Lon | Length | State | Fatalities |   Time   | Scale | State No | Width |    Date    | Injuries | Start Lat |
=====================================================================================================================
|    -86.78 |    0.1 |    IN |          0 | 19:12:00 |   0.0 |      8.0 |  10.0 | 1981-07-25 |        0 |      41.5 |
|    -82.05 |    0.5 |    FL |          0 | 17:27:00 |   0.0 |     15.0 |  30.0 | 2006-06-21 |        0 |     26.97 |
|   -104.27 |    0.1 |    WY |          0 | 14:50:00 |   0.0 |     13.0 |  30.0 | 1999-09-01 |        0 |     41.83 |
```

Similarly, a `Column` contains the same set of preview methods. For example, to view the top 5 entries from the `"Date"`
column we can write:
```java
tornadoes.select("Date").top(5).write().prettyPrint();
```
```
======================
| index |    Date    |
======================
|     0 | 1950-01-03 |
|     1 | 1950-01-03 |
|     2 | 1950-01-03 |
|     3 | 1950-01-03 |
|     4 | 1950-01-13 |
```

## Descriptive statistics

A variety of descriptive statistics are available on a per-column basis. For example, to generate the descriptive
statistics of the `"Injuries"` column, we simply invoke `descriptiveStats()` after selecting the column:
```java
Map<StatisticName, Number> descriptiveStats = tornadoes.select("Injuries").descriptiveStats();
```

Individual descriptive statistics may then be retrieved from the map using a supported `StatisticName.class` enumeration
value: `VARIANCE`, `MEAN`, `PERCENTILE_50`, etc.  To display a table of these statistics, we just need invoke the
`describe()` function of the column output:
```java
tornadoes.select("Injuries").write().describe();
```
```
      Column: "Injuries"       
===============================
| METRIC |       VALUE        |
===============================
|    25% |                0.0 |
|    50% |                0.0 |
|    75% |                0.0 |
|    max |             1740.0 |
|   mean | 1.7695849459500634 |
|    min |                0.0 |
|      n |            59944.0 |
|    std | 21.428911859227984 |
|    var |  459.1982634705618 |
```

For non-numeric columns, a default string will be displayed instead:
```java
tornadoes.select("State").write().describe();
```
```
Cannot describe non-numeric Column: 'State'
```

## Mapping operations

Mapping operations take a variety inputs and produce a *new* `DraftTable` as the output, enabling data processing chains
that do not require intermediate variables.

**Note: operations in a `FlexibleDraftTable` are generally not performed in-place.**

We can map arbitrary expressions onto a `DraftTable`, and many common operations are already built in: transforming,
melting, gathering, deriving, etc.

For instance, the following line will merge the `"Date"` and `"Time"` columns into a new column named
`"DateTime"` by mapping the respective components into a `LocalDateTime` object. Note that this
is a destructive action and will remove the `"Date"` and `"Time"` columns.
```java
tornadoes = tornadoes.melt("Date", "Time", into("DateTime"), (String date, String time) -> LocalDate.parse(date).atTime(LocalTime.parse(time)));
```

Removing columns is simple. We can call one of the flavors of the "drop" method to load shed or tidy up our table:
```java
tornadoes = tornadoes.drop("State No", "Start Lat", "Start Lon");
```

Equivalently, we could've written:
```java
tornadoes = tornadoes.dropAllExcept("State", "Time", "Scale", "DateTime", "Injuries", "Fatalities", "Length", "Width");
```

To add a new column(s), we, again, have several options, the simplest of which is to pass a `Colunmn` directly.
```java
Column c = FlexibleColumn.from("NewColumnName", List.of(importantData));
tornadoes = tornadoes.add(c);
```

For example, to add an index column, filling any missing values with `null`, we could write:
```java
tornadoes = tornadoes.add("Index", IntStream.range(0, tornadoes.rowCount()).boxed().toList(), null)
```
**Note:** that newly added columns should contain *at most* as many data points as the receiving `DraftTable`. Missing
entries will be padded with a user defined fill value. Attempting to add a column with *more* entries than the
`DraftTable` will result in an exception.

More often, however, we're interested in deriving or updating a column based on the existing data. To accomplish
these tasks we can call a flavor of `deriveFrom`, `transform`, `melt`, or `gatherInto`. We've already seen an
example of this after reading in the tornado CSV file, when we cast several `String` columns into numeric types.

Now let's look at a few examples. Suppose we want to introduce a new column containing information about whether the
tornado was observed in a particular [US Census Bureau division](https://en.wikipedia.org/wiki/List_of_regions_of_the_United_States).
We could add this derived column using:
```java
Function<String, String> toCensusBureauDivision = (String state) -> switch (state) {
    case "CT", "ME", "MA", "NH", "RI", "VT" -> "New England";
    case "NJ", "NY", "PA" -> "Middle Atlantic";
    case "IL", "IN", "MI", "OH", "WI" -> "East North Central";
    case "IA", "KS", "MN", "MO", "NE", "ND", "SD" -> "West North Central";
    case "DE", "FL", "GA", "MD", "NC", "SC", "VA", "WV", "DC" -> "South Atlantic";
    case "AL", "KY", "MS", "TN" -> "East South Central";
    case "AR", "LA", "OK", "TX" -> "West South Central";
    case "AZ", "CO", "ID", "MT", "NV", "NM", "UT", "WY" -> "Mountain";
    case "WA", "OR", "CA" -> "Pacific"; // Minus AK and HI - we're considering the mainland US
    default -> null;
};
tornadoes = tornadoes.deriveFrom("State", as("Region"), toCensusBureauDivision);
```

Next, suppose we're interested in assigning a severity category to each tornado based on rules around its `Length` and
`Width`. For instance, a simple mapping could be:
```java
BiFunction<Double, Double, String> categorize = (Double length, Double width) -> {
    if (length > 50.0 || width > 1000.0) 
        return "High";
    if (length > 30.0 || width > 500.0)
        return "Medium";
    else
        return "Low";
};
tornadoes = tornadoes.deriveFrom("Length", "Width", into("Category"), categorize);
```

**Note:** If the explicit type reference looks cumbersome, there is the option of using Java 17+ syntax. For example,
`BiFunction<Double, Double, String> categorize` could become `var categorize`. Use caution with this approach because it
comes at the cost of clarity in definition.


## Filtering

There are many options to filter rows based on a variety of criteria. The simplest of which would be to call one of the
flavors of `where` on a column with a `Matcher` such as:
```java
tornadoes.where("Injuries", is(0))
         .where("DateTime", lessThan(LocalDate.of(1951, 1, 1).atStartOfDay()));
```
**The nature of these chainable data processing pipelines is to act as an AND operation.** In other words, the next
`where` condition further filters the resulting `DraftTable` of the previous `where`.

So, the filtering condition applied in the prior example can be interpreted as *"Find all rows in which there were no
injuries & occurred before 1951."*

Let's look at a more involved example. Suppose we wanted to identify rows in which there was
- 1 or more fatalities
- The event occurred in the month of April
- The length > 10 or the width > 300

That last OR condition on two distinct columns will require some creativity on our part. An easy way to approach this
would be to bundle the relevant columns into a Java `record` or `Map` to enable us to use both fields in the same
conditional expression:
```java
// Define a local Java record for convenience in data processing
record Dimension(Double length, Double width) {}

tornadoes.where("Fatalities", greaterThan(0))
         .where("DateTime", LocalDateTime::getMonth, is(APRIL))
         .melt("Length", "Width", as("Dimension"), Dimension::new)
         .where("Dimension", (Dimension dim) -> (dim.length() > 10) || (dim.width() > 300), is(true))
         .select("State", "DateTime");
```
We end the pipeline by filtering out all columns other than `"State"` and `"DateTime"`.  Using this bundling strategy
we can create increasingly sophisticated filtering conditions that incorporate multiple columns. For example:
```java
record Dimension(Double length, Double width) {}
record Coordinate(Double lat, Double lon) {}
record TornadoPathInfo(Coordinate coordinate, Dimension dimension) {}

tornadoes.melt("Length", "Width", as("Dimension"), Dimension::new)
         .melt("Start Lat", "Start Lon", into("Coordinate"), Coordinate::new)
         .melt("Coordinate", "Dimension",  into("PathInfo"), TornadoPathInfo::new)
         .where("PathInfo", (TornadoPathInfo pathInfo) -> (pathInfo.dimension().length() > 10 || pathInfo.dimension().width() > 300) 
                                                       && (pathInfo.coordinate().lat() > 30.0 && pathInfo.coordinate().lat() < 40.0), is(true))
         .select("State", "DateTime");
```

Now, for fun, let's use our pipeline to answer a variety of questions before moving on:
```java
System.out.println("Mid-Atlantic tornadoes: " + tornadoes.where("Region", is("Middle Atlantic")).rowCount());
System.out.println("Tornadoes not in the Mid-Atlantic in the 1990's: " + tornadoes.where("Region", is(not("Middle Atlantic"))).where("DateTime", LocalDateTime::getYear, allOf(greaterThan(1989), lessThan(2000))).rowCount());
System.out.println("Tornadoes in 2010 rated F3 or higher: " + tornadoes.where("DateTime", LocalDateTime::getYear, is(2010)).where("Scale", greaterThanOrEqualTo(3.0)).rowCount());
```
```
Mid-Atlantic tornadoes: 1331
Tornadoes not in the Mid-Atlantic in the 1990's: 11878
Tornadoes in 2010 rated F3 or higher: 59
```


## Sorting

Sorting and ordering can be performed several ways. The simplest of which is to pass the name of the column to sort on
along with the `enum` value `ASCENDING` or `DESCENDING`.

Let's sort the tornadoes `DraftTable` in descending order by the `Fatalities` column.
```java
tornadoes = tornadoes.orderBy("Fatalities", DESCENDING)
```

We can also order by multiple columns, up to the total column count. For example, let's order by `"Fatalities"` and
`"DateTime"` and view the top 4 results:
```java
tornadoes.orderBy(these("Fatalities", "DateTime"), DESCENDING).top(4).write().prettyPrint();
```
```
                                                         tornadoes_1950-2014                                                          
=====================================================================================================================================
| Start Lon | Length | State | Fatalities |       Region       | Scale | State No | Width  | Injuries | Start Lat |     DateTime     |
======================================================================================================================================
|  -94.5932 |  21.62 |    MO |        158 | West North Central |   5.0 |     38.0 | 1600.0 |     1150 |   37.0524 | 2011-05-22T16:34 |
|    -83.85 |   18.9 |    MI |        116 | East North Central |   5.0 |     10.0 |  833.0 |      844 |      43.1 | 1953-06-08T19:30 |
|    -97.15 |   20.9 |    TX |        114 | West South Central |   5.0 |      9.0 |  583.0 |      597 |     31.55 | 1953-05-11T16:10 |
|    -72.17 |   34.9 |    MA |         94 |        New England |   4.0 |      1.0 |  900.0 |     1228 |     42.47 | 1953-06-09T14:25 |
```

Alternatively, we could explicitly define a `Comparator<Row>` to compare row values according to a user defined rule:
```java
tornadoes.orderBy(Comparator.comparing((Row row) -> ((LocalDateTime) row.valueOf("DateTime")).getDayOfMonth()))
         .top(3)
         .write()
         .prettyPrint();
```
```
                                                         tornadoes_1950-2014                                                          
=====================================================================================================================================
| Start Lon | Length | State | Fatalities |       Region       | Scale | State No | Width | Injuries | Start Lat |     DateTime     |
=====================================================================================================================================
|    -88.85 |    0.1 |    MS |          0 | East South Central |   1.0 |      3.0 |  10.0 |        0 |      32.5 | 1950-03-01T02:30 |
|     -93.3 |    2.0 |    LA |          0 | West South Central |   1.0 |     12.0 | 100.0 |        0 |      31.7 | 1950-05-01T01:00 |
|    -91.07 |    0.1 |    MS |          0 | East South Central |   1.0 |     11.0 |  10.0 |        0 |     31.73 | 1950-05-01T05:00 |
```

**Notes:** 
- Sorting without using a user-defined rule will be performed in "natural" order.
- Notice the casting performed in the `Comparator<Row>` example highlighted in this snippet: 
`((LocalDateTime) row.valueOf("DateTime"))`. This casting is *required* to be able to access the native methods of a 
particular `Row` value. It is the price we pay for dabbling deeply in wildcards.


## Grouping your data
To produce an aggregate grouping of a selected column, we can invoke the `group()` method to open up grouping
operations. For example, to group `tornadoes` by `Region` according to descending value counts we can write:
```java
tornadoes.select("Region").group().byValueCounts(DESCENDING).write().prettyPrint();
```
```
      Grouping: "Region"      
==============================
|       Value        | Count |
==============================
| West North Central | 16190 |
| West South Central | 15546 |
|     South Atlantic |  8158 |
| East North Central |  7107 |
| East South Central |  6153 |
|           Mountain |  4286 |
|    Middle Atlantic |  1331 |
|            Pacific |   634 |
|        New England |   539 |
```
**Note:** This operation will produce a *new* `DraftTable` with columns `Value` and `Count`.

We can even go one step further and group by value counts on a complex object. The best part is that since the type
information of the values are preserved, and a new `DraftTable` is produced, we can continue operating on the grouping to
produce aggregate filtering and transformation operations. For example:
```java
record PhysicalMeasurements (double Scale, double Length, double Width) {}

tornadoes.gatherInto(PhysicalMeasurements.class, as("Measurements"), using("Scale", "Length", "Width"))
         .select("Measurements")
         .group().byValueCounts(DESCENDING)
         .where("Value", notNullValue())
         .where("Value", PhysicalMeasurements::Scale, greaterThanOrEqualTo(0.0))
         .where("Count", greaterThan(1000L))
         .write().prettyPrint();
```
```
                     Grouping: "Measurements"                      
===================================================================
|                          Value                          | Count |
===================================================================
| PhysicalMeasurements[Scale=0.0, Length=0.1, Width=10.0] |  5691 |
| PhysicalMeasurements[Scale=1.0, Length=0.1, Width=10.0] |  2636 |
| PhysicalMeasurements[Scale=0.0, Length=0.1, Width=20.0] |  1086 |
| PhysicalMeasurements[Scale=0.0, Length=0.1, Width=50.0] |  1051 |
```

### Advanced Grouping
Beyond `valueCounts` there are several other grouping operations that generalize different aspects of the value count,
such as `byCountsOf` and `byValuesUsing`.

Further still, we can supply a value mapping and customized aggregation to the `by` method for more advanced grouping
operations. For example, suppose we wish to find the count of recorded tornadoes per year in the 2000's grouped by
region. One way to solve this is to write:
```java
record PlaceInTime(String place, LocalDateTime localDateTime) {}

tornadoes.where("DateTime", LocalDateTime::getYear, allOf(greaterThan(1999), lessThan(2010)))
         .melt("Region", "DateTime", into("PlaceInTime"), PlaceInTime::new)
         .select("PlaceInTime")
         .group()
         .by((PlaceInTime pit) -> pit.localDateTime().getYear(), Collectors.groupingBy(PlaceInTime::place, Collectors.counting()))
         .write()
         .prettyPrint();
```
```
                                                                                   Grouping: "PlaceInTime"                                                                                   
=============================================================================================================================================================================================
| Value |                                                                                 ValueAggregation                                                                                  |
=============================================================================================================================================================================================
|  2000 |  {Middle Atlantic=10, West North Central=269, East South Central=121, Mountain=98, West South Central=272, Pacific=15, New England=5, South Atlantic=171, East North Central=116} |
|  2001 |  {Middle Atlantic=17, West North Central=459, East South Central=136, Mountain=88, West South Central=274, Pacific=14, New England=8, East North Central=100, South Atlantic=135} |
|  2002 |  {Middle Atlantic=31, West North Central=245, East South Central=116, West South Central=262, Mountain=55, Pacific=5, New England=10, East North Central=119, South Atlantic=112} |
|  2003 |   {Middle Atlantic=34, West North Central=454, East South Central=174, Mountain=63, West South Central=327, Pacific=4, New England=2, East North Central=188, South Atlantic=169} |
|  2004 | {Middle Atlantic=36, West North Central=569, East South Central=152, Mountain=120, West South Central=318, Pacific=27, New England=4, East North Central=194, South Atlantic=416} |
|  2005 |  {Middle Atlantic=15, West North Central=402, East South Central=228, Mountain=76, West South Central=224, Pacific=35, New England=3, South Atlantic=171, East North Central=109} |
|  2006 |  {Middle Atlantic=17, West North Central=335, East South Central=149, Mountain=45, West South Central=215, Pacific=15, New England=5, South Atlantic=149, East North Central=209} |
|  2007 |    {Middle Atlantic=10, West North Central=374, East South Central=117, Mountain=92, West South Central=289, Pacific=8, New England=4, East North Central=94, South Atlantic=127} |
|  2008 |   {Middle Atlantic=8, West North Central=555, East South Central=286, Mountain=77, West South Central=365, Pacific=10, New England=7, South Atlantic=283, East North Central=146} |
|  2009 |  {Middle Atlantic=19, West North Central=299, East South Central=205, Mountain=74, West South Central=294, Pacific=12, New England=12, East North Central=96, South Atlantic=170} |
```

## Saving your data
### To CSV
We can write our data to a CSV file using:
```java
tornadoes.write().toCSV(new File("./src/test/resources/csv/temp.csv"));
```
or 
```java
CsvWritingOptions writingOptions = CustomizableWritingOptions.builder().delimiter('|').fillerValue("NULL").lineEnder(";").build();
tornadoes.write().toCSV(new File("./src/test/resources/csv/temp.csv"), writingOptions);
```
where the addition of the `CsvWritingOptions` argument enables us to overwrite the defaults of things like export
delimiters, quote characters, fill values for missing entries, etc.

**Note:** that this is a terminal operation (of return type `void`) and will end the data processing pipeline.

### To JSON
Exporting `DraftTable` or `Column` data to a JSON or JSON string is also supported:
```java
System.out.println(
    tornadoes.top(1).write().toJsonString()
);
```
```
{"label":"data","values":[{"Start Lon":-94.1689,"Length":15.56,"State":"TX","Fatalities":0,"Region":"West South Central","Scale":3.0,"State No":"2.0","Width":1087.0,"Injuries":0,"Start Lat":32.4869,"DateTime":"2010-01-20T17:18:00"}]}
```

Similarly, to produce a JSON output file we could write something like:
```java
tornadoes.write().toJson(new File(outputFilePath));
```

### Into a user-defined object
Alternatively, we can gather the entire `DraftTable` or a defined subset of columns into a collection of user defined
objects. For instance:

```java
// Note that the field names match the column names!
record PhysicalMeasurements(double Scale, double Length, double Width) {}
    
List<PhysicalMeasurements> measurements = tornadoes
        .gatherInto(PhysicalMeasurements.class, as("Measurements"), using("Scale", "Length", "Width"))
        .select("Measurements")
        .getValues();
```
**Note:** The field names of your target class **must exactly match** the column names used in the gathering!

So remember to follow Java conventions and best practices by using camelCase column names and field names. (The irony of
this statement is not lost on the author given the column names used in this tutorial.)


## Putting it all together

Suppose we want to know how frequently tornadoes occur in the summer in the Mid-Atlantic. First, we'll need to filter
out rows that don't correspond to summer dates. An easy way to approach this would be to define a function to act as a
boolean mask when we apply it to the dataset.
```java
Function<LocalDateTime, Boolean> isInSummer = (LocalDateTime date) -> (date.getMonth().equals(JUNE) && date.getDayOfMonth() >= 21)
                                                                   || (List.of(JULY, AUGUST).contains(date.getMonth()))
                                                                   || (date.getMonth().equals(SEPTEMBER) && date.getDayOfMonth() < 22);
tornadoes.where("DateTime", isInSummer, is(true))
         .where("Region", is("Middle Atlantic"))
         .select("DateTime")
         .orderBy(ASCENDING);
```

Alternatively, we could arrive at the same subset by chaining together several methods and using the introspection
capability to enable in-line self-referencing:
```java
tornadoes.introspect(df -> FlexibleDraftTable.emptyDraftTable()
              .append(df.where("DateTime", LocalDateTime::getMonth, is(JUNE))
                        .where("DateTime", LocalDateTime::getDayOfMonth, greaterThanOrEqualTo(21)))
              .append(df.where("DateTime", LocalDateTime::getMonth, in(List.of(JULY, AUGUST))))
              .append(df.where("DateTime", LocalDateTime::getMonth, is(SEPTEMBER))
                        .where("DateTime", LocalDateTime::getDayOfMonth, lessThan(22))))
         .where("Region", is("Middle Atlantic"))
         .select("DateTime")
         .orderBy(ASCENDING);
```

Note that this only works here because the filter conditions are *mutually exclusive*.


#### The complete pipeline
```java
Path inputFilepath = Path.of("csv/tornadoes_1950-2014.csv");
File outputFile = new File("./data/mid-atlantic_tornadoes_summer_1950-2014.csv");
FlexibleDraftTable.create().fromCsv().at(inputFilepath)
                 .dropAllExcept("Date", "Time", "State", "Injuries", "Fatalities")
                 .transform("Injuries", (String injuries) -> (int) Double.parseDouble(injuries))
                 .transform("Fatalities", (String fatalities) -> (int) Double.parseDouble(fatalities))
                 .melt("Date", "Time", into("DateTime"), (String date, String time) -> LocalDate.parse(date).atTime(LocalTime.parse(time)))
                 .deriveFrom("State", as("Region"), toCensusBureauDivision)
                 .where("DateTime", isInSummer, is(true))
                 .where("Region", is("Middle Atlantic"))
                 .orderBy(ASCENDING)
                 .write()
                 .toCSV(outputFile);
```


## Related Readings
- [Research Article: Relationship between tornado path length and width to intensity](https://www.nssl.noaa.gov/users/brooks/public_html/papers/lengthwidth.pdf)
- [Tablesaw: A brief tutorial](https://jtablesaw.github.io/tablesaw/tutorial)