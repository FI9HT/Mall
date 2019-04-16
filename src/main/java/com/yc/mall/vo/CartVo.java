package com.yc.mall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 一个用户中所有商品的集合，还包括是否勾选，总价等等
 */
public class CartVo{

    private List<CartProductVo> cartProductVoList;
    private BigDecimal cartTotalPrice;
    private Boolean allChecked;//是否已经都勾选
    private String imageHost;

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public Boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(Boolean allChecked) {
        this.allChecked = allChecked;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
