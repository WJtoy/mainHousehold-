package com.wolfking.jeesite.ms.tmall.rpt.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2b.rpt.B2BProcesslog;
import com.kkl.kklplus.entity.b2b.rpt.Processlog;
import com.kkl.kklplus.entity.b2b.rpt.ProcesslogSearchModel;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.Encodes;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.excel.ExportExcel;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.tmall.rpt.feign.B2BProcessFlag;
import com.wolfking.jeesite.ms.tmall.rpt.service.OrderInfoRptService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.apache.http.util.TextUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "${adminPath}/tmall/rpt/tmallorder/")
public class TmallOrderInfoController extends BaseController {

    @Autowired
    private OrderInfoRptService orderInfoRptService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ServicePointService servicePointService;

    public static final int EXECL_CELL_WIDTH_15 		= 15;
    public static final int EXECL_CELL_WIDTH_10 		= 10;
    public static final int EXECL_CELL_HEIGHT_TITLE 	= 30;
    public static final int EXECL_CELL_HEIGHT_HEADER 	= 20;
    public static final int EXECL_CELL_HEIGHT_DATA 		= 20;
    @ModelAttribute("processlogSearchModel")
    public ProcesslogSearchModel get(@ModelAttribute("processlogSearchModel") ProcesslogSearchModel processlogSearchModel) {
        if (processlogSearchModel == null) {
            processlogSearchModel = new ProcesslogSearchModel();
        }
        Date now = new Date(); //默认使用当天作为查询条件
        if (processlogSearchModel.getCreateDateStart() == null) {
            processlogSearchModel.setCreateDateStart(now);

        }
        if (processlogSearchModel.getCreateDateEnd() == null) {
            processlogSearchModel.setCreateDateEnd(now);
        }
        if (processlogSearchModel.getProcessFlag()==null){
            processlogSearchModel.setProcessFlag(2);
        }
        return processlogSearchModel;
    }

    //工单
    @RequestMapping(value = "tmallOrderSUMReport")
    public String tmallOrderInfo(ProcesslogSearchModel processlogSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        processlogSearchModel.setCreateDateStart(DateUtils.getStartOfDay(processlogSearchModel.getCreateDateStart()));
        processlogSearchModel.setCreateDateEnd(DateUtils.getEndOfDay(processlogSearchModel.getCreateDateEnd()));

        Page<B2BProcesslog> page = orderInfoRptService.getList(new Page<ProcesslogSearchModel>(request, response), processlogSearchModel);
        model.addAttribute("page",page);
        model.addAttribute("processlogSearchModel",processlogSearchModel);
        return "modules/tmall/rpt/tmallOrderSUMReport";
    }

