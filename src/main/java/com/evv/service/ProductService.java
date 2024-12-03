package com.evv.service;

import com.evv.database.repository.ProductRepository;
import com.evv.dto.ProductCreateEditDto;
import com.evv.dto.ProductFilter;
import com.evv.dto.ProductReadDto;
import com.evv.mapper.ProductMapper;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

import static com.evv.database.entity.QProduct.product;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    public Page<ProductReadDto> findAll(ProductFilter filter, Pageable pageable) {
        Predicate predicate = QPredicates.builder()
                .add(filter.name(), product.name::containsIgnoreCase)
                .add(filter.minCost(), product.cost::goe)
                .add(filter.maxCost(), product.cost::loe)
                .build();
        return productRepository.findAll(predicate, pageable)
                .map(productMapper::productToProductReadDto);
    }

    public Page<ProductReadDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::productToProductReadDto);
    }

    @Transactional
    public Optional<ProductReadDto> save(@Validated ProductCreateEditDto productCreateEditDto) {
        return Optional.of(productCreateEditDto)
                .map(productMapper::productCreateEditDtoToProduct)
                .map(productRepository::save)
                .map(productMapper::productToProductReadDto);
    }

    public Optional<ProductReadDto> findById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::productToProductReadDto);
    }
}
