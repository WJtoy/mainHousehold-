<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>待发货配件列表(客服)</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <%@ include file="/WEB-INF/views/include/WdateLimitPicker.jsp" %>
	<meta name="decorator" content="default" />
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/Material.js?_v=${OrderJsVersion}" type="text/javascript"></script>
<script type="text/javascript">
    top.layer.closeAll();
    Order.rootUrl = "${ctx}";
    Material.rootUrl = "${ctx}";
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
    $(document).on("click", "#btnExport", function () {
        $btnExport = $(this);
        var confirmClickTag = 0;
        layer.confirm('确定导出配件列表吗?', {icon: 3, title: '系统确认'}, function (index, layero) {
            if (confirmClickTag == 1) {
                return false;
            }
            var btn0 = $(".layui-layer-btn0", layero);
            if (btn0.hasClass("layui-btn-disabled")) {
                return false;
            }
            confirmClickTag = 1;

            var trs = $("#contentTable tbody").find("tr");
            if (trs.length == 0) {
                layer.close(index);//关闭本身
                layerInfo("查询无数据，无法导出！", "提示", true);
                return false;
            }

            btn0.addClass("layui-btn-disabled").attr("disabled", "disabled");
            layer.close(index);//关闭本身
            $btnExport.attr('disabled', 'disabled');
            var loadingIndex = layer.msg('正在导出，请稍等...', {
                icon: 16,
                time: 0,
                shade: 0.3
            });
            if ($("#exportForm").length > 0) {
                $("#exportForm").remove();
            }
            var html = Utils.formToHiddenInputHtml("searchForm");
            jQuery('<form id="exportForm" action="${ctx}/sd/kefuOrderMaterial/tosendlist/export" method="post" style="display:none;">'+ html + '</form>')
                .appendTo('body').submit().remove();
            setTimeout(function () {
                $btnExport.removeAttr('disabled');
                layer.close(loadingIndex);
            }, 2000);
        });
        return false;
    });

    // 同步申请单跟踪进度
    function updatePendingInfo(pending){
        Material.updatePendingInfo(pending);
    }
</script>
<style type="text/css">
	.table thead th,.table tbody td {
		text-align: center;
		vertical-align: middle;
		BackColor: Transparent;
	}
	.table tbody td.tdleft{text-align: left!important;padding-left: 15px;}
</style>
</head>
<body>
<ul id="navtabs" class="nav nav-tabs">
	<li ><a href="${ctx}/sd/kefuOrderMaterial/applylist" title="待审核配件单">待审核</a></li>
	<li class="active"><a href="javascript:void(0);" title="待发货配件单">待发货<span id="spn_count" class="badge badge-info">${page !=null ?page.count:''}</span></a></li>
	<li><a href="${ctx}/sd/kefuOrderMaterial/sendlist" title="已发货配件单">已发货</a></li>
	<li><a href="${ctx}/sd/kefuOrderMaterial/rejectlist" title="已驳回配件单">已驳回</a></li>
	<li><a href="${ctx}/sd/kefuOrderMaterial/closelist" title="已完成配件单">已完成</a></li>
    <li><a href="${ctx}/sd/kefuOrderMaterial/alllist" title="所有配件单">所有</a></li>
    <li><a href="${ctx}/sd/kefuOrderMaterial/waitSignMaterialReturnList" title="待签收(旧件)">待签收(旧件)</a></li>
</ul>
<form:form id="searchForm" modelAttribute="searchModel" method="post" action="${ctx}/sd/kefuOrderMaterial/tosendlist" class="breadcrumb form-search">
	<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
	<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
	<input id="repageFlag" name="repageFlag" type="hidden" value="false"/>
    <div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
        注:当用工单号和用户电话进行搜索查询时，不受当前时间、客户等其他条件的限制。
    </div>
	<div>
        <label class="label-search">客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
        <sys:treeselect id="customer" name="customer.id" value="${searchModel.customer.id}" labelName="customer.name"
                        labelValue="${searchModel.customer.name}" cssStyle="width:205px;"
                        title="客户" url="/md/customer/treeData?kefu=${currentuser.id}"
                        cssClass="input-small" allowClear="true"/>
		<label>工单单号：</label>
		<input type=text class="input-small" id="orderNo" name="orderNo" value="${searchModel.orderNo }" maxlength="20"/>
		<label>用户电话：</label>
		<input type=text class="input-small digits" id="userPhone" name="userPhone" value="${searchModel.userPhone}"
			   placeholder="用户电话 或 实际联络电话" maxlength="20"/>
		&nbsp;&nbsp;
		<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>&nbsp;&nbsp;
		<input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
	</div>
	<div style="margin-top:5px;">
        <label>申请日期：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:95px;"
               maxlength="20" class="input-small Wdate" value="${fns:formatDate(searchModel.beginDate,'yyyy-MM-dd')}" />
        <label>~</label>&nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:95px" maxlength="20"
               class="input-small Wdate" value="${fns:formatDate(searchModel.endDate,'yyyy-MM-dd')}" />
        <input type="hidden" id="materialType" name="materialType" value="1" />
        <label class="label-search">申请类型：</label>
        <form:select path="applyType" class="input-small" style="width:135px;">
            <form:option value="0" label="所有"/>
            <form:options items="${fns:getDictListFromMS('material_apply_type')}" itemLabel="label" itemValue="value"
                          htmlEscape="false"/>
        </form:select>
        <label class="label-search">跟踪状态：</label>
        <form:select path="pendingType" class="input-small" style="width:135px;">
            <form:option value="-1" label="所有"/>
            <form:option value="0" label=""/>
            <form:options items="${fns:getDictListFromMS('material_pending_type')}" itemLabel="label" itemValue="value"
                          htmlEscape="false"/>
        </form:select>
	</div>
