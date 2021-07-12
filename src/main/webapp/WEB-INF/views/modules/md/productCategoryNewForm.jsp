<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
	<script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
    <title>产品品类</title>
	<meta name="decorator" content="default"/>
	  <style type="text/css">
		  .form-horizontal .controls {
			  margin-left: 138px;
		  }
		  .form-horizontal .control-label {
			  width: 131px;
		  }
	  </style>
	<script type="text/javascript">
		var this_index = top.layer.index;
		$(document).ready(function() {
			$("#value").focus();
			$("#inputForm").validate({
                rules: {
                    code: {remote: "${ctx}/md/productcategory/checkProductCategoryCode?id=" + '${productCategory.id}'},
                    name: {remote: "${ctx}/md/productcategory/checkProductCategoryName?id=" + '${productCategory.id}'}
                },
                messages: {
                    code: {remote: "产品分类编码已存在"},
                    name: {remote: "产品分类名称已存在"}
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
						url:"${ctx}/md/productcategory/save",
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

		// 关闭页面
		function cancel() {
			top.layer.close(this_index);// 关闭本身
		}

		// 开关控制
		function switchControl(obj) {
			var flag = $("." + obj).val();
			if (flag == 0) {
				$("." + obj).attr("value", 1);
			} else {
				$("." + obj).attr("value", 0);
			}
		}
	</script>
  </head>
  
  <body>
	<form:form id="inputForm" modelAttribute="productCategory" action="${ctx}/md/productcategory/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group"style="margin-top: 20px;">
			<label class="control-label"><span class="red">*</span>品类编码：</label>
			<div class="controls">
				<form:input path="code" htmlEscape="false" minLength="2" maxlength="10" class="required"/>
			</div>
		</div>
		<div class="control-group" style="margin-top: 9px;">
			<label class="control-label"><span class="red">*</span>品类名称：</label>
			<div class="controls">
				<form:input path="name" htmlEscape="false" minLength="2" maxlength="20" class="required"/>
			</div>
		</div>
		<div class="control-group" style="margin-top: 9px;">
			<label class="control-label">品类分组：</label>
			<div class="controls">
				<select id="groupCategory" name="groupCategory" style="width:220px;">
				<option value="" <c:out value="${(empty productCategory.groupCategory)?'selected=selected':''}" />>无</option>
				<c:forEach items="${fns:getDictListFromMS('groupCategory')}" var="groupCategoryDitc"><!-- 切换为微服务 -->
				<option value="${groupCategoryDitc.value}" <c:out value="${(productCategory.groupCategory eq groupCategoryDitc.value)?'selected=selected':''}" />>${groupCategoryDitc.label}</option>
				</c:forEach>
				</select>
			</div>
		</div>
		<div class="control-group"style="margin-top: 9px;">
			<label class="control-label"><span class="red">*</span>排&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;序：</label>
			<div class="controls">
				<form:input path="sort" htmlEscape="false" maxlength="100" var="" class="required digits number" placeholder="数字越小越靠前"/>
			</div>
		</div>
		<div class="control-group"style="margin-top: 9px;">
			<label class="control-label">自动客评：</label>
			<div class="controls">
				<c:set value="${productCategory.autoGradeFlag}" var="c"/>
				<span class="<c:out value="${c == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
					  onclick="switchControl('autoGradeFlag')"></span>
				<input type="hidden" value="${c}" class="autoGradeFlag" name="autoGradeFlag">
				<span class="help-inline" style="margin-top: -13px;">通过短信回访,语音回访,APP完工自动完成客评</span>
			</div>
		</div>
		<div class="control-group"style="margin-top: 9px;">
			<label class="control-label">APP完工：</label>
			<div class="controls">
				<c:set value="${productCategory.appCompleteFlag}" var="c"/>
				<span class="<c:out value="${c == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
					  onclick="switchControl('appCompleteFlag')"></span>
				<input type="hidden" value="${c}" class="appCompleteFlag" name="appCompleteFlag">
				<span class="help-inline" style="margin-top: -13px;">APP完成后才能客评</span>
			</div>
		</div>
		<div class="control-group"style="margin-top: 9px;">
			<label class="control-label">描&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;述：</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="249" class="input-xlarge" cssStyle="width: 466px"/>
			</div>
		</div>
		<hr style="=border: 1px solid rgba(238, 238, 238, 1);margin: 0px;margin-top: 43px"/>
		<div>
			<shiro:hasPermission name="md:productcategory:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="width: 92px;height: 40px;margin-left: 69%;margin-top: 10px"/>
				&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="width: 92px;height: 40px;margin-top: 10px;margin-left: 10px"/>
		</div>
	</form:form>
  </body>
</html>
