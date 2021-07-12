﻿<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <title>网点付款清单</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            $(".triggle").on('hover', function(){
                $(".border").css({
                    display:"block"
                })
            })
            $(".triggle").on('mouseleave', function(){
                $(".border").css({
                    display:"none"
                })
            })
            $("#btnSubmit").click(function() {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action","${ctx}/rpt/provider/servicePointInvoice/servicePointInvoiceRpt");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/servicePointInvoice/checkExportTask?"+ (new Date()).getTime(),
                    data:$(searchForm).serialize(),
                    success: function (data) {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if(data && data.success == true){
                            top.$.jBox.closeTip();
                            top.$.jBox.confirm("确认要导出下游安维付款清单吗？", "系统提示", function (v, h, f) {
                                if (v == "ok") {
                                    top.$.jBox.tip('请稍候...', 'loading');
                                    $.ajax({
                                        type: "POST",
                                        url: "${ctx}/rpt/provider/servicePointInvoice/export?"+ (new Date()).getTime(),
                                        data:$(searchForm).serialize(),
                                        success: function (data) {
                                            if(ajaxLogout(data)){
                                                return false;
                                            }
                                            if(data && data.success == true){
                                                top.$.jBox.closeTip();
                                                top.$.jBox.tip(data.message, "success");
                                                $('#btnExport').removeAttr('disabled');
                                                return false;
                                            }
                                            else if( data && data.message){
                                                top.$.jBox.error(data.message,"导出错误");
                                            }
                                            else{
                                                top.$.jBox.error("导出错误","错误提示");
                                            }
                                            $('#btnExport').removeAttr('disabled');
                                            top.$.jBox.closeTip();
                                            return false;
                                        },
                                        error: function (e) {
                                            $('#btnExport').removeAttr('disabled');
                                            ajaxLogout(e.responseText,null,"导出错误，请重试!");
                                            top.$.jBox.closeTip();
                                        }
                                    });
                                }
                            }, {buttonsFocus: 1});
                            $('#btnExport').removeAttr('disabled');
                            top.$.jBox.closeTip();
                            return false;
                        }
                        else if( data && data.message){
                            top.$.jBox.error(data.message,"导出错误");
                        }
                        else{
                            top.$.jBox.error("导出错误","错误提示");
                        }
                        $('#btnExport').removeAttr('disabled');
                        top.$.jBox.closeTip();
                        return false;
                    },
                    error: function (e) {
                        $('#btnExport').removeAttr('disabled');
                        ajaxLogout(e.responseText,null,"导出错误，请重试!");
                        top.$.jBox.closeTip();
                    }
                });
                top.$('.jbox-body .jbox-icon').css('top', '55px');
            });

        });

    </script>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }

        .parent:after{
            content:"";
            height:0;
            line-height:0;
            display:block;
            visibility:hidden;
            clear:both;
        }

        .target{
            display:none;
            z-index: 4;
        }

        .triggle:hover + .target {
            display: block;
        }

        .border{
            display: none;
            opacity: 0.8;
            width: 0 !important;
            border-bottom:solid 12px #1B1E24;
            border-left:12px solid transparent;
            border-right: 6px solid transparent;
            boder-top: 0px solid transparent;
        }
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <li>
    <a href="${ctx}/rpt/provider/servicePointInvoiceSummary/servicePointPaymentSummary">网点付款汇总</a>
    </li>
    <li class="active"><a href="javascript:void(0);">网点付款清单</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/servicePointInvoice/servicePointInvoiceRpt" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label class="control-label">服务网点：</label>
        <rpt:servicePointSelector id="servicePoint" name="servicePointId" value="${rptSearchCondition.servicePointId}"
                                  labelName="servicePointName" labelValue="${rptSearchCondition.servicePointName}"
                                  width="900" height="700" noblackList="true" callbackmethod="" cssClass="required"/>
        &nbsp;&nbsp;
        <label>结帐单号：</label>
        <input type = "text" id="withdrawNo" name="withdrawNo" value="${rptSearchCondition.withdrawNo}" maxlength="20" class="input-small"/>
        &nbsp;&nbsp;
        <label>银行：</label>
        <select id="bank" name="bank" style="width:140px;">
            <option value="" <c:out value="${(empty rptSearchCondition.bank)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${fns:getDictListFromMS('banktype')}" var="bankDict"><!-- 切换为微服务 -->
            <option value="${bankDict.value}" <c:out value="${(rptSearchCondition.bank eq bankDict.value)?'selected=selected':''}" />>${bankDict.label}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>结算方式：</label>
        <select id="paymentType" name="paymentType" class="input-small" style="width:125px;">
            <option value="" <c:out value="${(empty rptSearchCondition.paymentType)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${fns:getDictListFromMS('PaymentType')}" var="dict"><%--切换为微服务--%>
                <option value="${dict.value}" <c:out value="${(rptSearchCondition.paymentType eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>状态：</label>
        <select id="status" name="status" class="input-small" style="width:125px;">
            <option value="0" <c:out value="${(empty rptSearchCondition.status)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${statusList}" var="dict">
                <option value="${dict.value}" <c:out
                        value="${(rptSearchCondition.status eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
            </c:forEach>
        </select>
    </div>
    <div style="margin-top:5px">
