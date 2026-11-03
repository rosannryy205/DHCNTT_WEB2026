package com.example.dhcnttweb2026.dto;

public record ProductStorageDto(
        String capacity,
        long startingPrice,
        String formattedStartingPrice
) {}
