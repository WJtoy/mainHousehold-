<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>完成时效图表</title>
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
                $("#searchForm").attr("action", "${ctx}/rpt/provider/keFuCompleteTimeNew/keFuCompleteTimeNewChart");
                $("#searchForm").submit();
            });
        });
    </script>
</head>
<body>
<ul id="navtabs" class="nav nav-tabs">
    <li>
        <a href="${ctx}/rpt/provider/keFuCompleteTimeNew/keFuCompleteTimeNewReport">完成时效统计</a>
    </li>
    <li class="active"><a href="javascript:void(0);">完成时效图表</a></li>

</ul>
<c:set var="currentuser" value="${fns:getUser() }" />
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/keFuCompleteTimeNew/keFuCompleteTimeNewChart"
           method="post" class="breadcrumb form-search">
    <div>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>下单时间：</label>
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:242px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false,maxDate:'${rptSearchCondition.endPlanDate}'});"/>
        &nbsp;&nbsp;
        <c:choose>
            <c:when test="${currentuser.isCustomer()}">
                <input type="hidden" readonly="true" id="customer.name" name="customer.name" value="${currentuser.customerAccountProfile.customer.name}" />
                <input type="hidden" readonly="true" id="customer.id" name="customer.id" value="${currentuser.customerAccountProfile.customer.id}" />
            </c:when>
            <c:otherwise>
                <label style="margin-left: 12px">客户：</label>
                <select id="customerId" name="customerId" class="input-small" style="width:255px;">
                    <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}" />>所有</option>
                    <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                        <option value="${dict.id}" <c:out value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                    </c:forEach>
                </select>

            </c:otherwise>
        </c:choose>
        &nbsp;&nbsp;
        <c:if test="${!currentuser.isCustomer() && !currentuser.isSaleman()}">
            <label class="control-label">客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;服：</label>
            <form:select path="kefuId" style="width:180px;">
                <form:option value="0" label="所有" />
                <form:options items="${fns:getKefuList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
            </form:select>
        </c:if>
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
        <c:set var="serviceTypeList" value="${fns:getDictListFromMS('order_service_type')}" />
        <label>工单类型：</label>
        <select id="orderServiceType" name="orderServiceType" style="width:100px;">
            <option value="" selected="selected">所有</option>
            <c:forEach items="${serviceTypeList}" var="serviceTypeDict">
                <option value="${serviceTypeDict.value}" <c:out value="${(rptSearchCondition.orderServiceType eq serviceTypeDict.value)?'selected=selected':''}" />>${serviceTypeDict.label}</option>
            </c:forEach>
        </select>
        <c:if test="${currentuser.isCustomer() || currentuser.isSaleman()}">
            &nbsp;&nbsp;
            <shiro:hasPermission name="rpt:keFuCompleteTimeNewReport:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询" /></shiro:hasPermission>
        </c:if>
    </div>
    &nbsp;&nbsp;
    <c:if test="${!currentuser.isCustomer() && !currentuser.isSaleman()}">
        <div>

            <label class="control-label">服务网点：</label>
            <rpt:servicePointSelector id="servicePoint" name="servicePointId" value="${rptSearchCondition.servicePointId}"
                                      labelName="servicePointName" labelValue="${rptSearchCondition.servicePointName}"
                                      width="900" height="700" noblackList="true" callbackmethod="" cssClass="required"/>
            &nbsp;&nbsp;
            <label>区域：</label>
            <sys:treeselectareanew id="area" name="areaId" value="${rptSearchCondition.areaId}" levelValue="${rptSearchCondition.areaLevel}" nodeLevel="true"
                            labelName="areaName" labelValue="${rptSearchCondition.areaName }" title="区域"
                            url="/sys/area/treeDataNew?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1"
                            nameLevel="3" />
            &nbsp;&nbsp;
            <label>服务品类：</label>
            <select id="productCategory" name="productCategory" class="input-small" style="width:182px;">
                <option value="0" <c:out value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}"/>>所有
                </option>
                <c:forEach items="${productCategoryList}" var="dict">
                    <option value="${dict.id}" <c:out
                            value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
                </c:forEach>
            </select>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <shiro:hasPermission name="rpt:keFuCompleteTimeNewReport:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询" /></shiro:hasPermission>
        </div>
    </c:if>
