<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>投诉统计图表</title>
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
                $("#searchForm").attr("action", "${ctx}/rpt/provider/complainStatistics/complainStatisticsDailyChart");
                $("#searchForm").submit();
            });
        });
    </script>
</head>
<body>
<ul id="navtabs" class="nav nav-tabs">
    <li>
        <a href="${ctx}/rpt/provider/complainStatistics/complainStatisticsDailyRpt">每日投诉统计报表</a>
    </li>
    <li class="active"><a href="javascript:void(0);">每日投诉统计图表</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }" />
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/complainStatistics/complainStatisticsDailyChart" method="post" class="breadcrumb form-search">
    <div>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>下单时间：</label>
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false,maxDate:'${rptSearchCondition.endPlanDate}'});"/>
        &nbsp;&nbsp;
        <c:if test="${!currentuser.isCustomer() && !currentuser.isSaleman()}">
            <label class="control-label">客服：</label>
            <form:select path="kefuId" style="width:180px;">
                <form:option value="0" label="所有" />
                <form:options items="${fns:getKefuList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
            </form:select>
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
            <label class="control-label">服务网点：</label>
            <rpt:servicePointSelector id="servicePoint" name="servicePointId" value="${rptSearchCondition.servicePointId}"
                                      labelName="servicePointName" labelValue="${rptSearchCondition.servicePointName}"
                                      width="900" height="700" noblackList="true" callbackmethod="" cssClass="required"/>

        </c:if>
        &nbsp;&nbsp;
        <label>区域：</label>
        <sys:treeselectareanew id="area" name="areaId" value="${rptSearchCondition.areaId}" levelValue="${rptSearchCondition.areaLevel}" nodeLevel="true"
                        labelName="areaName" labelValue="${rptSearchCondition.areaName }" title="区域"
                        url="/sys/area/treeDataNew?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1"
                        nameLevel="3" />
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询" />
    </div>
</form:form>
<sys:message content="${message}"/>
<div id="main" style="width: 1680px;height:750px;"></div>
<script type="text/javascript">
    var myChart = echarts.init(document.getElementById('main'));
    var orderCreateDates = ${fns:toGson(orderCreateDates)};
    var strDayComplainSum = ${fns:toGson(strDayComplainSum)};
    var strDayTheTotalOrderSum = ${fns:toGson(strDayTheTotalOrderSum)};

    setTimeout(function () {
        option = {

            title: {
                left: 'left',
                text: '月投诉统计',
                x: 'center'
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                    type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                }
            },
            legend: {
                data: ['投诉单量']
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
                    name: '投诉单量',
                    type: 'bar',
                    color: "#FFCC00",
                    data: strDayComplainSum
                }
            ]
        };

        myChart.setOption(option);

    });
</script>
<div id="main2" style="width: 1680px;height:750px;"></div>
<script type="text/javascript">
    var myChart2 = echarts.init(document.getElementById('main2'));
    var createDates = ${fns:toGson(createDates)};
    var strDayComplainSumRate = ${fns:toGson(strDayComplainSumRate)};
    var rate = ${rate!=null?rate:5};


    setTimeout(function () {
        option = {
            title: {
                left: 'left',
                text: '月投诉比率',
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
                }
            },
            legend: {
                data: ['投诉比率']
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
                max: rate,
                min: 0,
                splitNumber: 10
            },
            series: [
                {
                    name: '投诉比率',
                    type: 'line',
                    smooth: true,
                    color: "#FFCC00",
                    data: strDayComplainSumRate
                }


            ]
        };

        myChart2.setOption(option);
    });
</script>
</body>
</html>

