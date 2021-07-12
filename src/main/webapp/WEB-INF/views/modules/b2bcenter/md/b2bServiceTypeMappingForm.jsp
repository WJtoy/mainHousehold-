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
			$("#inputForm").validate({
                rules: {
                    b2bServiceTypeCode: {
						remote:{
                        type:"GET",
						contentType: "application/json;charset=UTF-8",
						url:"${ctx}/b2bcenter/md/serviceType/checkIsExist",
						data:{
                            dataSource:function(){
							  return $("#dataSource").val();
						    },
                            id:function(){
							  return ${b2BServiceTypeMapping.id==null?-1:b2BServiceTypeMapping.id};
						    },
                            b2bWarrantyType:function(){
							   return  $("#b2bWarrantyType").val();
						    }
					    }
					    }
                    }
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
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
	  </style>
  </head>
  
  <body>
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/b2bcenter/md/serviceType/getList">列表</a></li>
		<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:b2bservicetype:edit">${not empty b2BServiceTypeMapping.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:b2bservicetype:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="b2BServiceTypeMapping" action="${ctx}/b2bcenter/md/serviceType/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">数据源:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${b2BServiceTypeMapping.dataSource >0}">
						<form:hidden path="dataSource"></form:hidden>
						<input class="fromInput" readonly="true" value="${fns:getDictLabelFromMS(b2BServiceTypeMapping.dataSource, 'order_data_source','Unknow' )}"></input>
					</c:when>
					<c:otherwise>
						<form:select disabled="${b2BServiceTypeMapping.dataSource > 0?'true':'false'}" path="dataSource" cssClass="required input-medium" cssStyle="width: 220px;">
							<form:options items="${fns:getDictExceptListFromMS('order_data_source',1)}"
										  itemLabel="label" itemValue="value" htmlEscape="false" />
						</form:select>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">B2B质保类型:</label>
			<div class="controls">
				<form:input path="b2bWarrantyType" htmlEscape="false" minLength="1" maxlength="20" class="required"/>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">B2B服务类型名称:</label>
			<div class="controls">
				<form:input path="b2bServiceTypeName" htmlEscape="false" maxlength="50" class="required"/>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">B2B服务类型code:</label>
			<div class="controls">
				<form:input path="b2bServiceTypeCode" htmlEscape="false" minLength="1" maxlength="100" class="required"/>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">快可立服务类型:</label>
			<div class="controls" >
				<c:choose>
					<c:when test="${b2BServiceTypeMapping.serviceTypeId > 0}">
						<form:hidden path="serviceTypeId"></form:hidden>
						<form:input  path="serviceTypeName" readonly="true"></form:input>
					</c:when>
					<c:otherwise>
						<form:select path="serviceTypeId" cssClass="input-small required" cssStyle="width:225px;">
							<form:option value="" label="请选择"/>
							<form:options items="${fns:getServiceTypes()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
						</form:select>
					</c:otherwise>
				</c:choose>
				<span class="add-on red">*</span>
			</div>
		</div>
		<div class="control-group">
		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge" cssStyle="min-width: 260px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:b2bservicetype:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>
