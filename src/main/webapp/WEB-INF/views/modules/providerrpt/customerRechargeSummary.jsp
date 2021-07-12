
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <title>客户充值汇总</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
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
                $("#searchForm").attr("action","${ctx}/rpt/provider/customerRecharge/customerRechargeSummary");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/customerRecharge/checkExportTask?"+ (new Date()).getTime(),
                    data:$(searchForm).serialize(),
                    success: function (data) {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if(data && data.success == true){
                            top.$.jBox.closeTip();
                            top.$.jBox.confirm("确认要导出数据吗？", "系统提示", function (v, h, f) {
                                if (v == "ok") {
                                    top.$.jBox.tip('请稍候...', 'loading');
                                    $.ajax({
                                        type: "POST",
                                        url: "${ctx}/rpt/provider/customerRecharge/export?"+ (new Date()).getTime(),
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
    <li class="active"><a href="javascript:void(0);">客户充值汇总</a></li>
    <li>
        <a href="${ctx}/rpt/provider/rechargeRecord/rechargeRecordByPage">客户充值明细</a>
    </li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/customerRecharge/customerRechargeSummary" method="post" class="breadcrumb form-search"  >
    <div style="width: 90%">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label >客  户:</label>
        <select id="customerId" name="customerId" class="input-small"
                style="width:250px;">
            <option value=""
                    <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${fns:getMyCustomerListFromMS()}" var="customer">
                <option value="${customer.id}" <c:out value="${(rptSearchCondition.customerId eq customer.id)?'selected=selected':''}" />>${customer.name}</option>
            </c:forEach>
        </select>

        <label>年份：</label>
        <select id="selectedYear" name="selectedYear" class="input-small" style="width:85px;">
            <c:forEach items="${fns:getReportQueryYears()}" var="year">
                <option value="${year}" <c:out value="${(rptSearchCondition.selectedYear eq year)?'selected=selected':''}" />>${year}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;

        <label>月份：</label>
        <select id="selectedMonth" name="selectedMonth" class="input-mini" style="width:85px;">
            <c:forEach var="i" begin="0" end="11" step="1">
                <option value="${i+1}" <c:out value="${(rptSearchCondition.selectedMonth eq i+1)?'selected=selected':''}" />>${i+1}</option>
            </c:forEach>
        </select>
        &nbsp;
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>
        &nbsp;&nbsp;
        <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 250px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：实时数据<br/>
                    统计方式：充值时间<br/>
                    栏位说明：<br/>
                    【合计】=在线充值+财务充值+线下充值
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}"/>
<%--<script type="text/javascript">--%>
<%--    $(document).ready(function() {--%>
<%--        if($("#contentTable tbody>tr").length>0) {--%>
<%--            //无数据报错--%>
<%--            var h = $(window).height();--%>
<%--            var w = $(window).width();--%>
<%--            $("#contentTable").toSuperTable({--%>
<%--                width: w-10,--%>
<%--                height: h - 248,--%>
<%--                fixedCols: 1,--%>
<%--                headerRows: 3,--%>
<%--                colWidths:--%>
<%--                    [200,300,--%>
<%--                        300, 300, 250,250,250],--%>
<%--                onStart: function () {--%>
<%--                },--%>
<%--                onFinish: function () {--%>
<%--                }--%>
<%--            });--%>
<%--        }--%>
<%--        else {--%>
<%--            var h = $(window).height();--%>
<%--            $("#divGrid").height(h-248);--%>
<%--        }--%>

<%--    });--%>
<%--</script>--%>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table  table-bordered table-condensed">
        <thead>
<%--        <tr style="height: 4px; border-width: 0px; padding: 0px; margin: 0px;visibility: hidden;">--%>
<%--            <th width="100"></th>--%>

<%--            <th width="200"></th>--%>
<%--            <th width="200"></th>--%>
<%--            <th width="200"></th>--%>
<%--            <th width="200"></th>--%>
<%--            <th width="200"></th>--%>
<%--            <th width="200"></th>--%>

<%--        </tr>--%>
        <tr >
            <th rowspan="2"  width="100" >序号</th>
            <th rowspan="2"  width="200">客户编号</th>
            <th rowspan="2"  width="200">客户</th>
            <th rowspan="2"  width="200">业务员</th>
            <th colspan="4"  width="800">充值金额</th>

        </tr>
        <tr >
            <th >在线充值</th>
            <th >财务充值</th>
            <th >线下充值</th>
            <th >合计</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="totalPayAmount" value="0"/>
        <c:set var="totalPlatformFee" value="0"/>
        <c:set var="totalRechargeFee" value="0"/>
        <c:set var="total" value="0"/>
        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${list}" var="item">
            <tr >
                <c:set var="rowIndex" value="${rowIndex+1}"/>
                <td>${rowIndex}</td>
                <td>${item.customerCode}</td>
                <td>${item.customerName}</td>
                <td>${item.salesName}</td>

                <td><fmt:formatNumber value="${item.onlineRechargeAmount}" pattern="0.00"/></td>
                <c:set var="totalPayAmount" value="${totalPayAmount + item.onlineRechargeAmount}"/>

                <td><fmt:formatNumber value="${item.financialRechargeAmount}" pattern="0.00"/></td>
                <c:set var="totalPlatformFee" value="${totalPlatformFee + item.financialRechargeAmount}"/>

                <td><fmt:formatNumber value="${item.offlineRechargeAmount}" pattern="0.00"/></td>
                <c:set var="totalRechargeFee" value="${totalRechargeFee + item.offlineRechargeAmount}"/>

                <td><fmt:formatNumber value="${item.onlineRechargeAmount+item.financialRechargeAmount + item.offlineRechargeAmount}" pattern="0.00"/></td>
                <c:set var="total" value="${total + item.onlineRechargeAmount+item.financialRechargeAmount + item.offlineRechargeAmount}"/>
            </tr>
        </c:forEach>
        <c:if test="${list.size()>0}">
            <td colspan="4">合计</td>
            <td><fmt:formatNumber value="${totalPayAmount}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${totalPlatformFee}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${totalRechargeFee}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${total}" pattern="0.00"/></td>
        </c:if>
        </tbody>
    </table>
</div>
</body>
</html>


