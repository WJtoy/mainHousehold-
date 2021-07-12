<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>配件</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/treeview.jsp" %>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#inputForm").validate({
				rules: {
					name: {remote: "${ctx}/md/material/checkMaterialName?id=" + '${material.id}'}
				},
				messages: {
					name: {remote: "配件名称已存在"}
				},
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
		function changeCategory(obj){
         var value= $(obj).find("option:selected").text();
         $("#materialCategoryName").val(value);
        }
	</script>
  </head>
  
  <body>
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/md/material/list">配件列表</a></li>
		<li class="active"><a href="javascript:void(0);">配件<shiro:hasPermission name="md:material:edit">${not empty material.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:material:edit">查看</shiro:lacksPermission></a></li>
	</ul>
	<sys:message content="${message}"/>
	<form:form id="inputForm" modelAttribute="material" action="${ctx}/md/material/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<div class="control-group">
			<label class="control-label">配件类别:</label>
			<div class="controls">
			<select name="materialCategory.id" class="input-small required " style="width:225px;" onchange="changeCategory(this)">
				<option value=""
						<c:out value="${(empty material.materialCategory.id)?'selected=selected':''}" />>请选择</option>
				<c:forEach items="${materialCategoryList}" var="materialCategory">
					<option value="${materialCategory.id}"
							<c:out value="${(material.materialCategory.id eq materialCategory.id)?'selected=selected':''}" />>${materialCategory.name}</option>
				</c:forEach>
			</select>
			<input type="hidden" id="materialCategoryName" name="materialCategory.name" value="${material.materialCategory.name}">
				<span class="add-on red">*</span>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">名称:</label>
			<div class="controls">
				<input id="oldName" name="oldName" type="hidden" value="${material.name}">
				<form:input path="name" htmlEscape="false" minLength="2" maxlength="30" class="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">参考价:</label>
			<div class="controls">
				<form:input path="price" htmlEscape="false" maxlength="100" class="required number"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">是否反件:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${material.id>0 && material.isReturn==0}">
						<form:radiobutton path="isReturn" value="1"  label="是"/>
						<form:radiobutton path="isReturn" value="0" checked="checked" label="否"/>
					</c:when>
					<c:otherwise>
						<form:radiobutton path="isReturn" value="1" checked="checked" label="是"/>
						<form:radiobutton path="isReturn" value="0" label="否"/>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">备注:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" class="input-xlarge"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:material:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>
