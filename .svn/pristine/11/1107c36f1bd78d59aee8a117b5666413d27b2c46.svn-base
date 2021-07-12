<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>短信数量图表</title>
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
        $(document).ready(function() {
            $("#btnSubmit").click(function() {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action","${ctx}/rpt/provider/smsQtyStatistics/smsQtyStatisticsChart");
                $("#searchForm").submit();
            });
        });
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li>
        <a href="${ctx}/rpt/provider/smsQtyStatistics/smsQtyStatisticsReport">短信数量统计</a>
    </li>
    <li class="active"><a href="javascript:void(0);">短信数量统计图表</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/smsQtyStatistics/smsQtyStatisticsChart" method="post" class="breadcrumb form-search">
    <div>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>年份：</label>
        <select id="selectedYear" name="selectedYear" class="input-small" style="width:85px;">
            <c:forEach items="${fns:getReportQueryYears()}" var="year">
                <option value="${year}" <c:out value="${(rptSearchCondition.selectedYear eq year)?'selected=selected':''}" />>${year}</option>
            </c:forEach>
        </select>
        &nbsp; &nbsp;

        <label>月份：</label>
        <select id="selectedMonth" name="selectedMonth" class="input-mini" style="width:85px;">
            <c:forEach var="i" begin="0" end="11" step="1">
                <option value="${1+i}" <c:out value="${(rptSearchCondition.selectedMonth eq 1+i)?'selected=selected':''}" />>${1+i}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询" />
    </div>
</form:form>
<sys:message content="${message}"/>
<div id="main" style="width: 1680px;height:565px;"></div>
<script type="text/javascript">
    // 基于准备好的dom，初始化echarts实例
    var myChart = echarts.init(document.getElementById('main'));
    var mapList = ${fns:toGson(mapList)};

    setTimeout(function () {

        option = {
            title : {
                left: 'left',
                text: '${rptSearchCondition.selectedMonth}月短信合计',
                x:'center'
            },
            tooltip : {
                trigger: 'item',
                formatter: "{a} <br/>{b} : {c} ({d}%)"
            },
            color:['#91c7ae','#749f83','#546570','#6e7074','#61a0a8','#d48265','#bda29a','#CC0033'],
            legend: {

                data: ['派单','APP接单','客服预约','网点预约','验证码','客服工单详情界面','短信回访','订单取消']
            },
            series : [
                {
                    name: '数量',
                    type: 'pie',
                    radius : '75%',
                    center: ['50%', '50%'],
                    data:mapList,
                    itemStyle : {
                        normal : {
                            label : {
                                show : true,
                                formatter: "{b}({d}%)  "
                            }
                        }
                    }
                }
            ]
        };
        myChart.setOption(option);

    });
</script>

<div id="main2" style="width: 1680px;height:750px;"></div>
<script type="text/javascript">
    // 基于准备好的dom，初始化echarts实例
    var myChart1 = echarts.init(document.getElementById('main2'));
    var sendDates = ${fns:toGson(sendDateList)};
    var plannedList = ${fns:toGson(plannedList)};
    var acceptedAppList = ${fns:toGson(acceptedAppList)};
    var pendingList = ${fns:toGson(pendingList)};
    var pendingApps = ${fns:toGson(pendingApps)};
    var verificationCodes = ${fns:toGson(verificationCodes)};
    var orderDetailPages = ${fns:toGson(orderDetailPages)};
    var callBacks = ${fns:toGson(callBacks)};
    var cancelleds = ${fns:toGson(cancelleds)};

    setTimeout(function () {

        option = {

            title : {
                left: 'left',
                text: '${rptSearchCondition.selectedMonth}月短信统计',
                x:'center'
            },
            tooltip : {
                trigger: 'axis',
                axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                    type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                }
            },
            legend: {
                data:['派单','APP接单','客服预约','网点预约','验证码','客服工单详情界面','短信回访','订单取消']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '10%',
                containLabel: true
            },
            xAxis : [
                {
                    type : 'category',
                    data : sendDates
                }
            ],
            yAxis : [
                {
                    type : 'value'
                }
            ],
            series : [
                {
                    name:'派单',
                    type:'bar',
                    stack: '接派单',
                    color:"#91c7ae",
                    data:plannedList
                },
                {
                    name:'APP接单',
                    type:'bar',
                    stack: '接派单',
                    color:"#749f83",
                    data:acceptedAppList
                },
                {
                    name:'客服预约',
                    type:'bar',
                    stack: '预约',
                    color:"#546570",
                    data:pendingList
                },
                {
                    name:'网点预约',
                    type:'bar',
                    stack: '预约',
                    color:"#6e7074",
                    data:pendingApps
                },
                {
                    name:'验证码',
                    type:'bar',
                    color:"#61a0a8",
                    data:verificationCodes
                },

                {
                    name:'客服工单详情界面',
                    type:'bar',
                    color:"#d48265",
                    data:orderDetailPages
                },
                {
                    name:'短信回访',
                    type:'bar',
                    color:"#bda29a",
                    data:callBacks
                },
                {
                    name:'订单取消',
                    type:'bar',
                    color:"#CC0033",
                    data:cancelleds
                }
            ]
        };

        myChart1.setOption(option);

    });
