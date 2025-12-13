package com.example.back.service;

import com.example.back.dto.request.Products.ProductRequestDTO;
import com.example.back.dto.request.Products.ProductSizeRequestDTO;
import com.example.back.dto.request.Products.ProductUpdateRequest;
import com.example.back.dto.response.Product.*;
import com.example.back.entity.*;
import com.example.back.enums.ErrorCodes;
import com.example.back.exception.AppException;
import com.example.back.mapper.ProductMapper;
import com.example.back.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductService {


    ProductRepository productRepository;
    ProductMapper productMapper;
    CategoryRepository categoryRepository;
    SizeRepository sizeRepository;
    ImageRepository imageRepository;
    ProductSizeRepository productSizeRepository;
    public HomeResponseDTO getProductHome(){

        List<Product> productsNew= productRepository.findProductNew(PageRequest.of(0,10));
        List<Product> productsSale =productRepository.findProductSale(PageRequest.of(0,10));

        return HomeResponseDTO.builder()
                .productsSale(productsSale.stream().map(productMapper::toProductHomeDTO).toList())
                .productsNew(productsNew.stream().map(productMapper::toProductHomeDTO).toList())
                .build();

    }

    public ProductDetail getProductDetail(Integer id){
        Product product =productRepository.findProductWithDetail(id).orElseThrow(()->new AppException(ErrorCodes.PRODUCT_NOT_FOUND));
        return productMapper.toProductDetail(product);
    }
    public List<ProductHome> getRelatedProduct(Integer id){
        Product product =productRepository.findByProductId(id).orElseThrow(()-> new AppException(ErrorCodes.PRODUCT_NOT_FOUND));
        Category category = product.getCategory();
        List<Product> products =productRepository.findByCategory(category, PageRequest.of(0,10));
        return products.stream().map(productMapper::toProductHomeDTO).toList();

    }

    public List<ProductHome> getAllProduct(int page){
        List<Product> products =productRepository.findAllProduct(PageRequest.of(page,12));

        return products.stream().map(productMapper::toProductHomeDTO).toList();
    }
    public List<ProductHome> getByCategoryId(Integer CategoryId, int page,String sort){
        Category category= categoryRepository.findByCategoryId(CategoryId).orElseThrow(()-> new AppException(ErrorCodes.CATEGORY_NOT_FOUND));
        Sort sortOrder;
        switch (sort){
            case "bestSold":
                sortOrder=Sort.by(Sort.Direction.DESC, "soldCount");
                break;
            case "price_desc":
                sortOrder =Sort.by(Sort.Direction.DESC,"price");
                break;
            case "price_asc":
                sortOrder= Sort.by(Sort.Direction.ASC,"price");
                break;
            default:
                sortOrder = Sort.by(Sort.Direction.DESC, "createdDate");
                break;
        }
        List<Product> products =productRepository.findByCategory(category,PageRequest.of(page,12,sortOrder));
        return products.stream().map(productMapper::toProductHomeDTO).toList();
    }
    //admin
    public List<TopProductDTO> getTopRatedProducts() {
        List<Product> topProducts = productRepository.findTopRatedProducts(PageRequest.of(0, 5));

        return topProducts.stream().map(product -> {
            double avgRating = product.getReviews().stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);

            String imageUrl = product.getImages().stream()
                    .findFirst()
                    .map(Image::getImage)
                    .orElse(null);

            return TopProductDTO.builder()
                    .id(product.getProductId().longValue())
                    .name(product.getName())
                    .description(product.getDescription())
                    .image(imageUrl)
                    .price(product.getPrice())
                    .soldCount(product.getSoldCount())
                    .createdAt(product.getCreatedDate())
                    .category(product.getCategory() != null ? product.getCategory().getName() : null)
                    .averageRating(avgRating)
                    .build();
        }).collect(Collectors.toList());
    }
    // qlsp
    public Page<ProductInfoDTO> searchProducts(String name, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        List<Object[]> results = productRepository.searchProducts(name, categoryId, minPrice, maxPrice, limit, offset);
        int total = productRepository.countFilteredProducts(name, categoryId, minPrice, maxPrice);

        List<ProductInfoDTO> dtoList = results.stream().map(row -> new ProductInfoDTO(
                row[0] != null ? Arrays.asList(((String) row[0]).split(",")) : new ArrayList<>(),
                (String) row[1],
                row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO, // price
                row[3] != null ? ((Number) row[3]).intValue() : 0, // quantity
                (String) row[4],
                row[5] != null ? ((Number) row[5]).intValue() : 0, // soldCount
                row[6] != null ? Arrays.asList(((String) row[6]).split(",")) : new ArrayList<>(),
                row[7] != null ? ((Number) row[7]).intValue() : 0, // productId
                (String) row[8]
        )).toList();

        return new PageImpl<>(dtoList, pageable, total);
    }
    // create
    public void createProduct(ProductRequestDTO request, List<MultipartFile> images) throws IOException {
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .description(request.getDescription())
                .createdDate(LocalDateTime.now())
                .soldCount(0)
                .isDeleted(false)
                .build();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);

        // Lưu ảnh
        Set<Image> imageEntities = new HashSet<>();

        // Đường dẫn lưu thực tế (tuyệt đối)
        Path path = Paths.get("src/main/resources/static/images");
        if (!Files.exists(path)) Files.createDirectories(path);
        System.out.println("Processing " + images.size() + " new images for Create Product");
        System.out.println("Saving images to absolute path: " + path.toAbsolutePath());

        for (MultipartFile file : images) {
            if (!file.isEmpty()) {
                // Lấy phần mở rộng
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf(".")); 

                // Tạo tên ảnh không chứa đuôi
                String filenameNoExt = UUID.randomUUID().toString();

                // Tên file thực tế khi lưu (Lưu ý: nên lưu cả đuôi vào DB để dễ quản lý, nhưng DB hiện tại có vẻ chỉ lưu tên)
                // Tuy nhiên, để hỗ trợ nhiều định dạng, ta sẽ lưu TÊN FILE ĐẦY ĐỦ (uuid + ext) vào DB
                // Nếu DB column 'image' đủ dài.
                String savedFileName = filenameNoExt + extension;

                // Lưu file vào local
                Path savePath = path.resolve(savedFileName);
                Files.copy(file.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);

                // Lưu tên file vào DB (LƯU CẢ ĐUÔI)
                imageEntities.add(Image.builder()
                        .image(savedFileName) 
                        .product(product)
                        .build());
            }
        }

        product.setImages(imageEntities);
        productRepository.save(product);
        // Lưu size
        Set<ProductSize> productSizes = new HashSet<>();
        for (ProductSizeRequestDTO sizeReq : request.getSizes()) {
            Size size = sizeRepository.findById(sizeReq.getSizeId())
                    .orElseThrow(() -> new RuntimeException("Size not found"));
            productSizes.add(ProductSize.builder()
                    .product(product)
                    .size(size)
                    .quantity(sizeReq.getQuantity())
                    .build());
        }

        product.setProductSizes(productSizes);
        productRepository.save(product);
    }
    public void updateProduct(Integer productId, ProductUpdateRequest request, List<MultipartFile> newImages) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Cập nhật thông tin cơ bản
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setDescription(request.getDescription());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);

        // ===== Cập nhật ảnh =====
        Set<Image> currentImages = product.getImages();
        if (currentImages == null) {
            currentImages = new HashSet<>();
            product.setImages(currentImages);
        }

        // Xoá ảnh không còn giữ
        Iterator<Image> iterator = currentImages.iterator();
        while (iterator.hasNext()) {
            Image img = iterator.next();
            // Logic cũ: image trong DB không có đuôi -> phải + ".png" để check file
            // Logic mới: image trong DB CÓ THỂ có đuôi.
            // request.getOldImageNames() sẽ gửi tên đầy đủ.
            
            // Case 1: DB lưu "uuid" (cũ) -> check "uuid.png" hoặc "uuid" trong list giữ lại?
            // Case 2: DB lưu "uuid.webp" (mới) -> check "uuid.webp"
            
            String dbImageName = img.getImage();
            
            // Kiểm tra xem frontend có gửi tên này về để giữ lại không
            // Frontend gửi: ["uuid.png", "uuid.webp"]
            boolean keep = false;
            if (request.getOldImageNames() != null) {
                 if (request.getOldImageNames().contains(dbImageName)) {
                     keep = true;
                 } else if (!dbImageName.contains(".")) {
                     // Data cũ: DB chỉ lưu "uuid", nhưng frontend gửi "uuid.png"
                     if (request.getOldImageNames().contains(dbImageName + ".png")) {
                         keep = true;
                     }
                 }
            }

            if (!keep) {
                // Xóa file
                Path oldPath;
                if (dbImageName.contains(".")) {
                    oldPath = Paths.get("src/main/resources/static/images").resolve(dbImageName);
                } else {
                    oldPath = Paths.get("src/main/resources/static/images").resolve(dbImageName + ".png");
                }
                Files.deleteIfExists(oldPath);
                iterator.remove(); 
            }
        }

        // Thêm ảnh mới
        if (newImages != null) {
            Path path = Paths.get("src/main/resources/static/images");
            if (!Files.exists(path)) Files.createDirectories(path);
            System.out.println("Processing " + newImages.size() + " new images");
            System.out.println("Saving images to absolute path: " + path.toAbsolutePath());

            for (MultipartFile file : newImages) {
                if (!file.isEmpty()) {
                    String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                    String newName = UUID.randomUUID().toString();
                    
                    // Lưu cả đuôi vào DB
                    String savedFileName = newName + extension; 
                    
                    Path savePath = path.resolve(savedFileName);
                    Files.copy(file.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);

                    Image newImage = Image.builder()
                            .image(savedFileName)
                            .product(product)
                            .build();
                    currentImages.add(newImage);
                }
            }
        }

        // ===== Cập nhật size =====
        if (product.getProductSizes() == null) {
            product.setProductSizes(new HashSet<>());
        }
        product.getProductSizes().clear();
        
        for (ProductSizeRequestDTO sizeReq : request.getSizes()) {
            Size size = sizeRepository.findById(sizeReq.getSizeId())
                    .orElseThrow(() -> new RuntimeException("Size not found"));
            product.getProductSizes().add(ProductSize.builder()
                    .product(product)
                    .size(size)
                    .quantity(sizeReq.getQuantity())
                    .build());
        }
        // product.setProductSizes(productSizes); // No longer needed as we modified the collection directly

        productRepository.save(product);
    }
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setIsDeleted(true);
        productRepository.save(product);
    }



//    public List<ProductHome> searchProduct(String name){
//
//    }
}
