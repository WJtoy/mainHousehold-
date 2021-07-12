<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<head>
	<title>客评标准管理</title>
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
		<li><a href="${ctx}/md/grade">客评项目列表</a></li>
		<li class="active"><a href="javascript:void(0);">客评标准<shiro:hasPermission name="md:grade:edit">${not empty item.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:grade:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="item" action="${ctx}/md/grade/saveitem" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="grade.id"/>
		<form:hidden path="type"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">客评项目名称:</label>
			<div class="controls">
				<form:input path="grade.name" htmlEscape="false" readonly="true" maxlength="30" class=""/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">客评标准:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="4" maxlength="255" class="input-xxlarge"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">分值:</label>
			<div class="controls">
				<form:input path="point" htmlEscape="false" maxlength="6" class="{required:true,min:1}"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">关联字典值:</label>
			<div class="controls">
				<form:input path="dictValue" htmlEscape="false" maxlength="20" />
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:grade:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
</body>
</html>