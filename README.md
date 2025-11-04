# DraftTable

| Branch    | Status                                                                                                                                                                                                        |
|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `master`  | [![Build Status]()]()   |

## Introduction
**DraftTable** is a pure Java 17+ library loosely inspired by the Python DataFrame API and Java library Tablesaw that 
enables the creation of chainable data processing pipelines. It can be considered a collection of patterns built on top
of Java streams, lambdas, records, and generics to enable a more declarative style of programming. 

At a glance, `DraftTable` objects can be created from a collection of `Row`, `Column`, or other serializable objects. The
underlying data of a `Column` may be of an arbitrary type, and a `DraftTable` may have multiple columns of different 
types. `DraftTable` objects have a variety of capabilities to process tabular data.

## Features
- Import data from CSV, URL, or a collection of user defined Java objects
- Export data to a CSV, JSON, or a collection of user defined Java objects 
- Map/Filter/Reduce operations
- Sorting capabilities
- Grouping and aggregation
- Pipeline introspection in-line 
- Combine tables by appending 
- Add and remove columns or rows
- Handle missing values
- Edit existing values

## Tutorials
Ready to explore more? 

See the [Quick Start tutorial](https://github.com/VictorCannestro/drafttable/blob/master/tutorials/1_Quick_Start.md)
for a guided walkthrough and the [tutorials](https://github.com/VictorCannestro/drafttable/tree/master/tutorials)
directory, in general, for access to the latest tutorials.

## Examples
### Example 1: Reading in a CSV, processing it, then exporting the results to another CSV
```java
Path path = Path.of("./src/main/resources/csv/employee_data.csv");
FlexibleDraftTable.create().fromCSV().at(path)
                 .where("state", is(not("CA")))
                 .where("jobName", endsWith("manager"))
                 .transform("nonExempt", (String exemptValue) -> exemptValue.equals("1"))
                 .where("nonExempt", is(true))
                 .transform("hireDate", into("yearsOfService"), (String hireDate) -> Period.between(LocalDate.parse(hireDate), now()).getYears())
                 .orderBy("yearsOfService", DESCENDING)
                 .top(50)
                 .melt("countryCode", "employeeId", into("managerLoginName"), String::concat)
                 .selectMultiple(from("managerLoginName", "locationNumber"))
                 .write()
                 .toCSV("output/manager_login_information.csv");
```
### Example 2: Pretty printing a rich tabular display after combining and sorting by multiple columns
```java
URL url = url("https://raw.githubusercontent.com/VictorCannestro/drafttable/refs/heads/master/src/test/resources/csv/tornadoes_1950-2014.csv");
FlexibleDraftTable.create().fromCSV().at(url)
         .melt("Date", "Time", into("DateTime"), (String date, String time) -> LocalDate.parse(date).atTime(LocalTime.parse(time)))
         .orderByMultiple(using("Injuries", "DateTime"), DESCENDING)
         .top(10)
         .write()
         .prettyPrint();
```
```
                                             tornadoes_1950-2014                                                 
=================================================================================================================
| Start Lon | Length | State | Fatalities | Scale | State No | Width  | Injuries | Start Lat |     DateTime     |
=================================================================================================================
|    -98.65 |   46.9 |    TX |         42 |   4.0 |     43.0 | 1320.0 |     1740 |     33.82 | 1979-04-10T17:50 |
|    -98.65 |   34.1 |    TX |         42 |   4.0 |     43.0 | 1320.0 |     1740 |     33.82 | 1979-04-10T17:50 |
|   -87.935 |  80.68 |    AL |         64 |   4.0 |    103.0 | 2600.0 |     1500 |   33.0297 | 2011-04-27T15:43 |
|    -72.17 |   34.9 |    MA |         94 |   4.0 |      1.0 |  900.0 |     1228 |     42.47 | 1953-06-09T14:25 |
|  -94.5932 |  21.62 |    MO |        158 |   5.0 |     38.0 | 1600.0 |     1150 |   37.0524 | 2011-05-22T16:34 |
|    -84.05 |   31.3 |    OH |         36 |   5.0 |      3.0 |  533.0 |     1150 |     39.63 | 1974-04-03T13:30 |
|    -83.85 |   18.9 |    MI |        116 |   5.0 |     10.0 |  833.0 |      844 |      43.1 | 1953-06-08T19:30 |
|    -86.38 |   48.0 |    IN |         25 |   4.0 |     14.0 |  880.0 |      835 |      40.4 | 1965-04-11T18:20 |
|    -91.02 |  202.1 |    MS |         58 |   4.0 |     12.0 |  100.0 |      795 |      32.7 | 1971-02-21T16:00 |
|    -91.02 |  198.5 |    MS |         58 |   4.0 |     12.0 |  100.0 |      795 |      32.7 | 1971-02-21T16:00 |
```

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

## Development and Release
Development happens on the `develop` branch, which has the version number of the next release with "-SNAPSHOT" appended
to it in the `gradle.properties` file. All `master` branch releases will drop the  "-SNAPSHOT" post-fix in the version.
