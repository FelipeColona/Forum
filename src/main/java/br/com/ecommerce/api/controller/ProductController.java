package br.com.ecommerce.api.controller;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.ecommerce.api.exception.ErrorDetails;
import br.com.ecommerce.api.exception.ResourceNotFoundException;
import br.com.ecommerce.api.model.response.ProductResponse;
import br.com.ecommerce.api.model.response.ProductWithCategoriesResponse;
import br.com.ecommerce.api.model.response.ProductWithCategoriesAndImagesResponse;
import br.com.ecommerce.api.model.response.ProductWithImagesResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.ecommerce.api.assembler.ProductAssembler;
import br.com.ecommerce.api.model.input.ProductInput;
import br.com.ecommerce.domain.model.Product;
import br.com.ecommerce.domain.repository.ProductRepository;
import br.com.ecommerce.domain.service.ProductService;
import lombok.AllArgsConstructor;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/product")
@AllArgsConstructor
public class ProductController {
   private final ProductRepository productRepository;
   private final ProductService productService;
   private final ProductAssembler productAssembler;
   
    @GetMapping
    public Page<Product> getProductPage(@PageableDefault(page = 0, size = 10, sort = "name", direction = Direction.DESC) Pageable pageable){
            return productRepository.findAll(pageable);
    }

    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable long productId){
        Product product = productRepository.findById(productId).orElseThrow(() -> {
            Set<ErrorDetails.Field> fields = new HashSet<>();
            fields.add(new ErrorDetails.Field("productId", "Id given do not match"));
            throw new ResourceNotFoundException(fields);
        });
        return productAssembler.toAnyResponse(product, ProductResponse.class);
    }

    @GetMapping("/{productId}/categories")
    public ProductWithCategoriesResponse getProductAndRetrieveCategories(@PathVariable long productId){
        Product product = productRepository.findByIdAndRetrieveCategories(productId).orElseThrow(() -> {
            Set<ErrorDetails.Field> fields = new HashSet<>();
            fields.add(new ErrorDetails.Field("productId", "Id given do not match"));
            throw new ResourceNotFoundException(fields);
        });
        return productAssembler.toAnyResponse(product, ProductWithCategoriesResponse.class);
    }

    @GetMapping("/{productId}/images")
    public ProductWithImagesResponse getProductAndRetrieveImages(@PathVariable long productId){
        Product product = productRepository.findByIdAndRetrieveImages(productId).orElseThrow(() -> {
            Set<ErrorDetails.Field> fields = new HashSet<>();
            fields.add(new ErrorDetails.Field("productId", "Id given do not match"));
            throw new ResourceNotFoundException(fields);
        });
        return productAssembler.toAnyResponse(product, ProductWithImagesResponse.class);
    }

    @GetMapping("/{productId}/categories-images")
    public ProductWithCategoriesAndImagesResponse getProductAndRetrieveCategoriesAndImages(@PathVariable long productId){
        Product product = productRepository.findByIdAndRetrieveCategoriesAndImages(productId).orElseThrow(() -> {
            Set<ErrorDetails.Field> fields = new HashSet<>();
            fields.add(new ErrorDetails.Field("productId", "Id given do not match"));
            throw new ResourceNotFoundException(fields);
        });
        return productAssembler.toAnyResponse(product, ProductWithCategoriesAndImagesResponse.class);
    }


    @GetMapping("/search")
    public List<Map<String, Object>> getProductPageByCategories(@PageableDefault(page = 0, size = 10, sort = "name", direction = Direction.DESC) Pageable pageable, @RequestParam List<String> categories) throws SQLException{
       return productService.getProductsByCategories(pageable, categories); 
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductWithCategoriesAndImagesResponse createProduct(@RequestBody @Valid ProductInput productInput){
        Product product = productAssembler.toEntity(productInput);
        Product productSaved = productService.save(product);

        return productAssembler.toAnyResponse(productSaved, ProductWithCategoriesAndImagesResponse.class);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable long productId){
        productRepository.deleteFromLinkTable(productId);
        productRepository.deleteById(productId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{productId}")
    public ProductWithCategoriesAndImagesResponse updateProduct(@PathVariable long productId, @RequestBody @Valid ProductInput productInput) throws ResourceNotFoundException {
        Product product = productAssembler.toEntity(productInput);
        Product productUpdated = productService.update(productId, product);

        return productAssembler.toAnyResponse(productUpdated, ProductWithCategoriesAndImagesResponse.class);
    }
}
