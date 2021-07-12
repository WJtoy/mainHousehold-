<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>数据汇总图表</title>
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
                $("#searchForm").attr("action", "${ctx}/rpt/provider/dataDrawingList/dataDrawingListChart");
                $("#searchForm").submit();
            });
        });
        function planDetails() {
            var time = $("#endDate").val();
            window.location.href = "${ctx}/rpt/provider/dataDrawingList/getOrderPlanDailyChart?endDate="+time
        }
    </script>

    <style>
        body{font-family: "Noto Sans SC";}

        .query{
            float: left;
            height: 35px;
            width: 373px;
            margin-left: 16px;
            background: #fff;
            border: 1px solid #FFF;
            box-shadow: #EAEEEF 0px 0px 6px 0px;
        }
        .customer-plan{
            height: 348px;
            width: 373px;
            margin-top: 40px;
            margin-left: -375px;
            background: #fff;
            border: 1px solid #FFF;
            box-shadow: #EAEEEF 0px 0px 6px 0px;
        }
        .time-maintain{
            height: 388px;
            width: 1267px;
            margin-left: 16px;
            background: #fff;
            border: 1px solid #FFF;
            box-shadow: #EAEEEF 0px 0px 6px 0px;
        }

        .customer-maintain{
            height: 207px;
            margin-top: 16px;
            margin-left: 16px;
            background: #fff;
            border: 1px solid #FFF;
            box-shadow: #EAEEEF 0px 0px 6px 0px;
        }

        .charge-maintain{
            margin-top: 16px;
            margin-left: 16px;
            height: 289px;
            background: #fff;
            border: 1px solid #FFF;
            box-shadow: #EAEEEF 0px 0px 6px 0px;

        }
    </style>
