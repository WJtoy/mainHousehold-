<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>添加</title>
    <meta name="decorator" content="default"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <style type="text/css">

        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 0;
            width: 100%;
            height: 60px;
            background: #fff;
            z-index: 10;
            border-top: 1px solid #e5e5e5;
        }

    </style>
    <script type="text/javascript">
        var this_index = top.layer.index;
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }

        var clickTag = 0;
        $(document).ready(function() {
            $("#inputForm").validate({
                submitHandler: function(form){
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");

                    if(clickTag == 1){
                        return false;
                    }
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }

                    clickTag = 1;
                    $btnSubmit.prop("disabled", true);

                    $.ajax({
                        url:"${ctx}/provider/md/disableWord/save",
                        type:"POST",
                        data:$(form).serialize(),
                        dataType:"json",
                        success: function(data){
                            //提交后的回调函数
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg("保存成功");
                                top.layer.close(this_index);//关闭本身
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                top.layer.close(loadingIndex);
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            top.layer.close(loadingIndex);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            //var msg = eval(data);
                            top.layer.close(loadingIndex);
                        },
                        timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                    });
                },
                errorContainer : "#messageBox",
                errorPlacement : function(error, element)
                {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }});
        });
    </script>
</head>
<body>
<form:form id="inputForm" modelAttribute="mdDisableWord" method="post" action="${ctx}/provider/md/disableWord/save" class="form-horizontal">
    <sys:message content="${message}"/>

    <div class="control-group" style="margin-top:90px;float: left;margin-left: 60px;">
        <label style="float: left;margin-left: 20px;margin-top: 5px">禁用词语：</label>
    <div class="controls" style="margin-left: 90px;position: relative;">
        <form:textarea path="word" htmlEscape="false" rows="12" maxlength="255" class="input-xlarge"
                       cssStyle="width: 486px;float: left;" placeholder="输入禁用词语，多个禁用词语用“逗号”隔开"/>
    </div>
    </div>
    <div id="editBtn" class="line-row">
        <shiro:hasPermission name="md:disableWord:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="width: 96px;height: 40px;margin-left: 70%;margin-top: 10px;margin-bottom: 10px"/>
            &nbsp;</shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="width: 96px;height: 40px;margin-top: 10px;margin-left: 13px;margin-bottom: 10px"/>
    </div>
</form:form>

</body>
</html>
