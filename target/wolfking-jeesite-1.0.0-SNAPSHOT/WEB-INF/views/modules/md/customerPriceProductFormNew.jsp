<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>服务价格</title>
	<meta name="decorator" content="default" />
	<style>
		#editBtn{
			position: fixed;
			left: 0px;
			bottom: 5px;
			width: 100%;
			height: 50px;
			background: #fff;
			z-index: 10;
			border-top: 1px solid #e5e5e5;
			text-align: right;
		}
	</style>
<script type="text/javascript">
    var this_index = top.layer.index;
    var clickTag = 0;
    var $btnSubmit = $("#btnSubmit");
    $(document).ready(function() {
		$("#inputForm").validate({
			submitHandler: function (form) {
				// loading('正在提交，请稍等...');
				// form.submit();
                clickTag = 1;
                var loadingIndex;

                var options = {
                    url: "${ctx}/md/customer/price/saveProductPricesNew?qCustomerId=${customerId}&qCustomerName=${fns:urlEncode(customerName)}&qProductCategoryId=${productCategoryId}&qProductCategoryName=${fns:urlEncode(productCategoryName)}&qProductId=${productId}&qProductName=${fns:urlEncode(productName)}&qFirstSearch=${qFirstSearch}",
                    type: 'post',
                    dataType: 'json',
                    data:$(form).serialize(),
                    beforeSubmit: function(formData, jqForm, options){
                        loadingIndex = layer.msg('正在提交，请稍等...', {
                            icon: 16,
                            time: 0,
                            shade: 0.3
                        });
                        return true;
                    },// 提交前的回调函数
                    success:function (data) {
                        // 提交后的回调函数
                        if(loadingIndex) {
                            layer.close(loadingIndex);
                        }
                        if(ajaxLogout(data)){
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            return false;
                        }
                        if (data.success) {
                            layerMsg(data.message);
                            setTimeout(function () {
                                cancel();
                                // loading('同步中...');
                            }, 2000);
                            var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                            if(pframe){
                                pframe.repage();
                            }
                        } else {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            layerError("数据保存错误:" + data.message, "错误提示");
                        }
                        return false;
                    },
                    error: function (data) {
                        setTimeout(function () {
                            clickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                        }, 2000);
                        ajaxLogout(data,null,"数据保存错误，请重试!");
                    },
                };
                $("#inputForm").ajaxSubmit(options);
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

<form:form id="inputForm" modelAttribute="customerPrices" action="" method="post" class="form-horizontal">
	<sys:message content="${message}" />
	<c:if test="${canAction == true}">
		<div class="row-fluid" style="margin: 20px 0px 10px -127px;">
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

		<div class="alert alert-alert">提示：保内服务如价格或优惠价小于等于：0，不保存到数据库</div>
		<div id="divGrid" style="overflow: auto;height:480px;">
		<table id="contentTable" class="table table-striped table-bordered table-condensed" style="table-layout:fixed" cellspacing="0" width="100%">
			<thead>
			<tr>
				<th rowspan="2" style="width: 128px;">服务项目</th>
				<c:forEach items="${fns:getDictListFromMS('PriceType')}" var="dict">
					<th colspan="2" style="width: 144px;height: 40px;">${dict.label}</th>
				</c:forEach>
				<%--<th width="160">价格</th>--%>
				<%--<th width="160">优惠价</th>--%>
				<%--<th width="160">冻结价</th>--%>
				<th colspan="3" style="width: 240px;height: 40px;">厂商价格</th>
				<th rowspan="3" >描述</th>
			</tr>

			<tr>
				<c:forEach items="${fns:getDictListFromMS('PriceType')}" var="dict">
					<th width="70">价格</th>
					<th width="70">优惠价</th>
				</c:forEach>
				<th width="70">价格</th>
				<th width="70">优惠价</th>
				<th width="70">冻结价</th>
			</tr>
			</thead>
			<tbody>
			<c:forEach items="${customerPrices.prices}" var="price" varStatus="i" begin="0">
				<c:set var="index" value="${i.index}" />
				<%--class="${empty price.id?'warning':''}"--%>
				<tr id="tr_${index}">
				<%--服务名称--%>
				<c:choose>
					<c:when test="${price.delFlag==1}">
						<td>${price.serviceType.name}&nbsp;<span class="label">停用</span></td>
					</c:when>
					<c:otherwise>
						<td>${price.serviceType.name}&nbsp;
							<c:if test="${price.delFlag==2}"><span class="label label-important">待审核</span></c:if>
							<c:if test="${empty price.id}"><span class="label label-info">新增</span></c:if>
							<%--<c:if test="${not empty price.id}"><span class="label label-warning">修改</span></c:if>--%>
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
					</c:otherwise>
				</c:choose>

				<c:forEach items="${fns:getDictListFromMS('PriceType')}" var="dict" varStatus="d">
					<c:set var="dValue" value="${dict.value}" />
						<%--价格--%>
						<td style="text-align: center;vertical-align: middle;">
							<c:set var="priceHint" value="" />
							<c:set var="discountpriceHint" value="" />

							<c:forEach items="${price.productPriceList}" var="productPrice">
								<c:set var="pValue" value="${productPrice.priceType}" />
								<c:if test="${dValue == pValue}">
									<%--价格：0.0--%>
									<c:set var="priceHint" value="${priceHint}${productPrice.standPrice} " />
									<%--优惠价格：0.0--%>
									<c:set var="discountpriceHint" value="${discountpriceHint}${productPrice.discountPrice} " />
								</c:if>
							</c:forEach>
							<%--自定义价格--%>
							${priceHint}
						</td>

						<%--展示优惠价--%>
						<td style="text-align: center;vertical-align: middle;">
							${discountpriceHint}
						</td>
				</c:forEach>

					<c:choose>
						<c:when test="${price.delFlag==1}">
							<td>
								<input type="text" id="stop${index}.price" name="stop${index}.price"}
									   class="input-mini" value="${price.price}" disabled="disabled" style="text-align: center;width: 50px;" />
							</td>
							<td >
								<input type="text" id="stop${index}.discountPrice" name="stop${index}.discountPrice"
									   maxlength="7" class="input-mini" value="${price.discountPrice}" disabled="disabled" style="text-align: center;width: 50px;" />
							</td>
							<td >
								<input type="text" id="stop${index}.blockedPrice" name="stop${index}.blockedPrice"
									   class="input-mini" value="${price.blockedPrice}" disabled="disabled" style="text-align: center;width: 50px;" />
							</td>
							<td>
								<input type="text" id="stop${index}.remarks" name="stop${index}.remarks" disabled="disabled" style="width: 320px;"/>
							</td>
						</c:when>
						<c:otherwise>

							<c:choose>
								<c:when test="${price.serviceType.warrantyStatus.value eq 'IW'}">
									<td>
										<input type="text" id="prices[${index}].price" name="prices[${index}].price"}
											   maxlength="7" class="input-mini required number" min="0.0" value="${price.price}" style="text-align: center;width: 50px;"/>
									</td>
									<td >
										<input type="text" id="prices[${index}].discountPrice" name="prices[${index}].discountPrice"
											   maxlength="7" class="input-mini required number " min="0.0" comparePrice="[id='prices[${index}].price']" style="text-align: center;width: 50px;"
											   value="${price.discountPrice}"/>
									</td>
									<td >
										<input type="text" id="prices[${index}].blockedPrice" name="prices[${index}].blockedPrice" style="text-align: center;width: 50px;"
											   maxlength="7" class="input-mini required number " min="0.0" value="${price.blockedPrice}"/>
									</td>
									<td>
										<input type="text" id="prices[${index}].remarks" name="prices[${index}].remarks" maxlength="255"  value="${price.remarks}" style="width: 320px;"/>
									</td>
								</c:when>
								<c:otherwise>
									<td>
										<input type="text" id="prices[${index}].price" name="prices[${index}].price"}
											   maxlength="7" class="input-mini required number" min="0.0" value="${price.price}" style="text-align: center;width: 50px;"/>
									</td>
									<td >
										<input type="text" id="prices[${index}].discountPrice" name="prices[${index}].discountPrice"
											   maxlength="7" class="input-mini required number " min="0.0" style="text-align: center;width: 50px;"
											   value="${price.discountPrice}"/>
									</td>
									<td >
										<input type="text" id="prices[${index}].blockedPrice" name="prices[${index}].blockedPrice" style="text-align: center;width: 50px;"
											   maxlength="7" class="input-mini required number " min="0.0" value="${price.blockedPrice}"/>
									</td>
									<td>
										<input type="text" id="prices[${index}].remarks" name="prices[${index}].remarks" maxlength="255"  value="${price.remarks}" style="width: 320px;"/>
									</td>
								</c:otherwise>
							</c:choose>

						</c:otherwise>
					</c:choose>


				</tr>
			</c:forEach>
			</tbody>
		</table>
		</div>
		</c:if>

	<div id="editBtn">
		<c:if test="${canAction == true}">
			<shiro:hasPermission name="md:customerprice:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="margin-top: 10px;width: 85px;height: 35px;"/>&nbsp;
			</shiro:hasPermission>
		</c:if>
		<input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
	</div>

</form:form>

<script class="removedscript" type="text/javascript">
    $(document).ready(function() {
        $("th").css({"text-align":"center","vertical-align":"middle"});
        $("td").css({"text-align":"center","vertical-align":"middle"});
    });
    // 关闭页面
    function cancel() {
        top.layer.close(this_index);// 关闭本身
    }

</script>
</body>
</html>
