<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>下单</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<%
		response.setHeader("Cache-Control","no-store");
		response.setHeader("Pragrma","no-cache");
		response.setDateHeader("Expires",0);
	%>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/modules/sd/tpl/orderFormNew.html" %>
	<style type="text/css">
		.form-horizontal .control-label{width:120px;}
		.form-horizontal .controls {margin-left:140px;*margin-left:0px;*display:block;*padding-left:20px;}
		.form-horizontal .controls-urgent {margin-left:100px;*margin-left:0px;*display:block;*padding-left:20px;}
		.form-horizontal .controls-urgent span {margin-right:5px;}
		.divUrgent .control-group {border-bottom:0px;}
		.row-fluid .span4 {width: 400px;}
		.row-fluid .span6 {width: 600px;}
		.row-fluid .span7 {width: 630px;}
		.row-fluid .span8 {width: 760px;}
		.label-amout {text-align:right;width:80px;}
		legend {margin-bottom: 5px;}
	</style>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<script type="text/javascript">
        Order.rootUrl = "${ctx}";
        Order.reload = false;
        var urgentFlag = ${order.urgentFlag};
        var ma = false;
        $(document).on("click", "#btnMatch", function () {
            var addr = $("#txtAddress").val();
            if (addr.length == 0){
                return false;
            }
            if (ma) {
                return false;
            }
            if ($("#btnMatch").prop("disabled") == true)
            {
                return false;
            }
            $("#btnMatch").prop("disabled", true);
            restoreLocation('','');
            var dadata = {fullAddress:addr};
            $.ajax({
                cache: false,
                type: "POST",
                url: "${ctx}/sys/area/new_da",
                data: dadata,
                success: function (data) {
                    if (data.success){
                        if (data.data && data.data.length !== 0){
                            $("#areaId").val(data.data[0]);
                            // console.log("街道:" + data.data[1]);
                            $("#subAreaId").val(data.data[1]); //街道
                            $("#areaName").val(data.data[2]);
                            $("#address").val(data.data[3]);
                            if(data.data[5].length>0) {
                                $("#phone1").val(data.data[5]);
                            }
                            if(data.data[6].length>0) {
                                $("#userName").val(data.data[6]);
                            }
                            if (data.data[4] == "0"){
                                layerInfo("系统识别地址与填写地址不一致,请确认","信息提示");
                            }else{
                                //2019-04-15 经纬度
                                if(data.data.length === 9){
                                    $("#longitude").val(data.data[7]);
                                    $("#latitude").val(data.data[8]);
                                }
                            }
                            areaIdCallback(data.data[0],'');
                        }else{
                            $("#areaId").val(null);
                            $("#subAreaId").val(null); //街道
                            $("#areaName").val("");
                            $("#phone1").val("");
                            $("#userName").val("");
                            layerInfo("该信息暂时无法识别,请手动选择");
                            areaIdCallback('','');
                        }
                    }else{
                        $("#areaId").val(null);
                        $("#areaName").val("");
                        $("#phone1").val("");
                        $("#userName").val("");
                        layerInfo("该信息暂时无法识别,请手动选择");
                        areaIdCallback('','');
                    }
                    ma = true;
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    ajaxLogout(xhr.responseText,null,"该信息暂时无法识别,请手动选择!");
                }
            });

            $("#btnMatch").prop("disabled", false);
        });

        $(document).on("change", "#txtAddress", function () {
            ma = false;
        });

        //变更地址，经纬度座标还原
        function restoreLocation(id, name){
            $("#longitude").val(0);
            $("#latitude").val(0);
		}

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
<%--	<li class="active"><a href="javascript:void(0);">订单 ${empty order.orderNo?'添加':'修改 ['.concat(order.orderNo).concat(']') }</a></li>--%>
	<li class="active"><a href="javascript:void(0);">${empty createOrderConfig?'下单':createOrderConfig.firstType.title }</a></li>
	<c:if test="${not empty createOrderConfig.secondType.title}">
		<li><a href="${ctx}/sd/order/${createOrderConfig.secondType.url}">${createOrderConfig.secondType.title}</a></li>
	</c:if>
