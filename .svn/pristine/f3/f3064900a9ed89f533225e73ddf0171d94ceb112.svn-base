<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>服务价格</title>
	<meta name="decorator" content="default" />
<script type="text/javascript">
    $(document).ready(function() {
		$("#inputForm").validate({
			submitHandler: function (form) {
				loading('正在提交，请稍等...');
				form.submit();
			},
			errorContainer: "#messageBox",
			errorPlacement: function (error, element) {
				$("#messageBox").text("输入有误，请先更正。");
				if (element.is(":checkbox")
					|| element.is(":radio")
					|| element.parent().is(
						".input-append")) {
					error.appendTo(element.parent()
						.parent());
				} else {
					error.insertAfter(element);
				}
			}
		});
	});
    function delRow(idx){
        $("#tr_"+idx).hide();
        $("[id='prices[" + idx + "].delFlag']").val(1);
	}
</script>
</head>
<body>
<ul class="nav nav-tabs">
	<li><a href="${ctx}/md/customer/price?repage=true&customer.id=${customerPrices.customer.id}&customer.name=${fns:urlEncode(customerPrices.customer.name)}
											&productCategory.id=${productCategoryId}&productCategory.name=${fns:urlEncode(productCategoryName)}
											&product.id=${productId}&product.name=${fns:urlEncode(productName)}">客户价格列表</a></li>
	<li class="active"><a href="javascript:;">客户价格维护</a></li>
</ul>
<form:form id="inputForm" modelAttribute="customerPrices" action="${ctx}/md/customer/price/saveProductPrices?qCustomerId=${customerId}&qCustomerName=${fns:urlEncode(customerName)}
											&qProductCategoryId=${productCategoryId}&qProductCategoryName=${fns:urlEncode(productCategoryName)}
											&qProductId=${productId}&qProductName=${fns:urlEncode(productName)}&qFirstSearch=${qFirstSearch}" method="post" class="form-horizontal">
	<sys:message content="${message}" />
	<c:if test="${canAction == true}">
		<legend style="padding-top: 10px;">客户产品</legend>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">客户:</label>
					<div class="controls">
						<form:hidden path="customer.id" />
						<form:input path="customer.name" readonly="true" cssClass="input-medium" cssStyle="width:250px;"/>
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label">产品:</label>
					<div class="controls">
						<form:hidden path="product.id" />
						<form:input path="product.name" readonly="true" cssClass="input-medium"  cssStyle="width:250px;"/>
					</div>
				</div>
			</div>
		</div>
		<legend>价格</legend>
		<div class="alert alert-alert">提示：保内服务如价格或优惠价小于等于：0，不保存保存到数据库</div>
		<div id="divGrid" style="overflow: auto;height:480px;">
		<table id="contentTable" class="table table-striped table-bordered table-condensed" style="table-layout:fixed" cellspacing="0" width="100%">
			<thead>
			<tr>
				<th width="150">服务名称</th>
				<th width="160">价格</th>
				<th width="160">优惠价</th>
				<th width="160">冻结价</th>
				<th width="*">描述</th>
			</tr>
			</thead>
			<tbody>
			<c:forEach items="${customerPrices.prices}" var="price" varStatus="i" begin="0">
				<c:set var="index" value="${i.index}" />
				<%--class="${empty price.id?'warning':''}"--%>
				<tr id="tr_${index}">
				<c:choose>
					<c:when test="${price.delFlag==1}">
						<td>${price.serviceType.name}&nbsp;<span class="label">停用</span>
						</td>
						<td>
							<input type="text" id="stop${index}.price" name="stop${index}.price"}
								   class="input-mini" value="${price.price}" disabled="disabled" />
							<c:forEach items="${price.productPriceList}" var="productPrice">
								<br/>${productPrice.priceTypeName}:${productPrice.standPrice}
							</c:forEach>
						</td>
						<td >
							<input type="text" id="stop${index}.discountPrice" name="stop${index}.discountPrice"
								   maxlength="7" class="input-mini" value="${price.discountPrice}" disabled="disabled" />
							<c:forEach items="${price.productPriceList}" var="productPrice">
								<br/>${productPrice.priceTypeName}:${productPrice.discountPrice}
							</c:forEach>
						</td>
						<td >
							<input type="text" id="stop${index}.blockedPrice" name="stop${index}.blockedPrice"
								   class="input-mini" value="${price.blockedPrice}" disabled="disabled" />
						</td>
						<td>
							<input type="text" id="stop${index}.remarks" name="stop${index}.remarks" disabled="disabled" />
						</td>
					</c:when>
					<c:otherwise>
						<td>${price.serviceType.name}&nbsp;
							<c:if test="${price.delFlag==2}"><span class="label label-important">待审核</span></c:if>
							<c:if test="${empty price.id}"><span class="label label-info">新增</span></c:if>
							<c:if test="${not empty price.id}"><span class="label label-warning">修改</span></c:if>
								<%--<a href="javascript:;" onclick="delRow(${i.index});return false;" title="删除"><i class="icon-delete" style="margin-top: 0px;"></i></a>--%>
							<input type="hidden" id="prices[${index}].id" name="prices[${index}].id" value="${price.id}" />
							<input type="hidden" id="prices[${index}].serviceType.id" name="prices[${index}].serviceType.id"
								   value="${price.serviceType.id}" />
							<input type="hidden" id="prices[${index}].serviceType.name" name="prices[${index}].serviceType.name"
								   value="${price.serviceType.name}" />
							<input type="hidden" id="prices[${index}].serviceType.warrantyStatus.value" name="prices[${index}].serviceType.warrantyStatus.value"
								   value="${price.serviceType.warrantyStatus.value}" />
							<input type="hidden" id="prices[${index}].delFlag" name="prices[${index}].delFlag" value="${price.delFlag}" />
						</td>
						<td>
							<input type="text" id="prices[${index}].price" name="prices[${index}].price"}
								   maxlength="7" class="input-mini required number" min="0.0" value="${price.price}"/>
							<c:forEach items="${price.productPriceList}" var="productPrice">
								<br/>${productPrice.priceTypeName}:${productPrice.standPrice}
							</c:forEach>
						</td>
						<td >
							<input type="text" id="prices[${index}].discountPrice" name="prices[${index}].discountPrice"
								   maxlength="7" class="input-mini required number " min="0.0" comparePrice="[id='prices[${index}].price']"
								   value="${price.discountPrice}"/>
							<c:forEach items="${price.productPriceList}" var="productPrice">
								<br/>${productPrice.priceTypeName}:${productPrice.discountPrice}
							</c:forEach>
						</td>
						<td >
							<input type="text" id="prices[${index}].blockedPrice" name="prices[${index}].blockedPrice"
								   maxlength="7" class="input-mini required number " min="0.0" value="${price.blockedPrice}"/>
						</td>
						<td>
							<input type="text" id="prices[${index}].remarks" name="prices[${index}].remarks" maxlength="255"  value="${price.remarks}"/>
						</td>
					</c:otherwise>
				</c:choose>
				</tr>
			</c:forEach>
			</tbody>
		</table>
		</div>
		</c:if>
		<div class="form-actions">
			<c:if test="${canAction == true}">
			<shiro:hasPermission name="md:customerprice:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" />&nbsp;
			</shiro:hasPermission>
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)" />
		</div>

</form:form>
</body>
</html>
