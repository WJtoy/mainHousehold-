<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>日下单明细</title>
    <script src="${ctxStatic}/echarts/echarts.min.js"></script>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Noto+Sans+SC:100,300,400,500,700,900">
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
                $("#searchForm").attr("action", "${ctx}/rpt/provider/dataDrawingList/getOrderPlanDailyChart");
                $("#searchForm").submit();
            });
        });

    </script>

    <style>

        .query{
            float: left;
            height: 35px;
            width: 98%;
            margin-left: 16px;
            background: #fff;
            border: 1px solid #FFF;

        }
        .customer-plan{
            height: 460px;
            width: 819px;
            margin-top: 16px;
            margin-left: 24px;
            background: #fff;
            border: 1px solid #FFF;
            box-shadow: #EAEEEF 0px 0px 6px 0px;
        }

        .top-10 {
            float: left;
            width: 387px;
            margin-top: 16px;
            margin-left: 26px;
        }

        .top-10 ul {
            counter-reset: section;
        }

        .top-10 li {
            width: 364px;
            line-height: 50px;
            height: 41px;
            overflow: hidden;
            color: #525C66;
            font-size: 14px;

        }
        c {
            color: #333333;
            text-decoration: none;

        }

        b {
            text-decoration: none;
            color: #0096DA;
            float: right;
        }
        .top-10 li:nth-child(1):before {
            background: #FF3300;
            font-size: 14px;
        }

        .top-10 li:nth-child(2):before {
            background: #FF6600;
            font-size: 14px;
        }

        .top-10 li:nth-child(3):before {
            background: #FFCC00;

        }

        .top-10 li:before {
            counter-increment: section;
            content: counter(section);
            display: inline-block;
            margin-right: 10px;
            height: 24px;
            width: 24px;
            text-align: center;
            line-height: 24px;
            background: #b8c2cc;
            color: #fff;
            border-radius: 3px;
            font-size: 14px
        }
    </style>
