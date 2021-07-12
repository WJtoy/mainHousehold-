package com.wolfking.jeesite.ms.praise.service;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.praise.Praise;
import com.kkl.kklplus.entity.praise.PraiseListModel;
import com.kkl.kklplus.entity.praise.PraisePageSearchModel;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.praise.entity.PraiseLogModel;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.entity.mapper.PraiseModelMapper;
import com.wolfking.jeesite.ms.praise.feign.CustomerPraiseFeign;
import com.wolfking.jeesite.ms.praise.feign.OrderPraiseFeign;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 客户好评service
 * */
@Service
public class CustomerPraiseService {

    @Autowired
    private CustomerPraiseFeign customerPraiseFeign;

    @Autowired
    private OrderPraiseService orderPraiseService;

    @Autowired
    private OrderPraiseFeign orderPraiseFeign;


    /**
     * 已通过好配单列表
     * @param praisePageSearchModel
     * @return
     */
    public Page<ViewPraiseModel> waitProcessList(Page<ViewPraiseModel> page, PraisePageSearchModel praisePageSearchModel){
        praisePageSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<PraiseListModel>> msResponse = customerPraiseFeign.approvedList(praisePageSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<PraiseListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ViewPraiseModel> list = Mappers.getMapper(PraiseModelMapper.class).toViewModels(data.getList());
                page.setList(list);
            } else {
                page.setCount(0);
                page.setList(Lists.newArrayList());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return page;
    }

    /**
     * 客服订单详情查看好评信息
     * @param orderId
     * @return quarter
     */
    public ViewPraiseModel getPraiseForCustomer(Long orderId,String quarter,Long servicePointId){
        Praise praise = orderPraiseService.getByOrderId(quarter,orderId,servicePointId);
        if(praise!=null){
            ViewPraiseModel viewPraiseModel = Mappers.getMapper(PraiseModelMapper.class).PraiseToViewModel(praise);
            if(viewPraiseModel!=null){
                Gson gson = new Gson();
                List<String> picList = gson.fromJson(praise.getPicsJson(), new TypeToken<List<String>>(){}.getType());
                viewPraiseModel.setPics(picList);
                List<PraiseLogModel> praiseLogModelList = orderPraiseService.finPraiseLogList(quarter,viewPraiseModel.getId());
                viewPraiseModel.setPraiseLogModels(praiseLogModelList);
            }
            return viewPraiseModel;
        }else{
            return null;
        }
    }

    /**
     * 根据时间获取好评单数量
     * */
    public Integer praiseCount(Long beginDt){
        MSResponse<Integer> msResponse = orderPraiseFeign.praiseCount(beginDt);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return 0;
        }
    }
}
