<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<link rel="stylesheet" type="text/css" href="${ctxStatic}/layui/css/layui.css">
	<script src="${ctxStatic}/layui/layui.js"></script>
	<script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
	<title>服务价格</title>
	<meta name="decorator" content="default" />
	<style>
		#editBtn{
			position: fixed;
			left: 0px;
			bottom: 5px;
			width: 100%;
			height: 50px;
			background: #fff;
			z-index: 10;
			border-top: 1px solid #e5e5e5;
			text-align: right;
		}
		.table td{
			height: 50px!important;
		}
		.use_price{
			margin-top: -4px;
			border-radius: 4px;
			border: 1px solid;
			border-color: #0096DA;
			background-color: #0096DA;
			width: 120px;
			height: 32px;
			color: #FFFFFF;
		}
		.price{
			text-align: center!important;
			width: 50px!important;

		}
		.error {
			background-color: #FEEEEE!important;
			text-align: center!important;
			vertical-align: middle!important;
			color: #F54142!important;
		}
	</style>
<script type="text/javascript">
    var this_index = top.layer.index;
    var clickTag = 0;
    var $btnSubmit = $("#btnSubmit");
    $(document).ready(function() {
        var form;
        layui.use('form', function () {
            var form = layui.form,
                $ = layui.$;

            form.render();

            form.on('checkbox(all)', function (data) {
                if (data.elem.checked) {
                    $(":checkbox[name='serviceType_line']").attr("checked", "checked");
                    form.render();
                } else {
                    $(":checkbox[name='serviceType_line']").removeAttr("checked");
                    form.render();
                }
            });
        });

		$("#inputForm").validate({
			submitHandler: function (form) {
                clickTag = 1;
                var loadingIndex;

                var options = {
                    url: "${ctx}/md/customer/price/saveProductPricesNew?qCustomerId=${customerId}&qCustomerName=${fns:urlEncode(customerName)}&qProductCategoryId=${productCategoryId}&qProductCategoryName=${fns:urlEncode(productCategoryName)}&qProductId=${productId}&qProductName=${fns:urlEncode(productName)}&qFirstSearch=${qFirstSearch}",
                    type: 'post',
                    dataType: 'json',
                    data:$(form).serialize(),
                    beforeSubmit: function(formData, jqForm, options){
                        loadingIndex = layer.msg('正在提交，请稍等...', {
                            icon: 16,
                            time: 0,
                            shade: 0.3
                        });
                        return true;
                    },// 提交前的回调函数
                    success:function (data) {
                        // 提交后的回调函数
                        if(loadingIndex) {
                            layer.close(loadingIndex);
                        }
                        if(ajaxLogout(data)){
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            return false;
                        }
                        if (data.success) {
                            layerMsg(data.message);
                            setTimeout(function () {
                                cancel();
                                // loading('同步中...');
                            }, 2000);
                            var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                            if(pframe){
                                pframe.repage();
                            }
                        } else {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            layerError("数据保存错误:" + data.message, "错误提示");
                        }
                        return false;
                    },
                    error: function (data) {
                        setTimeout(function () {
                            clickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                        }, 2000);
                        ajaxLogout(data,null,"数据保存错误，请重试!");
                    },
                };
                $("#inputForm").ajaxSubmit(options);
			},
			errorContainer: "#messageBox",
			errorPlacement: function (error, element) {
				$("#messageBox").text("输入有误，请先更正。");
				if (element.is(":checkbox")
					|| element.is(":radio")
					|| element.parent().is(
						".input-append")) {
					error.appendTo(element.parent()
						.parent());
				} else {
					error.insertAfter(element);
				}
			}
		});

        $('.editImg').on({
            mouseenter:function(){
                var that = this;
                tips =layer.tips("<span style='color:#fff;'>修改</span>",that,{tips:[2,'#3E3E3E'],time:0,area: 'auto',maxWidth:500});
            },
            mouseleave:function(){
                layer.close(tips);
            }
        });

        $('.saveImg').on({
            mouseenter:function(){
                var that = this;
                tips =layer.tips("<span style='color:#fff;'>保存</span>",that,{tips:[2,'#3E3E3E'],time:0,area: 'auto',maxWidth:500});
            },
            mouseleave:function(){
                layer.close(tips);
            }
        });

	});

    function editImg(index){
		$("#tr_"+index+" .editImg").hide();
        $("#tr_"+index+" .saveImg").show();
        $("#tr_"+index+" .price").prop("readonly", false);
	}

	// 单个保存
	function saveImg(line){
        var price = $("#tr_"+line+" .p").val();// 价格
        var discountPrice = $("#tr_"+line+" .d").val();// 优惠价
		var blockedPrice = $("#tr_"+line+" .b").val();// 冻结价
		var priceId = $("#tr_"+line+" #priceId").val();
        var customerId = $("#customerId").val();
        var productId = $("#productId").val();
        var customerName = $("#customerName").val();
        var productName = $("#productName").val();
        var serviceTypeName = $("#tr_"+line+" #serviceTypeName").val();
        var serviceTypeId = $("#tr_"+line+" #serviceTypeId").val();
        var priceType = $("#priceType").val();

		var entity = {};
        entity['id'] = priceId;
        entity['price'] = price;
        entity['discountPrice'] = discountPrice;
        entity['blockedPrice'] = blockedPrice;
        entity['serviceType.id'] = serviceTypeId;
        entity['serviceType.name'] = serviceTypeName;
        entity['customer.id'] = customerId;
        entity['customer.name'] = customerName;
        entity['product.id'] = productId;
        entity['product.name'] = productName;
        entity['priceType.value'] = priceType;
        layer.confirm(
            '是否保存对【'+serviceTypeName+'】的服务价格修改',
            {
                btn: ['保存','取消'], //按钮
                title:'提示'
            }, function(index){
                layer.close(index);//关闭本身
                var loadingIndex = top.layer.msg('正在保存，请稍等...', {
                    icon: 16,
                    time: 0,//不定时关闭
                    shade: 0.3
                });
                $.ajax({
                    url: "${ctx}/provider/md/customerPrice/saveNew",
					data : entity,
					type : "POST",
                    dataType: "JSON",
                    success:function (data) {
                        // 提交后的回调函数
                        if(loadingIndex) {
                            setTimeout(function () {
                                layer.close(loadingIndex);
                            }, 2000);
                        }
                        if (data.success) {
                            $("#tr_"+line+" .price").prop("readonly", true);
                            $("#tr_"+line+" .editImg").show();
                            $("#tr_"+line+" .saveImg").hide();

                            $("#tr_"+line+" .label").attr("class","label label-warning");
                            $("#tr_"+line+" .label").text("待审核");
                            $("#tr_"+line+" .switch-on").hide();

                            layerMsg(data.message);
                        } else {
                            layerError("保存失败:" + data.message, "错误提示");
                        }
                        return false;
                    },
                    error: function (data) {
                        ajaxLogout(data,null,"数据保存错误，请重试!");
                    },
                });
                return false;
            }, function(){
                // 取消操作
            });
	}

    function disableOrEnable(obj, line){
        var serviceTypeName = $("#tr_"+line+" #serviceTypeName").val();
        var priceId = $("#tr_"+line+" #priceId").val();
        var flag = $("#tr_"+line+" .delFlag").val();
        var content;
        var url;
        var msg;
        var title;
        // 停用
        if (flag == 1) {
            title = "启用";
            content = '确认要启用【'+serviceTypeName+'】价格吗？';
            url = "${ctx}/provider/md/customerPrice/activeNew?id="+ priceId;
            msg = '正在启用价格，请稍等...'
		} else {
            title = "停用";
            content = '确认要停用【'+serviceTypeName+'】价格吗？';
            url = "${ctx}/provider/md/customerPrice/deleteNew?id="+ priceId;
            msg = '正在停用价格，请稍等...'
		}
        layer.confirm(
            content,
            {
                btn: ['确定','取消'], //按钮
                title:'提示',
				cancel: function(index, layero){
                    // 右上角叉
                    if ($(obj).attr("class") == 'switch-off') {
                        honeySwitch.showOn(obj);
                    } else {
                        honeySwitch.showOff(obj);
                    }
				}
            }, function(index){
                layer.close(index);//关闭本身
                var loadingIndex = top.layer.msg(msg, {
                    icon: 16,
                    time: 0,//不定时关闭
                    shade: 0.3
                });
                $.ajax({
                    url: url,
                    success:function (data) {
                        // 提交后的回调函数
                        if(loadingIndex) {
                            setTimeout(function () {
                                layer.close(loadingIndex);
                            }, 2000);
                        }
                        if (data.success) {
                            layerMsg(data.message);

							if (flag == 1) {
							    // 停用切换为启用
                                $("#tr_"+line+" .price").prop("readonly", true);
                                $("#tr_"+line+" .editImg").show();
                                $("#tr_"+line+" .price").css("color", "");
                                $("#tr_"+line+" .label").attr("class","label label-success");
                                $("#tr_"+line+" .label").text("已审核");
                                $("#tr_"+line+" .delFlag").attr("value", 0);

                                $("#tr_"+line).find("input[type=checkbox]").prop("disabled", false);
                                $("#tr_"+line).find("input[type=checkbox]").attr("name", "serviceType_line");
                                viewRendering();
							} else {
							    // 启用切换为停用
                                $("#tr_"+line+" .price").prop("readonly", true);
                                $("#tr_"+line+" .price").css("color", "#C5C8CE");
                                $("#tr_"+line+" .editImg").hide();
                                $("#tr_"+line+" .saveImg").hide();
                                $("#tr_"+line+" .label").attr("class","label");
                                $("#tr_"+line+" .label").text("停用");
                                $("#tr_"+line+" .delFlag").attr("value", 1);

                                $("#tr_"+line).find("input[type=checkbox]").prop("disabled", true);
                                $("#tr_"+line).find("input[type=checkbox]").attr("name", "serviceType_line_stop");
                                $("#tr_"+line).find("input[type=checkbox]").removeAttr("checked");
                                viewRendering();
							}
                        } else {
                            layerError("服务价格"+title+"失败:" + data.message, "错误提示");
                            // 取消操作
                            if ($(obj).attr("class") == 'switch-off') {
                                honeySwitch.showOn(obj);
                            } else {
                                honeySwitch.showOff(obj);
                            }
                        }
                        return false;
                    },
                    error: function (data) {
                        ajaxLogout(data,null,"数据保存错误，请重试!");
                        // 取消操作
                        if ($(obj).attr("class") == 'switch-off') {
                            honeySwitch.showOn(obj);
                        } else {
                            honeySwitch.showOff(obj);
                        }
                    },
                });
                return false;
            }, function(){
                // 取消操作
				if ($(obj).attr("class") == 'switch-off') {
                    honeySwitch.showOn(obj);
				} else {
                    honeySwitch.showOff(obj);
				}
            });
    }

    function useStandardPrice(){
        var serviceType_checked = $("input[type='checkbox']:checked").length;
        if (serviceType_checked == 0) {
            layerError("请先选中您要修改的服务项目", "错误提示");
            return false;
		}
        var v = $("#v").val();
        var confirmText;
        if (v != '') {
            confirmText = '确认所选服务项目使用标准价【'+ v +'】吗？';
		} else {
            confirmText = '确认所选服务项目使用产品标准价吗？';
		}

        var customerId = $("#customerId").val();
        var productId = $("#productId").val();
        var serviceTypeIdArr = [];
        $("input[type='checkbox'][name='serviceType_line']:checked").each(function (index, element) {
            serviceTypeIdArr.push(parseInt($(this).val()));
        });

        var model = {};
        model['customerId'] = customerId;
        model['productId'] = productId;
        model['serviceTypeId'] = serviceTypeIdArr;
        layer.confirm(
			confirmText,
            {
                btn: ['确定','取消'], //按钮
                title:'使用标准价',
                area: ['395px', '180px']
            }, function(index){
                layer.close(index);//关闭本身
                var loadingIndex = top.layer.msg("正在恢复... 请稍等", {
                    icon: 16,
                    time: 0,//不定时关闭
                    shade: 0.3
                });

                $.ajax({
                    url: "${ctx}/provider/md/customerPrice/updateCustomizePriceFlag",
                    data : model,
                    type : "POST",
                    dataType: "JSON",
                    traditional:true,
                    success:function (data) {
                        // 提交后的回调函数
                        if(loadingIndex) {
                            setTimeout(function () {
                                layer.close(loadingIndex);
                            }, 2000);
                        }
                        if (data.success) {
                            layerMsg(data.message);
                            setTimeout(function () {
                                cancel();
                                loading('同步中...');
                            }, 2000);
                        } else {
                            layerError("保存失败:" + data.message, "错误提示");
                        }
                    },
                    error: function (data) {
                        layer.close(loadingIndex);
                        ajaxLogout(data,null,"数据保存错误，请重试!");
                    },
                });
                return false;
            }, function(){
                // 取消操作
            });
    }

    // 渲染
	function viewRendering(){
        layui.use('form', function () {
            var form = layui.form,
                $ = layui.$;

            form.render();
        });
	}