</ul>
<input type="hidden" id="hasRepeatOrder" name="hasRepeatOrder" value="false" />
<sys:message content="${message}"/>
<c:set var="url" value="${ctx}/sd/order/add" />
<c:if test="${!canCreateOrder}">
	<c:set var="url" value="#" />
</c:if>
<form:form id="inputForm" modelAttribute="order" action="${url}" method="post" class="form-horizontal">
	<form:hidden path="id"/>
	<form:hidden path="updateDate"/>
	<form:hidden path="orderNo"/>
	<form:hidden path="orderPaymentType.value" />
	<form:hidden path="orderPaymentType.label" />
	<form:hidden path="createBy.id" />
	<form:hidden path="expectCharge" />
	<form:hidden path="blockedCharge" />
	<form:hidden path="totalQty" />
	<form:hidden path="repeateNo" />
	<form:hidden path="customerUrgentCharge" />
	<form:hidden path="engineerUrgentCharge" />
	<form:hidden path="longitude" />
	<form:hidden path="latitude" />
	<form:hidden path="subArea.id" id="subAreaId" />
    <form:hidden path="category.id" />
    <form:hidden path="orderChannel" />
	<!-- order head -->
	<legend>客户信息</legend>
	<!-- customer -->
	<c:choose>
		<c:when test="${currentuser.isCustomer()}">
			<div class="span4" style="display:none;">
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
				<div class="span4" style="width: auto;">
					<div class="control-group">
						<label class="control-label">购买店铺:</label>
						<div class="controls" >
							<select id="shopId" name="b2bShop.shopId" class="input-small" style="width:328px;">
								<option value="" data-channel="1" selected>请选择</option>
							</select>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">第三方单号:</label>
						<div class="controls" >
							<form:input path="b2bOrderNo" type="text" htmlEscape="false" maxlength="30" placeholder="第三方单号，可选" class="" cssStyle="width: 230px;"/>
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
					<div class="control-group" style="display: ${showReceive?'':'none'}">
						<label class="control-label">可下单金额:</label>
						<div class="controls" >
							<c:set var="cbalance" value="${order.customerBalance - order.customerBlockBalance}" />
							<input type="text" id="balance" name="balance" class="input-mini" readonly="readonly" style="border-color:#b94a48;color:#b94a48;"
								   value="<fmt:formatNumber value="${cbalance}" pattern="0.00"/>" />
							<label style="margin-left: 20px">信用额度:</label>
							<input type="text" id="credit" name="credit" class="input-mini" readonly="readonly" style="border-color:#b94a48;color:#b94a48;" value="${order.customerCredit}" />
							<shiro:hasPermission name="fi:customercurrency:chargeonline">
								&nbsp;&nbsp;<a class="btn btn-primary" target="_blank" href="${ctx}/fi/customercurrency/chargeonline">充&nbsp;&nbsp;值</a>
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
					<div class="control-group" style="display: ${showReceive?'':'none'}">
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
				<div class="span4" style="width: auto;">
					<div class="control-group">
						<label class="control-label">购买店铺:</label>
						<div class="controls" >
							<select id="shopId" name="b2bShop.shopId" class="input-small" style="width:328px;">
								<option value="" data-channel="1" selected>请选择</option>
							</select>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">第三方单号:</label>
						<div class="controls" >
							<form:input path="b2bOrderNo" type="text" htmlEscape="false" maxlength="50" placeholder="第三方单号，可选" class="" cssStyle="width: 230px;"/>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span4" style="width: auto;">
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
	<!-- user -->
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
					<sys:newareaselect name="area.id" id="area" value="${order.area.id}" labelValue="${order.area.name}" labelName="area.name"
									title="" mustSelectCounty="true" cssClass="required" cssStyle="width: 248px;"
									callback='${order.urgentFlag == 1?"areaIdCallback":"restoreLocation"}'>
					</sys:newareaselect>
					<span class="add-on red">*</span>
				</div>
			</div>
		</div>
		<div class="span7" style="margin-left:5px;">
			<div class="control-group">
				<div class="controls" style="padding-left:0px;margin-left:0px;display:inherit;">
					<form:input path="address" htmlEscape="false" maxlength="150" class="required" placeholder="详细地址不包含省、市、区县" style="width:400px;" />
					<span class="add-on red">* 请确认识别的信息是否准确</span>
				</div>
			</div>
		</div>
	</div>
	<div class="row-fluid">
		<div class="span9" style="width: auto;">
			<div class="control-group">
				<label class="control-label">智能填写:</label>
				<div class="controls" >
					<textarea id="txtAddress" htmlEscape="false" rows="2" maxlength="100" class="input-xlarge" style="width: 733px;" placeholder="粘贴整段信息，自动识别姓名、电话和地址 例：张三，13800000000，广东省佛山市顺德区容桂文星路3号"></textarea>
					<input type="button" id="btnMatch" value="识别" class="btn btn-primary" style="width: 60px;"/>
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
					<th style="display: ${showReceive?'':'none'}">服务金额</th>
					<th style="display: ${showReceive?'':'none'}">冻结金额</th>
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
							<a id="orderForm_btn_delete" class="" href="javascript:;" style="margin-right:20px;" title="批量删除"><i class="icon-delete" style="margin-top: 0px;"></i></a>
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
				<tr id="trUrgentCharge" style="display: ${!showReceive || order.urgentFlag == 0?'none':''}" >
					<td style="text-align:right;">加急金额</td>
					<td  style="align:left;"><label id="lblUrgentCharge">${order.customerUrgentCharge}</label></td>
				</tr>
				<tr style="display: ${showReceive?'':'none'}">
					<td style="text-align:right;">数量总计</td>
					<td  style="align:left;"><label id="lbltotalQty">${order.totalQty}</label></td>
				</tr>
				<tr style="display: ${showReceive?'':'none'}">
					<td style="text-align:right;">总计</td>
					<td  style="align:left;"><label id="lbltotalCharge" class="alert ${(order.expectCharge+order.blockedCharge)>(order.customerBalance+order.customerCredit- order.customerBlockBalance)?'alert-error':'alert-success'}">${order.expectCharge+order.blockedCharge+order.customerUrgentCharge}</label></td>
				</tr>
			</table>
		</div>

	</div>
	<div class="row-fluid">
		<!-- 加急 -->
		<div class="span5 divUrgent" id="divUrgent" style="width: auto;<c:if test="${order.urgentFlag == 0}">display:none;</c:if>">
			<div  class="control-group">
				<label class="control-label" style="width: 80px;margin-top: 6px;">加急:</label>
				<div class="controls-urgent" style="margin-right: 100px;">
					<span><form:radiobutton path="urgentLevel.id" value="0" /><label for="urgentLevel.id1" style="color: red;">不加急</label></span>
					<c:if test="${order.urgentFlag == 1 && !empty urgentLevels}">
						<form:radiobuttons path="urgentLevel.id" items="${urgentLevels}" itemLabel="remarks" itemValue="id" htmlEscape="false" class="required"/>
					</c:if>
					<span class="help-inline alert" style="color: red;padding: 3px 3px;display:${showReceive?'':'none'}">
						加急费：<label id="lblUrgent" style="min-width:30px;display:inherit;">0.0</label> 元
					</span>
					<br>
					<span class="help-inline">
						注：在时效内完成的工单才会扣除加急费用!
					</span>
					<input type="hidden" id="urgentFlag" name="urgentFlag" value="${order.urgentFlag}" />
				</div>
			</div>
		</div>
		<div class="span5" style="width: auto;">
			<shiro:hasPermission name="sd:order:add">
				<c:choose>
					<c:when test="${!canCreateOrder}">
					</c:when>
					<c:when test="${(order.customerBalance + order.customerCredit - order.customerBlockBalance - order.expectCharge - order.blockedCharge) <= 0}">
						<input id="btnSubmit" name="btnSubmit" class="btn btn-danger" type="submit" disabled="disabled" value="保 存"></button>
					</c:when>
					<c:otherwise>
						<input id="btnSubmit" name="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>
					</c:otherwise>
				</c:choose>
			</shiro:hasPermission>
			&nbsp;&nbsp;<a id="btnCancel" class="btn" href="#" >返回</a>
		</div>
	</div>
