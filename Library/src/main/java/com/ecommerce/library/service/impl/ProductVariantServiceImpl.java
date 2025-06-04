package com.ecommerce.library.service.impl;

import com.ecommerce.library.exception.ResourceNotFoundException;
import com.ecommerce.library.model.ProductVariant;
import com.ecommerce.library.repository.ProductRepository;
import com.ecommerce.library.repository.ProductVariantRepository;
import com.ecommerce.library.service.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductVariantServiceImpl implements ProductVariantService {

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Override
    public ProductVariant getProductVariantById(int id) {
        return productVariantRepository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException("Product Variant not found with ID-"+id));
    }

    @Override
    public ProductVariant saveProductVariant(ProductVariant productVariant) {
        return productVariantRepository.save(productVariant);
    }

    @Override
    public ProductVariant updateProductVariantById(int id, ProductVariant productVariant) {
        ProductVariant existedProductVariant = getProductVariantById(id);
        if(existedProductVariant!=null){
            existedProductVariant.setId(id);
            existedProductVariant.setBrand(productVariant.getBrand());
            existedProductVariant.setSize(productVariant.getSize());
            existedProductVariant.setMaterial(productVariant.getMaterial());
        }
        return productVariantRepository.save(existedProductVariant);
    }

    @Override
    public boolean deleteProductProductById(int id) {
        getProductVariantById(id);
        productVariantRepository.deleteById(id);
        return true;
    }
}
