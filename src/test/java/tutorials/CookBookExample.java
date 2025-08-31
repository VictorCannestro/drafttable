package tutorials;

import com.cannestro.drafttable.core.DraftTable;
import com.cannestro.drafttable.core.implementations.tables.FlexibleDraftTable;
import com.cannestro.drafttable.utils.helper.CookBook;

import java.io.FileReader;
import java.io.Reader;

import static com.cannestro.drafttable.core.options.Items.*;
import static com.cannestro.drafttable.utils.mappers.GsonSupplier.DEFAULT_GSON;
import static org.hamcrest.Matchers.*;


public class CookBookExample {

    public static void main(String[] args) {
        CookBook cookBook = null;
        try(Reader reader = new FileReader("./src/test/resources/json/sample.json")) {
            cookBook = DEFAULT_GSON.fromJson(reader, CookBook.class);
        } catch (Exception ignored) {}

        DraftTable kitchenTable = FlexibleDraftTable.fromObjects(cookBook.recipes());
        kitchenTable.write().structure();

        kitchenTable.where("cookTimeMinutes", is(lessThan(15.0)))
                .where("caloriesPerServing", is(lessThan(400.0)))
                .selectMultiple(using("name", "caloriesPerServing", "difficulty"))
                .write()
                .prettyPrint();
    }

}
