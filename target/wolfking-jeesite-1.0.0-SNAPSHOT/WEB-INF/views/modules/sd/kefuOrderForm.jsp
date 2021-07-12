<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>客服代下单</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/modules/sd/tpl/orderForm.html" %>
	<style type="text/css">
		.form-horizontal .control-label{width:120px;}
		.form-horizontal .controls {
			margin-left:140px;*margin-left:0px;*display:block;*padding-left:20px;
		}
		.row-fluid .span4 {width: 400px;}
		.row-fluid .span6 {width: 600px;}
		.row-fluid .span7 {width: 630px;}
		.row-fluid .span8 {width: 760px;}
		.label-amout {text-align:right;width:80px;}
	</style>
</head>
<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">订单 ${empty order.orderNo?'添加':'修改 ['.concat(order.orderNo).concat(']') }</a></li>
	</ul>
	<input type="hidden" id="hasRepeatOrder" name="hasRepeatOrder" value="false" />
	<sys:message content="${message}"/>
	<form:form id="inputForm" modelAttribute="order" action="${ctx}/sd/order/kefuadd" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="updateDate"/>
		<form:hidden path="orderNo"/>
		<form:hidden path="orderPaymentType.value" />
		<form:hidden path="orderPaymentType.label" />
		<form:hidden path="createBy.id" />
		<form:hidden path="expectCharge" />
		<form:hidden path="blockedCharge" />
		<form:hidden path="totalQty" />
		<c:set var="currentuser" value="${fns:getUser() }" />
		
		<!-- order head -->
		<legend>客户信息</legend>
		
		<c:choose>
		<c:when test="${currentuser.isCustomer()}">
			<div class="row-fluid" style="display:none;">
				<div class="span4">
					<div class="control-group">
						<label class="control-label">客户:</label>
						<div class="controls" >
							<input type="hidden" id="customer.id" name="customer.id"
								value="${order.customer.id}" maxlength="50"/>
							<input type="text" id="customer.name" name="customer.name"
								value="${order.customer.name}" maxlength="100"  />	
						</div>	
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span4">
					<div class="control-group">
						<label class="control-label">联系人:</label>
						<div class="controls">
							<form:input path="userName" htmlEscape="false" maxlength="100" class="required userName" /><span class="add-on red">*</span>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">可下单金额:</label>
						<div class="controls" >
							<input type="text" id="balance" name="balance" class="input-mini" readonly="readonly" style="border-color:#b94a48;color:#b94a48;"
								   value="${order.customerBalance + order.customerCredit - order.customerBlockBalance}" />
						<label >信用额度:</label>
							<input type="text" id="credit" name="credit" class="input-mini" readonly="readonly" style="border-color:#b94a48;color:#b94a48;" value="${order.customerCredit}" />
							<shiro:hasPermission name="fi:customercurrency:chargeonline">
								&nbsp;&nbsp;<a class="btn btn-primary" target="_blank" href="${ctx}/fi/customercurrency/chargeonline">在线充值</a>
							</shiro:hasPermission>
						</div>

					</div>
				</div>
			</div>
		</c:when>
		<c:otherwise>
			<div class="row-fluid">
				<div class="span4" style="width: auto;">
					<div class="control-group">
						<label class="control-label">客户:</label>
						<div class="controls" >
							<input type="hidden" id="customer.id" name="customer.id" value="${order.customer.id}" maxlength="50"/>
							<input type="hidden" id="customer.name" name="customer.name" value="${order.customer.name}" maxlength="100"  />
							<select id="customerId" name="customerId" class="input-small" style="width:328px;">
								<option value="">所有</option>
								<c:forEach items="${fns:getMyCustomerList()}" var="dict">
									<option value="${dict.id}">${dict.name}</option>
								</c:forEach>
							</select>
						</div>	
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">可下单金额:</label>
						<div class="controls" >
							<input type="text" id="balance" name="balance" class="input-mini" readonly="readonly" style="width:75px;border-color:#b94a48;color:#b94a48;"
								   value="${order.customerBalance + order.customerCredit - order.customerBlockBalance}" />
							<label >信用额度:</label>
							<input type="text" id="credit" name="credit" class="input-mini" readonly="readonly" style="width:75px;border-color:#b94a48;color:#b94a48;" value="${order.customerCredit}" />
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span14">
					<div class="control-group">
						<label class="control-label">联系人:</label>
						<div class="controls">
							<form:input path="userName" htmlEscape="false" maxlength="50" class="required userName" cssStyle="width: 290px;"/>
							<span class="add-on red">*</span>
						</div>
					</div>
				</div>
			</div>
			</c:otherwise>
		</c:choose>

		<div class="row-fluid">
			<div class="span4" style="width: auto;">
				<div class="control-group">
					<label class="control-label">手机:</label>
					<div class="controls" >
						<form:input path="phone1" type="tel" htmlEscape="false" maxlength="11" placeholder="第一联系电话，必填" class="required mobile " cssStyle="width: 290px;"/>
						<span class="add-on red">*</span>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">座机:</label>
					<div class="controls" >
						<form:input path="phone2" htmlEscape="false" maxlength="16" class="phone" placeholder="第二联系电话，可选" cssStyle="width: 230px;"/>
						<span class="red"></span>
					</div>
				</div>
			</div>
			
		</div>
		<div class="row-fluid">
			<div class="span4" style="width: auto;">
				<div class="control-group">
					<label class="control-label">地址:</label>
					<div class="controls" >
						<sys:areaselect name="area.id" id="area" value="${order.area.id}" labelValue="${order.area.name}" labelName="area.name"
							title="" mustSelectCounty="true" cssClass="required" cssStyle="width: 248px;"></sys:areaselect>
						<span class="add-on red">*</span>
					</div>
				</div>
			</div>
			<div class="span7" style="margin-left:5px;">
				<div class="control-group">
					<div class="controls" style="padding-left:0px;margin-left:0px;display:inherit;">
						<form:input path="address" htmlEscape="false" maxlength="150" class="required" style="width:400px;" />
						<span class="add-on red">* 详细地址不包含省、市、区县</span>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span8">
				<div class="control-group">
					<label class="control-label">服务描述:</label>
					<div class="controls" >
						<form:textarea path="description" htmlEscape="false" rows="2" maxlength="250" class="input-xlarge" style="width: 733px;" />
					</div>
				</div>
			</div>
		</div>
		<!-- order body -->
		<legend>产品详细清单</legend>
		<div class="row-fluid">
		<div class="control-group">
			<table id="productTable" class="table table-striped table-bordered table-condensed" style="margin-bottom: 0px;">
			<thead>
				<tr>
					<th width=10px><input type="checkbox" id="selectAll" name="selectAll"/></th>
					<th width=30px>序号</th>
					<th>产品</th>
					<th>服务类型</th>
					<th>数量</th>
					<th>品牌</th>
					<th>型号/规格</th>
					<th>快递公司</th>
					<th>快递单号</th>
					<th width=40px></th>
					<th>服务金额</th>
					<th>冻结金额</th>
				</thead>	
			<tbody>
			</tbody>
			</table>
			</div>

		<div class="control-group">
		<table class="table table-bordered table-condensed">
			<c:if test="${canCreateOrder}">
			<tr >
				<td colspan="2">
				 <a id="orderForm_btn_delete" class="" href="#" style="margin-right:20px;" onclick="Order.delProductRows();return false;" title="批量删除"><i class="icon-delete" style="margin-top: 0px;"></i></a>
				</td>
			</tr>
			</c:if>
			<tr>
				<td style="text-align:right;">服务金额</td>
				<td width="200px" style="align:left;"><label id="lblassignedCharge">${order.expectCharge }</label></td>
			</tr>
			<tr>
				<td style="text-align:right;">冻结金额</td>
				<td  style="align:left;"><label id="lblblockedCharge">${order.blockedCharge }</label></td>
			</tr>
			<tr>
				<td style="text-align:right;">数量总计</td>
				<td  style="align:left;"><label id="lbltotalQty">${order.totalQty}</label></td>
			</tr>
			<tr>
				<td style="text-align:right;">总计</td>
				<td  style="align:left;"><label id="lbltotalCharge" class="alert ${(order.expectCharge+order.blockedCharge)>(order.customerBalance+order.customerCredit- order.customerBlockBalance)?'alert-error':'alert-success'}">${order.expectCharge+order.blockedCharge}</label></td>
			</tr>
		</table>
		</div>
		
		</div>
		<div class="control-group">
			<div class="controls">
				<shiro:hasPermission name="sd:order:add">
				<c:choose>
					<c:when test="${!canCreateOrder}">
						<input id="btnSubmit" name="btnSubmit" class="btn btn-danger" type="submit"  disabled="disabled" value="保 存"></button>&nbsp;
					</c:when>
					<c:when test="${(order.customerBalance + order.customerCredit - order.customerBlockBalance - order.expectCharge - order.blockedCharge) <= 0}">
						<input id="btnSubmit" name="btnSubmit" class="btn btn-danger" type="submit" disabled="disabled" value="保 存"></button>&nbsp;
					</c:when>
					<c:otherwise>
						<input id="btnSubmit" name="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;
					</c:otherwise>
				</c:choose>
				</shiro:hasPermission>
			<a id="btnCancel" class="btn" href="#" >返回</a>
			</div>
		</div>
	</form:form>
	<%--<c:if test="${!currentuser.isCustomer()}">--%>
	<script class="removedscript" id="sc_AddItemRows" type="text/javascript">
        $(document).ready(function() {
            <c:if test="${order != null && order.customer !=null && order.customer.id != null && order.customer.id != 0}">
            	$("#customerId").select2().val("${order.customer.id}").trigger('change');
			</c:if>
            //load items
            //Order.addItemRows(${fns:toGson(order)});
			<c:if test="${canCreateOrder == true && order != null && order.customer !=null && order.customer.id != null && order.customer.id != 0}">
            Order.loadProducts(${order.customer.id});
			</c:if>
        });
	</script>
	<%--</c:if>--%>
	<script type="text/javascript">
		top.layer.closeAll();
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
				    var $btnSubmit = $("#btnSubmit");
					if($btnSubmit.prop("disabled") == true){
						return false;
					}

                    $btnSubmit.attr("disabled", "disabled");

					if(Utils.isEmpty($("[id='customer.id']").val())){
						layerAlert("您的账号未关联客户，请联系管理员。","系统提示");
                        $btnSubmit.removeAttr('disabled');
						return false;
					}
					// check items
					var minrow = 1;
					if($("#addrow").length >0){
						minrow = 2;
					}
					if($("#productTable tr:visible").length == minrow){
                        layerAlert("订单下未添加产品详细清单。","系统提示");
                        $btnSubmit.removeAttr('disabled');
						return false;
					}

					var oid = $("#id").val();
					//check repeate create order
					var phone1 = $("#phone1").val();
					var phone2 = $("#phone2").val();

					if(Utils.isEmpty(phone1) && Utils.isEmpty(phone2)){
						layerInfo("请输入用户电话。","信息提示");
                        $btnSubmit.removeAttr('disabled');
						return false;
					}else if(Utils.isEmpty(oid)){
						//check repeate order when create order
                        var cid = $("[id='customer.id']").val();
                        var check_index = layerLoading("正在检查重复下单，请稍等...");
						$.ajax({
							type: "GET",
							url: "${ctx}/sd/order/checkrepeatorder?phone1="+phone1+"&phone2="+phone2+"&customerId=" + cid +"&t="+(new Date()).getTime(),
							data:"",
							async: false,
							success: function (data) {
                                if(ajaxLogout(data)){
                                    return false;
                                }
								if(data){
									if (data.message != "OK"){
                                        $btnSubmit.removeAttr('disabled');
									    top.layer.close(check_index);
										layerError("检查重复下单发生错误：" + data.message + "，请重新保存。","错误提示");
										return false;
									}
                                    top.layer.close(check_index);
                                    if(data.success == true){
                                        top.layer.confirm(
                                            '该用户30天已有订单，确定要生成该订单吗？'
                                            ,{icon: 3,closeBtn: 0,title:'系统确认',success: function(layro, index) {
                                                $(document).on('keydown', layro, function(e) {
                                                    if (e.keyCode == 13) {
                                                        layro.find('a.layui-layer-btn0').trigger('click')
                                                    }else if(e.keyCode == 27){
                                                        $btnSubmit.removeAttr('disabled');
                                                        top.layer.close(index);//关闭本身
                                                    }
                                                })
                                            }}
                                            ,function(index){
                                                top.layer.close(index);//关闭本身
                                                // do something
                                                layerLoading('正在提交，请稍等...');
                                                form.submit();
                                                return false;
                                            },function(index){//cancel
                                                $btnSubmit.removeAttr('disabled');
                                            });
                                        return false;
                                    }else{
                                        var actiontype="生成";
                                        if(!Utils.isEmpty($("#id").val())){
                                            actiontype="保存";
                                        }
                                        top.layer.confirm('确定'+actiontype+'订单吗？', {icon: 3, title:'系统确认'}, function(index) {
                                            top.layer.close(index);//关闭本身
                                            var loaindIndex = layerLoading('正在提交，请稍等...');
                                            form.submit();
                                            return false;
                                        },function(index) {
                                            $btnSubmit.removeAttr('disabled');
										});
                                        return false;
                                    }
								}
								else{
                                    layerError("检查重复下单发生错误，请重新保存。","错误提示");
									return false;
								}
							},
							error: function (e) {
                                $btnSubmit.removeAttr('disabled');
                                top.layer.close(check_index);
                                ajaxLogout(e.responseText,null,"检查重复下单发生错误，请重试!");
                                //layerError("检查重复下单发生错误:"+e,"错误提示");
								return false;
							}
						});
					}
				},
				errorContainer: "#messageBox",
				errorPlacement: function(error, element) {
					$("#messageBox").text("输入有误，请先更正。");
					if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
						error.appendTo(element.parent().parent());
					} else {
						var nspan = $(element.parent()).find("span");
						if(nspan){
							error.insertAfter(nspan);
						}else{
							error.insertAfter(element);
						}
					}
				}
			});

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
            <c:if test="${currentuser.isSystemUser() || currentuser.isSaleman()}">
			$("#customerId").change(function() {
				//var id =$("[id='customer.id']").val();
				var id = $(this).val();
                $("[id='customer.id']").val(id);
                $("[id='customer.name']").val($("#customerId option:selected").text());
				Order.reload = true;
				Order.loadProducts(id);
			});
			</c:if>
			var tm = new Date().getTime();
			var url = "${ctx}/sd/order";
			if(Utils.isEmpty($("#id").val())){
				url=url+"/kefuform?_t"+tm;
			}else{
				var refurl = document.referrer;
				var frompage ="";
				url = url + frompage+"?orderId="+$("#id").val()+"&_"+tm;
			}
			$("#btnCancel").prop('href',url);
		});
	</script>
	<script class="removedscript" type="text/javascript">
        $(document).ready(function() {
            $('[data-toggle=tooltip]').darkTooltip();
            //remove important
            //$(".removedscript").remove();
        });
	</script>
</body>
</html>