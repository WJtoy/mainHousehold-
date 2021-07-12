<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>异常单列表-已处理</title>
    <meta name="description" content="已处理">
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <script src="${ctxStatic}/sd/ServicePointOrderService.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        #divNoRecord p {margin:10px 0 10px;}
    </style>
    <script type="text/javascript">
        top.layer.closeAll();
        Order.rootUrl = "${ctx}";
        ServicePointOrderService.rootUrl = "${ctx}";

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

        $(document).on("click", "#btnClearSearch", function () {
            $("#searchForm")[0].reset();
            $("#orderNo").val("");
            $("#formType").val("0");
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
        <li><a href="${ctx}/servicePoint/sd/abnormalForm/praiseAbnormalList" title="好评驳回">好评驳回</a></li>
        <li><a href="${ctx}/servicePoint/sd/abnormalForm/waitProcessList" title="审单异常">审单异常</a></li>
        <li><a href="${ctx}/servicePoint/sd/abnormalForm/appAbnormalList" title="app异常">app异常</a></li>
        <li><a href="${ctx}/servicePoint/sd/abnormalForm/appCompleteAbnormalList" title="app完工异常">app完工异常</a></li>
        <li class="active"><a href="javascript:void(0);" title="异常单已处理">已处理</a></li>
    </shiro:hasPermission>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="abnormalForm" action="${ctx}/servicePoint/sd/abnormalForm/processedList" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div class="control-group">
        <label>工单单号：</label>
        <input type=text class="input-small" id="orderNo" name="orderNo" value="${abnormalForm.orderNo}" maxlength="14" style="width: 240px"/>&nbsp;
        <label>异常类型：</label>
        <form:select path="formType" cssClass="input-small" cssStyle="width:150px;">
            <form:option value="0" label="所有"/>
            <form:options items="${formTypeList}" itemLabel="msg" itemValue="code" htmlEscape="false"/>
        </form:select>&nbsp;
        <label>异常分类：</label>
        <form:select path="subType" cssClass="input-small" cssStyle="width:180px;">
            <form:option value="0" label="所有"/>
            <form:options items="${formSubType}" itemLabel="label" itemValue="value" htmlEscape="false"/>
        </form:select>
    </div>
    <div style="margin-top: 8px">
        <label>所属网点：</label>
        <sd:servicePointSelect id="servicepoint" name="servicepointId" value="${abnormalForm.servicepointId}"
                             labelName="servicepointName" labelValue="${abnormalForm.servicepointName}"
                             width="1200" height="780" title="选择服务网点" areaId=""
                             showArea="false" allowClear="true" callbackmethod="" /> &nbsp;
        <label style="margin-left: 0px">反馈日期：</label>
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
<div id="divGrid" style="overflow-x:auto;">
    <table id="contentTable" class="table table-bordered table-condensed " style="table-layout:fixed;">
        <thead>
        <tr>
            <th width="40">序号</th>
            <th width="130">工单单号</th>
            <th width="200">处理时效</th>
            <th width="80">异常类型</th>
            <th width="100">异常分类</th>
            <th width="300">异常原因</th>
            <th width="200">异常描述</th>
            <th width="200">所属网点</th>
            <th width="100">反馈时间</th>
            <th width="80">用户姓名</th>
            <th width="100">用户电话</th>
            <th width="300">用户地址</th>
            <th width="100">处理人</th>
            <th width="300">处理内容</th>
            <th width="200">处理时间</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowcnt" value="${page.list.size()}"/>
        <c:forEach items="${page.list}" var="model">
            <c:set var="rowNumber" value="${rowNumber+1}"/>
            <tr>
                <td>${rowNumber}</td>
                <td>
                    <a href="javascript:void(0);" style="" onclick="ServicePointOrderService.showProcessOrderDetail('${model.orderId}','${model.quarter}',1);">
                       ${model.orderNo}
                    </a>
                </td>
                <c:choose>
                    <c:when test="${model.timeLiness>2}">
                        <td><p style="color: red;margin: auto">用时:${model.feedBackTimeliness}</p></td>
                    </c:when>
                    <c:otherwise>
                        <td>用时:${model.feedBackTimeliness}</td>
                    </c:otherwise>
                </c:choose>
                <td>${model.fromTypeName}</td>
                <td>${model.subTypeName}</td>
                <td>${model.reason}</td>
                <td>${model.description}</td>
                <td>${model.servicePoint.name}</td>
                <td>${model.strCreateDate}</td>
                <td>${model.userName}</td>
                <td>${model.userPhone}</td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.userAddress}">
                            ${fns:abbr(model.userAddress,30)}
                    </a>
                </td>
                <td>${model.closeByName}</td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.closeComment}">
                            ${fns:abbr(model.closeComment,30)}
                    </a>
                </td>
                <td>${model.closeDate}</td>
                <%--<td>${model.timeLiness}小时</td>--%>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
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
    $(document).ready(function() {
        if($("#contentTable tbody>tr").length>0) {
            $("#divNoRecord").hide();
            //无数据报错
            var screen = getHtmlWidthAndHeight();
            $("#divGrid").height(screen.height-295);
            $("#contentTable").toSuperTable({
                width: screen.width-20,
                height: screen.height - 315,
                fixedCols: 3,
                headerRows: 1,
                colWidths:
                    [   40,130,120,150,200,200,
                        200,150,150,130,
                        220,300,100,150,150
                ],
                onStart: function () {},
                onFinish: function () {}
            });
        }
        else {
            var h = document.body.clientHeight;
            $("#divGrid").height(h-295);
            $("#divNoRecord").show();
        }
    });
</script>
</body>
</html>
