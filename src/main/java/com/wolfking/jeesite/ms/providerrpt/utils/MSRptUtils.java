package com.wolfking.jeesite.ms.providerrpt.utils;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.kkl.kklplus.entity.rpt.CustomerSignTypeEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.web.RPTDict;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.KefuTypeEnum;

import java.util.*;
import java.util.stream.Collectors;

public class MSRptUtils {

    public static List<Dict> getAllReportList() {
        List<RPTReportEnum> reportEnums = RPTReportEnum.getAllReport();
        List<Dict> dicts = Lists.newArrayList();
        for (RPTReportEnum item : reportEnums) {
            dicts.add(new Dict(item.value, item.label));
        }
        return dicts;
    }

    public static List<Dict> getAllReportTypeList() {
        List<RPTReportTypeEnum> reportTypeEnums = RPTReportTypeEnum.getAllReportType();
        List<Dict> dicts = Lists.newArrayList();
        for (RPTReportTypeEnum item : reportTypeEnums) {
            dicts.add(new Dict(item.value, item.label));
        }
        return dicts;
    }

    public static List<Dict> getAllPraiseTypeList() {
        Dict praiseDict;
        List<Dict> dicts = Lists.newArrayList();
        ArrayList<PraiseStatusEnum> list = new ArrayList<>(Arrays.asList(PraiseStatusEnum.values())) ;
        for(int i =0; i<list.size();i++){
            if(list.get(i).code == 0){
                list.remove(i);
            }
        }

        for (PraiseStatusEnum item : list) {
            praiseDict = new Dict();
            praiseDict.setValue(String.valueOf(item.code));
            praiseDict.setLabel(String.valueOf(item.msg));
            dicts.add(praiseDict);
        }
        return dicts;
    }

    public static List<Dict> getAllKeFuTypeList() {
        Dict praiseDict;
        List<Dict> dicts = Lists.newArrayList();
        ArrayList<KefuTypeEnum> list = new ArrayList<>(Arrays.asList(KefuTypeEnum.values())) ;
        for (KefuTypeEnum item : list) {
            praiseDict = new Dict();
            praiseDict.setValue(String.valueOf(item.code));
            praiseDict.setLabel(String.valueOf(item.msg));
            dicts.add(praiseDict);
        }
        return dicts;
    }

    public static List<Dict> getServicePointReportList() {
        List<RPTReportEnum> reportEnums = RPTReportEnum.getServicePointReports();
        List<Dict> dicts = Lists.newArrayList();
        for (RPTReportEnum item : reportEnums) {
            dicts.add(new Dict(item.value, item.label));
        }
        return dicts;
    }

    public static List<Dict> getCustomerSignReportList() {
        Dict customerSignDict;
        ArrayList<CustomerSignTypeEnum> list = new ArrayList<>(Arrays.asList(CustomerSignTypeEnum.values())) ;
        List<Dict> dicts = Lists.newArrayList();
        for (CustomerSignTypeEnum item : list) {
            customerSignDict = new Dict();
            customerSignDict.setValue(String.valueOf(item.code));
            customerSignDict.setLabel(String.valueOf(item.msg));
            dicts.add(customerSignDict);
        }
        return dicts;
    }


    public static List<Dict> getCustomerReportList() {
        List<RPTReportEnum> reportEnums = RPTReportEnum.getCustomerReports();
        List<Dict> dicts = Lists.newArrayList();
        for (RPTReportEnum item : reportEnums) {
            dicts.add(new Dict(item.value, item.label));
        }
        return dicts;
    }

    public static List<Dict> getFinanceReportList() {
        List<RPTReportEnum> reportEnums = RPTReportEnum.getFinanceReports();
        List<Dict> dicts = Lists.newArrayList();
        for (RPTReportEnum item : reportEnums) {
            dicts.add(new Dict(item.value, item.label));
        }
        return dicts;
    }
}
