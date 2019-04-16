package com.yc.mall.common;

import com.google.common.collect.Sets;

import java.util.Set;

public class Const {
    public final static String CURRENT_USER = "currentUser";
    public final static String EMAIL = "email";
    public final static String USERNAME = "username";

    //接口中的变量本来就是静态的可以直接调用，增加一个接口的意思相当于分组
    //角色常量
    public interface Role{
        int ROLE_CUSTOMER = 0;  //普通用户
        int ROLE_ADMIN = 1;     //管理员
    }
    //购物车常量
    public interface Cart{
        int CHECKED = 1;      //选中状态
        int UN_CHECKED = 0;   //未选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    //产品列表的顺序常量
    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc", "price_asc");
    }

    //产品状态：下架、在线、删除等等
    public enum ProductStatusEnum{
        ON_SALE("在线", 1);

        private String value;
        private int code;

        ProductStatusEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }
        public String getValue() {
            return value;
        }
        public int getCode() {
            return code;
        }
    }

    //订单状态
    public enum OrderStatusEnum{
        CANCELED("已取消",0),
        NO_PAY("未支付",10),
        PAID("已支付",20),
        SHIPPED("已发货",40),
        ORDER_SUCCESS("订单已完成",50),
        ORDER_CLOSE("订单关闭",60)
        ;

        private String value;
        private int code;

        OrderStatusEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }
        public String getValue() {
            return value;
        }
        public int getCode() {
            return code;
        }

        public static OrderStatusEnum codeOf(int code){
            for(OrderStatusEnum orderStatusEnum : values()){
                if(orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的支付方式的类型");
        }
    }

    //Alipay的回调常量
    public interface AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    //支付平台
    public enum PayPlatformEnum{
        Alipay("支付宝",1),
        ;

        private String value;
        private int code;

        PayPlatformEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }
        public String getValue() {
            return value;
        }
        public int getCode() {
            return code;
        }
    }
    //支付方式
    public enum PaymentTypeEnum{
        ONLINE_PAY("在线支付",1),
        ;

        private String value;
        private int code;

        PaymentTypeEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }
        public String getValue() {
            return value;
        }
        public int getCode() {
            return code;
        }
        public static PaymentTypeEnum codeOf(int code){
            for(PaymentTypeEnum paymentTypeEnum : values()){
                if(paymentTypeEnum.getCode() == code){
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到对应的支付方式的类型");
        }
    }
}













