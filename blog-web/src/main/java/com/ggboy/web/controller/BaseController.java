package com.ggboy.web.controller;

import com.ggboy.common.domain.IPage;
import com.ggboy.core.convert.CoreConvert;
import com.ggboy.core.enums.BlogOrderBy;
import com.ggboy.core.service.BlogService;
import com.ggboy.core.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class BaseController {
    @Autowired
    private BlogService blogService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String index(ModelMap param) {
        var query = new HashMap<String, Object>(2);
        query.put("orderBy", BlogOrderBy.View.desc());
        var tops = blogService.queryTop();
        var blogList = blogService.queryList(query, new IPage(1, 6));
        param.put("tops", CoreConvert.convertToBlogVOs(tops));
        param.put("blogList", CoreConvert.convertToBlogVOs(blogList));
        return "index";
    }

    @GetMapping("/info/{blogId}")
    public String info(HttpServletRequest request, @PathVariable("blogId") String blogId, ModelMap param) {
        var blogVO = CoreConvert.convertToBlogVO(blogService.queryBlogDetail(blogId));
        if (blogVO == null)
            return "redirect:/error/404.html";
        // session 获取查看列表 如果没有则阅读量+1
        var readList = (List<Object>) request.getSession().getAttribute("readList");
        readList = readList != null ? readList : new ArrayList<>(1);
        if (readList.size() == 0 || !readList.contains(blogId)) {
            readList.add(blogId);
            blogService.viewPlusOne(blogId);
            blogVO.setViewCount(blogVO.getViewCount() + 1);
            request.getSession().setAttribute("readList", readList);
        }

        // 获取分类信息
        var categoryList = CoreConvert.convertToCategoryVOs(categoryService.queryCategoryList(blogId));

        param.put("blog", blogVO);
        param.put("categoryList", categoryList);
        return "info";
    }

    @GetMapping("/list/{categoryId}")
    public String list(@PathVariable("categoryId") String categoryId, ModelMap param) {
        param.put("categoryId",categoryId);
        return "list";
    }
}