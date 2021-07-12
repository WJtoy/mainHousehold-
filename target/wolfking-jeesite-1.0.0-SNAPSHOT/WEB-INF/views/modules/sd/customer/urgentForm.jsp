<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>厂商加急</title>
	<meta name="decorator" content="default"/>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
	</style>
</head>
<body>
	<form:form id="inputForm" modelAttribute="order" method="post" class="form-horizontal">
		<form:hidden path="orderId"/>
		<form:hidden path="customerId"/>
		<form:hidden path="areaId"/>
		<form:hidden path="chargeIn"/>
		<form:hidden path="chargeOut"/>
		<sys:message content="${message}"/>
		<c:if test="${canAction == true}">
		<div class="control-group">
			<label class="control-label">加急:</label>
			<div class="controls">
				<c:if test="${!empty order.urgentLevels}">
					<form:radiobuttons path="urgentLevel.id" items="${order.urgentLevels}" itemLabel="remarks" itemValue="id" htmlEscape="false" class="required"/>
				</c:if>
				<br>
				<span class="help-inline alert" style="color: red;padding: 3px 3px;">
						加急费：<label id="lblUrgentCharge" style="min-width:30px;display:inherit;">0.0</label> 元
					</span>
				<%--加急费：<label class="label label-important" style="min-width: 30px;" id="lblUrgentCharge">0.0</label>元--%>
			</div>
		</div>
		</c:if>
		<div class="control-group">
			<label class="control-label">备注:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="100" style="width:90%" />
			</div>
		</div>
		<div class="form-actions">
			<c:if test="${canAction == true }">
			<shiro:hasPermission name="sd:order:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
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
                    var $btnSubmit = $("#btnSubmit");
                    layer.confirm(
                        '确定要保存设定加急吗?'
                        ,{icon: 3, closeBtn: 0,title:'系统确认',success: function(layro, index) {
                            $(document).on('keydown', layro, function(e) {
                                if (e.keyCode == 13) {
                                    layro.find('a.layui-layer-btn0').trigger('click')
                                }else if(e.keyCode == 27){
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
							top.layer.close(index);//关闭确认窗口本身
							// do something
							var loadingIndex;
							$.ajax({
                                async: false,
                                cache: false,
								type: "POST",
								url: "${ctx}/sd/order/setUrgent?"+ (new Date()).getTime(),
								data:$(form).serialize(),
                                beforeSend: function () {
                                    loadingIndex = top.layer.msg('正在提交，请稍等...', {
                                        icon: 16,
                                        time: 0,
                                        shade: 0.3
                                    });
                                },
                                complete: function () {
								    //console.log("complete:" + new Date().getTime());
                                    if(loadingIndex) {
                                        top.layer.close(loadingIndex);
                                    }
                                    setTimeout(function () {
                                        clickTag = 0;
                                        $btnSubmit.removeAttr('disabled');
                                    }, 2000);
                                },
								success: function (data) {
									if(ajaxLogout(data)){
										return false;
									}
									if(data && data.success == true){
										var pframe = getActiveTabIframe();//定义在jeesite.min.js中
										if(pframe){
											pframe.repage();
										}
                                        //console.log("success end:" + new Date().getTime());
										top.layer.close(this_index);//关闭本身
										//layerMsg("加急修改成功！");//此行脚本在ie内核浏览器下错误
										return false;
									}
									else if( data && data.message){
										layerError(data.message,"加急");
									}
									else{
										layerError("加急错误","加急");
									}

									return false;
								},
								error: function (e) {
                                    ajaxLogout(e.responseText,null,"加急错误，请重试!");
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
        Order.rootUrl = "${ctx}";
        $("input:radio[name='urgentLevel.id']").change(function(event) {
			Order.changeUrgentLevel();
        });

        function closeme(){
            top.layer.close(this_index);
        }
        function testlayer(){
            layerMsg('测试');
        }

	</script>
</body>
</html>