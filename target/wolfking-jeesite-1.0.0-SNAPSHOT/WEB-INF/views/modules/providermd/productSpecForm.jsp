<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品规格</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
            $("#inputForm").validate({
               rules: {
                    name: {remote: {
                            url: "${ctx}/provider/md/productSpec/checkName",
                            type: "get",
                            data:{
                                id: function() {
                                    return $("#id").val();
                                }
                            }
                        }}
                },
                messages: {
                    name: {remote: "产品分类已存在"}
                },
				submitHandler: function(form){
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);

                    $.ajax({
                        url:"${ctx}/provider/md/productSpec/save",
                        type:"POST",
                        data:$(form).serialize(),
                        dataType:"json",
                        success: function(data){
                            //提交后的回调函数
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg("保存成功");
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                                top.layer.close(this_index);//关闭本身
                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data)
                        {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            //var msg = eval(data);
                        },
                        timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                    });
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
			var id = $("#id").val();
			if(id !=null && id>0){
			    var productTypeItemNames = $("#strTypeItemNames").val();
			    if(productTypeItemNames !=null && productTypeItemNames !=''){
			        var arr = productTypeItemNames.split(",");
                    $('#productTypeItemNames').val(arr).trigger("change");;
				}
			}
		});
	</script>
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
		  .form-horizontal {margin-top: 5px;}
		  .form-horizontal .control-label {width: 40px;}
		  .form-horizontal .controls {margin-left: 50px;}
	  </style>
  </head>
  
  <body>
	<form:form id="inputForm" modelAttribute="productSpecDto" action="" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<input type="hidden" id="strTypeItemNames" value="${productSpecDto.productTypeItemNames}">
		<div class="control-group" style="margin-top: 7px">
			<label class="control-label">规格:</label>
			<div class="controls">
				<input class="fromInput required" name="name" value="${productSpecDto.name}" htmlEscape="false" maxlength="45" style="width: 500px"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">参数:</label>
			<div class="controls">
				<form:textarea path="productSpecItemNames" htmlEscape="false" rows="5" maxlength="1000" class="input-xlarge required" cssStyle="width: 500px"/><br/>
				<span style="font-size: 9px;">输入分类名称,用逗号隔开</span>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">分类:</label>
			<div class="controls">
				<select id="productTypeItemNames" name="productTypeItemNames" class="input-large required" multiple="multiple" style="width: 515px">
					<c:forEach items="${productTypeItems}" var="productTypeItem">
						<option value="${productTypeItem.id}">${productTypeItem.name}</option>
					</c:forEach>
				</select>
			</div>
		</div>
		<%--<hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
		<shiro:hasPermission name="md:productspec:edit"><input id="btnSubmit" class="btn btn-primary" style="margin-left: 500px;margin-top: 15px;" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>--%>
	</form:form>
	<hr style="=border: 1px solid rgba(238, 238, 238, 1);margin: 0px;"/>
	<shiro:hasPermission name="md:producttype:edit">
		<input id="btnSubmit" class="btn btn-primary" type="submit" style="margin-left: 530px;margin-top:10px" onclick="$('#inputForm').submit()" value="保 存"/>
	</shiro:hasPermission>
  </body>
</html>
