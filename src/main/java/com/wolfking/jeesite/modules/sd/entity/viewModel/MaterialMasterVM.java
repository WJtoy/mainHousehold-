package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import lombok.*;
import org.assertj.core.util.Lists;

import java.util.List;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * 新配件视图数据模型
 * @date 2019-08-06 15:52
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialMasterVM {
    //等待B2B厂商回复
    private int waitingB2BCommand = 0;
    private Long id;
    private Long orderId;
    private String orderNo;
    private String quarter;
    private String masterNo;
    //产品id,辅助分组
    private Long productId;
    private Dict applyType;
    private Dict status;
    private Dict expressCompany;
    private String expressNo;
    //是否需要返件
    @Builder.Default
    private Integer returnFlag = 0;
    private String createDate;
    private double totalPrice = 0.00;
    private String remarks;
    private String receivedInfo = "";
    //最大行数(用来)
    private Integer maxRow;

    private String closeRemark="";

    //返件单是否发货标识 0:未发货1:已发货
    private int materialReturnSendFlag = 0;

    //返件单收件信息
    private String materialReturnReceiverInfo = "";

    private List<MaterialSubForm> subForms;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((productId == null) ? 0 : productId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        else {
            if (obj instanceof MaterialMasterVM) {
                MaterialMasterVM t = (MaterialMasterVM) obj;
                if (!t.id.equals(this.id)) {
                    return false;
                }
                return t.productId.equals(this.productId);
            }
        }
        return false;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialSubForm {
        private Product product;
        @Builder.Default
        private double totalPrice = 0.00;
        //配件列表
        private List<MaterialItemVM> materials;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialItemVM {

        private Long id;

        private Long materialId;

        private String materialName;

        private Integer qty;

        private double price = 0.0;

        private double totalPrice = 0.0;

        private int returnFlag = 0;

        private int recycleFlag = 0;

        private double totalRecyclePrice = 0.0;

        private double recyclePrice = 0.0;

    }
}
