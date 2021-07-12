<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>天猫一键求助反馈</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script type="text/javascript">
        var this_index = top.layer.index;
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        if(!parentIndex){
            parentIndex = getCookie('layer.parent.id');
        }
        var clickTag = 0;
        $(document).ready(function () {
            $('#replyContent').keyup(function() {
                var len=this.value.length;
                $('#textCount').text(len);
            });

            $("#inputForm").validate({
                submitHandler: function (form) {
                    if(clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr('disabled', 'disabled');
                    var loadingIndex;
                    var ajaxSuccess = 0;
                    $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: "${ctx}/sd/order/anomaly/reply?" + (new Date()).getTime(),
                        data: $(form).serialize(),
                        beforeSend: function () {
                            loadingIndex = layer.msg('正在提交，请稍等...', {
                                icon: 16,
                                time: 0,
                                shade: 0.3
                            });
                        },
                        complete: function () {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            if(ajaxSuccess == 0) {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                            }
                        },
                        success: function (data) {
                            if(ajaxLogout(data)){
                                return false;
                            }
                            if (data && data.success == true) {
                                ajaxSuccess = 1;
                                if($("#refreshType").val() == "refreshList"){//刷新list
                                    var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                                    if(iframe != undefined){
                                        iframe.repage();
                                    }
                                }else{
                                    //回调父窗口方法
                                    setTimeout(function() {
                                        top.layer.close(this_index);//关闭本窗口
                                        var layero = $("#layui-layer" + parentIndex,top.document);
                                        var iframeWin = top[layero.find('iframe')[0]['name']];
                                        iframeWin.refreshAnomalyRow(data.data);
                                        //clickTag = 0;//还原标记
                                        return false;
                                    }, 150);
                                    return false;
                                }
                            }
                            else if (data && data.message) {
                                layerError(data.message,"错误提示");
                            }
                            else {
                                layerError("反馈错误", "错误提示");
                            }
                            return false;
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"反馈错误，请重试!");
                        }
                    });
                    return false;
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

        function closethislayer() {
            top.layer.close(this_index);
        }
        function load(){
            $("#replyContent").focus();
        }
    </script>

    <style type="text/css">
        .form-horizontal {margin-top: 5px;}
        .form-horizontal .control-label {width:100px;}
        .form-horizontal .controls{margin-left:110px}
        i[class^="icon-"] {font-size:18px;}
    </style>
</head>
<body onload="load()">
<form:form id="inputForm" modelAttribute="anomaly"  method="post" class="form-horizontal">
    <form:hidden path="id"/>
    <form:hidden path="anomalyRecourseId"/>
    <form:hidden path="quarter"/>
    <input type="hidden" id="refreshType" name="refreshType" value="${refreshType}"/>
    <sys:message content="${message}"/>
    <div class="row-fluid">
        <div class="span12">
            <div class="control-group">
                <label class="control-label">反馈内容:</label>
                <div class="controls">
                    <form:textarea path="replyContent" htmlEscape="false" rows="3" cssClass="required"  maxlength="250" cssStyle="width: 470px;"/><span class="red">  *</span>
                    <p class="text-error"><span id="textCount">0</span>/250</p>
                </div>
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span12">
            <div class="control-group">
                <div class="controls">
                <div class="alert" style="margin:-5px 5px 5px;">
                    反馈内容不超过250个汉字
                </div>
                </div>
            </div>
        </div>
    </div>
    <div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
        <c:if test="${empty canSave || canSave ne false }">
            <shiro:hasPermission name="sd:order:anomaly">
                <input id="btnSubmit" class="btn btn-primary" type="submit" value="保存"/>&nbsp;</shiro:hasPermission>
        </c:if>
        <input id="btnCancel" class="btn" type="button" value="关闭" onclick="closethislayer();"/>
    </div>
</form:form>
</body>
</html>