</script>
<div id="main3" style="width: 1680px;height:750px;"></div>
<script type="text/javascript">
    var myChart2 = echarts.init(document.getElementById('main3'));
    var plannedRates = ${fns:toGson(plannedRates)};
    var acceptedAppRates = ${fns:toGson(acceptedAppRates)};
    var pendingRates = ${fns:toGson(pendingRates)};
    var pendingAppRates = ${fns:toGson(pendingAppRates)};
    var verificationCodeRates = ${fns:toGson(verificationCodeRates)};
    var orderDetailPageRates = ${fns:toGson(orderDetailPageRates)};
    var callBackRates = ${fns:toGson(callBackRates)};
    var cancelledRates = ${fns:toGson(cancelledRates)};

    setTimeout(function () {
        option = {
            title : {
                left: 'left',
                text: '${rptSearchCondition.selectedMonth}月短信比率',
                x:'center'
            },
            tooltip: {
                trigger: 'axis',
                formatter:function(params) {
                    var res = params[0].name +'<br/>';
                    for (var i = 0; i < params.length; i++) {
                        res+=params[i].seriesName +' : '+params[i].value+'%</br>';
                    }
                    return res;
                }
            },
            legend: {
                data:['派单','APP接单','客服预约','网点预约','验证码','客服工单详情界面','短信回访','订单取消']
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
                data: sendDates
            },
            yAxis: {
                type: 'value',
                axisLabel: {
                    show: true,
                    interval: 'auto',
                    formatter: '{value} %'
                },
                max:100,
                min:0,
                splitNumber:10
            },
            series: [
                {
                    name:'派单',
                    type:'line',
                    smooth: true,
                    color:"#91c7ae",
                    data:plannedRates
                },
                {
                    name:'APP接单',
                    type:'line',
                    smooth: true,
                    color:"#749f83",
                    data:acceptedAppRates
                },
                {
                    name:'客服预约',
                    type:'line',
                    smooth: true,
                    color:"#546570",
                    data:pendingRates
                },
                {
                    name:'网点预约',
                    type:'line',
                    smooth: true,
                    color:"#6e7074",
                    data:pendingAppRates
                },
                {
                    name:'验证码',
                    type:'line',
                    smooth: true,
                    color:"#61a0a8",
                    data:verificationCodeRates
                },
                {
                    name:'客服工单详情界面',
                    type:'line',
                    smooth: true,
                    color:"#d48265",
                    data:orderDetailPageRates
                },
                {
                    name:'短信回访',
                    type:'line',
                    smooth: true,
                    color:"#bda29a",
                    data:callBackRates
                },
                {
                    name:'订单取消',
                    type:'line',
                    smooth: true,
                    color:"#CC0033",
                    data:cancelledRates
                }

            ]
        };

        myChart2.setOption(option);
    });
</script>
</body>
</html>
