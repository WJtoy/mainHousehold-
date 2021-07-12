<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>订单详细信息(财务)</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default" />
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
	<%@include file="/WEB-INF/views/include/dialog.jsp"%>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/modules/sd/service/tpl/orderDetail.html" %>
	<style type="text/css">
	.table thead th,.table tbody td {text-align: center;vertical-align: middle;}
	.tdlable {width:160px;align: right;}
	.tdbody {width:300px;}
	.table th,.table td {padding: 4px;}
	.table thead th {text-align: center;vertical-align: middle;}
	.table .tdcenter {text-align: center;vertical-align: middle;}
	.alert {padding: 4px 5px 4px 4px;}
	</style>

<script type="text/javascript">
    Order.rootUrl = "${ctx}";
    var this_index = top.layer.index;
    $(document).ready(function(){
        $('a[data-toggle=tooltip]').darkTooltip({gravity : 'north'});
	});

    //设置异常
    $(document).off('click','#btnExceptionConfirm');//先解除事件绑定
    $(document).on("click", "#btnExceptionConfirm", function () {
		if ($("#btnExceptionConfirm").prop("disabled") == true) {
			return false;
		}

        var remarks = $("#remarks").val()
        var auditType =$('input:radio[name="auditType"]:checked').val();
        if(auditType == null || auditType == ''){
            layerError("请勾选异常类型");
            return false;
        }
        $("#btnExceptionConfirm").prop("disabled", true);

        var loadingIndex = layerLoading("正在提交，请稍等...");
        $.ajax({
            type: "POST",
            url: "${ctx}/fi/chargecreate/pending",
            data: {ids:'${order.id}', remarks:remarks,auditType:auditType},
            success: function (data) {
                setTimeout(function() {
					if (data.success){
							var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
							parent.layer.close(loadingIndex);
							parent.layer.close(index);
							layerMsg('标记异常成功');
							var iframe = getActiveTabIframe();//定义在jeesite.min.js中
							if(iframe != undefined){
								iframe.repage();
							}
					}
					else{
						parent.layer.close(loadingIndex);
						layerError(data.message);
					}
					$("#btnExceptionConfirm").removeAttr("disabled");
                }, 500);
                return false;
            },
            error: function (e) {
                $("#btnExceptionConfirm").removeAttr("disabled");
                layerError("标记异常错误:"+e,"错误提示");
            }
        });
        return false;

    });

    $(document).off('click', '#btnChargeConfirm');
    $(document).on('click', '#btnChargeConfirm', function(){
        // layerMsg("ddddd");
        if ($("#btnChargeConfirm").prop("disabled") == true) {
            return false;
		}
		$("#btnChargeConfirm").prop("disabled", true);

        var loadingIndex = layerLoading("正在提交，请稍等...");
        $.ajax({
            type: "POST",
            url: "${ctx}/fi/chargecreate/save",
            data: {ids:'${order.id}'},
            success: function (data) {
                setTimeout(function() {
					if (data.success){
						var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
						parent.layer.close(loadingIndex);
						parent.layer.close(index);
						layerMsg('生成对帐单成功');
						var iframe = getActiveTabIframe();//定义在jeesite.min.js中
						if(iframe != undefined){
							iframe.repage();
						}
					}
					else{
						parent.layer.close(loadingIndex);
						layerError(data.message);
					}
					$("#btnChargeConfirm").removeAttr("disabled");
                }, 500);
                return false;
            },
            error: function (e) {
                $("#btnChargeConfirm").removeAttr("disabled");
                layerError("生成对账单失败:"+e,"错误提示");
            }
        });
        return false;
	});

    $(document).off('click', '#btnCancel');
    $(document).on('click', '#btnCancel', function(){
        var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
        parent.layer.close(index);
        return false;
    });
