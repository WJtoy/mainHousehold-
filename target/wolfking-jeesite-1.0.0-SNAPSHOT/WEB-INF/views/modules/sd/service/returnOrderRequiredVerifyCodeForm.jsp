<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>退单</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <script type="text/javascript">
        Order.rootUrl = "${ctx}";
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var this_index = top.layer.index;
        var clickTag = 0;
        $(document).ready(function () {
            $("#inputForm").validate({
                submitHandler: function (form) {
                    var $btnSubmit = $("#btnSubmit");
                    top.layer.confirm(
                        '确定要退单吗？'
                        , {
                            icon: 3, closeBtn: 0, title: '系统确认', zIndex: 19891017, success: function (layro, index) {
                                $(document).on('keydown', layro, function (e) {
                                    if (e.keyCode == 13) {
                                        layro.find('a.layui-layer-btn0').trigger('click')
                                    } else if (e.keyCode == 27) {//esc
                                        clickTag = 0;
                                        $btnSubmit.removeAttr('disabled');
                                        top.layer.close(index);//关闭本身
                                    }
                                })
                            }
                        }
                        , function (index) {
                            if (clickTag == 1) {
                                return false;
                            }
                            clickTag = 1;
                            $btnSubmit.attr('disabled', 'disabled');
                            top.layer.close(index);//关闭确认窗口本身
                            //do something
                            var order = {};
                            order.orderId = $("#orderId").val();
                            var option = $("#pendingFlag option:selected");
                            order["pendingType.label"] = option.text();
                            order["pendingType.value"] = option.val();
                            order.verifyCode = $("#verifyCode").val();
                            order.remarks = $("#remarks").val();
                            var loadingIndex;
                            $.ajax({
                                async: false,
                                cache: false,
                                type: "POST",
                                url: "${ctx}/sd/order/returnOrderWithVerifyCode?_t=" + (new Date()).getTime(),
                                dataType: 'json',
                                data: order,
                                beforeSend: function () {
                                    loadingIndex = top.layer.msg('正在提交，请稍等...', {
                                        icon: 16,
                                        time: 0,
                                        shade: 0.3
                                    });
                                },
                                complete: function () {
                                    //console.log("" + new Date().getTime() + " [complete] clickTag:" + clickTag);
                                    if (loadingIndex) {
                                        top.layer.close(loadingIndex);
                                    }
                                    setTimeout(function () {
                                        clickTag = 0;
                                        $btnSubmit.removeAttr('disabled');
                                        //console.log("" + new Date().getTime() + " [complete] clickTag:" + clickTag);
                                    }, 2000);
                                },
                                success: function (data) {
                                    if (ajaxLogout(data)) {
                                        return false;
                                    }
                                    if (data && data.success == true) {
                                        //回调父窗口方法
                                        if (parentIndex && parentIndex != undefined && parentIndex != '') {
                                            var layero = $("#layui-layer" + parentIndex, top.document);
                                            var iframeWin = top[layero.find('iframe')[0]['name']];
                                            iframeWin.reload();
                                            closethiswindow();//关闭本窗口
                                        }
                                        top.layer.close(this_index);
                                        return false;
                                    } else if (data && data.message) {
                                        layerError(data.message, "错误提示");
                                    } else {
                                        layerError("退单错误", "错误提示");
                                    }
                                    return false;
                                },
                                error: function (e) {
                                    ajaxLogout(e.responseText, null, "退单错误，请重试!");
                                }
                            });
                            return false;
                        }, function (index) {//cancel
                            //$btnSubmit.removeAttr('disabled');
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
            $('#sendVerifyCode').click(function () {
                top.layer.confirm('确定要获取验证码吗?', {icon: 3, title: '系统确认'}, function (index) {
                    top.layer.close(index);
                    var loadingIndex = top.layer.msg('正在获取验证码，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
                    var order = {};
                    order.orderId = $("#orderId").val();
                    order.quarter = $("#quarter").val();
                    var option = $("#pendingFlag option:selected");
                    order["pendingType.label"] = option.text();
                    order["pendingType.value"] = option.val();
                    order.remarks = $("#remarks").val();
                    $.ajax({
                        cache: false,
                        type: "POST",
                        url: "${ctx}/sd/order/sendReturnVerifyCode?_t=" + (new Date()).getTime(),
                        data: order,
                        success: function (data) {
                            top.layer.close(loadingIndex);
                            if (data.success) {
                                top.layer.close(index);
                                timeOut("#sendVerifyCode", 90);
                                return false;
                            } else {
                                layerError(data.message, '错误提示');
                            }
                        },
                        error: function (e) {
                            top.layer.close(loadingIndex);
                            ajaxLogout(e.responseText, null, "获取验证码错误，请重试!");
                        }
                    });
                });
                return false;
            });
        });

        function closethiswindow() {
            top.layer.close(this_index);
        }

        function timeOut(id, second){
            var sendBtn = $(id);
            // sendBtn.addClass('btn-dis');
            sendBtn.val(second + "秒");
            sendBtn.attr('disabled',true);

            var timer = setInterval(function(){
                second--;
                sendBtn.val(second + "秒");
                if(second === 0){
                    clearInterval(timer);
                    sendBtn.val('验证码');
                    // sendBtn.removeClass('btn-dis');
                    sendBtn.attr('disabled',false);
                    return true;
                }else{
                    return false;
                }
            },1000);
        }

    </script>
    <style type="text/css">
        .form-horizontal {
            margin-top: 5px;
        }
    </style>
</head>
<body>
<form:form id="inputForm" modelAttribute="order" action="${ctx}/sd/order/returnOrderWithVerifyCode" method="post" class="form-horizontal">
    <form:hidden path="orderId"/>
    <form:hidden path="quarter"/>
    <sys:message content="${message}"/>
    <c:if test="${order.orderId != null && canReturn == true}">
        <div class="control-group" style="margin-top: 10px;">
            <label class="control-label">退单类型:</label>
            <div class="controls">
                <form:select path="pendingFlag" class="input-xlarge required" style="width:60%">
                    <form:options items="${fns:getDictExceptListFromMS('cancel_responsible','1,2,56,101')}"
                                  itemLabel="label" itemValue="value" htmlEscape="false"/><!-- 切换为微服务 -->
                </form:select>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">验证码:</label>
            <div class="controls">
                <form:input path="verifyCode" htmlEscape="false" maxlength="6" class="required" style="width:57%"/>
            </div>
        </div>
        <div class="control-group error">
            <label class="control-label">备注说明:</label>
            <div class="controls">
                <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="150" style="width:90%"/>
            </div>
        </div>
    </c:if>
    <div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
        <input id="sendVerifyCode" class="btn btn-info" type="button" value="验证码"/>
        <c:if test="${order.orderId != null && canReturn == true }">
            <shiro:hasPermission name="sd:order:return"><input id="btnSubmit" class="btn btn-primary" type="submit"
                                                               value="保 存"/>&nbsp;</shiro:hasPermission>
        </c:if>
        <input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethiswindow();"/>
        <c:if test="${manuCloseMaterialForm != null && manuCloseMaterialForm == 1}">
            <a class="btn btn-warning" data-toggle="tooltip" data-tooltip="点击处理配件" href="javascript:;"
               onclick="Order.attachlist('${order.orderId}','${order.orderNo}','${order.quarter}',this_index);">配件单</a>
        </c:if>
    </div>
</form:form>
</body>
</html>