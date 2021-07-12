<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品分类</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#value").focus();
			$("#inputForm").validate({
                rules: {
                    code: {remote: "${ctx}/md/brand/checkBrandCode?id=" + '${brand.id}'},
                    name: {remote: "${ctx}/md/brand/checkBrandName?id=" + '${brand.id}'}
                },
                messages: {
                    code: {remote: "品牌编码已存在"},
                    name: {remote: "品牌名称已存在"}
                },
				submitHandler: function(form){
					loading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);
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
		<li><a href="${ctx}/md/brand">品牌列表</a></li>
		<li class="active"><a href="javascript:void(0);">品牌<shiro:hasPermission name="md:brand:edit">${not empty brand.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:brand:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="brand" action="${ctx}/md/brand/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">编码:</label>
			<div class="controls">
				<form:input path="code" htmlEscape="false" minLength="2" maxlength="20" class="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">名称:</label>
			<div class="controls">
				<form:input path="name" htmlEscape="false" minLength="2" maxlength="20" class="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="250" class="input-xlarge" cssStyle="min-width: 280px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:brand:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>
