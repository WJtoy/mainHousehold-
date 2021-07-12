<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>跟踪进度</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/sd/ServicePointOrderService.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<!-- clipboard -->
	<script src="${ctxStatic}/common/clipboard.min.js" type="text/javascript"></script>
	<script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
		ServicePointOrderService.rootUrl = "${ctx}";
        var this_index = top.layer.index;

        $(document).ready(function() {
			//engineer
			var tmpl = $('#txengineertmsg').val();
			var doTtmpl = doT.template(tmpl);
			var data = {'servicePhone':'','serviceAddress':''};
			data.servicePhone = $("#lblservicePhone").val();
			data.serviceAddress = $("#lblserviceAddress").val();
			var html = doTtmpl(data);
			var engineerMsg = html.replace(/~n/g,'\n');
			$("#btn_engineer_copy").attr('data-clipboard-text',engineerMsg);
			var clip_engineer = new ClipboardJS('#btn_engineer_copy');
			clip_engineer.on('success', function(e) {
				layerMsg("安维短信复制成功");
			});
			clip_engineer.on('error', function(e) {
				layerError("安维短信复制失败： <br/>" + JSON.stringify(e.message));
			});
        });

        function reload()
		{
            return false;
		}


		function saveTacking(){
			ServicePointOrderService.saveServicePointTracking("closeAndReloadParent",this_index);
        }

        function closeme(){
            top.layer.close(this_index);
        }
        function repage()
        {
            return false;
        }

	</script>
	<style type="text/css">
		.tdlable {min-width: 100px;}
	</style>
</head>
<body style="margin: 0px 10px 3px 10px;">

<div class="accordion-group" style="margin-top:2px; table-layout: fixed;" >
	<div class="accordion-heading">
		<a href="#divheader" class="accordion-toggle" data-toggle="collapse">基本信息 <span class="arrow"></span></a>
	</div>
	<div id="divheader" class="accordion-body">
		<table class="table table-bordered table-striped" style="margin-bottom: 0px;">
			<tbody>
			<tr>
				<td class="tdlable"><label class="control-label">联系人:</label></td>
				<td class="tdbody">${order.orderCondition.userName}</td>
				<td class="tdlable"><label class="control-label">实际联系电话:</label></td>
				<td class="tdbody">${order.orderCondition.servicePhone}

				</td>
			</tr>
			<tr>
				<td class="tdlable"><label class="control-label">实际上门地址:</label></td>
				<td class="tdbody">${order.orderCondition.area.name}${order.orderCondition.serviceAddress}</td>
				<td class="tdlable"><label class="control-label">座机:</label></td>
				<td class="tdbody">${order.orderCondition.phone2}</td>
			</tr>
			<tr>
				<td class="tdlable"><label class="control-label">服务描述:</label></td>
				<td class="tdbody" colspan="3">${order.description}</td>
			</tr>
			<tr>
				<td class="tdlable"><label class="control-label">安维姓名:</label></td>
				<td class="tdbody">${order.orderCondition.engineer.name}</td>
				<td class="tdlable"><label class="control-label">安维手机/电话:</label></td>
				<td class="tdbody">${order.orderCondition.engineer.mobile}

				</td>
			</tr>
			</tbody>
		</table>
	</div>
</div>
<div class="control-group" style="table-layout: fixed;margin-top: 15px">
	<table id="productTable" class="table table-striped table-bordered table-condensed" style="margin-bottom: 0px;">
		<thead>
		<tr>
			<th width=30px>序号</th>
			<th>服务类型</th>
			<th>产品</th>
			<th>品牌</th>
			<th>型号/规格</th>
			<th>数量</th>
		</thead>
		<tbody>
		<c:forEach items="${order.items}" var="item" varStatus="i" begin="0">
			<tr>
				<td>${i.index+1}</td>
				<td>${item.serviceType.name }</td>
				<td>${item.product.name }</td>
				<td>${item.brand }</td>
				<td>${item.productSpec }</td>
				<td>${item.qty }</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
