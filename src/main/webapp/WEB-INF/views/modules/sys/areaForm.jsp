<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>区域管理</title>
    <meta name="decorator" content="default"/>
    <script type="text/javascript">
        $(document).ready(function () {
            $("#name").focus();
            $("#inputForm").validate({
                submitHandler: function (form) {
                    //loading('正在提交，请稍等...');
                    top.$.jBox.tip('正在提交，请稍等...', 'loading');
                    form.submit();
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

            $("#btnSaveAndRenew").on("click",function(){
                top.$.jBox.tip('正在提交，请稍等...', 'loading');
                var url = "${ctx}/sys/area/save?renew=1";
                $("#inputForm").attr("action",url);
                $("#inputForm").submit();
                return false;
            })
        });
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li><a href="${ctx}/sys/area/">区域列表</a></li>
    <li class="active"><a href="form?id=${area.id}&parent.id=${area.parent.id}">区域<shiro:hasPermission
            name="sys:area:edit">${not empty area.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission
            name="sys:area:edit">查看</shiro:lacksPermission></a></li>
</ul>
<br/>
<form:form id="inputForm" modelAttribute="area" action="${ctx}/sys/area/save" method="post" class="form-horizontal">
    <form:hidden path="id"/>
    <sys:message content="${message}"/>
    <div class="control-group">
        <label class="control-label">上级区域:</label>
        <div class="controls">
            <%--
            <sys:treeselect id="area" name="parent.id" value="${area.parent.id}" labelName="parent.name"
                            labelValue="${area.parent.name}"
                            title="区域" url="/sys/area/treeData" extId="${area.id}" cssClass="" allowClear="true"/>
                            --%>

            <sys:treeselectareanew id="area" name="parent.id" value="${area.parent.id}" levelValue=""
                                   labelName="parent.name" labelValue="${area.parent.name}" title="区域" clearIdValue="0"
                                   url="/sys/area/treeDataNew" allowClear="true" nodesLevel="-1" nameLevel="3" cssClass=""/>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">区域名称:</label>
        <div class="controls">
            <form:input path="name" htmlEscape="false" maxlength="50" class="required"/>
            <span class="help-inline"><font color="red">*</font> </span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">区域编码:</label>
        <div class="controls">
            <form:input path="code" htmlEscape="false" maxlength="50"/>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">区域类型:</label>
        <div class="controls">
            <form:select path="type" class="input-medium">
                <form:options items="${fns:getDictListFromMS('sys_area_type')}" itemLabel="label" itemValue="value"
                              htmlEscape="false"/><!-- 切换为微服务 -->
            </form:select>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">排序:</label>
        <div class="controls">
            <input type="number" id="sort" name="sort" value="${area.sort}" maxlength="11" />
            <%--<form:input path="sort"  htmlEscape="false" maxlength="11"/>--%>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">备注:</label>
        <div class="controls">
            <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge"/>
        </div>
    </div>
    <div class="form-actions">
        <shiro:hasPermission name="sys:area:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;
            <input id="btnSaveAndRenew" class="btn btn-primary" type="button" value="保存后新增"/>&nbsp;
        </shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
    </div>
</form:form>
</body>
</html>