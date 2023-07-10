package com.ankur.productservice.service;

import com.ankur.productservice.model.ProductRequest;
import com.ankur.productservice.model.ProductResponse;

public interface ProductService {
    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(long productId);
}
