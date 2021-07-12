<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>上门服务-异常处理</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/dialog.jsp" %>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
	<script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        Order.rootUrl = "${ctx}";
        var this_index = parent.layer.getFrameIndex(window.name);
        var clickTag = 0;
		$(document).ready(function() {
			<c:if test="${!empty order.id && order.canService() }">
			$('#productTable').
		      on('mouseover', 'tr', function() {
		    	  $(this).find("i.icon-delete").show()
		      }).
		      on('mouseout', 'tr', function() {
		    	  $(this).find("i.icon-delete").hide();
		      });
			</c:if>

            $('.fancybox').fancybox({
                openEffect : 'none',
                closeEffect	: 'none'
            });

            $("#btnSubmit").on("click",function(){
                if(clickTag == 1){
                    return false;
				}
				clickTag = 1;
                var $btnSubmit = $("#btnSubmit");
                $btnSubmit.attr("disabled", "disabled");
                var loadingIndex;
                var ajaxSuccess = 0;
                var json = {
                    id : $("#id").val(),
					quarter : $("#quarter").val(),
                    orderNo : $("#orderNo").val()
                };
                $.ajax({
                    async: false,
                    cache: false,
                    type: "POST",
                    url: "${ctx}/sd/pending/ajax/save",
                    data: json,
                    beforeSend: function () {
                        loadingIndex = layer.msg('正在提交，请稍等...', {
                            icon: 16,
                            time: 0,
                            shade: 0.3
                        });
                    },
                    complete: function () {
                        if(loadingIndex) {
                            layer.close(loadingIndex);
                        }
                        if(ajaxSuccess == 0) {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                        }
                    },
                    success: function (data) {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if(data && data.success == true){
							closeMeAndRefreshParent();
                            ajaxSuccess = 1;
                        }
                        else if( data && data.message){
                            layerError(data.message,"错误提示");
                        }
                        else{
                            layerError("保存失败","错误提示");
                        }
                        return false;
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText,null,"保存失败，请重试!");
                    }
                });
                return false;
            });

            $('a[data-toggle=tooltip]').darkTooltip();

		});

        function pendingService(orderId,quarter){
			layer.open({
                type: 2,
                id:'layer_pending_service',
                zIndex:19891016,
                title:'添加服务明细',
                content: "${ctx}/sd/pending/addservice?orderId="+orderId + "&quarter=" + (quarter || ''),
                shade: 0.3,
                area: ['1000px', '750px'],
                maxmin: false,
                success: function(layero){
                    // layer.setTop(layero);
                }
            });
        }

		function closeme(){
            top.layer.close(this_index);
		}

		function closeMeAndRefreshParent(){
            //回调父窗口方法
            var iframe = getActiveTabIframe();//定义在jeesite.min.js中
            if(iframe != undefined){
                iframe.repage();
            }
            top.layer.close(this_index);
		}

        //刷新本页面(弹窗调用)
        function reload(){
            var iframeWin = $(parent.document).contents().find("#layui-layer-iframe"+this_index)[0];
            iframeWin.src = iframeWin.src;
        }

        function delServiceDetail(id,orderId,quarter){
            var confirmClickTag = 0;
            layer.confirm("确认要删除该订单服务项目吗?", {icon: 3, title:'系统确认'}, function(index){
                if(confirmClickTag == 1){
                    return false;
				}
				confirmClickTag = 1;
                var loadingIndex;
                //do something
                layer.close(index);
                $.ajax({
                    async: false,
                    cache : false,
                    type : "POST",
                    url : "${ctx}/sd/pending/ajax/delservice?id=" + id +"&orderId=" + orderId + "&quarter=" + (quarter || ''),
                    data : null,
                    beforeSend: function () {
                        loadingIndex = layer.msg('正在提交，请稍等...', {
                            icon: 16,
                            time: 0,
                            shade: 0.3
                        });
                    },
                    complete: function () {
                        if(loadingIndex) {
                            layer.close(loadingIndex);
                        }
                    },
                    success : function(data)
                    {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if (data.success)
                        {
                            layerMsg("订单服务项目删除成功!");
                            reload();
                        }else
                        {
                            layerError("删除订单服务项目失败:" + data.message, "错误提示");
                        }
                    },
                    error : function(e)
                    {
                        ajaxLogout(e.responseText,null,"删除订单服务项目错误，请重试!");
                    }
                });
            });
            return false
        }

        //取消好评单
        function closePraise() {
            top.layer.confirm('取消后不能重新申请好评费,确定取消吗', {icon: 3, title:'系统确认'}, function(index){
                var quarter = $("#quarter").val();
                var id = $("#id").val();
                var orderNo = $("#orderNo").val();
                var loadingIndex = layerLoading('正在提交，请稍候...');
                var $btnClose = $("#close");
                if ($btnClose.prop("disabled") == true) {
                    event.preventDefault();
                    return false;
                }
                $btnClose.prop("disabled", true);

                $.ajax({
                    url:"${ctx}/sd/pending/cancelled",
                    type:"POST",
                    data:{quarter:quarter,id:id,orderNo:orderNo},
                    dataType:"json",
                    success: function(data){
                        //提交后的回调函数
                        if(loadingIndex) {
                            layer.close(loadingIndex);
                        }
                        if(ajaxLogout(data)){
                            setTimeout(function () {
                                $btnClose.removeAttr('disabled');
                            }, 2000);
                            return false;
                        }
                        if (data.success) {
                            layerMsg("操作成功");
                           /* var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                            if(pframe){
                                pframe.repage();
                            }
                            top.layer.close(this_index);//关闭本身*/
                        }else{
                            setTimeout(function () {
                                clickTag = 0;
                                $btnClose.removeAttr('disabled');
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
                            $btnClose.removeAttr('disabled');
                        }, 2000);
                        ajaxLogout(data,null,"数据保存错误，请重试!");
                        //var msg = eval(data);
                    },
                    timeout: 50000               //限制请求的时间，当请求大于5秒后，跳出请求
                });
            });
        }
	</script>
</head>
<body>
	<form:form id="inputForm" modelAttribute="order" class="form-horizontal">
		<sys:message content="${message}"/>
		<c:if test="${canAction}">
			<form:hidden path="id"/>
			<form:hidden path="orderNo"/>
			<form:hidden path="quarter"/>
			<!-- order head -->
		<legend>基本信息</legend>
		<table class="table table-bordered table-striped" style="margin-bottom: 0px;">
		<tbody>
			<tr>
				<td width="80" class="tdlable"><label class="control-label">订单编号:</label></td>
				<td width="140" class="tdbody">${order.orderNo}</td>
				<td width="80" class="tdlable"><label class="control-label">客户名称:</label></td>
				<td width="140" class="tdbody">${order.orderCondition.customer.name}</td>
				<td width="80" rowspan="5" valign="top" >
				<legend>完成照片上传</legend>
					<c:if test="${!empty order.id && order.canService() }">
						<a href="javascript:void(0);" style="margin-top:20px; margin-left: 50px;" onclick="Order.photolistNew('${order.id}','${order.quarter}',${fns:isNewOrder(order.orderNo)});" class="btn btn-mini btn-primary">上传照片</a>
					</c:if>
			    </td>
			</tr>
			<tr>
				<td class="tdlable"><label class="control-label">联系人:</label></td>
				<td class="tdbody">${order.orderCondition.userName}</td>
				<td class="tdlable"><label class="control-label">手机:</label></td>
				<td class="tdbody">${order.orderCondition.phone1}</td>
			</tr>
			<tr>
				<td class="tdlable"><label class="control-label">座机:</label></td>
				<td class="tdbody">${order.orderCondition.phone2}</td>
				<td class="tdlable"><label class="control-label">实际联络电话:</label></td>
				<td class="tdbody">${order.orderCondition.servicePhone}</td>
			</tr>
			<tr>
				<td class="tdlable"><label class="control-label">用户地址:</label></td>
				<td class="tdbody">${order.orderCondition.area.name} ${order.orderCondition.address}</td>
				<td class="tdlable"><label class="control-label">实际上门地址:</label></td>
				<td class="tdbody">${order.orderCondition.area.name} ${order.orderCondition.serviceAddress}</td>
			</tr>
			<tr>
				<td class="tdlable"><label class="control-label">服务描述:</label></td>
				<td class="tdbody" colspan="3">${order.description}</td>
			</tr>
		</tbody>
		</table>
		<legend>安维人员</legend>
		<table class="table table-bordered table-striped" style="margin-bottom: 0px;">
		<tbody>
			<tr>
				<td class="tdlable"><label class="control-label">网点编号:</label></td>
				<td class="tdbody">${order.orderCondition.servicePoint.servicePointNo}</td>
				<td class="tdlable"><label class="control-label">姓名:</label></td>
				<td class="tdbody">${order.orderCondition.engineer.name}</td>
			</tr>
			<tr>
				<td class="tdlable"><label class="control-label">手机号:</label></td>
				<td class="tdbody">${order.orderCondition.engineer.mobile}</td>
				<td class="tdlable"><label class="control-label">联络方式2:</label></td>
				<td class="tdbody">${order.orderCondition.servicePoint.contactInfo1}</td>
			</tr>
			<tr>
				<td class="tdlable"><label class="control-label">联络方式2:</label></td>
				<td class="tdbody">${order.orderCondition.servicePoint.contactInfo2}</td>
				<td class="tdlable"><label class="control-label">联络方式3:</label></td>
				<td class="tdbody">&nbsp;</td>
			</tr>
		</tbody>
		</table>
		<!-- order body -->
		
		<legend>服务详细信息
			<a class="btn" id="orderForm_btn_add" onclick="pendingService('${order.id}','${order.quarter}');" href ="javascript:;" style="margin-left:30px;" title="添加服务明细">添加</a>
		</legend>
		<div class="row-fluid">
		<div class="control-group">
			<table id="productTable" class="table table-striped table-bordered table-condensed" style="margin-bottom: 0px;">
			<thead>
				<tr>
					<th rowspan="2" width="30px">序号</th>
					<th rowspan="2" width="100px">日期</th>
					<th rowspan="2" width="60px">上门次数</th>
					<th rowspan="2" width="60px">服务类型</th>
					<th rowspan="2">产品</th>
					<th rowspan="2">品牌</th>
					<th rowspan="2">型号/规格</th>
                    <th rowspan="2" width="60px">服务项目</th>
					<th rowspan="2">数量</th>
					<th colspan="7">应付款</th>
					<th rowspan="2">备注</th>
				</tr>
				<tr>
					<th>服务费</th>
					<th>快递费</th>
					<th>远程费</th>
					<th>配件费</th>
					<th>其他</th>
					<th>小计</th>
					<th>安维</th>
				</tr>
				</thead>	
			<tbody>
			<c:set var="rownum" value="0" />
			<c:set var="totalQty" value="0" />
			<c:forEach items="${order.detailList}" var="item" varStatus="i" begin="0">
			<c:if test="${item.delFlag eq 0 }">
				<tr class="${(item.charge eq 0.00 || (item.qty eq 1 && item.charge lt item.standPrice) )?'error':'' }">
					<td class="tdcenter">
						${i.index+1}
					</td>
					<td class="tdcenter"><fmt:formatDate value="${item.createDate}" pattern="yyyy-MM-dd"/></td>
					<td class="tdcenter">${item.serviceTimes}</td>
                    <td>${item.serviceCategory.label}</td>
					<td>${item.product.name}
						<input type="hidden" id="detailId${i.index}" name="detailId${i.index}" value="${item.id}" />
						<a href="javascript:;" style="cursor:pointer;font-size:14px;" onclick="delServiceDetail('${item.id}','${order.id}','${order.quarter}');" ><i style="display:none;" class="icon-delete"></i></a>
					</td>
					<td>${item.brand}</td>
					<td>${item.productSpec}</td>
                    <td><a href="javascript:;" style="cursor:pointer;" data-toggle="tooltip" data-tooltip="${item.errorContent}">${item.serviceType.name}</a></td>
					<td class="tdcenter">${item.qty}</td>
					<c:set var="totalQty" value="${totalQty+item.qty}" />
					<td class="tdcenter">${item.engineerServiceCharge}</td>
					<td class="tdcenter" >${item.engineerExpressCharge}</td>
					<td class="tdcenter">${item.engineerTravelCharge}
						<c:if test="${!empty item.travelNo}">签核单号:${item.travelNo}</c:if>
					</td>
					<td class="tdcenter">${item.engineerMaterialCharge}</td>
					<td class="tdcenter">${item.engineerOtherCharge}</td>
					<td class="tdcenter">${item.engineerChage}</td>
					<td class="tdcenter">${item.engineer.name}</td>
					<td>${item.remarks}</td>
				</tr>
			</c:if>
			</c:forEach>
				<tr>
					<td style="text-align:right;" colspan="8" ><span class="alert alert-success">总计</span></td>
					<td class="tdcenter"><span class="alert alert-success"><strong>${totalQty}</strong></span></td>
					<td class="tdcenter"><span class="alert alert-success">${order.orderFee.engineerServiceCharge}</span></td>
					<td class="tdcenter"><span class="alert alert-success">${order.orderFee.engineerExpressCharge}</span></td>
					<td class="tdcenter"><span class="alert alert-success">${order.orderFee.engineerTravelCharge}</span></td>
					<td class="tdcenter"><span class="alert alert-success">${order.orderFee.engineerMaterialCharge}</span></td>
					<td class="tdcenter"><span class="alert alert-success">${order.orderFee.engineerOtherCharge}</span></td>
					<td class="tdcenter"><span class="alert alert-info"><strong>${order.orderFee.engineerTotalCharge}</strong></span></td>
					<td class="tdcenter"></td>
					<td class="tdcenter"></td>
				</tr>
			</tbody>
			</table>
			</div>
		</div>

		<div class="control-group">
			<div class="controls">
				<input id="btnSubmit" class="btn btn-success" type="button" value="完成返回" />
				<c:if test="${canCancelPraise == true}">
					<input id="close" class="btn btn-danger" type="button" onclick="closePraise()" value="取消好评" />
				</c:if>
				<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closeme()" />
			</div>
		</div>
		</c:if>
	</form:form>
	<style type="text/css">
		.tdlable {width:160px;align:right;}
		.tdbody{width:300px;}
		.table th, .table td{padding:4px;}
		.table thead th {text-align: center;vertical-align: middle;}
		.table .tdcenter{text-align: center;vertical-align: middle;}
		.alert {padding: 4px 5px 4px 4px;}
	</style>
</body>
</html>