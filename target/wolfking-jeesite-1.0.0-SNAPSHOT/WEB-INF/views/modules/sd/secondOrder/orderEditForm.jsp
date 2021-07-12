<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>下单(灯饰)</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%
        response.setHeader("Cache-Control","no-store");
        response.setHeader("Pragrma","no-cache");
        response.setDateHeader("Expires",0);
    %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/SecondOrder.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <!-- image viewer -->
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/modules/sd/secondOrder/tpl/createOrderForm.html" %>
    <c:set var="currentuser" value="${fns:getUser() }" />
    <script type="text/javascript">
        SecondOrder.rootUrl = "${ctx}";
        SecondOrder.reload = false;
        SecondOrder.resetData();
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
    <li class="active"><a href="javascript:void(0);">修改订单[${order.orderNo}]</a></li>
</ul>
<input type="hidden" id="hasRepeatOrder" name="hasRepeatOrder" value="false" />
<sys:message content="${message}"/>
<c:set var="url" value="${ctx}/sd/order/second/save" />
<c:if test="${!canCreateOrder}">
    <c:set var="url" value="#" />
</c:if>
<form:form id="inputForm" modelAttribute="order" action="${url}" method="post" class="form-horizontal">
    <form:hidden path="id"/>
    <form:hidden path="updateDate"/>
    <form:hidden path="orderNo"/>
    <form:hidden path="actionType" />
    <form:hidden path="status" />
    <form:hidden path="quarter" />
    <form:hidden path="category.id" />
    <form:hidden path="dataSource.value"/>
    <form:hidden path="orderChannel" />
