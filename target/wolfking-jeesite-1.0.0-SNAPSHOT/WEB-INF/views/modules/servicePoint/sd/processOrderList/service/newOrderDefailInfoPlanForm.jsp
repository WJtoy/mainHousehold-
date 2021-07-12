<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>订单详细信息(网点派单)</title>
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
	<!-- image viewer -->
	<script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
	<link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
	<script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
	<link href="${ctxStatic}/bootstrap/2.3.1/bwizard/bwizard.min.css" type="text/css" rel="stylesheet" />
	<%@ include file="/WEB-INF/views/modules/servicePoint/sd/processOrderList/service/tpl/newOrderDetailPlan.html" %>
	<%@ include file="/WEB-INF/views/modules/sd/crush/tpl/orderCrushList.html" %>
	<%@ include file="/WEB-INF/views/modules/servicePoint/sd/processOrderList/service/tpl/reminderReplyItem.html" %>
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
		.red {color: red;}
		.gallery-thumb {position: relative;cursor: pointer;padding: 5px;}
		.gallery-thumb img {width: 30px;height: 30px;margin-right: 5px;}
	</style>
	<script type="text/javascript">
        <c:set var="tabActiveName" value="${empty param.activeTab?'tabTracking':param.activeTab}" />
        Order.rootUrl = "${ctx}";
        var orderdetail_index = parent.layer.getFrameIndex(window.name);
	</script>
