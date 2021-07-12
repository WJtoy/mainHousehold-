<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>已发货配件列表(客服)</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<%@ include file="/WEB-INF/views/include/WdateLimitPicker.jsp" %>
	<meta name="decorator" content="default" />
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/Material.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<c:set var="currentuser" value="${fns:getUser() }"/>
<script type="text/javascript">
    top.layer.closeAll();
    Order.rootUrl = "${ctx}";
    Material.rootUrl = "${ctx}";

    function resetPage() {
		$("#btnSubmit").click();
    }
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
            jQuery('<form id="exportForm" action="${ctx}/sd/kefuOrderMaterial/sendlist/export" method="post" style="display:none;">'+ html + '</form>')
                .appendTo('body').submit().remove();
            setTimeout(function () {
                $btnExport.removeAttr('disabled');
                layer.close(loadingIndex);
            }, 2000);
        });
        return false;
    });

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
	<li ><a href="${ctx}/sd/material/customer/applylist" title="待审核配件单">待审核</a></li>
	<li><a href="${ctx}/sd/material/customer/tosendlist" title="待发货配件单">待发货</a></li>
	<li><a href="${ctx}/sd/material/customer/sendlist" title="已发货配件单">已发货</a></li>
	<li><a href="${ctx}/sd/material/customer/rejectlist" title="已驳回配件单">已驳回</a></li>
	<li><a href="${ctx}/sd/material/customer/closelist" title="已完成配件单">已完成</a></li>
	<li><a href="${ctx}/sd/material/customer/alllist" title="所有配件单">所有</a></li>
	<li class="active"><a href="javascript:void(0);" title="待签收(旧件)">待签收(旧件)<span id="spn_count" class="badge badge-info">${page !=null ?page.count:''}</span></a></li>
</ul>
<form:form id="searchForm" modelAttribute="searchModel" method="post" action="${ctx}/sd/material/customer/findCustomerWaitMaterialReturnList" class="breadcrumb form-search">
	<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
	<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
	<input id="repageFlag" name="repageFlag" type="hidden" value="false"/>
	<div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
		注:当用工单号和用户电话进行搜索查询时，不受当前时间、客户等其他条件的限制。
	</div>
	<div>
		<c:set var="isCustomer" value="${currentuser.isCustomer()}" />
		<c:choose>
			<c:when test="${isCustomer}">
				<form:hidden path="customer.id"/>
				<form:hidden path="customer.name"/>
			</c:when>
			<c:otherwise>
				<label>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
				<form:select path="customer.id" style="width:280px;">
					<form:option value="0" label="所有"/>
					<form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
				</form:select>
			</c:otherwise>
		</c:choose>
		<label>工单单号：</label>
		<input type=text class="input-small" id="orderNo" name="orderNo" value="${searchModel.orderNo }" maxlength="20"/>
		<label>用户电话：</label>
		<input type=text class="input-small digits" id="userPhone" name="userPhone" value="${searchModel.userPhone}"
			   maxlength="20"/>
		<c:if test="${isCustomer}">
			<label>申请时间：</label>
			<input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:106px;"
				   maxlength="20" class="input-small Wdate" value="${fns:formatDate(searchModel.beginDate,'yyyy-MM-dd')}" />
			<label>~</label>&nbsp;&nbsp;&nbsp;
			<input id="endDate" name="endDate" type="text" readonly="readonly" style="width:107px" maxlength="20"
				   class="input-small Wdate" value="${fns:formatDate(searchModel.endDate,'yyyy-MM-dd')}" />
		</c:if>
	</div>
	<div style="margin-top:5px;" id="div_search_customer">
		<div>
			<c:if test="${!isCustomer}">
				<label>申请时间：</label>
				<input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:106px;"
					   maxlength="20" class="input-small Wdate" value="${fns:formatDate(searchModel.beginDate,'yyyy-MM-dd')}" />
				<label>~</label>&nbsp;&nbsp;&nbsp;
				<input id="endDate" name="endDate" type="text" readonly="readonly" style="width:107px" maxlength="20"
					   class="input-small Wdate" value="${fns:formatDate(searchModel.endDate,'yyyy-MM-dd')}" />
			</c:if>
			<input type="hidden" id="materialType" name="materialType" value="1" />
			<label class="label-search">申请类型：</label>
			<form:select path="applyType" class="input-small" style="width:134px;">
				<form:option value="0" label="所有"/>
				<form:options items="${fns:getDictListFromMS('material_apply_type')}" itemLabel="label" itemValue="value"
							  htmlEscape="false"/>
			</form:select>
			<label>快递单号：</label>
			<input type=text class="input-small digits" id="expressNo" name="expressNo" value="${searchModel.expressNo}"
				   placeholder="快递单号" maxlength="50"/>&nbsp;
			<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>
		</div>
	</div>
</form:form>
<sys:message content="${message}"/>
<c:set var="rowcnt" value="${page.list.size()}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
	<thead>
	<tr>
		<th width="60">序号</th>
		<th width="120">工单单号</th>
		<th width="120">快递单号</th>
		<c:if test="${!isCustomer}">
			<th width="120">客户</th>
		</c:if>
		<th width="120">产品</th>
		<th width="60">申请类型</th>
		<th width="80">用户姓名</th>
		<th width="80">用户电话</th>
		<th width="200">用户地址</th>
		<th width="80">发货人</th>
		<th width="80">发货时间</th>
		<th width="60">操作</th>
	</tr>
	</thead>
	<tbody>
	<c:forEach items="${page.list}" var="master" varStatus="i" begin="0">
		<c:set var="rowspan" value="${master.productNameList.size() eq 0?1: master.productNameList.size()}"/>
		<tr id="tr_${master.id}" data-index="${i.index}">
			<td rowspan="${rowspan}">${i.index+1}</td>
			<td rowspan="${rowspan}"><a href="javascript:void(0);" onclick="Order.viewOrderDetail('${master.orderId}','${master.quarter}');" title="点击查看订单详情">${master.orderNo }</a></td>
			<td rowspan="${rowspan}">
				<a href="http://www.kuaidi100.com/chaxun?com=${master.expressCompany.value}&nu=${master.expressNo }" target="_blank" title="点击进入快递100">
						${master.expressNo}
				</a>
			</td>
			<c:if test="${!isCustomer}">
				<td rowspan="${rowspan}">${master.customer.name}</td>
			</c:if>
			<td>${master.productNameList[0]}</td>
			<td rowspan="${rowspan}">${master.applyType.label}</td>
			<td rowspan="${rowspan}">${master.userName}</td>
			<td rowspan="${rowspan}">${master.userPhone}</td>
			<td rowspan="${rowspan}">${master.area.fullName} ${master.subArea.name}${master.userAddress}</td>
			<td rowspan="${rowspan}">${master.updateBy.name}</td>
			<td rowspan="${rowspan}"><fmt:formatDate value="${master.updateDate}" pattern="yyyy-MM-dd HH:mm"/> </td>
			<td rowspan="${rowspan}">
				<a href="javascript:void(0);" onclick="Material.updateSign('${master.id}','${master.quarter}');">签收</a>
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