package com.wolfking.jeesite.modules.servicepoint.ms.receipt;

import com.kkl.kklplus.common.response.MSResponse;
import com.wolfking.jeesite.modules.sd.entity.MaterialMaster;
import com.wolfking.jeesite.modules.sd.service.OrderMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;


/**
 * 网点催单
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpOrderMaterialService {


    @Autowired
    private OrderMaterialService materialService;

    /**
     * 按订单返回配件申请单单头记录（不含配件和图片）
     */
    public List<MaterialMaster> findMaterialMasterHeadsByOrderId(Long orderId, String quarter) {
        return materialService.findMaterialMasterHeadsByOrderId(orderId, quarter);
    }

    /**
     * 修改配件申请当头
     */
    public void updateMaterialMaster(HashMap<String, Object> params) {
        materialService.updateMaterialMaster(params);
    }

    /**
     * 根据订单配件状态检查是否可以客评,退单,取消
     * old,作废
     * 1.配件单未审核,未发货，不能客评
     * 2.返件单未发货，不能客评
     * new 2019-07-10
     * 1.判断配件单，是否审核，没审核不能客评
     * 2.如驳回，可以客评
     * 3.审核通过，判断返件单是否需要返件，不需要返件，可以客评；需要返件，必须填了返件快递单号才能客评
     *
     * @return MSResponse MSResponse.isSuccesCode() == true ,可客评
     * code = 10000 ,不能客评
     * code = 1，需手动关闭再客评
     */
    public MSResponse<String> canGradeOfMaterialForm(Integer dataSource, long orderId, String quarter) {
        return materialService.canGradeOfMaterialForm(dataSource, orderId, quarter);
    }

}
