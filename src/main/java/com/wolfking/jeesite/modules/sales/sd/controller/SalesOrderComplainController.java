package com.wolfking.jeesite.modules.sales.sd.controller;


import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.BitUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.*;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.VisibilityFlagEnum;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 网点工单信息
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/sales/sd/complain/")
public class SalesOrderComplainController extends BaseController {


    @Autowired
    private OrderComplainService orderComplainService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private ServicePointService servicePointService;



    private static final String MODEL_ATTR_ENTITY = "complain";

    /**
     * [Ajax]订单日志-投诉列表
     * @param orderId	订单id
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/list")
    public AjaxJsonEntity orderComplainList(@RequestParam String orderId, @RequestParam String quarter, HttpServletResponse response)
    {
        response.setContentType("application/json; charset=UTF-8");
        User user = UserUtils.getUser();
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        if(user == null || user.getId()==null){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("登录已超时");
            return jsonEntity;
        }
        try
        {
            Long lorderId = Long.valueOf(orderId);
            if(lorderId == null || lorderId <=0){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("订单参数错误");
                return jsonEntity;
            }
            List<OrderComplain> list = orderComplainService.getComplainListByOrder(lorderId,"",quarter,true);
            if(list ==null){
                jsonEntity.setData(Lists.newArrayList());
            }else {
                Order order = orderService.getOrderById(lorderId, quarter,OrderUtils.OrderDataLevel.DETAIL,true);
                if(order!=null){
                    for(OrderComplain orderComplain:list){
                        orderComplain.setDataSource(order.getDataSourceId());
                    }
                }
                jsonEntity.setData(list);
            }
        } catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 新增/修改窗口
     * @param orderId	订单id
     */
   /* @RequiresPermissions(value="sd:complain:create")*/
    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String form(@RequestParam(required = false) String id,@RequestParam String orderId, @RequestParam(required = false) String quarter, Model model) {
        User user = UserUtils.getUser();
        Long lid = null;
        OrderComplain complain = new OrderComplain();
        complain.setProductCategoryId(0L);
        String formView = "modules/sales/sd/complain/complainForm";
        if(StringUtils.isBlank(id) && StringUtils.isBlank(orderId)){
            addMessage(model, "参数为空。");
            model.addAttribute("canAction", false);
            model.addAttribute(MODEL_ATTR_ENTITY,complain);
            model.addAttribute("hasOpenForm",false);//已有处理中投诉单标记
            return formView;
        }

        Long lorderId = Long.valueOf(orderId);
        if(StringUtils.isBlank(id)){

            Order order = orderService.getOrderById(lorderId,quarter, OrderUtils.OrderDataLevel.CONDITION,true);
            if (order == null || order.getOrderCondition() == null) {
                addMessage(model, "订单读取失败，或不存在。请重试");
                model.addAttribute("canAction", false);
                model.addAttribute(MODEL_ATTR_ENTITY,complain);
                model.addAttribute("hasOpenForm",false);
                return formView;
            }else{
                //check complain
                List<OrderComplain> complains = orderComplainService.getComplainListByOrder(lorderId,order.getOrderNo(),order.getQuarter(),true);
                if(complains != null && complains.size()>0){
                    OrderComplain openComplain = complains.stream().filter(t->Integer.valueOf(t.getStatus().getValue())<=1 || t.getStatus().getValue().equals("3")).findFirst().orElse(null);
                    if(openComplain != null){
                        model.addAttribute("canAction", false);
                        model.addAttribute(MODEL_ATTR_ENTITY,complain);
                        model.addAttribute("list",complains);
                        model.addAttribute("hasOpenForm",true);
                        return formView;
                    }
                }
                lid = SeqUtils.NextID();
                complain.setId(lid);
                complain.setAction(0);//new
                complain.setOrderId(lorderId);
                complain.setQuarter(order.getQuarter());
                complain.setOrderNo(order.getOrderNo());
                complain.setUserName(order.getOrderCondition().getUserName());
                complain.setUserPhone(order.getOrderCondition().getServicePhone());
                complain.setUserAddress(order.getOrderCondition().getArea().getName()+order.getOrderCondition().getServiceAddress());
                complain.setArea(order.getOrderCondition().getArea());
                complain.setCustomer(order.getOrderCondition().getCustomer());
                complain.setComplainBy(user.getName());
                complain.setKefu(order.getOrderCondition().getKefu());
                complain.setProductCategoryId(order.getOrderCondition().getProductCategoryId()==null?0L:order.getOrderCondition().getProductCategoryId());//2019-10-14
                //List<ServicePoint> servicePoints = orderService.getSetProductIdIncludeMe()
                //complain.setServicePoint(order.getOrderCondition().getServicePoint());
                complain.setComplainDate(new Date());
                model.addAttribute("canAction", true);
                model.addAttribute(MODEL_ATTR_ENTITY,complain);
                model.addAttribute("hasOpenForm",false);
                return formView;
            }
        }else{
            //edit
            try {
                lid = Long.valueOf(id);
            }catch (Exception e){
                lid = 0l;
            }
            if(lid<=0){
                addMessage(model, "投诉单参数类型错误。");
                model.addAttribute("canAction", false);
                model.addAttribute(MODEL_ATTR_ENTITY,complain);
                model.addAttribute("hasOpenForm",false);
                return formView;
            }
            complain = orderComplainService.getComplain(lid,quarter);
            if(complain ==null){
                addMessage(model, "读取投诉单失败或不存在，请重试。");
                model.addAttribute("canAction", false);
                model.addAttribute("hasOpenForm",false);
                complain=new OrderComplain();
            }
            else{
                if(!complain.getStatus().getValue().equalsIgnoreCase(OrderComplain.STATUS_APPLIED.toString())){
                    addMessage(model, "投诉单已在处理中，不能修改申请内容。");
                    model.addAttribute("canAction", false);
                    model.addAttribute(MODEL_ATTR_ENTITY,complain);
                    model.addAttribute("hasOpenForm",false);
                    return formView;
                }
                complain.setAction(1);//修改
                List<OrderComplainAttachment> attachments = orderComplainService.getComplainAttachements(lid,quarter,0);
                if(attachments != null && attachments.size()>0){
                    OrderComplainAttachment attachment;
                    for(int i=0,size=attachments.size();i<size;i++){
                        attachment = attachments.get(i);
                        attachment.setStrId(attachment.getId().toString());
                    }
                }
                if(attachments != null){
                    complain.setApplyAttaches(attachments);
                }
                //投诉对象complain_object
                if(complain.getComplainObject()>0){
                    complain.setComplainObjectsIds(BitUtils.getPositions(complain.getComplainObject(),String.class));
                }
                //投诉项目complain_item
                if(complain.getComplainItem()>0){
                    complain.setComplainItemsIds(BitUtils.getPositions(complain.getComplainItem(),String.class));
                }
                Customer customer = customerService.getFromCache(complain.getCustomer().getId());
                complain.setCustomer(customer);
                model.addAttribute("hasOpenForm",false);
                model.addAttribute("canAction", true);
            }
            model.addAttribute(MODEL_ATTR_ENTITY,complain);
            model.addAttribute("hasOpenForm",false);
            return formView;
        }
    }

