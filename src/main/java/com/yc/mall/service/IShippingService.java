package com.yc.mall.service;

import com.github.pagehelper.PageInfo;
import com.yc.mall.common.ServerResponse;
import com.yc.mall.pojo.Shipping;

public interface IShippingService {

    ServerResponse add(Integer userId, Shipping shipping);
    ServerResponse delete(Integer userId, Integer shippingId);
    ServerResponse update(Integer userId, Shipping shippingId);
    ServerResponse<Shipping> select(Integer userId, Integer shippingId);
    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);


}
