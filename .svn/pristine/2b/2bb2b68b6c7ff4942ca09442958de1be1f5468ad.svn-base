<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户店铺</title>
    <meta name="decorator" content="default"/>
    <script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        $(document).ready(function() {
            $("#inputForm").validate({
                submitHandler: function(form){
                    // loading('正在提交，请稍候...');
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);
                    // form.submit();
                    $.ajax({
                        url:"${ctx}/customer/md/customerShop/save",
                        type:"POST",
                        data:$(form).serialize(),
                        dataType:"json",
                        success: function(data){
                            // 提交后的回调函数
                            if (loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            if (ajaxLogout(data)) {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg("保存成功");
                                var pframe = getActiveTabIframe();// 定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                                top.layer.close(this_index);// 关闭本身
                            } else {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                    });
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

        // 取消
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }
    </script>
    <style type="text/css">
        .fromInput {
            border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
        }
    </style>
</head>

<body>
<div id="addShop" style="margin-top: 40px;">
    <%--<ul class="nav nav-tabs">--%>
    <%--<li><a href="${ctx}/md/customerShop/getList">列表</a></li>--%>
    <%--<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:customershop:edit">${not empty b2BCustomerMapping.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:customershop:edit">查看</shiro:lacksPermission></a></li>--%>
    <%--</ul><br/>--%>
    <c:set var="currentuser" value="${fns:getUser()}"/>
    <%--<form:form id="inputForm" modelAttribute="b2BCustomerMapping" action="${ctx}/md/customerShop/save" method="post" class="form-horizontal">--%>
    <form:form id="inputForm" modelAttribute="b2BCustomerMapping" action="" method="post" class="form-horizontal">
        <form:hidden path="id"/>
        <form:hidden path="dataSource" value="1"/>
        <form:hidden path="shopId"/>
        <sys:message content="${message}"/>
        <div class="control-group d-group" style="margin-bottom: 10px;">
            <label class="control-label">客户:</label>
            <div class="controls" >
                <c:choose>
                    <c:when test="${currentuser.isCustomer()==true}">
                        <form:hidden path="customerId" id="customerId"></form:hidden>
                        <form:input path="customerName" readonly="true"></form:input>
                    </c:when>
                    <c:otherwise>
                        <form:select path="customerId" cssClass="input-small required" cssStyle="width:225px;">
                            <form:option value="" label="请选择"/>
                            <form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
                        </form:select>
                    </c:otherwise>
                </c:choose>
                <span class="red">*</span>
            </div>
        </div>

        <div class="control-group d-group" style="margin-bottom: 10px;">
            <label class="control-label">销售渠道:</label>
            <div class="controls">
                <form:select path="saleChannel" cssClass="required input-medium" cssStyle="width: 226px;">
                    <form:option value="" label="请选择"></form:option>
                    <form:options items="${fns:getDictExceptListFromMS('sale_channel',0)}"
                                  itemLabel="label" itemValue="value" htmlEscape="false" />
                </form:select>
                <span class="red">*</span>
            </div>
        </div>

        <div class="control-group d-group" style="margin-bottom: 10px;">
            <label class="control-label">店铺名称:</label>
            <div class="controls">
                <form:input path="shopName" htmlEscape="false" maxlength="50" class="required"/>
                <span class="red">*</span>
                <span style="color: #08080878">数据源客户名称</span>
            </div>
        </div>
        <div class="control-group d-group" style="margin-bottom: 10px;">
            <label class="control-label">描述:</label>
            <div class="controls">
                <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge" cssStyle="min-width: 260px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
            </div>
        </div>
        <%--<div class="form-actions">--%>
        <%--<shiro:hasPermission name="md:customershop:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>--%>
        <%--<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>--%>
        <%--</div>--%>
    </form:form>

    <div id="editbtn" style="margin-top: 50px;">
        <hr style="=border: 1px solid rgba(238, 238, 238, 1);margin: 0px;margin-top: 5px"/>
        <shiro:hasPermission name="customer:md:customershop:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" onclick="$('#inputForm').submit()" style="margin-left: 450px;margin-top: 10px;"/>&nbsp;
            &nbsp;&nbsp;<input id="btnCancel" class="btn" type="button" value="关 闭" style="margin-top:10px"onclick="cancel()"/>
        </shiro:hasPermission>
    </div>

</div>

</body>
</html>
