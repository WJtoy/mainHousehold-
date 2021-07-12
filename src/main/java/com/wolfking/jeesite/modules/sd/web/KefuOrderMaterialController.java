package com.wolfking.jeesite.modules.sd.web;


import cn.hutool.core.util.StrUtil;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.*;
import com.wolfking.jeesite.common.utils.excel.ExportExcel;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderMaterialSearchModel;
import com.wolfking.jeesite.modules.sd.service.KefuOrderMaterialService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.KefuTypeEnum;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


/**
 * 订单配件管理
 *
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/kefuOrderMaterial")
@Slf4j
public class KefuOrderMaterialController extends BaseController
{

	private static final String MODEL_ATTR_PAGE = "page";
	private static final String MODEL_ATTR_SEARCH_MODEL = "searchModel";

	@Autowired
	private KefuOrderMaterialService kefuOrderMaterialService;

	//region 客服列表

	//region 公共
	/**
	 * 设置及初始化查询条件
	 * @param user  当前帐号
	 * @param searchModel   查询条件
	 * @param initMonths    初始最小查询时间段(月)
	 * @param byApplyDateRange by申请日期查询开关
	 * @param maxOrderDays   下单最大查询范围(天)
	 */
	private OrderMaterialSearchModel setSearchModel(User user, OrderMaterialSearchModel searchModel, Model model, Page<MaterialMaster> page,int initMonths, boolean byApplyDateRange , int maxOrderDays) {
		if (searchModel == null) {
			searchModel = new OrderMaterialSearchModel();
		}

		if (searchModel.getStatus() == null || searchModel.getStatus() == 0) {
			searchModel.setStatus(MaterialMaster.STATUS_NEW);
		}
		searchModel.setRejectReason(StrUtil.trimToEmpty(searchModel.getRejectReason()));
		if(StrUtil.isNotBlank(searchModel.getRejectReason()) && searchModel.getRejectReason().equals("0")){
			searchModel.setRejectReason(StrUtil.EMPTY);
		}
		Date now = new Date();
		//申请日期
		if(byApplyDateRange) {
			if (searchModel.getBeginDate() == null) {
				searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
				searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), 0 - initMonths)));
			} else {
				searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
			}
			//检查最大时间范围
			if(maxOrderDays > 0){
				Date maxDate = DateUtils.addDays(searchModel.getEndDate(),1-maxOrderDays);
				maxDate = DateUtils.getDateStart(maxDate);
				if(DateUtils.pastDays(searchModel.getBeginDate(),maxDate)>0){
					searchModel.setBeginDate(maxDate);
				}
			}
		}
		Boolean isValide = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValide){
			searchModel.setValid(false);
		}
		String checkRegion = kefuOrderMaterialService.loadAndCheckUserRegions(searchModel,user);
		if(org.apache.commons.lang3.StringUtils.isNotEmpty(checkRegion)){
			addMessage(model, checkRegion);
			searchModel.setValid(false);
			return searchModel;
		}
		//客服主管
		boolean isServiceSupervisor = user.getRoleEnNames().contains("Customer service supervisor");

		//vip客服查询自己负责的单，by客户+区域+品类
		//1.by 客户，前端客户已按客服筛选了
		if(user.isKefu()) {
			KefuTypeEnum kefuTypeEnum = KefuTypeEnum.fromCode(user.getSubFlag());
			if(kefuTypeEnum!=null){
				searchModel.setCustomerType(kefuTypeEnum.getCustomerType());
				searchModel.setKefuType(kefuTypeEnum.getKefuType());
			}else{
				addMessage(model, "错误:读取客服类型错误");
				searchModel.setValid(false);
				return searchModel;
			}
		}else{
			//其他类型帐号，不限制客户及突击区域订单
			searchModel.setCustomerType(null);
			searchModel.setKefuType(null);
		}
		//2.by 区域
		if (isServiceSupervisor) {
			searchModel.setCreateBy(user);//*
		} else if (user.isKefu()) {
			searchModel.setCreateBy(user);//*,只有客服才按帐号筛选
		} else if (user.isInnerAccount()) { //内部帐号
			searchModel.setCreateBy(user);//*
		}
		return searchModel;
	}

	/**
	 * 检查订单号，手机号输入
	 * @param searchModel
	 * @param model
	 * @return
	 */
	private Boolean checkOrderNoAndPhone(OrderMaterialSearchModel searchModel, Model model, Page page){
		if(searchModel == null){
			return true;
		}
		//检查电话
		if (StringUtils.isNotBlank(searchModel.getOrderNo())){
			int orderSerchType = searchModel.getOrderNoSearchType();
			if(orderSerchType != 1) {
				addMessage(model, "错误：请输入正确的工单号");
				model.addAttribute("canAction", true);
				model.addAttribute("page", page);
				model.addAttribute("searchModel", searchModel);
				return false;
			}else{
				//检查分片
				try {
					Date goLiveDate = OrderUtils.getGoLiveDate();
					String[] quarters = DateUtils.getQuarterRange(goLiveDate, new Date());
					if(quarters.length == 2) {
						int start = StringUtils.toInteger(quarters[0]);
						int end = StringUtils.toInteger(quarters[1]);
						if(start>0 && end > 0){
							int quarter = StringUtils.toInteger(searchModel.getQuarter());
							if(quarter < start || quarter > end){
								addMessage(model, "错误：请输入正确的工单号,日期超出范围");
								model.addAttribute("canAction", true);
								model.addAttribute("page", page);
								model.addAttribute("searchModel", searchModel);
								return false;
							}
						}
					}
				}catch (Exception e){
					log.error("检查分片错误,orderNo:{}",searchModel.getOrderNo(),e);
				}
			}
		}
		if (StringUtils.isNotBlank(searchModel.getUserPhone())){
			if(searchModel.getIsPhone() != 1){
				addMessage(model, "错误：请输入正确的用户电话");
				model.addAttribute("canAction", true);
				model.addAttribute("page", page);
				model.addAttribute("searchModel", searchModel);
				return false;
			}
		}
		return true;
	}
	//endregion 公共

	//region 待审核

	/**
	 * 待审核配件列表
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/applylist")
	public String kefuApplyList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/kefu/materialList/applyList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
		if(!searchModel.getValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
		//状态
		searchModel.setStatus(MaterialMaster.STATUS_NEW);
		if (!request.getMethod().equalsIgnoreCase("post")) {
			searchModel.setMaterialType(1);//配件申请
		}
		try {
			//查询
			page = kefuOrderMaterialService.findKefuMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}

	/**
	 * 导出
	 */
	@RequestMapping(value = "/applylist/export",method = RequestMethod.POST)
	public String exportKefuApplyList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		String viewForm = "modules/sd/kefu/materialList/applyList";
		Page<MaterialMaster> page = new Page<>();
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return kefuApplyList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					model.addAttribute(MODEL_ATTR_PAGE, page);
					model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
					return viewForm;
				}
			}
			searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
			if(!searchModel.getValid()){
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_NEW);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				//查询
				page = kefuOrderMaterialService.findKefuMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			String[] tableTitle = new String[]
					{ "序号","订单号", "厂商", "状态", "类型", "配件类型", "产品",
							"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
							"跟踪状态","跟踪时间","跟踪内容"
					};
			String xName = "待审核配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			//headRow.setHeightInPoints(16);
			for (int i = 0,titleSize=tableTitle.length; i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle[i]);
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);
				//dataRow.setHeightInPoints(12);

				cell = dataRow.createCell(0);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				cell = dataRow.createCell(1);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				cell = dataRow.createCell(2);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCustomer() == null?"":StringUtils.toString(item.getCustomer().getName()));

				cell = dataRow.createCell(3);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				cell = dataRow.createCell(4);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				cell = dataRow.createCell(5);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				cell = dataRow.createCell(6);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				cell = dataRow.createCell(7);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				cell = dataRow.createCell(8);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				cell = dataRow.createCell(9);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());
				//快递
				cell = dataRow.createCell(10);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				cell = dataRow.createCell(11);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				cell = dataRow.createCell(12);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(13);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(14);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(15);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				//cell = dataRow.createCell(16);
				//cell.setCellStyle(dataCellStyle);
				////cell.setCellStyle(xStyle.get("redData"));
				//cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0; i < tableTitle.length; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			xSheet.setColumnWidth(9,30*256);
			xSheet.setColumnWidth(10,30*256);
			xSheet.setColumnWidth(15,50*256);

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.length-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e){
			addMessage(model, "导出Excel失败：" + e.getMessage());
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
			//return kefuApplyList(searchModel,request,response,model);
		}
	}

	//endregion

	//region 待发配件

	/**
	 * 待发货配件列表
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/tosendlist")
	public String kefuToSendList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/kefu/materialList/toSendList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
		if(!searchModel.getValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
		//状态：
		searchModel.setStatus(MaterialMaster.STATUS_APPROVED);
		if (!request.getMethod().equalsIgnoreCase("post")) {
			searchModel.setMaterialType(1);//配件申请
		}
		try {
			page = kefuOrderMaterialService.findKefuMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}

	/**
	 * 导出
	 */
	@RequestMapping(value = "/tosendlist/export",method = RequestMethod.POST)
	public String exportKefuToSendList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		String viewForm = "modules/sd/kefu/materialList/toSendList";
		Page<MaterialMaster> page = new Page<>();
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return kefuToSendList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					return kefuToSendList(searchModel,request,response,model);
				}
			}
			searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
			if(!searchModel.getValid()){
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_APPROVED);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				page = kefuOrderMaterialService.findKefuMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			String[] tableTitle = new String[]
					{ "序号","订单号", "厂商", "状态", "类型", "配件类型", "产品",
							"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
							"审核人","审核时间","跟踪状态","跟踪时间","跟踪内容","是否返件"
					};
			String xName = "待发货配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			//headRow.setHeightInPoints(16);
			for (int i = 0,titleSize=tableTitle.length; i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle[i]);
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);
				//dataRow.setHeightInPoints(12);

				cell = dataRow.createCell(0);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				cell = dataRow.createCell(1);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				cell = dataRow.createCell(2);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCustomer() == null?"":StringUtils.toString(item.getCustomer().getName()));

				cell = dataRow.createCell(3);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				cell = dataRow.createCell(4);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				cell = dataRow.createCell(5);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				cell = dataRow.createCell(6);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				cell = dataRow.createCell(7);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				cell = dataRow.createCell(8);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				cell = dataRow.createCell(9);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());
				//快递
				cell = dataRow.createCell(10);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				cell = dataRow.createCell(11);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				cell = dataRow.createCell(12);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(13);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUpdateBy().getName());

				cell = dataRow.createCell(14);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(15);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(16);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(17);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				cell = dataRow.createCell(18);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellStyle(xStyle.get("redData"));
				cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0; i < tableTitle.length; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			xSheet.setColumnWidth(9,30*256);
			xSheet.setColumnWidth(10,30*256);
			xSheet.setColumnWidth(17,50*256);

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.length-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出Excel失败：" + e.getMessage());
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
	}

	//endregion

	//region 已发货

	/**
	 * 已发货配件列表
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/sendlist")
	public String kefuSendList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/kefu/materialList/sendList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
		if(!searchModel.getValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
		//状态：
		searchModel.setStatus(MaterialMaster.STATUS_SENDED);
		if (!request.getMethod().equalsIgnoreCase("post")) {
			searchModel.setMaterialType(1);//配件申请
		}
		try {
			page = kefuOrderMaterialService.findKefuMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}


	/**
	 * 导出
	 */
	@RequestMapping(value = "/sendlist/export",method = RequestMethod.POST)
	public String exportKefuSendList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		String viewForm = "modules/sd/kefu/materialList/sendList";
		Page<MaterialMaster> page = new Page<>();
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return kefuSendList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					return kefuSendList(searchModel,request,response,model);
				}
			}
			searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
			if(!searchModel.getValid()){
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_SENDED);
			//客服
			if(user.isKefu() || user.isKefuLeader()) {
				searchModel.setKefu(user.getId());
			}
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				//查询
				page = kefuOrderMaterialService.findKefuMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				return kefuSendList(searchModel,request,response,model);
			}
			String[] tableTitle = new String[]
					{ "序号","订单号", "厂商", "状态", "类型", "配件类型", "产品",
							"用户姓名", "电话", "区域", "详细地址", "申请人", "申请时间",
							"发货人","发货时间","跟踪状态","跟踪时间","跟踪内容","是否返件"
					};
			String xName = "已发货配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			//headRow.setHeightInPoints(16);
			for (int i = 0,titleSize=tableTitle.length; i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle[i]);
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);
				//dataRow.setHeightInPoints(12);

				cell = dataRow.createCell(0);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				cell = dataRow.createCell(1);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				cell = dataRow.createCell(2);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCustomer() == null?"":StringUtils.toString(item.getCustomer().getName()));

				cell = dataRow.createCell(3);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				cell = dataRow.createCell(4);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				cell = dataRow.createCell(5);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				cell = dataRow.createCell(6);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				cell = dataRow.createCell(7);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				cell = dataRow.createCell(8);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				cell = dataRow.createCell(9);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());
				//快递
				cell = dataRow.createCell(10);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				cell = dataRow.createCell(11);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				cell = dataRow.createCell(12);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(13);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUpdateBy().getName());

				cell = dataRow.createCell(14);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(15);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(16);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(17);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				cell = dataRow.createCell(18);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellStyle(xStyle.get("redData"));
				cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0; i < tableTitle.length; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			xSheet.setColumnWidth(9,30*256);
			xSheet.setColumnWidth(10,30*256);
			xSheet.setColumnWidth(17,50*256);

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.length-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出Excel失败：" + e.getMessage());
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
	}

	//endregion

	//region 驳回

	/**
	 * 驳回列表
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/rejectlist")
	public String kefuRejectList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/kefu/materialList/rejectList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
		if(!searchModel.getValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
		//状态：
		searchModel.setStatus(MaterialMaster.STATUS_REJECT);
		if (!request.getMethod().equalsIgnoreCase("post")) {
			searchModel.setMaterialType(1);//配件申请
		}
		try {
			page = kefuOrderMaterialService.findKefuMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}


	/**
	 * 导出
	 */
	@RequestMapping(value = "/rejectlist/export",method = RequestMethod.POST)
	public String exportKefuRejectList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		String viewForm = "modules/sd/kefu/materialList/rejectList";
		Page<MaterialMaster> page = new Page<>();
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return kefuRejectList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					model.addAttribute(MODEL_ATTR_PAGE, page);
					model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
					return viewForm;
				}
			}
			searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
			if(!searchModel.getValid()){
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_REJECT);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				page = kefuOrderMaterialService.findKefuMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			String[] tableTitle = new String[]
					{ "序号","订单号", "厂商", "状态", "类型", "配件类型", "产品",
							"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
							"审核人","驳回时间","驳回原因","详细描述","跟踪状态","跟踪时间","跟踪内容"
					};
			String xName = "驳回配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			//headRow.setHeightInPoints(16);
			for (int i = 0,titleSize=tableTitle.length; i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle[i]);
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);

				cell = dataRow.createCell(0);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				cell = dataRow.createCell(1);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				cell = dataRow.createCell(2);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCustomer() == null?"":StringUtils.toString(item.getCustomer().getName()));

				cell = dataRow.createCell(3);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				cell = dataRow.createCell(4);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				cell = dataRow.createCell(5);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue("配件申请");

				cell = dataRow.createCell(6);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getProductNames());

				cell = dataRow.createCell(7);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				cell = dataRow.createCell(8);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				cell = dataRow.createCell(9);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());
				//快递
				cell = dataRow.createCell(10);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				cell = dataRow.createCell(11);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				cell = dataRow.createCell(12);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(13);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUpdateBy().getName());

				cell = dataRow.createCell(14);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"));

				//驳回原因
				cell = dataRow.createCell(15);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCloseType());

				cell = dataRow.createCell(16);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCloseRemark());

				cell = dataRow.createCell(17);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(18);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(19);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0; i < tableTitle.length; i++) {
				xSheet.autoSizeColumn(i);
			}
			xSheet.setColumnWidth(0,10*256);
			xSheet.setColumnWidth(9,30*256);
			xSheet.setColumnWidth(10,30*256);
			xSheet.setColumnWidth(15,20*256);
			xSheet.setColumnWidth(16,30*256);
			xSheet.setColumnWidth(19,50*256);

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.length-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出Excel失败：" + e.getMessage());
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
	}

	//endregion

	//region 完成

	/**
	 * 完成列表
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/closelist")
	public String kefuCloseList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/kefu/materialList/closeList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
		if(!searchModel.getValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
		//状态：
		searchModel.setStatus(MaterialMaster.STATUS_CLOSED);
		Boolean isValide = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValide){
			return viewForm;
		}
		try {
			page = kefuOrderMaterialService.findKefuMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}


	/**
	 * 导出
	 */
	@RequestMapping(value = "/closelist/export",method = RequestMethod.POST)
	public String exportKefuCloseList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		String viewForm = "modules/sd/kefu/materialList/closeList";
		Page<MaterialMaster> page = new Page<>();
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					model.addAttribute(MODEL_ATTR_PAGE, page);
					model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
					return viewForm;
				}
			}

			searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
			if(!searchModel.getValid()){
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_CLOSED);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				page = kefuOrderMaterialService.findKefuMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			String[] tableTitle = new String[]
					{ "序号","订单号", "厂商", "状态", "类型", "配件类型", "产品",
							"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
							"完成人","完成时间","跟踪状态","跟踪时间","跟踪内容","是否返件"
					};
			String xName = "完成配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			//headRow.setHeightInPoints(16);
			for (int i = 0,titleSize=tableTitle.length; i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle[i]);
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);
				//dataRow.setHeightInPoints(12);

				cell = dataRow.createCell(0);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				cell = dataRow.createCell(1);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				cell = dataRow.createCell(2);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCustomer() == null?"":StringUtils.toString(item.getCustomer().getName()));

				cell = dataRow.createCell(3);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				cell = dataRow.createCell(4);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				cell = dataRow.createCell(5);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				cell = dataRow.createCell(6);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				cell = dataRow.createCell(7);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				cell = dataRow.createCell(8);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				cell = dataRow.createCell(9);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());
				//快递
				cell = dataRow.createCell(10);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				cell = dataRow.createCell(11);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				cell = dataRow.createCell(12);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(13);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUpdateBy().getName());

				cell = dataRow.createCell(14);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(15);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(16);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(17);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				cell = dataRow.createCell(18);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellStyle(xStyle.get("redData"));
				cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0; i < tableTitle.length; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			xSheet.setColumnWidth(9,30*256);
			xSheet.setColumnWidth(10,30*256);
			xSheet.setColumnWidth(17,50*256);

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.length-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出Excel失败：" + e.getMessage());
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
	}

	//endregion

	//region 所有

	/**
	 * 所有列表
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/alllist")
	public String kefuAllList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/kefu/materialList/allList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
		if(!searchModel.getValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
		//状态：所有
		searchModel.setStatus(0);
		searchModel.setMerchRjectReasonAndRemark(true);//将驳回原因及详细描述合并在closeRemark供前端显示
		try {
			page = kefuOrderMaterialService.findKefuMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}


	/**
	 * 导出
	 */
	@RequestMapping(value = "/alllist/export",method = RequestMethod.POST)
	public String exportKefuAllList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		String viewForm = "modules/sd/kefu/materialList/allList";
		Page<MaterialMaster> page = new Page<>();
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return kefuAllList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					model.addAttribute(MODEL_ATTR_PAGE, page);
					model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
					return viewForm;
				}
			}
			searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
			if(!searchModel.getValid()){
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			//状态
			searchModel.setStatus(0);
			searchModel.setMerchRjectReasonAndRemark(true);//将驳回原因及详细描述合并在closeRemark供前端显示
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				page = kefuOrderMaterialService.findKefuMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			String[] tableTitle = new String[]
					{ "序号","订单号", "厂商", "状态", "类型", "配件类型", "产品",
							"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
							"完成人","完成时间","完成内容","跟踪状态","跟踪时间","跟踪内容","是否返件"
					};
			String xName = "所有配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			//headRow.setHeightInPoints(16);
			for (int i = 0,titleSize=tableTitle.length; i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle[i]);
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);

				cell = dataRow.createCell(0);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				cell = dataRow.createCell(1);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				cell = dataRow.createCell(2);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCustomer() == null?"":StringUtils.toString(item.getCustomer().getName()));

				cell = dataRow.createCell(3);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				cell = dataRow.createCell(4);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				cell = dataRow.createCell(5);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				cell = dataRow.createCell(6);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				cell = dataRow.createCell(7);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				cell = dataRow.createCell(8);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				cell = dataRow.createCell(9);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());
				//快递
				cell = dataRow.createCell(10);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				cell = dataRow.createCell(11);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				cell = dataRow.createCell(12);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(13);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCloseBy().getId() > 0 ? item.getCloseBy().getName() : "");
				cell = dataRow.createCell(14);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCloseBy().getId()>0?DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"):"");

				cell = dataRow.createCell(15);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCloseBy().getId()>0?item.getCloseRemark():"");

				cell = dataRow.createCell(16);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(17);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(18);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				cell = dataRow.createCell(19);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellStyle(xStyle.get("redData"));
				cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0; i < tableTitle.length; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			xSheet.setColumnWidth(9,30*256);
			xSheet.setColumnWidth(10,30*256);
			xSheet.setColumnWidth(15,50*256);
			xSheet.setColumnWidth(18,50*256);

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.length-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出Excel失败：" + e.getMessage());
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
	}
	//endregion

	//region 返件单

	/**
	 * 设置及初始化查询条件
	 * @param user  当前帐号
	 * @param searchModel   查询条件
	 * @param initMonths    初始最小查询时间段(月)
	 * @param byApplyDateRange by申请日期查询开关
	 * @param maxOrderDays   下单最大查询范围(天)
	 */
	private OrderMaterialSearchModel setReturnSearchModel(User user, OrderMaterialSearchModel searchModel, Model model, Page<MaterialReturn> page,int initMonths, boolean byApplyDateRange , int maxOrderDays) {
		if (searchModel == null) {
			searchModel = new OrderMaterialSearchModel();
		}

		if (searchModel.getStatus() == null || searchModel.getStatus() == 0) {
			searchModel.setStatus(MaterialMaster.STATUS_NEW);
		}

		Date now = new Date();
		//申请日期
		if(byApplyDateRange) {
			if (searchModel.getBeginDate() == null) {
				searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
				searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), 0 - initMonths)));
			} else {
				searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
			}
			//检查最大时间范围
			if(maxOrderDays > 0){
				Date maxDate = DateUtils.addDays(searchModel.getEndDate(),1-maxOrderDays);
				maxDate = DateUtils.getDateStart(maxDate);
				if(DateUtils.pastDays(searchModel.getBeginDate(),maxDate)>0){
					searchModel.setBeginDate(maxDate);
				}
				/*
				Date maxDate = DateUtils.addDays(searchModel.getBeginDate(),maxOrderDays-1);
				maxDate = DateUtils.getDateEnd(maxDate);
				if(DateUtils.pastDays(maxDate,searchModel.getEndDate())>0){
					searchModel.setEndDate(maxDate);
				}*/
			}
		}
		//if (!request.getMethod().equalsIgnoreCase("post")) {
		//	searchModel.setMaterialType(1);//配件申请
		//}
		Boolean isValide = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValide){
			searchModel.setValid(false);
		}
		String checkRegion = kefuOrderMaterialService.loadAndCheckUserRegions(searchModel,user);
		if(org.apache.commons.lang3.StringUtils.isNotEmpty(checkRegion)){
			addMessage(model, checkRegion);
			searchModel.setValid(false);
			return searchModel;
		}
		//客服主管
		boolean isServiceSupervisor = user.getRoleEnNames().contains("Customer service supervisor");

		//vip客服查询自己负责的单，by客户+区域+品类
		//1.by 客户，前端客户已按客服筛选了
		if(user.isKefu()) {
			KefuTypeEnum kefuTypeEnum = KefuTypeEnum.fromCode(user.getSubFlag());
			if(kefuTypeEnum!=null){
				searchModel.setCustomerType(kefuTypeEnum.getCustomerType());
				searchModel.setKefuType(kefuTypeEnum.getKefuType());
			}else{
				addMessage(model, "错误:读取客服类型错误");
				searchModel.setValid(false);
				return searchModel;
			}
		}else{
			//其他类型帐号，不限制客户及突击区域订单
			searchModel.setCustomerType(null);
			searchModel.setKefuType(null);
		}
		//2.by 区域
		if (isServiceSupervisor) {
			searchModel.setCreateBy(user);//*
		} else if (user.isKefu()) {
			searchModel.setCreateBy(user);//*,只有客服才按帐号筛选
		} else if (user.isInnerAccount()) { //内部帐号
			searchModel.setCreateBy(user);//*
		}
		return searchModel;
	}

	/**
	 * 点签收列表(旧件)
	 */
	@RequestMapping(value = "/waitSignMaterialReturnList")
	public String waitSignMaterialReturnList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/kefu/materialList/waitSignMaterialReturnList";
		Page<MaterialReturn> page = new Page<>();
		User user = UserUtils.getUser();
		searchModel = setReturnSearchModel(user,searchModel,model,page,3,true,365);
		if(!searchModel.getValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
		try {
			page = kefuOrderMaterialService.waitSignMaterialReturnList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}
	//endregion
}
