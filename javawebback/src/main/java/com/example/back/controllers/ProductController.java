package com.example.back.controllers;

import com.example.back.dto.request.Products.ProductRequestDTO;
import com.example.back.dto.request.Products.ProductUpdateRequest;
import com.example.back.dto.response.APIResponse;
import com.example.back.dto.response.Product.*;
import com.example.back.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.key}/")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductController {
    ProductService productService;

    @GetMapping("products/home")
    public APIResponse<HomeResponseDTO> products(){
        return APIResponse.<HomeResponseDTO>builder()
                .result(productService.getProductHome())
                .build();
    }

    @GetMapping("product/{id}")
    public APIResponse<ProductDetail> getProductDetail(@PathVariable Integer id){
        System.out.println("productId "+id);
        return APIResponse.<ProductDetail>builder()
                .result(productService.getProductDetail(id))
                .build();
    }
    @GetMapping("product/{id}/related")
    public APIResponse<List<ProductHome>> getRelatedProduct(@PathVariable Integer id){
        return APIResponse.<List<ProductHome>>builder()
                .result(productService.getRelatedProduct(id))
                .build();
    }
    @GetMapping("products")
    public APIResponse<List<ProductHome>> getAllProduct(
            @RequestParam(defaultValue = "0") int page
    ){
        return  APIResponse.<List<ProductHome>>builder()
                .result(productService.getAllProduct(page))
                .build();
    }
    @GetMapping("category/{id}")
    public APIResponse<List<ProductHome>> getByCategory(@PathVariable Integer id ,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(required = false, defaultValue = "newest") String sort
    ){
        return APIResponse.<List<ProductHome>>builder()
                .result(productService.getByCategoryId(id,page,sort))
                .build();
    }
    //admin
    @GetMapping("products/tops")
    public APIResponse<List<TopProductDTO>> getFeaturedProducts() {
        return APIResponse.<List<TopProductDTO>>builder()
                .result(productService.getTopRatedProducts())
                .build();
    }
    @GetMapping("products/search")
    public APIResponse<Page<ProductInfoDTO>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductInfoDTO> result = productService.searchProducts(name, categoryId, minPrice, maxPrice, pageable);
        if (result.isEmpty()) {
            return APIResponse.<Page<ProductInfoDTO>>builder()
                    .code(404)
                    .build();
        }
        return APIResponse.<Page<ProductInfoDTO>>builder()
                .code(1000)
                .result(result)
                .build();
    }
    // add
    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public APIResponse<String> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart("images") List<MultipartFile> images
    ) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ProductRequestDTO request = objectMapper.readValue(productJson, ProductRequestDTO.class);

            productService.createProduct(request, images);
            return APIResponse.success("Product created successfully");

        } catch (Exception e) {
            return APIResponse.error("Error: " + e.getMessage());
        }
    }
    //update
    @PutMapping(value = "products/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public APIResponse<String> updateProduct(
            @PathVariable Integer productId,
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> newImages
    ) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ProductUpdateRequest request = mapper.readValue(productJson, ProductUpdateRequest.class);

            productService.updateProduct(productId, request, newImages);
            return APIResponse.success("Product updated successfully");

        } catch (Exception e) {
            return APIResponse.error("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("products/{id}")
    public APIResponse<String> deleteProduct(@PathVariable Integer id) {
        try {
            productService.deleteProduct(id);
            return APIResponse.success("Product deleted successfully");
        } catch (Exception e) {
            return APIResponse.error("Error: " + e.getMessage());
        }
    }
}
