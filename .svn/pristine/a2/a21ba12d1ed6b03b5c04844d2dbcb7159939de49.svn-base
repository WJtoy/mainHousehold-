<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>服务价格</title>

	<meta name="decorator" content="default" />
	<script type="text/javascript">
		$(document).ready(
				function()
				{
					$("#value").focus();
					$("#inputForm")
							.validate(
									{
										messages :
										{
											customerDiscountPrice :
											{
												nogt : "优惠价必须小于等于标准价"
											}
// 											,
//											engineerDiscountPrice :
//											{
//												nogt : "优惠价必须小于等于标准价"
//											}
										},
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
		<li class="active"><a href="javascript:void(0);">参考价格<shiro:hasPermission
					name="md:customerPrice:edit">${not empty productPrice.id?'修改':'添加'}</shiro:hasPermission>
				<shiro:lacksPermission name="md:customerPrice:edit">查看</shiro:lacksPermission>
		</a>
		</li>
	</ul>
	<br />
	<form:form id="inputForm" modelAttribute="productPrice" action="${ctx}/md/productprice/save" method="post" class="form-horizontal">
		<form:hidden path="id" />
		<input id="priceType" name="priceType.value" type="hidden" value="${productPrice.priceType.value}"/>
		<sys:message content="${message}" />
		<div class="control-group">
			<label class="control-label">价格类型:</label>
			<div class="controls">
				<input type="text" readonly="readonly" value="${fns:getDictLabelFromMS(productPrice.priceType.value, 'PriceType', '')}"/><%--切换为微服务--%>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">产品:</label>
			<div class="controls">
				<sys:treeselect id="product" name="product.id"
					value="${productPrice.product.id}" labelName="product.name"
					labelValue="${productPrice.product.name}" title="产品"
					url="/md/product/treeData" cssClass="required" disabled="true" />
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">服务类型:</label>
			<div class="controls">
				<sys:treeselect id="serviceType" name="serviceType.id"
					value="${productPrice.serviceType.id}"
					labelName="serviceType.name"
					labelValue="${productPrice.serviceType.name}" title="服务类型"
					url="/md/servicetype/treeData" cssClass="required" disabled="true" />
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">厂商标准价:</label>
			<div class="controls">
				<form:input path="customerStandardPrice" htmlEscape="false" maxlength="7"
							class="required number  double" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">厂商优惠价:</label>
			<div class="controls">
				<form:input path="customerDiscountPrice" htmlEscape="false" nogt="#customerStandardPrice"
							maxlength="7" class="number  double" />
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">网点标准价:</label>
			<div class="controls">
				<form:input path="engineerStandardPrice" htmlEscape="false" maxlength="7" class="number double" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">网点优惠价:</label>
			<div class="controls">
				<form:input path="engineerDiscountPrice" htmlEscape="false" maxlength="7" class="number double" />
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3"
					maxlength="255" class="" />
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:customerPrice:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit"
					value="保 存" />&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回"
				onclick="history.go(-1)" />
		</div>
	</form:form>
</body>
</html>
