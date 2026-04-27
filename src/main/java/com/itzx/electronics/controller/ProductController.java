package com.itzx.electronics.controller;

import com.itzx.electronics.biz.ProductBiz;
import com.itzx.electronics.entity.Product;
import com.itzx.electronics.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("product")
@CrossOrigin(origins = "http://localhost:8080",maxAge = 3600)
@Slf4j
public class ProductController {
    @Autowired
    private ProductBiz productBiz;
    @RequestMapping("/findProduct")
    @ResponseBody
    //查询所有商品
    public List<Product> findProduct(int index, int size){
        return productBiz.findProduct(index,size);
    }
    //模糊查询
    @RequestMapping("/findProductMo")
    @ResponseBody
    public List<Product> findProductMo(int index, int size, String pname){
        return productBiz.findProductMo(pname,index,size);
    }
    //按id查询商品
    @RequestMapping("/findProductById")
    @ResponseBody
    public Product findProductById(int id){
        return productBiz.findProductById(id);
    }
    //删除商品
    @RequestMapping("/delProduct")
    @ResponseBody
    public boolean delProduct(int id){
        return productBiz.delProduct(id);
    }
    //修改商品
    @RequestMapping("/updateProduct")
    @ResponseBody
    public boolean updateProduct(Product product){
        return productBiz.updateProduct(product);
    }
    //添加商品
    @RequestMapping("/addProduct")
    @ResponseBody
    public boolean addProduct(Product product){
        return productBiz.addProduct(product);
    }
}
