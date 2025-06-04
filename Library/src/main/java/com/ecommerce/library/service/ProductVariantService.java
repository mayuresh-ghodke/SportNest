package com.ecommerce.library.service;

import com.ecommerce.library.model.ProductVariant;

public interface ProductVariantService {

    ProductVariant getProductVariantById(int id);
    ProductVariant saveProductVariant(ProductVariant productVariant);
    ProductVariant updateProductVariantById(int id, ProductVariant productVariant);
    boolean deleteProductProductById(int id);
}
