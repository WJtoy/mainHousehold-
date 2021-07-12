<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>工单投诉单申诉列表</title>
    <meta name="description" content="申诉">
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <%@ include file="/WEB-INF/views/include/WdateLimitPicker.jsp" %>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <script type="text/javascript">
        top.layer.closeAll();
        Order.rootUrl = "${ctx}";

        $(document).on("click", "#btnClearSearch", function () {
            $("#searchForm")[0].reset();
            $("#complainNo").val("");
            $("#orderNo").val("");
            var startDate = moment().subtract(3,'M').format("YYYY-MM-01");
            var endDate = moment().format("YYYY-MM-DD");
            $("#beginDate").val(startDate);
            $("#endDate").val(endDate);
            $("#completeBeginDate").val("");
            $("#completeEndDate").val("");
            $("#status").val("");
            $("#s2id_status").find("span.select2-chosen").html('所有');
            $("#customerId").val("");
            $("#customerName").val("");
            $("#servicePointId").val("");
            $("#servicePointName").val("");
            $("#userName").val("");
            $("#userPhone").val("");
            $("#complainBy").val("");
            $("#complainObject").val("");
            $("#s2id_complainObject").find("span.select2-chosen").html('所有');
            $("#complainItem").val("");
            $("#s2id_complainItem").find("span.select2-chosen").html('所有');
            $("#judgeObject").val("");
            $("#s2id_judgeObject").find("span.select2-chosen").html('所有');
            $("#judgeItem").val("");
            $("#s2id_judgeItem").find("span.select2-chosen").html('所有');
            $("#areaId").val("");
            $("#areaLevel").val("");
            $("#areaName").val("");
            $("#kefuName").val("");
            $("#productCategoryId").val("0");
            search();
        });
        $(document).ready(function() {
            //责任项目变更
            $("#judgeObject").change(function () {
                var judgeObjectVal = $(this).val();
                if(judgeObjectVal === ""){//all
                    $("#judgeItem option").each(function(i){
                        $(this).removeAttr("disabled");
                    });
                }else{
                    $("#judgeItem option").each(function(i){
                        $(this).attr("disabled", "");
                        if($(this).val() === ""){
                            $(this).removeAttr("disabled");
                        }
                    });
                    $("#judgeItem").find("#optgroup_"+judgeObjectVal+" option").each(function(){
                        $(this).removeAttr("disabled");
                    })
                }
            });
        });
    </script>
</head>

