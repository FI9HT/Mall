package com.yc.mall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.yc.mall.common.Const;
import com.yc.mall.common.ResponseCode;
import com.yc.mall.common.ServerResponse;
import com.yc.mall.pojo.Shipping;
import com.yc.mall.pojo.User;
import com.yc.mall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService iShippingService;

    /**
     * 增加收货地址
     * @param session
     * @param shipping
     * @return
     */
    //Shipping 这是springmvc中的对象传递，用该对象承载字段
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse add(HttpSession session, Shipping shipping){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return iShippingService.add(user.getId(), shipping);
    }

    /**
     * 删除地址
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping("del.do")
    @ResponseBody
    public ServerResponse del(HttpSession session, Integer shippingId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return iShippingService.delete(user.getId(), shippingId);
    }

    /**
     * 更新
     * @param session
     * @param shipping
     * @return
     */
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update(HttpSession session, Shipping shipping){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return iShippingService.update(user.getId(), shipping);
    }

    /**
     * 查询地址，只有有收货人ID情况下才能查到他的收货地址
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse<Shipping> select(HttpSession session, Integer shippingId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return iShippingService.select(user.getId(), shippingId);
    }

    /**
     * 地址列表
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(HttpSession session,
                                         @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return iShippingService.list(user.getId(), pageNum, pageSize);
    }



}
