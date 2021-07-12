<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品分类</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
            $("#inputForm").validate({
                rules: {
                    name: {remote: {
                            url: "${ctx}/provider/md/productType/checkName",
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
                        url:"${ctx}/provider/md/productType/save",
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
		});
	</script>
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
		  .form-horizontal {margin-top: 5px;}
		  .form-horizontal .control-label {width: 80px;}
		  .form-horizontal .controls {margin-left: 90px;}
	  </style>
  </head>
  
  <body>
	<form:form id="inputForm" modelAttribute="productTypeDto" action="${ctx}/provider/md/productType/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group" style="margin-top: 10px">
			<label class="control-label">品类:</label>
				<div class="controls">
					<select id="productCategoryId" name="productCategoryId" class="input-small required selectCustomer" style="width:225px;">
						<option value=""
								<c:out value="${(empty productTypeDto.productCategoryId)?'selected=selected':''}" />>请选择</option>
						<c:forEach items="${fns:getProductCategories()}" var="productCategorie">
							<option value="${productCategorie.id}"
									<c:out value="${(productTypeDto.productCategoryId eq productCategorie.id)?'selected=selected':''}" />>${productCategorie.name}</option>
						</c:forEach>
					</select>
				<span class="add-on red">*</span>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">分类(一级):</label>
			<div class="controls">
				<input class="fromInput required" style="width: 500px" name="name" value="${productTypeDto.name}" htmlEscape="false" maxlength="45"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">分类(二级):</label>
			<div class="controls">
				<form:textarea path="itemNames" htmlEscape="false" rows="5" maxlength="1000" class="input-xlarge required" cssStyle="width: 500px;height: 100px"/><br/>
				<span style="font-size: 9px;">输入分类名称,用逗号隔开</span>
			</div>
		</div>
	</form:form>
	<hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
	<shiro:hasPermission name="md:producttype:edit">
		<div style="background: white;height: 50px;position: absolute;bottom: 5px;width: 100%">
			<input id="btnSubmit" class="btn btn-primary" type="submit" style="margin-left: 570px;margin-top:10px" onclick="$('#inputForm').submit()" value="保 存"/>
		</div>
	</shiro:hasPermission>
  </body>
</html>
