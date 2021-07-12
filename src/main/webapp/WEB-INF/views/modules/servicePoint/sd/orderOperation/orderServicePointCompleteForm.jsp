<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>订单完成</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
	</style>
</head>
<body>
	<form:form id="inputForm" modelAttribute="order" action="${ctx}/servicePoint/sd/orderOperation/servicePointComplete" method="post" class="form-horizontal">
		<sys:message content="${message}"/>
		<c:if test="${!empty canSave and canSave eq true}">
		<div class="control-group" style="margin-top:30px;">
			<label class="control-label">完成类型:</label>
			<div class="controls">
				<c:set var="completeTypes" value="${fns:getDictListFromMS('completed_type')}" />
				<c:forEach items="${completeTypes}" var="dict">
					<label class="radio">
						<input type="radio" name="completeType" id="completeType_${dict.value}" value="${dict.value}" class="required">
						${dict.label}
					</label>
				</c:forEach>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">备注:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="4" maxlength="100" class="input-block-level"/>
			</div>
		</div>
		</c:if>
		<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
			<c:if test="${!empty canSave and canSave eq true}">
			<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethisfancybox();" />
		</div>
	</form:form>
	<script type="text/javascript">
        var index = top.layer.getFrameIndex(window.name); //获取窗口索引
        var clickTag = 0;
        <c:if test="${!empty canSave and canSave eq true}">
        $(document).ready(function() {
            $("#inputForm").validate({
                submitHandler: function(form){
                    if (clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr("disabled", "disabled");
                    var option =$('input:radio[name="completeType"]:checked');
                    if(option.val() ==="" ){
                        layer.msg('请选择完成类型。', {
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

                    var ajaxSuccess = 0;
                    var order = {};
                    order.orderId = '${order.orderId}';
                    order.quarter = '${order.quarter}';
                    order.orderNo = '${order.orderNo}';
                    order["pendingType.label"] = option.text();
                    order["pendingType.value"] = option.val();
                    order.remarks = $("#remarks").val();
                    var loadingIndex;
                    $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: "${ctx}/servicePoint/sd/orderOperation/servicePointComplete?"+ (new Date()).getTime(),
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
								var iframe = getActiveTabIframe();
								if(iframe != undefined){
									iframe.repage();
								}
								top.layer.close(index);
                                ajaxSuccess = 1;
                            }
                            else if( data && data.message){
                                layerError(data.message,"错误提示");
                            }
                            else{
                                layerError("提交错误","错误提示");
                            }
                            return false;
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"提交错误，请重试!");
                            console.log(e);
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
        });
		</c:if>
        function closethisfancybox(){
            top.layer.close(index);
        }
	</script>
</body>
</html>