</form:form>
<sys:message content="${message}"/>
<div id="main" style="width: 1680px;height:665px;"></div>
<script type="text/javascript">
    // 基于准备好的dom，初始化echarts实例
    var myChart = echarts.init(document.getElementById('main'));
    var mapList = ${fns:toGson(mapList)};
    var completeMapList = ${fns:toGson(completeMapList)};

    setTimeout(function () {
        option = {
            title: {
                text: '月完成合计'
            },
            tooltip: {
                trigger: 'item',
                formatter: "{a} <br/>{b}: {c} ({d}%)"
            },
            legend: {
                data: ['订单完成', '24小时完成', '48小时完成', '72小时完成', '72小时外完成', '未完成']
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
                    color: ["#33CC66", '#FFCC00'],
                    data: completeMapList
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
    var createDates = ${fns:toGson(createDates)};
    var strComplete24hours = ${fns:toGson(strComplete24hours)};
    var strComplete48hours = ${fns:toGson(strComplete48hours)};
    var strComplete72hours = ${fns:toGson(strComplete72hours)};
    var strOverComplete72hours = ${fns:toGson(strOverComplete72hours)};
    var strUnfulfilledOrders = ${fns:toGson(strUnfulfilledOrders)};
    var strTheTotalOrders = ${fns:toGson(strTheTotalOrders)};

    setTimeout(function () {

        option = {

            title: {
                left: 'left',
                text: '日完成统计',
                x: 'center'
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                    type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                }
            },
            legend: {
                data: ['下单', '24小时完成', '48小时完成', '72小时完成', '72小时外完成', '未完成']
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
                    data: createDates
                }
            ],
            yAxis: [
                {
                    type: 'value'
                }
            ],
            series: [
                {
                    name: '下单',
                    type: 'bar',
                    color: "#6495ED",
                    data: strTheTotalOrders
                },
                {
                    name: '24小时完成',
                    type: 'bar',
                    stack: '完成',
                    color: "#00CC00",
                    data: strComplete24hours
                },
                {
                    name: '48小时完成',
                    type: 'bar',
                    stack: '完成',
                    color: "#009900",
                    data: strComplete48hours
                },
                {
                    name: '72小时完成',
                    type: 'bar',
                    stack: '完成',
                    color: "#006633",
                    data: strComplete72hours
                },
                {
                    name: '72小时外完成',
                    type: 'bar',
                    stack: '完成',
                    color: "#003300",
                    data: strOverComplete72hours
                },
                {
                    name: '未完成',
                    type: 'bar',
                    color: "#FFCC00",
                    data: strUnfulfilledOrders
                }
            ]
        };

        myChart1.setOption(option);

    });
</script>
<div id="main3" style="width: 1680px;height:750px;"></div>
<script type="text/javascript">
    var myChart2 = echarts.init(document.getElementById('main3'));
    var strComplete24hourRates = ${fns:toGson(strComplete24hourRates)};
    var strComplete48hourRates = ${fns:toGson(strComplete48hourRates)};
    var strComplete72hourRates = ${fns:toGson(strComplete72hourRates)};
    var strOverComplete72hourRates = ${fns:toGson(strOverComplete72hourRates)};
    var strUnfulfilledOrderRates = ${fns:toGson(strUnfulfilledOrderRates)};

    setTimeout(function () {
        option = {
            title: {
                left: 'left',
                text: '日完成比率',
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
                data: ['24小时完成', '48小时完成', '72小时完成', '72小时外完成', '未完成']
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
                    name: '24小时完成',
                    type: 'line',
                    smooth: true,
                    color: "#00CC00",
                    data: strComplete24hourRates
                },
                {
                    name: '48小时完成',
                    type: 'line',
                    smooth: true,
                    color: "#009900",
                    data: strComplete48hourRates
                },
                {
                    name: '72小时完成',
                    type: 'line',
                    smooth: true,
                    color: "#006633",
                    data: strComplete72hourRates
                },
                {
                    name: '72小时外完成',
                    type: 'line',
                    smooth: true,
                    color: "#003300",
                    data: strOverComplete72hourRates
                },
                {
                    name: '未完成',
                    type: 'line',
                    smooth: true,
                    color: "#FFCC00",
                    data: strUnfulfilledOrderRates
                }

            ]
        };

        myChart2.setOption(option);
    });
</script>
</body>
</html>
