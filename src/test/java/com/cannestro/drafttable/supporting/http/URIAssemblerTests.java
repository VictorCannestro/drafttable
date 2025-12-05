package com.cannestro.drafttable.supporting.http;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;


@Test(groups = {"unit", "component"})
public class URIAssemblerTests {

    @Test
    public void passedAlongUriStringEqualsOriginalUriString() {
        String urlString = "https://raw.githubusercontent.com/VictorCannestro/drafttable/refs/heads/develop/src/test/resources/json/multiple_recipes.json";

        Assert.assertEquals(urlString, URIAssembler.passAlong(URI.create(urlString)).toString());
    }

    @Test
    public void createdUriAssemblerCanNeverEqualPassedUriAssembler() {
        String uriString = "https://raw.cooking.com/books/cookbook.json?page=0&name=eggplant#recipes";
        URIAssembler control = URIAssembler.passAlong(URI.create(uriString));
        URIAssembler assembled = URIAssembler.create()
                .baseUri("https://raw.cooking.com")
                .path("/books/cookbook.json")
                .queryParam("page","0")
                .queryParam("name", "eggplant")
                .fragment("recipes");

        Assert.assertNotEquals(control, assembled);
    }

    @Test
    public void modifiedUriEqualsOriginalWhereUnchanged() {
        URI foodUri = URIAssembler.create()
                .baseUri("https://raw.cooking.com")
                .path("/books/cookbook.json")
                .queryParam("page","0")
                .queryParam("name", "eggplant")
                .fragment("recipes")
                .toURI();

        URI modifiedFoodUri = URIAssembler.modifyExisting(foodUri)
                .queryParam("foo", "bah ruh")
                .fragment("slug")
                .toURI();

        Assert.assertEquals(foodUri.getScheme(), modifiedFoodUri.getScheme());
        Assert.assertEquals(foodUri.getRawAuthority(), modifiedFoodUri.getRawAuthority());
        Assert.assertEquals(foodUri.getRawPath(), modifiedFoodUri.getRawPath());

        Assert.assertNotEquals(foodUri.getRawQuery(), modifiedFoodUri.getRawQuery());
        Assert.assertNotEquals(foodUri.getFragment(), modifiedFoodUri.getFragment());

        Assert.assertEquals(modifiedFoodUri.getFragment(), "slug");
        Assert.assertTrue(modifiedFoodUri.getRawQuery().contains("foo=bah+ruh"));
    }

}
