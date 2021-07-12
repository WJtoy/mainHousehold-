package com.wolfking.jeesite.ms.providersys.service;

import com.kkl.kklplus.entity.sys.SysOfficeAttributes;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import com.wolfking.jeesite.ms.providersys.feign.MSSysOfficeAttributesFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MSSysOfficeAttributesService {

    @Autowired
    private MSSysOfficeAttributesFeign msSysOfficeAttributesFeign;

    public SysOfficeAttributes getByOfficeIdAndAttributeId(Long officeId, Long attributesId) {

        return MDUtils.getObjUnnecessaryConvertType(() -> msSysOfficeAttributesFeign.getByOfficeIdAndAttributeId(officeId, attributesId));
    }
}