</script>
</head>
<body>

<input type="hidden" value="${useDefaultPrice}" id="priceType">
<input type="hidden" value="${fns:getDictLabelFromMS(useDefaultPrice,'PriceType','')}" id="v">
<form:form id="inputForm" modelAttribute="customerPrices" action="" method="post" class="form-horizontal">
	<sys:message content="${message}" />
	<c:if test="${canAction == true}">
		<div class="row-fluid" style="margin: 20px 0px 10px -91px;">
			<div class="span3">
				<div class="control-group">
					<label class="control-label">客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
					<div class="controls" style="margin-left: 160px;">
						<form:hidden path="customer.id" id="customerId"/>
						<form:input path="customer.name" id="customerName" readonly="true" cssClass="input-medium" cssStyle="width:180px;"/>
					</div>
				</div>
			</div>
			<div class="span3">
				<div class="control-group" style="margin-left: -15px;">
					<label class="control-label">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
					<div class="controls" style="margin-left: 160px;">
						<form:hidden path="product.id" id="productId"/>
						<form:input path="product.name" id="productName" readonly="true" cssClass="input-medium"  cssStyle="width:180px;"/>
					</div>
				</div>
			</div>
			<div class="span3">
				<div class="control-group" style="margin-left: -35px;">
					<label class="control-label">价格属性：</label>
					<div class="controls">
						<form:hidden path="customer.customizePriceFlag" />
						<input class="input-medium valid" style="width:180px;" readonly="readonly" type="text" value="${customer.customizePriceFlag == 0 ? '标准价':'自定义'}" aria-invalid="false">
					</div>
				</div>
			</div>
			<div class="span3">
				<div class="control-group">
					<label class="control-label">默认价格轮次：</label>
					<div class="controls">
						<form:hidden path="customer.useDefaultPrice" />
						<input class="input-medium valid" style="width:180px;" readonly="readonly" type="text" value="${fns:getDictLabelFromMS(useDefaultPrice,'PriceType','')}" aria-invalid="false">
					</div>
				</div>
			</div>
		</div>

		<%--<div class="alert alert-alert">提示：保内服务如价格或优惠价小于等于：0，不保存到数据库</div>--%>
		<div style="margin: 16px 0 16px 0;">
			<div>
				<button class="use_price" id="use_price" type="button" onclick="useStandardPrice()">
					<img src="${ctxStatic}/images/price01.png" style="width: 20px;height: 20px;">&nbsp;&nbsp;使用标准价
				</button>
			</div>
		</div>

