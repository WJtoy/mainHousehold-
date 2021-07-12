package com.wolfking.jeesite.modules.sd.web;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.utils.NumberUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sd.entity.AuxiliaryMaterial;
import com.wolfking.jeesite.modules.sd.entity.AuxiliaryMaterialMaster;
import com.wolfking.jeesite.modules.sd.service.OrderAuxiliaryMaterialService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
 * 订单辅材Controller
 *
 * @author Ryan
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/orderAuxiliaryMaterial/")
@Slf4j
public class OrderAuxiliaryMaterialController extends BaseController {

    public static String CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF8 = "application/json; charset=UTF-8";

    @Autowired
    private OrderAuxiliaryMaterialService orderAuxiliaryMaterialService;

    /**
     * 工单辅材详情
     */
    @ResponseBody
    @RequestMapping(value = "/detailInfo")
    public AjaxJsonEntity orderAuxiliaryMaterialInfo(@RequestParam String orderId, @RequestParam String quarter, HttpServletResponse response) {
        response.setContentType(CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF8);
        User user = UserUtils.getUser();
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        if (user == null || user.getId() == null) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("登录已超时");
            return jsonEntity;
        }
        try {
            long orderIdLong = StringUtils.toLong(orderId);
            if (orderIdLong <= 0) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("订单参数错误");
                return jsonEntity;
            }
            AuxiliaryMaterialMaster master = orderAuxiliaryMaterialService.getOrderAuxiliaryMaterialsV2(orderIdLong, quarter);
            if(master!=null){
                if(master.getFormType()==AuxiliaryMaterialMaster.FormTypeEnum.HAS_MATERIAL_ITEM.getValue()){//有辅材项
                    if (master.getItems() != null && !master.getItems().isEmpty()) {
                        Map<String, Object> map = Maps.newHashMap();
                        List<Map<String, String>> itemMapList = Lists.newArrayList();
                        Map<String, String> itemMap;
                        for (AuxiliaryMaterial item : master.getItems()) {
                            itemMap = Maps.newHashMap();
                            itemMap.put("productName", item.getProduct() != null ? StringUtils.toString(item.getProduct().getName()) : "");
                            itemMap.put("categoryName", item.getCategory() != null ? StringUtils.toString(item.getCategory().getName()) : "");
                            itemMap.put("materialName", item.getMaterial() != null ? StringUtils.toString(item.getMaterial().getName()) : "");
                            itemMap.put("materialPrice", item.getMaterial() != null && item.getMaterial().getPrice() != null ? NumberUtils.formatNum(item.getMaterial().getPrice()) : "0.00");
                            itemMap.put("materialUnit", item.getMaterial() != null ? StringUtils.toString(item.getMaterial().getUnit()) : "");
                            itemMap.put("type", item.getMaterial().getType().toString());
                            itemMap.put("qty", item.getQty() != null ? item.getQty().toString() : "0");
                            itemMap.put("subtotal", item.getSubtotal() != null ? NumberUtils.formatNum(item.getSubtotal()) : "0.00");
                            itemMapList.add(itemMap);
                        }
                        map.put("items", itemMapList);
                        map.put("totalCharge", master.getTotal() != null ? NumberUtils.formatNum(master.getTotal()) : "0.00");
                        map.put("actualTotalCharge", master.getActualTotalCharge() != null ? NumberUtils.formatNum(master.getActualTotalCharge()) : "0.0");
                        map.put("remarks", StringUtils.toString(master.getRemarks()));
                        map.put("filePath",master.getFilePath());
                        map.put("formType",master.getFormType());
                        jsonEntity.setData(map);
                    }
                }else{
                    Map<String, Object> map = Maps.newHashMap();
                    map.put("actualTotalCharge", master.getActualTotalCharge() != null ? NumberUtils.formatNum(master.getActualTotalCharge()) : "0.0");
                    map.put("filePath",master.getFilePath());
                    map.put("remarks", StringUtils.toString(master.getRemarks()));
                    map.put("formType",master.getFormType());
                    jsonEntity.setData(map);
                }
            }
        } catch (Exception e) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    //endregion 突击
}

