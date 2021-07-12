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
				/*    var customPriceFlag = $('input[name="customPriceFlag"]:checked ').val();
				    if(customPriceFlag ==0){
                        var charge = $("#charge").val()
                        if(charge<=0.00){
                            layerMsg('金额要大于0元');
                            return false
                        }
					}*/
                    var charge = $("#charge").val()
                    if(charge<=0.00){
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
            getServiceFeeCategory();

            $("#charge").keyup(function () {
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
		function getServiceFeeCategory() {
			var dataSource = $("#dataSource").val();
            if(dataSource ==null || dataSource ==''){
                return false;
            }
            $.ajax({
                    url:"${ctx}/b2bcenter/md/serviceFeeCategory/ajax/getServiceFeeCategory?dataSource=" + dataSource,
                    success:function (e) {
                        if(e && e.success == true){
                            $("#categoryId").empty();
                            var programme_sel=[];
                            programme_sel.push('<option value="" selected="selected">请选择</option>')
							for(var i=0,len=e.data.length;i<len;i++){
								var programme = e.data[i];
								programme_sel.push('<option value="'+programme.id+'">'+programme.categoryName+'</option>')
							}
                            $("#categoryId").append(programme_sel.join(' '));
                            $("#categoryId").val("");
                            $("#categoryId").change();
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
		<li><a href="${ctx}/b2bcenter/md/serviceFeeItem/getList">列表</a></li>
		<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:b2bservicefeeitem:edit">${not empty serviceFeeItem.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:b2bservicefeeitem:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="serviceFeeItem" action="${ctx}/b2bcenter/md/serviceFeeItem/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
        <div class="control-group">
            <label class="control-label">数据源:</label>
            <div class="controls">
                <c:choose>
                    <c:when test="${serviceFeeItem.dataSource >0}">
                        <form:hidden path="dataSource"></form:hidden>
                        <input class="fromInput" readonly="true" value="${fns:getDictLabelFromMS(serviceFeeItem.dataSource, 'order_data_source','Unknow' )}"></input>
                    </c:when>
                    <c:otherwise>
                        <form:select disabled="${serviceFeeItem.dataSource > 0?'true':'false'}" path="dataSource" cssClass="required input-medium" cssStyle="width: 220px;" onclick="getServiceFeeCategory()">
                            <form:options items="${fns:getDictListFromMS('order_data_source')}"
                                          itemLabel="label" itemValue="value" htmlEscape="false" />
                        </form:select>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
		<div class="control-group">
			<label class="control-label">分类名称:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${serviceFeeItem.category.id !=null && serviceFeeItem.category.id>0}">
						<form:hidden path="category.id"></form:hidden>
						<form:input path="category.categoryName" readonly="true"/>
						<span class="add-on red">*</span>
					</c:when>
					<c:otherwise>
						<select id="categoryId" name="category.id" style="width:225px;" class="required">
							<option value="">请选择</option>
						</select>
						<span class="add-on red">*</span>
					</c:otherwise>
				</c:choose>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">产品:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${serviceFeeItem.productId > 0}">
						<form:hidden path="productId"></form:hidden>
						<form:input path="productName" readonly="true"></form:input>
						<span class="add-on red">*</span>
					</c:when>
					<c:otherwise>
						<select id="productId" name="productId" class="input-small required selectCustomer" style="width:225px;">
							<option value=""
									<c:out value="${(empty serviceFeeItem.productId)?'selected=selected':''}" />>请选择</option>
							<c:forEach items="${fns:getProducts()}" var="product">
								<option value="${product.id}"
										<c:out value="${(serviceFeeItem.productId eq product.id)?'selected=selected':''}" />>${product.name}</option>
							</c:forEach>
						</select>
						<span class="add-on red">*</span>
					</c:otherwise>
				</c:choose>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">项目名称:</label>
			<div class="controls">
				<form:input path="itemName" htmlEscape="false" minLength="2" maxlength="45" class="required"/>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">价格类别:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${serviceFeeItem.customPriceFlag==1}">
						<form:radiobutton path="customPriceFlag" value="0"  label="固定价格"/>
						<form:radiobutton path="customPriceFlag" value="1" checked="checked" label="自定义价格"/>
					</c:when>
					<c:otherwise>
						<form:radiobutton path="customPriceFlag" value="0"  checked="checked" label="固定价格"/>
						<form:radiobutton path="customPriceFlag" value="1"  label="自定义价格"/>
					</c:otherwise>
				</c:choose>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">金额:</label>
			<div class="controls">
				<form:input path="charge" htmlEscape="false" minLength="0" maxlength="6" class="required" placeholder="0.00"/>
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
			<shiro:hasPermission name="md:b2bservicefeeitem:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>