    @RequestMapping(value = "export", method = RequestMethod.POST)
    public String customerAccountRptExport(ProcesslogSearchModel processlogSearchModel,
                                           HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        processlogSearchModel.setCreateDateStart(DateUtils.getStartOfDay(processlogSearchModel.getCreateDateStart()));
        processlogSearchModel.setCreateDateEnd(DateUtils.getEndOfDay(processlogSearchModel.getCreateDateEnd()));
        Page<ProcesslogSearchModel> processlogPage = new Page<ProcesslogSearchModel>(request, response);
        processlogPage.setPageSize(200000);
        Page<B2BProcesslog> page = orderInfoRptService.getList(processlogPage, processlogSearchModel);
        List<B2BProcesslog> list = page.getList();
        try {
            String xName = "天猫状态数据查询"+ DateUtils.formatDate(processlogSearchModel.getCreateDateStart(), "yyyy年MM月dd日") +
                    "~" + DateUtils.formatDate(processlogSearchModel.getCreateDateEnd(),"yyyy年MM月dd日")+ "）";

            ExportExcel exportExcel = new ExportExcel();
            SXSSFWorkbook xBook 	= new SXSSFWorkbook(500);
            Sheet xSheet 			= xBook.createSheet(xName);
            xSheet.setDefaultColumnWidth(EXECL_CELL_WIDTH_10);
            Map<String, CellStyle> xStyle = exportExcel.createStyles(xBook);
            int rowIndex = 0;

            //====================================================绘制标题行============================================================
            Row titleRow = xSheet.createRow(rowIndex++);
            titleRow.setHeightInPoints(EXECL_CELL_HEIGHT_TITLE);
            ExportExcel.createCell(titleRow,0, xStyle, ExportExcel.CELL_STYLE_NAME_TITLE, xName);
            xSheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), 0, 5));

            //====================================================绘制表头============================================================
            //表头第一行
            Row headerRow = xSheet.createRow(rowIndex++);
            headerRow.setHeightInPoints(EXECL_CELL_HEIGHT_HEADER);

            ExportExcel.createCell(headerRow,0, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "序号");
            ExportExcel.createCell(headerRow,1, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "接口");
            ExportExcel.createCell(headerRow,2, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "内容");
            ExportExcel.createCell(headerRow,3, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "创建时间");
            ExportExcel.createCell(headerRow,4, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "状态");
            ExportExcel.createCell(headerRow,5, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "备注");

            xSheet.createFreezePane(0, rowIndex); // 冻结单元格(x, y)

            Cell dataCell = null;
            if (list != null && list.size() > 0) {

                double totalBalance = 0.0;
                double totalBlockAmount = 0.0;
                double totalAllowCreateOrderCharge = 0.0;
                double totalCredit = 0.0;

                int rowsCount = list.size();
                for (int dataRowIndex = 0; dataRowIndex < rowsCount; dataRowIndex++) {
                    B2BProcesslog processlog = list.get(dataRowIndex);
                    Row dataRow = xSheet.createRow(rowIndex++);
                    dataRow.setHeightInPoints(EXECL_CELL_HEIGHT_DATA);
                    int columnIndex = 0;

                    ExportExcel.createCell(dataRow, columnIndex++, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, dataRowIndex+1);


                    ExportExcel.createCell(dataRow, columnIndex++, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, processlog.getInterfaceName());
                    ExportExcel.createCell(dataRow, columnIndex++, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, processlog.getInfoJson());
                    ExportExcel.createCell(dataRow, columnIndex++, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (DateUtils.formatDate(processlog.getCreateDate(), "yyyy-MM-dd HH:mm:ss")));
                    ExportExcel.createCell(dataRow, columnIndex++, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, processlog.getProcessFlag()==0?"受理":processlog.getProcessFlag()==1?"执行":processlog.getProcessFlag()==2?"拒绝":processlog.getProcessFlag()==3?"失败":"成功");
                    ExportExcel.createCell(dataRow, columnIndex++, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, processlog.getProcessComment());

                }

                Row sumRow = xSheet.createRow(rowIndex);
                sumRow.setHeightInPoints(EXECL_CELL_HEIGHT_DATA);

            }

            response.reset();
            response.setContentType("application/octet-stream; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename="
                    + Encodes.urlEncode(xName + ".xlsx"));
            xBook.write(response.getOutputStream());
            xBook.dispose();

            return null;
        }
        catch (Exception e) {
            addMessage(redirectAttributes, "导出天猫状态数据失败！失败信息：" + e.getMessage());
        }//tmall/rpt/tmallOrderSUMReport
        return "redirect:" + Global.getAdminPath() + "/tmall/rpt/tmallorder/tmallOrderSUMReport?repage";
    }

    @RequestMapping(value = "orderDetailInfo")
    public String tmallOrderDetailInfo(String workcardId,String quarter,HttpServletRequest request,HttpServletResponse response, Model model){
        Boolean errorFlag = false;
        Order order = new Order();
        Long orderId = null;
        String orderQuater = "";
        if (workcardId == null || TextUtils.isEmpty(workcardId)){
            errorFlag = true;
            addMessage(model, "订单代码传递错误");
        }else {
            Map<String, Object> orderId1 = orderInfoRptService.getOrderId(workcardId);
            orderId = (Long) orderId1.get("id");
            orderQuater = (String) orderId1.get("quarter");
        }

        if (orderId == null || orderId <= 0)
        {
            errorFlag = true;
            addMessage(model, "订单代码传递错误");
        } else
        {
            order = orderService.getOrderById(orderId, orderQuater,OrderUtils.OrderDataLevel.DETAIL,true);
            if(order == null || order.getOrderCondition() == null){
                errorFlag = true;
                addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
            }else {
                ServicePoint servicePoint = order.getOrderCondition().getServicePoint();
                if (servicePoint != null && servicePoint.getId() != null & servicePoint.getId() > 0) {
                    Engineer engineer = servicePointService.getEngineerFromCache(servicePoint.getId(), order.getOrderCondition().getEngineer().getId());
                    if (engineer != null) {
                        User engineerUser = new User(engineer.getId());
                        engineerUser.setName(engineer.getName());
                        engineerUser.setMobile(engineer.getContactInfo());
                        engineerUser.setSubFlag(engineer.getMasterFlag() == 1 ? 0 : 1);
                        order.getOrderCondition().setEngineer(engineerUser);
                    }
                }
            }
        }
        model.addAttribute("order", order);
        model.addAttribute("errorFlag",errorFlag);
        if(!errorFlag) {
            model.addAttribute("fourServicePhone", MSDictUtils.getDictSingleValue("400ServicePhone", "400-666-3653"));
        }else{
            model.addAttribute("fourServicePhone", "400-666-3653");
        }
        return "modules/tmall/rpt/tmallPopJson";
    }

}
