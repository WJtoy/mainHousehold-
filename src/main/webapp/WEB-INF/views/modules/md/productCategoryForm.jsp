<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品品类</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#value").focus();
			$("#inputForm").validate({
                rules: {
                    code: {remote: "${ctx}/md/productcategory/checkProductCategoryCode?id=" + '${productCategory.id}'},
                    name: {remote: "${ctx}/md/productcategory/checkProductCategoryName?id=" + '${productCategory.id}'}
                },
                messages: {
                    code: {remote: "产品分类编码已存在"},
                    name: {remote: "产品分类名称已存在"}
                },
				submitHandler: function(form){
					loading('正在提交，请稍候...');
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
		<li><a href="${ctx}/md/productcategory">产品品类列表</a></li>
		<li class="active"><a href="javascript:void(0);">产品品类<shiro:hasPermission name="md:productcategory:edit">${not empty category.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:productcategory:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="productCategory" action="${ctx}/md/productcategory/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">编码:</label>
			<div class="controls">
				<form:input path="code" htmlEscape="false" minLength="2" maxlength="10" class="required"/>
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
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" class="input-xlarge"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:productcategory:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>
