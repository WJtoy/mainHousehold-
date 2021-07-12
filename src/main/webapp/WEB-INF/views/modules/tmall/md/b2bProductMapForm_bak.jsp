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
		    $("#customerId").change(function (e) {
		        var dataSource =$("#dataSource").val();
                var customerId =$("#customerId").val();
				$.ajax({
						url:"${ctx}/tmall/md/b2bproduct/ajax/shopList?dataSource="+dataSource+"&customerId="+customerId,
                    	success:function (e) {
							if(e.success){
                                $("#shopId").empty();
                                var programme_sel=[];
                                programme_sel.push('<option value="" selected>请选择</option>')
                                for(var i=0,len=e.data.length;i<len;i++){
                                    var programme = e.data[i];
                                    programme_sel.push('<option value="'+programme.shopId+'">'+programme.shopName+'</option>')
                                }
                                $("#shopId").append(programme_sel.join(' '));
                                $("#shopId").val("");
                                $("#shopId").change();

							}else {
                                $("#shopId").html('<option value="" selected>请选择</option>');
                                layerMsg('该客户下没有店铺！');
							}
                        },
                     	error:function (e) {
                            layerError("请求加载客户店铺失败","错误提示");
                        }

					}
				);
                $.ajax({
                        url:"${ctx}/md/product/ajax/customerProductList?customerId="+customerId,
                        success:function (e) {
                            if(e.success){
                                var programme_sel=[];

                                for(var i=0,len=e.data.length;i<len;i++){
                                    var programme = e.data[i];
                                    var ProductHTML="<div class='row-fluid'>"+
														"<div class='span4'>"+
															"<div class='control-group'>" +
																"<label class='control-label'>产品:</label>" +
																"<div class='controls'>" +
																	 "<input type='hidden' name='list["+i+"].productId' class='input-large' value='"+programme.id+"'/>" +
																	 "<input class='fromInput' name='list["+i+"].productName' readonly='readonly' class='input-large' value='"+programme.name+"'/>" +
																"</div>" +
															"</div>"+
														"</div>"+
														"<div class='span4'>"+
															"<div class='control-group'>"+
																"<label class='control-label'>数据源产品ID:</label>" +
																"<div class='controls'>" +
																	"<input class='fromInput' name='list["+i+"].customerCategoryId' class='input-large'/>" +
																"</div>" +
															"</div>"+
														"</div>"+
													"</div>"
                                    programme_sel.push(ProductHTML)
                                }
                                $("#productDiv").html(programme_sel.join(' '));
                            }else {
                                layerMsg('该客户没有产品！');
                            }
                        },
                        error:function (e) {
                            layerError("请求加载客户产品失败","错误提示");
                        }

                    }
                );
            })
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
		});
	</script>
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
	  </style>
  </head>
  
  <body>
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/tmall/md/b2bproduct">列表</a></li>
		<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:b2bproduct:edit">${not empty b2bProductMap.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:b2bproduct:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="b2bProductMap" action="${ctx}/tmall/md/b2bproduct/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">数据源:</label>
			<div class="controls">
				<%--<form:select disabled="${b2bProductMap.dataSource > 0?'true':'false'}" path="dataSource" cssClass="required input-medium" cssStyle="width: 220px;">
					<form:options items="${fns:getDictExceptListFromMS('order_data_source',1)}"
								  itemLabel="label" itemValue="value" htmlEscape="false" />&lt;%&ndash;切换为微服务&ndash;%&gt;
				</form:select>--%>


				<c:choose>
					<c:when test="${b2bProductMap.dataSource >0}">
						<form:hidden path="dataSource"></form:hidden>
						<input class="fromInput required input-xlarge" readonly="true" value="${fns:getDictLabelFromMS(b2bProductMap.dataSource, 'order_data_source','Unknow' )}"></input>
					</c:when>
					<c:otherwise>
						<form:select disabled="${b2bProductMap.dataSource > 0?'true':'false'}" path="dataSource" cssClass="required input-medium" cssStyle="width: 220px;">
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
					<c:when test="${b2bProductMap.customerId >0}">
						<form:hidden path="customerId"></form:hidden>
						<form:input readonly="true" cssClass="required input-xlarge" path="customerName"></form:input>
					</c:when>
					<c:otherwise>
						<form:select path="customerId" cssClass="input-small required" cssStyle="width:220px;">
							<form:option value="" label="请选择"/>
							<form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
						</form:select>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<div id="divCustomer" class="control-group">
			<label class="control-label">数据源客户名称:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${b2bProductMap.shopId != ''}">
						<form:hidden path="shopId"></form:hidden>
						<form:input path="shopName" cssClass="required input-xlarge" readonly="true"></form:input>
					</c:when>
					<c:otherwise>
						<form:select path="shopId" cssClass="input-small required" cssStyle="width:225px;">
							<form:option value="" label="请先选择客户"/>
						</form:select>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<div id="productDiv">
			<c:forEach items="${b2bProductMap.list}" var="b2BProductModel" varStatus="i">
				<div class="row-fluid">
					<div class="span4">
						<div class="control-group">
							<label class="control-label">产品:</label>
							<div class="controls">
								<form:hidden path="list[${i.index}].productId" cssClass='input-large'></form:hidden>
								<form:input  path="list[${i.index}].productName" cssClass='input-large' readonly="true"></form:input>
							</div>
						</div>
					</div>
					<div class="span4">
						<div class="control-group">
							<label class="control-label">数据源产品ID:</label>
							<div class="controls">
								<form:input  path="list[${i.index}].customerCategoryId" cssClass='input-large'></form:input>
							</div>
						</div>
					</div>
				</div>
			</c:forEach>
		</div>
		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="250" class="input-xlarge" cssStyle="min-width: 280px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:b2bproduct:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>
