<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>待跟进</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery-datetimepicker/jquery.datetimepicker.css" rel="stylesheet"/>
	<script src="${ctxStatic}/jquery-datetimepicker/jquery.datetimepicker.full.js" type="text/javascript"></script>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
	</style>
</head>
<body>
	<form:form id="inputForm" modelAttribute="order" action="${ctx}/sd/order/appoint" method="post" class="form-horizontal">
		<form:hidden path="orderId"/>
		<sys:message content="${message}"/>
		<c:if test="${empty canSave}">
		<div class="control-group">
			<label class="control-label">下次跟进日期:</label>
			<div class="controls">
				<input id="appointmentDate" name="appointmentDate" class="Wdate" readonly="readonly" type="text" value="<fmt:formatDate value='${order.appointmentDate}' pattern='yyyy-MM-dd HH:mm' />"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">备注:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="7" maxlength="100" class="input-block-level"/>
			</div>
		</div>
		</c:if>
		<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
			<c:if test="${empty canSave}">
			<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethisfancybox();" />
		</div>
	</form:form>
	<script type="text/javascript">
        $('#appointmentDate').datetimepicker();
        var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
        var clickTag = 0;
		<c:if test="${empty canSave}">
        $(document).ready(function() {
            var fromType = ${order.feedbackId==null?0:order.feedbackId};//发起方 0-订单列表 1-订单详情页
            $("#inputForm").validate({
                submitHandler: function(form){
                    if (clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr("disabled", "disabled");
                    var ajaxSuccess = 0;
                    var order = {};
                    order.orderId = $("#orderId").val();
                    var time= $("#appointmentDate").val();
                    order.appointmentDate = time;
                    order.remarks = $("#remarks").val();

                    if(time=="" )
                    {
                        layer.msg('未设置下次跟进日期。', {
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
                    $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: "${ctx}/sd/order/nextFollowUpTime?"+ (new Date()).getTime(),
                        data:order,
                        beforeSend: function () {
                            loadingIndex = top.layer.msg('正在提交，请稍等...', {
                                icon: 16,
                                time: 0,//不定时关闭
                                shade: 0.3
                            });
                        },
                        complete: function () {
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
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
                                if(fromType == 1){
								    //from order detail
									top.layer.msg('保存成功',{time: 2000},function(){
                                        parent.layer.close(index);
									});
                                }else{
                                    var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                                    if(iframe != undefined){
                                        iframe.repage();
                                    }
                                    parent.layer.close(index);
                                }
                                ajaxSuccess = 1;
                            }
                            else if( data && data.message){
                                layerError(data.message,"错误提示");
                            }
                            else{
                                layerError("保存下次跟进日期错误","错误提示");
                            }
                            return false;
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"保存下次跟进日期错误，请重试!");
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
            //$("#appointmentDate").focus();
        });
		</c:if>
        function closethisfancybox(){
            parent.layer.close(index);
        }
	</script>
</body>
</html>