<%--    <form:hidden path="b2bShop.shopId" />--%>
    <form:hidden path="subArea.id" id="subAreaId"/>
    <form:hidden path="version" />
    <form:hidden path="customerUrgentCharge" />
    <form:hidden path="engineerUrgentCharge" />
    <input type="hidden" id="action" name="action" value="editv2" />
    <!-- order head -->
    <legend><span>客户信息</span></legend>
    <!-- customer -->
    <div class="row-fluid">
        <div class="span4">
            <div class="control-group">
                <label class="control-label"><span class="form-required">*</span>客户:</label>
                <div class="controls">
                    <input type="hidden" id="customer.id" name="customer.id" value="${order.customer.id}" maxlength="50"/>
                    <input type="text" id="customer.name" name="customer.name" readonly="readonly" value="${order.customer.name}" class="input-block-level" maxlength="100"/>
                </div>
            </div>
        </div>
        <div class="span4">
            <div class="control-group">
                <label class="control-label">购买店铺:</label>
                <div class="controls">
                    <select id="shopId" name="b2bShop.shopId" class="input-block-level">
                        <option value="" data-channel="1" selected>请选择</option>
                    </select>
                </div>
            </div>
        </div>
        <div class="span4">
            <div class="control-group">
                <label class="control-label">第三方单号:</label>
                <div class="controls">
                    <form:input path="b2bOrderNo" type="text" htmlEscape="false" maxlength="30" placeholder="第三方单号，可选"
                                cssClass="input-block-level" />
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid" style="display: ${showReceive?'':'none'}">
        <div class="span12">
            <span style="margin-left: 100px;">可下单金额：</span>
            <c:set var="cbalance" value="${order.customerBalance - order.customerBlockBalance}"/>
            <label id="balance" name="balance" class="amount-highlight" ><fmt:formatNumber value="${cbalance}" pattern="0.00"/></label><span class="amount-highlight">元</span>
            <span>信用额度:</span>
            <label id="credit" name="credit" class="amount-highlight"><fmt:formatNumber value="${order.customerCredit}" pattern="0.00"/></label><span class="amount-highlight">元</span>
            <c:if test="${currentuser.isCustomer()}">
                <shiro:hasPermission name="fi:customercurrency:chargeonline">
                    &nbsp;&nbsp;<a class="btn btn-mini btn-primary" target="_blank" href="${ctx}/fi/customercurrency/chargeonline">在线充值</a>
                </shiro:hasPermission>
            </c:if>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span4">
            <div class="control-group">
                <label class="control-label"><span class="form-required">*</span>联系人:</label>
                <div class="controls">
                    <form:input path="userName" htmlEscape="false" maxlength="100" class="required userName input-block-level"/>
                </div>
            </div>
        </div>
        <!-- user -->
        <div class="span4">
            <div class="control-group">
                <label class="control-label"><span class="form-required">*</span>手机:</label>
                <div class="controls" >
                    <form:input path="phone1" type="tel" htmlEscape="false" maxlength="11" placeholder="请输入手机号码" class="required mobile input-block-level" />
                </div>
            </div>
        </div>
        <div class="span4">
            <div class="control-group">
                <label class="control-label">座机:</label>
                <div class="controls" >
                    <form:input path="phone2" htmlEscape="false" maxlength="16" class="phone" cssClass="input-block-level"/>
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span4">
            <div class="control-group">
                <label class="control-label"><span class="form-required">*</span>地址:</label>
                <div class="controls" >
                    <sys:newareaselect name="area.id" id="area" value="${order.area.id}" labelValue="${order.area.name}" labelName="area.name"
                                       title="" mustSelectCounty="true" cssClass="required" cssStyle="width:230px;"
                                       callback='${order.urgentFlag == 1?"areaIdCallback":"restoreLocation"}'>
                    </sys:newareaselect>
                </div>
            </div>
        </div>
        <div class="span8">
            <div class="control-group">
                <div class="controls" style="padding-left:0px;margin-left:0px;display:inherit;">
                    <form:input path="address" htmlEscape="false" maxlength="150" class="required input-block-level" placeholder="详细地址不包含省、市、区县" />
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span12">
            <div class="control-group">
                <label class="control-label">智能填写:</label>
                <div class="controls" >
                    <div class="address-append">
                        <input type="text" id="txtAddress" class="address-append-input" htmlEscape="false" maxlength="100" placeholder="粘贴地址信息，可自动识别或填写，如：张三，13800000000，广东省佛山市顺德区容桂文星路3号" />
                        <input type="button" id="btnMatch" value="识别" class="btn btn-primary" style="width: 60px;"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span12">
            <div class="control-group">
                <label class="control-label">服务描述:</label>
                <div class="controls" >
                    <form:textarea path="description" htmlEscape="false" rows="2" maxlength="250" class="input-block-level" />
                </div>
            </div>
        </div>
    </div>
    <!-- order body -->
    <legend><span>产品信息</span></legend>
    <!-- product select card-->
    <div class="row-fluid product-selector" id="divProductSelectCard">
        <div class="span12">
            <div class="row">
                <div class="span9 left-content">
                    <div class="row">
                        <div class="span6">
                            <div class="control-group">
                                <label class="control-label"><span class="form-required">*</span>分类:</label>
                                <div class="controls">
                                    <select id="productType" name="productType" class="input-block-level">
                                        <option value="0" selected="selected">请选择</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="span6">
                            <select id="productTypeItem" name="productTypeItem" class="input-block-level">
                                <option value="0" selected="selected">请选择</option>
                            </select>
                        </div>
                    </div>
                    <div class="row">
                        <div class="span12">
                            <div class="control-group">
                                <label class="control-label">产品规格:</label>
                                <div class="controls">
                                    <select id="productSpec" name="productSpec" class="input-block-level">
                                        <option value="0" selected="selected">请选择</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="span6">
                            <div class="control-group">
                                <label class="control-label"><span class="form-required">*</span>产品:</label>
                                <div class="controls">
                                    <select id="product" name="product" class="input-block-level">
                                        <option value="0" selected="selected">请选择</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="span6">
                            <div class="control-group">
                                <label class="control-label">品牌:</label>
                                <div class="controls" id="divBrand">
                                    <select id="brand" name="brand" class="model input-block-level">
                                        <option value="0" selected="selected">请选择</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="span6">
                            <div class="control-group">
                                <label class="control-label"><span class="form-required">*</span>服务类型:</label>
                                <div class="controls">
                                    <select id="serviceType" name="serviceType" class="input-block-level">
                                        <option value="0" selected="selected">请选择</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="span6">
                            <div class="control-group">
                                <label class="control-label"><span class="form-required">*</span>数量:</label>
                                <div class="controls">
                                    <input type="number" id="qty" name="qty" value="1" min="1" class="input-block-level">
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="span6">
                            <label class="control-label">快递公司:</label>
                            <div class="controls">
                                <select id="expressCompany" name="expressCompany" class="input-block-level">
                                    <option value="0" selected="selected">请选择</option>
                                </select>
                            </div>
                        </div>
                        <div class="span6">
                            <label class="control-label">快递单号:</label>
                            <div class="controls">
                                <input type="text" id="expressNo" name="expressNo" value="" maxlength="20" class="input-block-level">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="span3 gellery-selector">
                    <div class="upload">
                        <input type="hidden" id="maxSelectGalleryQty" name="maxSelectGalleryQty" value="${createOrderConfig.secondType.maxSelectQty}" />
                        <input type="hidden" id="selectedQty" name="selectedQty" value="0" />
                        <div class="row">产品图片(最多3张):</div>
                        <div class="row">
                            <div id="divUploadFile" class="upload_warp">
                                <div id="btnSelectFile" class="upload_warp_left" onclick="SecondOrder.selectGallery();">
                                    <a href="javascript:;" class="badge retain-qty" data-toggle="tooltip" data-tooltip="还可上传图片数量">${createOrderConfig.secondType.maxSelectQty}</a>
                                </div>
                                <div class="upload_warp_right upload_warp_img" style="display: none;"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row command-content">
                <div class="span12" align="right" style="padding-right: 20px;padding-bottom: 2px;">
                    <a id="btnItemSubmit" class="btn btn-primary" style="margin-right: 10px;" href="javascript:;">确认</a>
                    <a id="btnItemRest" class="btn" href="javascript:;">重置</a>
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="control-group">
            <table id="productTable" class="table table-striped table-bordered table-condensed" style="margin-bottom: 0px;">
                <thead>
                <tr>
                    <th width="10px"><input type="checkbox" id="selectAll" name="selectAll"/></th>
                    <th width="30px">序号</th>
                    <th>分类</th>
                    <th width="150px">产品规格</th>
                    <th>产品</th>
                    <th width="60px">品牌</th>
                    <th width="80px">服务类型</th>
                    <th width="100px">快递公司</th>
                    <th width="120px">快递单号</th>
                    <th width="60px">数量</th>
                    <th width="60px" style="display: ${showReceive?'':'none'}">服务金额</th>
                    <th width="60px" style="display: ${showReceive?'':'none'}">冻结金额</th>
                </thead>
                <tbody>
                <tr id="tr_summry">
                    <td colspan="12" style="text-align: right;margin-right: 10px;line-height: 24px;">
                        <div class="row">
                            <div class="span2" style="float: left;margin: 10px 0px 0px 0px;">
                                <a id="orderForm_btn_delete" class="" href="javascript:;" title="批量删除"><i class="icon-delete-item" style="margin-top: 0px;"></i></a>
                            </div>
                            <div class="span10" style="float:right;padding-right: 20px;color:#000;">
                                共<label id="lbltotalQty">${order.totalQty}</label>件商品<span style="display: ${showReceive?'':'none'}">，合计：</span><label id="lbltotalCharge" style="display: ${showReceive?'':'none'}" class="amount-highlight">￥${order.expectCharge+order.blockedCharge+order.customerUrgentCharge}</label>
                                <span class="help-block" style="color:#000;display: ${showReceive?'':'none'}">(含服务金额：￥<label id="lblassignedCharge">${order.expectCharge }</label>，冻结金额：￥<label id="lblblockedCharge">${order.blockedCharge }</label>，加急金额：￥<label id="lblUrgentCharge">${order.customerUrgentCharge}</label> )</span>
                            </div>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
    <!-- 加急 -->
    <div class="row-fluid">
        <input type="hidden" id="urgentFlag" name="urgentFlag" value="${order.urgentFlag}" />
        <div class="span8 divUrgent" id="divUrgent" style="width: auto;<c:if test="${order.urgentFlag == 0}">display:none;</c:if>">
            <div class="row">
                <label class="checkbox inline">加急:</label>
                <c:if test="${order.urgentLevel != null}">
                <label class="checkbox inline">
                    <form:radiobutton path="urgentLevel.id" value="${order.urgentLevel.id}" label="${order.urgentLevel.remarks}" />
                </label>
                </c:if>
                <span class="help-inline alert-input" style="padding: 3px 3px;margin-left:10px;display:${showReceive?'':'none'}">
						加急费：<label id="lblUrgent" style="min-width:30px;display:inherit;text-align: right;">${fns:formatDouble(order.customerUrgentCharge,1)}</label> 元
                </span>
            </div>
            <span class="help-block" style="margin-left: 40px;">
				注：在时效内完成的工单才会扣除加急费用!
			</span>
        </div>
        <div class="span4" style="width: auto;float: right;padding-right: 20px">
            <shiro:hasPermission name="sd:order:add">
                <c:choose>
                    <c:when test="${!canCreateOrder}">
                        <input id="btnSubmit" name="btnSubmit" class="btn btn-danger" type="submit" disabled="disabled" style="margin-right: 3px;"  value="保存"></button>
                    </c:when>
                    <c:when test="${(order.customerBalance + order.customerCredit - order.customerBlockBalance - order.expectCharge - order.blockedCharge) <= 0}">
                        <input id="btnSubmit" name="btnSubmit" class="btn btn-danger" type="submit" disabled="disabled" style="margin-right: 3px;"  value="保存"></button>
                    </c:when>
                    <c:otherwise>
                        <input id="btnSubmit" name="btnSubmit" class="btn btn-primary" type="submit" value="保存"/>
                    </c:otherwise>
                </c:choose>
            </shiro:hasPermission>
            &nbsp;&nbsp;<a id="btnRest" class="btn" href="javascript:;">重置</a>
        </div>
    </div>