</script>
</head>
<body>
	<!-- new -->
	<form:form id="inputForm" action="#" method="post" class="form-horizontal">
		<a id="aSave" name="ids" type="hidden"></a>
		<sys:message content="${message}" />
		<c:if test="${errorFlag == false}">
			<c:set var="cuser" value="${fns:getUser()}" />
			<c:set var="isCustomer" value="${!empty cuser && cuser.isCustomer()?true:false }" />
			<input type="hidden" id="isCustomer" name="isCustomer" value="${isCustomer}" />
		<c:set var="cancopy" value="${order.canService()}" />
		<!-- order head -->
		<div class="accordion-group" style="margin-top:2px;">
			<div class="accordion-heading">
				<a href="#divheader" class="accordion-toggle" data-toggle="collapse">基本信息
					<span class="arrow"></span>
				</a>
			</div>
			<div id="divheader" class="accordion-body">
				<table class="table table-bordered table-striped table-hover" style="margin-bottom: 0px;">
					<tbody>
						<tr>
							<td class="tdlable">
								<label class="control-label">工单编号:</label>
							</td>
							<td class="tdbody">
								<span id="spOrderNo">${order.orderNo}</span>
								<a class="btn btn-primary" id="btnFinishPhoto" href="javascript:;" onclick="Order.photolistNew('${order.id}','${order.quarter}',${fns:isNewOrder(order.orderNo)});" title="点击查看完成照片"><i class="icon-camera icon-white"></i> 完成照片</a>
							</td>
							<td class="tdlable">
								<label class="control-label">客户:</label>
							</td>
							<td class="tdbody">
								<a href="javascript:" data-toggle="tooltip" data-tooltip="${order.orderCondition.customer.remarks}">${order.orderCondition.customer.name}</a>
							</td>
						</tr>
						<tr>
							<td class="tdlable"><label class="control-label">用户:</label></td>
							<td class="tdbody">${order.orderCondition.userName}</td>
							<td class="tdlable"><label class="control-label">联络电话:</label></td>
							<td class="tdbody">${order.orderCondition.servicePhone}</td>
						</tr>
						<tr>
							<td class="tdlable"><label class="control-label">上门地址:</label></td>
							<td class="tdbody" colspan="3">${order.orderCondition.area.name}${order.orderCondition.serviceAddress}</td>
						</tr>
						<tr>
							<td class="tdlable"><label class="control-label">服务描述:</label></td>
							<td class="tdbody" colspan="3">${order.description}</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
		<c:set var="cuser" value="${fns:getUser()}" />

		<shiro:hasAnyPermissions name="sd:order:showserviceinfo,sd:order:showengineerinfo">
			<div class="accordion-group" style="margin-top:2px;">
				<div class="accordion-heading">
					<a href="#divservice" class="accordion-toggle"
						data-toggle="collapse">服务信息 <span class="arrow"></span>
					</a>
				</div>
				<c:set var="service" value="${order.orderCondition.kefu}" />
				<div id="divservice" class="accordion-body">
					<table class="table table-bordered table-striped table-hover"
						style="margin-bottom: 0px;">
						<tbody>
							<shiro:hasPermission name="sd:order:showserviceinfo">
								<tr>
									<td class="tdlable"><label class="control-label">客服:</label>
									</td>
									<td class="tdbody">${empty service?'':order.orderCondition.kefu.name}
										<c:if test="${!empty order.orderCondition.kefu.qq}">
										<a style="padding-left: 20px;" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=${order.orderCondition.kefu.qq}&site=qq&menu=yes">
											<img border="0" src="http://wpa.qq.com/pa?p=2:572202493:52"
											alt="点击这里给我发消息" title="联系客服QQ：${order.orderCondition.kefu.qq}" />
										</a>
										</c:if>
									</td>
									<td class="tdlable"><label class="control-label">业务员:</label>
									</td>
									<td class="tdbody">${order.orderCondition.customer.sales.name}
										<c:if test="${!empty order.orderCondition.customer.sales.qq}">
											<a style="padding-left: 20px;" target="_blank"
											   href="http://wpa.qq.com/msgrd?v=3&uin=${order.orderCondition.customer.sales.qq}&site=qq&menu=yes"><img
													border="0" src="http://wpa.qq.com/pa?p=2:572202493:52" alt="点击这里给我发消息" title="联系业务员QQ：${order.orderCondition.customer.sales.qq}" />
											</a>
										</c:if>
									</td>
								</tr>
							</shiro:hasPermission>
							<shiro:hasPermission name="sd:order:showengineerinfo">
								<tr>
									<td class="tdlable"><label class="control-label">网点编号:</label>
									</td>
									<td class="tdbody">${order.orderCondition.servicePoint.servicePointNo} - ${order.orderFee.engineerPaymentType.label}
										<c:if test="${!empty order.orderCondition.servicePoint.finance.bankIssue && !empty order.orderCondition.servicePoint.finance.bankIssue.label }">
											<label id="lblServicePointNo" class="alert alert-error">${order.orderCondition.servicePoint.finance.bankIssue.label }</label>
										</c:if>
									</td>
									<td class="tdlable"><label class="control-label">安维师傅:</label>
									</td>
									<td class="tdbody">
											${order.orderCondition.engineer.name}
										<c:if test="${not empty order.orderCondition.engineer}">
											<c:choose>
												<c:when test="${order.orderCondition.engineer.subFlag==0}">(主)</c:when>
												<c:otherwise>(子)</c:otherwise>
											</c:choose>
										</c:if>
									</td>
								</tr>
							</shiro:hasPermission>
						</tbody>
					</table>
				</div>
			</div>
		</shiro:hasAnyPermissions>
		</c:if>
	</form:form>
    <c:if test="${errorFlag == false}">
		<div class="tabbable" style="margin:0px 20px">
			<ul class="nav nav-tabs">
				<li class="active"><a href="#tab1" data-toggle="tab">产品详细清单</a></li>
				<c:if test="${order.orderCondition.status.value !='90' && order.orderCondition.status.value !='100' && order.orderCondition.serviceTimes>0}">
				<li><a href="#tabService" data-toggle="tab">实际上门清单</a></li>
				</c:if>
				<li><a href="#tabTracking" data-toggle="tab" id="tab4link" onclick="Order.showTrackingLogs('${order.id}','${order.quarter}','${isCustomer}');">跟踪进度</a></li>
				<shiro:hasPermission name="sd:orderdetail:exception">
					<li><a href="#tabException" data-toggle="tab" id="tab5link" onclick="Order.showExceptLogs('${order.id}','${order.quarter}');">异常处理</a></li>
				</shiro:hasPermission>
				<c:if test="${order.orderCondition.feedbackId != 0}">
				<li><a href="#tabFeedback" data-toggle="tab" onclick="Order.showFeedbackLogs('${order.orderCondition.feedbackId}','${order.quarter}');">反馈处理</a></li>
				</c:if>
				<c:if test="${order.writeOff == 1 or order.writeOff == 3}">
					<li><a href="#tabCustomerReturn" data-toggle="tab" onclick="Order.showCustomerReturnAndAdditionalList('${order.id}','${order.orderNo}','${order.quarter}');">退补</a></li>
				</c:if>
			</ul>
			<div class="tab-content">
				<div class="tab-pane active" id="tab1" title="产品详细清单">
					<div class="control-group">
						<table id="productTable"
							class="table table-striped table-bordered table-condensed table-hover"
							style="margin-bottom: 0px;">
							<thead>
								<tr>
									<th width=30px>序号</th>
									<th>服务类型</th>
									<th>产品</th>
									<th>品牌</th>
									<th>型号/规格</th>
									<th>数量</th>
									<shiro:hasPermission name="sd:order:showreceive">
										<th>服务金额</th>
										<%--<th>冻结金额</th>--%>
										<%--<th>合计</th>--%>
									</shiro:hasPermission>
									<th>备注</th>
							</thead>
							<tbody>
								<c:set var="ridx" value="0"/>
								<c:set var="totalQty" value="0"/>
								<c:forEach items="${order.items}" var="item">
									<tr>
										<td>${ridx+1}</td>
										<td>${item.serviceType.name }</td>
										<td>${item.product.name }</td>
										<td>${item.brand }</td>
										<td>${item.productSpec }</td>
										<td>${item.qty }</td>
										<shiro:hasPermission name="sd:order:showreceive">
											<td>${item.charge }</td>
											<%--<td>${item.blockedCharge }</td>--%>
											<%--<td>${item.charge + item.blockedCharge}</td>--%>
										</shiro:hasPermission>
										<td><c:if test="${!empty item.expressCompany }">
											<a href="http://www.kuaidi100.com/chaxun?com=${item.expressCompany.value}&nu=${item.expressNo }"
											   target="_blank" title="点击进入快递100">
												${item.expressCompany.label}&nbsp;&nbsp;${item.expressNo}
											</a>
											</c:if>
										</td>
									</tr>
									<c:set var="ridx" value="${ridx+1}" />
									<c:set var="totalQty" value="${totalQty+item.qty}" />
								</c:forEach>
								<c:if test="${cuser.userType ne 2 && order.items.size()>0}">
									<tr>
										<td style="text-align:right; " colspan="5">
											<span  class="alert alert-success">合计</span>
										</td>
										<td class="tdcenter"><span class="alert alert-success">${totalQty}</span></td>
										<shiro:hasPermission name="sd:order:showreceive">
											<td class="tdcenter"><span class="alert alert-success">${order.orderFee.expectCharge}</span></td>
											<%--<td><label class="alert alert-success">${order.orderFee.blockedCharge}</label></td>--%>
											<%--<td><label class="alert alert-success">${order.orderFee.expectCharge+order.orderFee.blockedCharge}</label></td>--%>
										</shiro:hasPermission>
										<td></td>
									</tr>
								</c:if>
							</tbody>
						</table>
					</div>
				</div>
				<div class="tab-pane" id="tabService" title="实际上门清单">
					<div class="control-group">
						<table id="serviceTable"
							class="table table-striped table-bordered table-condensed table-hover"
							style="margin-bottom: 0px;">
							<thead>
								<tr>
									<th rowspan="2" width="30px">序号</th>
									<th rowspan="2" width="100px">日期</th>
									<th rowspan="2" width="60px">上门次数</th>
									<th rowspan="2" width="60px">服务类型</th>
									<th rowspan="2">产品</th>
									<th rowspan="2">品牌</th>
									<th rowspan="2">型号/规格</th>
									<th rowspan="2">数量</th>
									<shiro:hasPermission name="sd:order:showreceive">
										<c:choose>
											<c:when test="${isCustomer eq true}">
												<th colspan="7">应付款</th>
											</c:when>
											<c:otherwise>
												<th colspan="6">应收款</th>
											</c:otherwise>
										</c:choose>
									</shiro:hasPermission>
									<shiro:hasPermission name="sd:order:showpayment">
										<th colspan="7">应付款</th>
										<th rowspan="2">备注</th>

									</shiro:hasPermission>
								</tr>
								<tr>
									<shiro:hasPermission name="sd:order:showreceive">
										<th>服务费</th>
										<th>配件费</th>
										<th>快递费</th>
										<th>远程费</th>
										<th>其他</th>
										<th>小计</th>
									</shiro:hasPermission>
									<shiro:hasPermission name="sd:order:showpayment">
										<th>服务费</th>
										<th>配件费</th>
										<th>快递费</th>
										<th>远程费</th>
										<th>其他</th>
										<th>小计</th>
										<th>安维</th>
									</shiro:hasPermission>
								</tr>
							</thead>
							<tbody>
								<c:if test="${order != null && order.detailList != null}">
								<c:set var="totalQty" value="${0}" />
								<c:set var="ridx" value="${0}" />
								<c:set var="charge1" value="${0.00}" />
								<c:set var="charge2" value="${0.00}" />
								<c:set var="materialCharge1" value="${0.00}"  />
								<c:set var="materialCharge2" value="${0.00}" />
								<c:set var="expressCharge1" value="${0.00}" />
								<c:set var="expressCharge2" value="${0.00}" />
								<c:set var="travelCharge1" value="${0.00}" />
								<c:set var="travelCharge2" value="${0.00}" />
								<c:set var="otherCharge1" value="${0.00}" />
								<c:set var="otherCharge2" value="${0.00}" />
								<c:set var="totalCharge1" value="${0.00}" />
								<c:set var="totalCharge2" value="${0.00}" />
								<c:forEach items="${order.detailList}" var="item">
									<tr>
										<td class="tdcenter">${ridx+1}</td>
										<td class="tdcenter">${fns:formatDate(item.createDate,'yyyy-MM-dd HH:mm')}</td>
										<td class="tdcenter">${item.serviceTimes}</td>
										<td>${item.serviceType.name}</td>
										<td>${item.product.name}</td>
										<td>${item.brand}</td>
										<td>${item.productSpec}</td>
										<td class="tdcenter">${item.qty}</td>
										<c:set var="totalQty" value="${totalQty+item.qty}" />
										<shiro:hasPermission name="sd:order:showreceive">
											<td class="tdcenter"><c:if test="${item.charge ne 0}">${item.charge}</c:if></td>
											<td class="tdcenter"><c:if test="${item.materialCharge ne 0}">${item.materialCharge}</c:if></td>
											<td class="tdcenter"><c:if test="${item.expressCharge ne 0}">${item.expressCharge}</c:if></td>
											<td class="tdcenter"><c:if test="${item.travelCharge ne 0}">${item.travelCharge}</c:if></td>
											<td class="tdcenter"><c:if test="${item.otherCharge ne 0}">${item.otherCharge}</c:if></td>
											<td class="tdcenter"><b><c:if test="${item.customerCharge ne 0}">${item.customerCharge}</c:if></b>
											</td>
											<c:set var="charge1" value="${charge1 + item.charge}" />
											<c:set var="materialCharge1" value="${materialCharge1 + item.materialCharge}" />
											<c:set var="expressCharge1" value="${expressCharge1 + item.expressCharge}" />
											<c:set var="travelCharge1" value="${travelCharge1 + item.travelCharge}" />
											<c:set var="otherCharge1" value="${otherCharge1 + item.otherCharge}" />
											<c:set var="totalCharge1" value="${totalCharge1 + item.customerCharge}" />
										</shiro:hasPermission>
										<shiro:hasPermission name="sd:order:showpayment">
											<td class="tdcenter"><c:if test="${item.engineerServiceCharge ne 0}">${item.engineerServiceCharge}</c:if></td>
											<td class="tdcenter"><c:if test="${item.engineerMaterialCharge ne 0}">${item.engineerMaterialCharge}</c:if></td>
											<td class="tdcenter"><c:if test="${item.engineerExpressCharge ne 0}">${item.engineerExpressCharge}</c:if></td>
											<td class="tdcenter"><c:if test="${item.engineerTravelCharge ne 0}">${item.engineerTravelCharge}</c:if>
												<c:if test="${!empty item.travelNo}">签核单号:${item.travelNo}</c:if>
											</td>
											<td class="tdcenter"><c:if test="${item.engineerOtherCharge ne 0}">${item.engineerOtherCharge}</c:if></td>
											<td class="tdcenter"><b><c:if test="${item.engineerChage ne 0}">${item.engineerChage}</c:if></b>
											</td>
											<td class="tdcenter">${item.engineer.name}</td>
											<td class="tdcenter">${item.remarks}</td>
											<c:set var="charge2" value="${charge2 + item.engineerServiceCharge}" />
											<c:set var="materialCharge2" value="${materialCharge2 + item.engineerMaterialCharge}" />
											<c:set var="expressCharge2" value="${expressCharge2 + item.engineerExpressCharge}" />
											<c:set var="travelCharge2" value="${travelCharge2 + item.engineerTravelCharge}" />
											<c:set var="otherCharge2" value="${otherCharge2 + item.engineerOtherCharge}" />
											<c:set var="totalCharge2" value="${totalCharge2 + item.engineerChage}" />
										</shiro:hasPermission>
									</tr>
									<c:set var="ridx" value="${ridx+1}" />
								</c:forEach>
								<tr>
									<td style="text-align:right;" colspan="7"><span
										class="alert alert-success">总计</span>
									</td>
									<td class="tdcenter"><span class="alert alert-success"><strong>${totalQty}</strong>
									</span>
									</td>
									<shiro:hasPermission name="sd:order:showreceive">
										<td class="tdcenter"><c:if test="${charge1 ne 0}"><span class="alert alert-success">${charge1}</span></c:if>
										</td>
										<td class="tdcenter"><c:if test="${materialCharge1 ne 0}"><span class="alert alert-success">${materialCharge1}</span></c:if>
										</td>
										<td class="tdcenter"><c:if test="${expressCharge1 ne 0}"><span class="alert alert-success">${expressCharge1}</span></c:if>
										</td>
										<td class="tdcenter"><c:if test="${travelCharge1 ne 0}"><span class="alert alert-success">${travelCharge1}</span></c:if>
										</td>
										<td class="tdcenter"><c:if test="${otherCharge1 ne 0}"><span class="alert alert-success">${otherCharge1}</span></c:if>
										</td>
										<td class="tdcenter"><c:if test="${totalCharge1 ne 0}"><span class="alert alert-info"><strong>${totalCharge1}</strong></span></c:if>
										</td>
									</shiro:hasPermission>
									<shiro:hasPermission name="sd:order:showpayment">
										<td class="tdcenter"><c:if test="${charge2 ne 0}"><span class="alert alert-success">${charge2}</span></c:if>
										</td>
										<td class="tdcenter"><c:if test="${materialCharge2 ne 0}"><span class="alert alert-success">${materialCharge2}</span></c:if>
										</td>
										<td class="tdcenter"><c:if test="${expressCharge2 ne 0}"><span class="alert alert-success">${expressCharge2}</span></c:if>
										</td>
										<td class="tdcenter"><c:if test="${travelCharge2 ne 0}"><span class="alert alert-success">${travelCharge2}</span></c:if>
										</td>

										<td class="tdcenter"><c:if test="${otherCharge2 ne 0}"><span class="alert alert-success">${otherCharge2}</span></c:if>
										</td>
										<td class="tdcenter"><c:if test="${totalCharge2 ne 0}"><span class="alert alert-info"><strong>${totalCharge2}</strong></c:if>
										</span>
										</td>
										<td class="tdcenter"></td>
										<td></td>
									</shiro:hasPermission>
								</tr>
								</c:if>
							</tbody>
						</table>
					</div>
				</div>
				<div class="tab-pane " id="tabTracking" title="跟踪进度">
				</div>
				<shiro:hasPermission name="sd:orderdetail:exception">
				<div class="tab-pane" id="tabException" title="异常处理">
				</div>
				</shiro:hasPermission>
				<div class="tab-pane" id="tabFeedback" title="反馈处理">
				</div>
				<div class="tab-pane" id="tabCustomerReturn" title="退补">
				</div>
			</div>
		</div>
		</c:if>

	<br />

	<form:form id="inputExceptionForm" modelAttribute="order" class="form-horizontal">
			<div id="divOrderException" >
				<div class="control-group">
					<label class="control-label">异常类型:</label>
					<div class="controls">
						<div style="width: 90%">
							<c:forEach items="${fns:getDictListFromMS('fi_charge_audit_type')}" var="item">
								<input type="radio" id="auditType${item.value}" name="auditType" value="${item.value}">
								<label for="auditType${item.value}">${item.label}</label>&nbsp;&nbsp;&nbsp;
							</c:forEach>
						</div>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">问题描述:</label>
					<div class="controls">
						<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="250" class="input-xxlarge required" style="width:90%;" cssClass="required"/>
					</div>
				</div>
				<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px; margin: 0px 0px 0px 0px;">
					<input id="btnExceptionConfirm" class="btn btn-danger" type="submit" value="标记异常"/>
					&nbsp;&nbsp;
					<input id="btnChargeConfirm" class="btn btn-success" type="button" value="确认对账"/>
					&nbsp;&nbsp;
					<input id="btnCancel" class="btn" type="button" value="关 闭" />
				</div>
			</div>
	</form:form>

</body>
</html>