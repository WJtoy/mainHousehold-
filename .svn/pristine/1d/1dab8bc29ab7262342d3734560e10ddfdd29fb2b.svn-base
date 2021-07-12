<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>上门服务</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/dialog.jsp" %>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
	<script type="text/javascript">
        Order.rootUrl = "${ctx}";
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
			
			$("#buttonUploadLogo").click(function() {
				uploadfile($("#logo"),$("#logo_image"), "fileToUploadlogo");
				return false;
			});
			
		});

		function closeme(){
			window.opener.repage();
			window.close();
		}
		function uploadfile($obj1,$obj1_image, obj2) {
			var data = {
				fileName : $obj1.val()
			};
			
			$.ajaxFileUpload({
				url : '${pageContext.request.contextPath}/servlet/Upload?fileName=' + $obj1.val(),//处理图片脚本
				secureuri : false,
				data : data,
				fileElementId : obj2,//file控件id
				dataType : 'json',
				success : function(data, status) {
                    if(ajaxLogout(data)){
                        return false;
                    }
					$obj1.val(data.fileName);
					$("#orignalName").val(data.origalName);

					if(data.origalName =="" )
					{
						 window.$.jBox.error("请先选择文件，再点击上传","错误提示");
						return true;
					}
					
					//Aja 保存附件信息
					$.ajax({
							type: "POST",
							url: "${ctx}/sd/order/saveAttach",
							data:{filePath:$("#logo").val(),orginalName:$("#orignalName").val(),orderId:$("#id").val()},
							success: function (data) {
								if(ajaxLogout(data)){
									return false;
								}
							   if(data && data.success == true){
								  window.location=window.location;
							   }
							   else if( data && data.message){
								   window.$.jBox.error(data.message,"错误提示");
							   }
							   else{
								   window.$.jBox.error("添加附件错误","错误提示");
							   }
							   $('#btnSubmit').removeAttr('disabled');
							   return false;
							},
							error: function (e) {
								//window.$.jBox.error("添加附件错误:"+e,"错误提示");
								$('#btnSubmit').removeAttr('disabled');
								ajaxLogout(e.responseText,null,"添加附件错误，请重试!");
							}
						});
					//Aja 保存附件信息
					
				},
				error : function(e) {
					// alert(e);
                    ajaxLogout(e.responseText,null,"添加附件错误，请重试!");
				}
			});
		}
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">上门服务</a></li>
	</ul>
	<form:form id="inputForm" modelAttribute="order" action="${ctx}/sd/order/service" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<!-- order head -->
		<legend>基本信息</legend>
		<table class="table table-bordered table-striped" style="margin-bottom: 0px;">
		<tbody>
			<tr>
				<td width="80" class="tdlable"><label class="control-label">订单编号:</label></td>
				<td width="140" class="tdbody">${order.orderNo}</td>
				<td width="80" class="tdlable"><label class="control-label">客户名称:</label></td>
				<td width="140" class="tdbody">${order.orderCondition.customer.name}</td>
				<td width="250" rowspan="5" valign="top" >
				<legend>完成照片上传</legend>
				<div>
					<c:if test="${!empty order.id && order.canService() }">
					<img id="logo_image" class="logo_image" alt="" />
					<input name="logo" id="logo" type="hidden"  htmlEscape="false" /> 
					<input name="orignalName" id="orignalName" type="hidden" htmlEscape="false" /> 
					<input id="fileToUploadlogo" type="file" style="width: 170px;" size="20" name="fileToUploadlogo" value="${fileToUploadlogo}" >
					<button id="buttonUploadLogo" type="button" class="btn">上传</button>
					</c:if>
				</div>
				
				<fieldset>
						<table width="100%" border="0"
						class="table table-striped table-bordered table-condensed"
						style="margin-bottom: 0px;">
							<thead>
								<tr>
									<td width="*">名称</td>
									<td width="90">时间</td>
									<td width="20"></td>				
								</tr>
							</thead>
							<tbody>
							<c:set var="index" value="0"></c:set>
							<c:forEach items="${order.attachments}" var="access">
								<c:set var="index" value="${index+1}"></c:set>
								<tr id="accesstr${index}">
									<td><a href="${ctxUpload}/${access.filePath}" target="_blank">
									${empty access.remarks?"查看照片":access.remarks}</a></td>
									<td><fmt:formatDate value="${access.createDate}" pattern="yyyy-MM-dd"/></td>
									<td>
										<a class="" href="#" data-rowid="${index}"
											onclick="Order.deleteServiceAttachment(${index});" title="刪除"><i class="icon-delete" style="margin-top: 0px;"></i>
										</a>
										<input type="hidden" id="id${index}" name="id${index}" value="${access.id}" />
									</td>
								</tr>
							</c:forEach>
							<tbody>
						</table>
					</fieldset>
			
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
			<c:if test="${!empty order.id && order.canService() }"><a class="btn" id="orderForm_btn_add" onclick="Order.addService(${order.id});" href ="javascript:void(0);"  data-fancybox-type="iframe" style="margin-left:30px;" title="添加服务明细">添加</a> </c:if>
			<c:if test="${order.orderCondition.partsFlag == 1}">
					<a href="javascript:void(0);" class="btn btn-mini btn-primary" style="float: right;margin-right: 80px;"
								onclick="Order.attachlist('${order.id}','${order.orderNo}','${order.quarter}');"><abbr title="点击查看配件申请列表">订单配件</abbr>
					</a>
			</c:if>
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
					<th rowspan="2">数量</th>
					<th colspan="7">应付款</th>
					<th rowspan="2">备注</th>
					<th rowspan="2">配件申请</th>
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
					<td>${item.serviceType.name}</td>
					<td>${item.product.name}
						<input type="hidden" id="detailId${i.index}" name="detailId${i.index}" value="${item.id}" />
						<a href="${ctx}/sd/order/delservice?id=${item.id}&orderId=${order.id}" style="cursor:pointer;font-size:14px;"  onclick="return confirmx('确认要删除该订单服务项目吗？', this.href)" ><i style="display:none;" class="icon-delete"></i></a>
					</td>
					<td>${item.brand}</td>
					<td>${item.productSpec}</td>
					<td class="tdcenter">${item.qty}</td>
					<c:set var="totalQty" value="${totalQty+item.qty}" />
					<td class="tdcenter">${item.engineerServiceCharge}</td>
					<td class="tdcenter" >${item.engineerExpressCharge}</td>
					<td class="tdcenter">${item.engineerTravelCharge}
						<c:if test="${!empty item.travelNo}">签核单号:${item.travelNo}</c:if>
					</td>
					<td class="tdcenter">${item.engineerMaterialCharge}</td>
					<td class="tdcenter">${item.engineerOtherCharge}</td>
					<td class="tdcenter">${item.engineerTotalCharge}</td>
					<td class="tdcenter">${item.engineer.name}</td>
					<td>${item.remarks}
					</td>
					<td>
							<a id="btn_MaterialApply"  onclick="Order.materialApply(${order.id},${item.id});" href ="javascript:void(0);"  data-fancybox-type="iframe" style="margin-left:30px;" title="新增配件申请">申请配件</a>
					</td>
				</tr>
			</c:if>
			</c:forEach>
				<tr>
					<td style="text-align:right;" colspan="7" ><span class="alert alert-success">总计</span></td>
					<td class="tdcenter"><span class="alert alert-success"><strong>${totalQty}</strong></span></td>
					<td class="tdcenter"><span class="alert alert-success">${order.orderFee.engineerServiceCharge}</span></td>
					<td class="tdcenter"><span class="alert alert-success">${order.orderFee.engineerExpressCharge}</span></td>
					<td class="tdcenter"><span class="alert alert-success">${order.orderFee.engineerTravelCharge}</span></td>
					<td class="tdcenter"><span class="alert alert-success">${order.orderFee.engineerMaterialCharge}</span></td>
					<td class="tdcenter"><span class="alert alert-success">${order.orderFee.engineerOtherCharge}</span></td>
					<td class="tdcenter"><span class="alert alert-info"><strong>${order.orderFee.engineerTotalCharge}</strong></span></td>
					<td class="tdcenter" colspan="3"></td>
				</tr>
			</tbody>
			</table>
			</div>
		</div>
		
		<div class="control-group">
			<div class="controls">
				<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closeme()" />
			</div>
		</div>
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