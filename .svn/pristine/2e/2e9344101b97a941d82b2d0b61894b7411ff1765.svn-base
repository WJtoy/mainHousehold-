package com.wolfking.jeesite.ms.tmall.md.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.enums.YesNo;
import com.wolfking.jeesite.ms.tmall.md.entity.ServicePointProvinceBatch;
import com.wolfking.jeesite.ms.tmall.md.service.B2BServicePointService;
import com.wolfking.jeesite.ms.tmall.md.service.MdB2bTmallService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/tmall/md/servicepoint")
public class B2BServicePointController extends BaseController {

    @Autowired
    private B2BServicePointService b2BServicePointService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private MicroServicesProperties msProperties;

    //region 网点资料的批量操作

    @RequestMapping(value = "servicePointBatchProcessList")
    public String servicePointBatchProcessList(ServicePointProvinceBatch servicePointProvinceBatch,
                                               Model model, HttpServletRequest request, HttpServletResponse response) {
        List<ServicePointProvinceBatch> list = Lists.newArrayList();
        if (msProperties.getTmall().getEnabled()) {
            Long provinceId = null;
            if (servicePointProvinceBatch != null && servicePointProvinceBatch.getProvince() != null && servicePointProvinceBatch.getProvince().getId() != null) {
                provinceId = servicePointProvinceBatch.getProvince().getId();
            }
            list = b2BServicePointService.getServicePointProvinceBatchList(provinceId);
        }
        else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("list", list);
        model.addAttribute("servicePointProvinceBatch", servicePointProvinceBatch);
        return "modules/tmall/md/b2bServicePointBatchProcessList";
    }

    @ResponseBody
    @RequestMapping(value = "servicePointBatchUpload")
    public AjaxJsonEntity servicePointBatchUpload(@RequestParam("cityId") Long cityId) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        List<Integer> failureCountList = b2BServicePointService.uploadServicePointsToTmallByCityId(cityId);
        if (failureCountList.get(0) + failureCountList.get(1) + failureCountList.get(2) + failureCountList.get(3) == 0) {
            result.setMessage("网点批量上传操作成功");
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("网点批量上传失败：");
            if (failureCountList.get(0) > 0) {
                stringBuilder.append(failureCountList.get(0) + "个网点的网点基础资料、");
            }
            if (failureCountList.get(1) > 0) {
                stringBuilder.append(failureCountList.get(1) + "个网点的网点覆盖服务、");
            }
            if (failureCountList.get(2) > 0) {
                stringBuilder.append(failureCountList.get(2) + "个网点的网点容量、");
            }
            if (failureCountList.get(3) > 0) {
                stringBuilder.append(failureCountList.get(3) + "个安维师傅信息");
            }
            result.setMessage(stringBuilder.toString());
            result.setSuccess(false);
        }

        return result;
    }

    @ResponseBody
    @RequestMapping(value = "servicePointProvinceBatchUpload")
    public AjaxJsonEntity servicePointProvinceBatchUpload(@RequestParam("provinceId") Long provinceId) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        List<Area> cityList = areaService.findListByType(Area.TYPE_VALUE_CITY);
        cityList = cityList.stream().filter(i -> i.getParentId()==provinceId).collect(Collectors.toList());
        for (Area city : cityList) {
            List<Integer> failureCountList = b2BServicePointService.uploadServicePointsToTmallByCityId(city.getId());
        }
        result.setMessage("网点批量上传操作成功");
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "servicePointBatchDelete")
    public AjaxJsonEntity servicePointBatchDelete(@RequestParam("cityId") Long cityId) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        List<Integer> failureCountList = b2BServicePointService.deleteServicePointsToTmallByCityId(cityId);
        if (failureCountList.get(0) + failureCountList.get(1) + failureCountList.get(2) + failureCountList.get(3) == 0) {
            result.setMessage("网点批量删除操作成功");
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("网点批量删除失败：");
            if (failureCountList.get(0) > 0) {
                stringBuilder.append(failureCountList.get(0) + "个网点的网点基础资料、");
            }
            if (failureCountList.get(1) > 0) {
                stringBuilder.append(failureCountList.get(1) + "个网点的网点覆盖服务、");
            }
            if (failureCountList.get(2) > 0) {
                stringBuilder.append(failureCountList.get(2) + "个网点的网点容量、");
            }
            if (failureCountList.get(3) > 0) {
                stringBuilder.append(failureCountList.get(3) + "个安维师傅信息");
            }
            result.setMessage(stringBuilder.toString());
            result.setSuccess(false);
        }

        return result;
    }

    //endregion 网点资料的批量操作
}
