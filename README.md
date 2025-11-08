# DraftTable

| Branch    | Status                                                                                                                                                                                                        |
|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `master`  | [![Build Status]()]()   |

## Introduction
**DraftTable** is a pure Java 17+ library loosely inspired by the Python DataFrame API and Java library Tablesaw that 
enables the creation of chainable data processing pipelines. It can be considered a collection of patterns built on top
of Java streams, lambdas, records, generics, and other open source libraries to enable a more declarative style of
programming. 

At a glance, a `DraftTable` object is a collection of `Row` or `Column` objects. The underlying data of a `Column` may 
have an arbitrary, homogeneous type, and a `DraftTable` may have zero, one, or more columns, each having potentially 
different types. A `Row`, on the other hand, may have an arbitrary number of types (with data corresponding to the type
of each `Column`). `DraftTable` objects have a variety of capabilities to process tabular or nested data in a *tabular 
fashion*.

## Features
- Import data from a [supported format](#supported-formats) via local or URL
- Export data to [supported format](#supported-formats)
- Map/Filter/Reduce operations (powered in part by [Java Hamcrest Matchers](https://github.com/hamcrest/JavaHamcrest))
- Sorting capabilities
- Grouping and aggregation
- Pipeline introspection in-line for conditional or branching actions
- Combining compatible tables via appending 
- Add or drop columns (or rows)
- Fill missing values
- Edit/transform existing values

## Tutorials
Ready to explore more? 

See the [Quick Start tutorial](https://github.com/VictorCannestro/drafttable/blob/master/tutorials/1_Quick_Start.md)
for a guided walkthrough and the [tutorials](https://github.com/VictorCannestro/drafttable/tree/master/tutorials)
directory, in general, for access to the latest tutorials.

## Examples
### Example 1: Reading in a CSV, processing it, then exporting the results to another CSV
```java
Path inputFilepath = Path.of("./src/main/resources/csv/employee_data.csv");
File outputFile = new File("output/manager_login_information.csv");
FlexibleDraftTable.create().fromCSV().at(inputFilepath)
                 .where("state", is(not("CA")))
                 .where("jobName", endsWith("manager"))
                 .transform("nonExempt", (String exemptValue) -> exemptValue.equals("1"))
                 .where("nonExempt", is(true))
                 .transform("hireDate", into("yearsOfService"), (String hireDate) -> Period.between(LocalDate.parse(hireDate), now()).getYears())
                 .orderBy("yearsOfService", DESCENDING)
                 .top(50)
                 .melt("countryCode", "employeeId", into("managerLoginName"), String::concat)
                 .select("managerLoginName", "locationNumber")
                 .write()
                 .toCSV(outputFile);
```

### Example 2: Pretty printing a rich tabular display after combining and sorting by multiple columns
```java
record Dimension(Double length, Double width) {}
URL url = url("https://raw.githubusercontent.com/VictorCannestro/drafttable/refs/heads/master/src/test/resources/csv/tornadoes_1950-2014.csv");
FlexibleDraftTable.create().fromCSV().at(url)
                 .nameTable("tornadoes_by_highest_injuries")
                 .melt("Date", "Time", into("DateTime"), (String date, String time) -> LocalDate.parse(date).atTime(LocalTime.parse(time)))
                 .transform("Length", (String length) -> Double.parseDouble(length))
                 .transform("Width", (String width) -> Double.parseDouble(width))
                 .melt("Length", "Width", into("Dimension"), Dimension::new)
                 .orderBy(these("Injuries", "DateTime"), DESCENDING)
                 .top(10)
                 .write()
                 .prettyPrint();
```
```
                                                    tornadoes_by_highest_injuries                                                     
======================================================================================================================================
| Start Lon | State | Fatalities | Scale | State No |              Dimension               | Injuries | Start Lat |     DateTime     |
======================================================================================================================================
|    -84.92 |    KY |        1.0 |   3.0 |     15.0 |   Dimension[length=21.1, width=10.0] |     98.0 |     37.43 | 1974-04-03T17:35 |
|    -85.38 |    IN |        2.0 |   4.0 |     16.0 |   Dimension[length=30.9, width=10.0] |     97.0 |     40.55 | 1965-04-11T19:10 |
|    -94.03 |    LA |        1.0 |   3.0 |     11.0 |  Dimension[length=32.0, width=500.0] |     96.0 |      32.1 | 1987-11-15T18:45 |
|    -90.72 |    AR |        3.0 |   3.0 |     17.0 |  Dimension[length=51.8, width=880.0] |     96.0 |      35.6 | 1952-03-21T18:45 |
|    -96.05 |    OK |        8.0 |   3.0 |     21.0 | Dimension[length=22.0, width=1760.0] |     95.0 |     35.47 | 1984-04-26T23:33 |
|     -82.7 |    FL |        3.0 |   3.0 |     36.0 |   Dimension[length=1.5, width=200.0] |     94.0 |     27.92 | 1978-05-04T09:47 |
|     -82.8 |    OH |        7.0 |   5.0 |      6.0 |  Dimension[length=34.0, width=400.0] |     93.0 |      38.7 | 1968-04-23T15:05 |
|    -95.95 |    KS |        1.0 |   4.0 |     23.0 |  Dimension[length=27.5, width=880.0] |     92.0 |     39.18 | 1960-05-19T17:47 |
|    -83.25 |    MI |        0.0 |   2.0 |     15.0 |  Dimension[length=5.0, width=2500.0] |     90.0 |      42.4 | 1997-07-02T16:00 |
|    -88.47 |    IL |        2.0 |   2.0 |     22.0 |  Dimension[length=35.9, width=120.0] |     90.0 |      41.4 | 1965-11-12T14:35 |
```

## Supported Formats
|        Format        | Import | Export |
|:--------------------:|:------:|:------:|
|   Delimited `.txt`   |   ✅    |   ✅    |
|        `.csv`        |   ✅    |   ✅    |
|        `.tsv`        |   ✅    |   ✅    |
|       `.json`        |   *    |   ✅    |
|        Excel         |   *    |        |
|    Apache Parquet    |   *    |        |
| User defined objects |   ✅    |   ✅    |

*indicates work on the table

## Binaries
Binaries are available on ___. To pull all the Java 17 compatible components of the DraftTable library, in
your `build.gradle` file dependencies include:
```groovy
forthcoming
```
where `gradle.properties` contains the latest version. For example:
```groovy
draftTableVersion = 0.+
```