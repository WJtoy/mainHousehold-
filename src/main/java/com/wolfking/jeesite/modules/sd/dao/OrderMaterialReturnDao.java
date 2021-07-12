package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.MaterialReturn;
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
 * @description 返件单数据层
 * @date 2019/6/29
 */
@Mapper
public interface OrderMaterialReturnDao extends LongIDCrudDao<MaterialReturn> {

    /**
     * 按订单返回返件申请完整数据(包含明细)
     * 不包含图片，与配件单共用
     * product及material 需在service层单独读取
     * @param orderId       订单id
     * @param quarter       分片
     * @return
     */
    public List<MaterialReturn> findMaterialReturnListByOrderId(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    /**
     * 按订单返回配件单(不包含图片)
     * product及material 需在service层单独读取
     * @param orderId       订单id
     * @param quarter       分片
     * @param withOrder     返回订单基本信息
     * @param withPending   返回跟踪信息
     * @param withReceivor  返回收件信息
     * @param withClose     返回关闭信息
     * @return
     */
    public List<MaterialReturn> findMaterialReturnListByOrderId(@Param("orderId") Long orderId, @Param("quarter") String quarter,@Param("withOrder") Integer withOrder,@Param("withPending") Integer withPending,@Param("withReceivor") Integer withReceivor,@Param("withClose") Integer withClose);

    /**
     * 按订单返回配件单(不包含图片)
     * 不包含图片，与配件单共用
     * product及material 需在service层单独读取
     * @param orderId       订单id
     * @param quarter       分片
     * @param withOrder     返回订单基本信息
     * @param withPending   返回跟踪信息
     * @param withReceivor  返回收件信息
     * @param withClose     返回关闭信息
     * @return
     */
    public List<MaterialReturn> findMaterialReturnListNoAttachByOrderId(@Param("orderId") Long orderId, @Param("quarter") String quarter,@Param("withOrder") Integer withOrder,@Param("withPending") Integer withPending,@Param("withReceivor") Integer withReceivor,@Param("withClose") Integer withClose);

    /**
     * 按订单返回配件申请单单头
     * product及material 需在service层单独读取
     * @param orderId       订单id
     * @param quarter       分片
     * @param withOrder     返回订单基本信息
     * @param withPending   返回跟踪信息
     * @param withReceivor  返回收件信息
     * @param withClose     返回关闭信息
     * @return
     */
    public List<MaterialReturn> findMaterialReturnHeadListByOrderId(@Param("orderId") Long orderId, @Param("quarter") String quarter,@Param("withOrder") Integer withOrder,@Param("withPending") Integer withPending,@Param("withReceivor") Integer withReceivor,@Param("withClose") Integer withClose);

    /**
     * 按id返回返件申请记录(不包含图片)
     * @param id            返件单id
     * @param masterId      配件单id
     * @param quarter       分片
     * @param withOrder     返回订单基本信息
     * @param withPending   返回跟踪信息
     * @param withReceivor  返回收件信息
     * @param withClose     返回关闭信息
     * @return
     */
    public MaterialReturn getReturnFormById(@Param("id") Long id,@Param("masterId") Long masterId, @Param("quarter") String quarter,@Param("withOrder") Integer withOrder,@Param("withPending") Integer withPending,@Param("withReceivor") Integer withReceivor,@Param("withClose") Integer withClose);

    /**
     * 按id返回返件单单头
     * @param id            返件单id
     * @param masterId      配件单id
     * @param quarter       分片
     * @param withOrder     返回订单基本信息
     * @param withPending   返回跟踪信息
     * @param withReceivor  返回收件信息
     * @param withClose     返回关闭信息
     * @return
     */
    public MaterialReturn getMaterialReturnHeadById(@Param("id") Long id,@Param("masterId") Long masterId, @Param("quarter") String quarter,@Param("withOrder") Integer withOrder,@Param("withPending") Integer withPending,@Param("withReceivor") Integer withReceivor,@Param("withClose") Integer withClose);

    /**
     * 按返件单id查询图片列表
     * @param returnId      返件单id
     * @return
     */
    public List<MaterialAttachment> findAttachementsByReturnId(@Param("returnId") Long returnId, @Param("quarter") String quarter);

    /**
     * 添加返件单头
     * @param materialReturn
     */
    public void insertMaterialReturn(MaterialReturn materialReturn);

    /**
     * 添加单身项
     * @param materialItem
     */
    public void insertMaterialReturnItem(MaterialReturnItem materialItem);

    /**
     * 修改单头
     */
    public void updateMaterialReturn(HashMap<String, Object> params);

    /**
     * 更新跟踪进度
     */
    public void updateMaterialPendingInfo(@Param("id") Long id, @Param("quarter") String quarter,
                                          @Param("pendingType") Dict pendingType, @Param("pendingDate") Date pendingDate, @Param("pendingContent") String pendingContent);


    /**
     * 添加配件附件（和配件单头关联）
     * @param attachment
     */
    public void insertMaterialAttach(MaterialAttachment attachment);

    /**
     * 配件表（单头）- 附件 关联表
     * @param returnId  返件单id
     * @param attachmentId  附件id(sd_material_attachment)
     */
    public void insertMaterialMasterAttachMap(@Param("returnId") Long returnId, @Param("attachmentId") Long attachmentId, @Param("quarter") String quarter);

    /**
     * 关闭（驳回）配件申请单时，如选择不返件，同时关闭返件单
     */
    void manuRejectAndCloseReturnForm(@Param("id") Long id,@Param("masterId") Long masterId,@Param("quarter") String quarter,@Param("closeBy") User user,@Param("closeDate") Date date,@Param("closeRemark") String closeRemark,@Param("status") Integer status);

    /**
     * 读取订单返件状态信息，判断是否可客评
     * @return
     */
    List<MaterialReturn> getMaterialReturnListForGrade(@Param("orderId") Long orderId,@Param("quarter") String quarter);

    /**
     * 未发货返件单数量
     */
    Integer getNoApprovedMaterialReturnQty(@Param("orderId") Long orderId,@Param("quarter") String quarter);

    /**
     * 根据返件id获取返件收件地址信息
     */
    MaterialReturn getMaterialReturnAddress(@Param("id") Long id,@Param("quarter") String quarter);

    /**
     * 更新返件地址信息
     */
    void saveReturnAddress(MaterialReturn materialReturn);

    /**
     * 删除返件附件
     */
    void deleteReturnAttachment(@Param("id") Long id,@Param("quarter") String quarter,@Param("updateDate") Date updateDate,@Param("updateBy") Long updateBy);

    /**
     * 获取返件单快递单号以及状态
     */
    MaterialReturn getReturnExpressNoAndStatus(@Param("masterId") Long masterId,@Param("quarter") String quarter);

    /**
     * 保存旧件签收
     */
    void saveSign(MaterialReturn materialReturn);


    /**
     * 客户查询待签收返件单列表
     */
    List<MaterialReturn> findCustomerWaitMaterialReturnList(OrderMaterialSearchModel searchModel);

    /**
     * 根据id获取返件单状态
     */
    Integer getMaterialReturnStatus(@Param("id") Long id,@Param("quarter") String quarter);
}
