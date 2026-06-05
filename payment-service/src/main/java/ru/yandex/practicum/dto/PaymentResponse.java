package ru.yandex.practicum.dto;

public record PaymentResponse(boolean success, long remainingBalance) {}