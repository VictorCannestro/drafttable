package tutorials;

import com.cannestro.drafttable.core.outbound.DefaultDraftTableOutput;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.helper.Recipe;

import java.net.URL;

import static com.cannestro.drafttable.core.options.Items.*;
import static com.cannestro.drafttable.core.options.SortingOrderType.ASCENDING;
import static com.cannestro.drafttable.supporting.utils.NetUtils.url;
import static org.hamcrest.Matchers.*;


public class CookBookExample {

    public static void main(String[] args) {
        URL url = url("https://raw.githubusercontent.com/VictorCannestro/drafttable/refs/heads/develop/src/test/resources/json/multiple_recipes.json");
        DraftTable kitchenTable = FlexibleDraftTable.create().fromJsonArray().at(url, Recipe.class);

        kitchenTable.write().structure();

        System.out.println(
            kitchenTable.top(2).write().toJsonString()
        );

        kitchenTable.where("cuisine", is("Italian"))
                .orderBy(these("difficulty", "caloriesPerServing"), ASCENDING)
                .write(DefaultDraftTableOutput.class)
                .prettyPrint();
    }

}
