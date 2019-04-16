package com.yc.mall.controller.backend;

import com.yc.mall.common.Const;
import com.yc.mall.common.ResponseCode;
import com.yc.mall.common.ServerResponse;
import com.yc.mall.pojo.User;
import com.yc.mall.service.ICategoryService;
import com.yc.mall.service.IUserService;
import net.sf.jsqlparser.schema.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 增加品类
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value="parentId",defaultValue="0")int parentId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //校验成功，下面是业务逻辑
            return iCategoryService.addCategory(categoryName, parentId);
        }else{
            //校验失败
            return ServerResponse.createByErrorMessage("无操作权限，需要管理员权限");
        }

    }

    /**
     * 修改品类名称
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, Integer categoryId, String categoryName){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorMessage("用户未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //校验成功，下面是业务逻辑
            return iCategoryService.updateCategoryName(categoryId, categoryName);
        }else{
            //校验失败
            return ServerResponse.createByErrorMessage("无操作权限，需要管理员权限");
        }

    }

    /**
     * 获取分类的下级子类
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session,@RequestParam(value="categoryId", defaultValue = "0") Integer categoryId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorMessage("用户未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //校验成功，下面是业务逻辑
            return iCategoryService.getChildrenParallelCategory(categoryId);
        }else{
            //校验失败
            return ServerResponse.createByErrorMessage("无操作权限，需要管理员权限");
        }
    }

    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session,@RequestParam(value="categoryId", defaultValue = "0") Integer categoryId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorMessage("用户未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //校验成功，下面是业务逻辑
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        }else{
            //校验失败
            return ServerResponse.createByErrorMessage("无操作权限，需要管理员权限");
        }
    }
}
