package com.itzx.electronics.controller;

import com.itzx.electronics.biz.DetailBiz;
import com.itzx.electronics.biz.ProductBiz;
import com.itzx.electronics.biz.imp.DetailBizImpl;
import com.itzx.electronics.entity.Detail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("detail")
public class DetailController {
    @Autowired
    private DetailBiz detailBiz;
    private ProductBiz productBiz;
    @RequestMapping("/findDetailById")
    @ResponseBody
    public Detail findDetailById(int id){
        return detailBiz.findDetailById(id);
    }

}
