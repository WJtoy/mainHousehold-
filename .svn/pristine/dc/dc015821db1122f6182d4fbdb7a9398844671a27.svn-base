package com.wolfking.jeesite.ms.im.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.sys.IMNoticeInfo;
import com.kkl.kklplus.entity.sys.IMNoticeUser;
import com.kkl.kklplus.entity.sys.IMUserUtils;
import com.kkl.kklplus.entity.sys.vm.IMNoticeInfoSearchVM;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.BitUtils;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.Encodes;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.im.entity.IMNoticeModel;
import com.wolfking.jeesite.ms.im.entity.IMNoticeNewModel;
import com.wolfking.jeesite.ms.im.entity.mapper.IMNoticeMapper;
import com.wolfking.jeesite.ms.im.service.IMNoticeService;
import com.wolfking.jeesite.ms.service.push.APPMessagePushService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 站内即时消息之公告
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/im/notice")
public class IMNoticeController extends BaseController
{
	public static final List<NameValuePair> UserTypeList = Arrays.stream(IMNoticeInfo.UserType.values())
			.filter(t->t.getCode()>IMNoticeInfo.UserType.ALL.getCode())
			.filter(t->t.getCode()<IMNoticeInfo.UserType.PERSON.getCode())
			.map(p -> new NameValuePair(p.getName(),String.valueOf(p.getCode())))
			.collect(Collectors.toList());

	@Autowired
	private IMNoticeService imNoticeService;

	@Autowired
	private APPMessagePushService appMessagePushService;

	@Autowired
	private SequenceIdService sequenceIdService;

	//region 管理

