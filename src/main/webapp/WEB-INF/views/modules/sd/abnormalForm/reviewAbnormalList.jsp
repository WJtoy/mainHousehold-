<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>异常单列表-App异常</title>
    <meta name="description" content="待回复">
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
        }
        #divNoRecord p {margin:10px 0 10px;}
    </style>
    <script type="text/javascript">
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

        top.layer.closeAll();
        Order.rootUrl = "${ctx}";

        $(document).on("click", "#btnClearSearch", function () {
            $("#searchForm")[0].reset();
            $("#orderNo").val("");
            $("#subType").val("0");
            $("#servicepointId").val("0");
            $("#servicepointName").val("");
            var dateStr = DateFormat.format(new Date(), 'yyyy-MM-dd');
            $("#endDate").val(dateStr);
            $("#beginDate").val(DateFormat.format(DateFormat.addMonthStr(dateStr, -3), 'yyyy-MM-01'));
            search();
        });
    </script>

</head>

<body>
<ul id="navtabs" class="nav nav-tabs">
    <shiro:hasPermission name="sd:abnormal:view">
       <%-- <li class="active"><a href="javascript:void(0);" title="异常单待处理">待处理</a></li>--%>
        <li><a href="${ctx}/sd/abnormalForm/praiseAbnormalList" title="好评驳回">好评驳回</a></li>
        <li class="active"><a href="javascript:void(0);" title="审单异常">审单异常</a></li>
        <li><a href="${ctx}/sd/abnormalForm/appAbnormalList" title="app异常">app异常</a></li>
        <li><a href="${ctx}/sd/abnormalForm/appCompleteAbnormalList" title="app完工异常">app完工异常</a></li>
      <%--  <li><a href="${ctx}/sd/abnormalForm/smsAbnormalList" title="短信异常">短信异常</a></li>
        <li><a href="${ctx}/sd/abnormalForm/oldAppAbnormalList" title="旧app异常">旧app异常</a></li>--%>
        <li><a href="${ctx}/sd/abnormalForm/processedList" title="异常单已处理">已处理</a></li>
    </shiro:hasPermission>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="abnormalForm" action="${ctx}/sd/abnormalForm/waitProcessList" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
        注:当用工单号搜索查询时，不受当前时间、异常分类等其他条件的限制。
    </div>
    <div class="control-group">
        <label>工单单号：</label>
        <input type=text class="input-small" id="orderNo" name="orderNo" value="${abnormalForm.orderNo}" maxlength="14" style="width: 242px"/>&nbsp;
        <label>异常分类：</label>
        <form:select path="subType" cssClass="input-small" cssStyle="width:180px;">
            <form:option value="0" label="所有"/>
            <form:options items="${formSubType}" itemLabel="label" itemValue="value" htmlEscape="false"/>
        </form:select>&nbsp;
        <label>所属网点：</label>
        <sd:servicePointSelect id="servicepoint" name="servicepointId" value="${abnormalForm.servicepointId}"
                             labelName="servicepointName" labelValue="${abnormalForm.servicepointName}"
                             width="1200" height="780" title="选择服务网点" areaId=""
                             showArea="false" allowClear="true" callbackmethod="" />
    </div>
    <div class="control-group">
        <label>反馈日期：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:95px;"
               maxlength="20" class="input-small Wdate" value="${abnormalForm.beginDate}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>&nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:95px" maxlength="20"
               class="input-small Wdate" value="${abnormalForm.endDate}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>&nbsp;&nbsp;
        <input id="btnClearSearch" class="btn btn-primary" type="button" value="清除条件"/>&nbsp;&nbsp;
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
            <th width="130">反馈时效</th>
            <th width="200">异常分类</th>
            <th width="300">异常原因</th>
            <th width="200">异常描述</th>
            <th width="200">所属网点</th>
            <th width="100">反馈时间</th>
            <th width="80">用户姓名</th>
            <th width="100">用户电话</th>
            <th width="300">用户地址</th>
            <shiro:hasPermission name="sd:abnormal:close">
                <th width="100">操作</th>
            </shiro:hasPermission>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowcnt" value="${page.list.size()}"/>
        <c:forEach items="${page.list}" var="model">
            <c:set var="rowNumber" value="${rowNumber+1}"/>
            <tr>
                <td>${rowNumber}</td>
                <td>
                    <a href="javascript:void(0);" style="" onclick="Order.showKefuOrderDetail('${model.orderId}','${model.quarter}',1);">
                       ${model.orderNo}
                    </a>
                </td>
                <c:choose>
                    <c:when test="${model.cutOffTimeliness>=0}">
                        <c:choose>
                            <c:when test="${model.cutOffTimeliness>0.50}">
                                <td>${model.feedBackTimeliness}</td>
                            </c:when>
                            <c:otherwise>
                                <td><p style="color: #D46B08;margin: auto">${model.feedBackTimeliness}</p></td>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <td><p style="color: red;margin: auto">${model.feedBackTimeliness}</p></td>
                    </c:otherwise>
                </c:choose>
                <td>${model.subTypeName}</td>
                <td>${model.reason}</td>
                <td>${model.description}</td>
                <td>${model.servicePoint.name}</td>
                <td>${model.strCreateDate}</td>
                <%--<td>${model.feedBackTimeliness}小时</td>--%>
                <td>${model.userName}</td>
                <td>${model.userPhone}</td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.userAddress}">
                            ${fns:abbr(model.userAddress,30)}
                    </a>
                </td>
                <shiro:hasPermission name="sd:abnormal:close">
                    <td>
                        <input id="abnormalClose" class="btn btn-warning" type="button" onclick="Order.addPending('${model.orderId}','${model.quarter}');" value="解除异常"/>
                    </td>
                </shiro:hasPermission>
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
</body>
</html>
