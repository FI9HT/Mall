package com.yc.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yc.mall.common.ServerResponse;
import com.yc.mall.dao.ShippingMapper;
import com.yc.mall.pojo.Shipping;
import com.yc.mall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 这里要着重注意横向越权的问题
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * 增加收货地址
     * @param userId
     * @param shipping
     * @return
     */
    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int resultCount = shippingMapper.insert(shipping);
        if(resultCount > 0){
           Map<String, Integer> result = Maps.newHashMap();
           result.put("shippingId", shipping.getId());
           return ServerResponse.createBySuccess("新建地址成功", result);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    /**
     * 删除收货地址
     * @param userId
     * @param shippingId
     * @return
     */
    public ServerResponse delete(Integer userId, Integer shippingId){
        int resultCount = shippingMapper.deleteByShippingIdUserId(userId, shippingId);
        if(resultCount > 0){
            return ServerResponse.createBySuccess("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    /**
     * 更新
     * @param userId
     * @param shippingId
     * @return
     */
    public ServerResponse update(Integer userId, Shipping shippingId){
        shippingId.setUserId(userId);
        int resultCount = shippingMapper.updateByShipping(shippingId);
        if(resultCount > 0){
            return ServerResponse.createBySuccess("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    /**
     * 查找地址
     * @param userId
     * @param shippingId
     * @return
     */
    public ServerResponse<Shipping> select(Integer userId, Integer shippingId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(shippingId, userId);
        if(shipping == null){
            return ServerResponse.createByErrorMessage("无法查询到该地址");
        }
        return ServerResponse.createBySuccess("地址查询成功", shipping);
    }
    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        //因为没有创建新的shipping对象， 所以不用 pageInfo.setList(...)
        PageInfo pageInfo = new PageInfo(shippingList);
        return  ServerResponse.createBySuccess(pageInfo);
    }
}
