<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<head>
	<title>客评项目管理</title>
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
		<li class="active"><a href="javascript:void(0);">客评项目<shiro:hasPermission name="md:grade:edit">${not empty grade.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:grade:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="grade" action="${ctx}/md/grade/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<%--<input type="hidden" id="createBy.id" name="createBy.id" value="884631018219376640" />--%>
		<form:hidden path="type"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">名称:</label>
			<div class="controls">
				<form:input path="name" htmlEscape="false" maxlength="30" class="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">描述:</label>
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
			<label class="control-label">关联数据字典类型:</label>
			<div class="controls">
				<form:input path="dictType" htmlEscape="false" maxlength="30" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">排序:</label>
			<div class="controls">
				<form:input path="sort" htmlEscape="false" maxlength="11" class="required digits"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:grade:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
</body>
</html>