<%--        <label>请款日期：</label>--%>
<%--        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"--%>
<%--               value="<fmt:formatDate value='${rptSearchCondition.beginDate}' pattern='yyyy-MM-dd' type='date'/>"--%>
<%--               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>--%>
<%--        <label>~</label>--%>
<%--        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"--%>
<%--               value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"--%>
<%--               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>--%>
<%--        &nbsp;&nbsp;--%>

        <label>付款日期：</label>
        <input id="beginInvoiceDate" name="beginInvoiceDate" type="text" readonly="readonly" style="width:120px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.beginInvoiceDate}' pattern='yyyy-MM-dd' type='date'/>" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
        <label>~</label>
        <input id="endInvoiceDate" name="endInvoiceDate" type="text" readonly="readonly" style="width:125px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.endInvoiceDate}' pattern='yyyy-MM-dd' type='date'/>" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询" />
        &nbsp;&nbsp;
        <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
    </div>

    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 150px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：实时数据 <br/>
                    统计方式：付款时间
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>

</form:form>
<sys:message content="${message}"/>
<script type="text/javascript">
    $(document).ready(function() {
        if($("#contentTable tbody>tr").length>0) {
            //无数据报错
            var h = $(window).height();
            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w-10,
                height: h - 248,
                fixedCols: 2,
                headerRows: 1,
                colWidths:
                    [60,
                        130, 140,100, 100, 170,
                        160, 80, 100, 100,
                        100,120, 200],
                onStart: function () {

                },
                onFinish: function () {

                }
            });
        }
        else {
            var h = $(window).height();
            $("#divGrid").height(h-248);
        }
    });
</script>
<div id="divGrid" style="overflow-x:auto;">
    <table id="contentTable" class="table table-bordered table-condensed table-hover" style="table-layout:fixed;">
        <thead>
        <tr>
            <th width="60">序号</th>

            <th width="130">网点编号</th>
            <th width="140">网点名称</th>
<%--            <th width="100">负责人</th>--%>
<%--            <th width="100">手机</th>--%>
<%--            <th width="100">联系电话</th>--%>
            <th width="100">开户银行</th>
            <th width="100">开户人</th>
            <th width="170">银行账号</th>

            <th width="160">结账单号</th>
            <th width="80">结算方式</th>
            <th width="100">请款日期</th>
            <th width="100">付款日期</th>

            <th width="100">付款金额</th>
            <th width="120">平台服务费</th>
            <th width="200">付款描述</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="totalPayAmount" value="0"/>
        <c:set var="totalPlatformFee" value="0"/>
        <c:set var="i" value="0"/>
        <c:forEach items="${page.list}" var="item">
            <c:set var="i" value="${i+1}"/>
            <tr>
                <td>${i}</td>
                <td>${item.servicePointNo}</td>
                <td>${item.servicePointName}</td>
<%--                <td>${item.engineerName}</td>--%>
<%--                <td>${item.contactInfo1}</td>--%>
<%--                <td>${item.contactInfo2}</td>--%>
                <td>${item.bank.label}</td>
                <td>${item.bankOwner}</td>
                <td>${item.bankNo}</td>

                <td>${item.withdrawNo}</td>
                <td>${item.paymentType.label}</td>
                <td><fmt:formatDate value="${item.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td><fmt:formatDate value="${item.payDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>

                <td><fmt:formatNumber value="${item.payAmount}" pattern="0.00"/></td>
                <c:set var="totalPayAmount" value="${totalPayAmount + item.payAmount}"/>
                <td><fmt:formatNumber value="${item.platformFee}" pattern="0.00"/></td>
                <c:set var="totalPlatformFee" value="${totalPlatformFee + item.platformFee}"/>
                <td>${item.remarks}</td>
            </tr>
        </c:forEach>
        <c:if test="${page.list.size()>0}">
            <tr style="font-weight: bold; color: red;">
                <td colspan="10">合计</td>
                <td><fmt:formatNumber value="${totalPayAmount}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${totalPlatformFee}" pattern="0.00"/></td>
                <td></td>
            </tr>
        </c:if>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>


