package com.itzx.electronics.biz.imp;

import com.github.pagehelper.PageHelper;
import com.itzx.electronics.biz.ProductBiz;
import com.itzx.electronics.entity.Product;
import com.itzx.electronics.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductBizImpl implements ProductBiz {
    @Autowired
    private ProductMapper productMapper;


    //查询所有商品，分页
    @Override
    public List<Product> findProduct(int index, int size) {
        PageHelper.startPage(index, size);
        return productMapper.findProduct();
    }

    //模糊查询
    @Override
    public List<Product> findProductMo(String pname, int index, int size) {
        PageHelper.startPage(index, size);
        return productMapper.findProductMo(pname);
    }

    @Override
    public Product findProductById(int id) {
        return productMapper.findProductById(id);
    }

    @Override
    public boolean delProduct(int id) {
        return productMapper.delProduct(id) > 0;
    }

    @Override
    public boolean updateProduct(Product product) {
        return productMapper.updataProduct(product) > 0;

    }

    @Override
    public boolean addProduct(Product product) {
        int result = productMapper.addProduct(product);
        return result > 0;
    }
}