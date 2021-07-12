<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>修改订单</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/modules/sd/tpl/orderEditForm.html" %>
	<style type="text/css">
		.form-horizontal .control-label{width:120px;}
		.form-horizontal .controls {margin-left:140px;*margin-left:0px;*display:block;*padding-left:20px;}
		.form-horizontal .controls-urgent {margin-left:100px;*margin-left:0px;*display:block;*padding-left:20px;}
		.form-horizontal .controls-urgent span {margin-right:5px;}
		.divUrgent .control-group {border-bottom:0px;}
		.row-fluid .span4 {width: 400px;}
		.row-fluid .span6 {width: 600px;}
		.row-fluid .span7 {width: 600px;}
		.row-fluid .span8 {width: 760px;}
		.tooltip.bottom .tooltip-arrow {border-bottom-color:#EEADAD;}
		.tooltip.right .tooltip-arrow {border-right-color:#EEADAD;}
		.tooltip.top .tooltip-arrow {border-top-color:#EEADAD;}
		.tooltip.left .tooltip-arrow {border-left-color:#EEADAD;}
		.tooltip-inner {background-color:#EEADAD;color:#b94a48;}
		.label-amout {text-align:right;width:80px;}
	</style>
	<script type="text/javascript">
        top.layer.closeAll();
        var clickTag = 0;
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
                    if(clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr('disabled', 'disabled');
                    var subAreaId = $("#subAreaId").val();
                    if(Utils.isEmpty(subAreaId)){
                       $("#subAreaId").val(1)
					}
					if(Utils.isEmpty($("[id='customer.id']").val())){
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
						layerError("您的账号未设定客户，请联系管理员。","提示");
						return;
					}
					if($("#productTable tr:visible").length <= 1){
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
                        layerError("订单下未添加产品详细清单。","提示");
						return;
					}
					var oid = $("#id").val();
					//check repeate create order
					var phone1 = $("#phone1").val();
					var phone2 = $("#phone2").val();
					if(phone1 =="" && phone2 == ""){
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
						layerInfo("请输入用户电话。","信息提示");
				    	return false;
					}
                    var confirmClickTag = 0;
                    top.layer.confirm('确定保存订单吗?',
						{
                        	icon: 3,
							title:'系统确认',
							cancel: function(index, layero){
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }
						},
						function(index,layero){
                            if(confirmClickTag == 1){
                                return false;
                            }
                            var btn0 = $(".layui-layer-btn0",layero);
                            if(btn0.hasClass("layui-btn-disabled")){
                                return false;
                            }
                            confirmClickTag = 1;
                            btn0.addClass("layui-btn-disabled").attr("disabled","disabled");
                            top.layer.close(index);//关闭本身
							// do something
							layerLoading('正在提交，请稍等...');
							form.submit();
                    },function(index){//cancel
                            clickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                    });
                    return false;
				},
				errorContainer: "#messageBox",
				errorPlacement: function(error, element) {
					$("#messageBox").text("输入有误，请先更正。");
					if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
						error.appendTo(element.parent().parent());
					}if (element.parent().is(".input-td")){ 
						error.appendTo(element.parent());
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
		});
        $(document).ready(function() {
            <c:if test="${noKefuFlag==1}">
            var tip = $("#tip").html();
            top.layer.confirm('<div style="float:left;margin-top:10px;height:80px"><img src="${ctxStatic}/images/icon/icon-red-warning.png" style="width: 24px;margin-right: 8px;"></div>'+tip, {
                btn: ['好的'] //按钮
            }, function(index){
                top.layer.close(index);//关闭本身
            });
            </c:if>
        });
	</script>
</head>
<body>
   <div style="display: none" id="tip">
	  ${tip}
    </div>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">修改订单[${order.orderNo}]</a></li>
	</ul>
	<sys:message content="${message}"/>
	<form:form id="inputForm" modelAttribute="order" action="${ctx}/sd/order/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="updateDate"/>
		<form:hidden path="orderNo"/>
		<form:hidden path="b2bOrderNo"/>
        <form:hidden path="parentBizOrderId"/>
		<form:hidden path="customer.id" />
		<form:hidden path="version" />
		<form:hidden path="actionType" />
		<form:hidden path="status" />
		<form:hidden path="quarter" />
		<form:hidden path="category.id" />
        <form:hidden path="dataSource.value"/>
		<form:hidden path="b2bShop.shopId" />
		<form:hidden path="orderChannel" />
		<form:hidden path="subArea.id" id="subAreaId"/>
		<form:hidden path="customerUrgentCharge" />
		<c:set var="currentuser" value="${fns:getUser() }" />
		<!-- order head -->
		<legend>客户信息</legend>
		<div class="row-fluid" style="display: ${showReceive?'':'none'}">
			<div class="span6">
				<div class="control-group">
					<label class="control-label" data-desc="余额-冻结金额">可下单金额:</label>
					<div class="controls" >
						<c:set var="cbalance" value="${order.customerBalance-order.customerBlockBalance}" />
						<input type="text" id="balance" name="balance" class="input-mini" readonly="readonly" style="border-color:#b94a48;color:#b94a48;"
							   value="<fmt:formatNumber value="${cbalance}" pattern="0.00"/>" />
					<label >信用额度:</label>
						<input type="text" id="credit" name="credit" class="input-mini" readonly="readonly" style="border-color:#b94a48;color:#b94a48;" value="${order.customerCredit}" />
					</div>				
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">联系人:</label>
					<div class="controls">
						<form:input path="userName" htmlEscape="false" data-placement="right" maxlength="100" class="required userName" /><span class="add-on red">*</span>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">手机:</label>
					<div class="controls" >
						<form:input path="phone1" type="tel" htmlEscape="false" data-placement="right" maxlength="11" placeholder="第一联系电话，必填" class="required mobile " /><span class="add-on red">*</span>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">座机:</label>
					<div class="controls" >
						<form:input path="phone2" htmlEscape="false" maxlength="16" cssclass="phone" placeholder="第二联系电话，可选" />
					</div>
				</div>
			</div>
			
		</div>
		<div class="row-fluid">
			<div class="span4" style="width: auto;">
				<div class="control-group">
					<label class="control-label">地址:</label>
					<div class="controls" >
						<%--
						<sys:areaselect name="area.id" id="area" value="${order.area.id}" labelValue="${order.area.name}" labelName="area.name"
										title="" tooltipPlacement="bottom" mustSelectCounty="true" cssClass="required"
										>
						</sys:areaselect>
						--%>
                        <sys:newareaselect name="area.id" id="area" value="${order.area.id}" labelValue="${order.area.name}" labelName="area.name"
                                        title="" tooltipPlacement="bottom" mustSelectCounty="true" cssClass="required"
                        >
                        </sys:newareaselect>
						<span class="add-on red">*</span>
					</div>
				</div>
			</div>
			<div class="span7" style="margin-left:5px;">
				<div class="control-group">
					<div class="controls" style="padding-left:0px;margin-left:0px;display:inherit;">
						<form:input path="address" htmlEscape="false" maxlength="150" data-placement="bottom" class="required" style="width:340px;" />
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
						<form:textarea path="description" htmlEscape="false" rows="2" maxlength="250" class="input-xlarge" style="width:100%;" />
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
					<th width=30px>序号</th>
					<th>产品</th>
					<th>服务类型</th>
					<th>数量</th>
					<th>品牌</th>
					<th>型号/规格</th>
					<th>B2B产品编码</th>
					<th style="display: ${showReceive?'':'none'}">服务金额</th>
					<th style="display: ${showReceive?'':'none'}">冻结金额</th>
					<th>备注</th>
					<th></th>
				</thead>	
			<tbody>

			</tbody>
			</table>
			</div>
		<div class="control-group">
		<table class="table table-bordered table-condensed">
			<c:if test="${canCreateOrder == true}">
			<tr >
				<td colspan="2">
				 <a id="orderForm_btn_add" class="" href="javascript:;" onclick="addEmptyRow();"  style="margin-right:50px;"  title="添加产品"><i class="icon-add" style="margin-top: 0px;"></i></a>
				</td>
			</tr>
			</c:if>
			<tr style="display: ${showReceive?'':'none'}">
				<td style="text-align:right;">服务金额</td>
				<td width="200px" style="align:left;"><label id="lblassignedCharge">${order.expectCharge }</label></td>
			</tr>
			<tr style="display: ${showReceive?'':'none'}">
				<td style="text-align:right;">冻结金额</td>
				<td  style="align:left;"><label id="lblblockedCharge">${order.blockedCharge }</label></td>
			</tr>
			<c:choose>
			<c:when test="${order.urgentFlag == 1}">
				<tr id="trUrgentCharge" style="display: ${showReceive?'':'none'}">
			</c:when>
			<c:otherwise>
			<tr id="trUrgentCharge" style="display: none;">
			</c:otherwise>
			</c:choose>
				<td style="text-align:right;">加急金额</td>
				<td  style="align:left;"><label id="lblUrgentCharge">${order.customerUrgentCharge}</label></td>
			</tr>
			<tr style="display: ${showReceive?'':'none'}">
				<td style="text-align:right;">数量总计</td>
				<td  style="align:left;"><label id="lbltotalQty">${order.totalQty}</label></td>
			</tr>
			<tr style="display: ${showReceive?'':'none'}">
				<td style="text-align:right;">总计</td>
				<td  style="align:left;"><label id="lbltotalCharge" class="alert ${(order.expectCharge+order.blockedCharge+order.customerUrgentCharge)>order.customerBalance+order.customerCredit?'alert-error':'alert-success'}">${order.expectCharge + order.blockedCharge + order.customerUrgentCharge}</label>
				</td>
			</tr>
		</table>
		</div>
	</div>
	<div class="row-fluid">
		<!-- 加急 -->
		<div class="span5 divUrgent" id="divUrgent" style="width: auto;<c:if test="${order.urgentFlag == 0 || order.urgentLevel.id == 0}">display:none;</c:if>">
			<div  class="control-group">
				<label class="control-label" style="width: 80px;">加急:</label>
				<div class="controls-urgent" style="margin-right: 10px;">
					<c:if test="${order.urgentLevel.id>0}">
						<form:radiobutton path="urgentLevel.id" value="${order.urgentLevel.id}" label="${order.urgentLevel.remarks}" />
					</c:if>
					<input type="hidden" id="urgentFlag" name="urgentFlag" value="${order.urgentFlag}" />
					<span class="help-inline alert" style="color: red;padding: 3px 3px;display:${showReceive?'':'none'}">
						加急费：<label id="lblUrgent" style="min-width:30px;display:inherit;">${fns:formatDouble(order.customerUrgentCharge,1)}</label> 元
					</span>
					<br>
					<span class="help-inline">
						注：在时效内完成的工单才会扣除加急费用!
					</span>
				</div>
			</div>
		</div>
		<div class="span5" style="width: auto;">
				<shiro:hasPermission name="sd:order:add">
					<c:choose>
						<c:when test="${!canCreateOrder}">
							<input id="btnSubmit" name="btnSubmit" class="btn btn-danger disabled" type="submit"  disabled="disabled" value="保 存"></button>
						</c:when>
						<c:when test="${(order.customerBalance + order.customerCredit - order.customerBlockBalance - order.expectCharge - order.blockedCharge) <= 0}">
							<input id="btnSubmit" name="btnSubmit" class="btn btn-danger disabled" type="submit" disabled="disabled" value="保 存"></button>
						</c:when>
						<c:otherwise>
							<input id="btnSubmit" name="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>
						</c:otherwise>
					</c:choose>
				</shiro:hasPermission>
			&nbsp;&nbsp;<input id="btnCancel" class="btn" type="button" value="返回" onclick="history.go(-1)"/>
		</div>
	</div>
	</form:form>
	<script class="removedscript" id="sc_AddItemRows" type="text/javascript">
		$(document).ready(function() {
            Order.rootUrl = "${ctx}";
            //load items
			Order.hideChargeColumn = ${!showReceive};
            Order.editForm_addItemRows(${fns:toGson(order)});
            <%--<c:if test="${canCreateOrder == true && order != null && order.customer !=null && order.customer.id != null && order.customer.id != 0}">--%>
            <%--Order.editForm_loadProductsForKKL(${order.customer.id},'${order.dataSource.value}',false);--%>
            <%--</c:if>--%>
        });
		function addEmptyRow(){
			Order.hideChargeColumn = ${!showReceive};
			Order.editForm_loadProductsForKKL('${order.customer.id}','${order.dataSource.value}',true);
			return false;
		}
	</script>
</body>
</html>