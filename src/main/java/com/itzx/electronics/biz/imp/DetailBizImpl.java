package com.itzx.electronics.biz.imp;

import com.itzx.electronics.biz.DetailBiz;
import com.itzx.electronics.entity.Detail;
import com.itzx.electronics.mapper.DetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DetailBizImpl implements DetailBiz {
    @Autowired
    private DetailMapper detailMapper;
    @Override
    public Detail findDetailById(int id) {
       detailMapper.findDetailById(id);
       return detailMapper.findDetailById(id);
    }
}
