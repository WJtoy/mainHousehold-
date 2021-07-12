<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<!-- 网点单个产品的所有服务价格 -->
	<title>安维价格</title>
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
		/*
		 *自定义校验规则
		 * phone  要验证规则名称
		 * func  校验处理
		 * value 获取输入的值
		 * Element  当前的文本框
		 * params 校验参数
		 * */
        $.validator.addMethod('pricemin',function (value,element,params) {
			/*价格范围验证*/
            var num = parseFloat(value);
            if(num < parseFloat(params)){
                return false;
            }
            return true;
        },'价格最小值不能小于-1000');
        jQuery.validator.addMethod("validDiscountPrice", function(b, a, c) {
            var bval = parseFloat(b);
            var cval = parseFloat($(c).val());
            if(bval<=0 && cval<0){
                return Math.abs(bval) <= Math.abs(cval);
			}else {
                return bval <= cval;
            }
            //return Math.abs(bval) <= Math.abs(cval);
            //return parseFloat(b) < parseFloat($(c).val())
        }, "优惠价不能高于标准价");

	});
    function delRow(idx){
        $("#tr_"+idx).hide();
        $("[id='prices[" + idx + "].delFlag']").val(1);
	}
</script>
</head>
<body>
<ul class="nav nav-tabs">
	<li><a href="${ctx}/md/serviceprice?repage=true&servicePoint.id=${servicePointId}&servicePoint.name=${fns:urlEncode(servicePointName)}
											&productCategory.id=${productCategoryId}&productCategory.name=${fns:urlEncode(productCategoryName)}
											&product.id=${productId}&product.name=${fns:urlEncode(productName)}">安维价格列表</a></li>
	<li class="active"><a href="javascript:;">安维价格维护</a></li>
