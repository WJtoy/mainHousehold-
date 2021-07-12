<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>客户管理业务员变更</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <style type="text/css">
        .form-horizontal{margin-top:5px;}
        .form-horizontal .control-label {width: 80px;}
        .form-horizontal .controls {margin-left: 100px;}
    </style>
    <script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        $(document).ready(function() {
            $("#inputForm").validate({
                submitHandler: function(form){
                    if (clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    var ajaxSuccess = 0;
                    var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr('disabled', 'disabled');
                    var loadingIndex;
                    $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: "${ctx}/md/customer/saveSales?"+ (new Date()).getTime(),
                        data:{id:$("#id").val(),salesId:$("#sales\\.id").val()},
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
                            if(data && data.success == true){
                                top.layer.close(this_index);//关闭本窗口
                                ajaxSuccess = 1;

                                var iframe = getActiveTabIframe();
                                if(iframe != undefined){
                                    iframe.repage($("#sales\\.id").val(),data.data);
                                }

                                /*
                                var title = $("#remarks").val();
                                title = title.substrByContainerWidth(9);
                                if(parentIndex !='') {
                                    //订单详情页调用
                                    var layero = $("#layui-layer" + parentIndex, top.document);
                                    var iframeWin = top[layero.find('iframe')[0]['name']];
                                    iframeWin.updateFeedback(data.data, title);
                                }else{
                                    //订单列表调用
                                    //回调父窗口方法 旧方法：列表中弹出
                                    var iframe = getActiveTabIframe();
                                    if(iframe != undefined){
                                        iframe.repage();
                                    }
                                }
                                */
                            }
                            else if( data && data.message){
                                layerError(data.message,"错误提示");
                            }
                            else{
                                layerError("保存业务员错误","错误提示");
                            }
                            return false;
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"保存业务员错误1，请重试!");
                        }
                    });

                    return false;
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

            $("#remarks").focus();
        });

        function closethiswindow(){
            top.layer.close(this_index);
        }
    </script>
</head>
<body>
<form:form id="inputForm" modelAttribute="customer" action="${ctx}/md/customer/salesSave" method="post" class="form-horizontal">
    <form:hidden path="id"/>
    <sys:message content="${message}"/>
    <div class="control-group" style="margin-top:30px;">
        <label class="control-label">业务员:</label>
        <div class="controls">
            <div class="controls">
                <form:select path="sales.id" cssClass="required input-medium" cssStyle="width: 220px;">
                    <form:options items="${fns:getSaleList()}" itemLabel="name" itemValue="id" htmlEscape="false" />
                </form:select>
                <span class="red">*</span>
            </div>
        </div>
    </div>
    <div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;margin-top:40px;">
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>
        <input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethiswindow()"/>
        <%--<input id="btnCancel" class="btn" type="button" value="测试调用parent" onclick="test()"/>--%>
    </div>
</form:form>
</body>
</html>
