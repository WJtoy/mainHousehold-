<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>订单详细信息(投诉)</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default" />
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
	<meta name="generator" content="1.0"/>
	<%@include file="/WEB-INF/views/include/dialog.jsp"%>
	<!-- clipboard -->
	<script src="${ctxStatic}/common/clipboard.min.js" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/SalesOrderService.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
	<link href="${ctxStatic}/bootstrap/2.3.1/bwizard/bwizard.min.css" type="text/css" rel="stylesheet" />
	<!-- jquery.supgerTable -->
	<%@ include file="/WEB-INF/views/modules/sales/sd/complain/tpl/orderDetail.html" %>
	<style type="text/css">
		.table thead th,.table tbody td {text-align: center;vertical-align: middle;}
		.tdlable {width:90px;text-align: right;}
		.tdbody {width:300px;}
		.table th,.table td {padding: 4px;}
		.table thead th {text-align: center;vertical-align: middle;}
		.table .tdcenter {text-align: center;vertical-align: middle;}
		.alert {padding: 4px 5px 4px 4px; margin-right: 5px;}
		#toolbar{height: 40px;line-height: 40px;}
		.form-horizontal .control-label{width:90px;}
		.form-horizontal .controls {margin-left:120px;}
		i[class^="icon-"] {font-size:18px;}
	</style>
	<script type="text/javascript">
        <c:set var="tabActiveName" value="${empty param.activeTab?'tabTracking':param.activeTab}" />
        Order.rootUrl = "${ctx}";
        SalesOrderService.rootUrl = "${ctx}";
        var orderdetail_index = parent.layer.getFrameIndex(window.name);
	</script>