</ul>
<form:form id="inputForm" modelAttribute="servicePrices" action="${ctx}/md/serviceprice/saveProductPrices?qServicePointId=${servicePointId}&qServicePointName=${fns:urlEncode(servicePointName)}
											&qProductCategoryId=${productCategoryId}&qProductCategoryName=${fns:urlEncode(productCategoryName)}
											&qProductId=${productId}&qProductName=${fns:urlEncode(productName)}" method="post" class="form-horizontal">
	<sys:message content="${message}" />
	<c:if test="${canAction == true}">
		<legend style="padding-top: 10px;">网点产品</legend>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">网点:</label>
					<div class="controls">
						<form:hidden path="servicePoint.id" />
						<form:hidden path="servicePoint.name"/>
						<%--
						<form:input path="servicePoint.name" readonly="true" cssClass="input-medium" cssStyle="width:250px;"/>--%>
						<input class="input-medium valid" style="width:250px;" readonly="readonly" type="text" value="${servicePrices.servicePoint.name}(${servicePrices.servicePoint.servicePointNo})" aria-invalid="false">
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label">产品:</label>
					<div class="controls">
						<form:hidden path="product.id" />
						<form:input path="product.name" readonly="true" cssClass="input-medium" cssStyle="width:250px;"/>
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label">价格轮次:</label>
					<c:set var="priceInfo" value="${fns:getDictLabelFromMS(servicePrices.servicePoint.useDefaultPrice,'PriceType','')}" />
					<div class="controls">
						<c:choose>
							<c:when test="${servicePrices.servicePoint.customizePriceFlag eq 1}">
								<input class="input-medium valid" style="width:250px;" readonly="readonly" type="text" value="自定义价格(网点当前使用:${priceInfo})" aria-invalid="false">
							</c:when>
							<c:otherwise>
								<input class="input-medium valid" style="width:250px;" readonly="readonly" type="text" value="${priceInfo}" aria-invalid="false">
							</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>
		</div>
		<legend>价格</legend>
		<div id="divGrid" style="overflow: auto;height:480px;">
		<table id="contentTable" class="table table-striped table-bordered table-condensed" style="table-layout:fixed" cellspacing="0" width="100%">
			<thead>
			<tr>
				<th width="150">服务名称</th>
				<th>价格</th>
				<th>优惠价</th>
				<th width="*">描述</th>
			</tr>
			</thead>
			<tbody>
			<c:forEach items="${servicePrices.prices}" var="price" varStatus="i" begin="0">
				<c:set var="index" value="${i.index}" />
				<c:choose>
					<c:when test="${price.delFlag==1}">
						<tr id="tr_${index}">
							<td>${price.serviceType.name}&nbsp;
								<span class="label label-important">停用</span>
							</td>
							<td>
								<input type="text" id="stop${index}.price" name="stop[${index}].price"}
									   disabled="disabled" class="input-mini" value="${price.price}"/>
								<c:forEach items="${price.productPriceList}" var="productPrice">
									${productPrice.priceTypeName}:${productPrice.standPrice}<%--切换为微服务--%>
								</c:forEach>
							</td>
							<td >
								<input type="text" id="stop${index}.discountPrice" name="stop[${index}].discountPrice"
									   disabled="disabled" class="input-mini" value="${price.discountPrice}"/>
								<c:forEach items="${price.productPriceList}" var="productPrice">
									${productPrice.priceTypeName}:${productPrice.discountPrice}<%--切换为微服务--%>
								</c:forEach>
							</td>
							<td>
								<input type="text" id="stop${index}.remarks" name="stop[${index}].remarks" disabled="disabled"  />
							</td>
						</tr>
					</c:when>
						<c:otherwise>
							<%--class="${empty price.id?'warning':''}"--%>
							<tr id="tr_${index}" >
								<td>${price.serviceType.name}&nbsp;
									<c:if test="${empty price.id}"><span class="label label-info">新增</span></c:if>
									<c:if test="${not empty price.id}"><span class="label label-warning">修改</span></c:if>
										<%--<a href="javascript:;" onclick="delRow(${i.index});return false;" title="删除"><i class="icon-delete" style="margin-top: 0px;"></i></a>--%>
									<input type="hidden" id="prices[${index}].id" name="prices[${index}].id" value="${price.id}" />
									<input type="hidden" id="prices[${index}].serviceType.id" name="prices[${index}].serviceType.id"
										   value="${price.serviceType.id}" />
									<input type="hidden" id="prices[${index}].delFlag" name="prices[${index}].delFlag" value="${price.delFlag}" />
								</td>
								<td>
									<c:set var="priceHint" value="" />
									<c:set var="discountpriceHint" value="" />
									<c:set var="remarks" value="" />
									<c:set var="existsPriceType" value="false" />
									<c:set var="bExclude" value="false"/>
									<c:set var="lstCount" value="0" />
									<c:set var="priceDifferent" value="false" />
									<c:set var="discountPriceDifferent" value="false" />
									<c:forEach items="${price.productPriceList}" var="productPrice">
										<c:if test="${productPrice.priceType eq servicePrices.servicePoint.useDefaultPrice}">
											<c:set var="existsPriceType" value="true" />
											<c:if test="${productPrice.standPrice ne price.price}">
												<c:set var="priceDifferent" value="true" />
											</c:if>
											<c:if test="${productPrice.discountPrice ne price.discountPrice}">
												<c:set var="discountPriceDifferent" value="true" />
											</c:if>
										</c:if>
										<c:set var="priceHint" value="${priceHint}${productPrice.priceTypeName}:${productPrice.standPrice} " />
										<c:set var="discountpriceHint" value="${discountpriceHint}${productPrice.priceTypeName}:${productPrice.discountPrice} " />
										<c:set var="lstCount" value="${lstCount+1}" />
									</c:forEach>
									<c:if test="${fn:startsWith(servicePrices.servicePoint.servicePointNo,'YH') || servicePrices.servicePoint.customizePriceFlag eq 1}">
										<c:set var="bExclude" value="true"></c:set>
									</c:if>
									<c:if test="${bExclude eq false && existsPriceType eq false && lstCount gt 0}">
										<c:set var="remarks" value="${price.serviceType.name}价格没有维护" />
									</c:if>
									<input type="text" id="prices[${index}].price" name="prices[${index}].price" style="<c:out value='${not empty remarks?"visibility:hidden;":""}'/><c:out value='${priceDifferent eq true?"background:yellow;":""}'/>"
										   maxlength="7" class="input-mini required number" pricemin="-1000" value="${price.price}"/>
									<%--
									<input type="text" id="prices[${index}].price" name="prices[${index}].price"}
										   maxlength="7" class="input-mini required number" pricemin="-1000" value="${price.price}"/>
									<c:forEach items="${price.productPriceList}" var="productPrice">
										${productPrice.priceTypeName}:${productPrice.standPrice}
									</c:forEach>
									--%>
									${priceHint}
								</td>
								<td >
									<input type="text" id="prices[${index}].discountPrice" name="prices[${index}].discountPrice" style="<c:out value='${not empty remarks?"visibility:hidden;":""}'/><c:out value='${discountPriceDifferent eq true?"background:yellow;":""}'/>"
										   maxlength="7" class="input-mini required number" validDiscountPrice="[id='prices[${index}].price']"
										   value="${price.discountPrice}"/>
									<%--
									<input type="text" id="prices[${index}].discountPrice" name="prices[${index}].discountPrice"
										   maxlength="7" class="input-mini required number"  validDiscountPrice="[id='prices[${index}].price']"
										   value="${price.discountPrice}"/>
									<c:forEach items="${price.productPriceList}" var="productPrice">
										${productPrice.priceTypeName}:${productPrice.discountPrice}
									</c:forEach>
									--%>
									${discountpriceHint}
								</td>
								<td>
									<%--
									<input type="text" id="prices[${index}].remarks" name="prices[${index}].remarks" maxlength="255" />
									--%>
									<c:set var="textColor" value="" />
									<c:if test="${not empty remarks}">
										<c:set var="textColor" value="color:#FF0000" />
									</c:if>
									<input type="text" id="prices[${index}].remarks" name="prices[${index}].remarks" maxlength="255" style="${textColor}" value="${remarks}"/>
								</td>
							</tr>
						</c:otherwise>
				</c:choose>
			</c:forEach>
			</tbody>
		</table>
		</div>
		</c:if>
		<div class="form-actions">
			<c:if test="${canAction == true}">
				<shiro:hasPermission name="md:serviceprice:edit">
					<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" />&nbsp;
				</shiro:hasPermission>
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)" />
		</div>
</form:form>
</body>
</html>
