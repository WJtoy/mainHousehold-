<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<html>
<head>
    <title>每日对账方式情况及统计</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <script type="text/javascript" language="javascript">
        $(document).ready(function() {
                $("#btnSubmit").click(function () {
                    top.$.jBox.tip('请稍候...', 'loading');
                    $("#searchForm").attr("action", "${ctx}/rpt/provider/ChargeDaily/ChargeDayRptReport");
                    $("#searchForm").submit();
                });
                $("#btnExport").click(function () {
                    top.$.jBox.tip('请稍候...', 'loading');
                    $("#btnExport").prop("disabled", true);
                    $.ajax({
                        type: "POST",
                        url: "${ctx}/rpt/provider/ChargeDaily/checkExportTask?"+ (new Date()).getTime(),
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
                                            url: "${ctx}/rpt/provider/ChargeDaily/export?"+ (new Date()).getTime(),
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
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">每日对账方式情况统计</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/ChargeDaily/ChargeDayRptReport" method="post" class="breadcrumb form-search">
    <div>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
            <%--<label>客服：</label>--%>
            <%--<input id="kefuID" name="kefuName" value="${rptSearchCondition.kefuName}"/>--%>
            <%--&nbsp;&nbsp;--%>
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
                <option value="${1+i}" <c:out value="${(rptSearchCondition.selectedMonth eq 1+i)?'selected=selected':''}" />>${1+i}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary"type="button" value="查询"/>
        &nbsp;&nbsp;
        <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
    </div>
</form:form>
<sys:message content="${message}" />
<div id="divGrid" style="overflow-x:hidden;">
    <table id="contentTable" class="table table-bordered table-condensed table-hover " style="table-layout:fixed" cellspacing="0" width="100%">
        <thead>
        <tr>
            <th rowspan="2" width="60">序号</th>
            <th rowspan="2" width="120">统计名称</th>
            <c:choose>
                <c:when test="${rptSearchCondition.rowsCount == 0}">
                    <th rowspan="2">每日对账(单)</th>
                </c:when>
                <c:otherwise>
                    <th colspan="${rptSearchCondition.days}" width="${rptSearchCondition.days*80}">每日对账(单)</th>
                </c:otherwise>
            </c:choose>
            <th rowspan="2" width="120">合计(单)</th>
        </tr>
        <c:if test="${rptSearchCondition.rowsCount != 0}">
            <tr>
                <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                    <th>${i}</th>
                </c:forEach>
            </tr>
        </c:if>
        </thead>
        <tbody>
        <c:set var="rowIndex" value="0"></c:set>
        <c:forEach items="${rptSearchCondition.list}" var="item">
            <c:set var="rowIndex" value="${rowIndex+1}"></c:set>
            <tr>
                <td>${rowIndex}</td>
                <td>${item.chargeWay}</td>
                <c:if test="${rowIndex==1}">
                    <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                        <c:set var="colname" value="d${i}" />
                        <td>
                            <c:if test="${item[colname]!=0}">
                                <fmt:formatNumber maxFractionDigits="0">${item[colname]}</fmt:formatNumber>
                            </c:if>
                        </td>
                    </c:forEach>

                </c:if>
                <c:if test="${rowIndex!=1}">
                    <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                        <c:set var="colname" value="${i-1}" />
                        <td>
                            <c:if test="${item.list[colname]!=0}">
                                <fmt:formatNumber maxFractionDigits="0">${item.list[colname]}</fmt:formatNumber>
                            </c:if>
                        </td>
                    </c:forEach>

                </c:if>

                <td>
                    <c:if test="${item.rowSum!=0}">
                        <fmt:formatNumber maxFractionDigits="0">${item.rowSum}</fmt:formatNumber>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<script type="text/javascript">
    $(document).ready(function() {
        if($("#contentTable tbody>tr").length>0) {
            //无数据报错
            var h = $(window).height();
            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w-10,
                height: h - 138,
                fixedCols: 2,
                headerRows: 2,
                colWidths: [60, 120,
                    <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">80,</c:forEach>
                    120],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
    });
</script>

</body>
</html>