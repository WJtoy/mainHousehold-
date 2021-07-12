<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<head>
    <title>线下充值明细</title>
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
        .status_10{background-color: #0096DA;color: white;padding: 2px 4px;border-radius: 3px}
        .status_20{background-color: #34C758;color: white;padding: 2px 4px;border-radius: 3px}
        .status_30{background-color: #AEAEB2;color: white;padding: 2px 4px;border-radius: 3px}
    </style>
    <script type="text/javascript">
        $(document).ready(function() {
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
            $("#treeTable").treeTable({expandLevel : 5});

            $("#btnSubmit").on("click", function(){
                $("#pageNo").val(1);
                var val = $("#customerId").val();
                if (val == undefined || val.length == 0) {
                    layerInfo("请选择客户!", "信息提示");
                    return false;
                }
                var firstSearch = 1;
                layerLoading('正在查询，请稍等...',true);
                var url = "${ctx}/fi/customercurrency/customerofflinelist?firstSearch=" + firstSearch;
                $("#searchForm").attr("action",url);
                $("#searchForm").submit();
                return false;
            });
        });
    </script>
</head>
<body>
<sys:message content="${message}"/>
<ul class="nav nav-tabs">

    <li class="active"><a href="javascript:void(0);">线下充值明细</a></li>
    <shiro:hasPermission name="fi:blockamountlist:view"><li><a href="${ctx}/fi/customercurrency/blockamountlist">冻结明细</a></li></shiro:hasPermission>
    <li><a href="${ctx}/fi/customercurrency/list">余额明细</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }" />
<form:form id="searchForm" modelAttribute="search" action="${ctx}/fi/customercurrency/customerofflinelist" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="firstSearch" value="${firstSearch}" type="hidden" name="firstSearch">
    <div>
        <label class="control-label"><span class="red">*</span>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
        <c:choose>
            <c:when test="${currentuser.isCustomer()}">
                <input type="text" readonly="true" id="customerName" name="customerName" value="${currentuser.customerAccountProfile.customer.name}" />
                <input type="hidden" readonly="true" id="customerId" name="customerId" value="${currentuser.customerAccountProfile.customer.id}" />
            </c:when>
            <c:otherwise>
                <select id="customerId" name="customerId" class="input-small"
                        style="width:200px;">
                    <option value=""
                            <c:out value="${(empty search.customerId)?'selected=selected':''}" />>所有</option>
                    <c:forEach items="${fns:getMyCustomerListFromMS()}" var="dict">
                        <option value="${dict.id}"
                                <c:out value="${(search.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                    </c:forEach>
                </select>

            </c:otherwise>
        </c:choose>
        &nbsp;&nbsp;
        <%--<label>支付宝账号：</label>
        <form:input path="alipayAccount" htmlEscape="false" type="text" style="width: 186px" class="input-small" />--%>
        <label>充值方式：</label>
        <form:select path="payType" cssClass="input-small" cssStyle="width:113px;">
            <form:option value="0" label="所有"/>
            <form:option value="10" label="支付宝"/>
            <form:option value="20" label="微信"/>
        </form:select>
        &nbsp;&nbsp;
        <label>充值时间：</label>
        <input id="createDate" name="createDate" type="text" readonly="readonly" style="width:120px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${search.createDate}' pattern='yyyy-MM-dd' type='date'/>" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>
        <input id="updateDate" name="updateDate" type="text" readonly="readonly" style="width:125px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${search.updateDate}' pattern='yyyy-MM-dd' type='date'/>" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;&nbsp;
        <label>状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态：</label>
        <form:select path ="status" class="input-small" style="width:200px;">
            <form:option value="0" label="所有" />
            <form:option value="10" label="待审核" />
            <form:option value="20" label="通过" />
            <form:option value="30" label="无效" />
        </form:select>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>
    </div>
</form:form>
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th>序号</th>
        <th>客户名称</th>
        <th>充值方式</th>
        <th>交易单号</th>
        <th>状态</th>
        <th>充值金额</th>
        <th>实际到账金额</th>
        <th>实际入账金额</th>
        <th>充值时间</th>
        <th>审核时间</th>
        <th>备注</th>
    <tbody>
    <c:forEach items="${page.list}" var="item">
        <c:set var="index" value="${index+1}" />
        <tr>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${item.customerName}</td>
            <c:choose>
                <c:when test="${item.payType==10}">
                    <td>支付宝</td>
                </c:when>
                <c:when test="${item.payType==20}">
                    <td>微信</td>
                </c:when>
                <c:otherwise>
                    <td></td>
                </c:otherwise>
            </c:choose>
            <td>${item.transferNo}</td>
            <c:if test="${item.status == 10}">
                <td><span class="status_10">待审核</span></td>
            </c:if>
            <c:if test="${item.status == 20}">
                <td><span class="status_20">通过</span></td>
            </c:if>
            <c:if test="${item.status == 30}">
                <td><a href="javascript:void(0)" data-toggle="tooltip" style="text-decoration:none" data-tooltip="${fns:getDictLabelFromMS(item.invalidType,'recharge_invalid_type','')}">
                    <span class="status_30">无效</span></a></td>
            </c:if>

            <td><fmt:formatNumber pattern="0.00">${item.pendingAmount}</fmt:formatNumber></td>
            <td><fmt:formatNumber pattern="0.00">${item.actualAmount}</fmt:formatNumber></td>
            <td><fmt:formatNumber pattern="0.00">${item.finallyAmount}</fmt:formatNumber></td>
            <td>${item.rechargeTime}</td>
            <td>${item.pendingTime}</td>
            <td>${item.remarks}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>
