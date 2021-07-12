<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>每日派单图表</title>
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
                $("#searchForm").attr("action", "${ctx}/rpt/provider/dispatchList/dispatchListRptChart");
                $("#searchForm").submit();
            });
        });
    </script>
</head>
<body>
<ul id="navtabs" class="nav nav-tabs">
    <li>
        <a href="${ctx}/rpt/provider/dispatchList/dispatchListInforRpt">每日派单报表</a>
    </li>
    <li class="active"><a href="javascript:void(0);">每日派单图表</a></li>

</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/dispatchList/dispatchListRptChart" method="post" class="breadcrumb form-search">
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
        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询" />
    </div>
</form:form>
<sys:message content="${message}"/>
<div id="main" style="width: 1680px;height:665px;"></div>
<script type="text/javascript">
    // 基于准备好的dom，初始化echarts实例
    var myChart = echarts.init(document.getElementById('main'));
    var mapList = ${fns:toGson(mapList)};
    var planList = ${fns:toGson(planList)};

    setTimeout(function () {
        option = {
            title: {
                text: '${rptSearchCondition.selectedMonth}月派单合计'
            },
            tooltip: {
                trigger: 'item',
                formatter: "{a} <br/>{b}: {c} ({d}%)"
            },
            legend: {
                data: ['已派单','自动派单', 'APP抢单', '客服派单', '突击单', '取消单', '未派单']
            },
            series: [
                {
                    name: '单量',
                    type: 'pie',
                    selectedMode: 'single',
                    radius: [0, '40%'],
                    label: {
                        normal: {
                            position: 'inner',
                            formatter: " {b}\n{c} ({d})%"
                        }
                    },
                    labelLine: {
                        normal: {
                            show: false
                        }
                    },
                    color: ["#33CC66", "#CC0000", '#FFCC00'],
                    data: planList
                },
                {
                    name: '单量',
                    type: 'pie',
                    radius: ['50%', '75%'],
                    label: {
                        normal: {
                            formatter:['{a|{a}}{abg|}\n{hr|}\n  {b|{b}：}{c}  {per|{d}%}  '].join('\n'),
                            backgroundColor: '#eee',
                            borderColor: '#aaa',
                            borderWidth: 1,
                            borderRadius: 10,
                            rich:{
                                a: {
                                    color: '#999',
                                    lineHeight: 22,
                                    align: 'center'
                                },
                                hr: {
                                    borderColor: '#aaa',
                                    width: '100%',
                                    borderWidth: 0.5,
                                    height: 0
                                },
                                b: {
                                    fontSize: 16,
                                    lineHeight: 33
                                },
                                per: {
                                    color: '#eee',
                                    backgroundColor: '#334455',
                                    padding: [2, 4],
                                    borderRadius: 2
                                }
                            }
                        }
                    },
                    color: ['#00CC00', '#009900', '#006633', '#003300'],
                    data: mapList
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
    var orderCreateDates = ${fns:toGson(orderCreateDates)};

    var strAuto = ${fns:toGson(strAuto)};
    var strApp = ${fns:toGson(strApp)};
    var strKeFu = ${fns:toGson(strKeFu)};
    var strCrush = ${fns:toGson(strCrush)};
    var strNotPlan = ${fns:toGson(strNotPlan)};
    var strCancel = ${fns:toGson(strCancel)};


    setTimeout(function () {

        option = {

            title: {
                left: 'left',
                text: '${rptSearchCondition.selectedMonth}月派单统计',
                x: 'center'
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                    type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                }
            },
            legend: {
                data: ['自动派单','APP抢单','客服派单','突击单','未派单','取消单']
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
                    name: '自动派单',
                    type: 'bar',
                    stack: '已派单',
                    color: "#00CC00",
                    data: strAuto
                },
                {
                    name: 'APP抢单',
                    type: 'bar',
                    stack: '已派单',
                    color: "#009900",
                    data: strApp
                },
                {
                    name: '客服派单',
                    type: 'bar',
                    stack: '已派单',
                    color: "#006633",
                    data: strKeFu
                },
                {
                    name: '突击单',
                    type: 'bar',
                    stack: '已派单',
                    color: "#003300",
                    data: strCrush
                },
                {
                    name: '未派单',
                    type: 'bar',
                    color: "#FFCC00",
                    data: strNotPlan
                },

                {
                    name: '取消单',
                    type: 'bar',
                    color: "#CC0000",
                    data: strCancel
                }

            ]
        };

        myChart1.setOption(option);

    });
</script>
<div id="main3" style="width: 1680px;height:750px;"></div>
<script type="text/javascript">
    var myChart2 = echarts.init(document.getElementById('main3'));
    var createDates = ${fns:toGson(createDates)};
    var strAutoRate = ${fns:toGson(strAutoRate)};
    var strAppRate = ${fns:toGson(strAppRate)};
    var strKeFuRate = ${fns:toGson(strKeFuRate)};
    var strCrushRate = ${fns:toGson(strCrushRate)};
    var strNotPlanRate = ${fns:toGson(strNotPlanRate)};
    var strCancelRate = ${fns:toGson(strCancelRate)};

    setTimeout(function () {
        option = {
            title: {
                left: 'left',
                text: '${rptSearchCondition.selectedMonth}月派单比率',
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
                data: ['自动派单', 'APP抢单', '客服派单', '突击单','未派单','取消单']
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
                    name: '自动派单',
                    type: 'line',
                    smooth: true,
                    color: "#00CC00",
                    data: strAutoRate
                },
                {
                    name: 'APP抢单',
                    type: 'line',
                    smooth: true,
                    color: "#009900",
                    data: strAppRate
                },
                {
                    name: '客服派单',
                    type: 'line',
                    smooth: true,
                    color: "#006633",
                    data: strKeFuRate
                },
                {
                    name: '突击单',
                    type: 'line',
                    smooth: true,
                    color: "#003300",
                    data: strCrushRate
                },
                {
                    name: '未派单',
                    type: 'line',
                    smooth: true,
                    color: "#FFCC00",
                    data: strNotPlanRate
                },
                {
                    name: '取消单',
                    type: 'line',
                    smooth: true,
                    color: "#CC0000",
                    data: strCancelRate
                }

            ]
        };

        myChart2.setOption(option);
    });
</script>
</body>
</html>
