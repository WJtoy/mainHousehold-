<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>业务好评单-已审核</title>
    <meta name="description" content="已审核">
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
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
    <shiro:hasPermission name="sd:salespraise:view">
        <li><a href="${ctx}/sd/sales/praise/pendingReviewList" title="待审核">待审核</a></li>
        <li class="active"><a href="javascript:void(0);" title="已审核">已审核</a></li>
        <%--<li><a href="${ctx}/sd/sales/praise/findAllList" title="所有">所有</a></li>--%>
    </shiro:hasPermission>
</ul>
<form:form id="searchForm" modelAttribute="praisePageSearchModel" action="${ctx}/sd/sales/praise/approvedList" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div>
        <label>工单单号：</label>
        <input type=text class="input-small" name="orderNo" value="${praisePageSearchModel.orderNo}" maxlength="14" />&nbsp;
        <label>销售单号：</label>
        <input type=text class="input-small" name="parentBizOrderId" value="${praisePageSearchModel.parentBizOrderId}" maxlength="30" />&nbsp;
        <label>服务单号：</label>
        <input type=text class="input-small" name="workcardId" value="${praisePageSearchModel.workcardId}" maxlength="30" style="width: 240px"/>&nbsp;
        <label>好评单号：</label>
        <input type=text class="input-small" name="praiseNo" value="${praisePageSearchModel.praiseNo}" maxlength="13"/>
    </div>
    <div style="margin-top: 8px">
        <label>用户电话：</label>
        <input type=text class="input-small" name="userPhone" value="${praisePageSearchModel.userPhone}" maxlength="12" />&nbsp;
        <label>状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态：</label>
        <form:select path="status" cssClass="input-small" cssStyle="width:135px;">
            <form:option value="0" label="所有"/>
            <form:option value="40" label="通过"/>
            <form:option value="50" label="取消"/>
        </form:select>&nbsp;
        <label>申请时间：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:95px;"
               maxlength="20" class="input-small Wdate" value="${praisePageSearchModel.beginDate}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>&nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:95px" maxlength="20"
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
            <th width="150">好评时效</th>
            <th width="150">销售单号<br>服务单号</th>
            <th width="300">客户</th>
            <th width="200">产品</th>
            <th width="100">用户姓名</th>
            <th width="100">用户电话</th>
            <th width="300">用户地址</th>
            <th width="80">好评费</th>
            <th width="80">状态</th>
            <th width="100">审核人</th>
            <th width="250">审核时间</th>
            <th width="300">好评图片</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowcnt" value="${page.list.size()}"/>
        <c:forEach items="${page.list}" var="model">
            <c:set var="rowNumber" value="${rowNumber+1}"/>
            <tr>
                <td>${rowNumber}</td>
                <td>
                    <a href="javascript:showPraiseInfo('${model.id}','${model.quarter}');">
                            ${model.orderNo}
                    </a>
                </td>
                <c:choose>
                    <c:when test="${model.approveTimeLiness>1}">
                        <td><p style="color: red;margin: auto">用时:${model.timelinessLabel}</p></td>
                    </c:when>
                    <c:otherwise>
                        <td>用时:${model.timelinessLabel}</td>
                    </c:otherwise>
                </c:choose>
                <td>${model.parentBizOrderId}<br>${model.workcardId}</td>
                <td>${model.customerName}</td>
                <td>${model.productNames}</td>
                <td>${model.userName}</td>
                <td>${model.userPhone}</td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.userAddress}">
                            ${fns:abbr(model.userAddress,30)}
                    </a>
                </td>
                <c:choose>
                    <c:when test="${model.status==40}">
                        <td>${model.customerPraiseFee}</td>
                    </c:when>
                    <c:otherwise>
                        <td>${model.applyCustomerPraiseFee}</td>
                    </c:otherwise>
                </c:choose>
                <td>${model.strStatus}</td>
                <td>${model.strUpdateName}</td>
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
/*    $(document).ready(function() {
        if($("#contentTable tbody>tr").length>0) {
            $("#divNoRecord").hide();
            //无数据报错
        }
        else {
            var h = document.body.clientHeight;
            $("#divGrid").height(h-295);
            $("#divNoRecord").show();
        }
    });*/
    function showPraiseInfo(id,quarter) {
        top.layer.open({
            type: 2,
            id: 'layer_salesPending',
            zIndex: 19891015,
            title: '待审核',
            content: "${ctx}/sd/sales/praise/praiseInfoForSales?id="+id + "&quarter=" + quarter,
            area: ['900px','720px'],
            shade: 0.3,
            shadeClose: true,
            maxmin: true
        });
    }
</script>
</body>
</html>
