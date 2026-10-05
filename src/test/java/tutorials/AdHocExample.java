package tutorials;

import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.helper.CookBook;
import com.cannestro.drafttable.helper.Recipe;
import com.cannestro.drafttable.supporting.http.*;
import org.slf4j.event.Level;

import java.net.URI;
import java.net.http.HttpClient;


public class AdHocExample {

    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();

        String urlString = "https://raw.githubusercontent.com/VictorCannestro/drafttable/refs/heads/develop/src/test/resources/json/multiple_recipes.json";
        FlexibleDraftTable.create()
                .fromHttp(client)
                .getJsonArray(Recipe.class, HttpRequestWrapper.with(URI.create(urlString)))
                .write()
                .prettyPrint();

        URI test = URI.create(urlString + "?name=eggplant+parmesan");
        System.out.println(test);

        URI foodUri = URIAssembler.create()
                .baseUri("https://raw.githubusercontent.com")
                .path("/VictorCannestro/drafttable/refs/heads/develop/src/test/resources/json/cookbook.json")
                .queryParam("page","0")
                .queryParam("name", "eggplant parmesan")
                .fragment("recipes")
                .toURI();
        System.out.println(foodUri);

        URI modifiedFoodUri = URIAssembler.modifyExisting(foodUri)
                .queryParam("FOO", "BAR R")
                .fragment("stuff")
                .toURI();
        System.out.println(modifiedFoodUri);

        HttpRequestWrapper httpRequestWrapper = HttpRequestWrapper.builder()
                .uriAssembler(URIAssembler.passAlong(modifiedFoodUri))
                .logFormatter(HttpRequestLogFormatter.skipLogging())
                .build();

        HttpResponseWrapper httpResponseWrapper = HttpResponseWrapper
                .allDefaults()
                .withLogFormatter(HttpResponseLogFormatter.logEverything().withLogLevel(Level.DEBUG));
        FlexibleDraftTable.create()
                .fromHttp(client)
                .getAs(CookBook.class, CookBook::getRecipes, httpRequestWrapper, httpResponseWrapper)
                .write()
                .structure();
    }

}
