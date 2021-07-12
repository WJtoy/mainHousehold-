<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>预约时间</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
	</style>
</head>
<body>
	<form:form id="inputForm" modelAttribute="complain" action="${ctx}/sd/complain/appointSave" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="quarter"/>
		<sys:message content="${message}"/>
		<c:if test="${canSave}">
		<div class="control-group">
			<label class="control-label">跟进日期:</label>
			<div class="controls">
				<input id="appointDate" name="appointDate" class="Wdate" type="text" value="<fmt:formatDate value='${complain.appointDate}' pattern='yyyy-MM-dd HH:mm' />" onClick="WdatePicker({startDate: '%y-%M-%d 08:00:00' ,dateFmt:'yyyy-MM-dd HH:mm',isShowClear:true})"/>
			</div>
		</div>
		<%--<div class="control-group">
			<label class="control-label">备注:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="2" maxlength="100" class="input-block-level"/>
			</div>
		</div>--%>
		</c:if>
		<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
			<c:if test="${canSave}">
			<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethiswindow();" />
		</div>
	</form:form>
	<script type="text/javascript">
        var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
        <c:if test="${canSave}">
        var clickTag = 0;
        $(document).ready(function() {
            $("#inputForm").validate({
                submitHandler: function(form){
                    if(clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr('disabled', 'disabled');

                    var orderCompalin = {};
                    orderCompalin.id = $("#id").val();
                    orderCompalin.quarter = $("#quarter").val();
                    var time= $("#appointDate").val();
                    orderCompalin.appointDate = time;
                    if(time=="" )
                    {
                        layer.msg('未设置跟进日期。', {
                            time: 0,
                            icon: 5
                            ,btn: ['关闭']
                            ,btnAlign: 'c'
                            ,yes: function(index){
                                layer.close(index);
                            }
                        });
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
                        return false;
                    }
                    var loadingIndex;
                    var ajaxSuccess = 0;
                    $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: "${ctx}/sd/complain/appointSave?"+ (new Date()).getTime(),
                        data:orderCompalin,
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
                                //回调父窗口方法
								var iframe = getActiveTabIframe();//定义在jeesite.min.js中
								if(iframe != undefined){
									iframe.repage();
								}
								parent.layer.close(index);
								ajaxSuccess = 1;
                            }
                            else if( data && data.message){
                                layerError(data.message,"错误提示");
                            }
                            else{
                                layerError("保存跟进日期错误","错误提示");
                            }
                            return false;
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"保存跟进日期错误，请重试!");
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
            $("#appointmentDate").focus();
        });
		</c:if>
        function closethiswindow(){
            parent.layer.close(index);
        }
	</script>
</body>
</html>