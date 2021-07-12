<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>客服-历史派单记录</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/ServicePointOrderService.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <%@ include file="/WEB-INF/views/include/WdateLimitPicker.jsp" %>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
        }
    </style>
    <script type="text/javascript">
        ServicePointOrderService.rootUrl = "${ctx}";
        //覆盖分页前方法
        function beforePage() {
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            layerLoading("查询中...", true);
        }

        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function () {
            if (clicktag == 0) {
                clicktag = 1;
                beforePage();
                setPage();
                this.form.submit();
            }
        });
    </script>

</head>
<body>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="order" method="post" action="${ctx}/sd/order/kefuOrderList/historyPlanList" class="form-inline">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div class="control-group">
        <label class="label-search">区域:</label>&nbsp;
        <%--<sys:treeSelectAnyArea id="area" name="area.id" value="${order.area.id}"
                               levelValue="${order.areaLevel}" parentValue="${order.area.parent.id}"
                               labelName="area.name" labelValue="${order.area.name}"
                               title="区域" clearIdValue="0" allowClear="false"
                               url="/sys/area/treeData?kefu=${currentuser.id}"  nodesLevel="2" nameLevel="4" canSelectLevel="2"/>--%>

        <sys:treeselectareanew id="area" name="area.id" value="${order.area.id}" levelValue="${order.areaLevel}"  parentValue="${order.area.parent.id}"
                               labelName="area.name" labelValue="${order.area.name}" title="区域" clearIdValue="0"
                               url="/sys/area/treeDataNew?kefu=${currentuser.id}" allowClear="false" nodesLevel="2" nameLevel="4" canSelectLevel="2"/>

        <label>下单日期：</label>
        <input id="beginDate" name="beginDate" type="text"  style="width:95px;margin-left:4px"
               maxlength="20" class="input-small Wdate" value="${fns:formatDate(order.beginDate,'yyyy-MM-dd')}" />
        <label>~</label>&nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" style="width:95px" maxlength="20"
               class="input-small Wdate" value="${fns:formatDate(order.endDate,'yyyy-MM-dd')}" />
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>&nbsp;&nbsp;
    </div>
</form:form>
<sys:message content="${message}"/>
<c:set var="rowNumber" value="0"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="40">序号</th>
        <th width="120">单号</th>
        <th width="40">状态</th>
        <th width="70">品牌</th>
        <th width="130">产品</th>
        <th width="100">型号</th>
        <th width="45">数量</th>
        <th width="65">服务类型</th>
        <th width="65">网点</th>
        <th width="65">用户</th>
        <th width="100">电话</th>
        <th>地址</th>
        <th width="100">费用</th>
    </tr>
    </thead>
    <tbody>
    <c:set var="rowcnt" value="${page.list.size()}"/>
    <c:forEach items="${page.list}" var="order">
        <c:set var="rowNumber" value="${rowNumber+1}"/>
        <c:set var="rowspan" value="${order.items.size() eq 0?1:order.items.size() }"/>
        <tr>
            <td rowspan="${rowspan}">${rowNumber}</td>
            <td rowspan="${rowspan}"><a href="javascript:void(0);" onclick="ServicePointOrderService.showKefuHistoryOrderDetail('${order.orderId}','${order.quarter}','layer_orderdetailkefu');">${order.orderNo}</a></td>
            <td rowspan="${rowspan}">
                <span class="label status_${order.statusValue}">${order.statusName}</span>
            </td>
            <c:set var="item" value="${order.items[0]}"/>
            <td>${item.brand}</td>
            <td>${item.product.name}</td>
            <td>${item.productSpec}</td>
            <td>${item.qty}</td>
            <td>${item.serviceType.name }</td>
            <td rowspan="${rowspan}">${order.servicePointName}</td>
            <td rowspan="${rowspan}">${order.userName}</td>
            <td rowspan="${rowspan}">${order.userPhone}</td>
            <td rowspan="${rowspan}">${order.area.name}${order.userAddress}</td>
            <td rowspan="${rowspan}">
                ${order.chargeText}
            </td>
        </tr>
        <c:forEach items="${order.items}" var="item" varStatus="i" begin="1">
            <tr class="item">
                <td>${item.brand}</td>
                <td>${item.product.name }</td>
                <td>${item.productSpec}</td>
                <td>${item.qty}</td>
                <td>${item.serviceType.name}</td>
            </tr>
        </c:forEach>

    </c:forEach>
    </tbody>
</table>
<c:if test="${rowcnt > 0}">
    <div id="pagination" class="pagination">${page}</div>
</c:if>
<style type="text/css">
    .dropdown-menu {
        min-width: 80px;
    }

    .dropdown-menu > li > a {
        text-align: left;
        padding: 3px 10px;
    }

    .pagination {
        margin: 4px 0 0 4px;
    }

    .label-search {
        width: 70px;
        text-align: right;
    }

    form {
        margin: 0 0 5px;
    }
</style>
<script type="text/javascript">
    oneYearDatePicker('beginDate','endDate',false);
</script>
</body>
</html>
