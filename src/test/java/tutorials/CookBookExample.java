package tutorials;

import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.helper.CookBook;
import com.cannestro.drafttable.supporting.utils.ObjectMapperManager;

import java.io.FileReader;
import java.io.Reader;

import static com.cannestro.drafttable.core.options.Items.*;
import static com.cannestro.drafttable.core.options.SortingOrderType.ASCENDING;
import static org.hamcrest.Matchers.*;


public class CookBookExample {

    public static void main(String[] args) {
        CookBook cookBook = null;
        try(Reader reader = new FileReader("./src/test/resources/json/sample.json")) {
            cookBook = ObjectMapperManager.getInstance()
                    .defaultMapper()
                    .readValue(reader, CookBook.class);
        } catch (Exception ignored) {}

        DraftTable kitchenTable = FlexibleDraftTable.create().fromObjects(cookBook.recipes());
        kitchenTable.write().structure();

        kitchenTable.top(1)
                .write()
                .prettyPrint();

        kitchenTable.where("cuisine", is("Italian"))
                .select(using("name", "caloriesPerServing", "difficulty", "servings", "cookTimeMinutes"))
                .orderBy(these("difficulty", "caloriesPerServing"), ASCENDING)
                .write()
                .prettyPrint();
    }

}
