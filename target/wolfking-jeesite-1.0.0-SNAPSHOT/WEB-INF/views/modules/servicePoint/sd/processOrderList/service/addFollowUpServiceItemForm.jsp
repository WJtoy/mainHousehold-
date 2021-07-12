<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>服务明细for待回访</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<meta name="remark" content="添加上门服务">
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
	<script src="${ctxStatic}/sd/OrderService.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
	<script type="text/javascript">
		<%String parentIndex = request.getParameter("parentIndex");%>
		var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
		OrderService.rootUrl = "${ctx}";
		var this_index = top.layer.index;
		var clickTag = 0;
		$(document).ready(function() {
			$('#otherActionRemark').keyup(function() {
				var len=this.value.length;
				$('#otherActionRemark_cnt').text(len);
			});
			$("#btnSubmit").bind("click",function(){$("#inputForm").submit();});
			$("#inputForm").validate({
				rules: {
					qty: {required: true, min: 1},
					materialCharge: {min: 0, max: 10000.0},
					travelCharge: {min: 0, max: 500.0},
					expressCharge: {min: 0, max: 500},
					otherCharge: {min: 0, max: 1000},
					engineerMaterialCharge: {min: 0, max: 10000.0},
					engineerOtherCharge: {min: 0, max: 1000},
					engineerTravelCharge: {min: 0, max: 500},
					engineerExpressCharge: {min: 0, max: 500}
				},
				messages: {
					qty: {required: "请输入数量", min: "数量必须大于0"},
					materialCharge: {min: "应收配件费不能小于0", max: "应收配件费不能超过1万元"},
					travelCharge: {min: "应收远程费不能小于0", max: "应收远程费不能超过500元"},
					expressCharge: {min: "应收快递费不能小于0", max: "应收快递费不能超过500元"},
					otherCharge: {min: "应收其他费用不能小于0", max: "应收其他费用不能超过1000元"},
					engineerMaterialCharge: {min: "应付配件费不能小于0", max: "应付配件费不能超过1万元"},
					engineerOtherCharge: {min: "应付其他费用不能小于0", max: "应付其他费用不能超过1000元"},
					engineerTravelCharge: {min: "应付远程费不能小于0", max: "应付远程费不能超过500元"},
					engineerExpressCharge: {min: "应付快递费不能小于0", max: "应付快递费不能超过500元"}
				},
				submitHandler: function(form){
					if(clickTag == 1){
						return false;
					}
					clickTag = 1;
					var $btnSubmit =  $("#btnSubmit");
					$btnSubmit.attr('disabled', 'disabled');
					if(false === OrderService.checkSubmitInput()){
						$btnSubmit.removeAttr('disabled');
						clickTag = 0;
						return false;
					}
					var loadingIndex;
					$.ajax({
						async: false,
						cache: false,
						type: "POST",
						url: "${ctx}/sd/order/kefuOrderList/service/saveServiceForFollowUp?"+ (new Date()).getTime(),
						data:$(form).serialize(),
						beforeSend: function () {
							loadingIndex = layer.msg('正在提交，请稍等...', {
								icon: 16,
								time: 0,
								shade: 0.3
							});
						},
						complete: function () {
							if(loadingIndex) {
								layer.close(loadingIndex);
							}
							setTimeout(function () {
								clickTag = 0;
								$btnSubmit.removeAttr('disabled');
							}, 2000);
						},
						success: function (data) {
							if(ajaxLogout(data)){
								return false;
							}
							if(data && data.success == true){
								// 刷新订单详情页面
								if(parentIndex != '') {
									var layero = $("#layui-layer" + parentIndex, top.document);
									var iframeWin = top[layero.find('iframe')[0]['name']];
									iframeWin.reload('tabService');
								}
								top.layer.close(this_index);
								return false;
							}
							else if( data && data.message){
								layer.alert(data.message, {icon: 2, title: "错误提示"});
							}
							else{
								layer.alert("添加订单明细错误", {icon: 2, title: "错误提示"});
							}
							return false;
						},
						error: function (e) {
							ajaxLogout(e.responseText,null,"添加订单明细错误，请重试!");
						}
					});
					return false;
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

			var orderServiceType = $("#orderServiceType").val();
			if(orderServiceType){
				$("[name='serviceCategory.value']:checked").trigger("change");
			}
		});

		//上门服务功能中产品选择(select2)后回调方法
		function serviceproduct_callback(data){
			$("#brand").val(data.brand);
			$("#productSpec").val(data.model);
			//clear data
			$("input:radio[name='serviceCategory.value']").filter('[value='+data.orderservietype+']').prop('checked', true);
			$("[name='serviceCategory.value']:checked").trigger('change');
			$("#brand").focus();
		}

		function closeme(){
			top.layer.close(this_index);
		}
		$(document).ready(function () {
			$('a[data-toggle=tooltip]').darkTooltip();
			$('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
			$('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
		});
	</script>
	<style type="text/css">
		body {background-color:#fff;}
		html {color:#000;}
		.form-horizontal {margin: 0px 0px;}
		.limitAlert {color:#ffa528;padding-top:6px;padding-bottom:6px;}
	</style>
</head>
<body style="display:inline">
<sys:message content="${message}"/>
<c:if test="${canService == 'true'}">
	<form:form id="inputForm" modelAttribute="item" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="orderId"/>
		<form:hidden path="addType"/>
		<form:hidden path="engineer.id"/>
		<form:hidden path="servicePoint.id"/>
		<input type="hidden" id="customerId" name="customerId" value="${customerId}" />
		<input type="hidden" id="dataSource" name="dataSource" value="${dataSource}" />
		<input type="hidden" id="orderServiceType" name="orderServiceType" value="${item.serviceCategory.value}" />
		<form:hidden path="hasErrorType"/>
		<input type="hidden" id="limitRemoteCharge" name="limitRemoteCharge" value="${limitRemoteCharge}"/>
		<input type="hidden" id="areaRemoteFee" name="areaRemoteFee" value="${areaRemoteFee}"/>
		<table border="0">
			<tr>
				<td colspan="2">
					<div class="span6">
						<div class="control-group">
							<label class="control-label">上门次数:</label>
							<div class="controls">
								<c:forEach begin="1" end="${item.orderServiceTimes}" step="1" varStatus="i">
									<span>
										<c:choose>
											<c:when test="${ i.index == 0}">
												&nbsp;
											</c:when>
											<c:otherwise>
												<c:choose>
													<c:when test="${ (!empty item.id && i.index == item.serviceTimes) || (empty id && i.index == item.orderServiceTimes) }">
														<input id="serviceTims${i.index}" name="serviceTimes" class="required" type="radio" value="${i.index}" checked="checked">
														<label for="isShow${i.index}">第${i.index}次</label>
													</c:when>
													<c:otherwise>
														<input id="serviceTims${i.index}" name="serviceTimes" class="required" type="radio" value="${i.index}">
														<label for="isShow${i.index}">第${i.index}次</label>
													</c:otherwise>
												</c:choose>
											</c:otherwise>
										</c:choose>
									</span>
								</c:forEach>
							</div>
						</div>
					</div>
				</td>
			</tr>
			<tr>
				<td><div class="span6">
					<div class="control-group">
						<label class="control-label">产品:</label>
						<div class="controls">
							<sd:orderProductSelect id="product" name="product.id" value="${item.product.id}" labelName="product.name" labelValue="${item.product.name}"
												   width="700" height="520" callbackmethod="serviceproduct_callback" orderId="${item.orderId}" quarter="${order.quarter}"
												   title="产品" cssClass="input-block-level required"/>
						</div>
					</div>
				</div></td>
				<td><div class="span6">
					<div class="control-group">
						<label class="control-label">品牌:</label>
						<div class="controls">
							<form:input path="brand" htmlEscape="false" cssClass="input-block-level" maxlength="100" />
						</div>
					</div>
				</div></td>
			</tr>
			<tr>
				<td><div class="span6">
					<div class="control-group">
						<label class="control-label">规格/型号:</label>
						<div class="controls">
							<form:input path="productSpec" htmlEscape="false" cssClass="input-block-level" maxlength="100" />
						</div>
					</div>
				</div></td>
				<td>
					<div class="span6">
						<div class="control-group">
							<label class="control-label">服务类型:</label>
							<div class="controls">
								<form:radiobuttons path="serviceCategory.value" items="${serviceCategories}" itemLabel="label" itemValue="value" htmlEscape="false" class="input-block-level required"/>
								<span class=" red">*</span>
							</div>
						</div>
					</div>
				</td>
			</tr>
			<tr class="tr_repair">
				<td>
					<div class="span6">
						<div class="control-group">
							<label class="control-label">故障分类:</label>
							<div class="controls">
								<form:select path="errorType.id" class="input-block-level">
									<form:option value="0" label="请选择"/>
								</form:select>
								<form:hidden path="errorType.name"/>
							</div>
						</div>
					</div>
				</td>
				<td>
					<div class="span6">
						<div class="control-group">
							<label class="control-label">故障现象:</label>
							<div class="controls">
								<form:select path="errorCode.id" class="input-block-level">
									<form:option value="0" label="请选择"/>
								</form:select>
								<form:hidden path="errorCode.name"/>
							</div>
						</div>
					</div>
				</td>
			</tr>
			<tr class="tr_repair">
				<td colspan="2">
					<div class="span12">
						<div class="control-group">
							<label class="control-label">故障处理:</label>
							<div class="controls">
								<form:select path="actionCode.id" class="input-block-level">
									<form:option value="0" label="请选择"/>
								</form:select>
								<form:input path="actionCode.name" cssClass="input-block-level" maxlength="255" cssStyle="display: none;"/>
							</div>
						</div>
					</div>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<div class="span12">
						<div class="control-group">
							<label id="lbl_repair_other" style="display: none;" class="control-label">其他故障&nbsp;<br/>维修说明:</label>
							<label id="lbl_install_other" class="control-label">安装说明:</label>
							<div class="controls">
								<form:textarea path="otherActionRemark" rows="3" htmlEscape="false" cssClass="input-block-level" maxlength="250" />
								<p class="text-error"><span id="otherActionRemark_cnt">0</span>/250</p>
							</div>
						</div>
					</div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="span6">
						<div class="control-group">
							<label class="control-label">服务项目:</label>
							<div class="controls">
								<form:select path="serviceType.id" class="input-block-level">
									<form:option value="" label="请选择"/>
								</form:select>
								<form:hidden path="serviceType.name"/>
							</div>
						</div>
					</div>
				</td>
				<td><div class="span6">
					<div class="control-group">
						<label class="control-label">数量:</label>
						<div class="controls">
							<form:input path="qty" type="number" htmlEscape="false" maxlength="4"  cssClass="input-block-level required digits"/>
						</div>
					</div>
				</div></td>
				<td>

				</td>
			</tr>
			<tr>
				<td><div class="span6">
					<legend>应付</legend>
				</div></td>
				<td><div class="span6">
					<legend>应收</legend>
				</div></td>
			</tr>
			<c:if test="${limitRemoteCharge != null && !(areaRemoteFee!=null && areaRemoteFee==0)}">
				<td>
					<div class="span6 limitAlert">
						<c:choose>
							<c:when test="${limitRemoteCharge>0}">
								提示: 远程费用和其他费用合计不能超过${limitRemoteCharge}元，否则将不能添加上门服务! 请确认是否操作退单!
							</c:when>
							<c:otherwise>
								提示: 该客户订单不允许加远程费用和其他费用，请确认是否操作退单!
							</c:otherwise>
						</c:choose>
					</div>
				</td>
				<td></td>
			</c:if>
			<tr>
				<td><div class="span6">
					<div class="control-group">
						<label class="control-label">配件费：</label>
						<div class="controls">
							<form:input path="engineerMaterialCharge" htmlEscape="false" type="number" maxlength="11" class="input-block-level required number"/>
						</div>
					</div>
				</div></td>
				<td><div class="span6">
					<div class="control-group">
						<label class="control-label">配件费：</label>
						<div class="controls">
							<form:input path="materialCharge" htmlEscape="false" type="number" maxlength="11" class="input-block-level required number"/>
						</div>
					</div>
				</div></td>
			</tr>
			<tr>
				<td><div class="span6">
					<div class="control-group">
						<label class="control-label">其他：</label>
						<div class="controls">
							<c:choose>
								<c:when test="${order.orderFee.planOtherCharge ==0.0 && areaRemoteFee!=null && areaRemoteFee==0}">
									<input type="number" id="engineerOtherCharge" name="engineerOtherCharge" readonly="readonly" value="${order.orderFee.planOtherCharge}" maxlength="11" class="input-block-level required number" />
									<c:if test="${order.orderFee.planOtherCharge ne null}">
										<label>预设网点其他费用:<font color="red">${order.orderFee.planOtherCharge}</font></label>
									</c:if>
									<c:if test="${areaRemoteFee!=null && areaRemoteFee==0}">
										&nbsp;<a data-toggle="tooltip" data-tooltip="区域未设置远程费"><i class="icon-question-sign" style="color: red"></i></a>
									</c:if>
								</c:when>
								<c:when test="${limitRemoteCharge != null && limitRemoteCharge <= 0}">
									<input type="number" id="engineerOtherCharge" name="engineerOtherCharge" readonly="readonly" value="${order.orderFee.planOtherCharge}" maxlength="11" class="input-block-level required number" />
								</c:when>
								<c:otherwise>
									<input type="number" id="engineerOtherCharge" name="engineerOtherCharge" value="${order.orderFee.planOtherCharge}" maxlength="11" class="input-block-level required number" />
									<c:if test="${order.orderFee.planOtherCharge ne null}">
										<label>预设网点其他费用:<font color="red">${order.orderFee.planOtherCharge}</font></label>
									</c:if>
									<c:if test="${areaRemoteFee!=null && areaRemoteFee==0}">
										&nbsp;<a data-toggle="tooltip" data-tooltip="区域未设置远程费"><i class="icon-question-sign" style="color: red"></i></a>
									</c:if>
								</c:otherwise>
							</c:choose>
						</div>
					</div>
				</div>
				</td>
				<td><div class="span6">
					<div class="control-group">
						<label class="control-label">其他：</label>
						<div class="controls">
							<c:choose>
								<c:when test="${item.otherCharge ==0.0 && customerRemoteFee!=null && customerRemoteFee==0}">
									<form:input path="otherCharge" htmlEscape="false" type="number" maxlength="11" class="input-block-level required number" readonly="true" cssStyle="width: 250px"/>
									<c:if test="${customerRemoteFee!=null && customerRemoteFee==0}">
										&nbsp;<a data-toggle="tooltip" data-tooltip="厂商未设置远程费"><i class="icon-question-sign" style="color: red"></i></a>
									</c:if>
								</c:when>
								<c:when test="${limitRemoteCharge != null}">
									<%--远程费和其他费用合计限制品类--%>
									<input type="text" id="otherCharge" name="otherCharge" readonly="readonly" value="${order.orderFee.customerPlanOtherCharge}" class="input-block-level required number" />
								</c:when>
								<c:otherwise>
									<form:input path="otherCharge" htmlEscape="false" type="number" maxlength="11" class="input-block-level required number"/>
									<c:if test="${customerRemoteFee!=null && customerRemoteFee==0}">
										&nbsp;<a data-toggle="tooltip" data-tooltip="厂商未设置远程费"><i class="icon-question-sign" style="color: red"></i></a>
									</c:if>
								</c:otherwise>
							</c:choose>
						</div>
					</div>
				</div></td>
			</tr>
			<tr>
				<td><div class="span6">
					<div class="control-group">
						<label class="control-label">远程费：</label>
						<div class="controls">
							<c:choose>
								<c:when test="${order.orderFee.planTravelCharge ==0.0 && areaRemoteFee!=null && areaRemoteFee==0}">
									<input type="number" id="engineerTravelCharge" name="engineerTravelCharge" readonly="readonly" value="${order.orderFee.planTravelCharge}" maxlength="11" class="input-block-level required number" />
									<c:if test="${order.orderFee.planTravelCharge ne null}">
										<label>补助费用:<font color="red">${order.orderFee.planTravelCharge }</font></label>
									</c:if>
									<c:if test="${areaRemoteFee!=null && areaRemoteFee==0}">
										&nbsp;<a data-toggle="tooltip" data-tooltip="区域未设置远程费"><i class="icon-question-sign" style="color: red"></i></a>
									</c:if>
								</c:when>
								<c:when test="${limitRemoteCharge != null && limitRemoteCharge <= 0}">
									<input type="number" id="engineerTravelCharge" name="engineerTravelCharge" readonly="readonly" value="0.0" maxlength="11" class="input-block-level required number" />
								</c:when>
								<c:otherwise>
									<input type="number" id="engineerTravelCharge" name="engineerTravelCharge" value="${order.orderFee.planTravelCharge}" maxlength="11" class="input-block-level required number" />
									<c:if test="${order.orderFee.planTravelCharge ne null}">
										<label>补助费用:<font color="red">${order.orderFee.planTravelCharge }</font></label>
									</c:if>
									<c:if test="${areaRemoteFee!=null && areaRemoteFee==0}">
										&nbsp;<a data-toggle="tooltip" data-tooltip="区域未设置远程费"><i class="icon-question-sign" style="color: red"></i></a>
									</c:if>
								</c:otherwise>
							</c:choose>
						</div>
					</div>
				</div>
				</td>
				<td><div class="span6">
					<div class="control-group">
						<label class="control-label" >远程费：</label>
						<div class="controls">
							<c:choose>
								<c:when test="${order.orderFee.customerPlanTravelCharge == 0.0 && customerRemoteFee!=null && customerRemoteFee==0}">
									<input type="text" id="travelCharge" name="travelCharge" readonly="readonly" value="${order.orderFee.customerPlanTravelCharge}" class="input-block-level required number" />
									<c:if test="${order.orderFee.customerPlanTravelCharge ne null}">
										<label>预设厂商远程费:<font color="red">${order.orderFee.customerPlanTravelCharge }</font></label>
									</c:if>
									<c:if test="${customerRemoteFee!=null && customerRemoteFee==0}">
										&nbsp;<a data-toggle="tooltip" data-tooltip="厂商未设置远程费"><i class="icon-question-sign" style="color: red"></i></a>
									</c:if>
								</c:when>
								<c:when test="${limitRemoteCharge != null}">
									<%--远程费和其他费用合计限制品类--%>
									<input type="text" id="travelCharge" name="travelCharge" readonly="readonly" value="${order.orderFee.customerPlanTravelCharge}" class="input-block-level required number" />
								</c:when>
								<c:otherwise>
									<input type="text" id="travelCharge" name="travelCharge" value="${order.orderFee.customerPlanTravelCharge}" class="input-block-level required number" />
									<c:if test="${order.orderFee.customerPlanTravelCharge ne null}">
										<label>预设厂商远程费:<font color="red">${order.orderFee.customerPlanTravelCharge }</font></label>
									</c:if>
									<c:if test="${customerRemoteFee!=null && customerRemoteFee==0}">
										&nbsp;<a data-toggle="tooltip" data-tooltip="厂商未设置远程费"><i class="icon-question-sign" style="color: red"></i></a>
									</c:if>
								</c:otherwise>
							</c:choose>
						</div>
					</div>
				</div></td>
			</tr>

			<tr>
				<td><div class="span6">
					<div class="control-group">
						<label class="control-label">审核单号：</label>
						<div class="controls">
							<c:choose>
								<c:when test="${limitRemoteCharge != null && limitRemoteCharge <= 0}">
									<input type="text" id="travelNo" name="travelNo" readonly="readonly" value="" maxlength="11" class="input-block-level" />
								</c:when>
								<c:otherwise>
									<input type="text" id="travelNo" name="travelNo" value="${order.orderFee.planTravelNo}" maxlength="11" class="input-block-level" />
								</c:otherwise>
							</c:choose>
						</div>
					</div>
				</div>
				</td>
				<td><div class="span6">
				</div></td>
			</tr>
			<tr>
				<td><div class="span6">
					<div class="control-group">
						<label class="control-label">快递费：</label>
						<div class="controls">
							<form:input path="engineerExpressCharge" htmlEscape="false" type="number" maxlength="11" class="input-block-level required number"/>
						</div>
					</div>
				</div>
				</td>
				<td><div class="span6">
					<div class="control-group">
						<label class="control-label">快递费：</label>
						<div class="controls">
							<form:input path="expressCharge" htmlEscape="false" type="number" maxlength="11" class="input-block-level required number"/>
						</div>
					</div>
				</div></td>
			</tr>
			<tr>
				<td colspan="2">
					<div class="span12">
						<div class="control-group">
							<label class="control-label">备注:</label>
							<div class="controls">
								<form:textarea path="remarks" htmlEscape="false" rows="1" maxlength="100" class="input-block-level" />
							</div>
						</div>
					</div>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<div class="control-group">
						<label class="control-label"></label>
						<div class="controls">
							<shiro:hasPermission name="sd:order:service"><input id="btnSubmit" class="btn btn-primary" type="button" value="保存"/>&nbsp;</shiro:hasPermission>
							<input id="btnCancel" class="btn" type="button" value="关闭" onclick="closeme();"/>

						</div>
					</div>
				</td>
			</tr>
		</table>
	</form:form>
</c:if>
</body>
</html>