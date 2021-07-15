<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<head>
    <title>网点质保金明细清单(单个网点)</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        .label {padding: 4px 4px !important;text-shadow:none !important;font-weight:400;}
        .label-fullPay{background-color: #0096DA;}
        .label-notFullPay{background-color: #FF9500;}
        .label-notPay{background-color: #F64344;}
        .servicepoint_span{color:#46aeea;display: contents;line-height: 30px;}
    </style>
    <script type="text/javascript">

        $(document).on("click", "#btnSubmit", function () {
            $("#pageNo").val(1);
            this.form.submit();
        });

        $(document).ready(function() {
           $('a[data-toggle=tooltip]').darkTooltip({gravity: 'west'});
        });
    </script>
</head>
<body>
<sys:message content="${message}"/>
<form:form id="searchForm" modelAttribute="depositEntity" action="${ctx}/fi/servicepoint/deposit/myCurrencyList" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="servicePointId" type="hidden" name="servicePointId" value="${depositEntity.servicePointId}" />
    <div>
        <label>创建时间：</label><input id="startDate" name="startDate" type="text" readonly="readonly" style="width:95px;margin-left:4px" maxlength="20" class="input-small Wdate"
                                  value="${fns:formatDate(depositEntity.startDate,'yyyy-MM-dd')}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>&nbsp;&nbsp;&nbsp;<input id="endDate" name="endDate" type="text" readonly="readonly" style="width:95px" maxlength="20" class="input-small Wdate"
                                                 value="${fns:formatDate(depositEntity.endDate,'yyyy-MM-dd')}"  onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>

        <label>相关单号:</label>
        <form:input path="currencyNo" class="input-medium" maxlength="36" cssStyle="width: 186px"/>
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>
    </div>
</form:form>
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th>序号</th>
        <th>变更类型</th>
        <th>变更前余额(元)</th>
        <th>变更金额(元)</th>
        <th>变更后余额(元)</th>
        <th>相关单号</th>
        <th>创建时间</th>
        <th>备注</th>
    <tbody>
    <c:if test="${page.count > 0}" >
        <c:if test="${servicePoint != null}">
        <tr>
            <td style="text-align: left;padding-left: 10px" colspan="8">
                <span class="servicepoint_span">网点: ${servicePoint.servicePointNo}
                    ，${servicePoint.name}，主账号：${servicePoint.primary.name}
                    <c:if test="${servicePoint.mdDepositLevel.id != null}">
                        <c:set var="depositStatus" value="未缴费"/>
                        <c:set var="depositStatusClass" value="label-notPay"/>
                        <c:choose>
                            <c:when test="${servicePoint.finance.deposit >= servicePoint.deposit}">
                                <c:set var="depositStatus" value="已缴满"/>
                                <c:set var="depositStatusClass" value="label-fullPay"/>
                            </c:when>
                            <c:when test="${servicePoint.finance.deposit > 0}">
                                <c:set var="depositStatus" value="未缴满"/>
                                <c:set var="depositStatusClass" value="label-notFullPay"/>
                            </c:when>
                            <c:otherwise></c:otherwise>
                        </c:choose>
                        <a href="javascript:" style="text-decoration:none;" class="label ${depositStatusClass}" data-toggle="tooltip"  data-tooltip='质保等级：${servicePoint.mdDepositLevel.name}<br/>应缴金额：${fns:formatNum(servicePoint.deposit)}元<br/>已缴金额：${fns:formatNum(servicePoint.finance.deposit)}元<br/>每单扣除：${fns:formatNum(servicePoint.mdDepositLevel.deductPerOrder)}元' >${depositStatus}</a>
                    </c:if>
                    </span>
            </td>
        </tr>
        </c:if>
        <c:forEach items="${page.list}" var="currency">
            <c:set var="index" value="${index+1}" />
            <tr>
                <td>${index+(page.pageNo-1)*page.pageSize}</td>
                <td>${currency.actionTypeName}</td>
                <td><fmt:formatNumber pattern="0.0">${currency.beforeBalance}</fmt:formatNumber> </td>
                <td><fmt:formatNumber pattern="0.0">${currency.amount}</fmt:formatNumber></td>
                <td><fmt:formatNumber pattern="0.0">${currency.balance}</fmt:formatNumber></td>
                <td>${currency.currencyNo}</td>
                <td>${fns:formatDate(currency.createDate, 'yyyy-MM-dd HH:mm:ss')}</td>
                <td>${currency.remarks}</td>
            </tr>
        </c:forEach>
    </c:if>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>

