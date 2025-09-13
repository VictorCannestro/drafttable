package com.cannestro.drafttable.supporting.utils.helper;

import java.util.List;


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
                     List<String> mealType) {}
