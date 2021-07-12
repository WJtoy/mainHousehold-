package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import lombok.Data;

import java.util.List;

/**
 * 待发货配件单导入模型
 * */
@Data
public class ImportMaterialVM {

    private Long id = 0L;

    private Long orderId = 0L;

    private String orderNo;

    private String customerName;

    private String thirdNo;

    private String statusLabel;

    private String userName;

    private String userPhone;

    private String userArea;

    private String detailsArea;

    private String masterNo;

    private String applicant;

    private String applyDate;

    private String applyRemark;

    private String reviewer;

    private String approveTime;

    private String approveRemark;

    private String pendingLabel;

    private String pendingTime;

    private String pendingContent;

    private Dict expressCompany;

    private String expressNo;

    private String quarter;

    private String checkMessage;

    private int successFlag;

    List<ImportMaterialItemVM> items = Lists.newArrayList();

}
