<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
<title>客户服务价格</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default" />
<script type="text/javascript">
	$(document).ready(function() {
        var pagestyle = function() {
            var h = $(window).width() -0;
            $("#divGrid").css("width",h);
        }
        //注册加载事件
        $("#iframe",window).load(pagestyle);
        //注册窗体改变大小事件
        $(window).resize(pagestyle);
        $("th").css({"text-align":"center","vertical-align":"middle"});
        $("td").css({"vertical-align":"middle"});

		$("#value").focus();
		$("#inputForm").validate({
			submitHandler : function(form) {
				loading('正在提交，请稍等...');
				form.submit();
			},
			errorContainer : "#messageBox",
			errorPlacement : function(error, element) {
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
		$("#btnSearch").click(function(){
		    $("#inputForm").attr("action","${ctx}/md/customer/price/forms").submit();
		});

        $("#btnSubmit").click(function(){
            $("#inputForm").attr("action","${ctx}/md/customer/price/savePrices").submit();
        });
});
</script>
	<style>
		.input-mini {width:40px !important;}
		#contentTable {table-layout: auto !important;}
		.col_product {min-width: 250px;}
		.col_command {width: 60px;}
		.table tbody td.error {background-color: #f2dede!important;}
	</style>
</head>

<body>
	<ul class="nav nav-tabs">
		<li><a href="${ctx}/md/customer/price/list">客户价格列表</a>
		</li>
		<li class="active"><a href="javascript:void(0);">客户价格<shiro:hasPermission
					name="md:customerprice:edit">${not empty servicePrice.id?'修改':'添加'}</shiro:hasPermission>
				<shiro:lacksPermission name="md:customerprice:edit">查看</shiro:lacksPermission>
		</a>
		</li>
	</ul>
	<sys:message content="${message}" />
	<form:form id="inputForm" modelAttribute="customerPrices" action="${ctx}/md/customer/price/forms" method="post" class="form-horizontal">
		<div class="control-group">
			<label class="control-label">客户:</label>
			<div class="controls">
				<span>
				<select id="customerId" name="customer.id" class="input-xxlarge" >
						<option value=""
								<c:out value="${(empty customerPrices.customer.id)?'selected=selected':''}" />>所有</option>
						<c:forEach items="${fns:getCustomerList()}" var="customer">
							<option value="${customer.id}"
									<c:out value="${(customerPrices.customer.id eq customer.id)?'selected=selected':''}" />>${customer.name}</option>
						</c:forEach>
				</select>
				</span>
				<input id="btnSearch" class="btn btn-primary" type="button" value="查询" />
			</div>
		</div>
		<form:hidden path="customer.name" />
	<c:if test="${customerPrices.customer.id != null}">
		<legend>价格</legend>
	<div id="divGrid" style="overflow: auto;height:480px;">
		<table id="contentTable" class="table table-striped table-bordered table-condensed" style="table-layout:fixed" cellspacing="0" width="100%">
			<thead>
				<tr>
					<th rowspan="2"><label class="col_product">产品名称</label></th>
					<c:forEach items="${serviceTypes}" var="serviceType">
						<th colspan="3" style="width: 150px;">${serviceType.name}</th>
					</c:forEach>
				</tr>
				<tr>
					<c:forEach items="${serviceTypes}" var="serviceType">
						<th>价格</th>
						<th>优惠价</th>
						<th>冻结价</th>
					</c:forEach>
				</tr>
			</thead>
			<tbody>
				<c:set var="index" value="-1" />
				<c:forEach items="${priceMap}" var="map" varStatus="i" begin="0">
					<tr>
						<td>${map.key.name}</td>
						<c:forEach items="${map.value}" var="price">
							<c:set var="index" value="${index+1}" />
							<td class="${price.delFlag==1?'error':''}">
								<input type="hidden" id="prices[${index}].id" name="prices[${index}].id" value="${price.id}" />
								<input type="hidden" id="prices[${index}].delFlag" name="prices[${index}].delFlag" value="${price.delFlag}" />
								<input type="hidden" id="prices[${index}].serviceType.id" name="prices[${index}].serviceType.id" value="${price.serviceType.id}" />
								<input type="hidden" id="prices[${index}].product.id" name="prices[${index}].product.id"
									   value="${map.key.id}" />
								<input type="text" id="prices[${index}].price" name="prices[${index}].price" ${price.delFlag==1?'readonly':''}
									   maxlength="7" class="input-mini required number" value="${price.price}"/>
							</td>
							<td class="${price.delFlag==1?'error':''}">
								<input type="text" id="prices[${index}].discountPrice" name="prices[${index}].discountPrice" ${price.delFlag==1?'readonly':''}
									   maxlength="7" class="input-mini required number" <c:if test="${price.delFlag==0}">comparePrice="[id='prices[${index}].price']"</c:if>  value="${price.discountPrice}"/>
							</td>
							<td class="${price.delFlag==1?'error':''}">
								<input type="text" id="prices[${index}].blockedPrice" name="prices[${index}].blockedPrice" ${price.delFlag==1?'readonly':''}
									   maxlength="7" class="input-mini required number" value="${price.blockedPrice}"/>
							</td>
						</c:forEach>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:customerprice:edit">
				<input id="btnSubmit" class="btn btn-primary" type="button" value="保 存" />&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)" />
		</div>
		</c:if>
	</form:form>
</body>
</html>
