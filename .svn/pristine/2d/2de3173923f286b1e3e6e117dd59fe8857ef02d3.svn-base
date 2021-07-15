<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<!-- 网点单个产品的所有服务价格 -->
	<title>安维价格</title>
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
		.table thead th, .table tbody td {
			text-align: center;
			vertical-align: middle;
			BackColor: Transparent;
		}
	</style>
<script type="text/javascript">
    function child(obj) {
        var data = eval(obj);
        $("#servicePointId").val(data.servicePointId);
        $("#servicePointNo").val(data.servicePointNo);
        $("#serviceName").val(data.serviceName);
        $("#servicePointPrimaryName").val(data.servicePointPrimaryName);
        $("#contactInfo").val(data.contactInfo);
        $("#customizePriceFlag").val(data.customizePriceFlag);
        $("#useDefaultPrice").val(data.useDefaultPrice);
        $("#degree").val(data.degree);
        $("#serviceRemotePriceFlag").val(data.serviceRemotePriceFlag);
        $("#remotePriceFlag").val(data.remotePriceFlag);
        $("#remotePriceType").val(data.remotePriceType);
		$("#productId").val(data.productId);
		$("#productCategoryId").val(data.productCategoryId);
    }

    var this_index = top.layer.index;
    var clickTag = 0;
    var $btnSubmit = $("#btnSubmit");
    $(document).ready(function() {
        $("#inputForm :input").change(function(){
            $("#inputForm").data("changed",true);
        });

		$("#inputForm").validate({
			submitHandler: function (form) {
                clickTag = 1;
                var loadingIndex;

                var newPriceLength = $("[name='newPrice']");
                if (newPriceLength == undefined){

				} else {

                	if (newPriceLength.length >0) {
						$("#inputForm").data("changed", true);
					}
                }
                if ($("#inputForm").data("changed")) {
				} else {
                    layerError("当前无任何修改，请重新输入要修改的价格","错误提示");
                    return false;
				}

				var entity = {};

				$("input[type='checkbox'][name='checkedPrices']:checkbox:checked").each(function(index,element){

					var value = $(this).val();

					entity['prices['+index+'].id'] = $("#prices_" + value + "\\.id").val();

					entity['prices['+index+'].serviceType.id'] = $('#prices_' + value + '\\.serviceType\\.id').val();

					entity['prices['+index+'].delFlag'] = $('#prices_' + value + '\\.delFlag').val();

					entity['prices['+index+'].priceType'] = $('#prices_' + value + '\\.priceType').val();

					entity['prices['+index+'].price'] = $('#price_' + value + '').val();

					entity['prices['+index+'].discountPrice'] = $('#discountPrice_' + value + '').val();

				});


                var options = {
                    url: "${ctx}/md/serviceprice/saveRemotePrices",
                    type: 'post',
                    dataType: 'json',
                    data: entity,
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


                            setTimeout(function () {
                                var servicePointId = $("#servicePointId").val();
                                var servicePointNo = $("#servicePointNo").val();
                                var serviceName = $("#serviceName").val();
                                var servicePointPrimaryName = $("#servicePointPrimaryName").val();
                                var contactInfo = $("#contactInfo").val();
                                var customizePriceFlag = 1;
                                var useDefaultPrice = $("#useDefaultPrice").val();
                                var degree = $("#degree").val();
                                var serviceRemotePriceFlag = $("#serviceRemotePriceFlag").val();
                                var remotePriceFlag = 1;
                                var remotePriceType = $("#remotePriceType").val();
                                var selProductId = $("#productId").val();
								var selProductCategoryId = $("#productCategoryId").val();
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if (pframe) {
                                    pframe.reloadPrice(servicePointId, servicePointNo, serviceName, servicePointPrimaryName, contactInfo, customizePriceFlag,useDefaultPrice,degree,serviceRemotePriceFlag,remotePriceFlag,remotePriceType, selProductId,selProductCategoryId);
                                }
                            },1000);
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
				// form.submit();
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

		$.validator.addMethod("validDiscountPrice", function(b, a, c) {
			var bval = parseFloat(b);
			var cval = parseFloat($(c).val());
			if(bval<=0 && cval<0){
				return Math.abs(bval) <= Math.abs(cval);
			}else {
				return bval <= cval;
			}

		}, "优惠价不能高于标准价");


		$("#selectAll").change(function() {
			var $check = $(this);
			$("input:checkbox").each(function(){
				if ($(this).val() != "on") {
					if ($check.prop("checked") == "checked" || $check.prop("checked") == true) {
						$(this).prop("checked", true);
						var price = $("#price_" + $(this).val() +"").val();
						if(price == '-'){
							$("#price_" + $(this).val() +"").val('0.0');
							$("#discountPrice_" + $(this).val() +"").val('0.0');
						}
						$("#price_" + $(this).val() +"").removeAttr('disabled');
						$("#discountPrice_" + $(this).val() +"").removeAttr('disabled');
					} else {
						$(this).prop("checked", false);
						$("#price_" + $(this).val() +"").attr('disabled',true);
						$("#discountPrice_" + $(this).val() +"").attr('disabled',true);
					}
				}
			});
		});

		$("[name=checkedPrices]").change(function() {
			var index = $(this).val();
			var price = $("#price_" + index +"").val();
			if(price == '-'){
				$("#price_" + index +"").val('0.0');
				$("#discountPrice_" + index +"").val('0.0');
			}

			if($(this).is(':checked')){
				$("#price_" + index +"").removeAttr('disabled');
				$("#discountPrice_" + index +"").removeAttr('disabled');
			}else {
				$("#price_" + index +"").attr('disabled',true);
				$("#discountPrice_" + index +"").attr('disabled',true);
			}

		});
	});
</script>
</head>
<body>
<input type="hidden" id="servicePointId">
<input type="hidden" id="servicePointNo">
<input type="hidden" id="serviceName">
<input type="hidden" id="servicePointPrimaryName">
<input type="hidden" id="contactInfo">
<input type="hidden" id="customizePriceFlag">
<input type="hidden" id="useDefaultPrice">
<input type="hidden" id="degree">
<input type="hidden" id="serviceRemotePriceFlag">
<input type="hidden" id="remotePriceFlag">
<input type="hidden" id="remotePriceType">
<input type="hidden" id="productId"/>
<input type="hidden" id="productCategoryId" />

<form:form id="inputForm" modelAttribute="servicePrices" action="" method="post" class="form-horizontal">
	<sys:message content="${message}" type="loading"/>
	<c:if test="${canAction == true}">
		<div class="row-fluid">
			<div class="span4" style="display: none">
				<div class="control-group">
					<label class="control-label">网点:</label>
					<div class="controls">
						<form:hidden path="servicePoint.id" />
						<form:hidden path="servicePoint.name"/>
						<input class="input-medium valid" style="width:250px;" readonly="readonly" type="text" value="${servicePrices.servicePoint.name}(${servicePrices.servicePoint.servicePointNo})" aria-invalid="false">
					</div>
				</div>
			</div>
			<div class="span4" style="margin: 20px 0 10px 0;width: 35%;">
				<div class="control-group">
					<label style="color: #999999;">产品：</label>
						<form:hidden path="product.id" />
					    <%--<form:hidden path="product.category.id" />--%>
						<input name="product.name" htmlEscape="false" type="text" disabled="disabled" value="${servicePrices.product.name}"/>
				</div>
			</div>

		</div>
		<table id="contentTable" class="table table-striped table-bordered table-condensed"  width="100%">

			<thead>
			<tr>
				<th rowspan="2" width="50px"><input type="checkbox" id="selectAll" name="selectAll" style="zoom: 1.5"/></th>
				<th rowspan="2" style="width: 128px;">服务项目</th>
				<th colspan="2" style="width: 144px;">服务价格</th>
				<th colspan="2" style="width: 144px;">偏远价格</th>
			</tr>
			<tr>
				<th width="70">价格(元)</th>
				<th width="70">优惠价(元)</th>

				<th width="70">价格(元)</th>
				<th width="70" >优惠价(元)</th>
			</tr>
			</thead>
			<tbody>

			<%--服务项目--%>
			<c:forEach items="${servicePrices.prices}" var="price" varStatus="i" begin="0">
				<c:set var="index" value="${i.index}" />
				<tr id="tr_${index}" style="height: 48px;">
					<%--服务项目列--%>
					<td>
						<c:choose>
							<c:when test="${price.id == null}">
								<input type="checkbox" name="checkedPrices" value="${index}" style="zoom: 1.5"/>
							</c:when>
							<c:otherwise>
								<input type="checkbox" name="checkedPrices" checked="true" value="${index}" style="zoom: 1.5"/>
							</c:otherwise>
						</c:choose>

					</td>
					<td style="display: table-cell;vertical-align: middle;">${price.serviceType.name}&nbsp;

						<input type="hidden" id="prices_${index}.id"  value="${price.id}" />
						<input type="hidden" id="prices_${index}.serviceType.id" value="${price.serviceType.id}" />
						<input type="hidden" id="prices_${index}.delFlag" value="${price.delFlag}" />
						<input type="hidden" id="prices_${index}.priceType"  value="${price.priceType}" />
					</td>
					<td>${price.referPrice}</td>
					<td>${price.referDiscountPrice}</td>
						<c:choose>
							<c:when test="${price.id == null}">
								<td><input type="text" value="-" disabled='disabled' id="price_${index}" name="price_${index}" style="width: 70px" maxlength="7" class="input-mini number" pricemin="-1000"/></td>
								<td><input type="text" value="-" disabled='disabled' id="discountPrice_${index}" name="discountPrice_${index}" style="width: 70px" maxlength="7" class="input-mini number" validDiscountPrice="[id='price_${index}']"/></td>
							</c:when>
							<c:otherwise>
								<td><input type="text" id="price_${index}"  name="price_${index}" value="${price.price}" style="width: 70px" maxlength="7" class="input-mini required number" pricemin="-1000"/></td>
								<td><input type="text" id="discountPrice_${index}" name="discountPrice_${index}" style="width: 70px" maxlength="7" class="input-mini required number" validDiscountPrice="[id='price_${index}']" value="${price.discountPrice}"></td>
							</c:otherwise>
						</c:choose>

				</tr>
			</c:forEach>
			</tbody>
		</table>
		</c:if>
		<div style="height: 40px;"></div>
		<div id="editBtn">
			<c:if test="${canAction == true}">
				<shiro:hasPermission name="md:serviceprice:edit">
					<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="margin-top: 10px;width: 85px;height: 35px;"/>&nbsp;
				</shiro:hasPermission>
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="cancel()" style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
		</div>
</form:form>

<script class="removedscript" type="text/javascript">
    // 关闭页面
    function cancel() {
        top.layer.close(this_index);// 关闭本身
    }

</script>
</body>
</html>
