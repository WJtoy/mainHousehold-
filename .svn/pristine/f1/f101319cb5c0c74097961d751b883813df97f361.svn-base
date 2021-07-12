package com.wolfking.jeesite.ms.joyoung.rpt.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.jd.rpt.entity.JDSearchModel;
import com.wolfking.jeesite.ms.joyoung.rpt.entity.JoyoungSearchModel;
import com.wolfking.jeesite.ms.joyoung.rpt.service.JoyoungOrderProcessLogService;
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
public class JoyoungOrderProcessLogController extends BaseController {

    @Autowired
    private JoyoungOrderProcessLogService orderInfoRptService;


    @ModelAttribute("joyoungSearchModel")
    public JoyoungSearchModel get(@ModelAttribute("joyoungSearchModel") JoyoungSearchModel joyoungSearchModel) {
        if (joyoungSearchModel == null) {
            joyoungSearchModel = new JoyoungSearchModel();
        }
        Date now = new Date(); //默认使用当天作为查询条件

        if (joyoungSearchModel.getBeginCreateDt() == null) {
            joyoungSearchModel.setBeginDate(now);
        }
        if (joyoungSearchModel.getEndCreateDt() == null) {
            joyoungSearchModel.setEndDate(now);
        }
        if (joyoungSearchModel.getProcessFlag() == null) {
            joyoungSearchModel.setProcessFlag(2);
        }
        return joyoungSearchModel;
    }

    //工单
    @RequestMapping(value = "joyoungorder")
    public String JoyoungOrderProcessLog(JoyoungSearchModel joyoungSearchModel , HttpServletRequest request, HttpServletResponse response, Model model) {
        joyoungSearchModel.setBeginCreateDt(DateUtils.getStartOfDay(joyoungSearchModel.getBeginDate()).getTime());
        joyoungSearchModel.setEndCreateDt(DateUtils.getStartOfDay(joyoungSearchModel.getEndDate()).getTime());
        Page<B2BOrderProcesslog> page = orderInfoRptService.getList(new Page<JDSearchModel>(request, response), joyoungSearchModel);
        List<Dict> b2bInterfaceId = Lists.newArrayListWithCapacity(8);
        b2bInterfaceId.add(new Dict(5001,"九阳获取工单接口"));
        b2bInterfaceId.add(new Dict(5002,"九阳接单确认接口"));
        b2bInterfaceId.add(new Dict(5003,"九阳工单派单接口"));
        b2bInterfaceId.add(new Dict(5004,"九阳工单预约接口"));
        b2bInterfaceId.add(new Dict(5005,"九阳工单上门接口"));
        b2bInterfaceId.add(new Dict(5006,"九阳工单取消接口"));
        b2bInterfaceId.add(new Dict(5007,"九阳工单完工接口"));
        model.addAttribute("b2bInterfaceId",b2bInterfaceId);
        model.addAttribute("page",page);
        model.addAttribute("joyoungSearchModel",joyoungSearchModel);
        return "modules/joyoung/rpt/joyoungOrderProcessLogReport";
    }

}