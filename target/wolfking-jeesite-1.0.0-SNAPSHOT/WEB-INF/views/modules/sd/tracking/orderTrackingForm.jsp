<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>跟踪进度</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<script type="text/javascript">
		Order.rootUrl = "${ctx}";
		var this_index = top.layer.index;

		function closeme(){
		    top.layer.close(this_index);
		}

		function saveTacking(){
		    Order.saveTacking("refresh",this_index);
		}

		function orderConfirmDoor(id,quarter){
		    Order.orderConfirmDoor(id,quarter,this_index,0);
		}

		$(document).ready(function() {

			var trackings = []; 
			$(document).on("change", "[name='trackingIds']", function() {
				 var id = $(this).prop("id");
				 var val = $("label[for='"+id+"']").text();
				 if($(this).prop("checked") == true){ 
					var checked = jQuery.inArray(val, trackings); 
		            if (checked == -1) { 
		            	trackings.push(val); 
		            }
				 }
				 else{
					 trackings = $.grep(trackings, function (item, index) { 
		                    return item != val; 
		                }); 
				 }
				 $("#remarks").val(trackings.join(","));
			});
			
		});

		function repage()
		{
			return false;
		}

		  $(document).ready(function()
			{
				$('a[data-toggle=tooltip]').darkTooltip();
				$('a[data-toggle=tooltipnorth]').darkTooltip(
				{
					gravity : 'north'
				});
				$('a[data-toggle=tooltipeast]').darkTooltip(
				{
					gravity : 'east'
				});
			});