</head>
<body onload="load()">
    <c:set var="currentuser" value="${fns:getUser()}" />
	<!-- new -->
	<form:form id="inputForm" action="#" method="post" class="form-horizontal">
		<input type="hidden" id="changed" name="changed" value="${changed}" />
		<input type="hidden" id="refreshParent" name="refreshParent" value="${refreshParent}" />
		<input type="hidden" id="quarter" name="quarter" value="${order.quarter}" />
		<sys:message content="${message}" />
		<c:if test="${errorFlag == false}">
			<c:set var="status" value="${fns:stringToInteger(order.orderCondition.status.value)}" />
			<c:set var="cuser" value="${fns:getUser()}" />
			<c:set var="msg" value="" />
			<c:set var="brand" value=""/>
			<c:forEach items="${order.items}" var="item">
				<c:set var="msg" value="${msg} ${item.brand} ${item.product.name}${item.qty}${item.serviceType.name}" />
				<c:if test="${empty brand && !empty item.brand}">
					<c:set var="brand" value="${item.brand}"/>
				</c:if>
			</c:forEach>
			<c:set var="engineermsg" value="单号: ${order.orderNo} ~n用户: ${order.orderCondition.userName} {{=it.servicePhone}}  ${order.orderCondition.area.name} {{=it.serviceAddress}} ~n产品:${msg} ~n备注: ${order.description}"/>
			<c:set var="ordermsg" value="单号:${order.orderNo} ${order.orderCondition.customer.name}" />
			<c:if test="${order.dataSource.value != '0'}">
				<c:set var="ordermsg" value="${ordermsg}(${order.dataSource.label}${order.b2bShop==null || order.b2bShop.shopId == ''?'':'-'.concat(order.b2bShop.shopName)}${empty order.parentBizOrderId?'':'-'.concat(order.parentBizOrderId)})" />
			</c:if>
			<c:set var="ordermsg" value="${ordermsg} ~n联系人:${order.orderCondition.userName} {{=it.servicePhone}} ${order.orderCondition.area.name} {{=it.serviceAddress}} ~n服务明细:${msg} ~n服务描述:${order.description} ~n---(反馈):"/>
			<input type="hidden" id="txtordermsg" name="txtordermsg" value="${ordermsg}" />
        	<c:set var="usermsg" value="您的${msg }，${fn:substring(order.orderCondition.engineer.name,0,1)}师傅${order.orderCondition.engineer.mobile}已接单，客服${fn:substring(order.orderCondition.kefu.name,0,1)}小姐${order.orderCondition.kefu.phone}/${fourServicePhone}"/>
			<input type="hidden" id="txengineertmsg" name="txengineertmsg" value="${engineermsg }" />
			<input type="hidden" id="txtusermsg" name="txtusermsg" value="${usermsg}" />
			<c:set var="sendUserMsg" value="您好，我们是${brand}售后，由于无法联系上您，请留意接听来电，客服${fn:substring(order.orderCondition.kefu.name,0,1)}小姐${order.orderCondition.kefu.phone}/${fourServicePhone}"/>
			<input type="hidden" id="txtSendUserMsg" name="txtSendUserMsg" value="${sendUserMsg}"/>
		<!-- toolbar -->
		<div class="row-fluid" data-spy="affix" data-offset-top="10" style="z-index: 999999999;background-color: #fff;">
			<div class="span4">
				<div id="bwizard">
				</div>
			</div>

			<div class="span5">
				<div id="toolbar"></div>
			</div>
				<shiro:hasPermission name="sd_order_message_btn">
				<div class="span3" style="height: 40px;line-height: 40px;">
					<a class="btn btn-success" href="javascript:;" id="btn_engineer_copy" ><i class="icon-envelope-alt"></i> 安维信息</a>
					<a class="btn btn-success" href="javascript:;" id="btn_order_copy" ><i class="icon-list-alt"></i> 厂商信息</a>
					<a class="btn btn-success" href="javascript:;" id="btn_sendMsgToUser"><i class="icon-list-alt"></i>用户未接电话</a>
				</div>
				</shiro:hasPermission>
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
								<span id="span_logo"></span>
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
									<a data-toggle="tooltip" data-tooltip="${order.orderCondition.urgentLevel.remarks}<br/>加急费：${order.orderFee.engineerUrgentCharge}元" class='label label-important' style="background-color: ${order.orderCondition.urgentLevel.markBgcolor};">加急</a>
								</c:if>
							</td>
							<td class="tdlable">
								<label class="control-label">客户名称:</label>
							</td>
							<td class="tdbody">
								<a href="javascript:" data-toggle="tooltip" data-tooltip="${order.orderCondition.customer.remarks}">${order.orderCondition.customer.name}</a>
								<%--<c:if test="${order.dataSource.value != '1' && !order.dataSource.value != '0'}">--%>
								<c:if test="${order.dataSource.value != '0'}">
									<br>${order.dataSource.label}${order.b2bShop==null || order.b2bShop.shopId == ""?"":"-".concat(order.b2bShop.shopName)}
								</c:if>
							</td>
							<td class="tdlable"><label class="control-label">业务/跟单:</label>
							</td>
							<td class="tdbody">${order.orderCondition.customer.sales.name}
								<c:if test="${!empty order.orderCondition.customer.sales.qq}">
									<a style="padding-left: 10px;" target="_blank"
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
								<c:if test="${not empty order.orderCondition.phone2}">
									/&nbsp;${order.orderCondition.phone2}
								</c:if>
								<c:if test="${status>=20 && status<60}">
									<button class="btn btn-primary" type="button" onclick="Order.editUserInfo('${order.id}','${order.orderNo}','${order.quarter}',orderdetail_index);">编辑</button>
								</c:if>
								<button id="btnCall" class="btn btn-success" type="button" onclick="javascript:StartDial('${order.orderCondition.servicePhone}',false);"><i class="icon-phone-sign icon-white"></i>  拨号</button>
							</td>
							<td class="tdlable"><label class="control-label">客服:</label></td>
							<td class="tdbody">${order.orderCondition.kefu.name}
								<c:if test="${!empty order.orderCondition.kefu.qq}">
								<a style="padding-left: 20px;" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=${order.orderCondition.kefu.qq}&site=qq&menu=yes">
									<img border="0" src="http://wpa.qq.com/pa?p=2:572202493:52"
										 alt="点击这里给我发消息" title="联系客服QQ：${order.orderCondition.kefu.qq}" />
								</a>
								</c:if>
							</td>
						</tr>
						<tr>
							<td class="tdlable"><label class="control-label">上门地址:</label></td>
							<td class="tdbody" colspan="3"><label id="lblAreaName">${order.orderCondition.area.name}</label>&nbsp;&nbsp;<label id="lblserviceAddress">${order.orderCondition.serviceAddress}</label>
								<a style="padding-left: 10px;" href="javascript:void(0);" onclick="Order.locateAddr('${order.orderCondition.area.name}${order.orderCondition.serviceAddress}')">
									<img border="0" style="height: 21px;width: 19px;" src="${ctxStatic}/images/maps_96px.png" title="查看地图" />
								</a>
							</td>
							<td class="tdlable"><label class="control-label">购买时间:</label></td>
							<td><label id="lblBuyDate">${order.orderAdditionalInfo.buyDateString}</label></td>
						</tr>
						<tr>
							<td class="tdlable" rowspan="2"><label class="control-label">服务描述:</label></td>
							<td class="tdbody" rowspan="2" colspan="3"><label id="lbldescription">${order.description}</label></td>
							<td class="tdlable"><label class="control-label">实际到货:</label></td>
							<td><label id="lblArrivalDate"><fmt:formatDate value="${order.orderCondition.arrivalDate}" pattern="yyyy-MM-dd HH:mm"/></label></td>
						</tr>
						<tr>
							<td class="tdlable"><label class="control-label">预计到货:</label></td>
							<td class="tdbody">${order.orderAdditionalInfo.estimatedReceiveDate}</td>
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
						<th width="40px">安装规范</th>
						<th>品牌</th>
						<th>型号/规格</th>
						<th>产品图片</th>
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
							<td>${item.product.name}</td>
							<td>
								<c:choose>
									<c:when test="${item.fixSpec == 1}">
										<a href="javascript:void(0);" onclick="Order.getProductFixSpec('${order.orderCondition.customer.id}','${item.product.id}','${item.product.name}')" title="查看安装规范">查看</a>
									</c:when>
									<c:otherwise>无</c:otherwise>
								</c:choose>
							</td>
							<td>${item.brand }</td>
							<td>${item.productSpec }</td>
							<td>
								<c:if test="${not empty item.pics}">
									<div class="gallery-thumb">
										<c:forEach items="${item.pics}" var="pic">
											<a href="javascript:;"><img src="${pic}" data-original="${pic}"></a>
										</c:forEach>
									</div>
								</c:if>
							</td>
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
							<td colspan="9">${order.orderCondition.customer.remarks}</td>
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
									<c:if test="${!empty order.orderCondition.servicePoint.finance.bankIssue && order.orderCondition.servicePoint.finance.bankIssue.label ne '无' && !empty order.orderCondition.servicePoint.finance.bankIssue.label}">
										<label id="lbltotalCharge" class="alert alert-error">${order.orderCondition.servicePoint.finance.bankIssue.label }</label>
									</c:if>
									<c:if test="${status>=20 && status<60}">
										&nbsp;&nbsp;<button type="button" class="btn btn-primary" id="lnktabPlan" onclick="Order.plan('${order.id}','${order.orderNo}','${order.quarter}',orderdetail_index);"><i class="icon-user icon-white"></i>  ${empty order.orderCondition.servicePoint.servicePointNo?'派单':'改派'}</button>
									    <c:if test="${!currentuser.isRushKefu()}">
											<c:choose>
												<c:when test="${order.orderCondition.rushOrderFlag==1}">
													<label id="labelAssault" class="alert alert-error">突击中</label>
												</c:when>
												<c:otherwise>
													<button id="btnAssault" type="button" class="btn btn-primary" onclick="Order.crush_form('','${order.id}','${order.quarter}','');">突击单</button>
												</c:otherwise>
											</c:choose>
									    </c:if>
									</c:if>
								</td>
								<td class="tdlable"><label class="control-label">网点电话:</label>
								</td>
								<td class="tdbody">${order.orderCondition.servicePoint.contactInfo1}</td>
								<td class="tdlable"><label class="control-label">结算方式:</label></td>
								<td class="tdbody">${order.orderFee.engineerPaymentType.label}</td>
							</tr>
							<tr>
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
								<td class="tdlable"><label class="control-label">备注:</label></td>
								<td class="tdbody">${order.orderCondition.servicePoint.remarks}</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</shiro:hasPermission>
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
			<li id="litabTracking" class="${tabCount==1?'active':''}">
					<a href="#tabTracking" data-toggle="tab" id="lnktabTracking" onclick="Order.orderDetail_TrackingNew('${order.id}','${order.quarter}',${order.orderCondition.status.value});">跟踪进度</a>
            </li>
			<c:if test="${order.orderCondition.serviceTimes>0 || (status >= 40 && status<60)}">
				<c:set var="tabCount" value="${tabCount+1}"/>
				<c:if test="${tabCount==1 && empty tabActiveName}">
				    <c:set var="tabActiveName" value="tabService" />
				</c:if>
				<li id="litabService" class="${tabCount==1?'active':''}" >
					<a href="#tabService" id="lnktabService" data-toggle="tab">上门服务</a>
				</li>
			</c:if>
			<shiro:hasPermission name="sd:orderdetail:exception">
				<c:set var="tabCount" value="${tabCount+1}"/>
				<c:if test="${tabCount==1 && empty tabActiveName}">
				    <c:set var="tabActiveName" value="tabException" />
				</c:if>
				<li id="litabException" class="${tabCount==1?'active':''}"><a href="#tabException" data-toggle="tab" id="lnktabException" onclick="Order.showExceptLogs('${order.id}','${order.quarter}');">异常处理</a></li>
			</shiro:hasPermission>
			<c:if test="${order.orderStatus.complainFlag>0}">
				<c:set var="tabCount" value="${tabCount+1}"/>
				<c:if test="${tabCount==1 && empty tabActiveName}">
					<c:set var="tabActiveName" value="tabComplain"/>
				</c:if>
				<li id="litabComplain" class="${tabCount==1?'active':''}">
					<a href="#tabComplain" data-toggle="tab" id="lnktabComplain" onclick="Order.showComplainList('${order.id}','${order.quarter}');">投诉</a></li>
				</li>
			</c:if>
			<c:if test="${order.orderCondition.rushOrderFlag!=0}">
                <c:set var="tabCount" value="${tabCount+1}"/>
                <c:if test="${tabCount==1 && empty tabActiveName}">
                    <c:set var="tabActiveName" value="tabComplain" />
                </c:if>
				<li id="litabCrush" class="${tabCount==1?'active':''}">
					<a href="#tabOrderCrush" data-toggle="tab" id="lnktabCrush" onclick="Order.crush_showList('${order.id}','${order.quarter}');">突击单</a></li>
				</li>
			</c:if>
            <c:if test="${order.orderStatus.reminderStatus > 0}">
                <c:set var="tabCount" value="${tabCount+1}"/>
                <c:if test="${tabCount==1}">
                    <c:set var="tabActiveName" value="tabReminder" />
                </c:if>
                <li id="litabReminder" class="${tabCount==1?'active':''}">
                    <a href="#tabReminder" data-toggle="tab" id="lnktabReminder" onclick="Order.showReminderListForKefu('${order.id}','${order.quarter}',undefined,${cuser.userType});">催单</a></li>
                </li>
            </c:if>
		</ul>
		<!-- tab content -->
		<div class="tab-content">
			<!-- service -->
			<c:if test="${order.orderCondition.serviceTimes>0 || (status >= 40 && status<60)}">
				<c:set var="servicepoint" value="${order.orderCondition.servicePoint}" />
			<div class="tab-pane ${tabActiveName=="tabService"?"active":""}" id="tabService" title="实际上门清单">
				<div id="divserviceTable">
					<table id="serviceTable" class="table table-striped table-bordered table-condensed table-hover" style="margin-bottom: 0px;">
						<thead>
							<tr>
								<th rowspan="2" width="30px">序号</th>
								<th rowspan="2" width="100px">日期</th>
								<th rowspan="2" width="60px">上门次数</th>
