package com.wolfking.jeesite.ms.inse.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.inse.rpt.entity.InseSearchModel;
import com.wolfking.jeesite.ms.inse.rpt.feign.MSInseOrderRptFeign;
import com.wolfking.jeesite.ms.jd.rpt.entity.JDSearchModel;
import com.wolfking.jeesite.ms.jd.rpt.feign.MSJDOrderRptFeign;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class InseOrderProcessLogService {
    @Autowired
    private MSInseOrderRptFeign msOrderRptFeign;

    //
    public Page<B2BOrderProcesslog> getList(Page<InseSearchModel> page, InseSearchModel processlogSearchModel){
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


}
