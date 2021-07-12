<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>订单配件申请列表</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default" />
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<c:set var="currentuser" value="${fns:getUser() }"/>
	<script type="text/javascript">
		Order.rootUrl = "${ctx}";
		var this_index = 0;
		<%String parentIndex = request.getParameter("parentIndex");%>
		var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
		if(parentIndex != ''){
			this_index = parentIndex;
		}
        var parentLayerIndex = parent.layer.getFrameIndex(window.name);
		var quarter = "";
		<c:if test="${quarquarterter != null}">
		quarter = ${quarter};
		</c:if>
		function reloadApproveList(){
			location.reload();
		}

		$(document).ready(function() {
            $('a[data-toggle=tooltipSouth]').darkTooltip({gravity: 'south'});
            $('a[data-toggle=tooltipWest]').darkTooltip({gravity: 'west'});
			if(this_index == 0){
				this_index = top.layer.index;
			}
			$("#treeTable").treeTable({expandLevel : 2});

            $("input[type='radio'][name^='isReturnBox']").change(function(){
                var isCheck = $(this).is(":checked");
                if(isCheck){
                    var itemId = $(this).data("itemid");
                    if($("#recycleBox_"+itemId).length>0){
                        var recycleIsCheck =$("#recycleBox_"+itemId).is(":checked")
                        if(recycleIsCheck){
                            $("#recycleBox_"+itemId).prop("checked",false);
                            $("#recycle_"+itemId).hide();
                            $("#recyclePrice_"+itemId).text("0.0");
                            var masterId = $(this).data("masterid");
                            addTotalPrice(masterId)
						}
                    }
				}
            });
            $("input[type='checkbox'][name='recycleBox']").change(function(){
                var isCheck = $(this).is(":checked");
                var itemId = $(this).data("itemid");
                var masterId = $(this).data("masterid");
                if(isCheck){
					$("input:radio[name='chk_" + itemId + "']").filter('[value=0]').prop('checked', true);
                    // $("[#chk_"+itemId).prop("checked",false);
                    var recycleUtilPrice = $("#recycleUtilPrice_"+itemId).text();
                    $("#recyclePrice_"+itemId).text(recycleUtilPrice);
                    $("#recycle_"+itemId).show();
                }else{
                    $("#recyclePrice_"+itemId).text("0.0");
                    $("#recycle_"+itemId).hide();
				}
                addTotalPrice(masterId)
            });
		});
		
		//计算总价
		function addTotalPrice(masterId) {
		    var totalRecyclePrice = 0;
			var totalApplyPrice = 0;
		    var totalPrice = 0;
		    //回收价格
            $("span[name='recyclePrice_" + masterId+"']").each(function() {
                var qty = $(this).data("qty");
                totalRecyclePrice=parseFloat(totalRecyclePrice)+(parseFloat($(this).text())*qty);
            });
            //申请价格
            $("span[name='price_" + masterId+"']").each(function() {
                var qty = $(this).data("qty");
                totalApplyPrice=parseFloat(totalApplyPrice)+(parseFloat($(this).text())*qty);
            });
            totalPrice = totalRecyclePrice+totalApplyPrice;
            $("#totalPrice_"+masterId).text(parseFloat(totalPrice).toFixed(2))
        }

		function closeme(){
			window.opener.repage();
			window.close();
		}

		function materialMasterApprove(masterId,orderId,btn,itemCount)
		{
			if(itemCount <= 0){
				layerAlert("配件无配件信息，不能审核通过。", "错误提示");
				return false;
			}
			//check returns
			/*
			var checkedCount = $("input[type='radio'][class='isReturn_" + masterId+"']:checked").length;
			if(checkedCount < itemCount){
				layerAlert("请选择并确认配件:【返件】，还是【不返件】！", "错误提示");
				return false;
			}*/
			var noCheckedCount = 0;
			$("table[id='tr_" + masterId + "']").find("td.td_return").each(function() {
				var $_item = $(this);
				$_item.removeClass("td-invalid");
				var itemCheckedCount = $_item.find("input[type='radio']:checked").length;
				if(itemCheckedCount == 0){
					$_item.addClass("td-invalid");
					noCheckedCount=noCheckedCount+1;
				}
			});
			if(noCheckedCount > 0){
				layerAlert("请选择并确认配件是否返件:【是】，还是【否】！", "错误提示");
				return false;
			}
			var $btn = undefined;
			if(btn){
				$btn = $("#" + btn);
				if($btn.prop("disabled") === true){
					return false;
				}else{
					$btn.prop("disabled", true);
					$btn.addClass("disabled");
				}
			}
			var confirmClickTag = 0;
			top.layer.open({
                type: 1
				,title:'系统确认'
                ,area:['640px', '408px']
				,zIndex : 19891015
                ,btn:['确认','取消']
                ,content: '<div style="margin-left: 80px;margin-top: 55px"><label class="control-label" style="float: left">备&nbsp;&nbsp;&nbsp;&nbsp;注：</label><div class="controls" style="float: left"><textarea id="approveRemark" style="width:400px;height: 176px" maxlength="150"></textarea></div></div>'
				,yes:function(index,layero){
                    if (confirmClickTag == 1){
                        return false;
                    }
                    var items = [];
                    var recycleItem = [];
                    $("input[type='radio'][class='isReturn_" + masterId+"']:checked").each(function() {
                        var itemId = $(this).data("itemid");
                        var checkValue = $(this).val();
                        if(checkValue == 1) {
                            items.push('' + itemId);
                        }
                    });
                    $("input[type='checkbox'][class='isRecycle_" + masterId+"']:checked").each(function() {
                        var itemId = $(this).data("itemid");
                        recycleItem.push('' + itemId);
                    });
                    /*if(items.join(",").length>0){
                        var areaId = $("#areaId_"+masterId).val();
                        var address = $("#address_"+masterId).text();
                        if(areaId==null || areaId=='' || areaId<=0 || address==null || address==''){
                            layerAlert("客户未设置返件地址,请去添加","提示");
                            $btn.removeAttr("disabled");
                            $btn.removeClass("disabled");
                            return false;
                        }
                    }*/
                    var btn0 = $(".layui-layer-btn0",layero);
                    if(btn0.hasClass("layui-btn-disabled")){
                        return false;
                    }
                    confirmClickTag = 1;
                    btn0.addClass("layui-btn-disabled").attr("disabled","disabled");
                    var loadingIndex = layer.msg('正在提交，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
                    top.layer.close(index);//关闭本身
                    var data = {};
                    data.masterId = masterId;
                    data.quarter = quarter;
                    data.isMaterialReturn = 0;
                    data.isMaterialRecycle = 0;
                    data.itemIds = items.join(",");
                    data.recycleItemIds = recycleItem.join(",")
					data.approveRemark = top.$("#approveRemark").val();
                    if(data.itemIds.length>0){
                        data.isMaterialReturn = 1;
                        /*data.userName = $("#receiveName_"+masterId).text();
                        data.contactInfo = $("#receivePhone_"+masterId).text();
                        data.address = $("#address_"+masterId).text();
                        data.provinceId = $("#provinceId_" + masterId).val();
                        data.cityId = $("#cityId_"+masterId).val();
                        data.areaId = $("#areaId_"+masterId).val();*/
                    }
                    if(data.recycleItemIds.length>0){
                        data.isMaterialRecycle = 1;
                    }
                    $.ajax({
                        async: false,
                        cache : false,
                        type : "POST",
                        url : "${ctx}/sd/material/materialApprove",
                        data : data,
                        beforeSend: function () {
                        },
                        complete: function () {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                        },
                        success : function(data) {
                            if(ajaxLogout(data)){
                                return false;
                            }
                            if (data.success)
                            {
                                reloadApproveList();
                            }else
                            {
                                if($btn){
                                    $btn.removeAttr("disabled");
                                    $btn.removeClass("disabled");
                                }
                                layerError("审核失败:" + data.message, "错误提示");
                            }
                        },
                        error : function(e)
                        {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            if($btn){
                                $btn.removeAttr("disabled");
                                $btn.removeClass("disabled");
                            }
                            ajaxLogout(e.responseText,null,"审核失败，请重试!");
                        }
                    });
                },btn2:function(index) {//cancel
                    if($btn){
                        $btn.removeAttr("disabled");
                        $btn.removeClass("disabled");
                    }
                },cancel: function(index, layero){
                    //点击右上角的X
                    if($btn){
                        $btn.removeAttr("disabled");
                        $btn.removeClass("disabled");
                    }
                }
			});
			return false;
		}

		function rejectForm(masterId,quarter){
			top.layer.open({
				type: 2,
				id: 'layer_material_reject',
				zIndex:19891015,
				title:'驳回配件单',
				content: '/sd/material/materialmasterreject?masterId=' + masterId + "&quarter=" + (quarter || ''),
				area: ['600px', '400px'],
				shade: 0.3,
				maxmin: false,
				success: function(layero,index){
					setCookie('layer.three.pid',this_index);
				},
				end:function(layero,index){}
			});
		}

		function materialMasterReject(masterId,orderId,btn)
		{
			var $btn = undefined;
			if(btn){
				$btn = $("#" + btn);
				if($btn.prop("disabled") === true){
					return false;
				}else{
					$btn.prop("disabled", true);
					$btn.addClass("disabled");
				}
			}
			var confirmClickTag = 0;
			top.layer.confirm('确定要 <font color="red">驳回</font> 该项申请配件吗?', {
				icon: 3
				,title:'系统确认'
				,cancel: function(index, layero){
					//点击右上角的X
					if($btn){
						$btn.removeAttr("disabled");
						$btn.removeClass("disabled");
					}
				}
			}, function(index,layero){
				var btn0 = $(".layui-layer-btn0",layero);
				if(btn0.hasClass("layui-btn-disabled")){
					return false;
				}
				if(confirmClickTag == 1){
					return false;
				}
				confirmClickTag = 1;
				btn0.addClass("layui-btn-disabled").attr("disabled","disabled");
				top.layer.close(index);//关闭本身
				var data = {masterId : masterId,orderId:orderId,quarter:quarter};
				$.ajax({
					async: false,
					cache : false,
					type : "POST",
					url : "${ctx}/sd/material/materialmasterreject",
					data : data,
					beforeSend: function () {
					},
					complete: function () {
						//console.log("" + new Date().getTime() + " [complete] clickTag:" + clickTag + " ,ajaxSuccess:" + ajaxSuccess);
					},
					success : function(data)
					{
						if(ajaxLogout(data)){
							return false;
						}
						if (data.success)
						{
							location.reload();
						}else{
							if($btn){
								$btn.removeAttr("disabled");
								$btn.removeClass("disabled");
							}
						}
					},
					error : function(e)
					{
						if($btn){
							$btn.removeAttr("disabled");
							$btn.removeClass("disabled");
						}
						ajaxLogout(e.responseText,null,"驳回失败，请重试!");
					}
				});
			},function(index) {//cancel
				if($btn){
					$btn.removeAttr("disabled");
					$btn.removeClass("disabled");
				}
			});
			return false;
		}

		function returnForm(id,formNo){
			top.layer.open({
				type: 2,
				id: 'layer_material_return',
				zIndex:19891015,
				title:'返件物流信息',
				content: '${ctx}/sd/material/return/form?materialMasterId=' + id + "&parentIndex=" + (this_index || '') + "&quarter=" + (quarter || '') +"&formNo="+formNo,
				area: ['1000px', '800px'],
				shade: 0.3,
				maxmin: false,
				success: function(layero,index){
					//console.log('materil approve list:' + this_index);
					setCookie('layer.parent.id',this_index);
				},
				end:function(layero,index){}
			});
		}

		function expressForm(id,formNo){
			top.layer.open({
				type: 2,
				id: 'layer_material_express',
				zIndex:19891015,
				title:'填写物流信息',
				content: '${ctx}/sd/material/expressForm?materialMasterId=' + id + "&parentIndex=" + (this_index || '') + "&quarter=" + (quarter || ''),
				area: ['1000px', '600px'],
				shade: 0.3,
				maxmin: false,
				success: function(layero,index){
					setCookie('layer.parent.id',this_index);
				},
				end:function(layero,index){}
			});
		}

		function closeForm(id,formNo){
			top.layer.open({
				type: 2,
				id: 'layer_material_close',
				zIndex:19891015,
				title:'关闭配件单',
				content: '${ctx}/sd/material/close?id=' + id + "&quarter=" + (quarter || ''),
				area: ['600px', '400px'],
				shade: 0.3,
				maxmin: false,
				success: function(layero,index){
					setCookie('layer.parent.id',this_index);
				},
				end:function(layero,index){}
			});
		}

		//设置返件收件地址
		function editReturnReceiveAddress(customerId,materialId) {
            top.layer.open({
                type: 2,
                id:'layer_editReceiveAddress',
                zIndex:19891016,
                title:'添加返件信息',
                content:"${ctx}sd/material/returnReceiveAddressForm?customerId="+ customerId + "&parentIndex=" + (parentLayerIndex || '') +"&materialId="+materialId,
                area: ['800px', '500px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){

                }
			})
        }

        function setReceiveData(data) {
		    var materialId = data.materialId;
		    $("#areaId_"+materialId).val(data.areaId);
            $("#cityId_"+materialId).val(data.receiveCityId);
            $("#provinceId_"+materialId).val(data.receiveProvinceId);
            $("#receiveName_"+materialId).text(data.receiveName);
            $("#receivePhone_"+materialId).text(data.receivePhone);
            $("#detailAddress_"+materialId).text(data.detailAddre);
            $("#address_"+materialId).text(data.address);
            $("#noAddressSpan_"+materialId).hide();
            $("#returnAddressDiv_"+materialId).show();
        }
	</script>
	<style type="text/css">
		.table thead th,.table tbody td {
			text-align: center;
			vertical-align: middle;
			BackColor: Transparent;
			border: 1px solid black;
		}
		.table tbody td.tdleft{text-align: left!important;padding-left: 15px;}
		.td_left {text-align: left!important;}
		.alert { padding: 0px 0px 0px 0px !important;margin-bottom: 0px !important;}
		table, th, td {
			border: 1px solid #ccc;
		}
		.praise_status_1{background-color: #0096DA;}
		.praise_status_2{background-color: #FF9502;}
		.praise_status_3{background-color: #10B9B9;}
		.praise_status_4{background-color: #34C758;}
		.praise_status_5{background-color: #f54142;}
		.praise_status_6{background-color: #f54142;}
		.td-invalid {background-color: #f2dede;}
	</style>
</head>
<body>
<input type="hidden" id="orderId" name="orderId" value="${orderId}" />
<input type="hidden" id="orderNo" name="orderNo" value="${orderNo}" />

<fieldset style="margin-left:2%;width: 96%;margin-bottom: 10px">
	<c:if test="${materials != null && materials.size() >0}">
		<c:set var="firstMaterialForm" value="${materials[0]}" />
	</c:if>
	<div style="margin-top: 15px">
		<c:if test="${firstMaterialForm ne null}">
			工单单号：
			<c:choose>
				<c:when test="${currentuser.isCustomer()==true || currentuser.isSaleman()}">
					<a href="javascript:void(0);" onclick="Order.viewOrderDetail('${firstMaterialForm.orderId}','${firstMaterialForm.quarter}');" title="点击查看订单详情">${orderNo}</a>
				</c:when>
				<c:otherwise>
					<a href="javascript:void(0);" onclick="Order.showKefuOrderDetail('${firstMaterialForm.orderId}','${firstMaterialForm.quarter}',1);" title="点击查看订单详情">${orderNo}</a>
				</c:otherwise>
			</c:choose>
		</c:if>
	</div>
	<table style="width: 100%;margin-top: 15px" >
		<thead>
		<tr style="background: #F8F8F9" height="40px">
			<th width="180">产品</th>
			<th width="170">配件</th>
			<th width="110">返件</th>
			<th width="64">
				回收
				<a style="text-decoration:none" data-toggle="tooltipSouth" data-tooltip="旧件给师傅回收，收取回收费用">
				   <img src="${ctxStatic}/images/rpt/interrogation.png" id="recycleExplain">
				</a>
			</th>
			<th width="64">数量</th>
			<th width="120">价格(元)</th>
			<th width="96">合计(元)</th>
			<th width="120">物流信息</th>
			<th width="100">配件照片</th>
			<th width="160">故障描述</th>
			<th width="120">操作</th>
		</tr>
		</thead>
	</table>
	<c:if test="${firstMaterialForm ne null}">
		<c:forEach items="${materials}" var="master" varStatus="i" begin="0">
			<table style="width: 100%;margin-top: 5px" id="tr_${master.id}">
				<thead>
				<tr style="background: #F8F8F9" height="40px" >
					<td colspan="11" style="text-align: left">
						<p style="margin:0px 0px 0px 12px">
							配件单号：${master.masterNo}&nbsp;&nbsp;&nbsp;&nbsp;
							申请时间：${master.createDate}&nbsp;&nbsp;&nbsp;&nbsp;
							申请方式：${master.applyType.label}&nbsp;&nbsp;
							<span class="label praise_status_${master.status.value}">${master.status.label}</span>
							<c:if test="${master.status.value eq '6' && master.closeRemark!=null && master.closeRemark!=''}">
							  <a style="text-decoration:none" data-toggle="tooltipWest" data-tooltip="${master.closeRemark}">
								 <img src="${ctxStatic}/images/warning.png" style="width: 20px;height: 20px;margin-bottom: 2px">
							  </a>
							</c:if>
                            <c:if test="${master.status.value eq '5' && master.closeRemark != null && master.closeRemark != ''}">
                                <a style="text-decoration:none" data-toggle="tooltipWest" data-tooltip="${master.closeRemark}">
                                    <img src="${ctxStatic}/images/warning.png" style="width: 20px;height: 20px;margin-bottom: 2px">
                                </a>
                            </c:if>
						</p>
					</td>
				</tr>
				<c:forEach items="${master.subForms}" var="subForm" varStatus="s" begin="0">
					<tr style="text-align: center" height="40px" class="tr_item" data-itemid="${subForm.materials[0].id}">
						<td rowspan="${subForm.materials.size()}" width="180">
							<span>${subForm.product.name}</span>
						</td>
						<td width="170">${subForm.materials[0].materialName}</td>
						<td width="110" class="td_return" >
							<c:if test="${master.waitingB2BCommand == 0}">
								<c:choose>
									<c:when test="${master.status.value eq '1'}">
										<input type="radio" name="isReturnBox_${subForm.materials[0].id}" id="chk_${subForm.materials[0].id}_1" value="1" data-itemid="${subForm.materials[0].id}" data-masterid="${master.id}" class="isReturn_${master.id}" />
										<label for="chk_${subForm.materials[0].id}_1">是</label>
										<input type="radio" name="isReturnBox_${subForm.materials[0].id}" id="chk_${subForm.materials[0].id}_0" value="0" data-itemid="${subForm.materials[0].id}" data-masterid="${master.id}" class="isReturn_${master.id}" />
										<label for="chk_${subForm.materials[0].id}_0">否</label>
									</c:when>
									<c:otherwise>
										<c:out value="${subForm.materials[0].returnFlag == 1?'是':'否'}"/>
									</c:otherwise>
								</c:choose>
							</c:if>
						</td>
						<td width="64">
							<c:if test="${master.waitingB2BCommand == 0}">
								<c:choose>
									<c:when test="${master.status.value eq '1'}">
										<c:if test="${subForm.materials[0].recycleFlag==1}">
											<input type="checkbox" name="recycleBox" id="recycleBox_${subForm.materials[0].id}" data-itemid="${subForm.materials[0].id}" data-masterid="${master.id}" class="isRecycle_${master.id}"/>
											<p style="display: none" id="recycleUtilPrice_${subForm.materials[0].id}">${subForm.materials[0].recyclePrice}</p>
										</c:if>
									</c:when>
									<c:otherwise>
										<c:out value="${subForm.materials[0].recycleFlag == 1?'是':'否'}"/>
									</c:otherwise>
								</c:choose>
							</c:if>
						</td>
						<td width="64" id="qty_${master.id}">${subForm.materials[0].qty}</td>
						<td width="120" id="itemPrice_${subForm.materials[0].id}" name="itemPrice_${master.id}">
							<span name="price_${master.id}" data-qty="${subForm.materials[0].qty}">
								<fmt:formatNumber value="${subForm.materials[0].price}" pattern="0.00"></fmt:formatNumber>
							</span>
							<c:choose>
								<c:when test="${master.status.value eq '1'}">
									<div style="display: none" id="recycle_${subForm.materials[0].id}">
                                         回收：${subForm.materials[0].recyclePrice}
									</div>
									<span style="display: none" id="recyclePrice_${subForm.materials[0].id}" name="recyclePrice_${master.id}" data-qty="${subForm.materials[0].qty}">0.0</span>
								</c:when>
								<c:otherwise>
									<c:if test="${subForm.materials[0].recycleFlag == 1 && subForm.materials[0].recyclePrice!=0}">
                                        <br/>回收：${subForm.materials[0].recyclePrice}
									</c:if>
								</c:otherwise>
							</c:choose>
						</td>
						<c:if test="${s.count==1}">
							<td rowspan="${master.maxRow}" width="94" id="totalPrice_${master.id}">
								<fmt:formatNumber value="${master.totalPrice}" pattern="0.00"/>
							</td>
							<td rowspan="${master.maxRow}" width="120">
								<c:choose>
									<c:when test="${master.status.value eq '2'}">
										<c:choose>
											<c:when test="${master.waitingB2BCommand == 0}">
												<a href="javascript:void(0);" onclick="expressForm('${master.id}','${master.masterNo}');">填写物流单号</a>&nbsp;<br/>
											</c:when>
											<c:otherwise>
												<div class="alert alert-block">待商家系统处理</div>
											</c:otherwise>
										</c:choose>
									</c:when>
									<c:otherwise>
										<a href="http://www.kuaidi100.com/chaxun?com=${master.expressCompany.value}&nu=${master.expressNo }" target="_blank" title="点击进入快递100">
										  ${master.expressCompany.label}<br>
										  ${master.expressNo }
										</a>
									</c:otherwise>
								</c:choose>
							</td>
							<td rowspan="${master.maxRow}" width="100">
								<a href="javascript:void(0);" onclick="layerWindow('layer_material_photo','${ctx}/sd/material/materialMasterAttachmentForm?masterId=${master.id}&quarter=${master.quarter}&orderId=${master.orderId}','查看照片',1100,700)">查看照片</a>
							</td>
							<td rowspan="${master.maxRow}" width="160">${master.remarks}</td>
							<td rowspan="${master.maxRow}" width="120" >
								<c:if test="${master.status.intValue gt 1 && master.returnFlag == 1}">
									<a href="javascript:void(0);" id="lnk_return_${i.index}" onclick="returnForm('${master.id}','${master.masterNo}');"><c:out value="${master.materialReturnSendFlag==0?'填写返件物流信息':'查看返件'}" /></a><br/>
								</c:if>
								<c:choose>
									<c:when test="${master.status.value eq '1'}">
										<c:choose>
											<c:when test="${master.waitingB2BCommand == 0}">
												<a href="javascript:void(0);" id="lnk_approve_${i.index}" class="btn btn-primary btn-mini" onclick="materialMasterApprove('${master.id}','${master.orderId}','lnk_approve_${i.index}',${subForm.materials.size()})">确认</a>
												<a href="javascript:void(0);" id="lnk_reject_${i.index}" class="btn btn-danger btn-mini" onclick="rejectForm('${master.id}','${master.quarter}',)">驳回</a><br/>
											</c:when>
											<c:otherwise>
												<div class="alert alert-block">待商家系统处理</div>
											</c:otherwise>
										</c:choose>
									</c:when>
									<c:when test="${master.status.value eq '2'}">
										<c:choose>
											<c:when test="${master.waitingB2BCommand == 0}">
												<c:if test="${(currentuser.isKefu()==true || currentuser.isAdmin()==true) && master.returnFlag eq 1}">
													<a href="javascript:void(0);" id="lnk_close_${i.index}" onclick="closeForm('${master.id}','${master.masterNo}');">关闭申请单</a>&nbsp;
												</c:if>
											</c:when>
											<c:otherwise>
												<div class="alert alert-block">待商家系统处理</div>
											</c:otherwise>
										</c:choose>
									</c:when>
									<c:when test="${(currentuser.isKefu()==true || currentuser.isAdmin()==true) && master.status.value eq '3' && master.returnFlag eq 1}">
										<a href="javascript:void(0);" id="lnk_close_${i.index}" onclick="closeForm('${master.id}','${master.masterNo}');">关闭申请单</a>&nbsp;
									</c:when>
								</c:choose>
							</td>
						</c:if>
					</tr>
					<c:forEach items="${subForm.materials}" var="item" varStatus="j" begin="1">
						<tr style="text-align: center" height="40px">
							<td width="170">${item.materialName}</td>
							<c:if test="${master.waitingB2BCommand == 0}">
								<c:choose>
									<c:when test="${master.status.value eq '1'}">
									<td width="110" class="td_return">
										<input type="radio" name="isReturnBox_${item.id}" id="chk_${item.id}_1" value="1" data-itemid="${item.id}" data-masterid="${master.id}" class="isReturn_${master.id}" />
										<label for="chk_${item.id}_1">是</label>
										<input type="radio" name="isReturnBox_${item.id}" id="chk_${item.id}_0" value="0" data-itemid="${item.id}" data-masterid="${master.id}" class="isReturn_${master.id}" />
										<label for="chk_${item.id}_0">否</label>
									</td>
									</c:when>
									<c:otherwise>
										<td width="110">
										<c:out value="${item.returnFlag == 1?'是':'否'}"/>
										</td>
									</c:otherwise>
								</c:choose>
							</c:if>
							<td width="64">
								<c:if test="${master.waitingB2BCommand == 0}">
                                    <c:choose>
										<c:when test="${master.status.value eq '1'}">
											<c:if test="${item.recycleFlag==1}">
												<input type="checkbox" name="recycleBox" id="recycleBox_${item.id}"  data-itemid="${item.id}" data-masterid="${master.id}" class="isRecycle_${master.id}"/>
												<p style="display: none" id="recycleUtilPrice_${item.id}">${item.recyclePrice}</p>
											</c:if>
										</c:when>
										<c:otherwise>
											<c:out value="${item.recycleFlag == 1?'是':'否'}"/>
										</c:otherwise>
									</c:choose>
								</c:if>
							</td>
							<td width="64" id="qty_${master.id}">${item.qty}</td>
							<td width="120" id="itemPrice_${item.id}" name="itemPrice_${master.id}">
								<span name="price_${master.id}" data-qty="${item.qty}">
									<fmt:formatNumber value="${item.price}" pattern="0.00"></fmt:formatNumber>
								</span>
								<c:choose>
									<c:when test="${master.status.value eq '1'}">
										<div style="display: none" id="recycle_${item.id}">
											回收：${item.recyclePrice}
										</div>
										<span style="display: none" id="recyclePrice_${item.id}" name="recyclePrice_${master.id}" data-qty="${item.qty}">0.0</span>
									</c:when>
									<c:otherwise>
										<c:if test="${item.recycleFlag == 1 && item.recyclePrice!=0}">
											<br/>回收：${item.recyclePrice}
										</c:if>
									</c:otherwise>
								</c:choose>
							</td>
						</tr>
					</c:forEach>
				</c:forEach>
				<c:if test="${(master.applyType.value == 2 && not empty master.receivedInfo) || (master.materialReturnSendFlag==1 && not empty master.materialReturnReceiverInfo)}">
					<tr>
						<td colspan="11" style="text-align: left">
							<c:if test="${master.applyType.value == 2}">
								<div style="margin:5px 0px 5px 12px">收件信息：<span style="margin-left: 5px">${master.receivedInfo}</span></div>
							</c:if>
							<c:if test="${master.materialReturnSendFlag==1 && not empty master.materialReturnReceiverInfo}">
								<div style="margin:5px 0px 5px 12px">返件信息：<span style="margin-left: 5px">${master.materialReturnReceiverInfo}</span></div>
							</c:if>
							</div>
						</td>
					</tr>
				</c:if>
				</thead>
			</table>
		</c:forEach>
	</c:if>
</fieldset>
</body>
</html>