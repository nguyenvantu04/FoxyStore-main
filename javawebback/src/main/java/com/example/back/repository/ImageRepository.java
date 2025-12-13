package com.example.back.repository;

import com.example.back.entity.Image;
import com.example.back.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository <Image, Integer>{
    void deleteByProduct(Product product);
}
