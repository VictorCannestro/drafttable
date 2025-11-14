package com.cannestro.drafttable.helper;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.supporting.utils.MapBuilder;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;


public record Recipe(int id,
                     String name,
                     List<String> ingredients,
                     List<String> instructions,
                     int prepTimeMinutes,
                     int cookTimeMinutes,
                     int servings,
                     String difficulty,
                     String cuisine,
                     int caloriesPerServing,
                     List<String> tags,
                     int userId,
                     String image,
                     double rating,
                     int reviewCount,
                     List<String> mealType) implements Mappable {

    @Override
    public Map<@NonNull String, ?> asMap() {
        return new MapBuilder()
                .entry("id", id)
                .entry("name", name)
                .entry("ingredients", ingredients)
                .entry("instructions", instructions)
                .entry("prepTimeMinutes", prepTimeMinutes)
                .entry("cookTimeMinutes", cookTimeMinutes)
                .entry("servings", servings)
                .entry("difficulty", difficulty)
                .entry("cuisine", cuisine)
                .entry("caloriesPerServing", caloriesPerServing)
                .entry("tags", tags)
                .entry("userId", userId)
                .entry("image", image)
                .entry("rating", rating)
                .entry("reviewCount", reviewCount)
                .entry("mealType", mealType)
                .asMap();
    }
}