<div id="divGrid" class="layui-form" style="overflow: auto;height:572px;">
<table id="contentTable" class="table table-bordered table-condensed" style="table-layout:fixed" cellspacing="0" width="100%">
    <thead>
    <tr>
		<th rowspan="2" style="width: 45px">
			<input type="checkbox" name="all" lay-filter="all" lay-skin='primary' title="">
		</th>
        <th rowspan="2" style="width: 115px;">服务项目</th>
        <th rowspan="2" style="width: 50px;">状态</th>
        <c:forEach items="${fns:getDictListFromMS('PriceType')}" var="dict">
            <th colspan="2" style="width: 135px;height: 40px;">${dict.label}</th>
        </c:forEach>
        <th colspan="4" style="width: 266px;height: 40px;">服务价格</th>
        <th rowspan="4" style="width: 100px;">启用</th>
    </tr>

    <tr>
        <c:forEach items="${fns:getDictListFromMS('PriceType')}" var="dict">
            <th width="70">价格</th>
            <th width="70">优惠价</th>
        </c:forEach>
        <th width="70">价格</th>
        <th width="70">优惠价</th>
        <th width="70">冻结金额</th>
        <th width="70">操作</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${customerPrices.prices}" var="price" varStatus="i" begin="0">
        <c:set var="index" value="${i.index}" />
		<c:set var="flag" value="${price.delFlag}"/>
		<%--是否新增标识--%>
		<c:set var="isNewPrice" value="${empty price.id}"/>
				<tr id="tr_${index}">
					<input type="hidden" value="${price.id}" id="priceId">
					<input type="hidden" value="${price.serviceType.id}" id="serviceTypeId">
					<input type="hidden" value="${price.serviceType.name}" id="serviceTypeName">
					<%--多选框--%>
					<td>
						<c:choose>
							<c:when test="${flag == 1}">
								<input type="checkbox" name="serviceType_line_stop" lay-filter="serviceType_line_stop" value="${price.serviceType.id}" disabled='disabled' lay-skin='primary' title="">
							</c:when>
							<c:otherwise>
								<input type="checkbox" name="serviceType_line" lay-filter="serviceType_line" value="${price.serviceType.id}" lay-skin='primary' title="">
							</c:otherwise>
						</c:choose>
					</td>
				<%--服务名称--%>
				<c:choose>
					<%--停用项--%>
					<c:when test="${price.delFlag==1}">
						<td>${price.serviceType.name}&nbsp;</td>
						<td><span class="label">停用</span></td>
					</c:when>
					<c:otherwise>
						<td>${price.serviceType.name}&nbsp;
							<input type="hidden" id="prices[${index}].id" name="prices[${index}].id" value="${price.id}" />
							<input type="hidden" id="prices[${index}].serviceType.id" name="prices[${index}].serviceType.id"
								   value="${price.serviceType.id}" />
							<input type="hidden" id="prices[${index}].serviceType.name" name="prices[${index}].serviceType.name"
								   value="${price.serviceType.name}" />
							<input type="hidden" id="prices[${index}].serviceType.warrantyStatus.value" name="prices[${index}].serviceType.warrantyStatus.value"
								   value="${price.serviceType.warrantyStatus.value}" />
							<input type="hidden" id="prices[${index}].delFlag" name="prices[${index}].delFlag" value="${price.delFlag}" />
						</td>
						<%--状态--%>
						<td>
							<c:if test="${not empty price.id && price.delFlag==0}"><span class="label label-success">已审核</span></c:if>
							<c:if test="${not empty price.id && price.delFlag==2}"><span class="label label-warning">待审核</span></c:if>
							<c:if test="${empty price.id}"><span class="label label-info">新增</span></c:if>
						</td>

					</c:otherwise>
				</c:choose>

				<c:forEach items="${fns:getDictListFromMS('PriceType')}" var="dict" varStatus="d">
					<c:set var="dValue" value="${dict.value}" />
						<%--轮次参考价格--%>
						<c:set var="priceHint" value="" />
						<c:set var="discountpriceHint" value="" />

						<c:forEach items="${price.productPriceList}" var="productPrice">
							<c:set var="pValue" value="${productPrice.priceType}" />
							<c:if test="${dValue == pValue}">
								<%--价格：0.0--%>
								<c:set var="priceHint" value="${priceHint}${productPrice.standPrice} " />
								<%--优惠价格：0.0--%>
								<c:set var="discountpriceHint" value="${discountpriceHint}${productPrice.discountPrice} " />
							</c:if>
						</c:forEach>
						<c:choose>
							<c:when test="${priceHint == '' && discountpriceHint == ''}">
								<td class="error" colspan="2">未维护</td>
							</c:when>
							<c:otherwise>
								<td style="text-align: center;vertical-align: middle;">
										<%--价格--%>
										${priceHint == '' ? '-' : priceHint}
								</td>
								<%--展示优惠价--%>
								<td style="text-align: center;vertical-align: middle;">
										${discountpriceHint == '' ? '-' : discountpriceHint}
								</td>
							</c:otherwise>
						</c:choose>
				</c:forEach>

					<c:choose>
						<%--停用--%>
						<c:when test="${price.delFlag==1}">
							<td>
								<input type="text" id="stop${index}.price" name="stop${index}.price"}
									   class="input-mini number price p" value="${price.price}" readonly="true" style="text-align: center;width: 50px;color: #C5C8CE" />
							</td>
							<td >
								<input type="text" id="stop${index}.discountPrice" name="stop${index}.discountPrice"
									   maxlength="7" class="input-mini number price d" value="${price.discountPrice}" readonly="true" style="text-align: center;width: 50px;color: #C5C8CE" />
							</td>
							<td >
								<input type="text" id="stop${index}.blockedPrice" name="stop${index}.blockedPrice"
									   class="input-mini number price b" value="${price.blockedPrice}" readonly="true" style="text-align: center;width: 50px;color: #C5C8CE" />
							</td>

							<%--操作--%>
							<td>
								<img src="${ctxStatic}/images/price03.png" style="width: 20px;height: 20px;display: none" onclick="editImg('${index}')" class="editImg">
								<img src="${ctxStatic}/images/price02.png" style="width: 20px;height: 20px;display: none" onclick="saveImg('${index}')" class="saveImg">
							</td>

							<td style="display:table-cell; vertical-align:middle">
								<span class="switch-off" style="zoom: 0.7" onclick="disableOrEnable(this,'${index}')"></span>
								<input type="hidden" value="1" class="delFlag">
							</td>

						</c:when>
						<%--启用--%>
						<c:otherwise>

							<c:set var="p_referPrice" value="${price.referPrice}"/>
							<c:set var="p_referDiscountPrice" value="${price.referDiscountPrice}"/>
							<c:set var="p_price" value="${price.price}"/>
							<c:set var="p_discountPrice" value="${price.discountPrice}"/>
							<c:choose>
								<c:when test="${price.serviceType.warrantyStatus.value eq 'IW'}">
									<td>
										<input type="text" id="prices[${index}].price" name="prices[${index}].price" <c:out value="${isNewPrice eq true ? '' : 'readonly'}"/> style="<c:out value='${p_referPrice != p_price ? "color:#F54142":""}'/>"
											   maxlength="7" class="input-mini required number price p" min="0.0" value="${price.price}"/>
									</td>
									<td >
										<input type="text" id="prices[${index}].discountPrice" name="prices[${index}].discountPrice" <c:out value="${isNewPrice eq true ? '' : 'readonly'}"/> style="<c:out value='${p_referDiscountPrice != p_discountPrice ? "color:#F54142":""}'/>"
											   maxlength="7" class="input-mini required number price d" min="0.0" comparePrice="[id='prices[${index}].price']"
											   value="${price.discountPrice}"/>
									</td>
									<td >
										<input type="text" id="prices[${index}].blockedPrice" name="prices[${index}].blockedPrice" <c:out value="${isNewPrice eq true ? '' : 'readonly'}"/>
											   maxlength="7" class="input-mini required number price b" min="0.0" value="${price.blockedPrice}"/>
									</td>
									<td >
										<label>
											<c:choose>
												<c:when test="${isNewPrice eq true}">
													<img src="${ctxStatic}/images/price02.png" style="width: 20px;height: 20px;" onclick="saveImg('${index}')" class="saveImg">
												</c:when>
												<c:otherwise>
													<img src="${ctxStatic}/images/price03.png" style="width: 20px;height: 20px;" onclick="editImg('${index}')" class="editImg">
													<img src="${ctxStatic}/images/price02.png" style="width: 20px;height: 20px;display: none" onclick="saveImg('${index}')" class="saveImg">
												</c:otherwise>
											</c:choose>
										</label>
									</td>
									<td style="display:table-cell; vertical-align:middle">
										<c:if test="${price.delFlag!=2}">
											<span class="switch-on"  style="zoom: 0.7" onclick="disableOrEnable(this,'${index}')"></span>
											<input type="hidden" value="0" class="delFlag">
										</c:if>
									</td>
								</c:when>
								<c:otherwise>
									<td>
										<input type="text" id="prices[${index}].price" name="prices[${index}].price" <c:out value="${isNewPrice eq true ? '' : 'readonly'}"/> style="<c:out value='${p_referPrice != p_price ? "color:#F54142":""}'/>"
											   maxlength="7" class="input-mini required number price p" min="0.0" value="${price.price}"/>
									</td>
									<td >
										<input type="text" id="prices[${index}].discountPrice" name="prices[${index}].discountPrice" <c:out value="${isNewPrice eq true ? '' : 'readonly'}"/> style="<c:out value='${p_referDiscountPrice != p_discountPrice ? "color:#F54142":""}'/>"
											   maxlength="7" class="input-mini required number price d" min="0.0"
											   value="${price.discountPrice}"/>
									</td>
									<td >
										<input type="text" id="prices[${index}].blockedPrice" name="prices[${index}].blockedPrice" <c:out value="${isNewPrice eq true ? '' : 'readonly'}"/>
											   maxlength="7" class="input-mini required number price b" min="0.0" value="${price.blockedPrice}"/>
									</td>
									<td >
										<label>
											<c:choose>
												<c:when test="${isNewPrice eq true}">
													<img src="${ctxStatic}/images/price02.png" style="width: 20px;height: 20px;" onclick="saveImg('${index}')" class="saveImg">
												</c:when>
												<c:otherwise>
													<img src="${ctxStatic}/images/price03.png" style="width: 20px;height: 20px;" onclick="editImg('${index}')" class="editImg">
													<img src="${ctxStatic}/images/price02.png" style="width: 20px;height: 20px;display: none" onclick="saveImg('${index}')" class="saveImg">
												</c:otherwise>
											</c:choose>
										</label>
									</td>
									<td style="display:table-cell; vertical-align:middle">
										<c:if test="${price.delFlag!=2}">
											<span class="switch-on"  style="zoom: 0.7" onclick="disableOrEnable(this,'${index}')"></span>
											<input type="hidden" value="0" class="delFlag">
										</c:if>
									</td>
								</c:otherwise>
							</c:choose>

						</c:otherwise>
					</c:choose>


				</tr>
			</c:forEach>
			</tbody>
		</table>
		</div>
		</c:if>

	<div id="editBtn">
		<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="cancel()" style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
	</div>

</form:form>

<script class="removedscript" type="text/javascript">
    $(document).ready(function() {
        $("th").css({"text-align":"center","vertical-align":"middle"});
        $("td").css({"text-align":"center","vertical-align":"middle"});
    });
    // 关闭页面
    function cancel() {
        top.layer.close(this_index);// 关闭本身
        loading('同步中...');
        var pframe = getActiveTabIframe();//定义在jeesite.min.js中
        if(pframe){
            pframe.repage();
        }
    }

</script>
</body>
</html>
