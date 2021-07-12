package com.wolfking.jeesite.ms.tmall.sd.service;

import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.tmall.sd.AnomalyRecourseRemarkUpdate;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.tmall.sd.dao.TmallAnomalyRecourseDao;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallAnomalyRecourse;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallAnomalyRecourseImage;
import com.wolfking.jeesite.ms.tmall.sd.entity.ViewModel.TmallAnomalyRecourseSearchVM;
import com.wolfking.jeesite.ms.tmall.sd.feign.WorkcardFeign;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


/**
 * 天猫一键求助
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class TmallAnomalyRecouseService {

    @Autowired
    private WorkcardFeign workcardFeign;

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private TmallAnomalyRecourseDao anomalyRecourseDao;

    //region 工单系统

    public TmallAnomalyRecourse get(Long id){
        return anomalyRecourseDao.get(id);
    }

    @Transactional()
    public void insert(TmallAnomalyRecourse entity){
        anomalyRecourseDao.insert(entity);
    }

    public List<TmallAnomalyRecourse> getListByOrder(Long orderId, String orderNo,String quarter){
        List<TmallAnomalyRecourse> list = anomalyRecourseDao.getListByOrder(orderId,orderNo,quarter);
        if(list != null && !list.isEmpty()) {
            Map<String, Dict> questionTypeMaps = MSDictUtils.getDictMap("AnomalyQuestionType");
            List<TmallAnomalyRecourseImage> images;
            for (TmallAnomalyRecourse m : list) {
                images = GsonUtils.getInstance().getGson().fromJson(m.getRecourseJson(), new TypeToken<List<TmallAnomalyRecourseImage>>() {
                }.getType());
                m.setRecourseList(images);
                if (questionTypeMaps != null && !questionTypeMaps.isEmpty() && m.getQuestionType() != null && StringUtils.isNotBlank(m.getQuestionType().getValue())) {
                    m.setQuestionType(questionTypeMaps.get(m.getQuestionType().getValue()));
                }
            }
        }
        return list;
    }

    /**
     * 分页查询求助列表
     */
    public Page<TmallAnomalyRecourse> findList(Page<TmallAnomalyRecourseSearchVM> page, TmallAnomalyRecourseSearchVM entity) {
        entity.setPage(page);
        List<TmallAnomalyRecourse> list = anomalyRecourseDao.findList(entity);
        Page<TmallAnomalyRecourse> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if(list != null && !list.isEmpty()) {
            List<TmallAnomalyRecourseImage> images;
            Map<String, Dict> questionTypeMaps = MSDictUtils.getDictMap("AnomalyQuestionType");
            for (TmallAnomalyRecourse m : list) {
                images = GsonUtils.getInstance().getGson().fromJson(m.getRecourseJson(), new TypeToken<List<TmallAnomalyRecourseImage>>() {
                }.getType());
                m.setRecourseList(images);
                if (questionTypeMaps != null && !questionTypeMaps.isEmpty() && m.getQuestionType() != null && StringUtils.isNotBlank(m.getQuestionType().getValue())) {
                    m.setQuestionType(questionTypeMaps.get(m.getQuestionType().getValue()));
                }
            }
            rtnPage.setList(list);
        }
        return rtnPage;
    }

    //endregion

    //region B2B

    /**
     * 反馈一键求助
     */
    @Transactional()
    public void feedback(TmallAnomalyRecourse entity){
        if(entity == null || entity.getId()==null){
            return;
        }
        //MSResponse responseEntity = new MSResponse<>(MSErrorCode.SUCCESS);
        try {
            entity.setStatus(1);
            AnomalyRecourseRemarkUpdate feedback = new AnomalyRecourseRemarkUpdate();
            feedback.setAnomalyRecourseId(entity.getAnomalyRecourseId());
            feedback.setRemark(entity.getReplyContent());
            feedback.setCreateById(entity.getReplierId());
            feedback.setUpdateById(entity.getReplierId());
            //update db
            anomalyRecourseDao.update(entity);

            //sync to b2b
            MSResponse msResponse = workcardFeign.anomalyRecourseFeedback(feedback);
            //subcode=2 or 6,求助单已关闭不抛出异常 2019/03/14
            if(!MSResponse.isSuccessCode(msResponse) && msResponse.getThirdPartyErrorCode().getCode() != 6 && msResponse.getThirdPartyErrorCode().getCode() != 2){
                throw new RuntimeException(StringUtils.isNotBlank(msResponse.getMsg()) ? msResponse.getMsg() : "同步天猫错误,请稍后重试");
            }
        } catch (FeignException fe){
            log.error("[TmallAnomalyRecouseService].feedback orderId:{}",entity.getOrderId(), fe);
            throw fe;
        } catch (Exception e) {
            log.error("[TmallAnomalyRecouseService].feedback orderId:{}",entity.getOrderId(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    //endregion
}
