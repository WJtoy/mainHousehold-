package com.wolfking.jeesite.ms.keg.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.keg.sd.KegOrderCompletedData;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.canbo.rpt.dao.CanboProcessLogDao;
import com.wolfking.jeesite.ms.jd.rpt.entity.JDSearchModel;
import com.wolfking.jeesite.ms.keg.rpt.entity.KegSearchModel;
import com.wolfking.jeesite.ms.keg.rpt.feign.MSKegFailLogRptFeign;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MSKegFailLogService {


    @Autowired
    private MSKegFailLogRptFeign msFailLogRptFeign;

    @Autowired
    private CanboProcessLogDao canboProcessLogDao;


    //获取状态信息列表
    public Page<KegOrderCompletedData> getFailLogList(Page<JDSearchModel> page, KegSearchModel jdSearchModel){
        if(jdSearchModel.getPage()==null) {
            MSPage msPage = PageMapper.INSTANCE.toMSPage(page);
        }
        Page<KegOrderCompletedData> processlogPage  = new Page<>();
        processlogPage.setPageSize(page.getPageSize());
        processlogPage.setPageNo(page.getPageNo());
        jdSearchModel.setPage(new MSPage<>(processlogPage.getPageNo(),processlogPage.getPageSize()));
        MSResponse<MSPage<KegOrderCompletedData>> processLog = msFailLogRptFeign.getFailLogList(jdSearchModel);
        if (MSResponse.isSuccess(processLog)){
            MSPage<KegOrderCompletedData> data = processLog.getData();
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
    public KegOrderCompletedData getLogById(long id){
        MSResponse<KegOrderCompletedData> logById = msFailLogRptFeign.getLogById(id);
        KegOrderCompletedData data = null;
        if (MSResponse.isSuccess(logById)){
            data = logById.getData();
        }
        return data;
    }

    /**
     * 重发
     * @param retryOperationData
     */
    public AjaxJsonEntity retryData(KegOrderCompletedData retryOperationData){
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
     * @param
     */
    public AjaxJsonEntity closeLog(long id ,long updateBy ){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
            MSResponse response = msFailLogRptFeign.closeLog(id,updateBy);
            if (MSResponse.isSuccessCode(response)){
                ajaxJsonEntity.setSuccess(true);
            }else {
                ajaxJsonEntity.setSuccess(false);
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
