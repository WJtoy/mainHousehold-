package com.wolfking.jeesite.modules.servicepoint.receipt.service;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.cc.AbnormalForm;
import com.kkl.kklplus.entity.cc.AbnormalFormEnum;
import com.kkl.kklplus.entity.cc.vm.AbnormalFormSearchModel;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.praise.Praise;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.servicepoint.ms.receipt.SpOrderPraiseService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.utils.PraiseUtils;
import com.wolfking.jeesite.ms.cc.entity.AbnormalFormModel;
import com.wolfking.jeesite.ms.cc.entity.mapper.AbnormalFormModelMapper;
import com.wolfking.jeesite.ms.cc.feign.CCServicePointAbnormalFeign;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.entity.mapper.PraiseModelMapper;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 异常单服务层
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServicePointAbnormalService {

    @Autowired
    private CCServicePointAbnormalFeign servicePointAbnormalFeign;

    @Autowired
    private SpOrderPraiseService spOrderPraiseService;


    /**
     * 好评单异常
     * @param abnormalFormSearchModel
     * @return
     */
    public Page<AbnormalFormModel> praiseAbnormalList(Page<AbnormalFormModel> page,AbnormalFormSearchModel abnormalFormSearchModel){
        abnormalFormSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<AbnormalForm>> msResponse = servicePointAbnormalFeign.servicePointPraiseRejectAbnormalList(abnormalFormSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<AbnormalForm> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<AbnormalFormModel> list = Mappers.getMapper(AbnormalFormModelMapper.class).toViewModels(data.getList());
                if(list!=null && list.size()>0){
                    Map<String,Dict> dictList = MSDictUtils.getDictMap("praise_abnormal_type");
                    Date date = new Date();
                    TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                    long startDt = twoTuple.getAElement();
                    long endDt = twoTuple.getBElement();
                    for(AbnormalFormModel entity:list){
                        Dict dict = dictList.get(String.valueOf(entity.getSubType()));
                        if(dict!=null){
                            entity.setSubTypeName(dict.getLabel());
                        }
                        double praiseTimeliness = DateUtils.calculateTimeliness(date,entity.getTimeoutAt(),startDt,endDt);
                        entity.setFeedBackTimeliness(PraiseUtils.getCutOffTimelinessLabel(praiseTimeliness,120));
                        entity.setCutOffTimeliness(praiseTimeliness);
                    }
                }
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
     * 网点处理好评驳回详情
     * @param orderId
     * @param quarter
     * @return
     */
    public ViewPraiseModel praiseInfoForServicePoint(Long orderId, String quarter,Long servicePointId){
        Praise praise = spOrderPraiseService.getByOrderId(quarter,orderId,servicePointId);
        if(praise!=null){
            ViewPraiseModel viewPraiseModel = Mappers.getMapper(PraiseModelMapper.class).PraiseToViewModel(praise);
            if(viewPraiseModel!=null){
                Gson gson = new Gson();
                List<String> picList = gson.fromJson(praise.getPicsJson(), new TypeToken<List<String>>(){}.getType());
                viewPraiseModel.setPics(picList);
                PraiseStatusEnum praiseStatusEnum = PraiseStatusEnum.fromCode(viewPraiseModel.getStatus());
                if(praiseStatusEnum!=null){
                    viewPraiseModel.setStrStatus(praiseStatusEnum.msg);
                }
            }
            return viewPraiseModel;
        }else{
            return null;
        }
    }
}
