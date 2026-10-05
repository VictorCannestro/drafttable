package com.cannestro.drafttable.supporting.http;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;


@Test(groups = {"unit", "component"})
public class URIAssemblerTests {


    @Test
    public void createdUriAssemblerCanNeverEqualPassedUriAssembler() {
        String uriString = "https://raw.cooking.com/books/cookbook.json?page=0&name=eggplant+parmesan#recipes";
        URI control = URI.create(uriString);

        URIAssembler assembled = URIAssembler.builder()
                .baseUri("https://raw.cooking.com")
                .path("/books/cookbook.json")
                .queryParam("page","0")
                .queryParam("name", "eggplant parmesan")
                .fragment("recipes")
                .build();

        Assert.assertNotEquals(assembled.toURI(), control);
    }

    @Test
    public void modifyExistingPreservesExplicitPorts() {
        URI original = URI.create("http://localhost:8080/api?x=1");

        Assert.assertEquals(URIAssembler.modifyExisting(original).build().toURI(), original);
    }

    @Test
    public void modifiedUriEqualsOriginalWhereUnchanged() {
        URI foodUri = URIAssembler.builder()
                .baseUri("https://raw.cooking.com")
                .port(8080)
                .path("/books/cookbook.json")
                .queryParam("page","0")
                .queryParam("name", "eggplant")
                .fragment("recipes")
                .build().toURI();
        URI modifiedFoodUri = URIAssembler.modifyExisting(foodUri)
                .queryParam("foo", "bah ruh")
                .fragment("slug")
                .build().toURI();

        Assert.assertEquals(foodUri.getScheme(), modifiedFoodUri.getScheme());
        Assert.assertEquals(foodUri.getRawAuthority(), modifiedFoodUri.getRawAuthority());
        Assert.assertEquals(foodUri.getRawPath(), modifiedFoodUri.getRawPath());

        Assert.assertNotEquals(foodUri.getRawQuery(), modifiedFoodUri.getRawQuery());
        Assert.assertNotEquals(foodUri.getFragment(), modifiedFoodUri.getFragment());

        Assert.assertEquals(modifiedFoodUri.getFragment(), "slug");
        Assert.assertTrue(modifiedFoodUri.getRawQuery().contains("foo=bah+ruh"));
    }

}
