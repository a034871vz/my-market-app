package ru.yandex.practicum.dto;

public record PagingDto(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext
) {}