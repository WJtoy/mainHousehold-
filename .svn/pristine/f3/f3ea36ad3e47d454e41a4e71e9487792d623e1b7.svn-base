<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>区域半径管理</title>
    <meta name="decorator" content="default"/>
    <script type="text/javascript">
        $(document).ready(function () {
            $("#name").focus();
            $("#inputForm").validate({
                submitHandler: function (form) {
                    var areaId = $("#areaId").val();
                    console.log(areaId);
                    $.ajax({
                        cache: false,
                        type: "POST",
                        url:"${ctx}/sys/area/service/area/"+areaId,
                        dataType: 'json',
                        success: function (data) {
                            if (data.success ) {
                                // console.log(data.data.type);
                                if (data.data.type == 2 || data.data.type ==3) {
                                    top.$.jBox.confirm("是否要覆盖已设置的区/县半径设置","系统提示",function(v, h, f) {
                                        if (v == 'ok') {
                                            loading('正在提交，请稍等...');
                                            $("#coverSubArea").val("1");
                                        } else {
                                            $("#coverSubArea").val("0");
                                        }
                                        form.submit();
                                    });
                                } else {
                                    loading('正在提交，请稍等...');
                                    form.submit();
                                }
                            }else{
                                layerError(data.message);
                            }
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"保存网点等级错误，请重试!");
                        }
                    });

                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
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
    <li><a href="${ctx}/md/planradius/list?area.id=${planRadius.area.id}">区域半径列表</a></li>
    <li class="active"><a
            href="${ctx}/md/planradius/form?area.id=${planRadius.area.id}">区域半径<shiro:hasPermission
            name="md:planradius:edit">${not empty planRadius.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission
            name="md:planradius:edit">查看</shiro:lacksPermission></a></li>
</ul>
<br/>
<form:form id="inputForm" modelAttribute="planRadius" action="${ctx}/md/planradius/save" method="post" class="form-horizontal">
    <form:hidden path="id"/>
    <input id="coverSubArea" name="coverSubArea" type="hidden" value="0"/>
    <sys:message content="${message}"/>
    <div class="control-group">
        <label class="control-label">区域:</label>
        <div class="controls">
            <sys:treeselect id="area" name="area.id" value="${planRadius.area.id}" labelName="area.name"
                            labelValue="${planRadius.area.name}"
                            title="区域" url="/sys/area/treeData" cssClass="required"/>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">半径1(千米):</label>
        <div class="controls">
            <form:input path="radius1" htmlEscape="false" maxlength="50" class="required number"/>
            <span class="help-inline"><font color="red">*</font> </span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">半径2(千米):</label>
        <div class="controls">
            <form:input path="radius2" htmlEscape="false" maxlength="50" class="required number"/>
            <span class="help-inline"><font color="red">*</font> </span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">半径3(千米):</label>
        <div class="controls">
            <form:input path="radius3" htmlEscape="false" maxlength="50" class="required number"/>
            <span class="help-inline"><font color="red">*</font> </span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">备注:</label>
        <div class="controls">
            <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge"/>
        </div>
    </div>
    <div class="form-actions">
        <shiro:hasPermission name="md:planradius:edit"><input id="btnSubmit" class="btn btn-primary" type="submit"
                                                           value="保 存"/>&nbsp;</shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
    </div>
</form:form>
</body>
</html>