<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<html>
<head>
    <title>B2B接入统计</title>
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

            $("#btnSubmit").click(function(){
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action","${ctx}/tmall/rpt/tmallorder/workcardDailyQty");
                $("#searchForm").submit();
            });
        });
    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">B2B每日接入单量</a></li>
</ul>
<form:form id="searchForm" modelAttribute="workcardDailySearch" action="${ctx}/tmall/rpt/tmallorder/workcardDailyQty" method="post" class="breadcrumb form-search form-horizontal">
    <div>
        <input type="hidden" name="isSearching" value="${workcardDailySearch.isSearchingYes}"/>
        <label>工单来源：</label>
        <select id="dataSource" name="dataSource" class="input-small" style="width:85px;">
            <option value="" selected="selected">所有</option>
        <option value="2" <c:out value="${workcardDailySearch.dataSource == 2?'selected=selected':''}"/>>天猫</option>
            <option value="3" <c:out value="${workcardDailySearch.dataSource == 3?'selected=selected':''}"/>>康宝</option>
    </select>
        &nbsp;&nbsp;
        <label>统计类型：</label>
        <select id="statisticType" name="statisticType" class="input-small" style="width:140px;">
            <option value="" selected="selected">所有</option>
        <option value="10" <c:out value="${workcardDailySearch.statisticType == 10 ?'selected=selected':''}"/>>B2B接入单量</option>
        <option value="11" <c:out value="${workcardDailySearch.statisticType == 11?'selected=selected':''}"/>>B2B接入单量(含重复)</option>
        <option value="20" <c:out value="${workcardDailySearch.statisticType == 20 ?'selected=selected':''}"/>>解析成功单量</option>
        <option value="30" <c:out value="${workcardDailySearch.statisticType == 30 ?'selected=selected':''}"/>>工单系统接入单量</option>
    </select>
        &nbsp;&nbsp;

        <label>年份：</label>
        <select id="selectedYear" name="selectedYear" class="input-small" style="width:85px;">
            <c:forEach items="${fns:getReportQueryYears()}" var="year">
                <option value="${year}" <c:out value="${(workcardDailySearch.selectedYear eq year)?'selected=selected':''}" />>${year}</option>
            </c:forEach>
        </select>
        &nbsp; &nbsp;

        <label>月份：</label>
        <select id="selectedMonth" name="selectedMonth" class="input-mini" style="width:85px;">
            <c:forEach var="i" begin="0" end="11" step="1">
                <option value="${1+i}" <c:out value="${(workcardDailySearch.selectedMonth eq 1+i)?'selected=selected':''}" />>${1+i}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;

        <input id="btnSubmit" class="btn btn-primary"type="button" value="查询" />

    </div>
