package com.wolfking.jeesite.modules.finance.sd.web;

import com.kkl.kklplus.entity.md.GlobalMappingSalesSubFlagEnum;
import com.kkl.kklplus.entity.praise.PraisePageSearchModel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.service.SalesPraiseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Controller
@RequestMapping(value = "${adminPath}/sd/finance/praise/")
@Slf4j
public class FiPraiseController extends BaseController {


    @Autowired
    private SalesPraiseService salesPraiseService;



    /**
     * 业务查询待处理好评信息列表
     * @param praisePageSearchModel
     * @param request
     */
    @RequiresPermissions("sd:salespraise:view")
    @RequestMapping(value = "pendingReviewList")
    public String pendingReviewList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return "modules/finance/sd/praise/pendingReviewList";
        }
        Date date;
        if (StringUtils.isBlank(praisePageSearchModel.getBeginDate())) {
            date = DateUtils.getDateEnd(new Date());
            praisePageSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
            praisePageSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(praisePageSearchModel.getBeginDate());
            praisePageSearchModel.setBeginDt(date.getTime());
            date = DateUtils.parseDate(praisePageSearchModel.getEndDate());
            date = DateUtils.getDateEnd(date);
            praisePageSearchModel.setEndDt(date.getTime());
        }
        page = salesPraiseService.pendingReviewList(page,praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/finance/sd/praise/pendingReviewList";
    }



    /**
     * 已审核好评单列表 get请求 (不点击出查询按钮不加载数据)
     * */
    @RequiresPermissions("sd:salespraise:view")
    @RequestMapping(value = {"approvedList"}, method = RequestMethod.GET)
    public String approvedListGet(PraisePageSearchModel praisePageSearchModel, Model model) {
        Date date;
        date = DateUtils.getDateEnd(new Date());
        praisePageSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
        praisePageSearchModel.setEndDt(date.getTime());
        date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
        praisePageSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
        praisePageSearchModel.setBeginDt(date.getTime());
        model.addAttribute("page", null);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/finance/sd/praise/approvedList";
    }

    /**
     * 已审核好评单列表 post请求
     * */
    @RequiresPermissions("sd:salespraise:view")
    @RequestMapping(value = {"approvedList"},method = RequestMethod.POST)
    public String approvedList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return "modules/finance/sd/praise/approvedList";
        }
        Date date;
        if (StringUtils.isBlank(praisePageSearchModel.getBeginDate())) {
            date = DateUtils.getDateEnd(new Date());
            praisePageSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
            praisePageSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(praisePageSearchModel.getBeginDate());
            praisePageSearchModel.setBeginDt(date.getTime());
            date = DateUtils.parseDate(praisePageSearchModel.getEndDate());
            date = DateUtils.getDateEnd(date);
            praisePageSearchModel.setEndDt(date.getTime());
        }
        page = salesPraiseService.approvedList(page,praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/finance/sd/praise/approvedList";
    }



    /**
     * 所有好评单列表 get请求 (不点击出查询按钮不加载数据)
     * */
    @RequiresPermissions("sd:salespraise:view")
    @RequestMapping(value = {"findAllList"}, method = RequestMethod.GET)
    public String findAllListGet(PraisePageSearchModel praisePageSearchModel, Model model) {
        Date date;
        date = DateUtils.getDateEnd(new Date());
        praisePageSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
        praisePageSearchModel.setEndDt(date.getTime());
        date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
        praisePageSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
        praisePageSearchModel.setBeginDt(date.getTime());
        model.addAttribute("page", null);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/finance/sd/praise/allList";
    }

    /**
     * 业务查询所有好评信息列表
     * @param praisePageSearchModel
     * @param request
     */
    @RequiresPermissions("sd:salespraise:view")
    @RequestMapping(value = {"findAllList"},method = RequestMethod.POST)
    public String findAllList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return "modules/finance/sd/praise/allList";
        }
        Date date;
        if (StringUtils.isBlank(praisePageSearchModel.getBeginDate())) {
            date = DateUtils.getDateEnd(new Date());
            praisePageSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
            praisePageSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(praisePageSearchModel.getBeginDate());
            praisePageSearchModel.setBeginDt(date.getTime());
            date = DateUtils.parseDate(praisePageSearchModel.getEndDate());
            date = DateUtils.getDateEnd(date);
            praisePageSearchModel.setEndDt(date.getTime());
        }
        if (user.isSaleman()) {
            praisePageSearchModel.setSalesId(user.getId());//业务员
            praisePageSearchModel.setSubFlag(user.getSubFlag()==null?0:user.getSubFlag());
            /*
            if(user.isSalesPerson()){ //业务
                praisePageSearchModel.setSubFlag(GlobalMappingSalesSubFlagEnum.SALES.getValue());
            }else if(user.isMerchandiser()){ //跟单
                praisePageSearchModel.setSubFlag(GlobalMappingSalesSubFlagEnum.MERCHANDISER.getValue());
            }*/
        }
        page = salesPraiseService.findPraiseList(page,praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/finance/sd/praise/allList";
    }

}
