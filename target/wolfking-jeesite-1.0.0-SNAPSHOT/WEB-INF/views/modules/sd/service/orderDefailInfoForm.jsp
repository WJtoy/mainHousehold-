<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>订单详细信息(客户)</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default" />
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
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
	<%@ include file="/WEB-INF/views/modules/sd/service/tpl/orderDetail.html" %>
	<%@ include file="/WEB-INF/views/modules/sd/crush/tpl/orderCrushList.html" %>
	<%@ include file="/WEB-INF/views/modules/sd/tpl/orderAuxiliaryMaterialList.html" %>
	<%@ include file="/WEB-INF/views/modules/sd/service/tpl/reminderConfirmItem.html" %>
	<style type="text/css">
	.table thead th,.table tbody td {text-align: center;vertical-align: middle;}
	.tdlable {width:160px;align: right;}
	.tdbody {width:300px;}
	.table th,.table td {padding: 4px;}
	.table thead th {text-align: center;vertical-align: middle;}
	.table .tdcenter {text-align: center;vertical-align: middle;}
	.alert {padding: 4px 5px 4px 4px;}
	.gallery-thumb {position: relative;cursor: pointer;padding: 5px;}
	.gallery-thumb img {width: 30px;height: 30px;margin-right: 5px;}
	</style>

<script type="text/javascript">
	<c:set var="tabActiveName" value="${empty param.activeTab?'tabTracking':param.activeTab}" />
	<c:set var="cuser" value="${fns:getUser()}" />
    Order.rootUrl = "${ctx}";
    var orderdetail_index = parent.layer.getFrameIndex(window.name);
    $(document).ready(function(){
        $('a[data-toggle=tooltip]').darkTooltip();
        $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
        $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
        //图片预览
		$("div.viewer-container").remove();
		$.each($("#itemTable").find("div.gallery-thumb"),function(i,thumb){
			$(thumb).viewer('destroy').viewer({ url: "data-original"});
		});

		$('#btnCopy').click(function() {
			var html = "<div style='padding:10px;'><textarea id='txtmsg' name='txtmsg' rows='6' class='input-xxlarge'>"
					+ $('#txengineertmsg').val()
					+ "</textarea> </div>";
			top.$.jBox.open(html, '发给安/维人员的短信信息', 'auto',
					'auto',
					{
						top : '10px',
						buttons : {}
					});
			return false;
		});
		$('#btnCopyUser').click(function() {
			var html = "<div style='padding:10px;'><textarea id='txtmsg' name='txtmsg' rows='6' class='input-xxlarge'>"
					+ $('#txtusermsg').val()
					+ "</textarea> </div>";
			top.$.jBox.open(html, '发给用户的短信信息', 'auto', 'auto',
			{
				top : '10px',
				buttons : {}
			});
			return false;
		});
		//clipboard
        //engineer
		var engineerMsg = $('#txengineertmsg').val();
		$("#btn_engineer_copy").attr('data-clipboard-text',engineerMsg);
		var clip_engineer = new ClipboardJS('#btn_engineer_copy');
		clip_engineer.on('success', function(e) {
			layerMsg("安维短信复制成功");
		});
		clip_engineer.on('error', function(e) {
			layerError("安维短信复制失败： <br/>" + JSON.stringify(e.message));
		});
        //user
		var userMsg = $('#txtusermsg').val();
		$("#btn_user_copy").attr('data-clipboard-text',userMsg);
		var clip_user = new ClipboardJS('#btn_user_copy');
		clip_user.on('success', function(e) {
			layerMsg("用户短信复制成功");
		});
		clip_user.on('error', function(e) {
			layerError("用户短信复制失败： <br/>" + JSON.stringify(e.message));
		});
        //order
		var orderMsg = $('#txtordermsg').val();
		//换行处理
		// orderMsg = orderMsg.replace(/~n/g,'\n');
		orderMsg = orderMsg.replace(new RegExp(/~n/g,"gm"),"\n");
		$("#btn_order_copy").attr('data-clipboard-text',orderMsg);
		var clip_order = new ClipboardJS('#btn_order_copy');
		clip_order.on('success', function(e) {
			layerMsg("订单信息复制成功");
		});
		clip_order.on('error', function(e) {
			layerError("订单信息复制失败： <br/>" + JSON.stringify(e.message));
		});

		if(!Utils.isEmpty(tabName)) {
			setTimeout('loadTabContent()',100);
		}
	});

    // 更新问题反馈标题
    function updateFeedback(feedbackId, title) {
        var btnFeeback = $("#btnFeedback");
        if (btnFeedback) {
            btnFeeback.attr("title", "点击查看问题反馈");
            btnFeeback.attr("onclick", "Order.replylist('" + feedbackId + "','${order.quarter}');")
            btnFeeback.html(title);
        }
    }

    function reloadComplain() {
        Order.showComplainList('${order.id}','${order.quarter}');
    }
	//申诉成功回调方法
    function appealSuccessCallback(id){
    	$("#lbl" + id).text("申诉");
    	$("#lnk" + id).remove();
	}

    function cancelComplain(complainId,quarter){
        //top.layer.close(this_index);
        layer.confirm(
            '确定撤销该投诉单吗？'
            ,{icon: 3, title:'系统确认',success: function(layro, index) {
                    $(document).on('keydown', layro, function(e) {
                        if (e.keyCode == 13) {
                            Order.doCancelComplain('${order.id}',complainId,quarter);
                        }else if(e.keyCode == 27){
                            layer.close(index);//关闭本身
                        }
                    })
                }
            }
            ,function(index) {
                layer.close(index);//关闭本身
                //top.layer.close(this_index);
                Order.doCancelComplain('${order.id}',complainId,quarter);
            }
            ,function(index) {});
        return false;
    };