</form:form>
<sys:message content="${message}" />
<div id="divGrid" style="overflow-x:hidden;">
    <table id="contentTable" class="table table-bordered table-condensed " style="table-layout:fixed;">
        <thead>
        <tr>
            <th rowspan="2" width="140">工单来源</th>
            <th rowspan="2" width="140">统计类型</th>
            <c:choose>
                <c:when test="${workcardDailySearch.rowsCount == 0}">
                    <th rowspan="2">每日单量</th>
                </c:when>
                <c:otherwise>
                    <th colspan="${workcardDailySearch.days}" width="${workcardDailySearch.days*80}">每日单量</th>
                </c:otherwise>
            </c:choose>
            <th rowspan="2" width="120">合计(单)</th>
        </tr>
        <c:if test="${workcardDailySearch.rowsCount != 0}">
            <tr>
                <c:forEach var="i" begin="1" end="${workcardDailySearch.days}" step="1">
                    <th>${i}</th>
                </c:forEach>
            </tr>
        </c:if>
        </thead>
        <tbody>
        <c:set var="rowIndex" value="0"></c:set>
        <c:forEach items="${workcardDailySearch.list}" var="item">
            <c:set var="rowIndex" value="${rowIndex+1}"></c:set>
            <tr>
                <td>${item.dataSource==2?"天猫":item.dataSource==3?"康宝":""}</td>
                <td>${item.statisticType==10?"B2B接入工单":item.statisticType==11?"B2B接入工单(含重复)":item.statisticType==20?"解析成功单量":item.statisticType==30?"工单系统接入单量":""}</td>
                <c:forEach var="i" begin="1" end="${workcardDailySearch.days}" step="1">
                    <c:set var="colname" value="d${i}" />
                    <td>
                    <fmt:formatNumber maxFractionDigits="0">${item[colname]}</fmt:formatNumber>
                    </td>
                </c:forEach>
               <td>
                    <fmt:formatNumber maxFractionDigits="0">${item.total}</fmt:formatNumber>
               </td>
            </tr>
            <%--<c:choose>--%>
                <%--<c:when test="${item.rowNumber < workcardDailySearch.sumRowNumber}">--%>
                    <%--<tr>--%>
                        <%--<td>${rowIndex}</td>--%>
                        <%--<td>${item.kefuName}</td>--%>
                        <%--<c:forEach var="i" begin="1" end="${workcardDailySearch.days}" step="1">--%>
                            <%--<c:set var="colname" value="d${i}" />--%>
                            <%--<td>--%>
                                <%--<c:if test="${item[colname]!=0}">--%>
                                    <%--<fmt:formatNumber maxFractionDigits="0">${item[colname]}</fmt:formatNumber>--%>
                                <%--</c:if>--%>
                            <%--</td>--%>
                        <%--</c:forEach>--%>
                        <%--<td>--%>
                            <%--<c:if test="${item.total!=0}">--%>
                                <%--<fmt:formatNumber maxFractionDigits="0">${item.total}</fmt:formatNumber>--%>
                            <%--</c:if>--%>
                        <%--</td>--%>
                    <%--</tr>--%>
                <%--</c:when>--%>
                <%--<c:when test="${item.rowNumber == workcardDailySearch.sumRowNumber}">--%>
                    <%--<tr>--%>
                        <%--<td></td>--%>
                        <%--<td>${item.kefuName}</td>--%>
                        <%--<c:forEach var="i" begin="1" end="${workcardDailySearch.days}" step="1">--%>
                            <%--<c:set var="colname" value="d${i}" />--%>
                            <%--<td>--%>
                                <%--<c:if test="${item[colname]!=0}">--%>
                                    <%--<fmt:formatNumber maxFractionDigits="0">${item[colname]}</fmt:formatNumber>--%>
                                <%--</c:if>--%>
                            <%--</td>--%>
                        <%--</c:forEach>--%>
                        <%--<td>--%>
                            <%--<c:if test="${item.total!=0}">--%>
                                <%--<fmt:formatNumber maxFractionDigits="0">${item.total}</fmt:formatNumber>--%>
                            <%--</c:if>--%>
                        <%--</td>--%>
                    <%--</tr>--%>
                <%--</c:when>--%>
                <%--<c:when test="${item.rowNumber == workcardDailySearch.perRowNumber}">--%>
                    <%--<tr>--%>
                        <%--<td></td>--%>
                        <%--<td>${item.kefuName}</td>--%>
                        <%--<c:forEach var="i" begin="1" end="${workcardDailySearch.days}" step="1">--%>
                            <%--<c:set var="colname" value="d${i}" />--%>
                            <%--<td>--%>
                                <%--<c:if test="${item[colname]!=0}">--%>
                                    <%--<fmt:formatNumber maxFractionDigits="2">${item[colname]}</fmt:formatNumber>%--%>
                                <%--</c:if>--%>
                            <%--</td>--%>
                        <%--</c:forEach>--%>
                        <%--<td>--%>
                            <%--<c:if test="${item.total!=0}">--%>
                                <%--<fmt:formatNumber maxFractionDigits="2">${item.total}</fmt:formatNumber>%--%>
                            <%--</c:if>--%>
                        <%--</td>--%>
                    <%--</tr>--%>
                <%--</c:when>--%>
            <%--</c:choose>--%>
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
                colWidths: [120, 120,
                    <c:forEach var="i" begin="1" end="${workcardDailySearch.days}" step="1">80,</c:forEach>
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

