<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<head>
	<title>单据编号规则管理</title>
	<meta name="decorator" content="default"/>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#value").focus();
			$("#inputForm").validate({
				submitHandler: function(form){
					loading('正在提交，请稍等...');
					form.submit();
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
	<ul class="nav nav-tabs">
		<li><a href="${ctx}/sys/sequence">单据编号规则列表</a></li>
		<li class="active"><a href="javascript:void(0);">单据编号规则<shiro:hasPermission name="sys:sequence:edit">${not empty seq.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="sys:sequence:view">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="seq" action="${ctx}/sys/sequence/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">编号代码:</label>
			<div class="controls">
				<form:input path="code" htmlEscape="false" readonly="${not empty seq.id?'true':'false'}" maxlength="100" class="required abc"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="4" maxlength="255" class="input-xxlarge"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">前缀:</label>
			<div class="controls">
				<form:input path="prefix" htmlEscape="false" maxlength="5" class="required abc"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">类型:</label>
			<div class="controls">
				<form:select path="dateFormat" style="width:223px;">
					<form:option value="" label="请选择"/>
					<form:options items="${fns:getDictListFromMS('sequence_dateformat')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
				</form:select>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">日期分隔符:</label>
			<div class="controls">
				<form:input path="dateSeparator" htmlEscape="false" maxlength="1" class="abc"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">顺序号位数:</label>
			<div class="controls">
				<form:input path="digitBit" htmlEscape="false" maxlength="2" class="required digits"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">后缀:</label>
			<div class="controls">
				<form:input path="suffix" htmlEscape="false" maxlength="5" class="abc"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">分隔符:</label>
			<div class="controls">
				<form:input path="separator" htmlEscape="false" maxlength="1" class="abc"/>
			</div>
		</div>
		
		<div class="form-actions">
			<shiro:hasPermission name="sys:sequence:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
</body>
</html>