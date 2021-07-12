<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>b2b系统客户和产品配置</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
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
                    return false;
                }
                $.ajax({
                        url:"${ctx}/b2bcenter/md/product/ajax/getByCustomerId?customerId="+customerId +"&dataSource=" + dataSource,
                        success:function (e) {
                            if(e.success){
                                $("#productId").empty();
                                $("#shopId").empty();
                                var programme_sel=[];
                                var shopId_sel=[];
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
                                shopId_sel.push('<option value="" selected="selected">请选择</option>')
                                if(typeof(e.data.productList)!="undefined"){
                                    for(var i=0,len=e.data.productList.length;i<len;i++){
                                        var programme = e.data.productList[i];
                                        programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                                    }
                                }
                                $("#productId").append(programme_sel.join(' '));
                                $("#productId").val("");
                                $("#productId").change();

                                if(typeof(e.data.customerMappingList)!="undefined"){
                                    for(var i=0,len=e.data.customerMappingList.length;i<len;i++){
                                        var customerMapping = e.data.customerMappingList[i];
                                        shopId_sel.push('<option value="'+customerMapping.shopId+'">'+customerMapping.shopName+'</option>')
                                    }
                                }

                                $("#shopId").append(shopId_sel.join(' '));
                                $("#shopId").val("");
                                $("#shopId").change();


                            }else {
                                $("#productId").html('<option value="" selected>请选择</option>');
                                $("#productId").change();

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

            //根据数据源帅选客户
            $(document).on('change','#dataSource',function(e){
                var dataSource =$(this).val();
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
               //$("#customerId").change();
			});

			var id=$("#id").val();
			if(id==null || id==''){ //如果是添加
                //页面加载获取第一个数据源下客户列表
                $("#dataSource").change();
			}
            $(document).on('change','#productId',function(e){
                var customerId = $("#customerId").val();
                var productId = $(this).val();
                getModeleByProduct(customerId,productId);
			});

            //修改
            if(${productMapping.id !=null && productMapping.id >0}){
                var customerId = $("#customerId").val();
                var productId = $("#productId").val();
                var productCode = $("#codeValue").val();
                if(customerId !=null && customerId> 0 && productId!=null && productId>0){
                    $.ajax({
                        url:"${ctx}/b2bcenter/md/product/ajax/getModelList?customerId="+customerId + "&productId=" + productId,
                        success:function (e) {
                            if(e && e.success == true){
                                if(e.data != null && e.data.length>0){
                                    $("#selectCustomerProductModel").empty();
                                    var programme_sel=[];
                                    programme_sel.push('<option value="" selected="selected">请选择</option>')
                                    for(var i=0,len=e.data.length;i<len;i++){
                                        var programme = e.data[i];
                                        if(programme == productCode){
                                            programme_sel.push('<option value="'+programme+'" selected="selected">'+programme+'</option>')
										}else{
                                            programme_sel.push('<option value="'+programme+'">'+programme+'</option>')
										}
                                    }

                                    $("#selectCustomerProductModel").append(programme_sel.join(' '));
                                    $("#selectCustomerProductModel").change();
                                    $("#selectDiv").show();
                                    $("#inputDiv").hide();
                                    $("#inputCustomerProductModel").attr('name','');
                                    $("#selectCustomerProductModel").attr('name','customerProductModel');
                                }else{
                                    $("#selectDiv").hide();
                                    $("#inputDiv").show();
                                    $("#inputCustomerProductModel").attr('name','customerProductModel');
                                    $("#selectCustomerProductModel").attr('name','');
                                }
                            }else if(e.success == false){
                                $("#selectDiv").hide();
                                $("#inputDiv").show();
                                $("#inputCustomerProductModel").attr('name','customerProductModel');
                                $("#selectCustomerProductModel").attr('name','');
                            }
                        },
                        error:function (e) {
                            $("#selectDiv").hide();
                            $("#inputDiv").show();
                            $("#inputCustomerProductModel").attr('name','productCode');
                            $("#selectCustomerProductModel").attr('name','');
                        }
                    });
				}
			}
		});

        function categoryIdAdd(){
            var html="<input name='customerCategoryId' class='fromInput' maxlength=\"25\">"
            $("#imgAdd").before(html);
		}

		//根据客户和产品获取型号
		function getModeleByProduct(customerId,productId) {
			if((customerId!=null && customerId != '') && (productId!=null && productId != '')){
                $.ajax({
                    url:"${ctx}/b2bcenter/md/product/ajax/getModelList?customerId="+customerId + "&productId=" + productId,
                    success:function (e) {
                        if(e && e.success == true){
                            if(e.data != null && e.data.length>0){
                                $("#selectCustomerProductModel").empty();
                                var programme_sel=[];
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
                                for(var i=0,len=e.data.length;i<len;i++){
                                    var programme = e.data[i];
                                    programme_sel.push('<option value="'+programme+'">'+programme+'</option>')
                                }

                                $("#selectCustomerProductModel").append(programme_sel.join(' '));
                                $("#selectCustomerProductModel").val("");
                                $("#selectCustomerProductModel").change();
                                $("#selectDiv").show();
								$("#inputDiv").hide();
								$("#inputCustomerProductModel").attr('name','');
								$("#selectCustomerProductModel").attr('name','customerProductModel');
							}else{
                                $("#selectDiv").hide();
                                $("#inputDiv").show();
                                $("#inputCustomerProductModel").attr('name','customerProductModel');
                                $("#selectCustomerProductModel").attr('name','');
							}
                        }else if(e.success == false){
                            $("#selectDiv").hide();
                            $("#inputDiv").show();
                            $("#inputCustomerProductModel").attr('name','customerProductModel');
                            $("#selectCustomerProductModel").attr('name','');
                        }
                    },
                    error:function (e) {
                        $("#selectDiv").hide();
                        $("#inputDiv").show();
                        $("#inputCustomerProductModel").attr('name','customerProductModel');
                        $("#selectCustomerProductModel").attr('name','');
                    }
                });
			}
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
		<li><a href="${ctx}/b2bcenter/md/product/getList">列表</a></li>
		<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:b2bproduct:edit">${not empty productMapping.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:b2bproduct:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="productMapping" action="${ctx}/b2bcenter/md/product/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<input type="hidden" id= "codeValue" value="${productMapping.customerProductModel}">
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">数据源:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${productMapping.dataSource >0}">
						<form:hidden path="dataSource"></form:hidden>
						<input class="fromInput required" readonly="true" value="${fns:getDictLabelFromMS(productMapping.dataSource, 'order_data_source','Unknow' )}"></input>
					</c:when>
					<c:otherwise>
						<form:select disabled="${productMapping.dataSource > 0?'true':'false'}" path="dataSource" cssClass="required input-medium" cssStyle="width: 220px;">
						  <form:options items="${fns:getDictExceptListFromMS('order_data_source',1)}"
									  itemLabel="label" itemValue="value" htmlEscape="false" />
						  </form:select>
					</c:otherwise>
				</c:choose>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">客户:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${productMapping.customerId > 0}">
                        <form:hidden path="customerId"></form:hidden>
						<%--<input type="hidden" name="customerId" value="${productMapping.customerId}" />--%>
						<form:input path="customerName" readonly="true"></form:input>
					</c:when>
					<c:otherwise>
						<%--<form:select path="customerId" cssStyle="width: 220px;" cssClass="required">
							<form:option value="" label="请选择"></form:option>
							<form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id"></form:options>
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
					<c:when test="${productMapping.shopId !=null && productMapping.shopId!=''}">
						<%--<form:hidden path="shopId"></form:hidden>--%>
						<input type="hidden" name="shopId" value="${productMapping.shopId}">
						<input class="fromInput required" readonly="true" value="${productMapping.shopName}" />
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
			<label class="control-label">工单产品:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${productMapping.productId >0}">
						<form:hidden path="productId"></form:hidden>
						<input class="fromInput required" readonly="true" value="${productMapping.productName}" />
					</c:when>
					<c:otherwise>
						<%--<form:select path="productId" cssClass="input-small required" cssStyle="width: 220px;">
							<form:option value="" label="请选择"/>
							<form:options items="${fns:getProducts()}"
										  itemLabel="name" itemValue="id" htmlEscape="false" />
						</form:select>--%>
						<select id="productId" name="productId" style="width:225px;" class="required">
							<option value="" selected>请选择</option>
						</select>
					</c:otherwise>
				</c:choose>
				<span class="add-on red">*</span>
			</div>
		</div>
		<div id="productDiv">
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">数据源料号:</label>
						<div class="controls">
							<input name="customerCategoryId" value="${productMapping.customerCategoryId}" class='fromInput required' maxlength="100" placeholder="数据源产品ID,必填"/>
							<span class="add-on red">*</span>
							<span>(产品类目,数据源产品ID)</span>
						</div>
					</div>
				</div>
			</div>
		</div>
		<%--<div id="productDiv">
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">客户产品型号:</label>
						<div class="controls">
							<input name="productCode" value="${productMapping.productCode}" class='fromInput' maxlength="30" placeholder="客户产品型号，选填"/>
							<span>(产品型号)</span>
						</div>
					</div>
				</div>
			</div>
		</div>--%>
		<div class="control-group">
			<label class="control-label">数据源产品型号:</label>
			<div class="controls">
				<input name="productCode" value="${productMapping.productCode}" class='fromInput' maxlength="30" placeholder="数据源产品型号，选填"/>
			</div>
		</div>
		<%--<div class="control-group" id = "selectDiv">
			<label class="control-label">数据源产品型号:</label>
			<div class="controls">
				<select id = "selectProductCode" name="" style="width:225px;">
					<option value="" selected>请选择</option>
				</select>
			</div>
		</div>--%>
		<div class="control-group">
			<label class="control-label">客户料号:</label>
			<div class="controls">
				<input id = "customerCode" name="customerCode" value="${productMapping.customerCode}" class='fromInput' maxlength="30" placeholder="客户料号，选填"/>
			</div>
		</div>

		<div class="control-group" id ="inputDiv">
			<label class="control-label">客户产品型号:</label>
			<div class="controls">
				<input id ="inputCustomerProductModel" name="customerProductModel" value="${productMapping.customerProductModel}" class='fromInput' maxlength="50" placeholder="客户产品型号，选填"/>
			</div>
		</div>

		<div class="control-group" id = "selectDiv" style="display: none">
			<label class="control-label">客户产品型号:</label>
			<div class="controls">
				<select id = "selectCustomerProductModel" name="" style="width:225px;">
					<option value="" selected>请选择</option>
				</select>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge" cssStyle="min-width: 280px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:b2bproduct:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>