</form:form>
<sys:message content="${message}"/>
<c:set var="rowcnt" value="${page.list.size()}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="60">序号</th>
        <th width="120">工单单号</th>
        <th width="120">客户</th>
        <%--<th width="60">状态</th>--%>
        <th width="60">申请类型</th>
        <th width="80">产品</th>
        <th width="80">用户姓名</th>
        <th width="80">用户电话</th>
        <%--<th width="100">区域</th>--%>
        <th width="200">用户地址</th>
        <th width="80">申请人</th>
        <th width="80">申请时间</th>
        <th width="80">审核人</th>
        <th width="80">审核时间</th>
        <th width="80">跟踪进度</th>
        <%--<th width="80">跟踪时间</th>
        <th width="80">跟踪内容</th>--%>
        <th width="80">配件照片</th>
        <th width="60">返件</th>
        <th width="60">配件单</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${page.list}" var="master" varStatus="i" begin="0">
        <c:set var="rowspan" value="${ master.productNameList.size() eq 0?1: master.productNameList.size()}"/>
        <tr id="tr_${master.id}" data-index="${i.index}">
            <td rowspan="${rowspan}">${i.index+1}</td>
            <td rowspan="${rowspan}"><a href="javascript:void(0);" onclick="Order.showKefuOrderDetail('${master.orderId}','${master.quarter}',1);" title="点击查看订单详情">${master.orderNo }</a></td>
            <td rowspan="${rowspan}">${master.customer.name}</td>
           <%-- <td rowspan="${rowspan}">${master.status.label}</td>--%>
            <td rowspan="${rowspan}">${master.applyType.label}</td>
            <td>${master.productNameList[0]}</td>
            <td rowspan="${rowspan}">${master.userName}</td>
            <td rowspan="${rowspan}">${master.userPhone}</td>
           <%-- <td rowspan="${rowspan}">${master.area.fullName} ${master.subArea.name}</td>--%>
            <td rowspan="${rowspan}">${master.area.fullName} ${master.subArea.name}${master.userAddress}</td>
            <td rowspan="${rowspan}">${master.createBy.name}</td>
            <td rowspan="${rowspan}"><fmt:formatDate value="${master.createDate}" pattern="yyyy-MM-dd HH:mm"/> </td>
            <td rowspan="${rowspan}">${master.updateBy.name}</td>
            <td rowspan="${rowspan}"><fmt:formatDate value="${master.updateDate}" pattern="yyyy-MM-dd HH:mm"/> </td>
            <!-- 跟踪进度 -->
            <td rowspan="${rowspan}"> <label id="pendingType">${master.pendingType.label}</label>
                <c:if test="${master.pendingType.value ne '0'}"><br></c:if>
                <button class="btn-mini btn-primary" href="javascript:void(0);" onclick="Material.pendingForm('${master.id}','${master.orderNo}','${master.quarter}');">更新</button>
            </td>
            <%--<td rowspan="${rowspan}"><label id="pendingDate"><fmt:formatDate value="${master.pendingDate}" pattern="yyyy-MM-dd HH:mm"/></label> </td>
            <td rowspan="${rowspan}"><a id="pendingContent" href="javascript:" data-toggle="tooltip"  data-tooltip=" ${master.pendingContent}">${fns:abbr(master.pendingContent,40)}</a>
                <button class="btn-mini btn-primary" type="button" onclick="Material.viewPendingLog('${master.id}','${master.orderNo}','${master.quarter}');">历史</button>
            </td>--%>
            <td rowspan="${rowspan}">
                <a href="javascript:void(0);" onclick="layerWindow('layer_material_photo','${ctx}/sd/material/materialMasterAttachmentForm?masterId=${master.id}&quarter=${master.quarter}&orderId=${master.orderId}','配件照片',1100,700)">查看</a>
            </td>
            <td rowspan="${rowspan}">
                <c:choose>
		    <c:when test="${master.returnFlag eq 1}">
                   <font style="color: red">是</font>
                    </c:when>
                    <c:otherwise>否</c:otherwise>
                </c:choose>
            </td>
            <td rowspan="${rowspan}">
                <a href="javascript:void(0);" onclick="Order.attachlist('${master.orderId}','${master.orderNo}','${master.quarter}');">查看</a>
            </td>
        </tr>
        <c:forEach items="${master.productNameList}" var="productName" varStatus="i" begin="1">
            <tr>
                <td>${productName}</td>
            </tr>
        </c:forEach>
    </c:forEach>
    </tbody>
</table>
<c:if test="${rowcnt > 0}">
	<div id="pagination" class="pagination">${page}</div>
</c:if>
<script type="text/javascript" language="javascript">
    $(document).ready(function () {
        oneYearDatePicker('beginDate','endDate',false);
        $('a[data-toggle=tooltip]').darkTooltip();
    });
</script>
</body>
</html>