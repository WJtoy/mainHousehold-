package com.wolfking.jeesite.modules.md.web;

import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.PlanRadius;
import com.wolfking.jeesite.modules.md.entity.ServicePointStation;
import com.wolfking.jeesite.modules.md.service.PlanRadiusService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/md/planradius")
public class PlanRadiusController extends BaseController {

    @Autowired
    private PlanRadiusService planRadiusService;

    @Autowired
    private AreaService areaService;

    @RequiresPermissions("md:planradius:view")
    @RequestMapping(value = {""})
    public String index(Model model) {
        return "modules/md/areaIndex";
    }

    @RequiresPermissions(value = "md:planradius:view")
    @RequestMapping(value = {"list"})
    public String list(PlanRadius planRadius, HttpServletRequest request, HttpServletResponse response, Model model) {
        planRadius.setDelFlag(null);
        Long areaId = Optional.ofNullable(planRadius).map(PlanRadius::getArea).map(Area::getId).orElse(-1L);
        if(areaId != null) {
            planRadius.setArea(areaService.get(areaId));
        }
        Page<PlanRadius> page = planRadiusService.findPage(new Page<PlanRadius>(request, response), planRadius);
        if(page.getList()!=null && page.getList().size()>0){
            Map<Long,Area> subAreaMap = areaService.findMapByType(Area.TYPE_VALUE_COUNTY);
            if(subAreaMap!=null && subAreaMap.size()>0){
                for(PlanRadius item:page.getList()){
                    Area area = subAreaMap.get(item.getArea().getId());
                    if(area!=null){
                        item.setArea(area);
                    }
                }
            }
        }
        model.addAttribute("page", page);
        model.addAttribute("planRadius", planRadius);
        return "modules/md/planRadiusList";
    }

    @RequiresPermissions(value = {"md:planradius:view","md:planradius:edit"},logical = Logical.OR)
    @RequestMapping(value = "form")
    public String form(PlanRadius planRadius, Model model) {
        User user = UserUtils.getUser();
        Long areaId = Optional.ofNullable(planRadius).map(PlanRadius::getArea).map(Area::getId).orElse(-1L);
        planRadius = planRadiusService.getByAreaId(areaId);
        if (!ObjectUtils.isEmpty(planRadius)) {
            planRadius.setArea(areaService.get(areaId));
            if (planRadius.getRadius1() != null) {
                planRadius.setRadius1(planRadius.getRadius1()/1000);  // 米折算成千米
            }
            if (planRadius.getRadius2() != null) {
                planRadius.setRadius2(planRadius.getRadius2()/1000);  // 米折算成千米
            }
            if (planRadius.getRadius3() != null) {
                planRadius.setRadius3(planRadius.getRadius3()/1000);  // 米折算成千米
            }
        }
        else {
            planRadius = PlanRadius.builder().build();
        }

        model.addAttribute("planRadius", planRadius);

        return "modules/md/planRadiusForm";
    }

