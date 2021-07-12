<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>客户列表-好评</title>
    <meta name="description" content="待回复">
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
        }
        #divNoRecord p {margin:10px 0 10px;}
    </style>
    <script type="text/javascript">

        $(document).ready(function() {
            $("#contentTable").viewer();
        });

        //覆盖分页前方法
        function beforePage() {
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            $("#btnClearSearch").attr('disabled', 'disabled');
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
<ul id="navtabs" class="nav nav-tabs">
    <li><a href="${ctx}/sd/finance/customerOrder/materialList" data-toggle="tooltipnorth" data-tooltip="&nbsp;&nbsp;&nbsp;&nbsp;要发配件的订单">待发配件</a></li>
    <li><a href="${ctx}/sd/finance/customerOrder/list" data-toggle="tooltipnorth" data-tooltip="&nbsp;&nbsp;处理中订单列表">处理中</a></li>
    <li><a href="${ctx}/sd/finance/customerOrder/finishlist" data-toggle="tooltipnorth" data-tooltip="已完成订单列表">已完成</a></li>
    <li><a href="${ctx}/sd/finance/customerOrder/cancellist" data-toggle="tooltipnorth" data-tooltip="取消单列表">取消单</a></li>
    <li><a href="${ctx}/sd/finance/customerOrder/returnlist" data-toggle="tooltipnorth" data-tooltip="退单列表">退单</a></li>
    <li><a href="${ctx}/sd/finance/customerOrder/alllist" data-toggle="tooltipnorth" data-tooltip="所有订单列表">所有</a></li>
    <li><a href="${ctx}/sd/finance/customerOrder/complainlist" data-toggle="tooltipnorth" data-tooltip="投诉列表">投诉</a></li>
    <c:if test="${reminderFlag==1}">
        <li><a href="${ctx}/sd/finance/customerOrder/reminderList" data-toggle="tooltipnorth" data-tooltip="催单列表">催单</a></li>
    </c:if>
    <li class="active"><a href="javascript:void(0);" data-toggle="tooltipnorth" data-tooltip="好评单列表">好评</a></li>
</ul>
<form:form id="searchForm" modelAttribute="praisePageSearchModel" action="${ctx}/sd/finance/customerOrder/praiseList" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div>
        <label>工单单号：</label>
        <input type=text class="input-small" name="orderNo" value="${praisePageSearchModel.orderNo}" maxlength="14" />&nbsp;
        <label>销售单号：</label>
        <input type=text class="input-small" name="parentBizOrderId" value="${praisePageSearchModel.parentBizOrderId}" maxlength="14" />&nbsp;
        <label>服务单号：</label>
        <input type=text class="input-small" name="workcardId" value="${praisePageSearchModel.workcardId}" maxlength="14"/>
    </div>
    <div style="margin-top: 8px">
        <label>好评单号：</label>
        <input type=text class="input-small" name="praiseNo" value="${praisePageSearchModel.praiseNo}" maxlength="14" />&nbsp;
        <label>用户电话：</label>
        <input type=text class="input-small" name="userPhone" value="${praisePageSearchModel.userPhone}" maxlength="12" />&nbsp;
        <label>完成时间：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:120px;"
               maxlength="20" class="input-small Wdate" value="${praisePageSearchModel.beginDate}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>&nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:120px" maxlength="20"
               class="input-small Wdate" value="${praisePageSearchModel.endDate}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>&nbsp;&nbsp;
    </div>
</form:form>
<sys:message content="${message}"/>
<c:set var="rowNumber" value="0"/>
<div id="divNoRecord" style="display: none">
    <div class="alert alert-info">
        <h4>提示!</h4>
        <p>
            查询无符合的数据，请调整查询条件重新查询。
        </p>
    </div>
</div>
    <table id="contentTable" class="table table-bordered table-condensed table-hover">
        <thead>
        <tr>
            <th width="40">序号</th>
            <th width="130">工单单号</th>
            <th width="100">销售单号<br>服务单号</th>
            <th width="150">用户姓名</th>
            <th width="100">用户电话</th>
            <th width="200">用户地址</th>
            <th width="80">好评费</th>
            <th width="200">完成时间</th>
            <th width="250">图片</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowcnt" value="${page.list.size()}"/>
        <c:forEach items="${page.list}" var="model">
            <c:set var="rowNumber" value="${rowNumber+1}"/>
            <tr>
                <td>${rowNumber}</td>
                <td>
                    <a href="javascript:void(0);" style="" onclick="Order.viewOrderDetail('${model.orderId}','${model.quarter}');">
                       ${model.orderNo}
                    </a>
                </td>
                <td>${model.parentBizOrderId}<br>${model.workcardId}</td>
                <td>${model.userName}</td>
                <td>${model.userPhone}</td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.userAddress}">
                            ${fns:abbr(model.userAddress,30)}
                    </a>
                </td>
                <td>${model.customerPraiseFee}</td>
                <td>${model.strUpdateDate}</td>
                <td>
                    <c:if test="${model.picItems !=null and fn:length(model.picItems) >0}">
                        <c:forEach var="pic" items="${model.picItems}">
                            <img title='点击放大' src='${ctxUpload}/${pic.url}'  data-original='${ctxUpload}/${pic.url}' style="width: 50px;height: 40px"/>
                        </c:forEach>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
<c:if test="${rowcnt > 0}">
    <div id="pagination" class="pagination">${page}</div>
</c:if>
<script type="text/javascript" language="javascript">
    $(document).ready(function () {
        $('a[data-toggle=tooltip]').darkTooltip();
        $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
        $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
        customerLimitDatePicker('beginDate','endDate',6);
    });
</script>
<style type="text/css">
    .dropdown-menu {min-width: 80px;}
    .dropdown-menu > li > a {text-align: left;padding: 3px 10px;}
    .pagination {margin: 4px 0 0 4px;}
    .label-search {width: 70px;  text-align: right;}
    form {margin: 0 0 5px;}
</style>
<script type="text/javascript">
</script>
</body>
</html>
