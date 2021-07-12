<%@ taglib prefix="shifo" uri="http://shiro.apache.org/tags" %>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>催单列表-已回复</title>
    <meta name="description" content="已回复">
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        #divNoRecord p {margin:10px 0 10px;}
        .reminder_normal {margin: auto;}
        .reminder_timeout {color:#ff0000;margin: auto;}
    </style>
    <script type="text/javascript">
        top.layer.closeAll();
        Order.rootUrl = "${ctx}";

        $(document).on("click", "#btnClearSearch", function () {
            $("#searchForm")[0].reset();
            $("#reminderNo").val("");
            $("#orderNo").val("");
            var dateStr = DateFormat.format(new Date(), 'yyyy-MM-dd');
            $("#endDate").val(dateStr);
            $("#beginDate").val(DateFormat.format(DateFormat.addMonthStr(dateStr, -3), 'yyyy-MM-01'));
            $("#customerId").val("0");
            $("#customerName").val("");
            $("#areaId").val("0");
            $("#areaLevel").val("0");
            $("#areaName").val("");
            $("#userName").val("");
            $("#userPhone").val("");
            $("#productCategoryId").val("0");
            $("#s2id_productCategoryId").find("span.select2-chosen").html('所有');
            search();
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
    <shiro:hasPermission name="sd:reminder:view">
        <li><a href="${ctx}/sd/reminder/list/process" title="待客服回复列表">待回复</a></li>
        <li class="active"><a href="javascript:void(0);" title="客服已回复列表">已回复</a></li>
        <li><a href="${ctx}/sd/reminder/list/haveConfirmedList" title="跟单已处理列表">已处理</a></li>
        <li><a href="${ctx}/sd/reminder/list/complete" title="工单完成催单列表">完成</a></li>
        <li><a href="${ctx}/sd/reminder/list/all" title="所有催单列表">所有</a></li>
    </shiro:hasPermission>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<div style="margin-top: 12px; padding-top:6px;padding-bottom:6px;background-color: rgb(230, 247, 255);border: 1px solid #91D5FF;border-radius: 4px;color: #0096DA">
    &nbsp;&nbsp;已回复:已回复客户催单信息
</div>
<form:form id="searchForm" modelAttribute="reminder" action="${ctx}/sd/reminder/list/haveRepliedList" method="post" class="breadcrumb form-search" cssStyle="margin-top: 12px">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div>
        <label>区&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;域：</label>
        <%--
        <sys:treeSelectAnyArea id="area" name="areaId" value="${reminder.areaId}" levelValue="${reminder.areaLevel}"
                               labelName="areaName" labelValue="${reminder.areaName }" title="区域" clearIdValue="0"
                               url="/sys/area/treeData?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1" nameLevel="4"/>
                               --%>

        <sys:treeselectareanew id="area" name="areaId" value="${reminder.areaId}" levelValue="${reminder.areaLevel}"
                               labelName="areaName" labelValue="${reminder.areaName}" title="区域" clearIdValue="0"
                               url="/sys/area/treeDataNew?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1" nameLevel="4"/>
        <label>催单单号：</label>
        <input type=text class="input-small" id="reminderNo" name="reminderNo" value="${reminder.reminderNo }" maxlength="12" />
        <label>工单单号：</label>
        <input type=text class="input-small" id="orderNo" name="orderNo" value="${reminder.orderNo }" maxlength="14" />
        <label>用户姓名：</label>
        <input type=text class="input-mini" id="userName" name="userName" value="${reminder.userName}" maxlength="20" />
        <label>用户电话：</label>
        <input type=text class="input-small digits" id="userPhone" name="userPhone" value="${reminder.userPhone}" placeholder="用户电话" maxlength="13" />
        <label>客户：</label>
        <sys:treeselect id="customer" name="customerId" value="${reminder.customerId}" labelName="customerName"
                        labelValue="${reminder.customerName}" cssStyle="width:211px;"
                        title="客户" url="/md/customer/treeData" clearIdValue="0"
                        cssClass="input-small" allowClear="true"/>
    </div>
    <div style="margin-top: 8px">
        <label class="label-search">创建时间：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:94px;"
               maxlength="20" class="input-small Wdate" value="${reminder.beginDate}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>&nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:94px" maxlength="20"
               class="input-small Wdate" value="${reminder.endDate}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
       <%-- &nbsp;&nbsp;--%>
        <%--<label>区域</label>
        <sys:treeSelectAnyArea id="area" name="areaId" value="${reminder.areaId}" levelValue="${reminder.areaLevel}"
                               labelName="areaName" labelValue="${reminder.areaName }" title="区域" clearIdValue="0"
                               url="/sys/area/treeData?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1" nameLevel="4"/>--%>
        <label>产品品类：</label>
        <form:select path="productCategoryId" cssClass="input-small" cssStyle="width:135px;">
            <form:option value="0" label="所有"/>
            <form:options items="${categories}" itemLabel="name" itemValue="id" htmlEscape="false"/>
        </form:select>
        <label>催单时效：</label>
        <form:select path="timeLiness" class="input-small" style="width:135px;">
            <form:option value="0" label="所有"/>
            <form:option value="1" label="小于1小时" />
            <form:option value="2" label="大于1小时" />
        </form:select>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>&nbsp;&nbsp;
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
<div id="divGrid">
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover" style="table-layout:fixed;">
        <thead>
        <tr>
            <th width="40">序号</th>
            <th width="130">工单单号</th>
            <th width="100">催单单号</th>
            <th width="100">催单类型</th>
            <th width="80">催单来源</th>
            <th width="90">催单次数</th>
            <th width="80">产品品类</th>
            <!-- user -->
            <th width="80">用户姓名</th>
            <th width="85">用户电话</th>
            <th width="200">用户地址</th>
            <th width="150">客户</th>
            <!-- apply -->
            <th width="80">催单人</th>
            <th width="100">催单时间</th>
            <th width="300">备注</th>
            <!-- reply -->
            <th width="80">回复人</th>
            <th width="100">回复时间</th>
            <th width="300">回复内容</th>
            <th width="80">催单时效</th>
            <th width="80">完工时效
                <a href="javascript:void(0);" style="text-decoration:none" data-toggle="tooltip" data-tooltip="首次催单到完工的时效">
                    <img src="${ctxStatic}/images/rpt/interrogation.png">
                </a>
            </th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowcnt" value="${page.list.size()}"/>
        <c:forEach items="${page.list}" var="model">
            <c:set var="rowNumber" value="${rowNumber+1}"/>
            <tr>
                <td>${rowNumber}</td>
                <td>${model.orderNo}</td>
                <td>
                    <shiro:hasPermission name="sd:reminder:complete">
                        <a href="javascript:void(0);" onclick="Order.reminderToConfirm('${model.id}','${model.quarter}','${model.itemId}');">${model.reminderNo}</a>
                    </shiro:hasPermission>
                    <shiro:lacksPermission name="sd:reminder:complete">
                        <a href="javascript:void(0);" onclick="Order.reminderView('${model.id}','','${model.quarter}');">${model.reminderNo}</a>
                    </shiro:lacksPermission>
                </td>
                <td>${model.reminderReason.value}</td>
                <td>${model.reminderTypeName}</td>
                <td>第${model.itemNo}次催单</td>
                <td>${model.productCategoryName}</td>
                <td>${model.userName}</td>
                <td>${model.userPhone}</td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.userAddress}">
                            ${fns:abbr(model.userAddress,30)}
                    </a>
                </td>
                <td>${model.customer.name}</td>
                <td>${model.createName}</td>
                <td>${model.createDate}</td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.createRemark}">
                            ${fns:abbr(model.createRemark,100)}
                    </a>
                </td>
                <td>${model.processName}</td>
                <td>${model.processDate}</td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.processRemark}">
                            ${fns:abbr(model.processRemark,100)}
                    </a>
                </td>
                <td>
                    <c:set var="cutoffClass" value="normal"/>
                    <c:choose>
                        <c:when test="${model.processTimeLiness>1}">
                            <c:set var="cutoffClass" value="timeout"/>
                        </c:when>
                    </c:choose>
                    <p class="reminder_${cutoffClass}">用时:${model.cutOffLabel}</p>
                </td>
                <td>
                    <c:set var="cutoffClass" value="normal"/>
                    <c:choose>
                        <c:when test="${model.orderCompleteTimeliness<0}">
                            <c:set var="cutoffClass" value="timeout"/>
                        </c:when>
                        <c:when test="${model.orderCompleteTimeliness<1}">
                            <c:set var="cutoffClass" value="intime"/>
                        </c:when>
                    </c:choose>
                    <p class="reminder_${cutoffClass}">${model.completeTimelinessLabel}</p>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<c:if test="${rowcnt > 0}">
    <div id="pagination" class="pagination">${page}</div>
</c:if>
<script type="text/javascript" language="javascript">
   /* $(document).ready(function () {
        $('a[data-toggle=tooltip]').darkTooltip();
        $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
        $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
    });*/
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
        <c:if test="${reminder != null && reminder.area != null && reminder.area.parent != null && reminder.area.parent.id != null}">
        $("#areaParentId").val("${reminder.area.parent.id}");
        </c:if>

        if($("#contentTable tbody>tr").length>0) {
            $("#divNoRecord").hide();
            //无数据报错
            var screen = getHtmlWidthAndHeight();
            $("#divGrid").height(screen.height-305);
            $("#contentTable").toSuperTable({
                width: screen.width-35,
                height: screen.height - 320,
                fixedCols: 3,
                headerRows: 1,
                colWidths:
                    [   40,130,120,100,60,80,130,
                        90,100,200,150,
                        90,100,160,
                        90,100,300,100,100
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
        $('a[data-toggle=tooltip]').darkTooltip();
        $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
        $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
    });
</script>
</body>
</html>
