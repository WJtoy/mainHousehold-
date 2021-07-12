package com.wolfking.jeesite.ms.xyingyan.sd.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderSearchVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.xyingyan.sd.service.XYingYanOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("${adminPath}/b2b/xyingyan/order/")
public class XYingYanOrderController extends BaseController {

//    @Autowired
//    private XYingYanOrderService xYingYanOrderService;
//
//    /**
//     * 转换失败的订单列表
//     */
//    @RequiresPermissions("b2b:order:transfer")
//    @RequestMapping(value = "/toTransferList")
//    public String toTransferList(B2BOrderSearchVModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
//        String viewForm = "modules/xyingyan/sd/toTransferList";
//        Page<B2BOrderVModel> page = new Page<>();
//        Session session = UserUtils.getSession();
//        if (session == null) {
//            addMessage(model, "错误：登录超时！");
//            model.addAttribute("page", page);
//            model.addAttribute("order", order);
//            model.addAttribute("processFlags", Lists.newArrayList());
//            model.addAttribute("canSearch", false);
//            return viewForm;
//        }
//
//        Boolean canSearch = true;
//        Date now = new Date();
//        if (order.getBeginCreateDate() == null) {
//            order.setBeginCreateDate(DateUtils.addMonth(now, -1));
//        }
//        if (order.getEndCreateDate() == null) {
//            order.setEndCreateDate(now);
//        }
//        order.setBeginCreateDate(DateUtils.getStartOfDay(order.getBeginCreateDate()));
//        order.setEndCreateDate(DateUtils.getEndOfDay(order.getEndCreateDate()));
//        order.setBeginCreateDt(order.getBeginCreateDate().getTime());
//        order.setEndCreateDt(order.getEndCreateDate().getTime());
//        page = xYingYanOrderService.findPageOfToTransfer(new Page(request, response), order, B2BDataSourceEnum.XYINGYAN);
//        List<Dict> processFlags = Lists.newArrayListWithCapacity(3);
//        processFlags.add(new Dict("0", "受理，还未转换"));
//        processFlags.add(new Dict("2", "拒绝，转换但业务数据不满足要求"));
//        processFlags.add(new Dict("3", "失败，转换但发生错误"));
//
//        model.addAttribute("page", page);
//        model.addAttribute("order", order);
//        model.addAttribute("processFlags", processFlags);
//        model.addAttribute("canSearch", canSearch);
//
//        return viewForm;
//
//    }

}
