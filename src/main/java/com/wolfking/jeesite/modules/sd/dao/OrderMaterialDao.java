package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderMaterialSearchModel;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * @description 订单配件访问接口
 * @date 2019/5/31 11:24 AM
 */
@Mapper
public interface OrderMaterialDao extends LongIDCrudDao<MaterialMaster> {

    /**
     * 按订单返回配件申请记录(包含明细及图片)
     * 产品及配件需重缓存单独读取
     */
    public List<MaterialMaster> findMaterialMastersByOrderId(@Param("orderId") Long orderId,@Param("quarter") String quarter);

    /**
     * 按订单返回配件申请单单头(不包含明细及图片)
     */
    public List<MaterialMaster> findMaterialMasterHeadsByOrderId(@Param("orderId") Long orderId,@Param("quarter") String quarter);

    /**
     * 按订单返回配件申请记录(不包含图片) 预留
     * 产品及配件需重缓存单独读取
     */
    public List<MaterialMaster> findMaterialMastersNoAttachByOrderId(@Param("orderId") Long orderId,@Param("quarter") String quarter);

    /**
     * 读取订单配件状态信息，判断是否可客评
     */
    public List<MaterialMaster> getMaterialFormsForGrade(@Param("orderId") Long orderId,@Param("quarter") String quarter);

    /**
     * 读取下个申请次序
     */
    public int getNextApplyTime(@Param("orderId") Long orderId,@Param("quarter") String quarter);

    /**
     * 按申请单id返回配件申请记录(包含单头，单身)
     * 产品及配件需重缓存单独读取
     * @param masterId
     * @return
     */
    public MaterialMaster getMaterialMasterById(@Param("masterId") Long masterId,@Param("quarter") String quarter);

    /**
     * 按申请单id返回配件申请单单头
     * @param masterId 申请单id
     * @return
     */
    public MaterialMaster getMaterialMasterHeadById(@Param("masterId") Long masterId,@Param("quarter") String quarter);

    /**
     * 按申请单id返回配件申请单单身
     * 产品及配件需重缓存单独读取
     * @param masterId 申请单id
     */
    public List<MaterialItem> getMaterialMasterItemsById(@Param("masterId") Long masterId,@Param("quarter") String quarter);

    /**
     * 按配件申请单id查询其附件
     * @param masterId
     * @return
     */
    public List<MaterialAttachment> findAttachementsByMasterId(@Param("masterId") Long masterId,@Param("quarter") String quarter);

    /**
     * 添加配件单头
     * @param materialMaster
     */
    public void insertMaterialMaster(MaterialMaster materialMaster);

    /**
     * 添加配件产品信息
     */
    public void insertMaterialProduct(MaterialProduct materialProduct);

    /**
     * 修改配件申请当头
     */
    public int updateMaterialMaster(HashMap<String,Object> params);

    /**
     * 驳回配件申请单
     */
    public int rejectMaterial(HashMap<String,Object> params);

    public Integer getMaterialMasterCountByOrderId(@Param("orderId") Long orderId,@Param("quarter") String quarter,@Param("status") Integer status);

    /**
     * 物流接口更新签收时间
     */
    public void updateLogisticSignAt(@Param("orderId") Long orderId,@Param("quarter") String quarter,@Param("expressNo") String expressNo,@Param("signAt") Date signAt,@Param("updateDate") Date updateDate);

    /**
     * 订单未审核的配件申请单数量
     * @param orderId
     * @return
     */
    public Integer getNoApprovedMaterialMasterQty(@Param("orderId") Long orderId,@Param("quarter") String quarter);

    /**
     * 取消配件与上门服务项的关联
     * @param orderId  订单id
     * @param quarter  分片
     * @param detailId 上门服务id
     */
    public void cancelRelationOfServiceAndMaterial(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("detailId") Long detailId);

    /**
     * 添加配件与上门服务项的关联
     */
    public void  addRelationOfServiceAndMaterial(MaterialMaster model);

    /**
     * 更新跟踪进度
     */
    public void updateMaterialPendingInfo(@Param("id") Long id, @Param("quarter") String quarter,
                                          @Param("pendingType") Dict pendingType, @Param("pendingDate") Date pendingDate, @Param("pendingContent") String pendingContent);

    /**
     * 添加配件单身项
     * @param materialItem
     */
    public void insertMaterialItem(MaterialItem materialItem);

    /**
     * 添加配件附件（和配件单头关联）
     * @param attachment
     */
    public void insertMaterialAttach(MaterialAttachment attachment);

    /**
     * 配件表（单头）- 附件 关联表
     * @param materialMasterId
     * @param attachmentId
     */
    public void insertMaterialMasterAttachMap(@Param("materialMasterId") Long materialMasterId,@Param("attachmentId") Long attachmentId,@Param("quarter") String quarter);


    //region 客服

    List<MaterialMaster> findKefuMaterialList(OrderMaterialSearchModel searchModel);

    //endregion

    //region 客户

    List<MaterialMaster> findCustomerMaterialList(OrderMaterialSearchModel searchModel);

    // 配件单无返件，更新所有配件返件标记为：0
    void updateItemNoReturn(@Param("materialMasterId") Long materialMasterId,@Param("quarter") String quarter);

    // 返件，更新具体配件返件标记为：1
    void updateItemIsReturn(@Param("quarter") String quarter,@Param("id") Long id);

    // 客评时关闭配件单,status不变更
    void closeMaterialMasterWhenGrade(@Param("orderId") Long orderId,@Param("quarter") String quarter,@Param("closeBy") User closeBy,@Param("closeDate") Date date,@Param("closeRemark") String remark);

    // 取消或退单审核时驳回并关闭未发货配件单(驳回)
    void closeNoSendedMaterialMasterWhenCancel(@Param("orderId") Long orderId,@Param("quarter") String quarter,@Param("closeBy") User closeBy,@Param("closeDate") Date date,@Param("closeRemark") String remark);

    // 取消或退单审核时驳回并关闭已发货配件单(完成)
    void closeSendedMaterialMasterWhenCancel(@Param("orderId") Long orderId,@Param("quarter") String quarter,@Param("closeBy") User closeBy,@Param("closeDate") Date date,@Param("closeRemark") String remark);

    // 人工关闭配件单
    void manuCloseMaterialForm(@Param("id") Long id,@Param("quarter") String quarter,@Param("closeBy") User user,@Param("closeDate") Date date,@Param("closeRemark") String closeRemark,@Param("closeType") String closeType,@Param("returnFlag") Integer returnFlag,@Param("status") Integer status);

    //endregion

    //region 数据处理

    List<MaterialMaster> findToFixMaterialForms(@Param("quarters") List<String> quarters,@Param("id") Long id,@Param("maxId") Long maxId,@Param("pageSize") int pageSize);

    int updateMaterialItemProductItemId(@Param("quarter") String quarter,@Param("materialMasterId") Long materialMasterId,@Param("productId") Long productId,@Param("materialProductId") Long materialProductId);

    int updateMaterialFormApplyTime(@Param("quarter") String quarter,@Param("id") Long id,@Param("applyTime") int applyTime);

    void updateItemRecycle(MaterialItem MaterialItem);
    /**
     * 根据配件单号获取配件
     * */
    MaterialMaster getByMasterNo(@Param("masterNo") String masterNo);

    /**
     * 根据配件id获取配件指定信息(工单id,状态,产品id)
     * */
    MaterialMaster getAppointFields(@Param("id")Long id,@Param("quarter") String quarter);

    /**
     * 获取配件导出数据集合
     * */
    List<MaterialMaster> findMaterialExportList(OrderMaterialSearchModel searchModel);
    //endregion
}
