package com.wolfking.jeesite.modules.finance.sd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.AppFeedbackEnum;
import com.kkl.kklplus.entity.praise.*;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.PushMessageUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.utils.PraiseUtils;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.entity.mapper.PraiseModelMapper;
import com.wolfking.jeesite.ms.praise.feign.OrderPraiseFeign;
import com.wolfking.jeesite.ms.praise.feign.SalesPraiseFeign;
import com.wolfking.jeesite.ms.praise.service.OrderPraiseService;
import com.wolfking.jeesite.ms.praise.service.SalesPraiseService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 财务部门好评单service
 * */
@Slf4j
@Service
public class FiPraiseService {

   @Autowired
   private SalesPraiseService salesPraiseService;

    /**
     * 业务查询待处理好评信息列表
     * @param praisePageSearchModel
     * @return
     */
    public Page<ViewPraiseModel> pendingReviewList(Page<ViewPraiseModel> page, PraisePageSearchModel praisePageSearchModel){
        return salesPraiseService.pendingReviewList( page,praisePageSearchModel);
    }

    /**
     * 业务查询已审核好评信息列表
     * @param praisePageSearchModel
     * @return
     */
    public Page<ViewPraiseModel> approvedList(Page<ViewPraiseModel> page, PraisePageSearchModel praisePageSearchModel){
        return salesPraiseService.approvedList(page,praisePageSearchModel);
    }


    /**
     * 业务查询所有好评信息列表
     * @param praisePageSearchModel
     * @return

    public Page<ViewPraiseModel> findPraiseList(Page<ViewPraiseModel> page, PraisePageSearchModel praisePageSearchModel){
      return salesPraiseService.findPraiseList(page,praisePageSearchModel);
    }*/
}
