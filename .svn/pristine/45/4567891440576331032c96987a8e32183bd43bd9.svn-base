<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>普通区域VIP等级</title>
	<meta name="decorator" content="default"/>
	  <style type="text/css">
		  .form-horizontal .controls {
			  margin-left: 138px;
		  }
		  .form-horizontal .control-label {
			  width: 131px;
		  }

		  .layui-layer-iframe15{
			  height: 232px;
		  }

	  </style>
	<script type="text/javascript">
		var this_index = top.layer.index;
		$(document).ready(function() {
			$("#value").focus();
			$("#inputForm").validate({
				submitHandler: function(form){
					var loadingIndex = layerLoading('正在提交，请稍候...');
					var $btnSubmit = $("#btnSubmit");
					if ($btnSubmit.prop("disabled") == true) {
						event.preventDefault();
						return false;
					}

					$btnSubmit.prop("disabled", true);
                    var id = $("#name").val();
					$.ajax({
						url:"${ctx}/provider/md/regionPermissionNew/saveVipLevels?id="+id,
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
								top.layer.close(this_index);//关闭本身
								var pframe = getActiveTabIframe();//定义在jeesite.min.js中
								 if(pframe){
									pframe.repage();
								 }
							}else{
								setTimeout(function () {
									clickTag = 0;
									$btnSubmit.removeAttr('disabled');
								}, 2000);
								top.layer.close(loadingIndex);
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
							top.layer.close(loadingIndex);
							ajaxLogout(data,null,"数据保存错误，请重试!");
							//var msg = eval(data);
						},
						timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
					});
				},
			});

			$("#name").change(function(){
				var options=$("#name option:selected");
				var test = options.text();
				var vals = options.data("value");
				if(vals == 0){
					$("#vipc").text("所有VIP客户区域由KA处理。");
					$("#vipc").show();
					$("#vipd").hide();
					$("#vipe").hide();
				}else {
					$("#vipa").text(test+"以下(不含"+test+")");
					$("#vipb").text(test+"以上(含"+test+")");
					$("#vipc").hide();
					$("#vipd").show();
					$("#vipe").show();
				}
			});
		});

		// 关闭页面
		function cancel() {
			top.layer.close(this_index);// 关闭本身
		}
	</script>
  </head>
  
  <body>
	<form:form id="inputForm" action="${ctx}/provider/md/regionPermissionNew/form" method="post" class="form-horizontal">
<%--		<form:hidden path="id"/>--%>
		<sys:message content="${message}"/>
		<div style="margin-top: 28px;">
			<label class="control-label">VIP等级：</label>
			<div class="controls">
				<select id="name" name="name" style="width:220px;">
				  <c:forEach items="${list}" var="entity">
					  <c:choose>
						  <c:when test="${entity.id == mdCustomerVipLevels.id}">
							  <option value="${entity.id}" selected data-value=${entity.value}>${entity.name}</option>
						  </c:when>
						  <c:otherwise>
						    <option value="${entity.id}" data-value=${entity.value}>${entity.name}</option>
						  </c:otherwise>
					  </c:choose>
				  </c:forEach>
				</select>
			</div>
		</div>




	   <div style="margin-top:24px;margin-left: 61px;height: 42px">
		   <c:choose>
			   <c:when test="${mdCustomerVipLevels.value == 0}">
				   <span id="vipc">所有VIP客户区域由KA处理。</span>
				   <span id="vipd" style="display: none">客户VIP等级 <span style="color:red;" id="vipa">${mdCustomerVipLevels.name}以下(不含${mdCustomerVipLevels.name})</span> 的自动区域由自动客服部处理，</span><br/>
			       <span id="vipe" style="display: none">客户VIP等级 <span style="color:red;" id="vipb">${mdCustomerVipLevels.name}以上(含${mdCustomerVipLevels.name})</span> 的所有区域由KA处理。 <span>
			   </c:when>
			   <c:otherwise>
				   <span id="vipc" style="display: none">所有VIP客户区域由KA处理。</span>
				   <span id="vipd">客户VIP等级 <span style="color:red;" id="vipa">${mdCustomerVipLevels.name}以下(不含${mdCustomerVipLevels.name})</span> 的自动区域由自动客服部处理，</span><br/>
			       <span id="vipe">客户VIP等级 <span style="color:red;" id="vipb">${mdCustomerVipLevels.name}以上(含${mdCustomerVipLevels.name})</span> 的所有区域由KA处理。 <span>
			   </c:otherwise>
		   </c:choose>

	   </div>

		<hr style="=border: 1px solid rgba(238, 238, 238, 1);margin: 0px;margin-top: 43px"/>
		<div style="float: right;margin-top: 10px;margin-right: 20px">
			<shiro:hasPermission name="md:regionPermission:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="width: 92px;height: 40px;margin-top: 10px"/>
			</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="width: 92px;height: 40px;margin-top: 10px;margin-left: 10px"/>
		</div>
	</form:form>
  </body>
</html>
