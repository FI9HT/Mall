package com.yc.mall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yc.mall.common.Const;
import com.yc.mall.common.ServerResponse;
import com.yc.mall.dao.*;
import com.yc.mall.pojo.*;
import com.yc.mall.service.IOrderService;
import com.yc.mall.util.BigDecimalUtil;
import com.yc.mall.util.DateTimeUtil;
import com.yc.mall.util.FTPUtil;
import com.yc.mall.util.PropertiesUtil;
import com.yc.mall.vo.OrderItemVo;
import com.yc.mall.vo.OrderProductVo;
import com.yc.mall.vo.OrderVo;
import com.yc.mall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.spi.ResolveResult;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;

@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    static AlipayTradeService tradeService;

    /**
     * 获取订单列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, userId);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
    //组装返回给前段的订单列表OrderVo
    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for(Order order : orderList){
            List<OrderItem> orderItemList = Lists.newArrayList();
            if(userId == null){
                //管理员查询不需要id
                orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());
            }else{
                orderItemList = orderItemMapper.selectByOrderNoUserId(userId, order.getOrderNo());
            }
            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    /**
     * 获取订单详情
     * @param userId
     * @param orderNo
     * @return
     */
    public ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoUserId(userId, orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }


    /**
     * 获取购物车中已经勾选的产品信息
     * @param userId
     * @return
     */
    public ServerResponse getOrderCartProduct(Integer userId){
        OrderProductVo orderProductVo = new OrderProductVo();

        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        ServerResponse serverResponse = this.getCartOrderItem(userId, cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getCurrentUnitPrice().doubleValue());
            orderItemVoList.add(assemleOrderItemVo(orderItem));
        }
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return ServerResponse.createBySuccess(orderProductVo);
    }
    /**
     * 取消订单
     * @param userId
     * @param orderNo
     * @return
     */
    public ServerResponse cancel(Integer userId, Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if(orderNo == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if(order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()){
            return ServerResponse.createByErrorMessage("订单已经付款,无法取消");
        }
        Order updateOrder = new Order();
        //这里setId 是为了插入的时候找到order,真正要更新的只是Status而已
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());

        int resultCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if(resultCount > 0){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    /**
     * 创建订单
     * @param userId
     * @param shippingId
     * @return
     */
    //生成订单并且返回给前段相关数据
    public ServerResponse createOrder(Integer userId, Integer shippingId){

        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        //生成子订单！！！
        ServerResponse serverResponse = this.getCartOrderItem(userId, cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);

        //生成订单
        Order order = this.assembleOrder(userId, shippingId, payment);
        if(order == null) {
            return serverResponse.createByErrorMessage("生成订单失败");
        }
        if(CollectionUtils.isEmpty(orderItemList)){
            return ServerResponse.createByErrorMessage("购物车为空");    //为空也要插入order吗？？？
        }
        //在插入之前要把订单号的值添加到OrderItem里面
        for(OrderItem orderItem : orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }
        //mybatis insert
        orderItemMapper.batchInsert(orderItemList);

        //orderItem 子订单生成成功，要修改数据库产品的库存
        this.reduceProductStock(orderItemList);

        //清空购物车
        this.clearCart(cartList);

        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);


    }
    //生成返回给前段的订单，包括订单概要（总价、商品数量）以及订单明细
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(orderVo.getPaymentType()).getValue());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(orderVo.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(orderVo.getShippingId());
        if(shipping != null){
            orderVo.setReceiveName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for(OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = assemleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }

        orderVo.setOrderItemVoList(orderItemVoList);

        return orderVo;

    }

    //封装OrderVo要显示的明细对象OrderItemVo
    private OrderItemVo assemleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return  orderItemVo;
    }
    //封装收货对象
    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }

    private void clearCart(List<Cart> cartList){
        for(Cart cart : cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    private void reduceProductStock(List<OrderItem> orderItemList){
        for(OrderItem orderItem : orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            int resutlCount = productMapper.updateByPrimaryKeySelective(product);
        }
    }
    //生成订单
    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment){
        Order order = new Order();
        long orderNo = this.generateOrderNo();
        order.setOrderNo(orderNo);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);        //运费，待拓展
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPayment(payment);

        order.setUserId(userId);
        order.setShippingId(shippingId);
        //发货时间、付款时间在用户付完款以及商家发货的时候再进行更新
        int resultCount = orderMapper.insert(order);
        if(resultCount > 0){
            return order;
        }
        return null;
    }

    private long generateOrderNo(){
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }

    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    private ServerResponse<List<OrderItem>> getCartOrderItem(Integer userId, List<Cart> cartList){

        List<OrderItem> orderItemList = Lists.newArrayList();
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        //将Cart -> OrderItem
        //校验数据，包括是否上架，库存数量,
        for(Cart cartItem : cartList){
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
                return ServerResponse.createByErrorMessage("产品 " + product.getName() + " 没有上架");
            }
            //感觉好像没有必要检验数量，因为在购物车中对产品数量的操作已经有了限制，是符合要求的
            if(cartItem.getQuantity() > product.getStock()){
                return ServerResponse.createByErrorMessage("产品 " + product.getName() + " 库存不足");
            }

            orderItem.setUserId(userId);
            //orderItem.setOrderNo();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setCurrentUnitPrice(product.getPrice());  //记录一下下单的时候的单价
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartItem.getQuantity()));

            orderItemList.add(orderItem);

        }
        return ServerResponse.createBySuccess(orderItemList);

    }





    /**
     * 支付功能，向支付宝发送相关数据
     * @param orderNo
     * @param userId
     * @param path
     * @return
     */
    public ServerResponse pay(Long orderNo, Integer userId, String path) {
        Map<String, String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));

        //调用支付宝接口的相关参数

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = "订单号：" + order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = "SwordMall 门店扫码消费";

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = "订单" + outTradeNo + " 购买商品共" + totalAmount + "元";

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "60m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        //用自己的Orderitem填充GoodsDetail !!!需要注意的是价格为分
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoUserId(userId, orderNo);
        for (OrderItem orderItem : orderItemList) {
            GoodsDetail goodsDetail = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName().toString(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goodsDetail);
        }

        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        //GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "xxx小面包", 1000, 1);
        // 创建好一个商品后添加至商品明细列表
        //goodsDetailList.add(goods1);

        // 继续创建并添加第一条商品信息，用户购买的产品为“黑人牙刷”，单价为5.00元，购买了两件
        //GoodsDetail goods2 = GoodsDetail.newInstance("goods_id002", "xxx牙刷", 500, 2);
        //goodsDetailList.add(goods2);

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        Configs.init("zfbinfo.properties");

        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if(!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 需要修改为运行机器上的路径
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png", response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File target = new File(path, qrFileName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(target));
                } catch (IOException e) {
                    logger.error("上传二维码失败", e);
                }
                logger.info("qrPath:" + qrPath);

                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + qrFileName;
                resultMap.put("qrUrl", qrUrl);

                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }

    }
    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }
    //回调函数
    public ServerResponse aliCallBack(Map<String, String> params) {
        //params name->"out_trade_no" value->"订单号：123456789" 注意数字前面是有订单号：这几个字的！！
        String temp = params.get("out_trade_no");
        Long orderNo = Long.parseLong(temp.substring(temp.indexOf("：")+1, temp.length()));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            logger.info("order == null");
            return ServerResponse.createByErrorMessage("非Sword Mall订单，回调忽略");
        }
        //查询本地数据得知order订单状态已经付款完毕，因此不用理会该请求
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            logger.info("order == rest");
            return ServerResponse.createBySuccess("支付宝重复调用");
        }
        //本地order状态未完成，判断请求中order的status，修改本地
        if(tradeStatus.equals(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS)){
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        logger.info("XXXXXXXXXXXX");
        //支付宝只会回调一次吗？因此不管有没有支付都记录在数据库中
        PayInfo payInfo = new PayInfo();
        payInfo.setOrderNo(orderNo);
        payInfo.setPayPlatform(Const.PayPlatformEnum.Alipay.getCode());
        payInfo.setUserId(order.getUserId());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    //backend

    /**
     * 查看所有订单
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> manageList(int pageNum, int pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, null);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 查看订单详情
     * @param orderNo
     * @return
     */
    public ServerResponse<OrderVo> manageDetail(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return  ServerResponse.createByErrorMessage("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return  ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 查找订单
     * @param orderNo
     * @return
     */
    public ServerResponse<OrderVo> manageSearch(Long orderNo, int pageNum, int pageSize){
        PageHelper.startPage(pageNum, pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return  ServerResponse.createByErrorMessage("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
        pageInfo.setList(Lists.newArrayList(orderVo));
        return  ServerResponse.createBySuccess(orderVo);
    }

    public ServerResponse<String> manageSendGoods(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order != null){
            if(order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){
                order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                return ServerResponse.createBySuccess("发货成功");
            }
        }
        return ServerResponse.createBySuccess("发货成功");
    }


}



















