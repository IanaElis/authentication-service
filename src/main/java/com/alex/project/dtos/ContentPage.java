package com.alex.project.dtos;

import java.util.List;

public record ContentPage<T>(
        List<T> content,
        boolean hasNext,
        boolean hasPrevious
) {}