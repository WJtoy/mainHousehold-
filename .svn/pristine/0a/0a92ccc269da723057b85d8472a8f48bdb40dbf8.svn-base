package com.wolfking.jeesite.ms.konka.rpt.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.jd.rpt.entity.JDSearchModel;
import com.wolfking.jeesite.ms.jd.rpt.service.JDOrderInfoService;
import com.wolfking.jeesite.ms.konka.rpt.entity.KonkaSearchModel;
import com.wolfking.jeesite.ms.konka.rpt.service.KonkaOrderProcessLogService;
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
public class KonkaOrderProcessLogController extends BaseController {

    @Autowired
    private KonkaOrderProcessLogService orderInfoRptService;


    @ModelAttribute("konkaSearchModel")
    public KonkaSearchModel get(@ModelAttribute("konkaSearchModel") KonkaSearchModel konkaSearchModel) {
        if (konkaSearchModel == null) {
            konkaSearchModel = new KonkaSearchModel();
        }
        Date now = new Date(); //默认使用当天作为查询条件

        if (konkaSearchModel.getBeginCreateDt() == null) {
            konkaSearchModel.setBeginDate(now);
        }
        if (konkaSearchModel.getEndCreateDt() == null) {
            konkaSearchModel.setEndDate(now);
        }
        if (konkaSearchModel.getProcessFlag() == null) {
            konkaSearchModel.setProcessFlag(2);
        }
        return konkaSearchModel;
    }

    //工单
    @RequestMapping(value = "konkaorder")
    public String konkaOrderProcessLog(KonkaSearchModel konkaSearchModel , HttpServletRequest request, HttpServletResponse response, Model model) {
        konkaSearchModel.setBeginCreateDt(DateUtils.getStartOfDay(konkaSearchModel.getBeginDate()).getTime());
        konkaSearchModel.setEndCreateDt(DateUtils.getStartOfDay(konkaSearchModel.getEndDate()).getTime());
        Page<B2BOrderProcesslog> page = orderInfoRptService.getList(new Page<JDSearchModel>(request, response), konkaSearchModel);
        List<Dict> b2bInterfaceId = Lists.newArrayListWithCapacity(8);
        b2bInterfaceId.add(new Dict(4001,"康佳获取维修工单"));
        b2bInterfaceId.add(new Dict(4002,"康佳确认已收工单"));
        b2bInterfaceId.add(new Dict(4003,"康佳确认接单接口"));
        b2bInterfaceId.add(new Dict(4004,"康佳工单拒单接口"));
        b2bInterfaceId.add(new Dict(4005,"康佳工单派单接口"));
        b2bInterfaceId.add(new Dict(4006,"康佳工单预约接口"));
        b2bInterfaceId.add(new Dict(4007,"康佳工单上門接口"));
        b2bInterfaceId.add(new Dict(4008,"康佳工单完工接口"));
        model.addAttribute("b2bInterfaceId",b2bInterfaceId);
        model.addAttribute("page",page);
        model.addAttribute("konkaSearchModel",konkaSearchModel);
        return "modules/konka/rpt/konkaOrderProcessLogReport";
    }

}