</script>
</head>
<body>
	<!-- new -->
	<form:form id="inputForm" action="#" method="post" class="form-horizontal">
		<sys:message content="${message}" />
		<c:if test="${errorFlag == false}">
			<c:set var="isCustomer" value="${!empty cuser && cuser.isCustomer()?true:false }" />
			<input type="hidden" id="isCustomer" name="isCustomer" value="${isCustomer}" />
		<c:set var="cancopy" value="${order.canService()}" />
		<!-- order head -->
		<div class="accordion-group" style="margin-top:2px;">
			<div class="accordion-heading">
				<c:set var="msg" value="" />
				<c:forEach items="${order.items}" var="item">
					<c:set var="msg" value="${msg} ${item.brand} ${item.product.name}${item.qty }${item.serviceType.name}" />
				</c:forEach>
				<c:set var="ordermsg" value="单号:${order.orderNo}  ${order.orderCondition.customer.name}" />
				<c:if test="${order.dataSource.value != '0'}">
					<c:set var="ordermsg" value="${ordermsg}(${order.dataSource.label}${order.b2bShop==null || order.b2bShop.shopId == ''?'':'-'.concat(order.b2bShop.shopName)}${not empty order.parentBizOrderId?'-'.concat(order.parentBizOrderId):''})" />
				</c:if>
                <c:set var="ordermsg" value="${ordermsg} ~n联系人:${order.orderCondition.userName}  ${order.orderCondition.servicePhone}  ${order.orderCondition.area.name} ${order.orderCondition.serviceAddress} ~n服务明细:${msg} ~n---(反馈):" />
				<input type="hidden" id="txtordermsg" name="txtordermsg" value="${ordermsg}" />
				<c:if test="${cancopy eq true }">
					<c:set var="usermsg" value="您的${msg }，${fn:substring(order.orderCondition.engineer.name,0,1)}师傅${order.orderCondition.engineer.mobile}已接单，客服${fn:substring(order.orderCondition.kefu.name,0,1)}小姐${order.orderCondition.kefu.phone}/${fourServicePhone}"/>
					<c:set var="msg" value="${order.orderCondition.userName}${order.orderCondition.servicePhone }${' '}${order.orderCondition.area.name} ${order.orderCondition.serviceAddress} ${msg}请2小时内联系用户确认环境并预约，48小时内上门，严禁对产品作任何评价，有问题请联系客服" />
					<c:set var="msg" value="${msg}${fn:substring(order.orderCondition.kefu.name,0,1)}${'小姐'}${order.orderCondition.kefu.phone}/${fourServicePhone}。" />
					<input type="hidden" id="txengineertmsg" name="txengineertmsg" value="${msg }" />
					<input type="hidden" id="txtusermsg" name="txtusermsg" value="${usermsg}" />
					<c:if test="${cancopy eq true }">
					<shiro:hasPermission name="sd_order_message_btn">
						<a style="margin-left: 200px;" href="javascript:void(0)"
							id="btn_engineer_copy" class="btn btn-success btn-mini">复制安维短信</a>
						<a style="margin-left: 10px;" href="javascript:void(0)"
							id="btn_user_copy" class="btn btn-success btn-mini">复制用户短信</a>
					</shiro:hasPermission>
					</c:if>
				</c:if>
				<c:if test="${cancopy eq true }">
					<a style="margin-left: 10px;" href="javascript:void(0)"
						id="btn_order_copy" class="btn btn-success btn-mini">复制订单信息</a>
				</c:if>
				<c:if test="${cancopy ne true }">
					<a style="margin-left: 320px;" href="javascript:void(0)"
						id="btn_order_copy" class="btn btn-success btn-mini">复制订单信息</a>
				</c:if>
				<!-- 2019/06/12 -->
				<!-- 完成照片-->
				<c:if test="${order.orderCondition.finishPhotoQty > 0}">
				<a class="btn btn-primary btn-mini" id="btnFinishPhoto" href="javascript:;" onclick="Order.browsePhotolist('${order.id}','${order.quarter}');" title="点击浏览完成照片">完成照片</a>
				</c:if>
				<!-- 问题反馈-->
				<c:choose>
					<c:when test="${order.orderCondition.feedbackId >0 }">
						<a class="btn btn-warning btn-mini" id="btnReply" style="width: 150px;" href="javascript:;" onclick="Order.replylist('${order.orderCondition.feedbackId}','${order.quarter}','${order.orderNo}');" title="点击查看反馈内容">${fns:abbr(order.orderCondition.feedbackTitle,25)}</a>
					</c:when>
					<c:otherwise>
						<a class="btn btn-warning btn-mini" id="btnFeedback" style="width: 150px;" href="javascript:;" onclick="Order.feedback('${order.id}','${order.quarter}',orderdetail_index);" title="点击添加反馈内容">问题反馈</a>
					</c:otherwise>
				</c:choose>
				<a href="#divheader" class="accordion-toggle" data-toggle="collapse">基本信息
					<span class="arrow"></span>
				</a>
			</div>
			<div id="divheader" class="accordion-body">
				<table class="table table-bordered table-striped table-hover" style="margin-bottom: 0px;">
					<tbody>
						<tr>
							<td class="tdlable">
								<label class="control-label">订单编号:</label>
							</td>
							<td class="tdbody">
								<span id="spOrderNo">${order.orderNo}</span><br>
								<span class="alert alert-info">${order.orderCondition.status.label} </span>
								<c:if test="${order.orderCondition.urgentLevel.id >0}">
									<a data-toggle="tooltip" data-tooltip="${order.orderCondition.urgentLevel.remarks}<br/>加急费：${order.orderFee.customerUrgentCharge}元" class='label label-important' style="background-color: ${order.orderCondition.urgentLevel.markBgcolor};">加急</a>
								</c:if>
							</td>
							<td class="tdlable">
								<label class="control-label">客户名称:</label>
							</td>
							<td class="tdbody">
								<a href="javascript:" data-toggle="tooltipeast" data-tooltip="${order.orderCondition.customer.remarks}">
										${order.orderCondition.customer.name}
                                </a>
                                <c:if test="${order.dataSource.value != '0'}">
                                    <br>${order.dataSource.label}${order.b2bShop==null || order.b2bShop.shopId == ""?"":"-".concat(order.b2bShop.shopName)}
                                </c:if>
							</td>
						</tr>
						<tr>
							<td class="tdlable"><label class="control-label">联系人:</label></td>
							<td class="tdbody">${order.orderCondition.userName}</td>
							<td class="tdlable"><label class="control-label">手机:</label></td>
							<td class="tdbody">${order.orderCondition.phone1}</td>
						</tr>
						<tr>
							<td class="tdlable"><label class="control-label">座机:</label></td>
							<td class="tdbody">${order.orderCondition.phone2}</td>
							<td class="tdlable"><label class="control-label">实际联络电话:</label></td>
							<td class="tdbody">${order.orderCondition.servicePhone}</td>
						</tr>
						<tr>
							<td class="tdlable"><label class="control-label">用户地址:</label></td>
							<td class="tdbody">${order.orderCondition.area.name} ${order.orderCondition.address}</td>
							<td class="tdlable"><label class="control-label">实际上门地址:</label></td>
							<td class="tdbody">${order.orderCondition.area.name}${order.orderCondition.serviceAddress}</td>
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
		<shiro:hasPermission name="sd:order:showserviceinfo">
			<div class="accordion-group" style="margin-top:2px;">
				<div class="accordion-heading">
					<a href="#divservice" class="accordion-toggle collapsed"
						data-toggle="collapse">客服信息 <span class="arrow"></span>
					</a>
				</div>
				<c:set var="service" value="${order.orderCondition.kefu}" />
				<div id="divservice" class="accordion-body collapse">
					<table class="table table-bordered table-striped table-hover"
						style="margin-bottom: 0px;">
						<tbody>
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
								<td class="tdlable"><label class="control-label">手机号:</label>
								</td>
								<td class="tdbody">${empty service?'':order.orderCondition.kefu.mobile}</td>
							</tr>
							<tr>
								<td class="tdlable"><label class="control-label">电话:</label>
								</td>
								<td class="tdbody">${empty service?'':order.orderCondition.kefu.phone}
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
						</tbody>
					</table>
				</div>
			</div>
		</shiro:hasPermission>
		<c:if test="${order.orderCondition.servicePoint != null && order.orderCondition.servicePoint.id != null}">
		<shiro:hasPermission name="sd:order:showengineerinfo">
			<div class="accordion-group" style="margin-top:2px;">
				<div class="accordion-heading">
					<a href="#divengineer" class="accordion-toggle"
						data-toggle="collapse">安维人员 <span class="arrow"></span>
					</a>
				</div>
				<div id="divengineer" class="accordion-body">
					<table class="table table-bordered table-striped table-hover"
						style="margin-bottom: 0px;">
						<tbody>
							<tr>
								<td class="tdlable"><label class="control-label">网点编号:</label>
								</td>
								<td class="tdbody">${order.orderCondition.servicePoint.servicePointNo}
									<c:if test="${!empty order.orderCondition.servicePoint.finance.bankIssue && order.orderCondition.servicePoint.finance.bankIssue.label ne '无' && !empty order.orderCondition.servicePoint.finance.bankIssue.label}">
										<label id="lbltotalCharge" class="alert alert-error">${order.orderCondition.servicePoint.finance.bankIssue.label }</label>
									</c:if>
								</td>
								<td class="tdlable"><label class="control-label">姓名:</label>
								</td>
								<td class="tdbody">
										${order.orderCondition.servicePoint.primary.name}(主)
								</td>
							</tr>
							<tr>
								<td class="tdlable"><label class="control-label">手机号:</label>
								</td>
								<td class="tdbody">${order.orderCondition.servicePoint.primary.contactInfo}</td>
								<td class="tdlable"><label class="control-label">电话:</label>
								</td>
								<td class="tdbody"></td>
							</tr>
							<tr>
								<td class="tdlable"><label class="control-label">联络方式1:</label>
								</td>
								<td class="tdbody">${order.orderCondition.servicePoint.contactInfo1}</td>
								<td class="tdlable"><label class="control-label">联络方式2:</label>
								</td>
								<td class="tdbody">${order.orderCondition.servicePoint.contactInfo2}</td>
							</tr>
							<tr>
								<td class="tdlable"><label class="control-label">安维结算方式:</label>
								</td>
								<td class="tdbody">${order.orderFee.engineerPaymentType.label}</td>
								<td class="tdlable"><label class="control-label">订单结算方式:</label>
								</td>
								<td class="tdbody">${order.orderFee.orderPaymentType.label}</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</shiro:hasPermission>
		</c:if>
		</c:if>
	</form:form>
	<br />
	<c:if test="${errorFlag == false}">
	<div class="tabbable" style="margin:0px 20px">
		<ul class="nav nav-tabs">
			<c:set var="tabCount" value="${tabCount+1}"/>
			<c:if test="${tabCount==1 && empty tabActiveName}"><c:set var="tabActiveName" value="tabTracking"/></c:if>
			<li id="litabTracking" class="${tabCount==1?'active':''}">
				<a href="#tab1" data-toggle="tab">产品详细清单</a>
			</li>
			<c:if test="${order.orderCondition.status.value !='90' && order.orderCondition.status.value !='100' && order.orderCondition.serviceTimes>0}">
			<li><a href="#tabService" data-toggle="tab">实际上门清单</a></li>
			</c:if>
			<li>
                <a href="#tabTracking" data-toggle="tab" id="tab4link" onclick="Order.showTrackingLogs('${order.id}','${order.quarter}','${isCustomer}');">跟踪进度</a>
            </li>
			<shiro:hasPermission name="sd:orderdetail:exception">
				<li><a href="#tabException" data-toggle="tab" id="tab5link" onclick="Order.showExceptLogs('${order.id}','${order.quarter}');">异常处理</a></li>
			</shiro:hasPermission>
			<c:if test="${order.orderCondition.feedbackId != 0}">
			<li><a href="#tabFeedback" data-toggle="tab" onclick="Order.showFeedbackLogs('${order.orderCondition.feedbackId}','${order.quarter}');">反馈处理</a></li>
			</c:if>
			<c:if test="${order.writeOff == 1 or order.writeOff == 3}">
                <c:set var="tabCount" value="${tabCount+1}"/>
				<li>
                    <a href="#tabCustomerReturn" data-toggle="tab" onclick="Order.showCustomerReturnAndAdditionalList('${order.id}','${order.orderNo}','${order.quarter}');">退补</a>
                </li>
			</c:if>
			<%-- 投诉转移到orderStatus--%>
			<c:if test="${order.orderStatus.complainFlag>0}">
				<c:set var="tabCount" value="${tabCount+1}"/>
				<c:if test="${tabCount==1 && empty tabActiveName}">
					<c:set var="tabActiveName" value="tabComplain" />
				</c:if>
				<li id="litabComplain">
					<a href="#tabComplain" data-toggle="tab" id="lnktabComplain" onclick="Order.showComplainList('${order.id}','${order.quarter}');">投诉</a></li>
				</li>
			</c:if>
            <c:if test="${order.orderCondition.rushOrderFlag!=0}">
			    <shiro:hasAnyPermissions name="sd:orderdetail:crush,sd:orderCrush:edit,sd:ordercrush:view">
                    <c:set var="tabCount" value="${tabCount+1}"/>
					<li id="litabCrush">
						<a href="#tabOrderCrush" data-toggle="tab" id="lnktabCrush" onclick="Order.crush_showList('${order.id}','${order.quarter}');">突击单</a></li>
					</li>
			    </shiro:hasAnyPermissions>
            </c:if>
            <c:if test="${order.orderStatus.reminderStatus > 0}">
                <c:set var="tabCount" value="${tabCount+1}"/>
                <c:if test="${tabCount==1 && empty tabActiveName}">
                    <c:set var="tabActiveName" value="tabReminder" />
                </c:if>
                <li id="litabReminder">
                    <a href="#tabReminder" data-toggle="tab" id="lnktabReminder" onclick="Order.showReminderListForCustomer('${order.id}','${order.quarter}',undefined);">催单</a></li>
                </li>
            </c:if>
			<c:if test="${hasAuxiliaryMaterils == 1}">
				<c:set var="tabCount" value="${tabCount+1}"/>
				<li id="litabAuxiliaryMaterials" class="${tabCount==1?'active':''}">
					<a href="#tabAuxiliaryMaterials" data-toggle="tab" id="lnktabAuxiliaryMaterials" onclick="Order.auxiliaryMaterial_showDetailInfo('${order.id}','${order.quarter}');">辅材收费</a></li>
				</li>
			</c:if>
			<c:if test="${praiseFlag==1}">
				<li id="litabPraise">
					<a href="#tabPraise" data-toggle="tab" id="lnktabPraise" onclick="showPraise('${order.id}','${order.quarter}','${order.orderCondition.servicePoint.id}')">好评</a></li>
				</li>
			</c:if>
		</ul>
		<div class="tab-content">
			<div class="tab-pane active" id="tab1" title="产品详细清单">
				<div class="control-group">
					<table id="itemTable"
						class="table table-striped table-bordered table-condensed table-hover"
						style="margin-bottom: 0px;">
						<thead>
							<tr>
								<th width=30px>序号</th>
								<th>服务类型</th>
								<th>产品</th>
								<th>品牌</th>
								<th>型号/规格</th>
								<th>产品图片</th>
								<th>数量</th>
								<shiro:hasPermission name="sd:order:showreceive">
									<th>服务金额</th>
									<th>冻结金额</th>
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
									<shiro:hasPermission name="sd:order:showreceive">
										<td>${item.charge }</td>
										<td>${item.blockedCharge }</td>
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
						</tbody>
					</table>
				</div>
				<shiro:hasPermission name="sd:order:showreceive">
					<!-- 客服不显示金额 -->
					<c:if test="${cuser.userType ne 2 }">
						<div class="control-group">
							<table class="table table-bordered table-condensed table-hover">
								<tr>
									<td style="text-align:right;">服务金额</td>
									<td width="200px" style="align:left;">
										<label id="lblassignedCharge">${order.orderFee.expectCharge }</label>
									</td>
								</tr>
								<tr>
									<td style="text-align:right;">冻结金额</td>
									<td style="align:left;">
										<label id="lblblockedCharge">${order.orderFee.blockedCharge}</label>
									</td>
								</tr>
								<tr>
									<td style="text-align:right;">数量总计</td>
									<td style="align:left;">
										<label id="lbltotalQty">${totalQty}</label>
									</td>
								</tr>
								<tr>
									<td style="text-align:right;">总计</td>
									<td style="align:left;">
										<label id="lbltotalCharge" class="alert alert-success">${order.orderFee.expectCharge+order.orderFee.blockedCharge}</label>
									</td>
								</tr>
							</table>
						</div>
					</c:if>
				</shiro:hasPermission>
			</div>
			<div class="tab-pane" id="tabService" title="实际上门清单">
				<div class="control-group">
					<table id="productTable"
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
									<td>${item.product.name}</td>
									<td>${item.brand}</td>
									<td>${item.productSpec}</td>
									<td class="tdcenter">${item.qty}</td>
									<c:set var="totalQty" value="${totalQty+item.qty}" />
									<shiro:hasPermission name="sd:order:showreceive">
										<td class="tdcenter">${item.charge}</td>
										<td class="tdcenter">${item.materialCharge}</td>
										<td class="tdcenter">${item.expressCharge}</td>
										<td class="tdcenter">${item.travelCharge}</td>
										<td class="tdcenter">${item.otherCharge}</td>
										<td class="tdcenter"><b>${item.customerCharge}</b>
										</td>
										<c:set var="charge1" value="${charge1 + item.charge}" />
										<c:set var="materialCharge1" value="${materialCharge1 + item.materialCharge}" />
										<c:set var="expressCharge1" value="${expressCharge1 + item.expressCharge}" />
										<c:set var="travelCharge1" value="${travelCharge1 + item.travelCharge}" />
										<c:set var="otherCharge1" value="${otherCharge1 + item.otherCharge}" />
										<c:set var="totalCharge1" value="${totalCharge1 + item.customerCharge}" />
									</shiro:hasPermission>
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
								<td class="tdcenter"><span class="alert alert-success"><strong>${totalQty}</strong>
								</span>
								</td>
								<shiro:hasPermission name="sd:order:showreceive">
									<td class="tdcenter"><span class="alert alert-success">${charge1}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-success">${materialCharge1}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-success">${expressCharge1}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-success">${travelCharge1}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-success">${otherCharge1}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-info"><strong>${totalCharge1}</strong>
									</span>
									</td>
								</shiro:hasPermission>
								<shiro:hasPermission name="sd:order:showpayment">
									<td class="tdcenter"><span class="alert alert-success">${charge2}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-success">${materialCharge2}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-success">${expressCharge2}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-success">${travelCharge2}</span>
									</td>

									<td class="tdcenter"><span class="alert alert-success">${otherCharge2}</span>
									</td>
									<td class="tdcenter"><span class="alert alert-info"><strong>${totalCharge2}</strong>
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
			<!-- 投诉 -->
			<div class="tab-pane" id="tabComplain" title="投诉单">
			</div>
			<!-- 催单 -->
			<div class="tab-pane" id="tabReminder" title="催单">
			</div>
			<%--突击--%>
			<div class="tab-pane ${tabActiveName=="tabOrderCrush"?"active":""}" id="tabOrderCrush" >
			</div>
			<!-- 附加费用 -->
			<div class="tab-pane ${tabActiveName=="tabAuxiliaryMaterials"?"active":""}" id="tabAuxiliaryMaterials"></div>
			<%--好评费--%>
			<div class="tab-pane" id="tabPraise"></div>
		</div>
	</div>
		<script type="text/javascript">
			var tabName = '${tabActiveName}';
			function loadTabContent(){
				$("#lnk"+tabName).trigger("click");
			}

            //装载好评单
            function showPraise(orderId,quarter,servicePointId) {
                var parentLayerIndex = top.layer.getFrameIndex('layer_orderdetail');
                var loadingIndex = top.layer.msg('正在加载好评单...', {
                    icon: 16,
                    time: 0,//不定时关闭
                    shade: 0.3
                });
                $.ajax({
                    cache: false,
                    type: "GET",
                    url: "${ctx}/customer/praise/ajax/getPraiseForCustomer?orderId=" + orderId +"&quarter=" + (quarter || '') + "&servicePointId=" + servicePointId,
                    dataType: 'json',
                    success: function (data) {
                        top.layer.close(loadingIndex);
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if (data.success) {
                            if(data.data) {
                                var tmpl = document.getElementById('tpl-praise').innerHTML;
                                var doTtmpl = doT.template(tmpl);
                                var item = {parentLayerIndex:parentLayerIndex,data:data.data};
                                var html = doTtmpl(item);
                                $("#tabPraise").html(html);
                                //让模板的自定义提示生效
                                $('a[data-toggle=tooltip]').darkTooltip();
                                $('#picDiv').viewer();
                            }else {
                                $("#tabPraise").empty().append("<table id='litabPraise' style='display:none;'></table>无记录");
                            }
                        }else{
                            layerError(data.message);
                        }
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText,null,"装载好评单出错，请重试!");
                        top.layer.close(loadingIndex);
                    }
                });
            }
			
		</script>
	</c:if>
</body>
</html>