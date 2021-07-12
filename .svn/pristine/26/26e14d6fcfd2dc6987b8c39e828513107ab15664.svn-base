package com.wolfking.jeesite.ms.jd.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BRetryOperationData;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.canbo.rpt.dao.CanboProcessLogDao;
import com.wolfking.jeesite.ms.jd.rpt.entity.JDSearchModel;
import com.wolfking.jeesite.ms.jd.rpt.feign.MSJDFailLogRptFeign;
import com.wolfking.jeesite.ms.jd.rpt.feign.MSJDOrderRptFeign;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JDOrderInfoService {
    @Autowired
    private MSJDOrderRptFeign msOrderRptFeign;

    @Autowired
    private MSJDFailLogRptFeign msFailLogRptFeign;

    @Autowired
    private CanboProcessLogDao canboProcessLogDao;
    //
    public Page<B2BOrderProcesslog> getList(Page<JDSearchModel> page, JDSearchModel processlogSearchModel){
        if(processlogSearchModel.getPage()==null) {
            MSPage msPage = PageMapper.INSTANCE.toMSPage(page);
        }
        Page<B2BOrderProcesslog> processlogPage  = new Page<>();
        processlogPage.setPageSize(page.getPageSize());
        processlogPage.setPageNo(page.getPageNo());
        processlogSearchModel.setPage(new MSPage<>(processlogPage.getPageNo(),processlogPage.getPageSize()));
        MSResponse<MSPage<B2BOrderProcesslog>> processLog = msOrderRptFeign.getList(processlogSearchModel);
        if (MSResponse.isSuccess(processLog)){
            MSPage<B2BOrderProcesslog> data = processLog.getData();
            processlogPage.setCount(data.getRowCount());
            processlogPage.setList(data.getList());
        }
        return  processlogPage;
    }

    //获取状态信息列表
    public Page<B2BOrderProcesslog> getFailLogList(Page<JDSearchModel> page, JDSearchModel jdSearchModel){
        if(jdSearchModel.getPage()==null) {
            MSPage msPage = PageMapper.INSTANCE.toMSPage(page);
        }
        Page<B2BOrderProcesslog> processlogPage  = new Page<>();
        processlogPage.setPageSize(page.getPageSize());
        processlogPage.setPageNo(page.getPageNo());
        jdSearchModel.setPage(new MSPage<>(processlogPage.getPageNo(),processlogPage.getPageSize()));
        MSResponse<MSPage<B2BOrderProcesslog>> processLog = msFailLogRptFeign.getFailLogList(jdSearchModel);
        if (MSResponse.isSuccess(processLog)){
            MSPage<B2BOrderProcesslog> data = processLog.getData();
            processlogPage.setCount(data.getRowCount());
            processlogPage.setList(data.getList());
        }
        return  processlogPage;
    }

    /**
     * 根据Id获取信息
     * @param id
     * @return
     */
    public B2BOrderProcesslog getLogById(long id){
        MSResponse<B2BOrderProcesslog> logById = msFailLogRptFeign.getLogById(id);
        B2BOrderProcesslog data = null;
        if (MSResponse.isSuccess(logById)){
            data = logById.getData();
        }
        return data;
    }

    /**
     * 重发
     * @param retryOperationData
     */
    public AjaxJsonEntity retryData(B2BRetryOperationData retryOperationData){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if (retryOperationData!=null){
            MSResponse response = msFailLogRptFeign.retryData(retryOperationData);
            if (MSResponse.isSuccessCode(response)){
                ajaxJsonEntity.setSuccess(true);
            }else {
                ajaxJsonEntity.setSuccess(false);
            }
            ajaxJsonEntity.setMessage(response.getMsg());
        }
        return ajaxJsonEntity;
    }

    /**
     * 忽略本条信息
     * @param retryOperationData
     */
    public AjaxJsonEntity closeLog(B2BRetryOperationData retryOperationData){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if (retryOperationData!=null){
            MSResponse response = msFailLogRptFeign.closeLog(retryOperationData);
            if (MSResponse.isSuccessCode(response)){
                ajaxJsonEntity.setSuccess(true);
            }else {
                ajaxJsonEntity.setSuccess(false);
            }
            ajaxJsonEntity.setMessage(response.getMsg());
        }
        return ajaxJsonEntity;
    }

    /**
     * 根据dataSource和客户单号查询工单信息
     * @return
     */
    public HashMap<String,Object> getOrderInfoByCanbo(Integer dataSource , String workcardId){
        return canboProcessLogDao.getOrderInfoByCanbo(dataSource,workcardId);
    }

}