<%--								<th rowspan="2" width="60px">服务类型</th>--%>
								<th rowspan="2">产品</th>
								<th rowspan="2">品牌</th>
								<th rowspan="2">型号/规格</th>
								<th rowspan="2" width="60px">服务项目</th>
								<th rowspan="2">数量</th>
								<th colspan="6">应收款</th>
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
<%--									<td>${item.serviceCategory.label}</td>--%>
									<td>${item.product.name}
									</td>
									<td>${item.brand}</td>
									<td>${item.productSpec}</td>
									<td><a href="javascript:;" style="cursor:pointer;" data-toggle="tooltip" data-tooltip="${item.errorContent}">${item.serviceType.name}</a></td>
									<td class="tdcenter">${item.qty}</td>
									<c:set var="totalQty" value="${totalQty+item.qty}" />
									<shiro:hasPermission name="sd:order:showreceive">
										<td class="tdcenter">${item.charge}</td>
									</shiro:hasPermission>
									<shiro:lacksPermission name="sd:order:showreceive">
										<td class="tdcenter">*</td>
									</shiro:lacksPermission>
										<td class="tdcenter">${item.materialCharge==0?"":item.materialCharge}</td>
										<td class="tdcenter">${item.expressCharge==0?"":item.expressCharge}</td>
										<td class="tdcenter">${item.travelCharge==0?"":item.travelCharge}</td>
										<td class="tdcenter">${item.otherCharge==0?"":item.otherCharge}</td>
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
										<c:set var="engineerClass" value='${servicepoint.id == item.servicePoint.id?"":"red"}' />
										<td class="tdcenter">${item.engineerServiceCharge}</td>
										<td class="tdcenter">${item.engineerMaterialCharge==0?"":item.engineerMaterialCharge}</td>
										<td class="tdcenter">${item.engineerExpressCharge==0?"":item.engineerExpressCharge}</td>
										<td class="tdcenter">${item.engineerTravelCharge==0?"":item.engineerTravelCharge}
											<c:if test="${!empty item.travelNo}">签核单号:${item.travelNo}</c:if>
										</td>
										<td class="tdcenter">${item.engineerOtherCharge==0?"":item.engineerOtherCharge}</td>
										<td class="tdcenter"><b>${item.engineerChage}</b>
										</td>
										<td class="tdcenter ${engineerClass}">${item.engineer.name}</td>
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
									<td class="tdcenter">
										<c:if test="${materialCharge1 ne 0.00}">
											<span class="alert alert-success">${fns:formatNum(materialCharge1)}</span>
										</c:if>
									</td>
									<td class="tdcenter">
										<c:if test="${expressCharge1 ne 0.00}">
										<span class="alert alert-success">${fns:formatNum(expressCharge1)}</span>
										</c:if>
									</td>
									<td class="tdcenter">
										<c:if test="${travelCharge1 ne 0.00}">
										<span class="alert alert-success">${fns:formatNum(travelCharge1)}</span>
										</c:if>
									</td>
									<td class="tdcenter">
										<c:if test="${otherCharge1 ne 0.00}">
										<span class="alert alert-success">${fns:formatNum(otherCharge1)}</span>
										</c:if>
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
									<td class="tdcenter">
										<c:if test="${materialCharge2 ne 0.00}">
										<span class="alert alert-success">${fns:formatNum(materialCharge2)}</span>
										</c:if>
									</td>
									<td class="tdcenter">
										<c:if test="${expressCharge2 ne 0.00}">
										<span class="alert alert-success">${fns:formatNum(expressCharge2)}</span>
										</c:if>
									</td>
									<td class="tdcenter">
										<c:if test="${travelCharge2 ne 0.00}">
										<span class="alert alert-success">${fns:formatNum(travelCharge2)}</span>
										</c:if>
									</td>

									<td class="tdcenter">
										<c:if test="${otherCharge2 ne 0.00}">
										<span class="alert alert-success">${fns:formatNum(otherCharge2)}</span>
										</c:if>
									</td>
									<td class="tdcenter"><span class="alert alert-info"><strong>${fns:formatNum(totalCharge2)}</strong>
									</span>
									</td>
									<td class="tdcenter"></td>
									<td></td>
								</shiro:hasPermission>
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
			<div class="tab-pane ${tabActiveName=="tabComplain"?"active":""}" id="tabComplain" ></div>
			<!-- 催单 -->
			<div class="tab-pane" ${tabActiveName=="tabReminder"?"active":""} id="tabReminder" title="催单"></div>
			<!-- 突击 -->
			<div class="tab-pane ${tabActiveName=="tabOrderCrush"?"active":""}" id="tabOrderCrush" ></div>
		</div>
	</div>
	</c:if>

	<c:if test="${errorFlag == false}">
	<script type="text/javascript">
		// 回调方法
        // 更新问题反馈标题
        function updateFeedback(feedbackId,title){
            var btnFeeback = $("#btnFeedback");
            if(btnFeedback){

                btnFeeback.attr("title","点击查看问题反馈");
                btnFeeback.attr("onclick","Order.replylist('" + feedbackId + "','" + $("#quarter").val() + "');")
				btnFeeback.html("<i class='icon-comment icon-white'></i> " + title);
                setOrderListRepageFlag();
			}
		}

		// 变更订单列表的刷新标志，在订单详情页关闭时刷新
		function setOrderListRepageFlag(){
            if($("#refreshParent").val()=='true') {
                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                if (iframe != undefined) {
                    $("#repageFlag", iframe.document).val("true");
                }
            }
		}

		function refreshMaterialButton(){
			if($("#btnParts").length == 0) {
                $("#toolbar").prepend('<a id="btnParts" class="btn btn-primary" href="javascript:;" onclick="Order.attachlist(' + "'${order.id}','${order.orderNo}','${order.quarter}'" + ');" title="点击查看配件申请列表"><i class="icon-wrench icon-white"></i> 配件</a>');
                setOrderListRepageFlag();
            }
		}

		function updateService(){
			reload();
		}

		function updateUserServiceInfo(data){
		    //修改用户信息
			if(data){
			    $("#lblservicePhone").text(data.servicePhone);
			    $("#lblserviceAddress").text(data.serviceAddress);
			    $("#lbldescription").text(data.description);
				$("#lblAreaName").text(data.areaName);
                setOrderListRepageFlag();
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
            //setGlobalVar('layerFrameConfig.orderDetail',orderDetail,"json");
            setOrderListRepageFlag();
		}

		function delServiceDetail(id,orderId){
            var clicktag = 0;
            top.layer.confirm(
                '确认要删除该订单服务项目吗？'
                ,{icon: 3,closeBtn: 0,title:'系统确认',success: function(layro, index) {
                    $(document).on('keydown', layro, function(e) {
                        if (e.keyCode == 13) {
                            layro.find('a.layui-layer-btn0').trigger('click')
                        }else if(e.keyCode == 27){
                            layer.close(index);//关闭本身
                        }
                    })
                }}
                ,function(index){
                    if(clicktag == 1){
                        return false;
                    }
                    clicktag = 1;
                    var loadingIndex;
                    var ajaxSuccess = 0;
                    layer.close(index);
                    $.ajax({
                        async: false,
                        cache : false,
                        type : "POST",
                        url : "${ctx}/sd/order/ajaxdelservice?id=" + id +"&orderId=" + orderId,
                        data : null,
                        beforeSend: function () {
                            loadingIndex = layer.msg('正在删除，请稍等...', {
                                icon: 16,
                                time: 0,
                                shade: 0.3
                            });
                        },
                        complete: function () {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            //失败
                            if(ajaxSuccess == 0) {
                                setTimeout(function () {
                                    clicktag = 0;
                                }, 2000);
                            }
                        },
                        success : function(data)
                        {
                            if(ajaxLogout(data)){
                                return false;
                            }
                            if (data.success)
                            {
                                top.layer.msg("订单服务项目删除成功!");
                                reload('tabService');
                                ajaxSuccess = 1;
                            }else
                            {
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                        error : function(e)
                        {
                            ajaxLogout(e.responseText,null,"删除订单服务项目错误，请重试!");
                        }
                    });
                    return false;
                },function(index){//cancel
                    //$btnSubmit.removeAttr('disabled');
                });
            return false
		}

        var tabName = '${tabActiveName}';
        function loadTabContent(){
            $("#lnk"+tabName).trigger("click");
        }
	</script>
	<script class="removedscript" type="text/javascript">
        $(document).ready(function(){
			var logo = Order.getDataSourceLogo("${ctxStatic}","${order.dataSource.value}");
			$("#span_logo").html(logo);
            $(document).ready(function(){
                $('a[data-toggle=tooltip]').darkTooltip({gravity : 'north'});
                $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
            });
			// toolbar
			var orderInfo = ${fns:toGson(order)};
			orderInfo.orderCondition.quarter = orderInfo.quarter;
            Order.detail_order_process(orderInfo); //process
			orderInfo.orderCondition.layerIndex = orderdetail_index;//layer index of this dialog
            Order.detailPlan_toolbar(orderInfo.orderCondition); //button
			//load active tab
			//图片预览
			$("div.viewer-container").remove();
			$.each($("#productTable").find("div.gallery-thumb"),function(i,thumb){
				$(thumb).viewer('destroy').viewer({ url: "data-original"});
			});
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
	</script>
</body>
<script type="text/javascript">
    $(document).ready(function() {
        $(".removedscript").remove();//移除初始化脚本
		//短信控制
		//Clipboard
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
			layerMsg("厂商信息复制成功");
		});
		clip_order.on('error', function(e) {
			layerError("厂商信息复制失败： <br/>" + JSON.stringify(e.message));
		});

		//engineer
		var tmpl = $('#txengineertmsg').val();
		var doTtmpl = doT.template(tmpl);
		var data = {'servicePhone':'','serviceAddress':''};
		data.servicePhone = $("#lblservicePhone").text();
		data.serviceAddress = $("#lblserviceAddress").text();
		var html = doTtmpl(data);
		var engineerMsg = html.replace(/~n/g,'\n');
		// var engineerMsg = html.replace(new RegExp(/\\n /g,"gm"),"\n");
		$("#btn_engineer_copy").attr('data-clipboard-text',engineerMsg);
		var clip_engineer = new ClipboardJS('#btn_engineer_copy');
		clip_engineer.on('success', function(e) {
			layerMsg("安维短信复制成功");
		});
		clip_engineer.on('error', function(e) {
			layerError("安维短信复制失败： <br/>" + JSON.stringify(e.message));
		});

        // 用户未接电话
        $('#btn_sendMsgToUser').click(function () {
            var $btn = $("#btn_sendMsgToUser");
            if ($btn.prop("disabled") == true) {
                return false;
            }
            $btn.prop("disabled", true);
            top.layer.confirm(
                '确认要发送短信通知用户吗？'
                , {
                    icon: 3, closeBtn: 0, title: '系统确认', success: function (layro, index) {
                        //回车事件，默认触发第一个按钮click
                        $(document).on('keydown', layro, function (e) {
                            if (e.keyCode == 13) {
                                layro.find('a.layui-layer-btn0').trigger('click');
                                top.layer.close(index);//ESC关闭本身
                            } else if (e.keyCode == 27) {
                                $btn.removeAttr('disabled');
                                top.layer.close(index);//ESC关闭本身
                            }
                            //此处 不能返回false ，不然就会使输入框等不能输入;
                            return true;
                        });
                    }
                }
                , function (index) {
                    layer.close(index);
                    var mobile = "${order.orderCondition.servicePhone}";
                    var msg = $('#txtSendUserMsg').val();
                    $.ajax({
                        cache: false,
                        type: "POST",
                        url: "${ctx}/mq/shortmessage/send",
                        data: {mobile: mobile, content: msg},
                        success: function (data) {
                            if (ajaxLogout(data)) {
                                return false;
                            }
                            if (data.success) {
                                top.layer.msg("短信已发出!");
                            } else {
                                layerError(data.message, "错误提示");
                            }
                            $btn.removeAttr('disabled');
                            return false;
                        },
                        error: function (e) {
                            $btn.removeAttr('disabled');
                            ajaxLogout(e.responseText, null, "短信发送失败，请重试!");
                        }
                    });
                    return false;
                }, function (index) {//cancel
                    $btn.removeAttr('disabled');
                });
            return false
        });
	});
</script>
</html>