</script>
</head>
<body>
	<div class="accordion-group" style="margin-top:2px; table-layout: fixed;" >
    		<div class="accordion-heading">
			 <a href="#divheader" class="accordion-toggle" data-toggle="collapse">基本信息 <span class="arrow"></span></a>
			</div>
			<div id="divheader" class="accordion-body">
				<table class="table table-bordered table-striped" style="margin-bottom: 0px;">
				<tbody>
					<tr>
						<td class="tdlable"><label class="control-label">联系人:</label></td>
						<td class="tdbody">${order.orderCondition.userName}</td>
						<td class="tdlable"><label class="control-label">实际联系电话:</label></td>
						<td class="tdbody">${order.orderCondition.servicePhone}
						<input id="btnCall" class="btn" type="button" value="拔号" onclick="javascript:StartDial(${order.orderCondition.servicePhone},false)" />
						</td>
					</tr>
					<tr>
						<td class="tdlable"><label class="control-label">实际上门地址:</label></td>
						<td class="tdbody">${order.orderCondition.area.name}${order.orderCondition.serviceAddress}</td>
						<td class="tdlable"><label class="control-label">座机:</label></td>
						<td class="tdbody">${order.orderCondition.phone2}</td>
					</tr>
					<tr>
						<td class="tdlable"><label class="control-label">服务描述:</label></td>
						<td class="tdbody" colspan="3">${order.description}</td>
					</tr>
					<tr>
						<td class="tdlable"><label class="control-label">安维姓名:</label></td>
						<td class="tdbody">${order.orderCondition.engineer.name}</td>
						<td class="tdlable"><label class="control-label">安维手机/电话:</label></td>
						<td class="tdbody">${order.orderCondition.engineer.mobile}
						<input id="btnCall" class="btn" type="button" value="拔号" onclick="javascript:StartDial(${order.orderCondition.engineer.mobile},false)" />
						</td>
					</tr>
				</tbody>
				</table>
			</div>
		</div>
	<div class="control-group" style="table-layout: fixed;">
				<table id="orderItemTable" class="table table-striped table-bordered table-condensed" style="margin-bottom: 0px;">
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
				<c:forEach items="${order.items}" var="item" varStatus="i" begin="0">
					<tr>
						<td>${i.index+1}</td>
						<td>${item.serviceType.name }</td>
						<td>${item.product.name }</td>
						<td>${item.brand }</td>
						<td>${item.productSpec }</td>
						<td>${item.qty }</td>
						<td><a href="http://www.kuaidi100.com/chaxun?com=${item.expressCompany.value}&nu=${item.expressNo }" target="_blank" title="点击进入快递100">
							${fitem.expressCompany.label}  ${item.expressNo }
							</a>
						</td>
						<td>
							<a href="javascript:void(0);" data-fancybox-type="iframe" 
								style="margin-left:30px;" onclick="Order.getCustomerPrice('${order.orderCondition.customer.id}', '${item.product.id}','${item.serviceType.id}');">
									<abbr title="点击查看产品说明">查看</abbr>
								</a>
						</td>
					</tr>
				</c:forEach>
				<tr>
					<td colspan="2">客户说明</td>
					<td colspan="6">${order.orderCondition.customer.remarks}</td>
				</tr>
				</tbody>
				</table>
	</div>
	<legend>订单上门情况</legend>
	<div class="control-group" style="table-layout: fixed;">
				<table id="productTable" class="table table-striped table-bordered table-condensed" style="margin-bottom: 0px;">
				<thead>
					<tr>
						<th width=30px>上门次数</th>
						<th>服务类型</th>
						<th>产品</th>
						<th>数量</th>
						<th>服务费</th>
						<th>快递费</th>
						<th>远程费</th>
						<th>配件费</th>
						<th>其他</th>
						<th>小计</th>
						<th>安维</th>
						<th>配件</th>
					</thead>	
				<tbody>
				<c:forEach items="${order.detailList}" var="detail">
					<tr>
						<td>${detail.serviceTimes}</td>
						<td>${detail.serviceType.name}</td>
						<td>${detail.product.name }</td>
						<td>${detail.qty}</td>
						<td>${detail.engineerServiceCharge}</td>
						<td>${detail.engineerExpressCharge}</td>
						<td>${detail.engineerTravelCharge}</td>
						<td>${detail.engineerMaterialCharge}</td>
						<td>${detail.engineerOtherCharge}</td>
						<td>${detail.engineerTotalCharge}</td>
						<td>${detail.engineer.name}</td>
						<td>
							<c:if test="${order.orderCondition.partsFlag == 1}">
								<a href="javascript:void(0);" data-fancybox-type="iframe" style="margin-left:30px;" onclick="Order.attachlist('${order.id}','${order.orderNo}','${order.quarter}');">
									<abbr title="点击查看配件申请列表">查看</abbr>
								</a>
								<a title="新增配件申请" id="btn_MaterialApply"  class="btn btn-mini btn-primary" onclick="Order.materialApply(${order.id},${detail.id});" href="javascript:void(0);"  title="新增配件申请">申请</a>
								
							</c:if>
						</td>
					</tr>
				</c:forEach>
				</tbody>
				</table>
	</div>
	
	
	<table id="contentTable" class="table table-striped table-bordered table-condensed">
		<thead><tr><th width="50px">序号</th><th width="120px">跟踪日期</th><th>客服跟踪内容</th><th>厂商跟踪项目</th><th width="80px">跟踪人员</th></tr></thead>
		<tbody>
		<c:if test="${!empty order.id }">
		<c:set var="rowNumber" value="0" />
		<c:forEach items="${order.logList}" var="track">
			<c:set var="rowNumber" value="${rowNumber+1}" />
			<tr>
				<td>${rowNumber}</td>
				<td><fmt:formatDate value="${track.createDate}" pattern="yyyy-MM-dd HH:mm"/></td>				
				<c:choose>
					<c:when test="${track.statusFlag eq 4}">
						<td>${fns:escapeHtml(track.actionComment)}</td>
						<td>${fns:escapeHtml(track.remarks)}</td>
					</c:when>
					<c:otherwise>
						<td></td>
						<td>${fns:escapeHtml(track.actionComment)}</td>
					</c:otherwise>
				</c:choose>
				<td>${fns:escapeHtml(track.createBy.name)}</td>
			</tr>
		</c:forEach>
		</c:if>
		</tbody>
	</table>
	<sys:message content="${message}" />
	<c:if test="${canAction}">
	<legend>新的跟踪进度     <a href="javascript:void(0);" style="margin-left: 30px;" class="btn btn-primary" onclick="Order.addService(${order.id},this_index);">添加上门服务</a>
		<shiro:hasPermission name="sd:order:service">
			<c:if test="${ order.canService() }">
				<a href="javascript:void(0);" class="btn btn-primary" title="点击自动添加下单的明细到上门服务"
				   onclick="orderConfirmDoor('${order.id}','${order.quarter}')">
					<i class="icon-plus"></i>确认上门
				</a>
			</c:if>

			<c:if test="${ order.canGrade() }">
				<a href="javascript:void(0);" class="btn btn-primary" title="点击客户"
				   onclick="Order.grade('${order.id}','${order.quarter}');" href="javascript:;"><i class="icon-star"></i>客评</a>
			</c:if>
		</shiro:hasPermission>

		<a href="javascript:void(0);" class="btn btn-primary" title="点击取消掉手机标识异常的标志，订单号不再飘红"
		   onclick="Order.orderDealAPPException(${order.id});">
			<i class="icon-plus"></i>手机异常处理完成
		</a>
		<c:choose>
			<c:when test="${order.orderCondition.feedbackFlag eq 1}">
				<c:if test="${order.orderCondition.replyFlag eq 2 }">
					<img id="${order.orderCondition.feedbackId}" style="width:24px;height:24px;"
						 src="${ctxStatic}/images/complain.gif" />
				</c:if>
				<a href="javascript:;" onclick="Order.replylist('${order.orderCondition.feedbackId}','${order.quarter}','${order.orderNo}');" title="${order.orderCondition.feedbackTitle}" >${fns:abbr(order.orderCondition.feedbackTitle,20)} </a>
			</c:when>
			<c:otherwise>
				<shiro:hasPermission name="sd:feedback:edit">
					<a href="javascript:void(0);" class="btn btn-mini btn-primary"
					   onclick="Order.feedback('${order.id}');">反馈</a>
				</shiro:hasPermission>
			</c:otherwise>
		</c:choose>
	</legend>
	<form:form id="trackingForm" modelAttribute="order" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">厂商跟踪项目:</label>
			<div class="controls">
				<c:if test="${!empty order.id }">
					<c:forEach items="${tracks}" var="track" varStatus="i" begin="0">
						<span><input id="trackingIds${i.index+1}" name="trackingIds" type="checkbox" value="${track.value}">
							<label for="trackingIds${i.index+1}">${track.label}</label>
						</span>
					</c:forEach>
				</c:if>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">客服跟踪内容:</label>
			<div class="controls">
			<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="250" class="input-xxlarge required"/>
				<form:radiobuttons path="isCustomerSame" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label"
							itemValue="value" htmlEscape="false" class="required" /><%--切换为微服务--%>（客户是否可见）
			</div>
		</div>
		<div class="form-actions">
			<c:if test="${!empty order.id }">
			<shiro:hasPermission name="sd:order:tracking">
				<input id="btnSubmit" class="btn btn-primary" onclick="saveTacking();" type="button" value="保 存"/>&nbsp;</shiro:hasPermission>
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="closeme();" />
		</div>
	</form:form>
	</c:if>
	<object id="plugin0" type="application/x-nyteleactivex" width="300" height="300">
			    <param name="onload" value="pluginLoaded" />
				<param name="install-url" value="${ctxPlugin}/npNYTeleActiveX.dll" />
			</object>
</body>
<script type="text/javascript">
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
            alert('设备连接事件：'+number);
        });
        addEvent(plugin(), 'OnDeviceDisconnect', function(number){
            alert('设备断开事件：'+number);
        });
        addEvent(plugin(), 'OnCallOut', function(teleNo){
            alert('呼叫事件：'+teleNo);
        });
        addEvent(plugin(), 'OnCallIn', function(teleNo){
            alert('来电事件：'+teleNo);
        });
        addEvent(plugin(), 'OnHangUp', function(teleNo){
            alert('挂起事件：'+teleNo);
        });
        addEvent(plugin(), 'OnAnswer', function(teleNo){
            alert('应答事件：'+teleNo);
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

    //拔号
    function StartDial(teleNo, bRecord)
    {
        teleNo="00"+teleNo;
        alert(teleNo);
        if( !plugin().StartDial(teleNo, bRecord) )
            alert("StartDial Fail");
    }

    //挂机或挂断
    function HangUpCtrl()
    {
        if( !plugin().HangUpCtrl() )
            alert("HangUpCtrl Fail");
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
</html>