<body>
<ul id="navtabs" class="nav nav-tabs">
    <shiro:hasPermission name="sd:complain:judge">
        <li><a href="${ctx}/sd/complain/judgelist" title="待处理投诉单列表">待处理</a></li>
        <li><a href="${ctx}/sd/complain/dealinglist" title="处理中投诉单列表">处理中</a></li>
        <li><a href="${ctx}/sd/complain/appointlist" title="待跟进投诉单列表">待跟进</a></li>
    </shiro:hasPermission>
    <shiro:hasPermission name="sd:complain:complete">
        <li><a href="${ctx}/sd/complain/completelist" title="待完成投诉单列表">待完成</a></li>
        <li><a href="${ctx}/sd/complain/alreadyCompleteList" title="已完成投诉单列表">已完成</a></li>
        <li class="active"><a href="javascript:void(0);" title="申诉投诉单列表">申诉</a></li>
    </shiro:hasPermission>
    <li><a href="${ctx}/sd/complain/alllist" title="所有投诉单列表">所有</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="complain" action="${ctx}/sd/complain/appeallist" method="post" class="form-inline">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
        注:当用投诉单号,订单号和电话进行搜索查询时，不受当前时间、项目等其他条件的限制。
    </div>
    <div class="control-group">
        <label class="label-search">投诉单号：</label>&nbsp;
        <input type=text class="input-small" id="complainNo" name="complainNo" value="${complain.complainNo }" maxlength="20" />
        <label class="label-search">订单号：</label>&nbsp;
        <input type=text class="input-small" id="orderNo" name="orderNo" value="${complain.orderNo }" maxlength="20" />
        <label>投诉人：</label>
        <input type=text class="input-mini" id="complainBy" name="complainBy" value="${complain.complainBy}" placeholder="投诉人" maxlength="20" /> &nbsp;&nbsp;
        <label>客服：</label>
        <input type=text class="input-mini" id="kefuName" name="kefu.name" value="${complain.kefu.name}" placeholder="客服姓名" maxlength="20" />&nbsp;
        &nbsp;&nbsp;
        <label>用户：</label>
        <input type=text class="input-mini" id="userName" name="userName" value="${complain.userName}" maxlength="20" />
        <label>电话：</label>
        <input type=text class="input-small digits" id="userPhone" name="userPhone" value="${complain.userPhone}" placeholder="用户电话" maxlength="20" />
        <label>品类：</label>
        <form:select path="productCategoryId" cssClass="input-small" cssStyle="width:125px;">
            <form:option value="0" label="所有"/>
            <form:options items="${categories}" itemLabel="name" itemValue="id" htmlEscape="false"/>
        </form:select>
    </div>
    <div class="control-group">
        <label class="label-search">投诉日期：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:95px;"
               maxlength="20" class="input-small Wdate" value="${fns:formatDate(complain.beginDate,'yyyy-MM-dd')}" />
        <label>~</label>&nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:95px" maxlength="10"
               class="input-small Wdate" value="${fns:formatDate(complain.endDate,'yyyy-MM-dd')}" />
        <label class="label-search">结案日期：</label>
        <input id="completeBeginDate" name="completeBeginDate" type="text" readonly="readonly" style="width:95px;"
               maxlength="20" class="input-small Wdate" value="${fns:formatDate(complain.completeBeginDate,'yyyy-MM-dd')}" />
        <label>~</label>&nbsp;&nbsp;&nbsp;
        <input id="completeEndDate" name="completeEndDate" type="text" readonly="readonly" style="width:95px" maxlength="10"
               class="input-small Wdate" value="${fns:formatDate(complain.completeEndDate,'yyyy-MM-dd')}" />
        <label class="label-search">投诉对象：</label>
        <c:set var="complainObjectDicts" value="${fns:getDictListFromMS('complain_object')}"/><%--切换为微服务--%>
        <select id="complainObject" name="complainObject" class="input-small" style="width:125px;">
            <option value="">所有</option>
            <c:forEach items="${complainObjectDicts}" var="dict">
                <c:choose>
                    <c:when test="${dict.value eq complain.complainObject.value }">
                        <option value="${dict.value}" selected="selected">${dict.label}</option>
                    </c:when>
                    <c:otherwise>
                        <option value="${dict.value}">${dict.label}</option>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </select>
        <label class="label-search">投诉项目：</label>
        <c:set var="complainItemList" value="${fns:getDictListFromMS('complain_item')}"/><%--切换为微服务--%>
        <select id="complainItem" name="complainItem" class="input-small" style="width:125px;">
            <option value="">所有</option>
            <c:forEach items="${complainItemList}" var="dict">
                <c:choose>
                    <c:when test="${dict.value eq complain.complainItem.value }">
                        <option value="${dict.value}" selected="selected">${dict.label}</option>
                    </c:when>
                    <c:otherwise>
                        <option value="${dict.value}">${dict.label}</option>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </select>
        <label class="label-search">责任对象：</label>
        <c:set var="judgeObjectList" value="${fns:getDictListFromMS('judge_object')}"/><%--切换为微服务--%>
        <select id="judgeObject" name="judgeObject" class="input-small" style="width:125px;">
            <option value="">所有</option>
            <c:forEach items="${judgeObjectList}" var="judgeObject">
                <c:choose>
                    <c:when test="${judgeObject.value eq complain.judgeObject.value }">
                        <option value="${judgeObject.value}" selected="selected">${judgeObject.label}</option>
                    </c:when>
                    <c:otherwise>
                        <option value="${judgeObject.value}">${judgeObject.label}</option>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </select>
        <label class="label-search">责任项目：</label>
        <select id="judgeItem" name="judgeItem" class="input-small" style="width:165px;">
            <option value="">所有</option>
            <c:forEach items="${judgeObjectList}" var="judgeObject">
                <c:set var="dictType" value="judge_item_${judgeObject.value}" />
                <c:set var="judgeItems" value="${fns:getDictListFromMS(dictType)}" />
                <optgroup label="${judgeObject.label}" id="optgroup_${judgeObject.value}">
                    <c:set var="disabled" value="" />
                    <c:if test="${complain.judgeObject != null and !empty complain.judgeObject.value and complain.judgeObject.value != '' and complain.judgeObject.value != judgeObject.value}">
                        <c:set var="disabled" value="disabled" />
                    </c:if>
                    <c:forEach items="${judgeItems}" var="dict">
                        <c:choose>
                            <c:when test="${dict.value eq complain.judgeItem.value }">
                                <option value="${dict.value}" selected="selected" ${disabled}>${dict.label}</option>
                            </c:when>
                            <c:otherwise>
                                <option value="${dict.value}" ${disabled}>${dict.label}</option>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </optgroup>
            </c:forEach>
        </select>&nbsp;
    </div>
    <div class="control-group">
        <label class="label-search">客户：</label>
        <sys:treeselect id="customer" name="customer.id" value="${complain.customer.id}" labelName="customer.name"
                        labelValue="${complain.customer.name}" cssStyle="width:211px;"
                        title="客户" url="/md/customer/treeData?kefu=${currentuser.id}"
                        cssClass="input-small" allowClear="true"/>

        <label class="label-search">服务网点：</label>
        <sd:servicePointSelect id="servicePoint" name="servicePoint.id" value="${complain.servicePoint.id}" labelName="servicePoint.name" labelValue="${complain.servicePoint.name}"
                             width="1200" height="780" title="选择服务网点" areaId=""
                             showArea="false" allowClear="true" callbackmethod="" />
        &nbsp;&nbsp;
        <label>区域</label>
        <sys:treeselectareanew id="area" name="area.id" value="${complain.area.id}" levelValue="${complain.areaLevel}"
               labelName="area.name" labelValue="${complain.area.name}" title="区域" clearIdValue="0"
               url="/sys/area/treeDataNew?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1" nameLevel="3"/>&nbsp;
        &nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>&nbsp;&nbsp;
        <input id="btnClearSearch" class="btn btn-primary" type="button" value="清除条件"/>&nbsp;&nbsp;
    </div>
