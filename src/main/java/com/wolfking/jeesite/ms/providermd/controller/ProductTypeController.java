package com.wolfking.jeesite.ms.providermd.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.MDProductType;
import com.kkl.kklplus.entity.md.MDProductTypeItem;
import com.kkl.kklplus.entity.md.dto.MDProductSpecDto;
import com.kkl.kklplus.entity.md.dto.MDProductSpecItemDto;
import com.kkl.kklplus.entity.md.dto.MDProductSpecTypeDto;
import com.kkl.kklplus.entity.md.dto.MDProductTypeDto;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSProductSpecTypeService;
import com.wolfking.jeesite.ms.providermd.service.MSProductTypeItemService;
import com.wolfking.jeesite.ms.providermd.service.MSProductTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 客户产品型号服务
 */
@Controller
@Slf4j
@RequestMapping(value = "${adminPath}/provider/md/productType")
public class ProductTypeController extends BaseController {

    @Autowired
    private MSProductTypeService msProductTypeService;

    @Autowired
    private MSProductTypeItemService msProductTypeItemService;

    @Autowired
    private MSProductSpecTypeService msProductSpecTypeService;

    /**
     * 分页查询
     * @param mdProductTypeDto
     * @return
     */
    @RequiresPermissions("md:producttype:view")
    @RequestMapping(value = {"findList", ""})
    public String findList(MDProductTypeDto mdProductTypeDto, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<MDProductTypeDto> page = new Page<>(request, response);
        page = msProductTypeService.findListForPage(page,mdProductTypeDto);
        if(page.getList() !=null && page.getList().size()>0){
            String itemNames = "";
            for(MDProductTypeDto productTypeDto:page.getList()){
                 if(productTypeDto.getProductTypeItemList() !=null && productTypeDto.getProductTypeItemList().size()>0){
                     List<String> itemNameList = productTypeDto.getProductTypeItemList().stream().map(MDProductTypeItem::getName).collect(Collectors.toList());
                     itemNames = StringUtils.join(itemNameList, ",");
                     productTypeDto.setItemNames(itemNames);
                 }
            }
        }
        model.addAttribute("mdProductTypeDto",mdProductTypeDto);
        model.addAttribute("page", page);
        return "modules/providermd/productTypeList";
    }

    @RequiresPermissions("md:producttype:edit")
    @RequestMapping(value = "form")
    public String form(MDProductTypeDto productTypeDto, Model model) {
        if(productTypeDto.getId() !=null && productTypeDto.getId()>0){
            productTypeDto = msProductTypeService.getById(productTypeDto.getId());
            if(productTypeDto == null){
                productTypeDto = new MDProductTypeDto();
            }else{
                if(productTypeDto.getProductTypeItemList() !=null && productTypeDto.getProductTypeItemList().size()>0){
                    String itemNames = "";
                    List<String> itemNameList = productTypeDto.getProductTypeItemList().stream().map(MDProductTypeItem::getName).collect(Collectors.toList());
                    itemNames = StringUtils.join(itemNameList, ",");
                    productTypeDto.setItemNames(itemNames);
                }
            }
        }
        model.addAttribute("productTypeDto", productTypeDto);
        return "modules/providermd/productTypeForm";
    }

    /**
     * 验证名称是否存在
     * @param id
     * @param name
     * @return
     */
    @ResponseBody
    @RequestMapping(value={"checkName"})
    public String checkName(Long id,String name) {
        try {
            String result = msProductTypeService.checkName(id,name);
            return result.equalsIgnoreCase("true") ? result : "产品分类名称已存在";
        } catch (Exception ex) {
            log.error("error,", ex);
            return "false";
        }
    }


    /**
     * 保存数据(添加或修改)
     * @param mdProductTypeDto
     * @return
     */
    @RequiresPermissions("md:producttype:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity save(MDProductTypeDto mdProductTypeDto) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if(StringUtils.isBlank(mdProductTypeDto.getItemNames())){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("请输入二级分类");
            return ajaxJsonEntity;
        }
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            mdProductTypeDto.setCreateById(user.getId());
            mdProductTypeDto.setUpdateById(user.getId());
            try {
                msProductTypeService.save(mdProductTypeDto);
                ajaxJsonEntity.setSuccess(true);
                ajaxJsonEntity.setMessage("保存成功");
            }catch (Exception e){
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage(e.getMessage());
            }
        } else {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在");
        }
        return ajaxJsonEntity;
    }


    /**
     * 删除
     * @param mdProductTypeDto
     * @return
     */
    @RequiresPermissions("md:producttype:edit")
    @RequestMapping(value = "delete")
    public String delete(MDProductTypeDto mdProductTypeDto, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if(user !=null && user.getId()!=null && user.getId()>0){
            try {
                mdProductTypeDto.setUpdateById(user.getId());
                msProductTypeService.delete(mdProductTypeDto);
                addMessage(redirectAttributes, "删除成功" );
            }catch (Exception e){
                addMessage(redirectAttributes, e.getMessage());
            }
        }else{
            addMessage(redirectAttributes, "删除失败.失败原因:当前用户不存在" );
        }
        return "redirect:" + adminPath + "/provider/md/productType/findList?repage";
    }

    /**
     * 根据品类id获取
     * @param productCategoryId
     * */
    @RequestMapping("ajax/findListByCategoryId")
    @ResponseBody
    public AjaxJsonEntity findListByCategoryId(Long productCategoryId){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        try {
            List<MDProductType> list = msProductTypeService.findListByCategoryId(productCategoryId);
            ajaxJsonEntity.setSuccess(true);
            ajaxJsonEntity.setData(list);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }


    /**
     * 根据品类id获取
     * @param productTypeId
     * */
    @RequestMapping("ajax/findListByProductTypeId")
    @ResponseBody
    public AjaxJsonEntity findListByProductTypeId(Long productTypeId){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        try {
            List<MDProductTypeItem> list = msProductTypeItemService.findListByProductTypeId(productTypeId);
            ajaxJsonEntity.setSuccess(true);
            ajaxJsonEntity.setData(list);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 根据产品一级分类和二级分类获取产品规格以及参数
     * @param productTypeId
     * @param productTypeItemId
     * */
    @RequestMapping("ajax/findListByTypeIdAndItemId")
    @ResponseBody
    public AjaxJsonEntity findListByTypeIdAndItemId(Long productTypeId,Long productTypeItemId){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        try {
            List<MDProductSpecDto> list = msProductSpecTypeService.findListByTypeIdAndItemId(productTypeId,productTypeItemId);
            ajaxJsonEntity.setSuccess(true);
            ajaxJsonEntity.setData(list);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }
}
