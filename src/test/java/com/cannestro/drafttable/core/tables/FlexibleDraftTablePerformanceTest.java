package com.cannestro.drafttable.core.tables;

import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.columns.FlexibleColumn;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;


/**
 *  Use JMH for detailed performance analysis: https://www.baeldung.com/java-microbenchmark-harness
 */
public class FlexibleDraftTablePerformanceTest {


    @Test(timeOut = 10_000)
    public void largeDataFrameWhereTest(){
        int columnSize = 10;
        int rowSize = 10_000_000;
        List<Column> cols = new ArrayList<>();
        for (int i = 0; i < columnSize; i++) {
            cols.add(new FlexibleColumn(
                            String.valueOf(i),
                            ThreadLocalRandom.current().doubles(rowSize, 0, 1).boxed().toList()
                    )
            );
        }
        FlexibleDraftTable.create().fromColumns(cols).where("0", is(greaterThan(0.50)));
    }

}