</div>
<c:if test="${showInsurance == true}">
	<div class="accordion-group" style="margin-top:15px; table-layout: fixed;" >
		<div class="accordion-heading">
			<a class="accordion-toggle" data-toggle="collapse">互助基金</a>
		</div>
		<div class="accordion-body">
			<table class="table table-bordered table-striped" style="margin-bottom: 0px;">
				<thead>
				<tr>
					<th width="50px">序号</th>
					<th width="100px">保单号</th>
					<th width="100px">被保人</th>
					<th width="100px">保费</th>
					<th width="100px">投保日期</th>
				</tr>
				</thead>
				<tbody>
				<c:set var="rowNumber" value="0" />
				<c:forEach items="${orderInsurances}" var="orderInsurance">
					<c:set var="rowNumber" value="${rowNumber+1}" />
					<tr>
						<td>${rowNumber}</td>
						<td>${orderInsurance.insuranceNo}</td>
						<td>${orderInsurance.assured}</td>
						<td>${orderInsurance.amount}</td>
						<td><fmt:formatDate value="${orderInsurance.insureDate}" pattern="yyyy-MM-dd HH:mm"/></td>
					</tr>
				</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
</c:if>

<table id="contentTable" class="table table-striped table-bordered table-condensed" style="margin-top: 15px">
	<thead>
		<tr><th width="50px">序号</th>
			<th width="120px">跟踪日期</th>
			<th>安维跟踪内容</th>
			<th width="80px">跟踪人员</th>
		</tr>
	</thead>
	<tbody>
	<c:if test="${!empty order.id }">
		<c:set var="rowNumber" value="0" />
		<c:forEach items="${order.logList}" var="track">
			<c:set var="rowNumber" value="${rowNumber+1}" />
			<tr>
				<td>${rowNumber}</td>
				<td><fmt:formatDate value="${track.createDate}" pattern="yyyy-MM-dd HH:mm"/></td>
				<td>${fns:escapeHtml(track.actionComment)}</td>
				<td>${fns:escapeHtml(track.createBy.name)}</td>
			</tr>
		</c:forEach>
	</c:if>
	</tbody>
</table>
<c:if test="${canAction}">
<c:set var="msg" value="" />
<c:set var="brand" value=""/>
<c:forEach items="${order.items}" var="item">
	<c:set var="msg" value="${msg} ${item.brand} ${item.product.name}${item.qty}${item.serviceType.name}" />
	<c:if test="${empty brand && !empty item.brand}">
		<c:set var="brand" value="${item.brand}"/>
	</c:if>
</c:forEach>
<c:set var="engineermsg" value="单号: ${order.orderNo} ~n用户: ${order.orderCondition.userName} {{=it.servicePhone}}  ${order.orderCondition.area.name} {{=it.serviceAddress}} ~n产品:${msg} ~n备注: ${order.description}"/>
<input type="hidden" id="txengineertmsg" name="txengineertmsg" value="${engineermsg}" />
<input id="lblserviceAddress" type="hidden" value="${order.orderCondition.serviceAddress}">
<input id="lblservicePhone" type="hidden" value="${order.orderCondition.servicePhone}">
<legend>新的跟踪进度    <a href="javascript:void(0);" style="margin-left: 30px;"
					 onclick="ServicePointOrderService.photoListNew('${order.id}','${order.quarter}',${fns:isNewOrder(order.orderNo)});"
					 class="btn btn-mini btn-primary">添加完成照片</a>
	                <a class="btn btn-mini btn-success" href="javascript:;" id="btn_engineer_copy" style="padding-left: 20px">复制工单信息</a>
</legend>
<form:form id="trackingForm" modelAttribute="order" action="${ctx}/sd/order/tracking" method="post" class="form-horizontal">
	<form:hidden path="id"/>
	<form:hidden path="quarter"/>
	<sys:message content="${message}"/>
	<div class="control-group">
		<label class="control-label">跟踪日期:</label>
		<div class="controls">
			<input id="trackingDate" name="trackingDate" class="Wdate required" type="text" value='<fmt:formatDate value="${order.trackingDate}" pattern="yyyy-MM-dd HH:mm"/>' onClick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm'})"/>
		</div>
	</div>
	<div class="control-group">
		<label class="control-label">安维跟踪内容:</label>
		<div class="controls">
			<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="250" class="input-xxlarge required"/>
			<form:hidden path="isCustomerSame" htmlEscape="false" class="required" />
		</div>
	</div>
	<div class="form-actions">
		<c:if test="${!empty order.id }">
			<input id="btnSaveTracking" class="btn btn-primary" type="button" onclick="saveTacking();" value="保 存"/>&nbsp;
		</c:if>
		<input id="btnCancel" class="btn" type="button" value="返 回" onclick="closeme();" />
	</div>
</form:form>
</c:if>
</body>
</html>