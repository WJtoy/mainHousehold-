package com.wolfking.jeesite.ms.viomi.rpt.service;

import com.github.pagehelper.util.StringUtil;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.viomi.sd.VioMiApiLog;
import com.kkl.kklplus.entity.viomi.sd.VioMiExceptionOrder;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import com.wolfking.jeesite.ms.viomi.rpt.entity.ViomiFailLogSearchModel;
import com.wolfking.jeesite.ms.viomi.rpt.feign.MSViomiFailLogRptFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ViomiFailLogService {

    @Autowired
    private MSViomiFailLogRptFeign msFailLogRptFeign;

    //获取状态信息列表
    public Page<VioMiExceptionOrder> getFailLogList(Page<ViomiFailLogSearchModel> page, ViomiFailLogSearchModel model){
        if(model.getPage()==null) {
            MSPage msPage = PageMapper.INSTANCE.toMSPage(page);
        }
        Page<VioMiExceptionOrder> processlogPage  = new Page<>();
        processlogPage.setPageSize(page.getPageSize());
        processlogPage.setPageNo(page.getPageNo());
        model.setPage(new MSPage<>(processlogPage.getPageNo(),processlogPage.getPageSize()));
        MSResponse<MSPage<VioMiExceptionOrder>> processLog = msFailLogRptFeign.getFailLogList(model);
        if (MSResponse.isSuccess(processLog)){
            MSPage<VioMiExceptionOrder> data = processLog.getData();
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
    public List<VioMiApiLog> getLogById(long id){
        MSResponse<List<VioMiApiLog>> logById = msFailLogRptFeign.getLogById(id);
        List<VioMiApiLog> data = null;
        if (MSResponse.isSuccess(logById)){
            data = logById.getData();
        }
        return data;
    }

    public VioMiExceptionOrder getOrderInfo(long id){
        VioMiExceptionOrder vioMiExceptionOrder = null;
        MSResponse<VioMiExceptionOrder> processLog = msFailLogRptFeign.getOrderInfo(id);
        if (MSResponse.isSuccess(processLog)){
            vioMiExceptionOrder = processLog.getData();
        }
        return  vioMiExceptionOrder;
    }

    /**
     * 重发
     */
    public AjaxJsonEntity retryViomiData(Long apiLogId,String operator, Long operatorId){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if (apiLogId!=null && StringUtil.isNotEmpty(operator) && operatorId!= null){
            MSResponse response = msFailLogRptFeign.retryData(apiLogId,operator,operatorId);
            if (MSResponse.isSuccessCode(response)){
                ajaxJsonEntity.setSuccess(true);
            }else {
                ajaxJsonEntity.setSuccess(false);
            }
            ajaxJsonEntity.setMessage(response.getMsg());
        }
        return ajaxJsonEntity;
    }


}
