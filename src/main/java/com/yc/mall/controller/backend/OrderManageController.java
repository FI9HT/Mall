package com.yc.mall.controller.backend;


import com.yc.mall.common.Const;
import com.yc.mall.common.ResponseCode;
import com.yc.mall.common.ServerResponse;
import com.yc.mall.pojo.User;
import com.yc.mall.service.IOrderService;
import com.yc.mall.service.IProductService;
import com.yc.mall.service.IUserService;
import com.yc.mall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/order")
public class OrderManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService iOrderService;


    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录请登录管理员");
        }
        if(!iUserService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作");
        }else{
            //校验成功，增加产品的业务逻辑
            return iOrderService.manageList(pageNum, pageSize);
        }
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> orderDetail(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录请登录管理员");
        }
        if(!iUserService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作");
        }else{
            //校验成功，增加产品的业务逻辑
            return iOrderService.manageDetail(orderNo);
        }
    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<OrderVo> orderSearch(HttpSession session, Long orderNo,
                                               @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录请登录管理员");
        }
        if(!iUserService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作");
        }else{
            //校验成功，增加产品的业务逻辑
            return iOrderService.manageSearch(orderNo, pageNum, pageSize);
        }
    }

    /**
     * 发货
     * 只有付款完毕的订单才能发货
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<String> orderSendGoods(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录请登录管理员");
        }
        if(!iUserService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作");
        }else{
            //校验成功，增加产品的业务逻辑
            return iOrderService.manageSendGoods(orderNo);
        }
    }
}
