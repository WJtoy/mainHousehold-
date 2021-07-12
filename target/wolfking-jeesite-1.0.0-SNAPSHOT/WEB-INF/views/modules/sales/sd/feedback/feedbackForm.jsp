<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>问题反馈</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<style type="text/css">
	.form-horizontal{margin-top:10px;}
	</style>
	<!-- 禁用词 -->
	<md:filterDisabledWord />
	<script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
					var context = $("#remarks").val();
					var forbiddenArray = filterForbiddenStr(context);
					if(forbiddenArray != null){
						layerAlert("内容含<font color='#4EB4E4'>【" + forbiddenArray.toLocaleString() + "】</font>等不文明用语,请注意用词文明！","提示");
						return false;
					}
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
						url: "${ctx}/sales/sd/feedBack/save?"+ (new Date()).getTime(),
						data:$(form).serialize(),
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
					       }
					       else if( data && data.message){
                               layerError(data.message,"错误提示");
					       }
					       else{
                               layerError("保存问题反馈错误","错误提示");
					       }
					       return false;
						},
						error: function (e) {
                            ajaxLogout(e.responseText,null,"保存问题反馈错误，请重试!");
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
	<form:form id="inputForm" modelAttribute="feedback" action="${ctx}/sales/sd/feedBack/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="customer.id"/>
		<form:hidden path="order.id"/>
		<form:hidden path="quarter"/>
		<form:hidden path="title" />
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">订单号:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${empty feedback.order.orderNo }">
						<form:input path="order.orderNo" htmlEscape="false"  maxlength="30" />
					</c:when>
					<c:otherwise>
						<form:input path="order.orderNo" htmlEscape="false" readonly="true" maxlength="30" />
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">反馈方:</label>
			<div class="controls">
				<form:select path="feedFrom" class="required" cssStyle="width: 220px;">
					<option value="用户" <c:out value="${(feedback.feedFrom eq '用户')?'selected=selected':''}" />  >用户</option>
    				<option value="厂商" <c:out value="${(feedback.feedFrom eq '厂商')?'selected=selected':''}" /> >厂商</option>
   				</form:select>
			</div>
		</div>
		<%--<div class="control-group">--%>
			<%--<label class="control-label">标题:</label>--%>
			<%--<div class="controls">--%>
				<%--<form:input path="title" htmlEscape="false" maxlength="100" class="required"/>--%>
			<%--</div>--%>
		<%--</div>--%>
		<div class="control-group">
			<label class="control-label">反馈内容:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="8" maxlength="512" class="input-xxlarge required" disabled="${empty feedback.id ?false:true}"/>
			</div>
		</div>
		<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
			<shiro:hasAnyPermissions name="sd:feedback:add">
				<c:if test="${canAction == true}">
					<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>
				</c:if>&nbsp;
			</shiro:hasAnyPermissions>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethiswindow()"/>
				<%--<input id="btnCancel" class="btn" type="button" value="测试调用parent" onclick="test()"/>--%>
		</div>
	</form:form>
</body>
</html>