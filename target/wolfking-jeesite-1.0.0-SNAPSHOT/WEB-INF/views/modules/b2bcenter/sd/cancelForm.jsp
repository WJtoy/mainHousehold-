<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>取消工单转换</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <style type="text/css">
        .form-horizontal{margin-top:5px;}
    </style>
</head>
<body>
<form:form id="inputForm" modelAttribute="order" method="post" class="form-horizontal">
    <form:hidden path="b2bOrderNo"/>
    <form:hidden path="dataSource"/>
    <form:hidden path="b2bOrderId"/>
    <sys:message content="${message}"/>
    <div class="control-group error">
        <label class="control-label">取消原因:</label>
        <div class="controls">
            <form:textarea path="processComment" htmlEscape="false" rows="3" maxlength="150" style="width:90%" />
        </div>
    </div>
    <div class="form-actions">
        <c:if test="${canAction == true }">
            <shiro:hasPermission name="b2b:order:transfer"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
        </c:if>
        <input id="btnCancel" class="btn" type="button" value="返 回" onclick="closeme();" />
    </div>
</form:form>
<script type="text/javascript">
    var this_index = top.layer.index;
    var clickTag = 0;
    $(document).ready(function() {
        $("#inputForm").validate({
            submitHandler: function(form){
                var $btnSubmit =  $("#btnSubmit");
                layer.confirm(
                    '确定要取消订单吗?'
                    ,{icon: 3, closeBtn: 0,title:'系统确认',success: function(layro, index) {
                            $(document).on('keydown', layro, function(e) {
                                if (e.keyCode == 13) {
                                    layro.find('a.layui-layer-btn0').trigger('click')
                                }else if(e.keyCode == 27){
                                    $btnSubmit.removeAttr('disabled');
                                    top.layer.close(index);//关闭本身
                                }
                            })
                        }}
                    ,function(index){
                        if(clickTag == 1){
                            return false;
                        }
                        clickTag = 1;
                        $btnSubmit.attr('disabled', 'disabled');
                        layer.close(index);//关闭确认窗口本身
                        // do something
                        var loadingIndex;
                        var ajaxSuccess = 0;
                        $.ajax({
                            async: false,
                            cache: false,
                            type: "POST",
                            url: "${ctx}/b2b/b2bcenter/order/cancelOrderTransition",
                            data:$(form).serialize(),
                            beforeSend: function () {
                                loadingIndex = layer.msg('正在审核订单，请稍等...', {
                                    icon: 16,
                                    time: 0,//不定时关闭
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
                                setTimeout(function() {
                                    if(ajaxLogout(data)){
                                        return false;
                                    }
                                    if(data && data.success == true){
                                        //回调父窗口方法
                                        var iframe = getActiveTabIframe();
                                        if(iframe != undefined){
                                            iframe.repage();
                                            ajaxSuccess = 1;
                                        }
                                        top.layer.close(this_index);//关闭本身
                                        return false;
                                    }
                                    else if( data && data.message){
                                        layerError(data.message,"错误提示");
                                    }
                                    else{
                                        layerError("取消订单错误","错误提示");
                                    }
                                }, 500);
                                return false;
                            },
                            error: function (e) {
                                ajaxLogout(e.responseText,null,"取消订单错误，请重试!");
                            }
                        });
                        return false;
                    }
                    ,function(index){//cancel
                        //$btnSubmit.removeAttr('disabled');
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

    });

    function closeme(){
        top.layer.close(this_index);
    }

</script>
</body>
</html>