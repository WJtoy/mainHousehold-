<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户图片定义</title>
	<meta name="decorator" content="default"/>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@include file="/WEB-INF/views/include/treetable.jsp"%>
	  <style type="text/css">
		  .table thead th, .table tbody td {
			  text-align: center;
			  vertical-align: middle;
			  BackColor: Transparent;
			  height: 30px;
		  }
		  #editBtn {
			  position: fixed;
			  left: 0px;
			  bottom: 0;
			  width: 100%;
			  height: 60px;
			  background: #fff;
			  z-index: 10;
			  border-top: 1px solid #e5e5e5;
		  }
	  </style>
	<script type="text/javascript">
		var this_index = top.layer.index;
		function cancel() {
			top.layer.close(this_index);// 关闭本身
		}

        var clickTag = 0;
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
					var loadingIndex = layerLoading('正在提交，请稍候...');
					var $btnSubmit = $("#btnSubmit");
					if(clickTag == 1){
						return false;
					}
					if ($btnSubmit.prop("disabled") == true) {
						event.preventDefault();
						return false;
					}
					clickTag = 1;
					$btnSubmit.prop("disabled", true);
					if($("#contentTable input[type='checkbox'][name='checkedRecords']:checkbox:checked").length == 0){
						clickTag = 0;
						$btnSubmit.removeAttr('disabled');
						layerInfo("请选择子少一个图片类型.","系统提示");
						return false;
					}

					var entity = {};
					entity['id'] = $("#id").val();
					entity['product.id'] = $("#productId").val();
					if($("#id").val()!=null && $("#id").val() !='' && $("#id").val()!=undefined){
						entity['product.name'] = $("#productName").val();
					}else{
						entity['product.name'] = $("#productId").find("option:selected").text();
					}
					var customerId = $("#customerId").val();
					entity['customer.id'] = customerId;
					entity['barcodeMustFlag'] =  $(":radio:checked[name='barcodeMustFlag']").val();
					$("input[type='checkbox'][name='checkedRecords']:checkbox:checked").each(function(index,element){
						var id = $(this).data('id');
						entity['items['+index+'].checked'] = 1;

						var code = $(this).val();
						entity['items['+index+'].pictureCode'] = code;
						var title = $("#title-" + code +"").val();
						entity['items['+index+'].title'] = title;
						var sort = $(this).data("sort");
						entity['items['+index+'].sort'] = sort;
						var remarks = $("#remarks-" + code +"").val();
						entity['items['+index+'].remarks'] = remarks;
						var isChecked = $("#mustFlag-" + code).is(":checked");
						entity['items['+index+'].mustFlag'] = isChecked?1:0;
					});

					$.ajax({
						url:"${ctx}/md/customer/pic/save",
						type:"POST",
						data: entity,
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
								if(pframe!=undefined){
									pframe.document.location="${ctx}/md/customer/pic/list?customer.id="+customerId;
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
						error: function (data) {
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
							top.layer.close(loadingIndex);
						},
						timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
					});
				},
				errorContainer : "#messageBox",
				errorPlacement : function(error, element)
				{
					$("#messageBox").text("输入有误，请先更正。");
					if (element.is(":checkbox")
							|| element.is(":radio")
							|| element.parent().is(
									".input-append"))
					{
						error.appendTo(element.parent()
								.parent());
					} else
					{
						error.insertAfter(element);
					}
				}});


            $("#selectAll").change(function() {
                var $check = $(this);
                $("input:checkbox").each(function(){
                    if ($(this).val() != "on"){
                        if ($check.prop("checked") == "checked" || $check.prop("checked") == true) {
                            $(this).prop("checked", true);
                        }
                        else{
                            $(this).prop("checked", false);
                        }
                    }
                });
            });
		});

        $(document).on('change','.selectCustomer',function(e){
            var customerId =$(this).val();
            if (customerId == "")
            {
                return false;
            }
            $.ajax({
                    url:"${ctx}/md/customer/pic/ajax/customerProductList?customerId="+customerId,
                    success:function (e) {
                        if(e.success){
                            $("#productId").empty();
                            var programme_sel=[];
                            programme_sel.push('<option value="" selected="selected">请选择</option>')
                            for(var i=0,len=e.data.length;i<len;i++){
                                var programme = e.data[i];
                                programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                            }
                            $("#productId").append(programme_sel.join(' '));
                            $("#productId").val("");
                            $("#productId").change();
                        }else {
                            $("#productId").html('<option value="" selected>请选择</option>');
                            layerMsg('该客户还没有关联商品！');
                        }
                    },
                    error:function (e) {
                        layerError("请求客户产品失败","错误提示");
                    }
                }
            );

			$.ajax({
				url: "${ctx}/md/customer/pic/ajax/customerProductCategoryList",
				data: {customerId: customerId},
				success:function (e) {
					if(e.success){
						$("#productCategoryId").empty();
						var programme_sel=[];
						programme_sel.push('<option value="" selected="selected">请选择</option>')
						for(var i=0,len = e.data.length;i<len;i++){
							var programme = e.data[i];
							programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
						}
						$("#productCategoryId").append(programme_sel.join(' '));
						$("#productCategoryId").val("");
						$("#productCategoryId").change();
					}else {
						$("#productCategoryId").html('<option value="" selected>请选择</option>');
						layerMsg('该客户还没有配置产品品类！');
					}
				},
				error:function (e) {
					layerError("请求产品品类失败","错误提示");
				}
			});
        });

		//获取产品图片配置规格
		$(document).on('change','#productId',function (e){
			var customerId = $("#customerId").val();
			var productId = $("#productId").val();
			$("input[type=checkbox][name=checkedRecords]").each(function () {
				$(this).attr("checked", false);
			})
			$("input[type=checkbox][name=mustFlag]").each(function () {
				$(this).attr("checked", false);
			})
			if(productId ==null || productId == undefined || productId ==''){
				return false;
			}else{
				$.ajax({
					type: 'post',               //默认是form的method（get or post），如果申明，则会覆盖
					dataType: 'json',
					url: "${ctx}/md/customer/pic/ajax/getCompletePicItem?customerId="+ customerId + "&productId=" + productId,
					success:function (e) {
						if(e.success && e.data.items && e.data.items.length>0){
						    if(e.data.barcodeMustFlag == 1){
                                $("#barcodeMustFlag1").attr("checked","checked");
                                $("#barcodeMustFlag0").removeAttr("checked");
                            }
							$.each(e.data.items,function (index,value){
								var code = value.pictureCode;
								if(value.checked==1){
									$("#cbox-" + code).attr("checked", true);
								}
								$("#title-" + code).val(value.title);
								$("#remarks-" + code).val(value.remarks);
								if(value.mustFlag==1){
									$("#mustFlag-" + code).attr("checked", true);
								}
							});
						}
					},
					error:function (e) {
						layerError("请求产品图片配置失败","错误提示");
					}
				});
			}

		});

		$(document).on("change","#productCategoryId",function (e) {
			var productCategoryId = $(this).val();
			var customerId = $("#customerId").val();
			if (customerId !='') {
				if (productCategoryId != "") {
					$.ajax({
						url: "${ctx}/md/customer/pic/ajax/getProductCategoryProductList",
						data: {customerId : customerId,productCategoryId: productCategoryId},
						success:function (e) {
							if(e.success){
								$("#productId").empty();
								var programme_sel=[];
								programme_sel.push('<option value="" selected="selected">请选择</option>')
								for(var i=0,len = e.data.length;i<len;i++){
									var programme = e.data[i];
									programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
								}
								$("#productId").append(programme_sel.join(' '));
								$("#productId").val("");
								$("#productId").change();
							}else {
								$("#productId").html('<option value="" selected>请选择</option>');
								layerMsg('该客户还没有配置产品！');
							}
						},
						error:function (e) {
							layerError("请求产品失败","错误提示");
						}
					});
				}
				<%--else {--%>
				<%--	$.ajax({--%>
				<%--		url: "${ctx}/md/customer/pic/ajax/customerProductList",--%>
				<%--		data: {customerId: customerId},--%>
				<%--		success:function (e) {--%>
				<%--			console.log(e);--%>
				<%--			if(e.success){--%>
				<%--				$("#productId").empty();--%>
				<%--				var programme_sel=[];--%>
				<%--				programme_sel.push('<option value="" selected="selected">请选择</option>')--%>
				<%--				for(var i=0,len = e.data.length;i<len;i++){--%>
				<%--					var programme = e.data[i];--%>
				<%--					programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')--%>
				<%--				}--%>
				<%--				$("#productId").append(programme_sel.join(' '));--%>
				<%--				$("#productId").val("");--%>
				<%--				$("#productId").change();--%>
				<%--			}else {--%>
				<%--				$("#productId").html('<option value="" selected>请选择</option>');--%>
				<%--				layerMsg('该客户还没有配置产品！');--%>
				<%--			}--%>
				<%--		},--%>
				<%--		error:function (e) {--%>
				<%--			layerError("请求产品失败","错误提示");--%>
				<%--		}--%>
				<%--	});--%>
				<%--}--%>
			}else {
				layerMsg("请先选择客户");
			}
		});
	</script>
  </head>
  <body>
	<form:form id="inputForm" modelAttribute="entity" method="post" action="${ctx}/md/customer/pic/save" class="form-horizontal">
		<sys:message content="${message}"/>
		<form:hidden path="id"/>
		<c:if test="${canAction == true}">
			<div  style="margin-top: 20px;float: left;margin-right: 10px;">
				<label style="float: left;margin-top: 5px">客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户：</label>
				<div class="controls" style="margin-left: 80px">
					<c:choose>
						<c:when test="${entity.customer.id > 0}">
							<form:hidden path="customer.id" id="customerId"></form:hidden>
							<form:input path="customer.name" id="customerName" readonly="true"></form:input>
						</c:when>
						<c:otherwise>
							<form:select path="customer.id" id="customerId" cssClass="input-small required selectCustomer" cssStyle="width:225px;">
								<form:option value="" label="请选择"/>
								<form:options items="${customerList}" itemLabel="name" itemValue="id" htmlEscape="false"/>
							</form:select>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
			<c:set value="${entity.customer.id}" var="customerId"/>
			<c:if test="${customerId == null}">
			<div  style="margin-top: 20px;float: left;width: 500px">
				<label>产品品类：</label>
				<form:select path="productCategoryId" cssStyle="width: 200px;margin-left: 8px">
					<form:option value="" label="请选择"></form:option>
					<form:options items="${productCategoryList}" itemLabel="name" itemValue="id"></form:options>
				</form:select>
			</div>
			</c:if>
			<div  style="margin-top: 20px;float: left;">
				<label style="margin-top: 5px;float: left;">产&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp品：</label>
				<div class="controls" style="margin-left: 10px;float: left;">
					<c:choose>
						<c:when test="${entity.customer.id > 0}">
							<form:hidden path="product.id" id="productId"></form:hidden>
							<form:input path="product.name" id="productName" readonly="true"></form:input>
						</c:when>
						<c:otherwise>
							<select id="productId" name ="product.id" style="width:225px;" class="required">
								<option value="" selected>请选择</option>
							</select>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
			<div style="margin-top: 20px;float: left">
				<label style="float: left;margin-left: 10px;margin-top: 5px">上传条码：</label>
				<div class="controls" style="margin-left: 88px;margin-top: 5px">
						<span>
							<form:radiobutton path="barcodeMustFlag" value="1" cssClass="required"></form:radiobutton>是
						</span>
					<span>
							<form:radiobutton path="barcodeMustFlag" value="0" cssClass="required"></form:radiobutton>否
						</span>

				</div>
			</div>
			<div id="divGrid" style="float: left;margin-top: 18px">
				<table id="contentTable" class="table table-striped table-bordered table-condensed" style="table-layout:fixed">
					<thead>
					<tr>
						<th width="50px"><input type="checkbox" id="selectAll" name="selectAll" style="zoom: 1.5"/></th>
						<th width="37px">序号</th>
						<th width="220px">完工信息</th>
						<th width="50px">必选</th>
						<th width="500px">描述</th>
					</tr>
					</thead>
					<tbody>
					<c:forEach items="${entity.items}" var="item" varStatus="i" begin="0">
						<c:set var="index" value="${i.index}" />
						<tr id="tr_${index}">
							<td>
								<input type="checkbox" name="checkedRecords" id="cbox-${item.pictureCode}" ${item.checked==1?'checked':''} value="${item.pictureCode}" style="zoom: 1.5"
										   data-id="${index}" data-remarks="${item.remarks}" data-title="${item.title}" data-sort="${item.sort}" />
							</td>
							<td>${index+1}</td>
							<td><input type="text" id="title-${item.pictureCode}" value="${item.title}"/></td>
							<td>
								<input type="checkbox" id="mustFlag-${item.pictureCode}" name="mustFlag" ${item.mustFlag==1?'checked':''} style="zoom: 1.5" />
							</td>
							<td >
								<input type="text" id="remarks-${item.pictureCode}" value="${item.remarks}" style="width: 510px"/>
							</td>
						</tr>
					</c:forEach>
					</tbody>
				</table>
			</div>
		</c:if>
		<div id="editBtn" class="line-row">
			<shiro:hasPermission name="md:customerpic:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="width: 96px;height: 40px;margin-left: 76%;margin-top: 10px;margin-bottom: 10px"/>
				&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="width: 96px;height: 40px;margin-top: 10px;margin-left: 13px;margin-bottom: 10px"/>
		</div>
	</form:form>
	<script>
	</script>
  </body>
</html>
