<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>服务费项目分类</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
            $("#inputForm").validate({
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
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
	  </style>
  </head>
  
  <body>
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/provider/md/auxiliaryMaterialCategory/getList">列表</a></li>
		<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:auxiliarymaterialcategory:edit">${not empty auxiliaryMaterialCategory.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:auxiliarymaterialcategory:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="auxiliaryMaterialCategory" action="${ctx}/provider/md/auxiliaryMaterialCategory/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">分类名称:</label>
			<div class="controls">
				<form:input path="name" htmlEscape="false" minLength="2" maxlength="30" class="required"/>
				<span class="add-on red">*</span>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">类型:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${auxiliaryMaterialCategory.type==2}">
						<form:radiobutton path="type" value="1"  label="关联产品"  />
						<form:radiobutton path="type" value="2" checked="checked" label="关联工单"/>
					</c:when>
					<c:otherwise>
						<form:radiobutton path="type" value="1"  checked="checked" label="关联产品"/>
						<form:radiobutton path="type" value="2"  label="关联工单"/>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="5" maxlength="200" class="input-xlarge" cssStyle="min-width: 260px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:auxiliarymaterialcategory:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>
