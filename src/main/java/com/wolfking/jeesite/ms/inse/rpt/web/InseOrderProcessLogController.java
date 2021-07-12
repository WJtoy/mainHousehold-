package com.wolfking.jeesite.ms.inse.rpt.web;


import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.inse.rpt.entity.InseSearchModel;
import com.wolfking.jeesite.ms.inse.rpt.service.InseOrderProcessLogService;
import com.wolfking.jeesite.ms.jd.rpt.entity.JDSearchModel;
import com.wolfking.jeesite.ms.jd.rpt.service.JDOrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "${adminPath}/b2b/rpt/processlog/")
public class InseOrderProcessLogController extends BaseController {
    @Autowired
    private InseOrderProcessLogService orderInfoRptService;


    @ModelAttribute("inseSearchModel")
    public InseSearchModel get(@ModelAttribute("inseSearchModel") InseSearchModel inseSearchModel) {
        if (inseSearchModel == null) {
            inseSearchModel = new InseSearchModel();
        }
        Date now = new Date(); //默认使用当天作为查询条件

        if (inseSearchModel.getBeginCreateDt() == null) {
            inseSearchModel.setBeginDate(now);
        }
        if (inseSearchModel.getEndCreateDt() == null) {
            inseSearchModel.setEndDate(now);
        }
        if (inseSearchModel.getProcessFlag() == null) {
            inseSearchModel.setProcessFlag(2);
        }
        return inseSearchModel;
    }

    //工单
    @RequestMapping(value = "inseorder")
    public String InseOrderProcessLogInfo(InseSearchModel inseSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        inseSearchModel.setBeginCreateDt(DateUtils.getStartOfDay(inseSearchModel.getBeginDate()).getTime());
        inseSearchModel.setEndCreateDt(DateUtils.getEndOfDay(inseSearchModel.getEndDate()).getTime());
        Page<B2BOrderProcesslog> page = orderInfoRptService.getList(new Page<InseSearchModel>(request, response), inseSearchModel);
        List<Dict> b2bInterfaceId = Lists.newArrayListWithCapacity(7);
        b2bInterfaceId.add(new Dict(6002,"樱雪回传是否接收工单标识接口"));
        b2bInterfaceId.add(new Dict(6003,"樱雪工单派单接口"));
        b2bInterfaceId.add(new Dict(6004,"樱雪工单预约接口"));
        b2bInterfaceId.add(new Dict(6005,"樱雪工单上门接口"));
        b2bInterfaceId.add(new Dict(6006,"樱雪工单取消接口"));
        b2bInterfaceId.add(new Dict(6007,"樱雪工单完工接口"));
        model.addAttribute("b2bInterfaceId",b2bInterfaceId);
        model.addAttribute("page",page);
        model.addAttribute("inseSearchModel",inseSearchModel);
        return "modules/inse/rpt/inseOrderProcessLogReport";
    }

}