</form:form>

<script class="removedscript" id="sc_AddItemRows" type="text/javascript">
    $(document).ready(function() {
        <c:if test="${ !currentuser.isCustomer() && order != null && order.customer !=null && order.customer.id != null && order.customer.id != 0}">
        Order.reload = true;
        $("#customerId").select2().val("${order.customer.id}").trigger('change');
        </c:if>
        //load items
        <c:if test="${canCreateOrder == true && order != null && order.customer !=null && order.customer.id != null && order.customer.id != 0}">
		Order.hideChargeColumn = ${!showReceive};
		Order.loadProductsNew(${order.customer.id});
        </c:if>
    });
</script>

<script type="text/javascript">
    top.layer.closeAll();
    var clickTag = 0;
    $(document).ready(function() {
		$('[data-toggle=tooltip]').darkTooltip();
        $("#inputForm").validate({
            submitHandler: function(form){
                if(clickTag == 1){
                    return false;
				}
				if ($("#subAreaId").val() == "") {
                    // console.log("手工只选择到3级就保存了。");
                    $("#subAreaId").val("1");   //首选4级区域
				}
				clickTag = 1;
                var $btnSubmit = $("#btnSubmit");
                $btnSubmit.attr('disabled', 'disabled');

                if(Utils.isEmpty($("[id='customer.id']").val())){
                    layerAlert("您的账号未关联客户，请联系管理员。","系统提示");
                    clickTag = 0;
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
                    clickTag = 0;
                    $btnSubmit.removeAttr('disabled');
                    return false;
                }

                var oid = $("#id").val();
                //check repeate create order
                var phone1 = $("#phone1").val();
                var phone2 = $("#phone2").val();

                if(Utils.isEmpty(phone1) && Utils.isEmpty(phone2)){
                    layerInfo("请输入用户电话。","信息提示");
                    clickTag = 0;
                    $btnSubmit.removeAttr('disabled');
                    return false;
                }else if(Utils.isEmpty(oid)){
                    //check repeate order when create order
                    var cid = $("[id='customer.id']").val();
                    var check_index = layerLoading("正在检查重复下单，请稍等...");
                    $.ajax({
                        type: "GET",
                        <%--url: "${ctx}/sd/order/checkrepeatorder?phone1="+phone1+"&phone2="+phone2+"&customerId=" + cid +"&t="+(new Date()).getTime(),--%>
                        url: "${ctx}/sd/order/checkrepeatorder?phone1=" + phone1 + "&customerId=" + cid + "&t="+(new Date()).getTime(),
                        data:"",
                        async: false,
                        complete: function () {
                            clickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                            top.layer.close(check_index);
                        },
                        success: function (data) {
                            if(ajaxLogout(data)){
                                return false;
                            }
                            var confirmClickTag = 0;
                            $("#repeateNo").val("");
                            if(data){
                                if (data.message != "OK"){
                                    layerError("检查重复下单发生错误：" + data.message + "，请重新保存。","错误提示");
                                    return false;
                                }
                                if(data.success == true && data.data != ""){
                                    $("#repeateNo").val(data.data);
                                    top.layer.confirm(
                                    	'<div><img src="${ctxStatic}/images/icon/icon-red-warning.png" style="width: 24px;margin-right: 8px;">请确认是否有<font color="red">重单</font>?</div><div style="margin-left:32px;">相关单号：<font color="red">' + data.data + '</font></div>'
                                        ,{
											title:'貌似重单了！'
											,icon: -1
											,closeBtn: 0
											,btn: ['继续下单', '取消']
											,cancel: function(index, layero){
                                                clickTag = 0;
                                                $btnSubmit.removeAttr('disabled');
                                            },success: function(layro, index) {
                                                $(document).on('keydown', layro, function(e) {
                                                    if (e.keyCode == 13) {
                                                        layro.find('a.layui-layer-btn0').trigger('click')
                                                    }else if(e.keyCode == 27){
                                                        clickTag = 0;
                                                        $btnSubmit.removeAttr('disabled');
                                                        top.layer.close(index);//关闭本身
                                                    }
                                                })
                                            }}
                                        ,function(index,layero){
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
                                            return false;
                                        },function(index){//cancel
											clickTag = 0;
                                            $btnSubmit.removeAttr('disabled');
                                        });
                                    return false;
                                }else{
                                    var actiontype="生成";
                                    if(!Utils.isEmpty($("#id").val())){
                                        actiontype="保存";
                                    }
                                    top.layer.confirm('确定要'+actiontype+'订单吗？'
										,{
                                        	icon: -1
											,title:'下单确认'
                                            ,cancel: function(index, layero){
                                                clickTag = 0;
                                                $btnSubmit.removeAttr('disabled');
                                            }
                                            ,success: function(layro, index) {
                                                $(document).on('keydown', layro, function(e) {
                                                    if (e.keyCode == 13) {
                                                        layro.find('a.layui-layer-btn0').trigger('click')
                                                    }else if(e.keyCode == 27){
                                                        clickTag = 0;
                                                        $btnSubmit.removeAttr('disabled');
                                                        top.layer.close(index);//关闭本身
                                                    }
                                                })
											}
										}, function(index,layero) {
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
											var loaindIndex = layerLoading('正在提交，请稍等...');
											form.submit();
											return false;
										},function(index) {
											clickTag = 0;
											$btnSubmit.removeAttr('disabled');
										});
										return false;
                                }
                            }
                            else{
                                layerError("检查重复下单发生错误，请重试。","错误提示");
                                return false;
                            }
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"检查重复下单发生错误，请重试!");
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
        $("#customerId").change(function(event) {
            var id = $(this).val();
            $("[id='customer.id']").val(id);
            $("[id='customer.name']").val($("#customerId option:selected").text());
			Order.hideChargeColumn = ${!showReceive};
            Order.loadProductsNew(id);
            event.preventDefault();//important
            //console.log('divUrgent:' + $("#divUrgent").is(":visible"));
			if(Order.reload == false) {//装载界面时不触发
                //加急
                setTimeout(function () {
                    Order.getUrgentCharge();
                }, 300);
            }
            Order.reload = false;//还原
            //console.log('divUrgent:' + $("#divUrgent").is(":visible"));
        });
        </c:if>

		$("#shopId").change(function(event) {
			var $selOption = $("#shopId").find("option:selected");
			var channel = $selOption.data("channel") || '1';
			$("#orderChannel").val(channel);
			event.preventDefault();//important
		});

        $("input:radio[name='urgentLevel.id']").change(function(event) {
            if (urgentFlag == 1) {
                Order.getUrgentCharge();
                //event.preventDefault();
            }
        });

        var tm = new Date().getTime();
        var url = "${ctx}/sd/order";
        if(Utils.isEmpty($("#id").val())){
            url=url+"/form?_t"+tm;
        }else{
            var refurl = document.referrer;
            var frompage ="";
            url = url + frompage+"?orderId="+$("#id").val()+"&_"+tm;
        }
        $("#btnCancel").prop('href',url);

        $("#orderForm_btn_delete").click(function(event) {
            if(clickTag == 1){
                return false;
			}
			clickTag = 1;
            Order.delProductRows();
        });

    });
    //选择区域的回调方法
    function areaIdCallback(id,name){
        if (urgentFlag = 1) {
            Order.getUrgentCharge(id);
        }
        if(ma){
            //人工
            restoreLocation('','');
		}
    }
</script>
</body>
</html>