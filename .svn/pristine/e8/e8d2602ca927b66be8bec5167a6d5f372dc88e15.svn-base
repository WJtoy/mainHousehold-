<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>产品价格</title>
	<meta name="decorator" content="default" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@include file="/WEB-INF/views/include/treetable.jsp"%>
	<script type="text/javascript">
		$(document).ready(
		function()
		{
			$("#product").focus();
			$("#inputForm").validate(
				{
					submitHandler : function(form)
					{
						loading('正在提交，请稍等...');
						form.submit();
					},
					errorContainer : "#messageBox",
					errorPlacement : function(error, element)
					{
						$("#messageBox").text("输入有误，请先更正。");
						if (element.is(":checkbox")
								|| element.is(":radio")
								|| element.parent().is(
										".input-append"))
						{
							error.appendTo(element.parent()
									.parent());
						} else
						{
							error.insertAfter(element);
						}
					}
				});
		});
	</script>
</head>

<body>
	<ul class="nav nav-tabs">
		<c:set var="priceTypes" value="${fns:getDictListFromMS('PriceType')}" /><%--切换为微服务--%>
		<c:forEach items="${priceTypes}" var="dict">
			<li>
				<a href="${ctx}/md/productprice/list?type=${dict.value}">${dict.label}参考价格</a>
			</li>
		</c:forEach>
		<li class="active"><a href="javascript:void(0);"><shiro:hasPermission
					name="md:customerPrice:edit">${not empty productprice.id?'修改':'添加'}</shiro:hasPermission>产品价格<shiro:lacksPermission
					name="md:customerPrice:edit">查看</shiro:lacksPermission> </a></li>
	</ul>
	<form:form id="inputForm" modelAttribute="productPrices" action="${ctx}/md/productprice/saveproductprices" method="post" class="form-horizontal">
		<form:hidden path="id" />
		<sys:message content="${message}" />
		<legend>主体信息</legend>
		<div class="control-group">
			<label class="control-label">价格类型:</label>
			<form:select path="priceType.value" class="required" style="width:150px;">
				<form:option value="" label="请选择"/>
				<form:options items="${priceTypes}" itemLabel="label" itemValue="value" htmlEscape="false"/>
			</form:select>
			<label style="padding-left: 10px;">产品:</label>
			<form:select path="product.id" class="required" style="width:300px;">
				<form:option value="" label="请选择"/>
				<form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
			</form:select>
		</div>
		<legend>服务价格维护</legend>
		<table id="treeTable"
			class="table table-striped table-bordered table-condensed">
			<thead>
				<tr>
					<th>服务代码</th>
					<th>服务名称</th>
					<th>厂商标准价(元)</th>
					<th>厂商优惠价(元)</th>
					<th>网点标准价(元)</th>
					<th>网点优惠价(元)</th>
					<th>描述</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${productPrices.listProductPrice}"
					var="productPrice" varStatus="i" begin="0">
					<tr>
						<td>${productPrice.serviceType.code}</td>
						<td>${productPrice.serviceType.name} <form:hidden
								path="listProductPrice[${i.index}].serviceType.id"
								id="serviceType.id${i.index}" htmlEscape="false" maxlength="64"
								class="required" />
						</td>
						<td>
							<form:input
								path="listProductPrice[${i.index}].customerStandardPrice"
								id="customerStandardPrice${i.index}" htmlEscape="false"
								maxlength="7" class="required number double" /></td>
						<td>
							<form:input
								path="listProductPrice[${i.index}].customerDiscountPrice"
								id="customerDiscountPrice${i.index}"
								nogt="#customerStandardPrice${i.index}" htmlEscape="false"
								maxlength="7" class="required number double" /></td>
						<td>
							<form:input
							path="listProductPrice[${i.index}].engineerStandardPrice"
							id="engineerStandardPrice${i.index}" htmlEscape="false"
							maxlength="7" class="required number double" />

						</td>
						<td>
							<form:input
							path="listProductPrice[${i.index}].engineerDiscountPrice"
							id="engineerDiscountPrice${i.index}" htmlEscape="false"
							maxlength="7" class="required number double" />
						</td>

						<td><form:input path="listProductPrice[${i.index}].remarks"
								id="remarks${i.index}" htmlEscape="false" maxlength="255" /></td>

					</tr>
				</c:forEach>
			</tbody>
		</table>
		<div class="form-actions">
			<shiro:hasPermission name="md:customerPrice:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit"
					value="保 存" />&nbsp;</shiro:hasPermission>
			<%--<a class="btn" href="${ctx}/md/customerprice">返 回</a>--%>
			<input id="btnCancel" class="btn" type="button" value="返 回"
				   onclick="history.go(-1)" />
		</div>
	</form:form>
</body>
</html>
