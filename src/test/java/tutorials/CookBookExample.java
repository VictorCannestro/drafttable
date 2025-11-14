package tutorials;

import com.cannestro.drafttable.core.inbound.DefaultJsonLoader;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.helper.Recipe;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;

import static com.cannestro.drafttable.core.options.Items.*;
import static com.cannestro.drafttable.core.options.SortingOrderType.ASCENDING;
import static org.hamcrest.Matchers.*;


public class CookBookExample {

    public static void main(String[] args) {
        Path path = Path.of("./src/test/resources/json/sample.json");
        DraftTable kitchenTable = FlexibleDraftTable.create()
                .fromJson(DefaultJsonLoader.class)
                .at(path, Recipe.class);
        kitchenTable.write().structure();

        kitchenTable.top(1)
                .write()
                .prettyPrint();

        kitchenTable.where("cuisine", is("Italian"))
                .orderBy(these("difficulty", "caloriesPerServing"), ASCENDING)
                .write()
                .prettyPrint();
    }

}