    @RequiresPermissions("md:planradius:edit")
    @RequestMapping(value = "save")
    public String save(PlanRadius planRadius, @RequestParam Map<String, Object> paramMap, Model model, RedirectAttributes redirectAttributes) {
        if (!beanValidator(model, planRadius)){
            return form(planRadius, model);
        }
        planRadius.setDelFlag(PlanRadius.DEL_FLAG_NORMAL);

        if (planRadius.getRadius1() != null) {
            planRadius.setRadius1(planRadius.getRadius1()*1000);  // 千米换算成米
        }
        if (planRadius.getRadius2() != null) {
            planRadius.setRadius2(planRadius.getRadius2()*1000);  // 千米换算成米
        }
        if (planRadius.getRadius3() != null) {
            planRadius.setRadius3(planRadius.getRadius3()*1000);  // 千米换算成米
        }

        String coverSubArea = paramMap.get("coverSubArea")!= null? paramMap.get("coverSubArea").toString():"0";
        System.out.println(String.format("是否要覆盖区/县记录:%s",coverSubArea));

        Long areaId = Optional.ofNullable(planRadius).map(PlanRadius::getArea).map(Area::getId).orElse(-1L);
        Area area = areaService.get(areaId);
        Integer areaType = Optional.ofNullable(area).map(Area::getType).orElse(-1);
        if (planRadius.getIsNewRecord() && areaType > 3) {
            PlanRadius tempPlanRadius = planRadiusService.getByAreaId(areaId);
            if (!ObjectUtils.isEmpty(tempPlanRadius) && !ObjectUtils.isEmpty(tempPlanRadius.getId())) {
                String strTempName = Optional.ofNullable(tempPlanRadius)
                        .map(PlanRadius::getArea)
                        .map(Area::getName)
                        .orElse("");
                addMessage(redirectAttributes, "区域[" + strTempName + "] 半径已设置,请不要重复添加!");
                return String.format("redirect:%s/md/planradius/list",adminPath);
            }
        }


        String strName = "";
        strName = Optional.ofNullable(planRadius)
                .map(PlanRadius::getArea)
                .map(Area::getName)
                .orElse("");
        try {
            if (areaType > 3) {
                planRadiusService.save(planRadius);
                addMessage(redirectAttributes, "保存区域[" + strName + "] 半径范围成功");
            }
            else if (areaType >=2 && areaType <= 3) {
                List<Area> areaList = planRadiusService.findAreaList(area);
                final int[] iCount = {0};
                if (!ObjectUtils.isEmpty(areaList)) {
                    areaList.stream().filter(r -> r.getType().equals(4)).forEach(r -> {
                        PlanRadius tempPlanRadius = planRadiusService.getByAreaId(r.getId());
                        PlanRadius entity = PlanRadius.builder().build();
                        if (!ObjectUtils.isEmpty(tempPlanRadius) && ObjectUtils.isEmpty(tempPlanRadius.getId())) {
                            entity.setIsNewRecord(true);
                            entity.setRemarks(planRadius.getRemarks());
                        } else {
                            entity.setId(tempPlanRadius.getId());
                            entity.setRemarks(StringUtils.isEmpty(planRadius.getRemarks())?tempPlanRadius.getRemarks():planRadius.getRemarks());
                            entity.setIsNewRecord(false);
                        }
                        entity.setRadius1(planRadius.getRadius1());
                        entity.setRadius2(planRadius.getRadius2());
                        entity.setRadius3(planRadius.getRadius3());
                        entity.setArea(r);
                        entity.setDelFlag(PlanRadius.DEL_FLAG_NORMAL);

                        // 0 不覆盖,1 覆盖
                        if ((coverSubArea.equals("0") && entity.getIsNewRecord() ==true) || coverSubArea.equals("1"))  {
                            planRadiusService.save(entity);
                            iCount[0]++;
                        }
                    });
                    addMessage(redirectAttributes, "保存区域[" + strName + "] 下的["+ iCount[0]+"]个区县半径范围下成功");
                }
            }

            return String.format("redirect:%s/md/planradius/list?repage",adminPath);
        }catch (Exception ex){
            return form(planRadius,model);
        }
    }

    /**
     * 停用
     * 逻辑删除
     */
    @RequiresPermissions("md:planradius:stop")
    @RequestMapping(value = "stop")
    public String delete(Long id, HttpServletRequest request, RedirectAttributes redirectAttributes)
    {
        PlanRadius planRadius = planRadiusService.get(id);
        if(ObjectUtils.isEmpty(planRadius)){
            addMessage(redirectAttributes, "该区域半径设定不存在");
            return "redirect:" + Global.getAdminPath() + "/md/planradius/list?repage";
        }else if(planRadius.getDelFlag() == ServicePointStation.DEL_FLAG_DELETE){
            addMessage(redirectAttributes, "该区域半径已停用");
            return "redirect:" + Global.getAdminPath() + "/md/planradius/list?repage";
        }
        planRadiusService.delete(planRadius);
        addMessage(redirectAttributes, "停用区域半径成功");

        return "redirect:" + Global.getAdminPath() + "/md/planradius/list?repage";
    }

    /**
     * 启用
     * 逻辑删除
     */
    @RequiresPermissions("md:planradius:stop")
    @RequestMapping(value = "enable")
    public String enable(Long id, HttpServletRequest request, RedirectAttributes redirectAttributes)
    {
        PlanRadius planRadius = planRadiusService.get(id);
        if(ObjectUtils.isEmpty(planRadius)){
            addMessage(redirectAttributes, "该区域半径设定不存在");
            return "redirect:" + Global.getAdminPath() + "/md/planradius/list?repage";
        }else if(planRadius.getDelFlag() == ServicePointStation.DEL_FLAG_NORMAL){
            addMessage(redirectAttributes, "该区域半径已启用");
            return "redirect:" + Global.getAdminPath() + "/md/planradius/list?repage";
        }
        planRadiusService.enable(planRadius);
        addMessage(redirectAttributes, "启用区域半径成功");

        return "redirect:" + Global.getAdminPath() + "/md/planradius/list?repage";
    }
}