</head>
<body>
<div style="width: 1680px;height: 927px;">
    <form:form id="searchForm" modelAttribute="rptSearchCondition"
               action="${ctx}/rpt/provider/dataDrawingList/dataDrawingListChart" method="post" class="breadcrumb form-search" style="
    padding: 0px 0px;">
        <div class="query">
                <label style="margin-left: 60px;margin-top: 5px;"></label>
                <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:150px;margin-left:4px" maxlength="20" class="input-small Wdate"
                value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
                onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false,maxDate:'${rptSearchCondition.endPlanDate}'});"/>
                &nbsp;&nbsp;
            <input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/>
        </div>
    </form:form>

    <sys:message content="${message}"/>

    <div class="customer-plan" style="float: left" onclick="planDetails()">
        <%@include file="/WEB-INF/views/modules/providerrpt/chart/customerPlanChart.jsp" %>
        <div style="float: left;width: 135px">
            <font style="margin-left: 24px;font-size: 14px;color:#333333;margin-top:16px;float: left;width: 100px">日下单量</font>
            <font id="planOrderQty" style="line-height: 37px;font-size: 32px;color: #101010;float: left;margin-top: 8px;margin-left: 24px;">0</font>

        </div>
        <div id="main1" style="width: 230px;height: 90px;float: left;margin-top: 10px;">

        </div>
        <div style="float: left;">
            <font style="margin-left: 24px;float: left;font-size: 12px;color:#999999;">同比:</font>
            <font id="lastMonthPlanOrderRate" style="margin-left: 5px;float: left;font-size: 12px;color:#999999;width: 50px;">-100%</font>
            <font style="margin-left: 52px;float: left;font-size: 12px;color:#999999;">环比:</font>
            <font id="lastYearPlanOrderRate" style="margin-left: 5px;font-size: 12px;color:#999999;width: 50px;float: left;width: 50px">-100%</font>
        </div>
        <%@include file="/WEB-INF/views/modules/providerrpt/chart/orderQtyDailyChart.jsp" %>
        <div style="float: left;margin-top: 25px">
            <font style="margin-left: 24px;float: left;"><img src="${ctxStatic}/images/rpt/uncompleted.png" style="width: 24px;margin-right: 8px;">未完成工单</font>
            <font  style="margin-left: 88px;float: left;width: 100px"><img src="${ctxStatic}/images/rpt/cancelled.png" style="width: 24px;margin-right: 8px;">取消单</font>
            <font id="uncompletedOrderQty" style="font-size: 24px;margin-top: 10px; float: left;color: #333333;margin-left: 24px;width: 70px">0</font>

            <font id="cancelledOrderQty" style="font-size: 24px;margin-top: 10px;margin-left: 122px;float: left;color: #333333">0</font>
        </div>

        <div style="float: left;margin-top: 21px">
            <font style="margin-left: 24px;float: left;"><img src="${ctxStatic}/images/rpt/completed.png" style="width: 24px;margin-right: 8px;">完工单</font>
            <font style="margin-left: 116px;float: left;width: 149px;"><img src="${ctxStatic}/images/rpt/financialAudit.png" style="width: 24px;margin-right: 8px;">财务审单</font>

            <font id="completedOrderQty" style="font-size: 24px;margin-top: 10px;float: left;margin-left:24px;width: 70px;">0</font>

            <font id="financialAuditQty" style="font-size: 24px;margin-top: 10px;margin-left: 122px;float: left;">0</font>
        </div>

        <div style="float: left;margin-top: 12px">
            <font style="float: left;font-size: 12px;color: #999999;margin-left: 24px;">自动完工:</font>
            <font id="autoCompletedQty" style="float: left;font-size: 12px;color: #999999;margin-left: 5px;width: 50px">0</font>

            <font style="float: left;font-size: 12px;color: #999999;margin-left: 86px;">自动审单:</font>
            <font id="autoFinancialAuditQty" style="float: left;font-size: 12px;color: #999999;margin-left: 5px;">0</font>

            <font style="float: left;font-size: 12px;color: #999999;margin-left: 24px;margin-top: 9px;">自动完工占比:</font>
            <font id="autoCompletedRate" style="float: left;font-size: 12px;color: #999999;margin-left: 5px;width: 70px;margin-top: 9px;">0%</font>

            <font style="float: left;font-size: 12px;color: #999999;margin-left: 43px;margin-top: 9px;">审单异常:</font>
            <font id="abnormalOrderQty" style="float: left;font-size: 12px;color: #999999;margin-left: 5px;margin-top: 9px;">0</font>
        </div>


    </div>

    <div class="time-maintain" style="float: left">
        <div id="main3" style="width: 633px;height:382px;float: left">
            <%@include file="/WEB-INF/views/modules/providerrpt/chart/keFuCompleteTimeInstallChart.jsp" %>
        </div>
        <div style="border-left: 1px dashed #BBBBBB;height:95%;line-height:1px;float: left;margin-top: 12px;"></div>
        <div id="main4" style="width: 633px;height:382px;float: left">
            <%@include file="/WEB-INF/views/modules/providerrpt/chart/keFuCompleteTimeMaintainChart.jsp" %>
        </div>
    </div>

    <div class="customer-maintain" style="width: 228px;float: left;">
        <font style="margin-left: 16px;font-size: 16px;color:#666666;margin-top:16px;float: left;">客诉</font>
        <font id="customerComplain" style="margin-left: 10px;font-size: 16px;color:#333333;margin-top:16px;float: left;width: 100px">0</font >

        <%@include file="/WEB-INF/views/modules/providerrpt/chart/customerComplainChart.jsp" %>
        <dev id="main7" style="width: 56px;height: 56px;float: left;margin-left:34px;margin-top: 25px"></dev>
        <dev id="main8" style="width: 56px;height: 56px;float: left;margin-left:48px;margin-top: 25px"></dev>

        <p id="validComplain" style="color: #00B0FF;float: left;font-size: 14px;margin-left: 34px;margin-top: 8px;width:50px;text-align: center;">0</p>
        <p id="mediumPoorEvaluate" style="color: #00B0FF;float: left;font-size: 14px;margin-left: 56px;margin-top: 8px;width:50px;text-align: center;">0</p>
        <p id="validComplainRate" style="color: #666666;float: left;font-size: 14px;margin-left: 28px;width:70px;text-align: center;">0%</p>
        <p id="mediumPoorEvaluateRate" style="color: #666666;float: left;font-size: 14px;margin-left: 39px;width:65px;text-align: center;">0%</p>
    </div>
    <div class="customer-maintain" style="float: left;width: 380px">
        <font style="margin-left: 24px;font-size: 16px;color:#666666;margin-top:16px;float: left;">催单</font>
        <font style="float: left;font-size: 12px;margin-top: 44px;margin-left: 100px;margin-bottom: 5px;color:#666666;">一次催单</font>
        <font style="float: left;font-size: 12px;margin-top: 44px;margin-left: 25px;margin-bottom: 5px;color:#666666;">多次催单</font>
        <font style="font-size: 12px;margin-top: 44px;margin-left: 25px;float: left;margin-bottom: 5px;color:#666666;">超48小时催单</font>

        <%@include file="/WEB-INF/views/modules/providerrpt/chart/customerReminderChart.jsp" %>
        <font id="reminderQty" style="float: left;font-size: 36px;margin-left: 24px;width: 30px;margin-top: -8px;">0</font>
        <font id="reminderFirstQty" style="float: left;font-size: 16px;margin-left: 100px;width: 25px;color: #00B0FF">0</font>
        <font id="reminderMultipleQty" style="float: left;font-size: 16px;margin-left: 50px;width: 25px;color: #00B0FF">0</font>
        <font id="exceed48hourReminderQty" style="float: left;font-size: 16px;margin-left: 48px;width: 25px;color: #00B0FF">0</font>
        <font style="float: left;font-size: 12px;color: #999999;margin-left: 24px;margin-top: 12px">占下单比例:</font>
        <font id="reminderRate" style="float: left;font-size: 12px;color: #999999;margin-left:5px;width: 34px;margin-top: 12px">0%</font>
        <font id="reminderFirstRate" style="float: left;font-size: 12px;color: #999999;margin-left:29px;width: 34px;margin-top: 12px">0%</font>
        <font id="reminderMultipleRate" style="float: left;font-size: 12px;color: #999999;margin-left:41px;width: 34px;margin-top: 12px">0%</font>
        <font id="exceed48hourReminderRate" style="float: left;font-size: 12px;color: #999999;margin-left:39px;width:57px;margin-top: 12px">0%</font>

        <dev id="main5" style="width: 40px;height: 40px;float: left;margin-left:20px;margin-top: 20px"></dev>

        <p style="float: left;font-size: 12px;color: #999999;margin-left: 15px;margin-top: 18px;width: 95px">24小时完成率</p>
        <p id="complete24hourRate" style="float: left;font-size: 12px;color: #00B0FF;margin-left: -95px;margin-top: 44px">0%</p>

        <dev id="main6" style="width: 40px;height: 40px;float: left;margin-left:41px;margin-top: 20px"></dev>
        <p style="float: left;font-size: 12px;color: #999999;margin-left: 15px;margin-top: 18px;width: 100px">48小时完成率</p>
        <p id="over48ReminderCompletedRate" style="float: left;font-size: 12px;color: #00B0FF;margin-left: 15px;margin-top: -6px;">0%</p>
    </div>
    <div class="customer-maintain" style="float: left;width: 303px">
        <%@include file="/WEB-INF/views/modules/providerrpt/chart/praiseOrderChart.jsp" %>
        <font style="margin-left: 16px;font-size: 16px;color:#666666;margin-top:14px;float: left;">好评单</font>
        <font style="margin-left: 10px;font-size: 16px;color:#666666;margin-top:14px;float: left;width: 213px">432</font >
        <div style="float: left;width: 70px;height: 70px;margin-left: 16px;margin-top: 11px">
            <font style="color: #00B0FF;float: left;font-size: 14px;">■</font>

            <font style="float: left;font-size: 12px;margin-left: 5px;color: #666666;">按时审核</font>
            <font style="color: #13C2C2;float: left;font-size: 14px;margin-top: 6px;">■</font>

            <font style="float: left;font-size: 12px;margin-top: 6px;margin-left: 5px;color: #666666">超时审核</font>
            <font style="color: #F3637B;float: left;font-size: 14px;margin-top: 6px;">■</font>

            <font style="float: left;font-size: 12px;margin-top: 6px;margin-left: 5px;color: #666666">未审核</font>
        </div>
        <div id="main19" style="float: left;width: 180px;height: 90px;margin-left: 32px">

        </div>
        <font style="float: left;font-size: 10px;margin-left: 104px;color: #0096DA">支出</font>
        <font style="float: left;font-size: 10px;margin-left: 5px;color: #0096DA">70%</font>
        <font style="float: left;font-size: 10px;margin-left: 5px;color: #0096DA">924</font>
        <font style="float: left;font-size: 10px;color: #0096DA">元</font>
        <div id="main20" style="float: left;width: 300px;height: 15px;">

        </div>

        <font style="float: left;font-size: 10px;margin-left: 104px;color: #0096DA">有好评费</font>
        <font style="float: left;font-size: 10px;margin-left: 5px;color: #0096DA">80%</font>
        <font style="float: left;font-size: 10px;margin-left: 5px;color: #0096DA">279</font>

        <div id="main21" style="float: left;width: 300px;height: 15px;">

        </div>
    </div>
    <div class="customer-maintain" style="float: left;width: 694px">
        <%@include file="/WEB-INF/views/modules/providerrpt/chart/crushOrderChart.jsp" %>
        <dev style="width: 347px;height:215px;float: left;">
            <font style="margin-left: 16px;font-size: 16px;color:#666666;margin-top:14px;float: left;">突击单</font>
            <font id="orderCrushQty" style="margin-left: 10px;font-size: 16px;color:#666666;margin-top:14px;float: left;width: 213px">0</font >
            <font style="float: left;font-size: 12px;color: #999999;margin-left:16px;margin-top: 6px">一次突击</font>
            <font id="onceCrushQty" style="float: left;font-size: 12px;color: #00B0FF;margin-left:5px;margin-top: 6px">0</font>
            <font style="float: left;font-size: 12px;color: #999999;margin-left:16px;margin-top: 6px">多次突击</font>
            <font id="repeatedlyCrushQty" style="float: left;font-size: 12px;color: #00B0FF;margin-left:5px;margin-top: 6px;width: 150px">0</font>
            <dev style="float: left;width: 65px;height: 60px;margin-top: 28px">
                <font style="float: left;font-size: 12px;color: #999999;margin-left:16px;">一次突击</font>
                <font id="orderCrush" style="margin-left: 28px;font-size: 16px;color:#666666;margin-top:10px;float: left;">0</font>
            </dev>
            <div id="main17" style="width: 265px;height: 90px;float: left;margin-left: 16px;margin-top: 16px"></div>
            <div style="float: left;margin-right: 150px;height: 32px;width: 315px;margin-left:16px;background:#F6F6F6;">
                <font style="float: left;margin-top: 5px;font-size: 12px;color: #999999;margin-left: 85px;margin-bottom: 5px;">一次突击占下单比例：</font>
                <font id="onceCrushPlainOrderRate" style="float: left;margin-top: 5px;font-size: 12px;color: #00B0FF;margin-left: 5px;">0%</font>
            </div>
        </dev>
        <div style="border-left: 1px dashed #EEEEEE;height:90%;line-height:1px;float: left;margin-top: 16px;"></div>
        <dev style="width: 346px;height:215px;float: left;">
            <font style="margin-left: 16px;font-size: 16px;color:#666666;margin-top:14px;float: left;">已完成</font>
            <font id="completedCrushQty" style="margin-left: 10px;font-size: 16px;color:#666666;margin-top:14px;float: left;width: 213px">0</font >
            <font style="float: left;font-size: 12px;color: #999999;margin-left:16px;margin-top: 6px">一次突击</font>
            <font id="completedOnceCrushRate" style="float: left;font-size: 12px;color: #999999;margin-left:5px;margin-top: 6px">0%</font>
            <font id="completedOnceCrush" style="float: left;font-size: 12px;color: #00B0FF;margin-left:5px;margin-top: 6px">0</font>
            <font style="float: left;font-size: 12px;color: #999999;margin-left:16px;margin-top: 6px">多次突击</font>
            <font id="completedRepeatedlyCrushRate" style="float: left;font-size: 12px;color: #999999;margin-left:5px;margin-top: 6px">0%</font>
            <font id="completedRepeatedlyCrush" style="float: left;font-size: 12px;color: #00B0FF;margin-left:5px;margin-top: 6px">0</font>

            <div id="main18" style="float: left;width: 347px;height: 100px;"></div>
            <font style="color: #00B0FF;float: left;margin-left: 57px;font-size: 14px;margin-top: 10px;">■</font>

            <font style="float: left;font-size: 12px;margin-top: 10px;margin-left: 5px;color: #666666">24小时</font>
            <font style="color: #13C2C2;float: left;margin-left: 32px;font-size: 14px;margin-top: 10px;">■</font>

            <font style="float: left;font-size: 12px;margin-top: 10px;margin-left: 5px;color: #666666">48小时</font>
            <font style="color: #F3637B;float: left;margin-left: 32px;font-size: 14px;margin-top: 10px;">■</font>

            <font style="float: left;font-size: 12px;margin-top: 10px;margin-left: 5px;color: #666666">48小时以上</font>
        </dev>
    </div>

    <div class="charge-maintain" style="float: left;width: 575px">
        <%@include file="/WEB-INF/views/modules/providerrpt/chart/incurExpenseChart.jsp" %>
        <font style="margin-left: 16px;font-size: 16px;color:#666666;margin-top:16px;float: left;">支出费用</font>
        <font id="total" style="margin-left: 10px;font-size: 16px;color:#666666;margin-top:16px;float: left;width: 100px">0</font >
        <dev id="main2" style="width:570px;height: 240px;float: left;"></dev>
    </div>
    <div class="charge-maintain" style="float: left;width:1066px;margin-left:16px;">

        <div style="float: left;width: 532px;height: 326px">
            <%@include file="/WEB-INF/views/modules/providerrpt/chart/servicepointQtyChart.jsp" %>
            <font style="margin-left: 16px;font-size: 16px;color:#666666;margin-top:16px;float: left;">网点</font>
            <font id="servicePointTotal" style="margin-left: 10px;font-size: 16px;color:#666666;margin-top:16px;float: left;width: 100px">0</font >

            <font style="color: #00B0FF;float: left;margin-left: 180px;font-size: 14px;margin-top: 17px;">■</font>

            <font style="float: left;font-size: 12px;margin-top: 17px;margin-left: 5px;color: #666666">常用网点</font>

            <font style="color: #13C2C2;float: left;margin-top: 17px;font-size: 14px;margin-left: 32px;">■</font>

            <font style="float: left;font-size: 12px;margin-top: 17px;margin-left: 5px;color: #666666">试用网点</font>
            <div id="main9" style="float: left;width: 520px;height: 40px;margin-top: 10px;margin-left: 1px"></div>
            <div id="main10" style="float: left;width: 177px;height: 90px"></div>
            <div id="main11" style="float: left;width: 177px;height: 90px"></div>
            <div id="main12" style="float: left;width: 177px;height: 90px"></div>

            <p style="float: left;margin-left: 55px;margin-top: 7px;font-size: 12px;color:#999999">占比:</p>
            <p id="ProductCategory1Rate" style="float: left;margin-top: 7px;font-size: 12px;color: #666666;width: 60px;text-align: left;">0%</p>

            <p style="float: left;margin-left: 86px;margin-top: 7px;font-size: 12px;color:#999999">占比:</p>

            <p id="ProductCategory2Rate" style="float: left;margin-top: 7px;font-size: 12px;color: #666666;width: 60px;text-align: left;">0%</p>

            <p style="float: left;margin-left: 92px;margin-top: 7px;font-size: 12px;color:#999999">占比:</p>
            <p id="ProductCategory3Rate" style="float: left;margin-top: 7px;font-size: 12px;color: #666666;width: 60px;text-align: left;">0%</p>

            <div style="float: left;margin-right: 150px;height: 55px;width: 499px;margin-left:16px;background:#F6F6F6;margin-top: 9px">
                <p style="float: left;margin-top: 18px;font-size: 14px;color: #999999;width: 72px;margin-left: 16px">自动派单</p>
                <div style="border-left: 1px dashed #BBBBBB;height:62%;line-height:1px;float: left;margin-top: 12px;"></div>

                <dev style="float: left;width: 125px;height: 64px;margin-left:48px;">
                    <font id="autoPlanProductCategory1Name" style="float: left;margin-top: 5px;font-size: 12px;color: #999999;margin-bottom: 5px;width: 118px">厨电</font>
                    <p id="autoPlan1Qty" style="float: left;font-size: 14px;color: #00B0FF;">0</p>
                    <p id="autoPlan1Rate" style="float: left;font-size: 12px;color: #999999;text-align: left;margin-left: 6px">0%</p>
                </dev>
                <dev style="float: left;width: 125px;height: 64px">
                    <font id="autoPlanProductCategory2Name" style="float: left;margin-top: 5px;font-size: 12px;color: #999999;margin-bottom: 5px;width: 118px">净水</font>
                    <p id="autoPlan2Qty" style="float: left;font-size: 14px;color: #00B0FF;">0</p>
                    <p id="autoPlan2Rate" style="float: left;font-size: 12px;color: #999999;margin-left: 6px">0%</p>
                </dev>
                <dev style="float: left;width: 100px;height: 64px">
                    <font id="autoPlanProductCategory3Name" style="float: left;margin-top: 5px;font-size: 12px;color: #999999;margin-bottom: 5px;width: 93px">特价</font>
                    <p id="autoPlan3Qty" style="float: left;font-size: 14px;color: #00B0FF;">0</p>
                    <p id="autoPlan3Rate" style="float: left;font-size: 12px;color: #999999;margin-left: 6px">0%</p>
                </dev>


            </div>
        </div>

        <div style="border-left: 1px dashed #BBBBBB;height:90%;line-height:1px;float: left;margin-top: 16px;"></div>

        <div style="float: left;width: 532px;height: 326px;">
            <%@include file="/WEB-INF/views/modules/providerrpt/chart/servicePointStreetQtyChart.jsp" %>
            <font style="margin-left: 16px;font-size: 16px;color:#666666;margin-top:16px;float: left;">街道</font>
            <font id="servicePointStreet" style="margin-left: 10px;font-size: 16px;color:#666666;margin-top:16px;float: left;width: 100px">0</font >


            <font style="color: #00B0FF;float: left;margin-left: 40px;font-size: 14px;margin-top: 17px;">■</font>

            <font style="float: left;font-size: 12px;margin-top: 17px;margin-left: 5px;color: #666666">常用网点街道</font>

            <font style="color: #13C2C2;float: left;margin-top: 17px;font-size: 14px;margin-left: 24px;">■</font>

            <font style="float: left;font-size: 12px;margin-top: 17px;margin-left: 5px;color: #666666">试用网点街道</font>

            <font style="color: #F3637B;float: left;margin-top: 17px;font-size: 14px;margin-left: 24px;">■</font>

            <font style="float: left;font-size: 12px;margin-top: 17px;margin-left: 5px;color: #666666">无网点街道</font>

            <div id="main13" style="float: left;width: 520px;height: 40px;margin-top: 10px"></div>
            <div id="main14" style="float: left;width: 177px;height: 100px;margin-top: 10px;"></div>
            <div id="main15" style="float: left;width: 177px;height: 100px;margin-top: 10px;"></div>
            <div id="main16" style="float: left;width: 177px;height: 100px;margin-top: 10px;"></div>

            <div style="float: left;margin-right: 150px;height: 55px;width: 499px;margin-top: 26px;margin-left:16px;background:#F6F6F6;">

                <div style="float: left;height: 55px;width: 150px;">
                    <font style="float: left;margin-top: 5px;font-size: 12px;color: #999999;margin-left: 24px;margin-bottom: 5px;width: 100px">自动派单街道</font>
                    <font id="autoPlanStreet" style="float: left;font-size: 14px;margin-left:25px;color: #00B0FF;">0</font>
                    <font id="autoPlanStreetRate" style="float: left;font-size: 12px;margin-left:5px;color: #999999;width: 60px;text-align: left">0%</font>
                </div>


                <font style="float: left;margin-left:10px;margin-top: 5px;font-size: 12px;color: #999999;margin-bottom: 5px;">自动派单街道无常用网点</font>
                <font style="float: left;margin-left:51px;margin-top: 5px;font-size: 12px;color: #999999;margin-bottom: 5px;">常用网点街道无自动派单</font>



                <font id="autoPlanWithoutFrequent" style="float: left;margin-left:11px;font-size: 14px;width:40px;color: #F54142;text-align: left;">0</font>

                <font id="frequentWithoutAutoPlan" style="float: left;margin-left:143px;font-size: 14px;width:40px;color: #F54142;text-align: left;">0</font>

            </div>
        </div>
    </div>
</div>


</body>
</html>