	/**
	 * 新通知
	 */
	@RequiresPermissions("im:notice:new")
	@RequestMapping(value = "manage/new")
	public String newNoticeForm(IMNoticeNewModel notice,Model model)
	{
		if(notice == null || notice.getId() == null) {
			notice = new IMNoticeNewModel();
			long id = sequenceIdService.nextId();
			notice.setId(id);
		}
		IMNoticeInfo.UserType[] userTypes = IMNoticeInfo.UserType.values();
		for(int i=0,size=userTypes.length;i<size;i++){
			IMNoticeInfo.UserType userType = userTypes[i];
			if(userType.getCode()> IMNoticeInfo.UserType.ALL.getCode() && userType.getCode() < IMNoticeInfo.UserType.PERSON.getCode()){
				notice.getUserTypeList().add(new NameValuePair(userType.getName(),String.valueOf(userType.getCode())));
			}
		}
		model.addAttribute("canAction",true);
		model.addAttribute("notice",notice);
		return "modules/im/manage/newNoticeForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("im:notice:new")
	@RequestMapping(value = "manage/save")
	public String save(IMNoticeNewModel noticeNewModel, Model model, RedirectAttributes redirectAttributes)
	{
		if (!beanValidator(model, noticeNewModel))
		{
			return newNoticeForm(noticeNewModel, model);
		}
		try{
			User user = UserUtils.getUser();
			if(user==null || user.getId() == null || user.getId()<=0){
				addMessage(model, "登录超时,请重新登录");
				model.addAttribute("canAction",true);
				return newNoticeForm(noticeNewModel, model);
			}
			IMNoticeInfo notice = Mappers.getMapper(IMNoticeMapper.class).viewModelToBean(noticeNewModel);
			//user types
			List<Integer> types = noticeNewModel.getUserTypeValues();
			if(types == null || types.isEmpty() ){
				addMessage(model,"请选择通知对象！");
				model.addAttribute("canAction",true);
				return newNoticeForm(noticeNewModel, model);
			}
			int userTypes = 0;
			int userType;
			boolean sendToServicePoint = false;
			for(int i=0,size=types.size();i<size;i++){
				userType = types.get(i);
				userTypes = userTypes + (1<<userType);
				if(userType == IMNoticeInfo.UserType.SERVICEPOINT.getCode()){
					sendToServicePoint = true;
				}
			}
			notice.setUserTypes(userTypes);
			if(notice.getId() == null || notice.getId() <= 0){
				long id = sequenceIdService.nextId();
				notice.setId(id);
				noticeNewModel.setId(id);
			}
            notice.setContent(Encodes.unescapeHtml(notice.getContent()));//*,对html标签特殊处理
			//notice.setCreateAt(System.currentTimeMillis());
			notice.setCreateById(user.getId());
			notice.setCreateBy(user.getName());
			imNoticeService.saveNewNotice(notice);
			//app通知
			if(sendToServicePoint == true){
				appMessagePushService.broadcastToAllServicePoints("系统通知",notice.getTitle(),new Date());
			}
			addMessage(redirectAttributes, "通知已发布");
			return "redirect:/im/notice/manage/list";
		}catch (Exception e){
			addMessage(model,e.getMessage());
			model.addAttribute("canAction",true);
			return newNoticeForm(noticeNewModel, model);
		}
	}

	/**
	 * 公告列表
	 * @return
	 */
	@RequiresPermissions(value ="im:notice:manage")
	@RequestMapping(value = { "/manage/list", "/manage" })
	public String noticeList(IMNoticeInfoSearchVM notice, HttpServletRequest request, HttpServletResponse response, Model model){
		Page<IMNoticeModel> page = new Page<>(request,response);
        Date date;
		if(StringUtils.isBlank(notice.getStartAt())){
		    date = DateUtils.getDateEnd(new Date());
            notice.setEndAt(DateUtils.formatDate(date,"yyyy-MM-dd"));
			notice.setEndAtTime(date.getTime());
            date = DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1));
			notice.setStartAt(DateUtils.formatDate(date,"yyyy-MM-dd"));
			notice.setStartAtTime(date.getTime());
		}else {
		    date = DateUtils.parseDate(notice.getStartAt());
			notice.setStartAtTime(date.getTime());
			date = DateUtils.parseDate(notice.getEndAt());
            date = DateUtils.getDateEnd(date);
			notice.setEndAt(DateUtils.formatDate(date,"yyyy-MM-dd"));
			notice.setEndAtTime(date.getTime());
		}
		//notice.setIsCanceled(0);
		if(notice.getUserTypeValues() == null){
			notice.setUserTypeValues(Lists.newArrayList());
			notice.setUserTypes(0);
		}else{
			notice.setUserTypes(BitUtils.markedAndToTags(notice.getUserTypeValues()));
		}
		page = imNoticeService.getList(page,notice);
		model.addAttribute("notice",notice);
		model.addAttribute("page",page);
		model.addAttribute("userTypeList",UserTypeList);
		return "modules/im/manage/noticeList";
	}

	/**
	 * 重送通知
	 */
	@RequiresPermissions(value ="im:notice:resend")
	@ResponseBody
	@RequestMapping(value = "manage/resend", method = RequestMethod.POST)
	public AjaxJsonEntity resend(String id, HttpServletResponse response)
	{
		try
		{
			long noticeId = Long.parseLong(id);
			if(noticeId<=0){
				return AjaxJsonEntity.fail("参数不合法",null);
			}else {
				IMNoticeInfo notice = imNoticeService.getNoticeById(noticeId);
				if(notice == null){
					return AjaxJsonEntity.fail("读取通知错误",null);
				}
				boolean sendToServicePoint = false;
				List<Integer> posList = BitUtils.getPositions(Long.valueOf(notice.getUserTypes()).intValue(),Integer.class);
				int servicePointUserType = posList.stream()
						.filter(t->t.intValue() == IMNoticeInfo.UserType.SERVICEPOINT.getCode())
						.findFirst().orElse(0);
				if(servicePointUserType>0){
					sendToServicePoint = true;
				}
				//app通知
				if(sendToServicePoint == true){
					appMessagePushService.broadcastToAllServicePoints("系统通知",notice.getTitle(),new Date());
				}
				imNoticeService.resend(notice);
				return AjaxJsonEntity.success("通知重送成功",null);
			}
		} catch (Exception e)
		{
			return AjaxJsonEntity.fail(e.getMessage(),null);
		}
	}

	/**
	 * 撤销通知
	 */
	@RequiresPermissions(value ="im:notice:cancel")
	@ResponseBody
	@RequestMapping(value = "manage/cancel", method = RequestMethod.POST)
	public AjaxJsonEntity cancel(String id, HttpServletResponse response)
	{
		try
		{
			long noticeId = Long.parseLong(id);
			if(noticeId<=0){
				return AjaxJsonEntity.fail("参数不合法",null);
			}else {
				MSResponse<Boolean> msResponse = imNoticeService.cancel(noticeId);
				if(MSResponse.isSuccessCode(msResponse)){
					if(msResponse.getData() == true){
						return AjaxJsonEntity.success("通知撤销成功",null);
					}else{
						return AjaxJsonEntity.fail("通知撤销失败，请重试",null);
					}
				}else{
					return AjaxJsonEntity.fail(msResponse.getMsg(),null);
				}
			}
		} catch (Exception e)
		{
			return AjaxJsonEntity.fail(e.getMessage(),null);
		}
	}

	/**
	 * 查看通知
	 */
	@RequestMapping(value = "/manage/view", method = RequestMethod.GET)
	public String viewNotice(String id, HttpServletRequest request, Model model) {
		String viewForm = "modules/im/viewNotice";
		Long noticeId = Long.valueOf(id);

		if (noticeId == null || noticeId <= 0) {
			addMessage(model, "错误：参数无效");
			model.addAttribute("notice", null);
			return viewForm;
		}
		IMNoticeInfo notice = imNoticeService.getNoticeById(noticeId);
		if(notice == null){
			addMessage(model, "读取通知内容失败，请重试！");
			model.addAttribute("notice", null);
			return viewForm;
		}
		//notice.setContent(Encodes.unescapeHtml(notice.getContent()));
		model.addAttribute("notice", notice);
		return viewForm;
	}

	//endregion

	//region 个人

	/**
	 * 列表
	 */
	@RequestMapping(value = "user/list")
	public String userList(IMNoticeInfoSearchVM notice, HttpServletRequest request, HttpServletResponse response, Model model){
		String VIEW_NAME = "modules/im/user/userNoticeList";
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时！");
            return VIEW_NAME;
        }
        Integer userType = IMUserUtils.userTypeToReceiverType(user.getUserType());
        if(userType == null){
        	userType = IMNoticeInfo.UserType.SYSTEM_ACCOUNT.getCode();
		}

		Page page = new Page<>(request,response);
		notice.setUserId(user.getId());
        Date date;
        if(StringUtils.isBlank(notice.getStartAt())){
            date = DateUtils.getDateEnd(new Date());
            notice.setEndAt(DateUtils.formatDate(date,"yyyy-MM-dd"));
            notice.setEndAtTime(date.getTime());
            date = DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1));
            notice.setStartAt(DateUtils.formatDate(date,"yyyy-MM-dd"));
            notice.setStartAtTime(date.getTime());
        }else {
            date = DateUtils.parseDate(notice.getStartAt());
            notice.setStartAtTime(date.getTime());
            date = DateUtils.parseDate(notice.getEndAt());
            date = DateUtils.getDateEnd(date);
            notice.setEndAt(DateUtils.formatDate(date,"yyyy-MM-dd"));
            notice.setEndAtTime(date.getTime());
        }
        if(userType == IMNoticeInfo.UserType.SERVICEPOINT.getCode()){
            //网点：查看公共(通知对象包含：网点)
            VIEW_NAME = "modules/im/user/servicePointNoticeList";
            page = imNoticeService.getListForServicePoint(page,notice);
        }else {
            //其他帐号：查看个人收到的通知，包含在线和离线
            notice.setUserId(user.getId());
            page = imNoticeService.getUserNoticeList(page, notice);
        }
		model.addAttribute("notice",notice);
		model.addAttribute("page",page);

		return VIEW_NAME;
	}

	/**
	 * 查看通知
	 */
	@RequestMapping(value = "/user/view", method = RequestMethod.GET)
	public String userViewNotice(String id,HttpServletRequest request, Model model) {
		String viewForm = "modules/im/viewNotice";
		User user = UserUtils.getUser();
		if (user == null || user.getId() == null) {
			addMessage(model, "错误：登录超时！");
			model.addAttribute("notice", null);
			return viewForm;
		}
		Long rowId = Long.valueOf(id);
		if (rowId == null || rowId <= 0) {
			addMessage(model, "错误：参数无效");
			model.addAttribute("notice", null);
			return viewForm;
		}
		IMNoticeUser notice = imNoticeService.getUserNoticeById(rowId);
		if(notice == null || notice.getNotice() == null){
			addMessage(model, "读取通知内容失败，请重试！");
			model.addAttribute("notice", null);
			return viewForm;
		}
		if(notice.getIsReaded() == 0) {
			try {
				imNoticeService.markUserNoticeReaded(user.getId(), rowId);
			} catch (Exception e) {
				log.error("用户:{} 的通知:{} 标记已读错误",user.getId(),id,e);
			}
		}
		model.addAttribute("notice", notice.getNotice());
		return viewForm;
	}

	//endregion

    //region client

    @ResponseBody
    @RequestMapping(value = "/client/onlineList", method = RequestMethod.GET)
    public AjaxJsonEntity onlineClients(@RequestParam("withList") String withList, HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        MSResponse<Map> msResponse = imNoticeService.onlineClients(withList);
        if(MSResponse.isSuccessCode(msResponse)){
            result.setData(msResponse.getData());
        }else{
            result.setSuccess(false);
            result.setMessage(msResponse.getMsg());
        }
        return result;
    }

    //endregion

}
