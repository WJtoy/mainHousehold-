<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>修改导入订单</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/modules/sd/tpl/tempOrderEditForm.html" %>
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
	</style>
	<script type="text/javascript">
		var clickTag = 0;
		$(document).ready(function() {
		    top.layer.closeAll();
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
                        layerAlert("您的账号未设定客户，请联系管理员。","提示");
						return;
					}
					if($("#productTable tr:visible").length <= 1){
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
                        layerAlert("订单下未添加产品详细清单。","提示");
						return;
					}

					var oid = $("#id").val();
                    //check repeate create order
                    var phone1 = $("#phone1").val();
                    var phone2 = $("#phone2").val();

                    if(Utils.isEmpty(phone1) && Utils.isEmpty(phone2)){
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
                        layerInfo("请输入用户电话。","信息提示");
                        return false;
                    }else{
                        $("#repeateNo").val("");
                        //check repeate order when create order
                        var cid = $("[id='customer.id']").val();
                        var check_index = layerLoading("正在检查重复下单，请稍等...");
                        $.ajax({
                            type: "GET",
                            url: "${ctx}/sd/order/checkOrderBeforeTransfer?id=${order.id}&phone1="+phone1+"&phone2="+phone2+"&customerId=" + cid +"&t="+(new Date()).getTime(),
                            data:"",
                            async: false,
                            complete: function () {
                                top.layer.close(check_index);
                              	clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            },
                            success: function (data) {
                                if(ajaxLogout(data)){
                                    return false;
                                }
                                if(data){
                                    if (data.message != "OK"){
                                        layerError(data.message,"错误提示");
                                        return false;
                                    }
                                    var confirmClickTag = 0;
                                    if(data.success == true && data.data != ""){
                                        $("#repeateNo").val(data.data);
                                        top.layer.confirm(
                                            '请确认是否有重单，相关单号：' + data.data
											,{
                                                icon: 3
												,closeBtn: 0
												,title:'貌似重单了！'
                                                ,cancel: function(index, layero){
                                                    clickTag = 0;
                                                    $btnSubmit.removeAttr('disabled');
                                                },success: function(layro, index) {
													$(document).on('keydown', layro, function(e) {
														if (e.keyCode == 13) {
															layro.find('a.layui-layer-btn0').trigger('click');
														}else if(e.keyCode == 27){
															clickTag = 0;
															$btnSubmit.removeAttr('disabled');
															top.layer.close(index);//关闭本身
														}
                                                        if(e && e.preventDefault){
                                                            e.preventDefault();
                                                        }else{
                                                            window.event.returnValue=false;
                                                        };
														return false;
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
                                        	},function(index){//cancel
                                                clickTag = 0;
												$btnSubmit.removeAttr('disabled');
											});
                                        return false;
                                    }else{
                                        //not repeate
                                        top.layer.confirm(
                                            '确定保存订单吗？'
											,{
                                                icon: 3
												,title:'系统确认'
                                                ,cancel: function(index, layero){
                                                    clickTag = 0;
                                                    $btnSubmit.removeAttr('disabled');
                                                }
                                                ,success: function(layro, index) {
													$(document).on('keydown', layro, function(e) {
													    if (e.keyCode == 13) {
															layro.find('a.layui-layer-btn0').trigger('click');
														}else if(e.keyCode == 27){
															clickTag = 0;
															$btnSubmit.removeAttr('disabled');
															top.layer.close(index);//关闭本身
														}
                                                        if(e && e.preventDefault){
                                                            e.preventDefault();
                                                        }else{
                                                            window.event.returnValue=false;
                                                        }
														return false;
													})
                                                }
                                            }
											,function(index,layero) {
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
                                        	},function(index) {//cancel
                                                clickTag = 0;
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

        var ma = false;
/*        $(document).on("click", "#btnMatch", function () {
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
                url: "${ctx}/sys/area/da",
                data: dadata,
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success){
                        if (data.data.length !== 0){
                            $("#areaId").val(data.data[0]);
                            $("#areaName").val(data.data[1]);
                            $("#address").val(data.data[2]);
                            if (data.data[3] == "0"){
                                layerInfo("系统识别地址与填写地址不一致,请确认","信息提示");
                            }else{
                                //2019-04-15 经纬度
                                if(data.data.length === 8){
                                    $("#longitude").val(data.data[6]);
                                    $("#latitude").val(data.data[7]);
                                }
                            }
                            areaIdCallback(data.data[0],'');
                        }else{
                            $("#areaId").val(null);
                            $("#areaName").val("");
//                            $("#address").val("");
                            layerInfo("该信息暂时无法识别,请手动选择");
                            areaIdCallback('','');
                        }
                    }else{
                        $("#areaId").val(null);
                        $("#areaName").val("");
//                        $("#address").val("");
                        layerInfo("该信息暂时无法识别,请手动选择");
                        areaIdCallback('','');
                    }

                    ma = true;
                },
                error: function (e) {
                    //layerInfo("该信息暂时无法识别,请手动选择");
                    ajaxLogout(e.responseText,null,"该信息暂时无法识别,请手动选择!");
                }
            });

            $("#btnMatch").prop("disabled", false);
        });*/

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
		<li class="active"><a href="javascript:void(0);">手动转单</a></li>
	</ul>
	<sys:message content="${message}"/>
	<form:form id="inputForm" modelAttribute="order" action="${ctx}/sd/order/import/new/manualTransferOrder" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="updateDate"/>
		<form:hidden path="orderNo"/>
		<form:hidden path="customer.id" />
		<form:hidden path="status" />
		<form:hidden path="repeateNo" />
		<form:hidden path="customerOwner" />
		<form:hidden path="customerUrgentCharge" />
		<form:hidden path="engineerUrgentCharge" />
		<form:hidden path="longitude" />
		<form:hidden path="latitude" />
		<form:hidden path="category.id" />
		<form:hidden path="createBy.id" />
		<form:hidden path="subArea.id" id="subAreaId"/>
		<form:hidden path="createBy.name" />
		<c:set var="currentuser" value="${fns:getUser() }" />
		<!-- order head -->
		<legend>客户信息</legend>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">购买店铺:</label>
					<div class="controls">
						<form:select path="b2bShop.shopId" cssClass="input-medium" cssStyle="width: 240px;">
							<form:option value="" label="请选择"/>
							<form:options items="${b2bShopList}"
										  itemLabel="label" itemValue="value" htmlEscape="false" />
						</form:select>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">第三方单号:</label>
					<div class="controls" >
						<form:input path="b2bOrderNo" htmlEscape="false" maxlength="50" placeholder="第三方单号，可选" />
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
			<div class="span6">
				<div class="control-group">
					<label class="control-label">可下单金额:</label>
					<div class="controls" >
						<input type="text" id="balance" name="balance" class="input-mini" readonly="readonly" style="border-color:#b94a48;color:#b94a48;" value="${(empty order.customerBalance) ? 0 : order.customerBalance }" />
					<label >信用额度:</label>
						<input type="text" id="credit" name="credit" class="input-mini" readonly="readonly" style="border-color:#b94a48;color:#b94a48;" value="${order.customerCredit}" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">手机:</label>
					<div class="controls" >
						<form:input path="phone1" type="tel" htmlEscape="false" data-placement="right" maxlength="11" placeholder="第一联系电话，必填" class="required cellphone " /><span class="add-on red">*</span>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">座机:</label>
					<div class="controls" >
						<form:input path="phone2" htmlEscape="false" maxlength="16" placeholder="第二联系电话，可选" />
					</div>
				</div>
			</div>

		</div>
		<div class="row-fluid">
			<div class="span4" style="width: auto;">
				<div class="control-group">
					<label class="control-label">地址:</label>
					<div class="controls" >
						<%--<sys:areaselect name="area.id" id="area" value="${order.area.id}" labelValue="${order.area.name}" labelName="area.name"
										title="" mustSelectCounty="true" cssClass="required"
										callback='${order.urgentFlag == 1?"areaIdCallback":"restoreLocation"}'>
						</sys:areaselect>--%>
						<sys:newareaselect name="area.id" id="area" value="${order.area.id}" labelValue="${order.area.name}" labelName="area.name"
										   title="" tooltipPlacement="bottom" mustSelectCounty="true" cssClass="required">
						</sys:newareaselect>
						<span class="add-on red">*</span>
					</div>
				</div>
			</div>
			<div class="span7" style="margin-left:5px;">
				<div class="control-group">
					<div class="controls" style="padding-left:0px;margin-left:0px;display:inherit;">
						<form:input path="address" htmlEscape="false" maxlength="150" data-placement="bottom" class="required" style="width:350px;" />
						<span class="add-on red">* 详细地址不包含省、市、区县</span>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span9">
				<div class="control-group">
					<label class="control-label">智能填写:</label>
					<div class="controls" >
						<input type="text" id="txtAddress" class="input-xlarge" placeholder="粘贴完整地址,点击识别,系统自动识别省市区" style="width: 640px;" value="${order.address}"/>
						<input type="button" id="btnMatch" value="识别" class="btn btn-primary" style="width: 60px;"/>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span10">
				<div class="control-group">
					<label class="control-label">服务描述:</label>
					<div class="controls" >
						<form:textarea path="description" htmlEscape="false" rows="2" maxlength="255" class="input-xlarge" style="width:640px;" />
					</div>
				</div>
			</div>
		</div>
		<%--<div class="row-fluid">
			<div class="span8">
				<div class="control-group">
					<label class="control-label">第三方单号:</label>
					<div class="controls" >
						<form:input path="b2bOrderNo" htmlEscape="false" maxlength="50" placeholder="第三方单号，可选" />
					</div>
				</div>
			</div>
		</div>--%>
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
					<th>快递公司</th>
					<th>快递单号</th>
					<th>服务金额</th>
					<th>冻结金额</th>
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
						 <a id="orderForm_btn_add" class="" href="#" onclick="Order.editForm_loadProductsForIMP('${order.customer.id}',1,true);return false;"  style="margin-right:50px;"  title="添加产品"><i class="icon-add" style="margin-top: 0px;"></i></a>
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
						<c:choose>
							<c:when test="${order.urgentFlag == 1}">
								<tr id="trUrgentCharge">
							</c:when>
							<c:otherwise>
								<tr id="trUrgentCharge" style="display: none;">
							</c:otherwise>
						</c:choose>
						<td style="text-align:right;">加急金额</td>
						<td  style="align:left;"><label id="lblUrgentCharge">${order.customerUrgentCharge}</label></td>
					</tr>
					<tr>
						<td style="text-align:right;">数量总计</td>
						<td  style="align:left;"><label id="lbltotalQty">${order.totalQty}</label></td>
					</tr>
					<tr>
						<td style="text-align:right;">总计</td>
						<td  style="align:left;"><label id="lbltotalCharge" class="alert ${(order.expectCharge+order.blockedCharge)>order.customerBalance+order.customerCredit?'alert-error':'alert-success'}">${order.expectCharge+order.blockedCharge+order.customerUrgentCharge}</label>
						</td>
					</tr>
				</table>
			</div>
		</div>

		<div class="row-fluid">
			<!-- 加急 -->
			<div class="span5 divUrgent" id="divUrgent" style="width: auto;<c:if test="${order.urgentFlag == 0}">display:none;</c:if>">
				<div  class="control-group">
					<label class="control-label" style="width: 80px;">加急:</label>
					<div class="controls-urgent" style="margin-right: 10px;">
						<span><form:radiobutton path="urgentLevel.id" value="0" /><label for="urgentLevel.id1" style="color: red;">不加急</label></span>
						<c:if test="${order.urgentFlag == 1 && !empty urgentLevels}">
							<form:radiobuttons path="urgentLevel.id" items="${urgentLevels}" itemLabel="remarks" itemValue="id" htmlEscape="false" class="required"/>
						</c:if>
						<input type="hidden" id="urgentFlag" name="urgentFlag" value="${order.urgentFlag}" />
					</div>
				</div>
			</div>
			<div class="span5" style="width: auto;">
				<c:choose>
					<c:when test="${canCreateOrder == false || (order.expectCharge+order.blockedCharge)>(order.customerBalance+order.customerCredit) || (order.customerBalance+order.customerCredit) <= 0 }">
						<shiro:hasPermission name="sd:order:add"><button id="btnSubmit" name="btnSubmit" class="btn btn-danger disabled" type="submit" disabled="true" >保 存</button></shiro:hasPermission>
					</c:when>
					<c:otherwise>
						<shiro:hasPermission name="sd:order:add"><input id="btnSubmit" name="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/></shiro:hasPermission>
					</c:otherwise>
				</c:choose>
				&nbsp;&nbsp;<input id="btnCancel" class="btn" type="button" value="返回" onclick="history.go(-1)"/>
				</div>
		</div>
	</form:form>
	<script class="removedscript" id="sc_AddItemRows" type="text/javascript">
        var urgentFlag = ${order.urgentFlag};
		$(document).ready(function() {
            Order.rootUrl = "${ctx}";
            //load items
            Order.editForm_addItemRows(${fns:toGson(order)});
            <c:if test="${canCreateOrder == true && order != null && order.customer !=null && order.customer.id != null && order.customer.id != 0}">
            Order.editForm_loadProducts(${order.customer.id});
            </c:if>
            $("input:radio[name='urgentLevel.id']").change(function(event) {
                if (urgentFlag == 1) {
                    Order.getUrgentCharge();
                    //event.preventDefault();
                }
            });
        });
        //选择区域的回调方法
        function areaIdCallback(id,name){
            if (urgentFlag == 1) {
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