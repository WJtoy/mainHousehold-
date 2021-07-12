<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>客服日完工图表</title>
    <script src="${ctxStatic}/echarts/echarts.min.js"></script>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>

    <script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action", "${ctx}/rpt/provider/gradedOrder/keFuOrderCompleteChart");
                $("#searchForm").submit();
            });
        });
    </script>

    <style>
        h4 {
            margin-left: 20px;
            font-family: 'Telex', sans-serif;
            font-weight: bold;
            line-height: 30px;
            color: #2b2b2b;
            text-rendering: optimizelegibility;
        }

        c {
            color: #525C66;
            text-decoration: none;

        }

        b {
            text-decoration: none;
            float: right;
        }

        .top-10 {
            float: left;
            width: 400px;
            margin-top: 10px;
            margin-left: 20px;
            background: #fff;
            border: 1px solid #FFF;
            box-shadow: #d0d0d0 1px 1px 10px 0px;
        }

        .top-10 ul {
            counter-reset: section;
        }

        .top-10 li {

            width: 350px;
            line-height: 50px;
            height: 50px;
            overflow: hidden;
            color: #525C66;
            font-size: 14px;

        }

        .top-10 li:nth-child(1):before {
            background: #FF3300
        }

        .top-10 li:nth-child(2):before {
            background: #FF6600
        }

        .top-10 li:nth-child(3):before {
            background: #FFCC00
        }

        .top-10 li:before {
            counter-increment: section;
            content: counter(section);
            display: inline-block;
            padding: 0 12px;
            margin-right: 10px;
            height: 18px;
            line-height: 18px;
            background: #b8c2cc;
            color: #fff;
            border-radius: 3px;
            font-size: 9px
        }


    </style>
</head>
<body>
<ul class="nav nav-tabs">
    <li>
        <a href="${ctx}/rpt/provider/gradedOrder/kefuOrderCompletedDailyRpt">客服完工情况</a>
    </li>
    <li class="active"><a href="javascript:void(0);">客服完工图表</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="searchForm" modelAttribute="rptSearchCondition" method="post"
           class="breadcrumb form-search">
    <div>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>客服：</label>
        <form:select path="kefuId" style="width:180px;">
            <form:option value="0" label="所有" />
            <form:options items="${fns:getKefuList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
        </form:select>
        &nbsp;&nbsp;
        <label>客服类型：</label>
        <select id="subFlag" name="subFlag" class="input-small" style="width:125px;">
            <option value="-1" <c:out value="${(empty rptSearchCondition.subFlag)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${keFuTypeEnumList}" var="dict">
                <option value="${dict.value}" <c:out
                        value="${(rptSearchCondition.subFlag eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
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
        <label>服务品类：</label>
        <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
            <option value="0" <c:out value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}"/>>所有
            </option>
            <c:forEach items="${productCategoryList}" var="dict">
                <option value="${dict.id}" <c:out
                        value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/>
    </div>
</form:form>
<sys:message content="${message}"/>
<div style="width: 1720px;height:650px;">
    <div id="main3" style="width: 1280px;height:650px;float: left"></div>
    <script type="text/javascript">
        var myChart2 = echarts.init(document.getElementById('main3'));
        var daySums = ${fns:toGson(daySums)};
        var createDates = ${fns:toGson(createDates)};

        setTimeout(function () {
            option = {
                title: {
                    left: 'left',
                    text: '客服${rptSearchCondition.selectedMonth}月完工合计',
                    x: 'center'
                },
                tooltip: {
                    trigger: 'axis'
                },
                legend: {
                    data: ['每日完工合计']
                },
                grid: {
                    left: '3%',
                    right: '4%',
                    bottom: '3%',
                    containLabel: true
                },
                xAxis: {
                    type: 'category',
                    data: createDates
                },
                yAxis: {
                    type: 'value',
                    minInterval: 1,
                    boundaryGap: [0, 0.1]
                },
                series: [
                    {
                        name: '每日完工合计',
                        type: 'bar',
                        smooth: true,
                        color: "#58AFFF",
                        data: daySums,
                        itemStyle:
                            {
                                normal:
                                    {
                                        label: {
                                            show: true,
                                            position: 'top'
                                        }
                                    }
                            }
                    }

                ]
            };

            myChart2.setOption(option);
        });
    </script>
    <div class="top-10">
        <h4>客服${rptSearchCondition.selectedMonth}月派单排名</h4>

        <ul>
            <c:forEach items="${list}" var="item">
                <li>
                    <c>${item.kefuName}</c>
                    <b><fmt:formatNumber maxFractionDigits="2">${item.total}</fmt:formatNumber></b>
                </li>

            </c:forEach>
        </ul>
    </div>
</div>
</body>
</html>