</head>
<body>
<div>
    <form:form id="searchForm" modelAttribute="rptSearchCondition"
               action="${ctx}/rpt/provider/dataDrawingList/getOrderPlanDailyChart" method="post" class="breadcrumb form-search" style="
    padding: 0px 0px;">
        <div class="query">
            <label style="margin-left: 7px;margin-top: 5px;font-size: 14px;color: #333333">时&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;间:</label>
            <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:186px;margin-left:4px" maxlength="20" class="input-small Wdate"
                   value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
                   onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false,maxDate:'${rptSearchCondition.endPlanDate}'});"/>
            &nbsp;&nbsp;
            <input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/>
        </div>
    </form:form>

    <sys:message content="${message}"/>
    <div style="width: 1682px">
    <div id="main" class="customer-plan" style="float: left;margin-top: 10px">
        <script type="text/javascript">
            var myChart = echarts.init(document.getElementById('main'));
            var orderPlanQty = ${fns:toGson(orderPlanQty)};
            var createDate = ${fns:toGson(createDate)};


            setTimeout(function () {
                option = {
                    title: {
                        left: '2%',
                        top:'3%',
                        text: '每日下单合计',
                        textStyle: {
                            fontSize: '16',
                            color:"#333333"
                        }

                    },
                    tooltip: {
                        trigger: 'axis'
                    },
                    grid: {
                        left: '5%',
                        right: '4%',
                        bottom: '3%',
                        top:'13%',
                        containLabel: true
                    },
                    xAxis: {
                        type: 'category',
                        data: createDate,
                        axisTick: {
                            show: false
                        },
                        axisPointer: {
                            type: 'shadow'
                        },
                        axisLine:{  //x轴线
                            show:true,
                            lineStyle: {
                                color: '##DDDDDD'
                            }
                        },
                        axisLabel : {
                            formatter : function(params){
                                //将最终的字符串返回
                                return params.substring(3, 6);
                            }

                        }

                    },
                    yAxis: {
                        type: 'value',
                        minInterval: 1,
                        boundaryGap: [0, 0.1],
                        axisLine:{  //x轴线
                            show:true,
                            lineStyle: {
                                color: '##DDDDDD'
                            }
                        },
                        axisTick:{ //y轴刻度线
                            show:false
                        },
                    },
                    series: [
                        {
                            name: '下单量',
                            type: 'bar',
                            smooth: true,
                            barWidth: 10,
                            color: "#58AFFF",
                            data: orderPlanQty
                        }

                    ]
                };

                myChart.setOption(option);
            });
        </script>

    </div>

    <div id="main1" class="customer-plan" style="float: left;margin-left: 16px;margin-top: 10px">

        <script type="text/javascript">
            var myChart1 = echarts.init(document.getElementById('main1'));
            var productPlan1Qty = ${fns:toGson(productPlan1Qty)};
            var productPlan2Qty = ${fns:toGson(productPlan2Qty)};
            var productPlan3Qty = ${fns:toGson(productPlan3Qty)};
            var productCategory1Name = "${productCategory1Name}";
            var productCategory2Name = "${productCategory2Name}";
            var productCategory3Name = "${productCategory3Name}";

            var productCategoryName = ${fns:toGson(productCategoryName)};

            setTimeout(function () {
                option = {
                    title: {
                        left: '2%',
                        top:'3%',
                        text: '各品类每日下单合计',
                        textStyle: {
                            fontSize: '16',
                            color:"#333333"
                        }
                    },
                    tooltip: {
                        trigger: 'axis',
                        formatter: function (params) {
                            var res = params[0].name + '<br/>';
                            var sum = 0;
                            var rate = 0;
                            for(var j = 0; j < params.length; j++){
                                sum += params[j].value;
                            }
                            for (var i = 0; i < params.length; i++) {
                                if(sum > 0){
                                    rate = params[i].value / sum * 100;
                                    rate = rate.toFixed(2);
                                }
                                res += params[i].seriesName + '：\n ' + rate + '%   \n'+ params[i].value + '<br>' ;
                            }
                            return res;
                        }
                    },
                    legend: {
                        left:'69%',
                        top:'3%',
                        data: productCategoryName
                    },
                    grid: {
                        left: '5%',
                        right: '4%',
                        bottom: '3%',
                        top:'13%',
                        containLabel: true
                    },
                    xAxis: {
                        type: 'category',
                        boundaryGap: false,
                        data: createDate,
                        axisLine:{  //x轴线
                            show:true,
                            lineStyle: {
                                color: '##DDDDDD'
                            }
                        },
                        axisLabel : {
                            formatter : function(params){
                                //将最终的字符串返回
                                return params.substring(3, 6);
                            }

                        }
                    },
                    yAxis: {
                        type: 'value',
                        axisLine:{
                            show:true,
                            lineStyle: {
                                color: '##DDDDDD'
                            }
                        },
                        axisTick:{ //y轴刻度线
                            show:false
                        }

                    },
                    series: [
                        {
                            name: productCategory1Name,
                            type: 'line',
                            color: "#00B0FF",
                            data: productPlan1Qty,
                            symbolSize: 8,
                            itemStyle : {
                                normal : {
                                    lineStyle:{
                                        width:2//折线宽度
                                    }
                                }
                            },
                        },
                        {
                            name: productCategory2Name,
                            type: 'line',
                            color: "#13C2C2",
                            symbolSize: 8,
                            itemStyle : {
                                normal : {
                                    lineStyle:{
                                        width:2//折线宽度
                                    }
                                }
                            },
                            data: productPlan2Qty
                        },
                        {
                            name: productCategory3Name,
                            type: 'line',
                            color: "#F3637B",
                            symbolSize: 8,
                            itemStyle : {
                                normal : {
                                    lineStyle:{
                                        width:2//折线宽度
                                    }
                                }
                            },
                            data: productPlan3Qty
                        }

                    ]
                };

                myChart1.setOption(option);
            });
        </script>
    </div>
    </div>
    <div class="customer-plan" style="float: left;width: 1656px;">
        <div class="top-10" style="margin-left: 24px;">
            <h4 style="color: #333333;font-size: 18px">客户下单排名</h4>
            <ul style="margin-left: 0px;">
                <c:forEach items="${customerPlanQtyList}" var="item">
                    <li>
                        <c>${item.customerName}</c>
                        <b><fmt:formatNumber maxFractionDigits="2">${item.daySum}</fmt:formatNumber></b>
                    </li>

                </c:forEach>
            </ul>
        </div>

        <div style="border-left: 1px dashed #BBBBBB;height:90%;line-height:1px;float: left;margin-top: 16px;"></div>

        <div class="top-10">
            <h4 style="color: #333333;font-size: 16px">${productCategory1Name}客户下单排名</h4>
            <ul style="margin-left: 0px;">
                <c:forEach items="${customerProductCategory1}" var="item">
                    <li>
                        <c>${item.customerName}</c>
                        <b><fmt:formatNumber maxFractionDigits="2">${item.daySum}</fmt:formatNumber></b>
                    </li>

                </c:forEach>
            </ul>
        </div>
        <div style="border-left: 1px dashed #BBBBBB;height:90%;line-height:1px;float: left;margin-top: 16px;"></div>

        <div class="top-10">
            <h4 style="color: #333333;font-size: 16px">${productCategory2Name}客户下单排名</h4>
            <ul style="margin-left: 0px;">
                <c:forEach items="${customerProductCategory2}" var="item">
                    <li>
                        <c>${item.customerName}</c>
                        <b><fmt:formatNumber maxFractionDigits="2">${item.daySum}</fmt:formatNumber></b>
                    </li>

                </c:forEach>
            </ul>
        </div>

        <div style="border-left: 1px dashed #BBBBBB;height:90%;line-height:1px;float: left;margin-top: 16px;"></div>

        <div class="top-10">
            <h4 style="color: #333333;font-size: 16px">${productCategory3Name}客户下单排名</h4>
            <ul style="margin-left: 0px;">
                <c:forEach items="${customerProductCategory3}" var="item">
                    <li>
                        <c>${item.customerName}</c>
                        <b><fmt:formatNumber maxFractionDigits="2">${item.daySum}</fmt:formatNumber></b>
                    </li>

                </c:forEach>
            </ul>
        </div>
    </div>
</div>
</body>
</html>
