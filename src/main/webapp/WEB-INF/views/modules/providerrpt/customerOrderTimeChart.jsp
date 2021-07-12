<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>工单时效图表</title>
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
    <style>
        .chart_base{width:1680px;height:750px}
        .chart_left{float:left;width:840px;height:580px;margin-top: 30px}
        .chart_right{float:right;width:840px;height:580px;margin-top: 30px}
    </style>
    <script type="text/javascript" language="javascript">
        $(document).ready(function() {
            $("#btnSubmit").click(function() {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action","${ctx}/rpt/provider/customerOrderTime/customerOrderTimeChart");
                $("#searchForm").submit();
            });
        });
    </script>
</head>
<body>
<ul id="navtabs" class="nav nav-tabs">
    <li>
        <a href="${ctx}/rpt/provider/customerOrderTime/customerOrderTimeReport">工单时效统计</a>
    </li>
    <li class="active"><a href="javascript:void(0);">工单时效图表</a></li>

</ul>
<c:set var="currentuser" value="${fns:getUser() }" />
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/customerOrderTime/customerOrderTimeChart" method="post" class="breadcrumb form-search">
    <div>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>下单时间：</label>
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false,maxDate:'${rptSearchCondition.endPlanDate}'});"/>
        &nbsp;&nbsp;
        <c:choose>
            <c:when test="${currentuser.isCustomer()}">
                <input type="hidden" id="customer.name" name="customer.name" value="${currentuser.customerAccountProfile.customer.name}" />
                <input type="hidden" id="customer.id" name="customer.id" value="${currentuser.customerAccountProfile.customer.id}" />
            </c:when>
            <c:otherwise>
                <label>客户：</label>
                <select id="customerId" name="customerId" class="input-small" style="width:225px;">
                    <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}" />>所有</option>
                    <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                        <option value="${dict.id}" <c:out value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                    </c:forEach>
                </select>

            </c:otherwise>
        </c:choose>
        &nbsp;&nbsp;
        <c:if test="${!currentuser.isCustomer() && !currentuser.isSaleman()}">
            <label class="control-label">客服：</label>
            <form:select path="kefuId" style="width:180px;">
                <form:option value="0" label="所有" />
                <form:options items="${fns:getKefuList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
            </form:select>
        </c:if>
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
            <input id="btnSubmit" class="btn btn-primary" type="button" value="查询" />
            &nbsp;&nbsp;
            <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
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
            &nbsp;&nbsp;
            <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
        </div>
    </c:if>
</form:form>
<sys:message content="${message}"/>
<div id="main1" class="chart_left"></div>
<script type="text/javascript">
    // 基于准备好的dom，初始化echarts实例
    var myChartPlan = echarts.init(document.getElementById('main1'));
    var mapPlanList = ${fns:toGson(mapPlanList)};

    setTimeout(function () {

        option = {
            title : {
                left: 'left',
                text: '派单合计',
                x:'center'
            },
            tooltip : {
                trigger: 'item',
                formatter: "{a} <br/>{b} : {c} ({d}%)"
            },
            color:['#009900','#FFCC33'],
            legend: {

                data: ['小于3小时','大于3小时']
            },
            series : [
                {
                    name: '单量',
                    type: 'pie',
                    radius : '75%',
                    center: ['50%', '50%'],
                    data:mapPlanList,
                    itemStyle : {
                        normal : {
                            label : {
                                show : true,
                                formatter: "{b}({d}%)  "
                            }
                        }
                    },
                }
            ]
        };
        myChartPlan.setOption(option);

    });
</script>

<div id="main" class="chart_right"></div>

<script type="text/javascript">
    // 基于准备好的dom，初始化echarts实例
    var myChart = echarts.init(document.getElementById('main'));
    var mapList = ${fns:toGson(mapCloseList)};

    setTimeout(function () {

        option = {
            title : {
                left: 'left',
                text: '结单合计',
                x:'center'
            },
            tooltip : {
                trigger: 'item',
                formatter: "{a} <br/>{b} : {c} ({d}%)"
            },
            color:['#00CC66','#009900','#006400','#003333','#FFCC33'],
            legend: {

                data: ['小于12小时','小于24小时','小于48小时','小于72小时','大于72小时']
            },
            series : [
                {
                    name: '单量',
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
                    },
                }
            ]
        };
        myChart.setOption(option);

    });
</script>
<div id="main2" style="width: 1680px;height:750px;margin-top:30px;clear: both"></div>
<script type="text/javascript">
    // 基于准备好的dom，初始化echarts实例
    var myChart2 = echarts.init(document.getElementById('main2'));
    var dateAllList = ${fns:toGson(dateAllList)};
    var less12List = ${fns:toGson(less12List)};
    var less24List = ${fns:toGson(less24List)};
    var less48List = ${fns:toGson(less48List)};
    var less72List = ${fns:toGson(less72List)};
    var more72List = ${fns:toGson(more72List)};


    setTimeout(function () {

        option = {

            title : {
                left: 'left',
                text: '完成统计',
                x:'center'
            },
            tooltip : {
                trigger: 'axis',
                axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                    type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                }
            },
            legend: {
                data:['小于12小时','小于24小时','小于48小时','小于72小时','大于72小时']
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
                    data :dateAllList
                }
            ],
            yAxis : [
                {
                    type : 'value'
                }
            ],
            series : [
                {
                    name:'小于12小时',
                    type:'bar',
                    color:"#00CC66",
                    data:less12List
                },
                {
                    name:'小于24小时',
                    type:'bar',
                    stack: '完成',
                    color:"#009900",
                    data:less24List
                },
                {
                    name:'小于48小时',
                    type:'bar',
                    stack: '完成',
                    color:"#006400",
                    data:less48List
                },
                {
                    name:'小于72小时',
                    type:'bar',
                    stack: '完成',
                    color:"#003333",
                    data:less72List
                },
                {
                    name:'大于72小时',
                    type:'bar',
                    stack: '完成',
                    color:"#FFCC33",
                    data:more72List
                },


            ]
        };

        myChart2.setOption(option);

    });
</script>
<div id="main3" style="width: 1680px;height:750px;clear: both"></div>
<script type="text/javascript">
    var myChart3 = echarts.init(document.getElementById('main3'));
    var less12RateList = ${fns:toGson(less12RateList)};
    var dateList = ${fns:toGson(dateList)};
    var less24RateList = ${fns:toGson(less24RateList)};
    var less48RateList = ${fns:toGson(less48RateList)};
    var less72RateList = ${fns:toGson(less72RateList)};
    var more72RateList = ${fns:toGson(more72RateList)};


    setTimeout(function () {
        option = {
            title : {
                left: 'left',
                text: '完成比率',
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
                },
            },
            legend: {
                data:['小于12小时','小于24小时','小于48小时','小于72小时','大于72小时']
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
                data: dateList
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
                    name:'小于12小时',
                    type:'line',
                    smooth: true,
                    color:"#00CC66",
                    data:less12RateList
                },
                {
                    name:'小于24小时',
                    type:'line',
                    smooth: true,
                    color:"#009900",
                    data:less24RateList
                },
                {
                    name:'小于48小时',
                    type:'line',
                    smooth: true,
                    color:"#006400",
                    data:less48RateList
                },
                {
                    name:'小于72小时',
                    type:'line',
                    smooth: true,
                    color:"#003333",
                    data:less72RateList
                },
                {
                    name:'大于72小时',
                    type:'line',
                    smooth: true,
                    color:"#FFCC33",
                    data:more72RateList
                }


            ]
        };

        myChart3.setOption(option);
    });
</script>
</body>
</html>
