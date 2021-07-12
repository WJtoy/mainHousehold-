<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<head>
	<title>服务类型管理</title>
	<meta name="decorator" content="default"/>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<style type="text/css">
		#editBtn {
			position: fixed;
			left: 0px;
			bottom: 0;
			width: 100%;
			height: 60px;
			background: #fff;
			z-index: 10;
			border-top: 1px solid #e5e5e5;
		}
	</style>
	<script type="text/javascript">
		var this_index = top.layer.index;
		// 关闭页面
		function cancel() {
			top.layer.close(this_index);// 关闭本身
		}
		$(document).ready(function() {
			$("#code").focus();
			$("#inputForm").validate({
				rules: {
					code: {remote: "${ctx}/md/servicetype/checkLoginName?oldLoginName=" + encodeURIComponent('${serviceType.code}')}},
				messages: {
					code: {remote: "服务类型名已存在"},
					discountPrice:{
						nogt:"默认折扣价必须小于等于默认价格"},
					engineerDiscountPrice:{
						nogt:"安维折扣价必须小于等于安维默认价"}
					},
				submitHandler: function(form){
					var loadingIndex = layerLoading('正在提交，请稍候...');
					var $btnSubmit = $("#btnSubmit");
					if ($btnSubmit.prop("disabled") == true) {
						event.preventDefault();
						return false;
					}
					$btnSubmit.prop("disabled", true);
					$.ajax({
						url:"${ctx}/md/servicetype/ajax/save",
						type:"POST",
						data:$(form).serialize(),
						dataType:"json",
						success: function(data){
							//提交后的回调函数
							if(loadingIndex) {
								top.layer.close(loadingIndex);
							}
							if(ajaxLogout(data)){
								setTimeout(function () {
									clickTag = 0;
									$btnSubmit.removeAttr('disabled');
								}, 2000);
								return false;
							}
							if (data.success) {
								layerMsg("保存成功");
								top.layer.close(this_index);//关闭本身
								var pframe = getActiveTabIframe();//定义在jeesite.min.js中
								if(pframe){
									pframe.repage();
								}
							}else{
								setTimeout(function () {
									clickTag = 0;
									$btnSubmit.removeAttr('disabled');
								}, 2000);
								top.layer.close(loadingIndex);
								layerError("保存失败", "错误提示");
							}
							return false;
						},
						error: function (data)
						{
							if(loadingIndex) {
								layer.close(loadingIndex);
							}
							setTimeout(function () {
								clickTag = 0;
								$btnSubmit.removeAttr('disabled');
							}, 2000);
							top.layer.close(loadingIndex);
							ajaxLogout(data,null,"数据保存错误，请重试!");
							//var msg = eval(data);
							top.layer.close(loadingIndex);
						},
						timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
					});
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
		});
	</script>
</head>
<body>
	<form:form id="inputForm" modelAttribute="serviceType" action="${ctx}/md/servicetype/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group" style="margin-top: 30px;margin-left: 115px">
			<label class="control-label">代码:</label>
			<div class="controls">
				<input id="oldCode" name="oldCode" type="hidden"  value="${serviceType.code}">
				<c:choose>
					<c:when test="${serviceType.id==null}">
						<form:input path="code" htmlEscape="false" maxlength="100" class="required" disabled="false" />
					</c:when>
					<c:otherwise>  
						<form:input path="code" htmlEscape="false" maxlength="100" class="required" disabled="true"/>
					</c:otherwise>
				</c:choose>

			</div>
		</div>
		<div class="control-group" style="margin-left: 115px">
			<label class="control-label">名称:</label>
			<div class="controls">
				<form:input path="name" htmlEscape="false" maxlength="100" class="required"/>
			</div>
		</div>
		<div class="control-group" style="margin-left: 115px">
			<label class="control-label">客户可见:</label>
			<div class="controls">
				<form:radiobutton path="openForCustomer" value="1"></form:radiobutton>是
				<form:radiobutton path="openForCustomer" value="0"></form:radiobutton>否
			</div>
		</div>
		<div class="control-group" style="margin-left: 115px">
			<label class="control-label">质保:</label>
			<div class="controls">
				<form:select path="warrantyStatus.value">
					<form:options items="${fns:getDictListFromMS('warrantyStatus')}" itemLabel="label" itemValue="value"
								  htmlEscape="false"/><%--切换为微服务--%>
				</form:select>
			</div>
		</div>
		<div class="control-group" style="margin-left: 115px">
			<label class="control-label">工单类型:</label>
			<div class="controls">
				<form:select path="orderServiceType" cssClass="required input-medium" cssStyle="width: 220px;">
					<form:options items="${fns:getDictListFromMS('order_service_type')}"
								  itemLabel="label" itemValue="value" htmlEscape="false" />
				</form:select>
			</div>
		</div>
		<div class="control-group" style="margin-left: 115px">
			<label class="control-label">是否自动客评:</label>
			<div class="controls">
				<form:radiobutton path="autoGradeFlag" value="0"  label="否"/>
				<form:radiobutton path="autoGradeFlag" value="1"  label="是"/>
			</div>
		</div>
		<div class="control-group" style="margin-left: 115px">
			<label class="control-label">是否自动对账:</label>
			<div class="controls">
				<form:radiobutton path="autoChargeFlag" value="0"  label="否"/>
				<form:radiobutton path="autoChargeFlag" value="1"  label="是"/>
			</div>
		</div>
		<div class="control-group" style="margin-left: 115px">
			<label class="control-label">关联故障类别:</label>
			<div class="controls">
				<form:radiobutton path="relateErrorTypeFlag" value="1" label="是"/>
				<form:radiobutton path="relateErrorTypeFlag" value="0"  label="否"/>
			</div>
		</div>
		<div class="control-group" style="margin-left: 115px">
			<label class="control-label">是否扣点:</label>
			<div class="controls">
				<form:radiobutton path="taxFeeFlag" value="1" label="是"/>
				<form:radiobutton path="taxFeeFlag" value="0"  label="否"/>
			</div>
		</div>
		<div class="control-group" style="margin-left: 115px">
			<label class="control-label">是否收取平台信息费:</label>
			<div class="controls">
				<form:radiobutton path="infoFeeFlag" value="1" label="是"/>
				<form:radiobutton path="infoFeeFlag" value="0" label="否"/>
			</div>
		</div>
		<div class="control-group" style="margin-left: 115px">
			<label class="control-label">是否扣质保金:</label>
			<div class="controls">
				<form:radiobutton path="depositFlag" value="1" label="是"/>
				<form:radiobutton path="depositFlag" value="0" label="否"/>
			</div>
		</div>
		<div class="control-group" style="margin-left: 115px">
			<label class="control-label">排序:</label>
			<div class="controls">
				<form:input path="sort" htmlEscape="false" maxlength="3"/>
			</div>
		</div>
		<div class="control-group" style="margin-left: 115px">
			<label class="control-label">备注:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" class="input-xlarge"/>
			</div>
		</div>
		<div id="editBtn" class="line-row">
			<shiro:hasPermission name="md:servicetype:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="width: 92px;height: 40px;margin-left: 70%;margin-top: 10px;margin-bottom: 10px"/>
				&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="width: 92px;height: 40px;margin-top: 10px;margin-left: 19px;margin-bottom: 10px"/>
		</div>
	</form:form>
	
	
</body>
</html>