</head>
<body onload="load()">
	<!-- new -->
	<form:form id="inputForm" action="#" modelAttribute="order" method="post" class="form-horizontal">
		<input type="hidden" id="changed" name="changed" value="${changed}" />
		<input type="hidden" id="refreshParent" name="refreshParent" value="${refreshParent}" />
		<input type="hidden" id="quarter" name="quarter" value="${order.quarter}" />
		<sys:message content="${message}" />
		<c:if test="${errorFlag == false}">
			<c:set var="status" value="${fns:stringToInteger(order.orderCondition.status.value)}" />
			<c:set var="cuser" value="${fns:getUser()}" />
			<c:set var="isCustomer" value="${!empty cuser && cuser.isCustomer()?true:false }" />
			<input type="hidden" id="isCustomer" name="isCustomer" value="${isCustomer}" />
			<c:set var="msg" value="" />
			<c:set var="brand" value="" />
			<c:forEach items="${order.items}" var="item">
				<c:set var="msg" value="${msg} ${item.brand} ${item.product.name}${' '}${item.qty }${' 需要' }${item.serviceType.name } " />
				<c:if test="${empty brand && !empty item.brand}">
					<c:set var="brand" value="${item.brand}" />
				</c:if>
			</c:forEach>
			<c:set var="ordermsg" value="单号:${order.orderNo} ~n客户名称:${order.orderCondition.customer.name}" />
			<c:if test="${order.dataSource.value != '0'}">
				<c:set var="ordermsg" value="${ordermsg}(${order.dataSource.label}${order.b2bShop==null || order.b2bShop.shopId == ''?'':'-'.concat(order.b2bShop.shopName)})" />
			</c:if>
			<c:set var="ordermsg" value="${ordermsg} ~n联系人:${order.orderCondition.userName} ~n电话:{{=it.servicePhone}} ~n用户地址:${order.orderCondition.area.name} {{=it.serviceAddress}} ~n服务明细:${msg} ~n服务描述:${order.description} ~n---(反馈):" />
			<input type="hidden" id="txtordermsg" name="txtordermsg" value="${ordermsg}" />
		<!-- toolbar -->
		<div class="row-fluid" data-spy="affix" data-offset-top="10" style="z-index: 999999999;background-color: #fff;">
			<div class="span5">
				<div id="bwizard">
				</div>
			</div>
			<c:set var="status" value="${fns:stringToInteger(order.orderCondition.status.value)}" />
			<div class="span3">
				<div id="toolbar">
					<c:if test="${status >= 40 && order.orderCondition.finishPhotoQty>0}">
					<a class="btn btn-primary" id="btnFinishPhoto" href="javascript:;" onclick="Order.photolistNew('${order.id}','${order.quarter}');" title="点击查看完成照片"><i class="icon-camera icon-white"></i> 完成照片</a>
					</c:if>
					<a class="btn btn-success" href="javascript:;" id="btn_order_copy" ><i class="icon-list-alt"></i> 订单信息</a>
				</div>
			</div>

		</div>
		<!-- order -->
		<div class="accordion-group" style="margin-top:2px;">
			<div class="accordion-heading">
				<a href="#divheader" class="accordion-toggle" data-toggle="collapse">基本信息
					<span class="arrow"></span>
				</a>
			</div>
			<div id="divheader" class="accordion-body">
				<!-- order head -->
				<table class="table table-bordered table-hover" style="margin-bottom: 0px;">
					<tbody>
						<tr>
							<td class="tdlable">
								<label class="control-label">订单编号:</label>
							</td>
							<td class="tdbody">
								<span id="spOrderNo">
									<c:choose>
										<c:when test="${not empty order.repeateNo}">
											<a href="javascript:" data-toggle="tooltip" style="color:red;" data-tooltip="疑似重复订单，相关单号: ${order.repeateNo}">${order.orderNo}</a>
										</c:when>
										<c:otherwise>${order.orderNo}</c:otherwise>
									</c:choose>
								</span>
								<span class="alert alert-info">${order.orderCondition.status.label} </span>
								<c:if test="${order.orderCondition.urgentLevel.id >0}">
									<a data-toggle="tooltip" data-tooltip="${order.orderCondition.urgentLevel.remarks}<br/>加急费：${order.orderFee.engineerUrgentCharge}元" class='label label-important'>加急</a>
								</c:if>
							</td>
							<td class="tdlable">
								<label class="control-label">客户名称:</label>
							</td>
							<td class="tdbody">
								<a href="javascript:" data-toggle="tooltip" data-tooltip="${order.orderCondition.customer.remarks}">${order.orderCondition.customer.name}</a>
								<c:if test="${order.dataSource.value != '0'}">
									<br>${order.dataSource.label}${order.b2bShop==null || order.b2bShop.shopId == ""?"":"-".concat(order.b2bShop.shopName)}
								</c:if>
							</td>
							<td class="tdlable"><label class="control-label">业务/跟单:</label>
							</td>
							<td class="tdbody">${order.orderCondition.customer.sales.name}
								<c:if test="${!empty order.orderCondition.customer.sales.qq}">
									<a style="padding-left: 20px;" target="_blank"
									   href="http://wpa.qq.com/msgrd?v=3&uin=${order.orderCondition.customer.sales.qq}&site=qq&menu=yes"><img
											border="0" src="http://wpa.qq.com/pa?p=2:572202493:52" alt="点击这里给我发消息" title="联系业务员QQ：${order.orderCondition.customer.sales.qq}" />
									</a>
								</c:if>
								&nbsp;&nbsp;&nbsp;&nbsp;
								<c:if test="${!empty order.orderCondition.customer.merchandiser}">
									${order.orderCondition.customer.merchandiser.name}
									<c:if test="${!empty order.orderCondition.customer.merchandiser.qq}">
										<a style="padding-left: 10px;" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=${order.orderCondition.customer.merchandiser.qq}&site=qq&menu=yes">
											<img border="0" src="http://wpa.qq.com/pa?p=2:572202493:52" alt="点击这里给我发消息" title="联系跟单员QQ：${order.orderCondition.customer.merchandiser.qq}"/>
										</a>
									</c:if>
								</c:if>
							</td>
						</tr>
						<tr>
							<td class="tdlable"><label class="control-label">联系人:</label></td>
							<td class="tdbody">${order.orderCondition.userName}</td>
							<td class="tdlable"><label class="control-label">联络电话:</label></td>
							<td class="tdbody"><label id="lblservicePhone">${order.orderCondition.servicePhone}</label>
								<button id="btnCall" class="btn btn-success" type="button" onclick="javascript:StartDial('${order.orderCondition.servicePhone}',false);"><i class="icon-phone-sign icon-white"></i>  拨号</button>
							</td>
							<td class="tdlable"><label class="control-label">座机:</label></td>
							<td class="tdbody">${order.orderCondition.phone2}</td>
						</tr>
						<tr>
							<td class="tdlable"><label class="control-label">上门地址:</label></td>
							<td class="tdbody" colspan="3">${order.orderCondition.area.name}&nbsp;&nbsp;<label id="lblserviceAddress">${order.orderCondition.serviceAddress}</label></td>
							<td class="tdlable"><label class="control-label">客服:</label>
							</td>
							<td class="tdbody">${order.orderCondition.kefu.name}
								<c:if test="${!empty order.orderCondition.kefu.qq}">
									<a style="padding-left: 20px;" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=${order.orderCondition.kefu.qq}&site=qq&menu=yes">
										<img border="0" src="http://wpa.qq.com/pa?p=2:572202493:52"
											 alt="点击这里给我发消息" title="联系客服QQ：${order.orderCondition.kefu.qq}" />
									</a>
								</c:if>
						</tr>
						<tr>
							<td class="tdlable"><label class="control-label">服务描述:</label></td>
							<td class="tdbody" colspan="5"><label id="lbldescription">${order.description}</label></td>
						</tr>
					</tbody>
				</table>
				<!-- order items -->
				<table id="productTable"
					   class="table table-bordered table-condensed table-hover"
					   style="margin-bottom: 0px;pmargin-top:3px;">
					<thead>
					<tr>
						<th width=30px>序号</th>
						<th>服务类型</th>
						<th>产品</th>
						<th>品牌</th>
						<th>型号/规格</th>
						<th>数量</th>
						<th>快递</th>
						<th>产品说明</th>
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
							<td><c:if test="${!empty item.expressCompany }">
								<a href="http://www.kuaidi100.com/chaxun?com=${item.expressCompany.value}&nu=${item.expressNo }"
								   target="_blank" title="点击进入快递100">
										${item.expressCompany.label}&nbsp;&nbsp;${item.expressNo}
								</a>
							</c:if>
							</td>
							<td>
								<a href="javascript:void(0);" onclick="Order.getCustomerPrice(${order.orderCondition.customer.id}, ${item.product.id},${item.serviceType.id});">
									<abbr title="点击查看产品说明">查看</abbr>
								</a>
							</td>
						</tr>
						<c:set var="ridx" value="${ridx+1}" />
						<c:set var="totalQty" value="${totalQty+item.qty}" />
					</c:forEach>
					<!-- 客户备注 -->
						<tr>
							<td colspan="2">客户说明</td>
							<td colspan="6">${order.orderCondition.customer.remarks}</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
		<!-- engineer -->
		<shiro:hasPermission name="sd:order:showengineerinfo">
			<div class="accordion-group" style="margin-top:2px;">
				<div class="accordion-heading">
					<a href="#divengineer" class="accordion-toggle"
						data-toggle="collapse">网点师傅 <span class="arrow"></span>
					</a>
				</div>
				<div id="divengineer" class="accordion-body">
					<table class="table table-bordered table-hover" style="margin-bottom: 0px;">
						<tbody>
							<tr>
								<td class="tdlable"><label class="control-label">网点编号:</label></td>
								<td class="tdbody">${order.orderCondition.servicePoint.servicePointNo}
									<c:if test="${!empty order.orderCondition.servicePoint.finance.bankIssue && !empty order.orderCondition.servicePoint.finance.bankIssue.label}">
										<label id="lbltotalCharge" class="alert alert-error">${order.orderCondition.servicePoint.finance.bankIssue.label }</label>
									</c:if>
								</td>
								<td class="tdlable"><label class="control-label">姓名:</label></td>
								<td class="tdbody">${order.orderCondition.engineer.name}
									<c:if test="${not empty order.orderCondition.engineer}">
									<c:choose>
										<c:when test="${order.orderCondition.engineer.subFlag==0}">(主)</c:when>
										<c:otherwise>(子)</c:otherwise>
									</c:choose>
									</c:if>
								</td>
								<td class="tdlable"><label class="control-label">手机:</label></td>
								<td class="tdbody">${order.orderCondition.engineer.mobile}&nbsp;&nbsp;
									<c:if test="${not empty order.orderCondition.engineer.mobile}">
										<button id="btnEngineerCall" class="btn btn-success" type="button" onclick="javascript:StartDial('${order.orderCondition.engineer.mobile}',false)"><i class="icon-phone-sign icon-white"></i>  拨号</button>
									</c:if>
								</td>
							</tr>
							<tr>
								<td class="tdlable"><label class="control-label">电话:</label>
								</td>
								<td class="tdbody">${order.orderCondition.engineer.mobile}</td>
								<td class="tdlable"><label class="control-label">结算方式:</label></td>
								<td class="tdbody">${order.orderFee.engineerPaymentType.label}</td>
								<td class="tdlable"><label class="control-label">备注:</label></td>
								<td class="tdbody">${order.orderCondition.servicePoint.remarks}</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</shiro:hasPermission>
		<%--</c:if>--%>
		</c:if>
	</form:form>
	<br />
	<!-- tabs -->
	<c:if test="${errorFlag == false}">
		<c:set var="tabCount" value="0"/>

	<div class="tabbable" style="margin:0px 20px">
		<ul class="nav nav-tabs">
			<c:set var="tabCount" value="${tabCount+1}"/>
			<c:if test="${tabCount==1 && empty tabActiveName}">
				<c:set var="tabActiveName" value="tabTracking" />
			</c:if>
			<li id="litabTracking" class="${tabCount==1?'active':''}"><a href="#tabTracking" data-toggle="tab" id="lnktabTracking" onclick="Order.orderDetail_Tracking('${order.id}','${order.quarter}',${order.orderCondition.status.value});">跟踪进度</a></li>

			<c:if test="${order.orderCondition.serviceTimes>0 || (status >= 40 && status<60)}">
				<c:set var="tabCount" value="${tabCount+1}"/>
				<c:if test="${tabCount==1 && empty tabActiveName}">
				<c:set var="tabActiveName" value="tabService" />
				</c:if>
				<li id="litabService" class="${tabCount==1?'active':''}" >
					<a href="#tabService" id="lnktabService" data-toggle="tab">上门服务</a>
				</li>
			</c:if>

			<%--<shiro:hasPermission name="sd:orderdetail:exception">
				<c:set var="tabCount" value="${tabCount+1}"/>
				<c:if test="${tabCount==1 && empty tabActiveName}">
				<c:set var="tabActiveName" value="tabException" />
				</c:if>
				<li id="litabException" class="${tabCount==1?'active':''}"><a href="#tabException" data-toggle="tab" id="lnktabException" onclick="Order.showExceptLogs('${order.id}','${order.quarter}');">异常处理</a></li>
			</shiro:hasPermission>--%>
			<c:if test="${order.orderCondition.isComplained>0}">
				<c:set var="tabCount" value="${tabCount+1}"/>
				<c:if test="${tabCount==1 && empty tabActiveName}">
					<c:set var="tabActiveName" value="tabComplain" />
				</c:if>
				<li id="litabComplain" class="${tabCount==1?'active':''}">
					<a href="#tabComplain" data-toggle="tab" id="lnktabComplain" onclick="Order.showComplainList('${order.id}','${order.quarter}');">投诉</a></li>
				</li>
			</c:if>
		</ul>
		<!-- tab content -->
		<div class="tab-content">
			<!-- service -->
			<c:if test="${order.orderCondition.serviceTimes>0 || (status >= 40 && status<60)}">
			<div class="tab-pane ${tabActiveName=="tabService"?"active":""}" id="tabService" title="实际上门清单">

				<div id="divserviceTable">
					<table id="serviceTable" class="table table-striped table-bordered table-condensed table-hover" style="margin-bottom: 0px;">
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
									<c:choose>
										<c:when test="${isCustomer eq true}">
											<th colspan="7">应付款</th>
										</c:when>
										<c:otherwise>
											<th colspan="6">应收款</th>
										</c:otherwise>
									</c:choose>
								<shiro:hasPermission name="sd:order:showpayment">
									<th colspan="7">应付款</th>
									<th rowspan="2">备注</th>
								</shiro:hasPermission>
							</tr>
							<tr>
									<th>服务费</th>
									<th>配件费</th>
									<th>快递费</th>
									<th>远程费</th>
									<th>其他</th>
									<th>小计</th>
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
							<c:set var="totalQty" value="0" />
							<c:set var="ridx" value="0" />
							<c:set var="charge1" value="0" />
							<c:set var="charge2" value="0" />
							<c:set var="materialCharge1" value="0.00" />
							<c:set var="materialCharge2" value="0.00" />
							<c:set var="expressCharge1" value="0.00" />
							<c:set var="expressCharge2" value="0.00" />
							<c:set var="travelCharge1" value="0.00" />
							<c:set var="travelCharge2" value="0.00" />
							<c:set var="otherCharge1" value="0.00" />
							<c:set var="otherCharge2" value="0.00" />
							<c:set var="totalCharge1" value="0.00" />
							<c:set var="totalCharge2" value="0.00" />
							<c:forEach items="${order.detailList}" var="item">
								<tr>
									<td class="tdcenter">${ridx+1}</td>
									<td class="tdcenter">${fns:formatDate(item.createDate,'yyyy-MM-dd HH:mm')}</td>
									<td class="tdcenter">${item.serviceTimes}</td>
									<td>${item.serviceType.name}</td>
									<td>${item.product.name}
										<input type="hidden" id="detailId${i.index}" name="detailId${i.index}" value="${item.id}" />
									</td>
									<td>${item.brand}</td>
									<td>${item.productSpec}</td>
									<td class="tdcenter">${item.qty}</td>
									<c:set var="totalQty" value="${totalQty+item.qty}" />
									<shiro:hasPermission name="sd:order:showreceive">
										<td class="tdcenter">${item.charge}</td>
									</shiro:hasPermission>
									<shiro:lacksPermission name="sd:order:showreceive">
										<td class="tdcenter">*</td>
									</shiro:lacksPermission>
										<td class="tdcenter">${item.materialCharge}</td>
										<td class="tdcenter">${item.expressCharge}</td>
										<td class="tdcenter">${item.travelCharge}</td>
										<td class="tdcenter">${item.otherCharge}</td>
									<shiro:hasPermission name="sd:order:showreceive">
										<td class="tdcenter"><b>${item.customerCharge}</b></td>
									</shiro:hasPermission>
									<shiro:lacksPermission name="sd:order:showreceive">
										<td class="tdcenter">*</td>
									</shiro:lacksPermission>
										<c:set var="charge1" value="${charge1 + item.charge}" />
										<c:set var="materialCharge1" value="${materialCharge1 + item.materialCharge}" />
										<c:set var="expressCharge1" value="${expressCharge1 + item.expressCharge}" />
										<c:set var="travelCharge1" value="${travelCharge1 + item.travelCharge}" />
										<c:set var="otherCharge1" value="${otherCharge1 + item.otherCharge}" />
										<c:set var="totalCharge1" value="${totalCharge1 + item.customerCharge}" />

									<shiro:hasPermission name="sd:order:showpayment">
										<td class="tdcenter">${item.engineerServiceCharge}</td>
										<td class="tdcenter">${item.engineerMaterialCharge}</td>
										<td class="tdcenter">${item.engineerExpressCharge}</td>
										<td class="tdcenter">${item.engineerTravelCharge}
											<c:if test="${!empty item.travelNo}">签核单号:${item.travelNo}</c:if>
										</td>
										<td class="tdcenter">${item.engineerOtherCharge}</td>
										<td class="tdcenter"><b>${item.engineerChage}</b>
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
								<td class="tdcenter"><span class="alert alert-success"><strong>${totalQty}</strong></span>
								</td>
								<shiro:hasPermission name="sd:order:showreceive">
									<td class="tdcenter"><span class="alert alert-success">${fns:formatNum(charge1)}</span></td>
								</shiro:hasPermission>
								<shiro:lacksPermission name="sd:order:showreceive">
									<td class="tdcenter"><span class="alert alert-success">*</span></td>
								</shiro:lacksPermission>
									<td class="tdcenter"><span class="alert alert-success">${fns:formatNum(materialCharge1)}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-success">${fns:formatNum(expressCharge1)}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-success">${fns:formatNum(travelCharge1)}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-success">${fns:formatNum(otherCharge1)}</span>
									</td>
								<shiro:hasPermission name="sd:order:showreceive">
									<td class="tdcenter"><span class="alert alert-info"><strong>${fns:formatNum(totalCharge1)}</strong></span></td>
								</shiro:hasPermission>
								<shiro:lacksPermission name="sd:order:showreceive">
									<td class="tdcenter"><span class="alert alert-success">*</span></td>
								</shiro:lacksPermission>

								<shiro:hasPermission name="sd:order:showpayment">
									<td class="tdcenter"><span class="alert alert-success">${fns:formatNum(charge2)}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-success">${fns:formatNum(materialCharge2)}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-success">${fns:formatNum(expressCharge2)}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-success">${fns:formatNum(travelCharge2)}</span>
									</td>

									<td class="tdcenter"><span class="alert alert-success">${fns:formatNum(otherCharge2)}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-info"><strong>${fns:formatNum(totalCharge2)}</strong>
									</span>
									</td>
									<td class="tdcenter"></td>
								</shiro:hasPermission>
								<td>&nbsp;</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
			</c:if>
			<div class="tab-pane ${tabActiveName=="tabTracking"?"active":""}" id="tabTracking" >
			</div>
			<shiro:hasPermission name="sd:orderdetail:exception">
			<div class="tab-pane ${tabActiveName=="tabException"?"active":""}" id="tabException" >
			</div>
			</shiro:hasPermission>
			<!-- 投诉 -->
			<div class="tab-pane ${tabActiveName=="tabComplain"?"active":""}" id="tabComplain" >
			</div>
		</div>
	</div>
	</c:if>

	<c:if test="${errorFlag == false}">
	<script type="text/javascript">
		// 回调方法
		// 变更订单列表的刷新标志，在订单详情页关闭时刷新
		function setOrderListRepageFlag(){
            if($("#refreshParent").val()=='true') {
                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                if (iframe != undefined) {
                    $("#repageFlag", iframe.document).val("true");
                }
            }
		}

		//刷新页面(弹窗调用)
		function reload(activeTab){
            var iframeWin = $(parent.document).contents().find("#layui-layer-iframe"+ orderdetail_index)[0];
            var url = iframeWin.src;
            var idx = url.indexOf("activeTab");
            if(idx==-1){
                url = url + "&activeTab=" + (activeTab || 'tabTracking');
			}else{
                url = url.substr(0,idx-1)+"&activeTab=" + (activeTab || 'tabTracking');
			}
            iframeWin.src = url;
            setOrderListRepageFlag();
		}

        var tabName = '${tabActiveName}';
        function loadTabContent(){
            $("#lnk"+tabName).trigger("click");
        }
	</script>
	<script class="removedscript" type="text/javascript">
        $(document).ready(function(){

            $(document).ready(function(){
                $('a[data-toggle=tooltip]').darkTooltip({gravity : 'north'});
            });
			// toolbar
			var orderInfo = ${fns:toGson(order)};
			orderInfo.orderCondition.quarter = orderInfo.quarter;
            Order.detail_order_process(orderInfo); //process
			orderInfo.layerIndex = orderdetail_index;//layer index of this dialog
			if(!Utils.isEmpty(tabName)) {
                setTimeout('loadTabContent()',100);
            }
		});
	</script>
	</c:if>
	<object id="plugin0" type="application/x-nyteleactivex" width="30" height="30">
		<param name="onload" value="pluginLoaded" />
		<param name="install-url" value="${ctxPlugin}/npNYTeleActiveX.dll" />
	</object>
	<script type="text/javascript">
        <!-- 拨号插件 -->
        function plugin0()
        {
            return document.getElementById('plugin0');
        }
        plugin = plugin0;
        function addEvent(obj, name, func)
        {
            if (window.addEventListener) {
                obj.addEventListener(name, func, false);
            } else {
                obj.attachEvent("on"+name, func);
            }
        }

        function load()
        {
            addEvent(plugin(), 'OnDeviceConnect', function(number){
                //alert('设备连接事件：'+number);
            });
            addEvent(plugin(), 'OnDeviceDisconnect', function(number){
                //alert('设备断开事件：'+number);
            });
            addEvent(plugin(), 'OnCallOut', function(teleNo){
                //alert('呼叫事件：'+teleNo);
            });
            addEvent(plugin(), 'OnCallIn', function(teleNo){
                //alert('来电事件：'+teleNo);
            });
            addEvent(plugin(), 'OnHangUp', function(teleNo){
                //alert('挂起事件：'+teleNo);
            });
            addEvent(plugin(), 'OnAnswer', function(teleNo){
                //alert('应答事件：'+teleNo);
            });
        }
        function pluginLoaded() {
            //alert("Plugin loaded!");
        }

        //摘机
        function OffHookCtrl()
        {
            if( !plugin().OffHookCtrl() )
                alert("OffHookCtrl Fail");
        }

        function call(id){
//		var teleNo = $("#"+id).val();
            var teleNo = $("[id='" + id + "']").val();
            StartDial(teleNo,false);
        }

        //拔号
        function StartDial(teleNo, bRecord)
        {
            //teleNo = "00" + $("[id='orderCondition.servicePhone']").val();
            if(Utils.isEmpty(teleNo)){
                return false;
            }
            if( !plugin().StartDial("00" + teleNo, bRecord) )
                alert("StartDial Fail");
        }

        //挂机或挂断
        function HangUpCtrl()
        {
//            if( !plugin().HangUpCtrl() )
//                alert("HangUpCtrl Fail");
        }
        //上传录音
        function HangUpCtrl()
        {
            alert(plugin().UploadRecord());
        }
        function testEvent()
        {
            plugin().testEvent();
        }

        function pluginValid()
        {
            if(plugin().valid){
                alert(plugin().echo("This plugin seems to be working!"));
            } else {
                alert("Plugin is not working :(");
            }
        }
		//order
		var tmpl = $('#txtordermsg').val();
		var doTtmpl = doT.template(tmpl);
		var data = {'servicePhone':'','serviceAddress':''};
		data.servicePhone = $("#lblservicePhone").text();
		data.serviceAddress = $("#lblserviceAddress").text();
		var html = doTtmpl(data);
		var orderMsg = html.replace(/~n/g,'\n');
		$("#btn_order_copy").attr('data-clipboard-text',orderMsg);
		var clip_order = new ClipboardJS('#btn_order_copy');
		clip_order.on('success', function(e) {
			layerMsg("订单信息复制成功");
		});
		clip_order.on('error', function(e) {
			layerError("订单信息复制失败： <br/>" + JSON.stringify(e.message));
		});
	</script>
</body>
<script type="text/javascript">
    $(document).ready(function() {
        $(".removedscript").remove();//移除初始化脚本
	});
</script>
</html>