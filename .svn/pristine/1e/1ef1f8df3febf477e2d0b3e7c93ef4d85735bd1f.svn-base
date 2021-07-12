package com.wolfking.jeesite.ms.tmall.rpt.web;


import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.tmall.rpt.entity.B2BRptSearchModel;
import com.wolfking.jeesite.ms.tmall.rpt.service.CanboOrderInfoRptService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "${adminPath}/b2b/rpt/processlog/")
public class CanboOrderInfoController extends BaseController {

    @Autowired
    private CanboOrderInfoRptService orderInfoRptService;


    @ModelAttribute("canboSearchModel")
    public B2BRptSearchModel get(@ModelAttribute("canboSearchModel") B2BRptSearchModel canboSearchModel) {
        if (canboSearchModel == null) {
            canboSearchModel = new B2BRptSearchModel();
        }
        Date now = new Date(); //默认使用当天作为查询条件

        if (canboSearchModel.getBeginCreateDt() == null) {
            canboSearchModel.setBeginDate(now);
        }
        if (canboSearchModel.getEndCreateDt() == null) {
            canboSearchModel.setEndDate(now);
        }
        if (canboSearchModel.getProcessFlag() == null) {
            canboSearchModel.setProcessFlag(2);
        }
        return canboSearchModel;
    }

    //工单
    @RequestMapping(value = "canboorder")
    public String tmallOrderInfo(@ModelAttribute("canboSearchModel")B2BRptSearchModel canboSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        canboSearchModel.setBeginCreateDt(DateUtils.getStartOfDay(canboSearchModel.getBeginDate()).getTime());
        canboSearchModel.setEndCreateDt(DateUtils.getEndOfDay(canboSearchModel.getEndDate()).getTime());
        Page<B2BOrderProcesslog> page = orderInfoRptService.getList(new Page<B2BRptSearchModel>(request, response), canboSearchModel);

        List<Dict> b2bInterfaceId = Lists.newArrayListWithCapacity(6);
        b2bInterfaceId.add(new Dict(2001,"从康宝获取工单列表"));
        b2bInterfaceId.add(new Dict(2002,"向康宝回传是否接收工单标识"));
        b2bInterfaceId.add(new Dict(2003,"向康宝反馈派工信息"));
        b2bInterfaceId.add(new Dict(2004,"向康宝反馈预约信息"));
        b2bInterfaceId.add(new Dict(2005,"向康宝反馈取消工单信息"));
        b2bInterfaceId.add(new Dict(2006,"向康宝反馈工单单完工信息"));
        model.addAttribute("b2bInterfaceId",b2bInterfaceId);
        model.addAttribute("page",page);
        model.addAttribute("canboSearchModel",canboSearchModel);
        return "modules/tmall/rpt/canboOrderReport";
    }


}