</form:form>

<script class="removedscript" id="sc_AddItemRows" type="text/javascript">
    $(document).ready(function() {
        SecondOrder.hideChargeColumn = ${!showReceive};
        SecondOrder.editFormShowItemRows(${fns:toGson(order)});
        <c:if test="${canCreateOrder == true && order != null && order.customer !=null && order.customer.id != null && order.customer.id != 0}">
        SecondOrder.loadCustomerInfoWhenEdit(${order.customer.id},'${order.b2bShop.shopId}',true);
        </c:if>
    });
</script>

<script type="text/javascript">
    top.layer.closeAll();
    var clickTag = 0;
    var loaindIndex = 0;
    $(document).ready(function() {
        // $('[data-toggle=tooltip]').darkTooltip();
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
                }
                var confirmClickTag = 0;
                top.layer.confirm('确定保存订单吗？'
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
                        loaindIndex = layerLoading('正在提交，请稍等...');
                        form.submit();
                        return false;
                    },function(index) {
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

        $("input:radio[name='urgentLevel.id']").change(function(event) {
            if (urgentFlag == 1) {
                SecondOrder.getUrgentCharge();
                //event.preventDefault();
            }
        });

        $("#shopId").change(function(event) {
            var $selOption = $("#shopId").find("option:selected");
            var channel = $selOption.data("channel") || '1';
            $("#orderChannel").val(channel);
            event.preventDefault();//important
        });

        $("#orderForm_btn_delete").click(function(event) {
            if(clickTag == 1){
                return false;
            }
            clickTag = 1;
            SecondOrder.delProductRows();
        });

        $("#btnItemRest").click(function(event) {
            SecondOrder.resetProductSelectCard();
        });

        //删除图片
        $("a.upload_warp_img_div_del").off().on("click",SecondOrder.removeSelectGallery);
    });
    //选择区域的回调方法
    function areaIdCallback(id,name){
        if (urgentFlag = 1) {
            Order.getUrgentCharge(id);
        }
        if(ma){//人工
            restoreLocation('','');
        }
    }
</script>
</body>
</html>