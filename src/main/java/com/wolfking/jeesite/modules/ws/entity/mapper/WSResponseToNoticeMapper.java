package com.wolfking.jeesite.modules.ws.entity.mapper;

import com.wolfking.jeesite.modules.api.entity.sd.RestOrder;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sys.entity.Notice;
import com.wolfking.jeesite.modules.ws.entity.WSResponse;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class WSResponseToNoticeMapper extends CustomMapper<WSResponse, Notice>{

    @Override
    public void mapAtoB(WSResponse a, Notice b, MappingContext context) {
        b.setId(Long.valueOf(a.getId()));
        b.setQuarter(a.getQuarter());
        b.setTitle(a.getTitle());
        b.setContext(a.getContext());
        b.setNoticeType(a.getNoticeType());
        b.setReferId(Long.valueOf(a.getReferId()));
        b.setReferNo(a.getReferNo());
        b.setLink(a.getLink());
    }

    @Override
    public void mapBtoA(Notice b, WSResponse a, MappingContext context) {

    }
}
