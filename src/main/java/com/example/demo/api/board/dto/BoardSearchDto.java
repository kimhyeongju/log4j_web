package com.example.demo.api.board.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardSearchDto {

    @NotBlank(message = "Search keyword is required")
    private String keyword;

    private String searchType = "ALL"; // ALL, TITLE, CONTENT

    @Min(value = 0, message = "Page number must be non-negative")
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 50, message = "Page size must not exceed 50")
    private int size = 10;
}