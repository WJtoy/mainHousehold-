<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>查看催单</title>
	<meta name="description" content="查看催单">
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<!-- clipboard -->
	<script src="${ctxStatic}/common/clipboard.min.js" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/modules/sd/reminder/tpl/confirmItems.html" %>
	<c:set var="currentuser" value="${fns:getUser() }"/>
	<script type="text/javascript">
		Order.rootUrl = "${ctx}";
		var this_index = top.layer.index;
		function closeme(){
			top.layer.close(this_index);
		};
		<c:if test="${canAction eq true}">
		$(document).ready(function() {
			<%--//装载日志--%>
			<%--$("#divLog").show();--%>
			<%--Order.loadReminderLogList('${reminder.id}', '${reminder.quarter}'); --%>
			//项目
			var items = ${fns:toJson(reminder.items)};
			var model = {
				orderNo:'${reminder.orderNo}',
				userName:'${reminder.userName}',
				userPhone:'${reminder.userPhone}',
				userAddress:'${reminder.userAddress}',
				shopName:'${reminder.shopName}',
				dataSourceName:'${reminder.dataSourceName}',
				parentBizOrderId:'${reminder.parentBizOrderId}',
				items: items
			};
			Order.showReminderConfirmItems(model);
		});
		</c:if>
	</script>
	<style type="text/css">
		/*legend span {
			border-bottom: #0096DA 4px solid;
			padding-bottom: 6px;}*/
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label {width: 120px;}
		.form-horizontal .controls {margin-left: 135px;}
		.myalert {padding: 2px 5px 2px 5px;margin-bottom: 2px;}
		.centerForm {margin:0 auto; width:800px; line-height:20px; border:0px solid}
		.reminder-no{
			padding: 4px 5px;border-radius:2px;background-color:#34C758;color: white;margin-left: 5px;
		}
		.order-no{
			padding: 4px 5px;border-radius:2px;background-color:#0096DA;color: white;
		}
	</style>
</head>
<body>
	<div class="centerForm">
	<form:form id="inputForm" modelAttribute="reminder" method="post" cssClass="form-horizontal" cssStyle="width:800px;margin-left: 0px;">
		<sys:message content="${message}"/>
		<c:if test="${canAction eq true}">
		<fieldset>
			<legend>
				<div class="row-fluid">
					<div class="span8">
					</div>
					<div class="span4">
						<p style="float: right;font-size: 14px">${reminder.reminderNo}<span class="reminder-no">${reminder.statusName}</span></p>
					</div>
				</div>
			</legend>
			<div class="row-fluid">
				<div class="span6">
					<div class="control-group" style="margin-top: 7px">
						<label class="control-label" style="padding: 0px">工单单号：</label>
						<div class="controls">
							<form:hidden path="orderNo"  htmlEscape="false" cssClass="input-block-level required" readonly="true"/>
							<c:choose>
								<c:when test="${currentuser.isKefu()}">
								   <a href="javascript:void(0);" onclick="Order.showKefuOrderDetail('${reminder.orderId}','${reminder.quarter}');">${reminder.orderNo}</a>
								</c:when>
								<c:otherwise>
									<a href="javascript:void(0);" onclick="Order.viewOrderDetail('${reminder.orderId}','${reminder.quarter}');">${reminder.orderNo}</a>
								</c:otherwise>
							</c:choose>
							<span class="order-no">${reminder.orderStatusName}</span>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid" style="margin-top: 10px">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">工单来源：</label>
						<div class="controls">
							<form:input path="dataSourceName"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
						<div class="controls">
							<form:input path="customer.name"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid" style="margin-top: 5px">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">购买店铺：</label>
						<div class="controls">
							<form:input path="shopName"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">服务网点：</label>
						<div class="controls">
							<form:input path="servicePoint.name"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid" style="margin-top: 10px">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">用户姓名：</label>
						<div class="controls">
							<form:input path="userName"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">用户电话：</label>
						<div class="controls">
							<form:input path="userPhone"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid" style="margin-top: 10px">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">用户地址：</label>
						<div class="controls">
							<form:input path="userAddress"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
			</div>
			<legend style="margin-top: 10px"><span style="border-bottom: #0096DA 4px solid;padding-bottom: 6px;">催单</span></legend>
			<div class="row-fluid">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">类&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;型：</label>
						<div class="controls">
							<form:input path="reminderReason.value"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid" style="margin-top: 10px">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注：</label>
						<div class="controls">
							<form:textarea path="createRemark" htmlEscape="false" rows="3" maxlength="250" readonly="true" class="input-block-level" />
						</div>
					</div>
				</div>
			</div>
			<legend><span style="border-bottom: #0096DA 4px solid;padding-bottom: 6px;">回复</span></legend>
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">回复内容：</label>
						<div class="controls">
							<form:textarea path="processRemark" htmlEscape="false" rows="3" maxlength="250" readonly="true" class="input-block-level" />
						</div>
					</div>
				</div>
			</div>
		</fieldset>
		</c:if>
	</form:form>
	</div>
	<!-- 催单项目 -->
	<c:if test="${canAction eq true}">
		<div id="divItems" class="tab-content" style="margin: 0 20px;margin-bottom: 20px">
			<legend style="margin-top: 10px"><span style="border-bottom: #0096DA 4px solid;padding-bottom: 6px;">跟踪进度</span></legend>
			<div class="tab-pane active" id="tabItems"></div>
		</div>
		<div style="height: 60px;width: 100%"></div>
		<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
			<hr style="margin: 0px;"/>
			<div style="float: right;margin-top: 10px;margin-right: 20px">
				<input id="btnCancel" class="btn" type="button" value="关 闭" style="width: 96px;height: 40px" onclick="closeme()"/>
			</div>
		</div>
	</c:if>
</body>
<script type="text/javascript">
	$(document).ready(function() {
		$('a.zclip').each(function () {
			$_this = $(this);
			var msg = $_this.data('clipboard-text');
			msg = msg.replace(/\\n/g,'\n');
			$_this.attr('data-clipboard-text',msg);
			var id = $_this.attr('id');
			var clip_item = new ClipboardJS('#'+id);
			clip_item.on('success', function(e) {
				layerMsg("信息复制成功");
			});
		});
	});
</script>
</html>