package com.wolfking.jeesite.ms.providermd.controller;

import com.kkl.kklplus.entity.md.MDProductTypeItem;
import com.kkl.kklplus.entity.md.dto.MDProductSpecDto;
import com.kkl.kklplus.entity.md.dto.MDProductSpecItemDto;
import com.kkl.kklplus.entity.md.dto.MDProductSpecTypeDto;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSProductSpecService;
import com.wolfking.jeesite.ms.providermd.service.MSProductTypeItemService;
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
import java.util.stream.Collectors;


/**
 * 客户产品型号服务
 */
@Controller
@Slf4j
@RequestMapping(value = "${adminPath}/provider/md/productSpec")
public class ProductSpecController extends BaseController {

    @Autowired
    private MSProductSpecService msProductSpecService;

    @Autowired
    private MSProductTypeItemService productTypeItemService;

    /**
     * 分页查询
     * @param productSpecDto
     * @return
     */
    @RequiresPermissions("md:productspec:view")
    @RequestMapping(value = {"findList", ""})
    public String findList(MDProductSpecDto productSpecDto, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<MDProductSpecDto> page = new Page<>(request, response);
        page = msProductSpecService.findListForPage(page,productSpecDto);
        if(page.getList() !=null && page.getList().size()>0){
            String itemNames = "";
            for(MDProductSpecDto item:page.getList()){
                 if(item.getProductSpecItemDtoList() !=null && item.getProductSpecItemDtoList().size()>0){
                     List<String> itemNameList = item.getProductSpecItemDtoList().stream().map(MDProductSpecItemDto::getName).collect(Collectors.toList());
                     itemNames = StringUtils.join(itemNameList, ",");
                     item.setProductSpecItemNames(itemNames);
                 }
                 if(item.getProductSpecTypeDtoList()!=null && item.getProductSpecTypeDtoList().size()>0){
                     List<String>productTypeItem = item.getProductSpecTypeDtoList().stream().map(MDProductSpecTypeDto::getProductTypeItemName).collect(Collectors.toList());
                     item.setProductTypeItemNames(StringUtils.join(productTypeItem, ","));
                 }
            }
        }
        model.addAttribute("productSpecDto",productSpecDto);
        model.addAttribute("page", page);
        return "modules/providermd/productSpecList";
    }

    @RequiresPermissions("md:productspec:edit")
    @RequestMapping(value = "form")
    public String form(MDProductSpecDto productSpecDto, Model model) {
        if(productSpecDto.getId() !=null && productSpecDto.getId()>0){
            productSpecDto = msProductSpecService.getDtoWithSpecId(productSpecDto.getId());
            if(productSpecDto == null){
                productSpecDto = new MDProductSpecDto();
            }else{
                if(productSpecDto.getProductSpecItemDtoList() !=null && productSpecDto.getProductSpecItemDtoList().size()>0){
                    List<String> itemNameList = productSpecDto.getProductSpecItemDtoList().stream().map(MDProductSpecItemDto::getName).collect(Collectors.toList());
                    productSpecDto.setProductSpecItemNames(StringUtils.join(itemNameList, ","));
                }
                if(productSpecDto.getProductSpecTypeDtoList()!=null && productSpecDto.getProductSpecTypeDtoList().size()>0){
                    List<Long>productTypeItem = productSpecDto.getProductSpecTypeDtoList().stream().map(MDProductSpecTypeDto::getProductTypeItemId).collect(Collectors.toList());
                    productSpecDto.setProductTypeItemNames(StringUtils.join(productTypeItem, ","));
                }
            }
        }
        List<MDProductTypeItem> productTypeItems = productTypeItemService.findAllList();
        model.addAttribute("productTypeItems", productTypeItems);
        model.addAttribute("productSpecDto", productSpecDto);
        return "modules/providermd/productSpecForm";
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
            String result = msProductSpecService.checkName(id,name);
            return result.equalsIgnoreCase("true") ? result : "产品规格名称已存在";
        } catch (Exception ex) {
            log.error("error,", ex);
            return "false";
        }
    }


    /**
     * 保存数据(添加或修改)
     * @param productSpecDto
     * @return
     */
    @RequiresPermissions("md:productspec:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity save(MDProductSpecDto productSpecDto) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if(StringUtils.isBlank(productSpecDto.getProductSpecItemNames())){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("请输入二级分类");
            return ajaxJsonEntity;
        }
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            productSpecDto.setCreateById(user.getId());
            productSpecDto.setUpdateById(user.getId());
            try {
                msProductSpecService.save(productSpecDto);
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
     * @param productSpecDto
     * @return
     */
    @RequiresPermissions("md:productspec:edit")
    @RequestMapping(value = "delete")
    public String delete(MDProductSpecDto productSpecDto, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if(user !=null && user.getId()!=null && user.getId()>0){
            try {
                productSpecDto.setUpdateById(user.getId());
                msProductSpecService.delete(productSpecDto);
                addMessage(redirectAttributes, "删除成功" );
            }catch (Exception e){
                addMessage(redirectAttributes, e.getMessage());
            }
        }else{
            addMessage(redirectAttributes, "删除失败.失败原因:当前用户不存在" );
        }
        return "redirect:" + adminPath + "/provider/md/productSpec/findList?repage";
    }

}
