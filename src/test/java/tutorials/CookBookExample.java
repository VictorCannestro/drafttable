package tutorials;

import com.cannestro.drafttable.supporting.utils.helper.CookBook;
import com.cannestro.drafttable.supporting.utils.ObjectMapperManager;

import java.io.FileReader;
import java.io.Reader;


public class CookBookExample {

    public static void main(String[] args) {
        CookBook cookBook = null;
        try(Reader reader = new FileReader("./src/test/resources/json/sample.json")) {
            cookBook = ObjectMapperManager.getInstance()
                    .defaultMapper()
                    .readValue(reader, CookBook.class);
        } catch (Exception ignored) {}

//        DraftTable kitchenTable = FlexibleDraftTable.create().fromObjects(cookBook.recipes());
//        kitchenTable.write().structure();
//
//        kitchenTable.where("cookTimeMinutes", is(lessThan(15.0)))
//                .where("caloriesPerServing", is(lessThan(400.0)))
//                .selectMultiple(using("name", "caloriesPerServing", "difficulty"))
//                .orderByMultiple(these("difficulty", "caloriesPerServing"), ASCENDING)
//                .write()
//                .prettyPrint();
//
//        kitchenTable.where("cuisine", is("Italian"))
//                .selectMultiple(using("name", "caloriesPerServing", "difficulty", "servings", "cookTimeMinutes"))
//                .orderByMultiple(these("difficulty", "caloriesPerServing"), ASCENDING)
//                .write()
//                .prettyPrint();
    }

}
