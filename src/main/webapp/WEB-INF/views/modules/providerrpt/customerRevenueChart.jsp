<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>客户营收排名</title>
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

        .parent:after{
            content:"";
            height:0;
            line-height:0;
            display:block;
            visibility:hidden;
            clear:both;
        }

        .target{
            display:none;
            z-index: 4;
        }

        .triggle:hover + .target {
            display: block;
        }

        .border{
            display: none;
            opacity: 0.8;
            width: 0 !important;
            border-bottom:solid 12px #1B1E24;
            border-left:12px solid transparent;
            border-right: 6px solid transparent;
            boder-top: 0px solid transparent;
        }
    </style>

    <script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $(".triggle").on('hover', function(){
                $(".border").css({
                    display:"block"
                })
            })
            $(".triggle").on('mouseleave', function(){
                $(".border").css({
                    display:"none"
                })
            })
            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action", "${ctx}/rpt/provider/customerRevenue/customerRevenueChart");
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
            margin-right: 150px;
            float: left;
            width: 425px;
            margin-top: 50px;
            margin-left: 100px;
            background: #fff;
            border: 1px solid #FFF;
            box-shadow: #d0d0d0 1px 1px 10px 0px;

        }

        .top-10 ul {
            counter-reset: section;
        }

        .top-10 li {

            width: 375px;
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
        <a href="${ctx}/rpt/provider/customerRevenue/customerRevenueReport">客户营收明细</a>
    </li>
    <li class="active"><a href="javascript:void(0);">客户营收排名</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition"
           action="${ctx}/rpt/provider/customerRevenue/customerRevenueChart" method="post"
           class="breadcrumb form-search">
    <div>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>客　　户：</label>
        <select id="customerId" name="customerId" class="input-small" style="width:225px;">
            <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}"/>>所有
            </option>
            <c:forEach items="${fns:getCustomerList()}" var="dict">
                <option value="${dict.id}" <c:out
                        value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
            </c:forEach>
        </select>
        &nbsp; &nbsp;
        <label>产品品类：</label>
        <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
            <option value="0" <c:out value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${productCategoryList}" var="dict">
                <option value="${dict.id}" <c:out value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
            </c:forEach>
        </select>
        &nbsp; &nbsp;
        <label>年份：</label>
        <select id="selectedYear" name="selectedYear" class="input-small" style="width:85px;">
            <c:forEach items="${fns:getReportQueryYears()}" var="year">
                <option value="${year}" <c:out
                        value="${(rptSearchCondition.selectedYear eq year)?'selected=selected':''}"/>>${year}</option>
            </c:forEach>
        </select>
        &nbsp; &nbsp;

        <label>月份：</label>
        <select id="selectedMonth" name="selectedMonth" class="input-mini" style="width:85px;">
            <c:forEach var="i" begin="0" end="11" step="1">
                <option value="${1+i}" <c:out
                        value="${(rptSearchCondition.selectedMonth eq 1+i)?'selected=selected':''}"/>>${1+i}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 250px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：隔天数据<br/>
                    统计方式：审单时间<br/>
                    栏位说明：<br/>
                    【工单毛利】应收合计-应付合计<br/>
                    【每单毛利】工单毛利/完成订单
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}"/>
<div style="width: 1500px">
    <div class="top-10" style="float: left">
        <h4>客户${rptSearchCondition.selectedMonth}月完成排名</h4>

        <ul>
            <c:forEach items="${finishOrders}" var="item">
                <li>
                    <c>${item.customerName}</c>
                    <b>${item.finishOrder}</b>
                </li>

            </c:forEach>
        </ul>
    </div>
    <div class="top-10" >
        <h4>客户${rptSearchCondition.selectedMonth}月应收排名</h4>

        <ul>
            <c:forEach items="${totalReceivables}" var="item">
                <li>
                    <c>${item.customerName}</c>
                    <b><fmt:formatNumber maxFractionDigits="2">${item.receivableCharge}</fmt:formatNumber></b>
                </li>

            </c:forEach>
        </ul>
    </div>
    <div class="top-10" style="float: left">
        <h4>客户${rptSearchCondition.selectedMonth}月工单毛利排名</h4>

        <ul>
            <c:forEach items="${orderGrossProfits}" var="item">
                <li>
                    <c>${item.customerName}</c>
                    <b><fmt:formatNumber maxFractionDigits="2">${item.orderGrossProfit}</fmt:formatNumber></b>
                </li>

            </c:forEach>
        </ul>
    </div>
    <div class="top-10">
        <h4>客户${rptSearchCondition.selectedMonth}月每单毛利排名</h4>

        <ul>
            <c:forEach items="${everySingleGrossProfits}" var="item">
                <li>
                    <c>${item.customerName}</c>
                    <b><fmt:formatNumber maxFractionDigits="2">${item.everySingleGrossProfit}</fmt:formatNumber></b>
                </li>

            </c:forEach>
        </ul>
    </div>
</div>
</body>
</html>
