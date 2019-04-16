package com.yc.mall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.yc.mall.common.ServerResponse;
import com.yc.mall.dao.ProductMapper;
import com.yc.mall.pojo.Product;
import com.yc.mall.service.IProductService;
import com.yc.mall.service.impl.ProductServiceImpl;
import com.yc.mall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private IProductService iProductService;

    /**
     * 前台查看产品详情
     * @param productId
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId){
        return iProductService.getProductDetail(productId);
    }

    /**
     * 前台产品搜索
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value="keyword",required = false)String keyword,
                                         @RequestParam(value="categoryId",required = false)Integer categoryId,
                                         @RequestParam(value="pageNum",defaultValue = "1") int pageNum,
                                         @RequestParam(value="pageSize",defaultValue = "10") int pageSize,
                                         @RequestParam(value="orderBy",defaultValue = "") String orderBy){
        return iProductService.getProductByKeywordCategory(keyword, categoryId, pageNum, pageSize, orderBy);

    }
}
