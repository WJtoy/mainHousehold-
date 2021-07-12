<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>客户产品型号</title>
	<meta name="decorator" content="default" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@include file="/WEB-INF/views/include/treetable.jsp"%>
	<script type="text/javascript">
        $(document).ready(function() {
            $("#inputForm").validate({
					rules: {
						name: {remote: "${ctx}/md/materialCategory/checkMaterialCategoryName?id=" + '${materialCategory.id}'}
					},
					messages: {
						name: {remote: "配件类别名称已存在"}
					},
                    submitHandler : function(form)
                    {
                        loading('正在提交，请稍等...');
                        var $btnSubmit = $("#btnSubmit");
                        if ($btnSubmit.prop("disabled") == true) {
                            event.preventDefault();
                            return false;
                        };
                        $btnSubmit.prop("disabled", true);
                        form.submit();
                    },
                    errorContainer : "#messageBox",
                    errorPlacement : function(error, element)
                    {
                        $("#messageBox").text("输入有误，请先更正。");
                        if (element.is(":checkbox")
                            || element.is(":radio")
                            || element.parent().is(
                                ".input-append"))
                        {
                            error.appendTo(element.parent()
                                .parent());
                        } else
                        {
                            error.insertAfter(element);
                        }
			}});

            $("th").css({"text-align":"left","vertical-align":"middle"});
            $("td").css({"text-align":"left","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
        });

	</script>
	<style type="text/css">
		.form-horizontal .control-label{
			width: 180px;
		}
		.form-horizontal .controls{
			margin-left: 200px;
		}
		.form-horizontal .control-group{
			margin-bottom: 15px;
		}
		.fromInput {
			border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		}
	</style>
</head>

<body>
<ul class="nav nav-tabs">
	<li>
		<a href="${ctx}/md/materialCategory/list">列表</a>
	</li>
	<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:materialcategory:edit">${not empty materialCategory.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:materialcategory:edit">查看</shiro:lacksPermission></a></li>
</ul><br>
<form:form id="inputForm" modelAttribute="materialCategory" action="${ctx}/md/materialCategory/save" method="post" class="form-horizontal">
	<sys:message content="${message}" />
	<c:if test="${canAction == true}">
		<form:hidden path="id"></form:hidden>
		<div class="control-group">
			<label class="control-label">名称:</label>
			<div class="controls">
				<input class="fromInput required"  name="name" placeholder="名称" value="${materialCategory.name}" htmlEscape="false" maxlength="30"/>
                <span class="add-on red">*</span>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="230" class="input-xlarge" cssStyle="min-width: 280px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
			</div>
		</div>
	</c:if>
	<div id="formActions" class="form-actions">
		<c:if test="${canAction == true}">
			<shiro:hasPermission name="md:customermaterial:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit"
					   value="保 存" />&nbsp;</shiro:hasPermission>
		</c:if>
		<input id="btnCancel" class="btn" type="button" value="返 回"
			   onclick="history.go(-1)" />
	</div>
</form:form>
</body>
</html>
