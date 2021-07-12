package com.wolfking.jeesite.modules.sd.service;

import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.modules.sd.dao.B2BMsLogDao;
import com.wolfking.jeesite.modules.sd.entity.B2BMsLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Jeff on 2017/7/24.
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class B2BMsLogService extends LongIDCrudService<B2BMsLogDao, B2BMsLog> {
//
//    public Page<B2BMsLog> find(Page<B2BMsLog> page, B2BMsLog model) {
//        model.setPage(page);
//        // 执行分页查询
//        List<B2BMsLog> temp = super.findList(model);
//        page.setList(temp);
//        return page;
//    }
//
//    /**
//     * 新增，更新
//     */
//    @Override
//    @Transactional()
//    public void save(B2BMsLog log){
//        if (log.getIsNewRecord()){
//            dao.insert(log);
//        }else{
//            dao.update(log);
//        }
//    }

}
