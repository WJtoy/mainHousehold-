<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>每日工单图表</title>
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
                $("#searchForm").attr("action", "${ctx}/rpt/provider/createdOrderWork/orderSourceChartRpt");
                $("#searchForm").submit();
            });
        });
    </script>
</head>
<body>
<ul id="navtabs" class="nav nav-tabs">
    <li>
        <a href="${ctx}/rpt/provider/createdOrderWork/createdOrderRpt">每日工单统计</a>
    </li>
    <li class="active"><a href="javascript:void(0);">每日工单图表</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/createdOrderWork/orderSourceChartRpt" method="post" class="breadcrumb form-search">
    <div>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
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
        <label>客　　户：</label>
        <select id="customerId" name="customerId" class="input-small" style="width:225px;">
            <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}"/>>所有
            </option>
            <c:forEach items="${fns:getCustomerList()}" var="dict">
                <option value="${dict.id}" <c:out
                        value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>服务品类：</label>
        <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
            <option value="0" <c:out value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${productCategoryList}" var="dict">
                <option value="${dict.id}" <c:out value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询" />
    </div>
</form:form>
<sys:message content="${message}"/>
<div id="main" style="width: 1680px;height:750px;"></div>
<script type="text/javascript">
    var myChart = echarts.init(document.getElementById('main'));
    var orderCreateDates = ${fns:toGson(orderCreateDates)};
    var manualOrders = ${fns:toGson(manualOrders)};
    var tmOrders = ${fns:toGson(tmOrders)};
    var jdOrders = ${fns:toGson(jdOrders)};
    var pddOrders = ${fns:toGson(pddOrders)};
    var restOrders = ${fns:toGson(restOrders)};
    var daySums = ${fns:toGson(daySums)};
    setTimeout(function () {
        option = {

            title: {
                left: 'left',
                text: '${rptSearchCondition.selectedMonth}月工单统计',
                x: 'center'
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                    type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                }
            },
            legend: {
                data: ['总单量','手动工单', '天猫工单','拼多多工单','京东工单','其他B2B工单']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '10%',
                containLabel: true
            },
            xAxis: [
                {
                    type: 'category',
                    data: orderCreateDates
                }
            ],
            yAxis: [
                {
                    type: 'value'
                }
            ],
            series: [
                {
                    name: '总单量',
                    type: 'bar',
                    color: "#6495ED",
                    data: daySums
                },
                {
                    name: '手动工单',
                    type: 'bar',
                    color: "#bda29a",
                    data: manualOrders
                },
                {
                    name: '其他B2B工单',
                    type: 'bar',
                    stack: 'b2b工单',
                    color: "#003300",
                    data: restOrders
                },
                {
                    name: '京东工单',
                    type: 'bar',
                    stack: 'b2b工单',
                    color: "#006633",
                    data: jdOrders
                },
                {
                    name: '拼多多工单',
                    type: 'bar',
                    stack: 'b2b工单',
                    color: "#009900",
                    data: pddOrders
                },
                {
                    name: '天猫工单',
                    type: 'bar',
                    stack: 'b2b工单',
                    color: "#50B683",
                    data: tmOrders
                }
            ]
        };

        myChart.setOption(option);

    });
</script>
<div id="main3" style="width: 1680px;height:750px;"></div>
<script type="text/javascript">
    var myChart2 = echarts.init(document.getElementById('main3'));
    var createDates = ${fns:toGson(createDates)};
    var manualOrderRates = ${fns:toGson(manualOrderRates)};
    var b2bOrderRates = ${fns:toGson(b2bOrderRates)};

    setTimeout(function () {
        option = {
            title: {
                left: 'left',
                text: '${rptSearchCondition.selectedMonth}月工单比率',
                x: 'center'
            },
            tooltip: {
                trigger: 'axis',
                formatter: function (params) {
                    var res = params[0].name + '<br/>';
                    for (var i = 0; i < params.length; i++) {
                        res += params[i].seriesName + ' : ' + params[i].value + '%</br>';
                    }
                    return res;
                },
            },
            legend: {
                data: ['手动工单', 'B2B工单']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: createDates
            },
            yAxis: {
                type: 'value',
                axisLabel: {
                    show: true,
                    interval: 'auto',
                    formatter: '{value} %'
                },
                max: 100,
                min: 0,
                splitNumber: 10
            },
            series: [
                {
                    name: '手动工单',
                    type: 'line',
                    smooth: true,
                    color: "#91c7ae",
                    data: manualOrderRates
                },
                {
                    name: 'B2B工单',
                    type: 'line',
                    smooth: true,
                    color: "#006633",
                    data: b2bOrderRates
                }

            ]
        };

        myChart2.setOption(option);
    });
</script>
</body>
</html>

