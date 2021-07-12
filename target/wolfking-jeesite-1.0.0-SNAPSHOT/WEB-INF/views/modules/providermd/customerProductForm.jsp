<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户产品</title>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
	<script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
            $("#inputForm").validate({
				submitHandler: function(form){
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);

                    $.ajax({
                        url:"${ctx}/provider/md/customerProduct/save",
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
            if(id!=null && id>0){
                $("#btnSubmit").css({'margin-left':'580px','margin-top':'10px'});
			}else{
                $("#btnSubmit").css({'margin-left':'650px','margin-top':'10px'});
			}
		});

		//取消
		function cancel() {
            top.layer.close(this_index);//关闭本身
        }

        //删除
		function del(id,customerId,productId) {
            layer.confirm('确定要删除吗？', {
                btn: ['确定','取消'] //按钮
            }, function(){
                var loadingIndex = layerLoading('正在提交，请稍候...');
                var $btnDelete = $("#btnDelete");
                if ($btnDelete.prop("disabled") == true) {
                    event.preventDefault();
                    return false;
                }
                $btnDelete.prop("disabled", true);
                $.ajax({
                    url:"${ctx}/provider/md/customerProduct/ajax/remove?id=" + id +"&customer.id="+customerId+"&product.id="+productId,
                    type:"POST",
                    dataType:"json",
                    success: function(data){
                        //提交后的回调函数
                        if(loadingIndex) {
                            top.layer.close(loadingIndex);
                        }
                        if(ajaxLogout(data)){
                            setTimeout(function () {
                                $btnDelete.removeAttr('disabled');
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
                                $btnDelete.removeAttr('disabled');
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
                            $btnDelete.removeAttr('disabled');
                        }, 2000);
                        ajaxLogout(data,null,"数据删除错误，请重试!");
                        //var msg = eval(data);
                    },
                    timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                });
            }, function(){
            });
        }

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
	<form:form id="inputForm" modelAttribute="customerProduct" action="" method="post" class="form-horizontal">
		<c:set var="cuser" value="${fns:getUser()}" />
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="row-fluid" style="margin-top: 7px">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">客户:</label>
					<div class="controls">
						<c:choose>
							<c:when test="${customerProduct.customerDto.id> 0}">
								<input type="hidden" name="customer.id" value="${customerProduct.customerDto.id}">
								<%--<form:hidden id="customer.id" path="customerDto.id"></form:hidden>--%>
								<form:input id = "customer.name" path="customerDto.name" readonly="true" cssStyle="width: 250px"></form:input>
							</c:when>
							<c:otherwise>
								<select id="customerId" name="customer.id" class="input-small required selectCustomer" style="width:250px;">
									<option value=""
											<c:out value="${(empty customerProduct.customerDto.id)?'selected=selected':''}" />>请选择</option>
									<c:forEach items="${fns:getMyCustomerListFromMS()}" var="customer">
										<option value="${customer.id}"
												<c:out value="${(customerProductModel.customerDto.id eq customer.id)?'selected=selected':''}" />>${customer.name}</option>
									</c:forEach>
								</select>
							</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">产品:</label>
					<div class="controls">
						<c:choose>
							<c:when test="${customerProduct.productDto.id > 0}">
								<input type="hidden" name="product.id" value="${customerProduct.productDto.id}">
								<%--<form:hidden id="product.id" path="productDto.id"></form:hidden>--%>
								<form:input path="productDto.name" readonly="true" cssStyle="width: 240px"></form:input>
							</c:when>
							<c:otherwise>
								<select id="productId" name ="product.id" style="width:250px;" class="required">
									<option value=""
											<c:out value="${(empty customerProduct.productDto.id)?'selected=selected':''}" />>请选择</option>
									<c:forEach items="${fns:getProducts()}" var="product">
										<option value="${product.id}"
												<c:out value="${(customerProductModel.productDto.id eq product.id)?'selected=selected':''}" />>${product.name}</option>
									</c:forEach>
								</select>
							</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">安装规范:</label>
					<div class="controls">
						<form:textarea path="fixSpec" htmlEscape="false" rows="5" maxlength="1000" class="input-xlarge" cssStyle="min-height: 70px;max-height: 210px;width: 650px" placeholder="请输入"/><br/>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">视频链接:</label>
					<div class="controls">
						<input class="fromInput url" name="videoUrl" value="${customerProduct.videoUrl}" htmlEscape="false" maxlength="512" style="width: 650px" placeholder="请准确输入第三方视频网站,如优酷,爱奇艺,腾讯等"/>
					</div>
				</div>
			</div>
		</div>
	</form:form>
	<hr style="=border: 1px solid rgba(238, 238, 238, 1);margin: 0px;margin-top: 5px"/>
	<shiro:hasPermission name="md:customerproduct:edit">
		<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="$('#inputForm').submit()" value="保 存"/>
        <c:if test="${customerProduct.id !=null and customerProduct.id>0}">
			&nbsp;&nbsp;<input id="btnDelete" class="btn" type="button" value="删 除" style="margin-top:10px"onclick="del('${customerProduct.id}','${customerProduct.customerDto.id}','${customerProduct.productDto.id}')"/>
		</c:if>
		&nbsp;&nbsp;<input id="btnCancel" class="btn" type="button" value="取 消" style="margin-top:10px"onclick="cancel()"/>
	</shiro:hasPermission>
  </body>
</html>
