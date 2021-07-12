<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE HTML>
<head>
    <title>网点质保金明细清单</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }

        .label {padding: 4px 4px !important;text-shadow:none !important;font-weight:400;}
        .label-fullPay {background-color: #0096DA;}

        .label-notFullPay {background-color: #FF9500;}

        .label-notPay {background-color: #F64344;}
        .btn-new {margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 105px;height: 30px;}
        .servicepoint_span{color:#46aeea;display: contents;line-height: 30px;}
    </style>
    <script type="text/javascript">
        $(document).ready(function () {
            $('a[data-toggle=tooltip]').darkTooltip();
            $("#treeTable").treeTable({expandLevel : 2});
            $("#btnSubmit").on("click", function () {
                $("#pageNo").val(1);
                layerLoading('正在查询，请稍等...', true);
                $("#searchForm").submit();
                return false;
            });
        });

        function depositCharge(sid, sname) {
            top.layer.open({
                type: 2,
                id: 'layer_deposit_charge',
                zIndex: 19891015,
                title: '充值',
                content: "${ctx}/fi/servicepoint/deposit/chargeForm?servicePoint.id=" + sid + "&servicePoint.name=" + encodeURIComponent(sname),
                shade: 0.3,
                area: ['800px', '410px'],
                resize: false,
                maxmin: false,
                success: function (layero, index) {
                },
                end: function () {
                }
            });
        }
    </script>
</head>
<body>
<sys:message content="${message}"/>
<c:set var="currentuser" value="${fns:getUser() }"/>
<ul id="navtabs" class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">质保金明细</a></li>
</ul>
<form:form id="searchForm" modelAttribute="depositEntity" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div>
        <c:choose>
            <c:when test="${currentuser.isEngineer()}">
                <input id="servicePointId" type="hidden" name="servicePointId" value="${depositEntity.servicePointId}"/>
                <input id="servicePointName" type="hidden" name="servicePointNo.name"
                       value="${depositEntity.servicePointName}"/>
            </c:when>
            <c:otherwise>
                <label>网点名称：</label>
                <md:selectServicePointForDeposit id="servicePoint" name="servicePointId"
                                                 value="${depositEntity.servicePointId}"
                                                 labelName="servicePointName"
                                                 labelValue="${depositEntity.servicePointName}"
                                                 width="1400" height="760" title="网点" allowClear="true" disabled="false"
                                                 cssStyle="width: 245px;"/>
            </c:otherwise>
        </c:choose>
        <label>创建时间：</label><input id="startDate" name="startDate" type="text" readonly="readonly"
                                   style="width:95px;margin-left:4px" maxlength="20" class="input-small Wdate"
                                   value="${fns:formatDate(depositEntity.startDate,'yyyy-MM-dd')}"
                                   onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>&nbsp;&nbsp;&nbsp;<input id="endDate" name="endDate" type="text" readonly="readonly"
                                                 style="width:95px" maxlength="20" class="input-small Wdate"
                                                 value="${fns:formatDate(depositEntity.endDate,'yyyy-MM-dd')}"
                                                 onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>

        <label>相关单号:</label>
        <form:input path="currencyNo" class="input-medium" maxlength="36" cssStyle="width: 186px"/>
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>
    </div>
</form:form>
<c:set value="" var="servicePointId"/>
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50px">序号</th>
        <th width="100px">变更类型</th>
        <th width="120px">变更前余额(元)</th>
        <th width="120px">变更金额(元)</th>
        <th width="120px">变更后余额(元)</th>
        <th width="220px">相关单号</th>
        <th width="140px">创建时间</th>
        <th>备注</th>
    <tbody>
    <c:forEach items="${page.list}" var="depositCurrency">
        <c:set var="index" value="${index+1}"/>
        <c:if test="${!(depositCurrency.servicePoint.id eq servicePointId) }">
            <c:set value="${depositCurrency.servicePoint.id}" var="servicePointId"/>
            <tr id="${depositCurrency.servicePoint.id}" pId="">
                <td style="text-align: left;padding-left: 10px" colspan="8">
                    <span class="servicepoint_span">网点: ${depositCurrency.servicePoint.servicePointNo}
                    ，${depositCurrency.servicePoint.name}，主账号：${depositCurrency.servicePoint.primary.name}，网点电话：${depositCurrency.servicePoint.contactInfo1}
                    <c:if test="${depositCurrency.servicePoint.mdDepositLevel.id != null}">
                        <c:set var="depositStatus" value="未缴费"/>
                        <c:set var="depositStatusClass" value="label-notPay"/>
                        <c:choose>
                            <c:when test="${depositCurrency.servicePoint.finance.deposit>=depositCurrency.servicePoint.deposit}">
                                <c:set var="depositStatus" value="已缴满"/>
                                <c:set var="depositStatusClass" value="label-fullPay"/>
                            </c:when>
                            <c:when test="${depositCurrency.servicePoint.finance.deposit > 0}">
                                <c:set var="depositStatus" value="未缴满"/>
                                <c:set var="depositStatusClass" value="label-notFullPay"/>
                            </c:when>
                            <c:otherwise></c:otherwise>
                        </c:choose>
                        <a href="javascript:" style="text-decoration:none;" class="label ${depositStatusClass}" data-toggle="tooltip"  data-tooltip='质保等级：${depositCurrency.servicePoint.mdDepositLevel.name}<br/>应缴金额：${fns:formatNum(depositCurrency.servicePoint.deposit)}元<br/>已缴金额：${fns:formatNum(depositCurrency.servicePoint.finance.deposit)}元<br/>每单扣除：${fns:formatNum(depositCurrency.servicePoint.mdDepositLevel.deductPerOrder)}元' >${depositStatus}</a>
                    </c:if>
                    </span>
                </td>
            </tr>
        </c:if>
        <tr id="${depositCurrency.id}" pId="${servicePointId}">
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${depositCurrency.actionTypeName} ${depositCurrency.paymentTypeName}</td>
            <td><fmt:formatNumber pattern="0.00">${depositCurrency.beforeBalance}</fmt:formatNumber></td>
            <td><fmt:formatNumber pattern="0.00">${depositCurrency.amount}</fmt:formatNumber></td>
            <td><fmt:formatNumber pattern="0.00">${depositCurrency.balance}</fmt:formatNumber></td>
            <td>${depositCurrency.currencyNo}</td>
            <td>${fns:formatDate(depositCurrency.createDate, 'yyyy-MM-dd HH:mm:ss')}</td>
            <td>${depositCurrency.remarks}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>