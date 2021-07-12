<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>回访失败[客服]</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
		    // $("#createDate").val(moment().format("YYYY-MM-DD HH:mm:ss"));
			$("#inputForm").validate({
				submitHandler: function(form){
                    var $btnSubmit = $("#btnSubmit");
                    top.layer.confirm(
                        '确定回访失败吗？'
                        ,{icon: 3,closeBtn: 0,title:'系统确认',success: function(layro, index) {
                            $(document).on('keydown', layro, function(e) {
                                if (e.keyCode == 13) {
                                    layro.find('a.layui-layer-btn0').trigger('click')
                                }else if(e.keyCode == 27){//esc
                                    clickTag = 0;
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
                            top.layer.close(index);//关闭确认窗口本身
                            //do something
                            var order = {};
                            order.orderId = $("#orderId").val();
                            order.quarter = $("#quarter").val();
                            order.remarks = $("#remarks").val();
                            //order.createDate = $("#createDate").val();
                            var loadingIndex;
                            $.ajax({
                                async: false,
                                cache: false,
                                type: "POST",
                                url: "${ctx}/sd/order/kefuOrderList/service/followUpFail?_t="+ (new Date()).getTime(),
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
                                        var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                                        if(iframe != undefined) {
                                            iframe.repage();
                                        }
                                        // //回调父窗口方法
                                        // if(parentIndex && parentIndex != undefined && parentIndex != ''){
                                        //     var layero = $("#layui-layer" + parentIndex,top.document);
                                        //     var iframeWin = top[layero.find('iframe')[0]['name']];
                                        //     iframeWin.reload();
                                        //     closethiswindow();//关闭本窗口
                                        // }
                                        return false;
                                    }
                                    else if( data && data.message){
                                        layerError(data.message,"错误提示");
                                    }
                                    else{
                                        layerError("保存错误","错误提示");
                                    }
                                    return false;
                                },
                                error: function (e) {
                                    ajaxLogout(e.responseText,null,"保存错误，请重试!");
                                }
                            });
                            return false;
                        },function(index){//cancel
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

		function closethiswindow(){
			top.layer.close(this_index);
		}

	</script>
	<style type="text/css">
		.form-horizontal {margin-top:5px;}
	</style>
</head>
<body>
	<form:form id="inputForm" modelAttribute="order" action="${ctx}/sd/order/kefuOrderList/service/followUpFail" method="post" class="form-horizontal">
		<form:hidden path="orderId"/>
		<form:hidden path="quarter"/>
		<%--<input type="hidden" id="createDate" name="createDate" value="" />--%>
		<sys:message content="${message}"/>
		<c:if test="${order.orderId != null && canSave == true}">
		<div class="control-group error">
			<label class="control-label">说明:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="150" style="width:90%" />
			</div>
		</div>
		</c:if>
		<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
			<c:if test="${order.orderId != null && canSave == true }">
				<shiro:hasPermission name="sd:order:return"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethiswindow();" />
		</div>
	</form:form>
</body>
</html>