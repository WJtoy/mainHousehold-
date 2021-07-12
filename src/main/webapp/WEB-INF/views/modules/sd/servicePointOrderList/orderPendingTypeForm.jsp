<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>停滞原因</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery-datetimepicker/jquery.datetimepicker.css?v=1543199858" rel="stylesheet"/>
	<script src="${ctxStatic}/jquery-datetimepicker/jquery.datetimepicker.full.js?v=1543199858" type="text/javascript"></script>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
	</style>
</head>
<body>
	<form:form id="inputForm" modelAttribute="order" action="${ctx}/sd/order/servicePointOrderList/service/pending" method="post" class="form-horizontal">
		<%--<form:hidden path="orderId"/>--%>
		<%--<form:hidden path="quarter"/>--%>
		<%--<form:hidden path="orderNo"/>--%>
		<sys:message content="${message}"/>
		<c:if test="${!empty canSave and canSave eq true}">
		<div class="control-group" style="margin-top:30px;">
			<label class="control-label">停滞原因:</label>
			<div class="controls">
				<form:select path="pendingType.value" class="required" cssStyle="width: 220px;">
					<!-- 排除：待跟进 -->
					<form:options items="${fns:getDictExceptListFromMS('PendingType','7')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
				</form:select>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">预约日期:</label>
			<div class="controls">
					<input id="appointmentDate" name="appointmentDate" class="Wdate" type="text" readonly="readonly" value="<fmt:formatDate value='${order.appointmentDate}' pattern='yyyy-MM-dd HH:mm' />"/>
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
        $('#appointmentDate').datetimepicker({
            allowTimes:[
                '12:00', '17:00', '22:00'
            ],
            allowTimeTitles:[
              	'上午：7:00-12:00','下午：12:00-18:00','晚上：18:00-22:00'
			],
			timerPickerWidth: 120,
		});
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
                    var ajaxSuccess = 0;
                    var order = {};
                    order.orderId = '${order.orderId}';
                    order.quarter = '${order.quarter}';
                    order.orderNo = '${order.orderNo}';
                    var option = $("[id='pendingType.value'] option:selected");
                    order["pendingType.label"] = option.text();
                    order["pendingType.value"] = option.val();
                    var time= $("#appointmentDate").val();
                    order.appointmentDate = time;
                    order.remarks = $("#remarks").val();

                    if(option.val() !="" && time=="" )
                    {
                        layer.msg('设定了停滞原因必须也设置时间。', {
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
                        url: "${ctx}/sd/order/servicePointOrderList/service/pending?"+ (new Date()).getTime(),
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
								var iframe = getActiveTabIframe();//定义在jeesite.min.js中
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
                                layerError("设置停滞原因错误","错误提示");
                            }
                            return false;
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"设置停滞原因错误，请重试!");
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