package com.yc.mall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yc.mall.common.ServerResponse;
import com.yc.mall.controller.backend.CategoryManageController;
import com.yc.mall.dao.CategoryMapper;
import com.yc.mall.pojo.Category;
import com.yc.mall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;
import javax.xml.ws.ServiceMode;
import java.util.*;


@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 增加分类
     * @param categoryName
     * @param parentId
     * @return
     */
    public ServerResponse addCategory(String categoryName, Integer parentId){
        if(parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);

        int resultCount = categoryMapper.insert(category);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("分类添加成功");
        }else{
            return ServerResponse.createByErrorMessage("分类添加失败");
        }

    }

    /**
     * 修改分类名称
     * @param categoryId
     * @param categoryName
     * @return
     */
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName){
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setId(categoryId);

        int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("更新分类名称成功");
        }
        return ServerResponse.createByErrorMessage("更新分类名称失败");

    }

    /**
     * 查找下级分类
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            //就算集合为空，也不需要返回错误的值，返回空就行了，在运行日志中记录一下
            logger.info("未找到当前分类的子类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 返回该节点以及所有子节点
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet = Sets.newHashSet();
        findChildrenCategory(categorySet, categoryId);
        List<Integer> categoryList = Lists.newArrayList();
        //category为空意味着categorySet为空
        if(categoryId != null){
            System.out.println("enter");
            for(Category categoryItem : categorySet){
                categoryList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryList);
    }
    //递归寻找下级节点
    private Set<Category> findChildrenCategory(Set<Category> categorySet, Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null){
            categorySet.add(category);
        }
        //就算categoryId为空，Mybatis查询出来的结果也不会为null
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        //遍历子类节点，当category为最下级节点时，不会再调用方法
        for(Category categoryItem : categoryList){
            findChildrenCategory(categorySet, categoryItem.getId());
        }
        return categorySet;
    }
}
