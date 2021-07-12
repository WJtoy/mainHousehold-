<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>条码</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
                    var $btnSubmit =  $("#btnSubmit");
                    top.layer.confirm(
                        '确定要反馈吗？'
                        ,{icon: 3,closeBtn: 0,title:'系统确认',success: function(layro, index) {
                            $(document).on('keydown', layro, function(e) {
                                if (e.keyCode == 13) {
                                    layro.find('a.layui-layer-btn0').trigger('click')
                                }else if(e.keyCode == 27){//esc
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
                            var loadingIndex;
                            var ajaxSuccess = 0;
                            $.ajax({
                                async: false,
                                cache: false,
                                type: "POST",
                                url: "${ctx}/sd/order/serviceMonitor/updateFeedback",
                                data: $(form).serialize(),
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
										if(parentIndex && parentIndex != undefined && parentIndex != ''){
										    //刷新订单详情页面
                                            var layero = $("#layui-layer" + parentIndex, top.document);
                                            var iframeWin = top[layero.find('iframe')[0]['name']];
                                            iframeWin.reload('tabTmallMonitor');
										}else{
										    //刷新预警列表
                                            var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                                            if (iframe != undefined) {
                                                $("#repageFlag", iframe.document).val("true");
                                            }
										}
                                        closethiswindow();
                                        //layerMsg("加急修改成功！");//此行脚本在ie内核浏览器下错误
                                    }
                                    else if( data && data.message){
                                        layerError(data.message,"错误提示");
                                    }
                                    else{
                                        layerError("天猫预警反馈失败","错误提示");
                                    }
                                    return false;
                                },
                                error: function (e) {
                                    ajaxLogout(e.responseText,null,"天猫预警反馈，请重试!");
                                }
                            });
                            return false;
                        },function(index){//cancel
                            $btnSubmit.removeAttr('disabled');
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
		.form-horizontal{margin-top:20px;}
	</style>
</head>
<body>
<form:form id="inputForm" modelAttribute="entity" method="post" class="form-horizontal">
<input type="hidden" name="monitorId" id="monitorId" value="${entity.monitorId}">
<input type="hidden" name="id" id="id" value="${entity.id}">
<div class="control-group">
	<label class="control-label">反馈:</label>
	<div class="controls">
		<form:textarea path="replyContent" cssClass="required" htmlEscape="false" rows="3" maxlength="225" style="width:90%" />
	</div>
</div>
<div class="form-actions">
	<shiro:hasPermission name="sd:servicemonitor:feedback"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
	<input id="btnCancel" class="btn" type="button" value="返 回" onclick="closethiswindow();" />
</div>
</form:form>
</body>
</html>