</form:form>
<sys:message content="${message}"/>
<c:set var="rowNumber" value="0"/>
<div id="divGrid">
    <table id="contentTable" class="table table-bordered table-condensed " style="table-layout:fixed;">
        <thead>
        <tr>
            <th width="40">序号</th>
            <th width="100">单号</th>
            <th width="120">工单号</th>
            <th width="100">品类</th>

            <th width="60">客服</th>
            <th width="70">投诉方</th>
            <th width="60">状态</th>
            <th width="120">厂商</th>
            <th width="200">地址</th>

            <th width="80">投诉日期</th>
            <th width="100">投诉对象</th>
            <th width="150">投诉项目</th>
            <th width="200">投诉描述</th>
            <th width="50">判定人</th>
            <th width="80">判定日期</th>

            <th width="60">用户</th>
            <th width="85">电话</th>
            <th width="60">投诉人</th>

            <th width="120">责任对象</th>
            <th width="350">判定项目</th>
            <th width="400">判定意见</th>

            <th width="50">结案人</th>
            <th width="80">结案日期</th>
            <th width="150">处理方案</th>
            <th width="200">处理意见</th>

            <th width="100">申诉人</th>
            <th width="100">申诉日期</th>
            <th width="200">申诉内容</th>

            <th width="50">操作</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowcnt" value="${page.list.size()}"/>
        <c:forEach items="${page.list}" var="model">
            <c:set var="rowNumber" value="${rowNumber+1}"/>
            <tr>
                <td>${rowNumber}</td>
                <td>
                    <c:choose>
                        <c:when test="${model.status.value eq '1'}">
                            <a href="javascript:void(0);" onclick="Order.complain_complete('${model.id}','${model.quarter}');">${model.complainNo}</a>
                        </c:when>
                        <c:when test="${model.status.value eq '3'}">
                            <a href="javascript:void(0);" onclick="Order.appeal_deal('${model.id}','${model.quarter}');">${model.complainNo}</a>
                        </c:when>
                        <c:otherwise>
                            <a href="javascript:void(0);"  onclick="Order.complain_view('${model.id}','${model.quarter}');">${model.complainNo}</a>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td>${model.orderNo}<br/>${model.orderStatus.label}</td>
                <td>${model.productCategoryName}</td>
                <td>${model.kefu.name}</td>
                <td>${model.complainType.label}</td>
                <td>
                    <c:set var="statusclass" value="success" />
                    <c:choose>
                        <c:when test="${model.status.value eq '0'}">
                            <c:set var="statusclass" value="warning" />
                        </c:when>
                        <c:when test="${model.status.value eq '1'}">
                            <c:set var="statusclass" value="info" />
                        </c:when>
                        <c:when test="${model.status.value eq '2'}">
                            <c:set var="statusclass" value="success" />
                        </c:when>
                    </c:choose>
                    <span class="label label-${statusclass}">
                            ${model.status.label}
                    </span>
                </td>

                <td>${model.customer.name}</td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.userAddress}">
                            ${fns:abbr(model.userAddress,26)}
                    </a>
                </td>
                <td><fmt:formatDate value="${model.complainDate}" pattern="yyyy-MM-dd"/></td>
                <td>${model.complainObjectLabels}</td>
                <td>${model.complainItemLabels}</td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.complainRemark}">
                            ${fns:abbr(model.complainRemark,60)}
                    </a>
                </td>
                <td>${model.judgeBy.name}</td>
                <td><fmt:formatDate value="${model.judgeDate}" pattern="yyyy-MM-dd"/></td>

                <td>${model.userName}</td>
                <td>${model.userPhone}</td>
                <td>${model.complainBy}</td>
                <td>${model.judgeObjectLabels}</td>
                <td>${model.judgeItemLabels}</td>
                <td>
                <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.judgeRemark}">
                        ${fns:abbr(model.judgeRemark,60)}
                </a>
                </td>
                <td>${model.completeBy.name}</td>
                <td><fmt:formatDate value="${model.completeDate}" pattern="yyyy-MM-dd"/></td>
                <td>${model.completeResultLabels}</td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.completeRemark}">
                            ${fns:abbr(model.completeRemark,60)}
                    </a>
                </td>

                <td>${model.appealBy.name}</td>
                <td><fmt:formatDate value="${model.appealDate}" pattern="yyyy-MM-dd"/></td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.appealRemark}">
                            ${fns:abbr(model.appealRemark,60)}
                    </a>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${model.status.value eq '1' and not empty model.judgeDate}">
                            <shiro:hasPermission name="sd:complain:complete">
                            <a href="javascript:void(0);" class="btn btn-mini btn-warning" onclick="Order.complain_complete('${model.id}','${model.quarter}');">结案</a>
                            </shiro:hasPermission>
                        </c:when>
                        <c:otherwise>
                            <a href="javascript:void(0);" class="btn btn-mini btn-warning" onclick="Order.complain_view('${model.id}','${model.quarter}');">查看</a>
                        </c:otherwise>
                    </c:choose>
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
            var screen = getHtmlWidthAndHeight();
            $("#divGrid").height(screen.height-305);
            $("#contentTable").toSuperTable({
                width: screen.width-20,
                height: screen.height - 315,
                fixedCols: 4,
                headerRows: 1,
               colWidths:
                   [   40,100,130,90,
                       90, 70, 60, 150,150,
                       90, 100, 100, 150,90, 90,
                       80, 100, 80,
                       120, 150,180,
                       100, 90,150,180,
                       90,90,180,
                       60
                ],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
        else {
            var h = document.body.clientHeight;
            $("#divGrid").height(h-305);
        }
    });
</script>
<script type="text/javascript">
    $(document).ready(function()
    {
        oneYearDatePicker('beginDate','endDate',false);
        oneYearDatePicker('completeBeginDate','completeEndDate',true);
        <c:if test="${complain != null && complain.status !=null && complain.status.value != null && complain.status.value != ''}">
        $("#status").select2().val("${complain.status.value}").trigger('change');
        </c:if>
    });
</script>
</body>
</html>
