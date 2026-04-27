package com.itzx.electronics.biz;

import com.itzx.electronics.entity.Product;

import java.util.List;

public interface ProductBiz {
    List<Product> findProduct(int index, int size);
    List<Product> findProductMo(String pname, int index, int size);
    Product findProductById(int id);
    boolean delProduct(int id);
    boolean updateProduct(Product product);
    boolean addProduct(Product product);
}
