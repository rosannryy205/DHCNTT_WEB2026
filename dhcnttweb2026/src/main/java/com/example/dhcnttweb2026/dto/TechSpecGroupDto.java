package com.example.dhcnttweb2026.dto;

import java.util.Map;

public record TechSpecGroupDto(
        String groupName,
        Map<String, String> specs
) {}
