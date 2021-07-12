<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>服务费项目分类</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
            $("#inputForm").validate({
				submitHandler: function(form){
                    var price = $("#price").val()
                    if(price<=0.00){
                        layerMsg('金额要大于0元');
                        return false
                    }
					loading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);
					form.submit();
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
            //加载服务费分类
            getAllCategoryList();

            $("#price").keyup(function () {
                var reg = $(this).val().match(/\d+\.?\d{0,2}/);
                var txt = '';
                if (reg != null) {
                    txt = reg[0];
                }
                $(this).val(txt);
            }).change(function () {
                $(this).keyup();
            });

		});

		//根据数据源获取服务费项目分类
		function getAllCategoryList() {
            $.ajax({
                    url:"${ctx}/provider/md/auxiliaryMaterialCategory/ajax/findAllList",
                    success:function (e) {
                        if(e && e.success == true){
                            $("#categoryId").empty();
                            var programme_sel=[];
                            var hiddenCategoryId = $("#hiddenCategoryId").val();
                            programme_sel.push('<option value="" selected="selected">请选择</option>')
							for(var i=0,len=e.data.length;i<len;i++){
								var programme = e.data[i];
								if(programme.id == hiddenCategoryId){
                                    programme_sel.push('<option value="'+programme.id+'" selected="selected">'+programme.name+'</option>')
                                }else{
                                    programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                                }
							}
                            $("#categoryId").append(programme_sel.join(' '));
                            $("#categoryId").change();
                            $("#hiddenCategoryId").val("");
                        }else if(e.success==false){
                            $("#categoryId").html('<option value="" selected>请选择</option>');
                            $("#categoryId").change();
                            layerError(e.message,"错误提示");
                        }
                    },
                    error:function (e) {
                        ajaxLogout(e.responseText,null,"请求服务费项目分类失败","错误提示！");
                    }
                }
            );
        }

	</script>
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
	  </style>
  </head>
  
  <body>
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/provider/md/auxiliaryMaterialItem/getList">列表</a></li>
		<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:auxiliarymaterialitem:edit">${not empty auxiliaryMaterialItem.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:auxiliarymaterialitem:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
    <input type="hidden" id="hiddenCategoryId" value="${auxiliaryMaterialItem.category.id}">
	<form:form id="inputForm" modelAttribute="auxiliaryMaterialItem" action="${ctx}/provider/md/auxiliaryMaterialItem/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">分类名称:</label>
			<div class="controls">
                <select id="categoryId" name="category.id" style="width:225px;" class="required">
                    <option value="">请选择</option>
                </select>
                <span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">产品:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${auxiliaryMaterialItem.id>0 && auxiliaryMaterialItem.productId>0}">
						<form:hidden path="productId"></form:hidden>
						<input class="fromInput required" readonly="true" value="${auxiliaryMaterialItem.productName}" />
					</c:when>
					<c:otherwise>
						<select id="productId" name="productId" class="input-small required selectCustomer" style="width:225px;" >
							<option value=""
									<c:out value="${(empty auxiliaryMaterialItem.productId)?'selected=selected':''}" />>请选择</option>
							<c:forEach items="${fns:getProducts()}" var="product">
								<option value="${product.id}"
										<c:out value="${(auxiliaryMaterialItem.productId eq product.id)?'selected=selected':''}" />>${product.name}</option>
							</c:forEach>
						</select>
					</c:otherwise>
				</c:choose>
                <span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">项目名称:</label>
			<div class="controls">
				<form:input path="name" htmlEscape="false" minLength="2" maxlength="45" class="required"/>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">价格类别:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${auxiliaryMaterialItem.type==1}">
						<form:radiobutton path="type" value="0"  label="固定价格"/>
						<form:radiobutton path="type" value="1" checked="checked" label="自定义价格"/>
					</c:when>
					<c:otherwise>
						<form:radiobutton path="type" value="0"  checked="checked" label="固定价格"/>
						<form:radiobutton path="type" value="1"  label="自定义价格"/>
					</c:otherwise>
				</c:choose>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">金额:</label>
			<div class="controls">
				<form:input path="price" htmlEscape="false" minLength="0" maxlength="6" class="required" placeholder="0.00"/>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">单位:</label>
			<div class="controls">
				<form:input path="unit" htmlEscape="false" minLength="1" maxlength="5" class="required"/>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="5" maxlength="200" class="input-xlarge" cssStyle="min-width: 260px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:auxiliarymaterialitem:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>
