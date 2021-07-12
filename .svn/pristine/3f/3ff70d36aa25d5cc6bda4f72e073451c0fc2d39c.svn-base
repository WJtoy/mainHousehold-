<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>安维升级管理</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treeview.jsp" %>
    <script type="text/javascript">
        $(document).ready(function () {
            $("#inputForm").validate({
                submitHandler: function(form){
                    //检查网点时候相同
                    if($("#oldPointId").val() == $("#servicePointId").val()){
                        top.$.jBox.error("升级后网点与原网点相同");
                        return false;
                    }
                    var buttonsubmit = function(v, h, f) {
                        if (v == 'ok') {
                            loading('正在提交，请稍等...');
                            $("#btnSubmit").prop("disabled", true);
                            $.ajax({
                                type: "POST",
                                url: "${ctx}/md/engineer/saveUpgrade?"+ (new Date()).getTime(),
                                data:$(form).serialize(),
                                success: function (data) {
                                    if(data && data.success == true){
                                        top.mainFrame.repage();
                                        top.$.jBox.close();
                                    }
                                    else if( data && data.message){
                                        top.$.jBox.closeTip();
                                        top.$.jBox.error(data.message,"错误提示");
                                    }
                                    else{
                                        top.$.jBox.closeTip();
                                        top.$.jBox.error("保存错误","错误提示");
                                    }
                                    top.$.jBox.closeTip();
                                    $('#btnSubmit').removeAttr('disabled');
                                    return false;
                                },
                                error: function (e) {
                                    top.$.jBox.error("保存错误:"+e,"错误提示");
                                    $('#btnSubmit').removeAttr('disabled');
                                }
                            });

                        }else if (v == 'cancel') {
                            // 取消
                        }
                        return true; //close
                    };
                    top.$.jBox.confirm('确定要升级该安维为网点吗？', '确认', buttonsubmit);
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

        function closethisfancybox(){
            top.$.jBox.close();
        }

        function pointSelect_callback(data){
            $("[id^='servicePoint.servicePointNo']").val(data.servicePointNo);
        }

    </script>
</head>
<body>
<sys:message content="${message}"/>
<form:form id="inputForm" modelAttribute="engineer" method="post" class="form-horizontal">
    <form:hidden path="id"/>
    <legend>安维信息</legend>
    <div class="control-group">
        <label class="control-label">姓名:</label>
        <div class="controls">
            <form:input path="name" htmlEscape="false" readonly="true" class="required" />
            <span class=" red">*</span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">手机:</label>
        <div class="controls">
            <form:input path="contactInfo" htmlEscape="false" readonly="true"/>
            <span class=" red">*</span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">等级:</label>
        <div class="controls">
            <form:input path="level.label" htmlEscape="false" readonly="true" />
        </div>
    </div>
    <legend>订单信息</legend>
    <div class="control-group info">
        <label class="control-label">派单数:</label>
        <div class="controls">
            <form:input path="planCount" type="number" readonly="true" htmlEscape="false" class="uneditable-input"/>
        </div>
    </div>
    <div class="control-group success">
        <label class="control-label">完成数:</label>
        <div class="controls">
            <form:input path="orderCount" type="number" readonly="true" htmlEscape="false" class="uneditable-input"/>
        </div>
    </div>
    <div class="control-group error">
        <label class="control-label">违约单数:</label>
        <div class="controls">
            <form:input path="breakCount" type="number" readonly="true" htmlEscape="false" class="uneditable-input"/>
        </div>
    </div>
    <legend>原网点信息</legend>
    <div class="control-group">
        <label class="control-label">网点编号:</label>
        <div class="controls">
            <input type="text" id="oldPointNo" name="oldPointNo" value="${engineer.servicePoint.servicePointNo}" readonly="true" />
            <input type="hidden" id="oldPointId" name="oldPointId" value="${engineer.servicePoint.id}" />
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">网点名称:</label>
        <div class="controls">
            <input type="text" id="oldPointName" name="oldPointName" value="${engineer.servicePoint.name}" readonly="true" />
        </div>
    </div>
    <legend>升级后归属网点信息</legend>
    <div class="control-group">
        <label class="control-label">网点编号:</label>
        <div class="controls">
            <input id="servicePoint.servicePointNo" name="servicePoint.servicePointNo" class="input-small required valid" readonly="readonly" type="text" value="">
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">网点名称:</label>
        <div class="controls">
            <md:pointselectlayer id="servicePoint" name="servicePoint.id" value="" labelName="servicePointNo.name" labelValue=""
                                 width="1200" height="780" noSubEnginner="true" noblackList="true"
                                 showArea = "false" allowClear="false" callbackmethod="pointSelect_callback"
                                 title="选择服务网点" areaId="" cssClass="required"/>
        </div>
    </div>

    <div class="form-actions">
        <shiro:hasPermission name="md:engineer:upgrade">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="返 回" onclick="closethisfancybox();"/>
    </div>
</form:form>
</body>
</html>