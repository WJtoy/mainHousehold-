<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>订单处理-天猫一键求助列表</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <!-- image viewer -->
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            /*background: transparent;*/
        }
    </style>
    <script type="text/javascript">
        top.layer.closeAll();
        Order.rootUrl = "${ctx}";
        //覆盖分页前方法
        function beforePage(){
            var $btnSubmit  = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            $("#btnClearSearch").attr('disabled', 'disabled');
            layerLoading("查询中...",true);
        }
        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function(){
            if (clicktag == 0) {
                clicktag = 1;
                beforePage();
                setPage();
                this.form.submit();
            }
        });
        $(document).on("click", "#btnClearSearch", function () {
            $("#searchForm")[0].reset();
            var dateStr = DateFormat.format(new Date(), 'yyyy-MM-dd');
            $("#submitEndDate").val(dateStr);
            $("#submitStartDate").val(DateFormat.format(DateFormat.addMonthStr(dateStr, -1), 'yyyy-MM-01'));
            $("[id='status.value']").val("");
            $("#orderNo").val("");
            $("[id='status.value']").val("0");
            $("[id='status.value']").val("");
            search();
        });

        function reply(id,anomalyId,quarter){
            var tmallReply = top.layer.open({
                type: 2,
                id:'layer_tmallReply',
                zIndex:19891015,
                title:'天猫求助反馈',
                content: "${ctx}/sd/order/anomaly/reply?id="+ id + "&anomalyId=" + anomalyId + "&quarter=" + (quarter || '') + "&refreshType=refreshList",
                shade: 0.3,
                area: ['650px', '300px'],
                maxmin: false,
                success: function(layero,index){
                    //setCookie('layer.parent.id',index);
                },
                end:function(){
                }
            });
        }
    </script>

</head>

<body>
<ul id="navtabs" class="nav nav-tabs">
    <li><a href="${ctx}/sd/order/kefu/processlist" title="处理中工单列表">处理中</a></li>
    <li class="active"><a href="javascript:void(0);" title="天猫一键求助">求助</a></li>
    <li><a href="${ctx}/sd/order/serviceMonitor/list" title="天猫预警">预警</a></li>
    <li><a href="${ctx}/sd/order/kefu/pendinglist" title="需要等待的工单">停滞</a></li>
    <li><a href="${ctx}/sd/order/kefu/appointedlist" title="未预约的工单">未预约</a></li>
    <li><a href="${ctx}/sd/order/kefu/rushinglist" title="突击中的工单">突击单</a></li>
    <li><a href="${ctx}/sd/order/kefu/complainlist" title="投诉的工单">投诉</a></li>
    <li><a href="${ctx}/sd/order/kefu/finishlist" title="已完成工单列表">已完成</a></li>
    <li ><a href="${ctx}/sd/order/kefu/cancellist" title="取消的工单，未接单">取消单</a></li>
    <li ><a href="${ctx}/sd/order/kefu/returnlist" title="退单">退单</a></li>
    <li><a href="${ctx}/sd/order/kefu/alllist" title="所有工单">所有</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="searchEntity" method="post" class="form-inline">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="repageFlag" name="repageFlag" type="hidden" value="false" />
    <div class="control-group">
        <label class="label-search">订单号：</label>&nbsp;
        <form:input path="orderNo" cssClass="input-small" maxlength="20" />
        <label>问题分类：</label>
        <c:set var="typeList" value="${fns:getDictListFromMS('AnomalyQuestionType')}"/>
        <form:select path="questionType" cssClass="input-small" cssStyle="width:225px;">
            <form:option value="" label="所有"/>
            <form:options items="${typeList}" itemLabel="label" itemValue="value" htmlEscape="false"/>
        </form:select>
        <label>状态：</label>
        <form:select path="status" cssClass="input-small" cssStyle="width:125px;">
            <form:option value="-1" label="所有"/>
            <form:option value="0" label="未反馈"/>
            <form:option value="1" label="进行中"/>
            <form:option value="2" label="已关闭"/>
        </form:select>
        &nbsp;&nbsp;
        <label>反馈日期：</label>
        <input id="submitStartDate" name="submitStartDate" type="text" readonly="readonly" style="width:95px;margin-left:4px"
               maxlength="20" class="input-small Wdate" value="${fns:formatDate(searchEntity.submitStartDate,'yyyy-MM-dd')}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
        <label>~</label>&nbsp;&nbsp;&nbsp;
        <input id="submitEndDate" name="submitEndDate" type="text" readonly="readonly" style="width:95px" maxlength="20"
               class="input-small Wdate" value="${fns:formatDate(searchEntity.submitEndDate,'yyyy-MM-dd')}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>&nbsp;&nbsp;
        <input id="btnClearSearch" class="btn btn-primary" type="button" value="清除条件"/>&nbsp;&nbsp;
    </div>