    /**
     * 提交投诉申请
     */
    @RequiresPermissions("sd:complain:create")
    @ResponseBody
    @RequestMapping(value = "/save")
    public AjaxJsonEntity save(OrderComplain complain, HttpServletRequest request, HttpServletResponse response, Model model) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        if(complain == null){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("提交表单为空");
            return jsonEntity;
        }
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("登录超时，请重新登录。");
            jsonEntity.setLogin(false);
            return jsonEntity;
        }
        //System.out.println(GsonUtils.getInstance().toGson(complain));
        if (!beanValidator(model, complain))
        {
            jsonEntity.setSuccess(false);
            if (model.containsAttribute("message"))
            {
                jsonEntity.setMessage((String) model.asMap().get("message"));
            } else
            {
                jsonEntity.setMessage("输入错误，请检查。");
            }
            return jsonEntity;
        }
        Order order = orderService.getOrderById(complain.getOrderId(), complain.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
        OrderCondition orderCondition = order.getOrderCondition();
        if (order == null || orderCondition == null) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("读取订单错误，请重试!");
            return jsonEntity;
        }
        try{
            if(complain.getAction()==0){
                //new
                Area area = areaService.getFromCache(complain.getArea().getId());
                if(area != null) {
                    List<String> ids = Splitter.onPattern(",")
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(area.getParentIds());
                    if (ids.size() >= 2) {
                        complain.setCity(new Area(Long.valueOf(ids.get(ids.size() - 1))));
                        complain.setProvince(new Area(Long.valueOf(ids.get(ids.size() - 2))));
                    }else{
                        jsonEntity.setSuccess(false);
                        jsonEntity.setMessage("读取区域所属省/市错误");
                        return jsonEntity;
                    }
                }else{
                    jsonEntity.setSuccess(false);
                    jsonEntity.setMessage("无区县信息");
                    return jsonEntity;
                }
                complain.setStatus(new Dict(OrderComplain.STATUS_APPLIED.toString(),"待处理"));
                String no = SeqUtils.NextSequenceNo("ComplainNo");
                if(StringUtils.isBlank(no)){
                    no =  SeqUtils.NextSequenceNo("ComplainNo");
                }
                complain.setComplainNo(no);
            }
            UserUtils.substrUserName(user,20);
            complain.setCreateBy(user);
            complain.setCreateDate(new Date());
            complain.setUpdateBy(user);
            complain.setUpdateDate(new Date());
            complain.setOrderStatus(orderCondition.getStatus());
            complain.setCanRush(orderCondition.getCanRush());
            complain.setCreateType(OrderComplain.CREATE_TYPE_MANUANL);
            complain.setKefuType(orderCondition.getKefuType());
            orderComplainService.saveComplainApply(complain,order.getOrderStatus());
        }catch (Exception e){
            if(complain.getAction()==0 && StringUtils.isNotBlank(complain.getComplainNo())){
                try{
                    SeqUtils.reputSequenceNo("ComplainNo",complain.getCreateDate(),complain.getComplainNo());
                }catch (Exception e1){}
            }
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("保存投诉单失败:" + e.getMessage());
            log.error("[ComplainController.save] orderNo:{} ,userId:{}",complain.getOrderNo(),user.getId(),e);
            LogUtils.saveLog("投诉单-申请","ComplainController.save",complain.getOrderNo(),e,user);
        }
        return jsonEntity;
    }

    /**
     * [Ajax]撤销投诉单
     * @param complainId	投诉单id
     * @param quarter 分片
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/cancleComplain")
    public AjaxJsonEntity cancleComplain(@RequestParam String complainId, @RequestParam String quarter, HttpServletResponse response)
    {
        response.setContentType("application/json; charset=UTF-8");
        User user = UserUtils.getUser();
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        if(user == null || user.getId()==null){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("登录已超时");
            return jsonEntity;
        }
        try
        {
            Long LcomplainId = Long.valueOf(complainId);
            if(LcomplainId == null || LcomplainId <=0){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("投诉单参数错误");
                return jsonEntity;
            }

            orderComplainService.cancleComplain(LcomplainId,quarter,user);

        } catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 申诉申请窗口
     * @param id	投诉单id
     */
    @RequiresPermissions(value="sd:complain:create")
    @RequestMapping(value = "/appealForm", method = RequestMethod.GET)
    public String appealForm(@RequestParam(required = false) String id,
                             @RequestParam(required = false) String complainNo,
                             @RequestParam(required = false) String quarter, Model model) {
        User user = UserUtils.getUser();
        OrderComplainLog complainlog = new OrderComplainLog();
        String formView = "modules/sales/sd/complain/appealForm";

        Long lid = null;
        try{
            lid = Long.valueOf(id);
            complainlog.setComplainId(lid);
            complainlog.setQuarter(quarter);
            model.addAttribute("canAction", true);
            model.addAttribute("complainlog", complainlog);
            model.addAttribute("complainNo", complainNo);
        }catch (Exception e){
            model.addAttribute("canAction", false);
            addMessage(model,"错误：参数错误");
        }

        return formView;
    }

    /**
     * 提交申诉
     */
    @RequiresPermissions(value="sd:complain:create")
    @ResponseBody
    @RequestMapping(value = "/ajax/appealSave")
    public AjaxJsonEntity appealSave(OrderComplainLog complainlog, HttpServletRequest request, HttpServletResponse response, Model model) {
        response.setContentType("application/json; charset=UTF-8");
        User user = UserUtils.getUser();
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        if(user == null || user.getId()==null){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("登录已超时");
            return jsonEntity;
        }
        try
        {
            UserUtils.substrUserName(user,30);
            complainlog.setCreateBy(user);
            complainlog.setCreateDate(new Date());
            Dict status =new Dict(OrderComplain.STATUS_APPEAL,"申诉");
            complainlog.setStatus(status);
            orderComplainService.saveAppeal(complainlog);

        } catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 浏览窗口
     * @param id		投诉单id
     * @param quarter 	分片
     */
    @RequiresPermissions(value={"sd:complain:complete","sd:complain:judge","sd:complain:create","sd:complain:view"},logical = Logical.OR)
    @RequestMapping(value = "/view", method = RequestMethod.GET)
    public String viewForm(@RequestParam String id, @RequestParam(required = false) String quarter, Model model) {
        User user = UserUtils.getUser();
        OrderComplain complain = new OrderComplain();
        String formView = "modules/sales/sd/complain/viewForm";
        if(StringUtils.isBlank(id)){
            addMessage(model, "参数为空。");
            model.addAttribute("canAction", false);
            model.addAttribute(MODEL_ATTR_ENTITY,complain);
            return formView;
        }

        Long lid = null;
        try {
            lid = Long.valueOf(id);
        }catch (Exception e){
            lid = 0l;
        }
        if (lid == null || lid <= 0){
            addMessage(model, "投诉单参数类型错误。");
            model.addAttribute("canAction", false);
            model.addAttribute(MODEL_ATTR_ENTITY,complain);
            return formView;
        }
        try {
            complain = orderComplainService.getComplain(lid, quarter);
        }catch (Exception e){
            addMessage(model, "投诉单读取错误。");
            model.addAttribute("canAction", false);
            model.addAttribute(MODEL_ATTR_ENTITY,complain);
            return formView;
        }
        if(complain ==null){
            addMessage(model, "读取投诉单失败或不存在，请重试。");
            model.addAttribute("canAction", false);
            complain=new OrderComplain();
            return formView;
        }

        if(complain.getAttachmentQty()>0) {
            List<OrderComplainAttachment> attachments = orderComplainService.getComplainAttachements(lid, quarter, 0);
            if (attachments != null && attachments.size() > 0) {
                complain.setApplyAttaches(attachments);
            }
        }

        //加载判定附件
        List<OrderComplainAttachment> judgeattachments = orderComplainService.getComplainAttachements(lid, quarter, OrderComplainAttachment.ATTACHMENTTYPE_JUDEG);
        if (judgeattachments != null && judgeattachments.size() > 0) {
            complain.setJudgeAttaches(judgeattachments);
        }
        //status
        //Dict dict = MSDictUtils.getDictByValue(complain.getStatus().getValue(),"complain_status");//切换为微服务
        Dict dict = getStatus(String.valueOf(complain.getStatus().getValue()));
        if (dict != null) {
            complain.setStatus(dict);
        }
        //投诉对象 complain_object
        if(complain.getComplainObject()>0){
            complain.setComplainObjectsIds(BitUtils.getPositions(complain.getComplainObject(),String.class));
            if(complain.getComplainObjectsIds() != null && complain.getComplainObjectsIds().size() > 0) {
                complain.setComplainObjects(MSDictUtils.getDictInclueList("complain_object", Joiner.on(",").join(complain.getComplainObjectsIds())));
            }
        }
        //投诉项目 complain_item
        if(complain.getComplainItem()>0){
            complain.setComplainItemsIds(BitUtils.getPositions(complain.getComplainItem(),String.class));
            if(complain.getComplainItemsIds() != null && complain.getComplainItemsIds().size() > 0) {
                complain.setComplainItems(MSDictUtils.getDictInclueList("complain_item",Joiner.on(",").join(complain.getComplainItemsIds())));
            }
        }
        Customer customer = customerService.getFromCache(complain.getCustomer().getId());
        complain.setCustomer(customer);

        //判定
        //对象
        if(complain.getJudgeObject()>0){
            complain.setJudgeObjectsIds(BitUtils.getPositions(complain.getJudgeObject(),String.class));
            if(complain.getJudgeObjectsIds() != null && complain.getJudgeObjectsIds().size() > 0) {
                complain.setJudgeObjects(MSDictUtils.getDictInclueList("judge_object",Joiner.on(",").join(complain.getJudgeObjectsIds())));
            }
        }
        //test
        //complain.setJudgeObjectsIds(Lists.newArrayList("3"));
        //项目
        if(complain.getJudgeItem()>0){
            complain.setJudgeItemsIds(BitUtils.getPositions(complain.getJudgeItem(),String.class));
        }
        //判定
        //servicepoint
        ServicePoint servicePoint = complain.getServicePoint();
        if(servicePoint!= null && servicePoint.getId() != null && servicePoint.getId()>0){
            servicePoint = servicePointService.getFromCache(servicePoint.getId());
            if(servicePoint != null){
                complain.setServicePoint(servicePoint);
                complain.getServicePoints().add(servicePoint);
            }
        }
        //complete_result
        if(complain.getCompleteResult()>0){
            complain.setCompleteResultIds(BitUtils.getPositions(complain.getCompleteResult(),String.class));
            if(complain.getCompleteResultIds() != null && complain.getCompleteResultIds().size() > 0) {
                complain.setCompleteResults(MSDictUtils.getDictInclueList("complete_result",Joiner.on(",").join(complain.getCompleteResultIds())));
            }
        }
        model.addAttribute("canAction", true);
        model.addAttribute(MODEL_ATTR_ENTITY,complain);
        return formView;
    }

    private Dict getStatus(String statusValue){
        Dict dict = null;
        try{
            dict = MSDictUtils.getDictByValue(statusValue,"complain_status");//切换为微服务
        }catch (Exception e){
            log.error("读取数据字典错误,type:{},value:{}","complain_status",statusValue,e);
        }
        if(dict==null) {
            dict = new Dict(statusValue);
            int intValue = 0;
            try {
                intValue = Integer.parseInt(statusValue);
                switch (intValue) {
                    case 0:
                        dict.setLabel("待处理");
                        break;
                    case 1:
                        dict.setLabel("处理中");
                        break;
                    case 2:
                        dict.setLabel("已关闭");
                        break;
                    case 3:
                        dict.setLabel("已申诉");
                        break;
                    case 4:
                        dict.setLabel("已撤销");
                        break;
                    default:
                        dict.setLabel("状态错误");
                        break;
                }
            } catch (Exception e) {
                dict.setValue("0");
                dict.setLabel("状态错误");
            }
        }
        return dict;
    }

    /**
     * [Ajax]订单日志-投诉列表
     * @param complainId	投诉单id
     * @quarter 分片
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/complainLogList")
    public AjaxJsonEntity complainLogList(@RequestParam String complainId, @RequestParam String quarter, HttpServletResponse response)
    {
        response.setContentType("application/json; charset=UTF-8");
        User user = UserUtils.getUser();
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        if(user == null || user.getId()==null){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("登录已超时");
            return jsonEntity;
        }
        try
        {
            Long lorderId = Long.valueOf(complainId);
            if(lorderId == null || lorderId <=0){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("投诉单参数错误");
                return jsonEntity;
            }

            List<OrderComplainLog> list = orderComplainService.getComplainLogListByCompliaId(lorderId,quarter);
            if(list ==null){
                jsonEntity.setData(Lists.newArrayList());
            }else {
                //若当前用户是客户，则只显示客户可见的投诉单日志
                if (user.isCustomer()) {
                    list = list.stream().filter(i-> i.getVisibilityFlag() == 0 || VisibilityFlagEnum.has(i.getVisibilityFlag(), VisibilityFlagEnum.CUSTOMER)).collect(Collectors.toList());
                }

                List<Dict> status = MSDictUtils.getDictList("complain_status");//切换为微服务
                OrderComplainLog complainlog;
                List<Dict> dictList;
                final StringBuffer buffer = new StringBuffer();
                Dict dict;
                for(int i=0,size=list.size();i<size;i++){
                    complainlog = list.get(i);
                    //status
                    buffer.setLength(0);
                    buffer.append(complainlog.getStatus().getValue());
                    dict = status.stream().filter(t->t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                    if(dict != null){
                        complainlog.setStatus(dict);
                    }
                }
                jsonEntity.setData(list);
            }
        } catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 投诉单详情查看工单明细
     * @param id	订单id
     * @return
     */
    @RequestMapping(value = { "/orderDetailInfo" })
    public String kefuOrderDetailInfo(@RequestParam String id,@RequestParam String quarter,String refreshParent, HttpServletRequest request,HttpServletResponse response, Model model)
    {
        Boolean errorFlag = false;
        Order order = new Order();
        Long lid = Long.valueOf(id);
        if (lid == null || lid <= 0)
        {
            errorFlag = true;
            addMessage(model, "订单代码传递错误");
        } else
        {
            order = orderService.getOrderById(lid, quarter,OrderUtils.OrderDataLevel.DETAIL,true);
            if(order == null || order.getOrderCondition() == null){
                errorFlag = true;
                addMessage(model,"错误：读取订单失败，请重试!");
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
        model.addAttribute("refreshParent",StringUtils.isBlank(refreshParent)?"true":refreshParent);//调用方法决定是否在关闭详情页后刷新iframe
        String changed = request.getParameter("changed");
        model.addAttribute("changed",StringUtils.isBlank(changed)?"false":changed);
        return "modules/sales/sd/complain/complainOrderDefailInfoForm";
    }

}

