<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>服务详情</title>
    <meta name="decorator" content="default"/>
    <script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        $(document).ready(function() {
            $("#inputForm").validate({
                submitHandler: function(form){
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);
                    $.ajax({
                        url:"${ctx}/b2bcenter/md/serviceSign/serviceTypeSave",
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
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                                top.layer.close(this_index);//关闭本身
                            } else {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError(data.message, "提示");
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
        // 关闭页面
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
    <form:form id="inputForm" modelAttribute="b2BServiceTypeMapping" action="" method="post" class="form-horizontal">
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
                <form:hidden path="b2bWarrantyType"></form:hidden>
                <input class="fromInput" readonly="true" value="${b2BServiceTypeMapping.b2bWarrantyType}"></input>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">B2B服务类型名称:</label>
            <div class="controls">
                <form:hidden path="b2bServiceTypeName"></form:hidden>
                <input class="fromInput" readonly="true" value="${b2BServiceTypeMapping.b2bServiceTypeName}"></input>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">B2B服务类型code:</label>
            <div class="controls">
                <form:hidden path="b2bServiceTypeCode"></form:hidden>
                <input class="fromInput" readonly="true" value="${b2BServiceTypeMapping.b2bServiceTypeCode}"></input>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">快可立服务类型:</label>
            <div class="controls" >
                <form:select path="serviceTypeId" cssClass="input-small required" cssStyle="width:220px;">
                    <form:option value="" label="请选择"/>
                    <form:options items="${fns:getServiceTypes()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
                </form:select>
                <span class="red">*</span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">描述:</label>
            <div class="controls">
                <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge" cssStyle="min-width: 260px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
            </div>
        </div>
        <div id="editbtn" style="margin-top: 90px;margin-left: 410px;">
            <shiro:hasPermission name="md:b2bservicesign:edit">
                <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>
                &nbsp;
            </shiro:hasPermission>
            <input id="btnCancel" class="btn" type="button" value="关 闭" onclick="cancel()"/>
        </div>
    </form:form>
</div>
</body>
</html>