</form:form>
<sys:message content="${message}"/>
<c:set var="rowNumber" value="0"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="40">序号</th>
        <th width="145">订单号</th>
        <th width="120">问题分类</th>
        <th width="60">状态</th>
        <th width="130">提交时间</th>
        <th >图片</th>
        <th width="80">反馈人</th>
        <th width="80">反馈时间</th>
        <th width="80">反馈内容</th>
        <th width="60">操作</th>
    </tr>
    </thead>
    <tbody>
    <c:set var="rowcnt" value="${page.list.size()}"/>
    <c:forEach items="${page.list}" var="order" varStatus="i" begin="0">
        <tr id="${order.id}">
            <td>${i.index+1}</td>
            <td>
                <a href="javascript:void(0);" onclick="Order.showOrderDetail('${order.orderId}','${order.quarter}','true');">
                            <abbr title="点击查看订单详情">${order.orderNo}</abbr>
                </a>
            </td>
            <td >${order.questionType.label}</td>
            <td class="status">
                <c:choose>
                    <c:when test="${order.status == 0}"><span class="label status_70">未反馈</span></c:when>
                    <c:when test="${order.status == 1}"><span class="label status_40">进行中</span></c:when>
                    <c:when test="${order.status == 2}"><span class="label status_80">已关闭</span></c:when>
                    <c:otherwise></c:otherwise>
                </c:choose>
            </td>
            <td><fmt:formatDate value="${order.submitDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            <!-- image list -->
            <td>
                <c:forEach items="${order.recourseList}" var="img" >
                <div class="row-fluid">
                    <div class="span12">
                        <div class="control-group">
                            <div class="controls">
                                <div  class="upload_warp_img_div">
                                    <c:forEach items="${img.imageUrls}" var="item" >
                                    <a href="javascript:;"><img src="${item}" data-original="${item}" ></a>
                                    </c:forEach>
                                </div>
                            </div>
                            <label class="control-label">${img.recourseText}(<fmt:formatDate value="${img.submitTime}" pattern="yyyy-MM-dd HH:mm:ss"/>)</label>
                        </div>
                    </div>
                </div>
                </c:forEach>
            </td>
            <td class="replierName">${order.replierName}</td>
            <td class="relpyDate"><fmt:formatDate value="${order.replyDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            <td class="replyContent">
                <c:if test="${!empty order.replyContent }">
                <a href="javascript:void(0);" data-toggle="tooltipeast"
                   data-tooltip="${order.replyContent}">${fns:abbr(order.replyContent,30)}</a>
                </c:if>
            </td>
            <td >
                <c:if test="${order.status == 0}">
                    <shiro:hasPermission name="sd:order:anomaly">
                    <a href="javascript:void(0);" title="点击反馈给天猫" class="btn btn-mini btn-warning replyBtn" onclick="Order.anomalyReply('${order.id}','${order.anomalyRecourseId}','${order.quarter}','refreshList');">反馈</a>
                    </shiro:hasPermission>
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
        imageViewer();
    });
    function imageViewer(){
        $("div.upload_warp_img_div").each(function(){
            $(this).viewer();//{url: "data-original"}
        });
    }
</script>
<style type="text/css">
    .dropdown-menu {min-width: 80px;}
    .dropdown-menu > li > a {text-align: left;padding: 3px 10px;}
    .pagination {margin: 4px 0 0 4px;}
    .label-search {width: 70px;  text-align: right;}
    form {margin: 0 0 5px;}
    .form-horizontal .control-label {width: 100px;}
    .form-horizontal .controls {margin-left: 120px;}
    .upload_warp_img_div img{width:80px;height:80px;vertical-align:middle}
    .upload_warp_left img{margin-top:0px;width:100%;height:100%}
    /*td div.row-fluid:not(:last-child) {border-bottom: 1px dashed #eee;margin-bottom: 5px;}*/
</style>
</body>
</html>
