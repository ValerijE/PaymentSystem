package com.evv.mapper;

import com.evv.database.entity.Product;
import com.evv.dto.ProductCreateEditDto;
import com.evv.dto.ProductReadDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    ProductReadDto productToProductReadDto(Product source);

    Product productReadDtoToProduct(ProductReadDto source);

    Product productCreateEditDtoToProduct(ProductCreateEditDto source);
}
