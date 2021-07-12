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
				    var unitBarcode = $("#unitBarcode").val();
                    var regEn = /[`~!@#$%^&*()_+<>?:"{},\\.;'[\]]/im,
                        regCn = /[·！#￥（——）：；“”‘、，|《。》？、【】[\]]/im;
                    if(regEn.test(unitBarcode) || regCn.test(unitBarcode)) {
                        layerError("不能包含特殊字符","错误提示");
                        return false;
                    }
                    var $btnSubmit =  $("#btnSubmit");
                    top.layer.confirm(
                        '确定要修改产品条码吗？'
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
                            var productIndex = $("#productIndex").val();
                            $.ajax({
                                async: false,
                                cache: false,
                                type: "POST",
                                url: "${ctx}/sd/orderItemComplete/updateBarcode",
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
                                        if(parentIndex && parentIndex != undefined && parentIndex != '' && productIndex && productIndex != undefined && productIndex != ''){
                                            var layero = $("#layui-layer" + parentIndex,top.document);
                                            var iframeWin = top[layero.find('iframe')[0]['name']];
                                            iframeWin.updateBarCode(data,productIndex);
                                            ajaxSuccess = 1;
                                            closethiswindow();//关闭本窗口
                                        }
                                    }
                                    else if( data && data.message){
                                        layerError(data.message,"错误提示");
                                    }
                                    else{
                                        layerError("修改产品条码失败","错误提示");
                                    }
                                    return false;
                                },
                                error: function (e) {
                                    ajaxLogout(e.responseText,null,"修改产品条码，请重试!");
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
		.form-horizontal{margin-top:5px;}
		.fromInput {
			border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		}
	</style>
</head>
<body>
	<form:form id="inputForm" action="" method="post" class="form-horizontal">
		<input type="hidden" id="productIndex" value="${productIndex}">
		<input type="hidden" name="id" value="${orderItemComplete.id}">
		<input type="hidden" name="itemNo" value = "${dataSource}" />
		<input type="hidden" name="outBarcode" value="${b2bOrderNo}" />
		<sys:message content="${message}"/>
		<div class="control-group" style="margin-top: 10px;">
			<label class="control-label">产品条码:</label>
			<div class="controls">
               <input class="fromInput required" id="unitBarcode" name="unitBarcode" value="${orderItemComplete.unitBarcode}" maxlength="40"/>
			</div>
		</div>
		<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
			<shiro:hasPermission name="sd:order:plan"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethiswindow();" />
		</div>
	</form:form>
</body>
</html>