package com.cannestro.drafttable.core.outbound;

import org.jspecify.annotations.NonNull;

import java.util.List;


public record JsonOutputFormat(@NonNull String label, @NonNull List<?> values) {}
