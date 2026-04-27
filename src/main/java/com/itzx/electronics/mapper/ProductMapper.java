package com.itzx.electronics.mapper;

import com.itzx.electronics.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {
    //查询所有商品
    public List<Product> findProduct();
    //按商品名模糊查询商品列表
    List<Product> findProductMo(@Param("pname") String pname);
    //按id查询商品
    Product findProductById(@Param("id") int id);
    //按id删除商品，返回影响行数
    int delProduct(@Param("id") int id);

    int decreaseStockIfEnough(@Param("id") int id, @Param("qty") int qty);

    int updataProduct(Product product);

    int addProduct(Product product);
}
