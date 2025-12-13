package com.example.back.dto.response.Product;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductInfoDTO {
    List<String> images;
    String name;
    BigDecimal price;
    int quantity;
    String category;
    int soldCount;
    List<String> sizes;
    Integer id;
    String description;
}
