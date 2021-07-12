<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>质保类型关联</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
            $("#inputForm").validate({
				submitHandler: function(form){
				    var customerCategoryId = $("#customerCategoryId").val();
				    var b2bWarrantyCode = $("#b2bWarrantyCode").val();
				    if((customerCategoryId==null || customerCategoryId=='') && (b2bWarrantyCode==null || b2bWarrantyCode=='' || b2bWarrantyCode.trim().length==0)){
                        layerMsg('数据源料号和outerIdSKU不能同时为空');
                        return false
					}
					var cancelFlag = $("input[name='autoCancelFlag']:checked").val();
				    var transferFlag = $("input[name='autoTransferFlag']:checked").val();
				    if(cancelFlag==1 && transferFlag==1){
                        layerMsg('不能同时为自动取消,自动转单');
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

            $(document).on('change','#customerId',function(e){
                var customerId =$(this).val();
                var dataSource = $("#dataSource").val();
                if(dataSource ==null || dataSource ==''){
                    layerMsg('请先选择数据源！');
                    return false;
                }
                if (customerId == "")
                {
                    $("#shopId").empty();
                    $("#shopId").html('<option value="" selected>请选择</option>');
                    $("#shopId").val("");
                    $("#shopId").change();
                    return false;
                }
                $.ajax({
                        url:"${ctx}/b2bcenter/md/customer/getShopList?customerId="+customerId +"&dataSource=" + dataSource,
                        success:function (e) {
                            if(e.success){
                                $("#shopId").empty();
                                var shopId_sel=[];
                                shopId_sel.push('<option value="" selected="selected">请选择</option>')
                                for(var i=0,len=e.data.length;i<len;i++){
                                    var customerMapping = e.data[i];
                                    shopId_sel.push('<option value="'+customerMapping.shopId+'">'+customerMapping.shopName+'</option>')
                                }
                                $("#shopId").append(shopId_sel.join(' '));
                                $("#shopId").val("");
                                $("#shopId").change();
                            }else {
                                $("#shopId").html('<option value="" selected>请选择</option>');
                                $("#shopId").change();
                            }
                        },
                        error:function (e) {
                            layerError("请求客户产品失败","错误提示");
                        }
                    }
                );
            });

            $(document).on('change',"#dataSource",function (e){
                $("#shopId").empty();
                $("#shopId").html('<option value="" selected>请选择</option>');
                $("#shopId").val("");
                $("#shopId").change();
                $("#customerId").val("");
                $("#customerId").change();
                getCustomerListByDataSource();
                getCustomerCategoryByDataSource();
			});
            getCustomerCategoryByDataSource();
            var id=$("#id").val();
            if(id==null || id==''){ //如果是添加
                //页面加载获取第一个数据源下客户列表
                getCustomerListByDataSource();
            }
		});


		//根据数据获取类目
		function getCustomerCategoryByDataSource() {
			var dataSource = $("#dataSource").val();
            $.ajax({
                    url:"${ctx}/b2bcenter/md/customerCategory/ajax/getListByDataSource?dataSource=" + dataSource,
                    success:function (e) {
                        if(e.success ==true){
                            $("#customerCategoryId").empty();
                            var shopId_sel=[];
                            shopId_sel.push('<option value="" selected="selected">请选择</option>')
                            var customerCategoryValue = $("#customerCategoryValue").val();
                            for(var i=0,len=e.data.length;i<len;i++){
                                var customerCategory = e.data[i];
                                if(customerCategoryValue !=null && customerCategoryValue!=''){
                                    if(customerCategoryValue == customerCategory.customerCategoryId){
                                        shopId_sel.push('<option value="'+customerCategory.customerCategoryId +'" + selected="selected">'+customerCategory.customerCategoryId+'</option>')
									}else{
                                        shopId_sel.push('<option value="'+customerCategory.customerCategoryId+'">'+customerCategory.customerCategoryId+'</option>')
									}
								}else {
                                    shopId_sel.push('<option value="'+customerCategory.customerCategoryId+'">'+customerCategory.customerCategoryId+'</option>')
								}
                            }
                            $("#customerCategoryId").append(shopId_sel.join(' '));
                            $("#customerCategoryId").change();
                        }else if(e.success ==false){
                            $("#customerCategoryId").html('<option value="" selected>请选择</option>');
                            $("#customerCategoryId").change();
                            layerAlert(e.message,"提示");
                        }
                    },
                    error:function (e) {
                        ajaxLogout(e.responseText,null,"数据源料号失败","错误提示！");
                    }
                }
            );
        }

		//根据数据源获取客户
		function getCustomerListByDataSource() {
            var dataSource = $("#dataSource").val();
            if(dataSource==null || dataSource==''){
                return false;
            }
            $.ajax({
                url:"${ctx}/b2bcenter/md/customer/ajax/getCustomerListByDataSource?dataSource="+dataSource,
                success:function (e){
                    if(e.success){
                        $("#customerId").empty();
                        var customer_sel=[];
                        customer_sel.push('<option value="" selected="selected">请选择</option>')
                        for(var i=0,len=e.data.length;i<len;i++){
                            var programme = e.data[i];
                            customer_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                        }
                        $("#customerId").append(customer_sel.join(' '));
                        $("#customerId").val("");
                        $("#customerId").change();
                    }else{
                        layerError("获取客户错误:" + e.message, "错误提示");
                        $("#customerId").html('<option value="" selected>请选择</option>');
                        $("#customerId").change();
                    }
                },
                error:function (e) {
                    layerError("获取客户信息错误","错误提示");
                    $("#customerId").html('<option value="" selected>请选择</option>');
                    $("#customerId").change();
                }
            });
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
		<li><a href="${ctx}/b2bcenter/md/warranty/getList">列表</a></li>
		<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:b2bwarranty:edit">${not empty b2BCustomerMapping.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:b2bwarranty:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<input type="hidden" id="customerCategoryValue" value="${b2bWarrantyMapping.customerCategoryId}"/>
	<form:form id="inputForm" modelAttribute="b2bWarrantyMapping" action="${ctx}/b2bcenter/md/warranty/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">数据源:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${b2bWarrantyMapping.dataSource >0}">
						<form:hidden path="dataSource"></form:hidden>
						<input class="fromInput" readonly="true" value="${fns:getDictLabelFromMS(b2bWarrantyMapping.dataSource, 'order_data_source','Unknow' )}"></input>
					</c:when>
					<c:otherwise>
						<form:select disabled="${b2bWarrantyMapping.dataSource > 0?'true':'false'}" path="dataSource" cssClass="required input-medium" cssStyle="width: 220px;">
							<form:options items="${fns:getDictExceptListFromMS('order_data_source',1)}"
										  itemLabel="label" itemValue="value" htmlEscape="false" />
						</form:select>
					</c:otherwise>
				</c:choose>
				<span class="add-on red">*</span>
			</div>
		</div>

		<%--<div class="control-group">
			<label class="control-label">数据源:</label>
			<div class="controls">
				<form:hidden path="dataSource" value="2"></form:hidden>
				<input class="fromInput" readonly="true" value="天猫"></input>
				<span class="add-on red">*</span>
			</div>
		</div>--%>

		<div class="control-group">
			<label class="control-label">客户:</label>
			<div class="controls" >
				<c:choose>
					<c:when test="${b2bWarrantyMapping.customerId > 0}">
						<form:hidden path="customerId"></form:hidden>
						<form:input path="customerName" readonly="true"></form:input>
					</c:when>
					<c:otherwise>
						<%--<form:select path="customerId" cssClass="input-small required" cssStyle="width:225px;">
							<form:option value="" label="请选择"/>
							<form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
						</form:select>--%>
						<select id="customerId" name="customerId" style="width:225px;" class="required">
							<option value="">请选择</option>
						</select>
					</c:otherwise>
				</c:choose>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">店铺名称:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${b2bWarrantyMapping.shopId !=null && b2bWarrantyMapping.shopId!=''}">
						<form:hidden path="shopId"></form:hidden>
						<input class="fromInput required" readonly="true" value="${b2bWarrantyMapping.shopName}" />
					</c:when>
					<c:otherwise>
						<select id="shopId" name="shopId" style="width:225px;" class="required">
							<option value="" selected>请选择</option>
						</select>
					</c:otherwise>
				</c:choose>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">outerIdSKU:</label>
			<div class="controls">
				<form:input path="b2bWarrantyCode" htmlEscape="false" minLength="2" maxlength="28"/>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">数据源料号:</label>
			<div class="controls">
				<select id="customerCategoryId" name="customerCategoryId" style="width:225px;" >
					<option value="" selected>请选择</option>
				</select>
				<span>(产品类目,数据源产品ID)</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">质保类型:</label>
			<div class="controls">
				<%--<form:input path="warrantyType" htmlEscape="false" minLength="2" maxlength="28" class="required"/>--%>
                <select  name="warrantyType" style="width:225px;" class="required">
                    <c:choose>
                        <c:when test="${b2bWarrantyMapping.warrantyType eq '保内'}">
                            <option value="保内" selected>保内</option>
                            <option value="保外">保外</option>
                        </c:when>
                         <c:otherwise>
                             <option value="保内">保内</option>
                             <option value="保外" selected>保外</option>
                         </c:otherwise>
                    </c:choose>
                </select>
				<span class="add-on red">*</span>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">是否自动取消:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${b2bWarrantyMapping.autoCancelFlag==1}">
						<form:radiobutton path="autoCancelFlag" value="0"  label="否"/>
						<form:radiobutton path="autoCancelFlag" value="1" checked="checked" label="是"/>
					</c:when>
					<c:otherwise>
						<form:radiobutton path="autoCancelFlag" value="0"  checked="checked" label="否"/>
						<form:radiobutton path="autoCancelFlag" value="1"  label="是"/>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">是否自动转单:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${b2bWarrantyMapping.autoTransferFlag==1}">
						<form:radiobutton path="autoTransferFlag" value="0"  label="否"/>
						<form:radiobutton path="autoTransferFlag" value="1" checked="checked" label="是"/>
					</c:when>
					<c:otherwise>
						<form:radiobutton path="autoTransferFlag" value="0"  checked="checked" label="否"/>
						<form:radiobutton path="autoTransferFlag" value="1"  label="是"/>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge" cssStyle="min-width: 260px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:b2bwarranty:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>
