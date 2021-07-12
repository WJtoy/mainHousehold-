<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>客户价格管理</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
	
		$(document).ready(function() {
			$("#engineer").focus();
			$("#inputForm").validate({
				messages: {
					discountPrice:{
						nogt:"优惠价必须小于等于价格"}
					},
				errorContainer: "#messageBox",
				errorPlacement: function(error, element) {
					$("#messageBox").text("输入有误，请先更正。");
					if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
						error.appendTo(element.parent().parent());
					} else {
						error.insertAfter(element);
					}
				}
			});
		});
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li><a href="${ctx}/md/customer/price/list">客户价格列表</a></li>
		<li class="active"><a href="javascript:;">客户价格<shiro:hasPermission name="md:customerprice:edit">${not empty customerPrice.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:customerprice:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="customerPrice" action="${ctx}/md/customer/price/save?qCustomerId=${customerId}&qCustomerName=${customerName}&qProductCategoryId=${productCategoryId}&qProductCategoryName=${productCategoryName}&qProductId=${productId}&qProductName=${productName}&qFirstSearch=${qFirstSearch}&examine=examine" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="customer.id"/>
        <form:hidden path="product.id"/>
        <sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">客户:${params}</label>
			<div class="controls">
				<input type="text" id="customer.name" name="customer.name" value="${customerPrice.customer.name}" readonly="readonly" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">产品:</label>
			<div class="controls">
				<input type="text" id="product.name" name="product.name" value="${customerPrice.product.name}" readonly="readonly" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">服务类型:</label>
			<div class="controls">
				<sys:treeselect id="serviceType" name="serviceType.id" value="${customerPrice.serviceType.id}" labelName="serviceType.name" labelValue="${customerPrice.serviceType.name}"
					title="服务类型" url="/md/servicetype/treeData" cssClass="required" disabled="true"/>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">价格:</label>
			<div class="controls">
				<form:input path="price" htmlEscape="false" maxlength="7"  class="required number"/>
				<c:forEach items="${productPriceList}" var="productPrice">
					${productPrice.priceTypeName}:${productPrice.standPrice}<%--切换为微服务--%>
				</c:forEach>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">优惠价:</label>
			<div class="controls">
				<form:input path="discountPrice" htmlEscape="false" maxlength="7" class="required number"/>
				<c:forEach items="${productPriceList}" var="productPrice">
					${productPrice.priceTypeName}:${productPrice.discountPrice}<%--切换为微服务--%>
				</c:forEach>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">冻结价:</label>
			<div class="controls">
				<form:input path="blockedPrice" htmlEscape="false" maxlength="7" class="required number"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">备注:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" class="input-xlarge"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">是否启用:</label>
			<div class="controls">
				<form:radiobutton path="delFlag" value="0"></form:radiobutton>是
				<form:radiobutton path="delFlag" value="1"></form:radiobutton>否
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:customerprice:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
	
	
</body>
</html>