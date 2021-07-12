/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Material;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.MaterialService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.sd.dao.OrderMaterialLogDao;
import com.wolfking.jeesite.modules.sd.dao.OrderMaterialReturnDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderMaterialSearchModel;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单返件件管理服务
 * 包含返件单及跟踪进度
 *
 * @author Ryan
 * @date 2019-06-28
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderMaterialReturnService extends LongIDBaseService {

    /**
     * 持久层对象
     */
    @Autowired
    protected OrderMaterialLogDao logDao;

    @Autowired
    private OrderMaterialReturnDao dao;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private KefuOrderMaterialService kefuOrderMaterialService;

    //region 返件单

    @Transactional
    public void insertMaterialReturn(MaterialReturn materialReturn){
        // 单头
        dao.insertMaterialReturn(materialReturn);
        // 单身
        for(MaterialReturnItem item:materialReturn.getItems()){
            dao.insertMaterialReturnItem(item);
        }
        // 图片
        if(!ObjectUtils.isEmpty(materialReturn.getAttachments())) {
            for (MaterialAttachment attachment : materialReturn.getAttachments()) {
                dao.insertMaterialMasterAttachMap(materialReturn.getId(), attachment.getId(), materialReturn.getQuarter());
            }
        }
    }

    /**
     * 按订单id读取返件单单头列表(不包含配件列表)

    public List<MaterialReturn> findMaterialReturnHeadListByOrderId(Long orderId, String quarter, Integer withOrder,Integer withPending,Integer withReceivor,Integer withClose) {
        List<MaterialReturn> list = dao.findMaterialReturnHeadListByOrderId(orderId, quarter, withOrder,withPending,withReceivor,withClose);
        if (list.size() > 0) {
            Map<String, Dict> expressTypeMap = MSDictUtils.getDictMap("express_type");
            Map<String, Dict> statusMap = MSDictUtils.getDictMap("material_apply_status");
            Map<Long,Product> productMap = Maps.newHashMap();
            Product product;
            //user微服务
            List<Long> userIds = list.stream().filter(i -> i.getCreateBy() != null && i.getCreateBy().getId() != null)
                    .map(i -> i.getCreateBy().getId()).distinct().collect(Collectors.toList());
            Map<Long, String> nameMap = MSUserUtils.getNamesByUserIds(userIds);
            for (MaterialReturn item : list) {
                if (item.getExpressCompany() != null && item.getExpressCompany().getValue() != null) {
                    Dict expressTypeDict = expressTypeMap.get(item.getExpressCompany().getValue());
                    item.getExpressCompany().setLabel(expressTypeDict != null ? expressTypeDict.getLabel() : "");
                }
                if (item.getStatus() != null && StringUtils.toInteger(item.getStatus().getValue()) > 0) {
                    Dict statusDict = statusMap.get(item.getStatus().getValue());
                    item.getStatus().setLabel(statusDict != null ? statusDict.getLabel() : "");
                }
                if (item.getCreateBy() != null && item.getCreateBy().getId() != null) {
                    item.getCreateBy().setName(StringUtils.toString(nameMap.get(item.getCreateBy().getId())));
                }
                product = productMap.get(item.getProduct().getId());
                if(product == null){
                    product = productService.getProductByIdFromCache(item.getProduct().getId());
                    if(product != null){
                        item.setProduct(product);
                        productMap.put(product.getId(),product);
                    }
                }else{
                    item.setProduct(product);
                }
            }
        }
        return list;
    }*/


    /**
     * 按id获得返件单信息
     * 包含单身及附件
     * @param id            返件单id
     * @param masterId      配件单id
     * @param quarter       分片
     * @param withOrder     返回订单基本信息
     * @param withPending   返回跟踪信息
     * @param withReceivor  返回收件信息
     * @param withClose     返回关闭信息
     * @return
     */
    public MaterialReturn getMaterialReturnById(Long id,Long masterId, String quarter, Integer withOrder,Integer withPending,Integer withReceivor,Integer withClose) {
        MaterialReturn materialReturn = dao.getReturnFormById(id, masterId,quarter, withOrder,withPending,withReceivor,withClose);
        loadMaterialReturnInfo(materialReturn,true);
        if(materialReturn != null) {
            //图片
            List<MaterialAttachment> attachements = dao.findAttachementsByReturnId(materialReturn.getId(), materialReturn.getQuarter());
            if (attachements == null) {
                attachements = Lists.newArrayList();
            }
            materialReturn.setAttachments(attachements);
        }
        return materialReturn;
    }

    /**
     * 按id获得返件单信息，包含单身
     * 不包含图片
     * @param id            返件单id
     * @param masterId      配件单id
     * @param quarter       分片
     * @param withOrder     返回订单基本信息
     * @param withPending   返回跟踪信息
     * @param withReceivor  返回收件信息
     * @param withClose     返回关闭信息
     * @return
     */
    public MaterialReturn getMaterialReturnNoAttachById(Long id,Long masterId, String quarter, Integer withOrder,Integer withPending,Integer withReceivor,Integer withClose) {
        MaterialReturn materialReturn = dao.getReturnFormById(id, masterId,quarter, withOrder,withPending,withReceivor,withClose);
        //切换为微服务
        loadMaterialReturnInfo(materialReturn,true);
        return materialReturn;
    }

    /**
     * 按id获得返件单头信息
     *
     * @param id            返件单id
     * @param masterId      配件单id
     * @param quarter       分片
     * @param withOrder     返回订单基本信息
     * @param withPending   返回跟踪信息
     * @param withReceivor  返回收件信息
     * @param withClose     返回关闭信息
     */
    public MaterialReturn getMaterialReturnHeadById(Long id, Long masterId,String quarter, Integer withOrder,Integer withPending,Integer withReceivor,Integer withClose) {
        MaterialReturn materialReturn = dao.getMaterialReturnHeadById(id, masterId,quarter, withOrder,withPending,withReceivor,withClose);
        loadMaterialReturnInfo(materialReturn,false);
        return materialReturn;
    }

    /**
     * 读取订单返件状态信息，判断是否可客评
     * 包含id,status,masterId
     */
    public List<MaterialReturn> getMaterialReturnListForGrade(@Param("orderId") Long orderId, @Param("quarter") String quarter) {
        return dao.getMaterialReturnListForGrade(orderId,quarter);
    }

    /**
     * 返件单图片列表
     * @param returnId  返件单id
     * @param quarter   数据分片
     * @return
     */
    public List<MaterialAttachment> findAttachementsByReturnId(@Param("returnId") Long returnId, @Param("quarter") String quarter){
        return dao.findAttachementsByReturnId(returnId,quarter);
    }

    /**
     * 更新返件申请单-物流信息
     * 必须要有快递单号
     * 2019/06/28 返件不订阅物流
     *
     */
    @Transactional(readOnly = false)
    public void updateMaterialReturnExpressInfo(Order order, MaterialReturn materialReturn) {
        if (order == null) {
            order = orderService.getOrderById(materialReturn.getOrderId(), materialReturn.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
        }
        if (order == null || order.getOrderCondition() == null) {
            throw new OrderException("读取订单单据失败，请重试");
        }
        //检查，返件申请快递必须填写
        if (StringUtils.isBlank(materialReturn.getExpressCompany().getValue()) || StringUtils.isBlank(materialReturn.getExpressNo())) {
            throw new OrderException("快递信息不完整，请输入完整后再保存");
        }
        //update 申请单
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(20);
        params.put("quarter", order.getQuarter());
        params.put("id", materialReturn.getId());
        params.put("expressCompany", materialReturn.getExpressCompany());
        params.put("expressNo", materialReturn.getExpressNo());
        params.put("sendBy", materialReturn.getUpdateBy());
        params.put("sendDate", materialReturn.getUpdateDate());
        params.put("updateBy", materialReturn.getUpdateBy());
        params.put("updateDate", materialReturn.getUpdateDate());
        params.put("status", new Dict(MaterialMaster.STATUS_SENDED, "已发货"));
        params.put("receiverAreaId",materialReturn.getReceiverAreaId());
        params.put("receivor",materialReturn.getReceivor());
        params.put("receivorPhone",materialReturn.getReceivorPhone());
        params.put("receivorAddress",materialReturn.getReceivorAddress());
        Area area = areaService.getFromCache(materialReturn.getReceiverAreaId());
        if (area != null) {
            List<String> ids = Splitter.onPattern(",")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(area.getParentIds());
            if (ids.size() >= 2) {
                params.put("receiverCityId",Long.valueOf(ids.get(ids.size() - 1)));
                params.put("receiverProvinceId",Long.valueOf(ids.get(ids.size() - 2)));
            }
        }
        dao.updateMaterialReturn(params);

        //订单log
        OrderProcessLog processLog = new OrderProcessLog();
        processLog.setQuarter(order.getQuarter());
        processLog.setAction("返件发货");
        processLog.setOrderId(order.getId());
        processLog.setActionComment(String.format("返件发货,操作人:%s", materialReturn.getUpdateBy().getName()));
        processLog.setStatus(order.getOrderCondition().getStatus().getLabel());
        processLog.setStatusValue(order.getOrderCondition().getStatusValue());
        processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
        processLog.setCloseFlag(0);
        processLog.setCreateBy(materialReturn.getUpdateBy());
        processLog.setCreateDate(materialReturn.getUpdateDate());
        processLog.setCustomerId(order.getOrderCondition().getCustomerId());
        processLog.setDataSourceId(order.getDataSourceId());
        orderService.saveOrderProcessLogNew(processLog);
    }

    /**
     * 更新返件申请单-物流信息 For app
     * 必须要有快递单号
     * 2018/05/15,状态为：已发货
     * 2019/06/28 返件不订阅物流
     */
    @Transactional(readOnly = false)
    public void updateMaterialReturnApplyExpressForApp(Order order, MaterialReturn materialReturn) {
        if (order == null || order.getOrderCondition() == null) {
            throw new OrderException("读取订单单据失败");
        }
        //检查，返件申请快递必须填写
        if (StringUtils.isBlank(materialReturn.getExpressCompany().getValue()) || StringUtils.isBlank(materialReturn.getExpressNo())) {
            throw new OrderException("快递信息不完整，请输入完整后再保存");
        }
        //update 返件单
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(10);
        params.put("quarter", order.getQuarter());
        params.put("id", materialReturn.getId());
        params.put("expressCompany", materialReturn.getExpressCompany());
        params.put("expressNo", materialReturn.getExpressNo());
        params.put("updateBy", materialReturn.getUpdateBy());
        params.put("updateDate", materialReturn.getUpdateDate());
        params.put("sendDate", materialReturn.getUpdateDate());
        params.put("status", new Dict(MaterialMaster.STATUS_SENDED, "已发货"));//2018/05/15
        dao.updateMaterialReturn(params);

        //attachment
        if (materialReturn.getAttachments() != null && materialReturn.getAttachments().size() > 0) {
            List<MaterialAttachment> attachments = materialReturn.getAttachments();
            MaterialAttachment attachment;
            Long attcId;
            for (int k = 0, ksize = attachments.size(); k < ksize; k++) {
                attachment = attachments.get(k);
                dao.insertMaterialAttach(attachment);
                //关系表
                dao.insertMaterialMasterAttachMap(materialReturn.getId(), attachment.getId(), materialReturn.getQuarter());
            }
        }

        //订单log
        OrderProcessLog processLog = new OrderProcessLog();
        processLog.setQuarter(order.getQuarter());
        processLog.setAction("返件发货");
        processLog.setOrderId(order.getId());
        processLog.setActionComment(String.format("APP返件发货,操作人:%s", materialReturn.getUpdateBy().getName()));
        processLog.setStatus(order.getOrderCondition().getStatus().getLabel());
        processLog.setStatusValue(order.getOrderCondition().getStatusValue());
        processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
        processLog.setCloseFlag(0);
        processLog.setCreateBy(materialReturn.getUpdateBy());
        processLog.setCreateDate(materialReturn.getUpdateDate());
        processLog.setCustomerId(order.getOrderCondition().getCustomerId());
        processLog.setDataSourceId(order.getDataSourceId());
        orderService.saveOrderProcessLogNew(processLog);
    }

    // 修改配件申请当头
    public void updateMaterialMaster(HashMap<String,Object> params){
        dao.updateMaterialReturn(params);
    }

    /**
     * 订单未审核的配件申请单数量
     * 2018/04/18变更:
     * 1.配件及返件单状态必须都通过审核(2 - 待发货)或驳回(5 - 已驳回)
     * 2.返件：状态必须是：3 - 已发货  (配件单不必检查)
     */
    public Integer getNoApprovedMaterialReturnQty(Long orderId, String quarter) {
        return dao.getNoApprovedMaterialReturnQty(orderId, quarter);
    }

    /**
     * 关闭（驳回）配件申请单时，如选择不返件，同时关闭返件单
     * @param masterId  配件单id
     * @param quarter
     * @param user
     * @param date
     * @param closeRemark
     * @param status
     */
    @Transactional
    public void manuRejectAndCloseReturnForm(Long id,Long masterId, String quarter,User user,Date date, String closeRemark, Integer status){
        dao.manuRejectAndCloseReturnForm(id,masterId,quarter,user,date,closeRemark,status);
    }

    //endregion

    //region 日志

    @Transactional
    public void insertLog(MaterialLog log){
        log.setCreateBy(StringUtils.left(log.getCreateBy(),30));
        log.setContent(StringUtils.left(log.getContent(),150));
        logDao.insertLog(log);
    }

    public List<MaterialLog> getLogs(long materialMasterId,String quarter){
        return logDao.getLogs(materialMasterId,quarter,MaterialLog.SORT_DESC);
    }

    public List<MaterialLog> getLogs(long materialMasterId,String quarter,String sortBy){
        return logDao.getLogs(materialMasterId,quarter,sortBy);
    }

    //endregion

    //region 跟踪进度

    /**
     * 更新跟踪进度
     * 更新申请单，并记录日志
     * @param id
     * @param quarter
     * @param pendingType
     * @param pendingDate
     * @param pendingContent
     * @param createBy
     */
    @Transactional
    public void updateMaterialPendingInfo(long id, String quarter, Dict pendingType, Date pendingDate, String pendingContent,String createBy){
        dao.updateMaterialPendingInfo(id,quarter,pendingType,pendingDate,pendingContent);
        MaterialLog log = MaterialLog.builder()
                .quarter(quarter)
                .MaterialMasterId(id)
                .content(pendingContent)
                .createBy(createBy)
                .createDate(pendingDate)
                .build();
        logDao.insertLog(log);
    }

    //endregion

    //region 公共方法

    private void loadMaterialReturnInfo(MaterialReturn form,boolean loadItems){
        if (form != null) {
            if (form.getStatus() != null && StringUtils.toInteger(form.getStatus().getValue()) > 0) {
                String statusLabel = MSDictUtils.getDictLabel(form.getStatus().getValue(), "material_apply_status", "");
                form.getStatus().setLabel(statusLabel);
            }
            if (form.getApplyType() != null && StringUtils.toInteger(form.getApplyType().getValue()) > 0) {
                String applyTypeLabel = MSDictUtils.getDictLabel(form.getApplyType().getValue(),"material_apply_type", "");
                form.getApplyType().setLabel(applyTypeLabel);
            }
            Product product = productService.getProductByIdFromCache(form.getProduct().getId());
            if(product != null){
                form.setProduct(product);
            }
            //express company
            if(StringUtils.isNotBlank(form.getExpressCompany().getValue())){
                Dict company = MSDictUtils.getDictByValue(form.getExpressCompany().getValue(),"express_type");
                if(company != null){
                    form.setExpressCompany(company);
                }
            }
            //items
            if(loadItems) {
                loadItemMaterials(form.getItems(), null);
            }
        }
    }
    /**
     * 从缓存读取配件单明细中配件信息
     * @param items
     * @param materialMap   配件本地缓存
     */
    private void loadItemMaterials(List<MaterialReturnItem> items,Map<Long, Material> materialMap){
        if(ObjectUtils.isEmpty(items)){
            return;
        }
        Material material,tmpMaterial;
        for(MaterialReturnItem itm:items){
            if(itm == null) {
                continue;
            }
            tmpMaterial = itm.getMaterial();
            if(materialMap == null){
                material = materialService.getFromCache(tmpMaterial.getId());
                if(material != null){
                    itm.setMaterial(material);
                }
            }else {
                if (materialMap.containsKey(tmpMaterial.getId())) {
                    itm.setMaterial(materialMap.get(tmpMaterial.getId()));
                } else {
                    material = materialService.getFromCache(tmpMaterial.getId());
                    if (material != null) {
                        materialMap.put(tmpMaterial.getId(), material);
                        itm.setMaterial(material);
                    }
                }
            }
        }
    }

    //endregion 公共方法

    /**
     * 根据id获取返件地址信息
     * */
    public MaterialReturn getMaterialReturnAddress(Long id,String quarter){
        return dao.getMaterialReturnAddress(id,quarter);
    }

    /**
     * 保存修改的返件地址
     * */
    public void saveReturnAddress(MaterialReturn materialReturn){
        dao.saveReturnAddress(materialReturn);
    }

    /**
     * 删除返件附件
     */
    public void deleteReturnAttachment(Long id,String quarter,Date updateDate,Long updateBy){
        dao.deleteReturnAttachment(id,quarter,updateDate,updateBy);
    }

    /**
     * 获取返件单快递单号以及状态
     */
    public MaterialReturn getReturnExpressNoAndStatus(Long masterId,String quarter){
        return dao.getReturnExpressNoAndStatus(masterId,quarter);
    }

    /**
     * 保存旧件签收
     */
    public void saveSign(MaterialReturn materialReturn){
        dao.saveSign(materialReturn);
    }


    /**
     * 客户业务分页查询待签收旧件单
     * @param page
     * @param searchModel
     * @return
     */
    public Page<MaterialReturn> findCustomerWaitMaterialReturnList(Page<OrderMaterialSearchModel> page, OrderMaterialSearchModel searchModel){
        searchModel.setPage(page);
        List<MaterialReturn> list = dao.findCustomerWaitMaterialReturnList(searchModel);
        kefuOrderMaterialService.getMaterialReturnInfo(list);
        Page<MaterialReturn> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        rtnPage.setList(list);
        return rtnPage;
    }

    public Integer getMaterialReturnStatus(Long id,String quarter){
        return  dao.getMaterialReturnStatus(id,quarter);
    }

}
