<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>取消导入订单</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
	</style>
</head>
<body>
<form:form id="inputForm" modelAttribute="order" method="post" class="form-horizontal">
	<form:hidden path="id"/>
	<sys:message content="${message}"/>
	<div class="control-group error">
		<label class="control-label">取消原因:</label>
		<div class="controls">
			<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="150" style="width:90%" />
		</div>
	</div>
	<div class="form-actions">
			<shiro:hasPermission name="sd:temporder:cancel"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
		<input id="btnCancel" class="btn" type="button" value="返 回" onclick="closeme();" />
	</div>
</form:form>
<script type="text/javascript">
    //var this_index = top.layer.index;
    // var this_index = getCookie('layer.parent.id');
    // if(!this_index){
    //     this_index = top.layer.index;
    // }
    var clickTag = 0;
    $(document).ready(function() {
        $("#inputForm").validate({
            submitHandler: function(form){
                if(clickTag == 1){
                    return false;
				}
				clickTag = 1;
                var confirmClickTag = 0;
                var $btnSubmit = $("#btnSubmit");
                top.layer.confirm('确定要取消订单吗?', {
                    icon: 3
					,title:'系统确认'
                    ,cancel: function(index, layero){
                        clickTag=0;
                        $btnSubmit.removeAttr('disabled');
					}
				}, function(index,layero){
                    if(confirmClickTag == 1){
                        return false;
                    }
                    var btn0 = $(".layui-layer-btn0",layero);
                    if(btn0.hasClass("layui-btn-disabled")){
                        return false;
                    }
                    confirmClickTag = 1;
                    btn0.addClass("layui-btn-disabled").attr("disabled","disabled");

                    // do something
                    $btnSubmit.attr('disabled', 'disabled');
                    var loadingIndex = layer.msg('正在处理中，请稍等...', {
                        icon: 16,
                        time: 0,//不定时关闭
                        shade: 0.3
                    });
                    var ajaxSuccess = 0;
                    top.layer.close(index);//关闭确认窗口本身
                    $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: "${ctx}/sd/order/import/new/cancel?"+ (new Date()).getTime(),
                        data:$(form).serialize(),
                        complete: function () {
                            layer.close(loadingIndex);
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
                                ajaxSuccess = 1;
                                //top.layer.closeAll();//关闭本身
                                //回调父窗口方法
                                var iframe = getActiveTabIframe();
                                if(iframe != undefined){
                                    iframe.repage();
                                }
                                var this_index = parent.layer.getFrameIndex(window.name);
                                parent.layer.close(this_index);
                                return false;
                            }
                            else if( data && data.message){
                                layerError(data.message,"错误提示");
                            }
                            else{
                                layerError("取消订单错误","错误提示");
                            }
                            return false;
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"取消订单错误，请重试!");
                        }
                    });
                    return false;
                },function(index) {//cancel
                    $btnSubmit.removeAttr('disabled');
                    clickTag = 0;
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
        var this_index = parent.layer.getFrameIndex(window.name);
        parent.layer.close(this_index);
    }

</script>
</body>
</html>