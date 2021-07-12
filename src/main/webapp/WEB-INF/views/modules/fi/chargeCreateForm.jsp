<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>生成对帐单</title>
	<meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<style type="text/css">.sort{color:#0663A2;cursor:pointer;}</style>
	<style type="text/css">
		.table thead th {
			text-align: center;
    		vertical-align: middle;
		}
		.table tbody td {
			vertical-align: middle;
		}
	</style>
	<script type="text/javascript">
		$(document).ready(function() {
            var selectedIdStrings = $("#selectedIds").val();
            var selectedIds = selectedIdStrings.split(",");
            for(var i=0;i<selectedIds.length;i++){
                $(":checkbox[name='checkedRecords'][value='"+selectedIds[i]+"']").attr("checked", true);
            }

			// 表格排序
			var orderBy = $("#orderBy").val().split(" ");
			$("#treeTable th.sort").each(function(){
				if ($(this).hasClass(orderBy[0])){
					orderBy[1] = orderBy[1]&&orderBy[1].toUpperCase()=="DESC"?"down":"up";
					$(this).html($(this).html()+" <i class=\"icon icon-arrow-"+orderBy[1]+"\"></i>");
				}
			});
			$("#treeTable th.sort").click(function(){
				var order = $(this).attr("class").split(" ");
				var sort = $("#orderBy").val().split(" ");
				for(var i=0; i<order.length; i++){
					if (order[i] == "sort"){order = order[i+1]; break;}
				}
				if (order == sort[0]){
					sort = (sort[1]&&sort[1].toUpperCase()=="DESC"?"ASC":"DESC");
					$("#orderBy").val(order+" DESC"!=order+" "+sort?"":order+" "+sort);
				}else{
					$("#orderBy").val(order+" ASC");
				}
				page();
			});
			$("#treeTable").treeTable({expandLevel : 2});
			$(document).ready(function() {
			 $("a#aSave").fancybox({
			     fitToView : false,
			     width  : 700,
			     height  : 280,
			     autoSize : false,
			     closeClick : false,
			     type  : 'iframe',
			     openEffect : 'none',
			     closeEffect : 'none'
			    });
			});
			
			//全选绑定
			$("#selectAll").change(function() {
				 var $check = $(this);
				 $("input:checkbox").each(function(){
					 if ($(this).val() != "on"){
						 if ($check.attr("checked") == "checked") {
							 $(this).attr("checked", true);

						 }
						 else{
							 $(this).attr("checked", false);
						 }
					 }
				 });
			});
		});
		function page(n,s){
			$("#pageNo").val(n);
			$("#pageSize").val(s);
			$("#searchForm").attr("action","${ctx}/fi/chargecreate");
			$("#searchForm").submit();
	    	return false;
	    }
		function repage(){
			$("#searchForm").submit();
	    	return false;
		}
		var ids = [];
		function setIds(){
			ids = [];
			$("input:checkbox").each(function(){
				 if ($(this).attr("checked")){
				 	var temp=$(this).attr("id");
				 	if(temp!="selectAll" && temp!="travelChargeFlag" && temp!="partsFlag" && temp!="otherChargeFlag")
				 	{
				 		temp=$(this).next().attr("value");
						if(temp!="" && temp!="on")
						{
						 	ids.push($(this).val());
						 }
				 	}
				 }
			 });
		}

        $(document).off('click','#btnPending');//先解除事件绑定
		$(document).on("click", "#btnPending", function () {
            if ($("#btnPending").prop("disabled") == true) {
                return false;
            }

            $("#btnPending").prop("disabled", true);

			setIds();
	        if (ids.length == 0) {
	            top.$.jBox.error('请选择要标记为异常的订单', '生成对帐单');
                $("#btnPending").removeAttr("disabled");
	            return;
	        }

	        var submit = function (v, h, f) {
	            if (v == 'ok') {
	    	 		$("#aSave").attr("href", "${ctx}/fi/chargecreate/pending?processType=ChargeCreate");
	    	 		$("#aSave").val(ids);
	    	 		$("#aSave").click();
	            }
	            else if (v == 'cancel') {
                    $("#btnPending").removeAttr("disabled");
	            }

	            return true; //close
	        };

	        top.$.jBox.confirm('确定要将选择的订单设置为异常吗？', '生成对帐单', submit);

	    });

        $(document).off('click','#btnConfirm');//先解除事件绑定
		$(document).on("click", "#btnConfirm", function () {
            if ($("#btnConfirm").prop("disabled") == true)
            {
                return false;
            }

            $("#btnConfirm").prop("disabled", true);

			setIds();
	        if (ids.length == 0) {
	            top.$.jBox.error('请选择要生成对帐单的订单', '生成对帐单');
                $("#btnConfirm").removeAttr("disabled");
	            return;
	        }

	        var submit = function (v, h, f) {
	            if (v == 'ok') {
	                top.$.jBox.tip("正在生成对帐单...", "loading");
	    	        var data = {ids:ids.join(",")};
                    $("#selectedIds").val(ids.join(","));
	    	        $.ajax({
	    	            cache: false,
	    	            type: "POST",
	    	            url: "${ctx}/fi/chargecreate/save",
	    	            data: data,
	    	            success: function (data) {
                            setTimeout(function() {
                                if (data.success){
                                    $("#selectedIds").val("");
                                    top.$.jBox.tip("生成对帐单成功", "success");
                                }
                                else{
                                    top.$.jBox.error(data.message, '生成对帐单失败');
                                }
                                $("#btnConfirm").removeAttr("disabled");
                                repage();
                                top.$.jBox.closeTip();
                            }, 500);
	    	            },
	    	            error: function (xhr, ajaxOptions, thrownError) {
	    	                $("#btnConfirm").removeAttr("disabled");
	    	                top.$.jBox.closeTip();
	    	                top.$.jBox.error(thrownError.toString(), '生成对帐单失败');
	    	            }
	    	        });
	            }
	            else if (v == 'cancel') {
                    $("#btnConfirm").removeAttr("disabled");
	            }

	            return true; //close
	        };

	        top.$.jBox.confirm('确定要生成对帐单吗？', '生成对帐单', submit);

	    });
		function openjBox(url,title,width,height){
			top.$.jBox.open("iframe:" + url , title, width, height,{top:'5%',buttons:{}	});
		}

		//为订单做标记
       function markOrder(id,quarter){
            var markOrderId = top.layer.open({
                type: 2,
                id:'layer_orderdetail',
                zIndex:19891015,
                title:'工单详情',
                content: "${ctx}/fi/chargecreate/markOrder?id="+ id+ "&quarter=" + (quarter || ''),
                shade: 0.3,
                shadeClose: true,
                area:['1200px','800px'],
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

	</script>
</head>
  
  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">生成对帐单</a></li>
	</ul>
	<form:form id="searchForm" action="${ctx}/fi/chargecreate" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<input id="orderBy" name="orderBy" type="hidden" value="${page.orderBy}"/>
		<input id="selectedIds" name="selectedIds" type="hidden" value="${selectedIds}"/>
		<a id="aSave" type="hidden"></a>
		<div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
			注:当用工单号进行搜索查询时，不受当前时间、地区等其他条件的限制。
		</div>
		<div>
			<label>客　　户：</label>
				<%--<sys:treeselect id="customer" name="customerId" value="${customerId}" labelName="customerName" labelValue="${customerName}"
				title="客户" url="/md/customer/treeData" allowClear="true"/>--%>
			<select name="customerId" class="input-large">
				<option value="">所有</option>
				<c:forEach items="${fns:getMyCustomerListFromMS()}" var="customer">
					<option value="${customer.id}"  <c:out value="${customer.id==customerId ?'selected':''}"/>>${customer.name}</option>
				</c:forEach>
			</select>
			<label class="control-label">产品类别：</label>
				<sys:treeselect id="category" name="productCategoryId" value="${productCategoryId}" labelName="productCategoryName" labelValue="${productCategoryName}"
				title="产品类别" url="/md/productcategory/treeData" allowClear="true" cssStyle="width:140px;"/>
			<label>产品名称：</label>
			<input type = "text" id="productName" name="productName" value="${productName}" maxlength="30" class="input-small" style="width:162px;"/>
		</div>
		<div style="margin-top:8px">
			<label>完成日期：</label><input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:98px;margin-left:4px" maxlength="20" class="input-small Wdate"
				value="${beginDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			<label>~</label>&nbsp;&nbsp;&nbsp;<input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
				value="${endDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			<label>订单编号：</label>
			<input type = "text" id="orderNo" name="orderNo" value="${orderNo}" maxlength="30" class="input-small" style="width:179px;"/>
			<label>累计上门：</label>
			<input type = "text" id="serviceTimes" name="serviceTimes" value="${serviceTimes}" maxlength="3" class="input-small" style="width:162px;"/>
		</div>
		<div style="margin-top:8px">
			<label>服务网点：</label>
			<sd:servicePointSelect id="servicePoint" name="servicePointId" value="${servicePointId}" labelName="servicePointName" labelValue="${servicePointName}"
								 width="1200" height="780" title="选择服务网点" areaId="" cssClass="required"
								 showArea="false" allowClear="true" callbackmethod="" />&nbsp;&nbsp;
			<input type="checkbox" id="travelChargeFlag" name="travelChargeFlag" <c:out value="${(travelChargeFlag eq 'on')?'checked=true':''}" ></c:out>>远程</input>
			<input type="checkbox" id="partsFlag" name="partsFlag" <c:out value="${(partsFlag eq 'on')?'checked=true':''}" ></c:out>>配件</input>
			<input type="checkbox" id="otherChargeFlag" name="otherChargeFlag" <c:out value="${(otherChargeFlag eq 'on')?'checked=true':''}" ></c:out>>其他</input>
			<%--<label style="margin-left:10px">　　应收：</label><input type = "text" id="totalInStart" name="totalInStart" value="${totalInStart}" maxlength="3" class="input-small digits" style="width:33px;"/>--%>
			<%--<label>~</label>&nbsp;&nbsp;<input type = "text" id="totalInEnd" name="totalInEnd" value="${totalInEnd}" maxlength="3" class="input-small digits" style="width:33px;"/>--%>
			<%--<label>应付：</label><input type = "text" id="totalOutStart" name="totalOutStart" value="${totalOutStart}" maxlength="3" class="input-small digits" style="width:33px;"/>--%>
			<%--<label>~</label>&nbsp;&nbsp;<input type = "text" id="totalOutEnd" name="totalOutEnd" value="${totalOutEnd}" maxlength="3" class="input-small digits" style="width:33px;"/>--%>
			&nbsp;&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询" onclick="top.$.jBox.tip('请稍候...', 'loading');"  style="margin-left:10px"/>
			&nbsp;&nbsp;<input id="btnConfirm" class="btn btn-success" type="button" value="确认"/>
			&nbsp;&nbsp;<input id="btnPending" class="btn btn-danger" type="button" value="异常"/>
		</div>
	</form:form>
	<sys:message content="${message}"/>
	<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr style="vertical-align: middle;">
				<th rowspan="2" width="40"><input type="checkbox" id="selectAll" name="selectAll"/></th>
				<th rowspan="2" width="30">序号</th>
				<th rowspan="2" width="60" class="sort orderNo">订单单号</th>
				<%--<th rowspan="2" width="30" class="sort customer">客户<br/>编码</th>--%>
				<th rowspan="2">客户名称</th>
				<%--<th rowspan="2">业务员</th>--%>
				<th rowspan="2">客服</th>
				<th rowspan="2" class="sort serviceTimes">累计<br/>上门</th>
				<%--<th rowspan="2">状态</th>--%>
				<th rowspan="2" width="80" class="sort closeDate">完成<br/>日期</th>
				<th rowspan="2" width="40">配件</th>
				<th rowspan="2" width="30">完成<br/>照片</th>
				<th colspan="8">应收款</th>
				<th colspan="10">应付款</th>
			</tr>
			<tr>
				<th>服务费</th>
				<th>快递费</th>
				<th>远程费</th>
				<th>配件费</th>
				<th>其他</th>
				<th>时效费</th>
				<th>加急费</th>
				<th>合计</th>
				<th>服务费</th>
				<th>快递费</th>
				<th>远程费</th>
				<th>配件费</th>
				<th>其他</th>
				<%--<th>小计</th>--%>
				<th>时效</th>
				<th>厂商时效</th>
				<th>加急费</th>
				<th>互助基金</th>
				<th>合计</th>
			</tr>
		</thead>
		<tbody>
		<%int i=0;%>
		<c:set var="inTotalCharge" value="0" />
		<c:set var="inTimeLinessTotalCharge" value="0" />
		<c:set var="inUrgentTotalCharge" value="0" />
		<c:set var="outTotalCharge" value="0" />
		<c:set var="insuranceTotalCharge" value="0" />
		<c:set var="customerTimeLinessTotalCharge" value="0" />
		<c:set var="urgentTotalCharge" value="0" />
		<c:set var="timeLinessTotalCharge" value="0" />
		<c:forEach items="${page.list}" var="order">
			<tr id="${order.id}" pId="0">
			<%i++;%>
				<td><c:if test="${order.orderCondition.status.value eq 80}"><input type="checkbox" id="cbox<%=i%>" value="${order.id}" name="checkedRecords"/></c:if></td>
				<td><%=i%></td>
				<td <c:if test="${order.orderCondition.pendingFlag eq 3}">style="background-color:yellow"</c:if> ><a href="javascript:void(0);" onclick="markOrder('${order.id}','${order.quarter}');"  ><abbr title="点击查看订单详情">${order.orderNo}</abbr></a></td>
				<%--<td>${order.orderCondition.customer.code}</td>--%>
				<td>${order.orderCondition.customer.name}</td>
				<%--<td>${order.orderCondition.customer.sales.name}</td>--%>
				<td>${order.orderCondition.kefu.name}</td>
				<td style="color:blue">${order.orderCondition.serviceTimes}</td>
				<%--<td><span class="label status_${order.orderCondition.status.value}">--%>
						<%--${order.orderCondition.status.label}--%>
				<%--</span>--%>
				<%--</td>--%>
				<td><fmt:formatDate value="${order.orderCondition.closeDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
				<td>
					<shiro:hasPermission name="md:material:view">
						<c:if test="${order.orderCondition.partsFlag eq 1}">
							<a href="javascript:void(0);" class="btn btn-mini btn-primary"
							   onclick="Order.attachlist('${order.id}','${order.orderNo}','${order.quarter}');"><abbr
									title="点击查看配件申请列表">配件</abbr>
							</a>
						</c:if>
					</shiro:hasPermission>
				</td>
				<td>
					<c:if test="${order.orderCondition.finishPhotoQty>0 }">
						<i class="icon-camera" title="已上传完成照片,点击查看" style="cursor: pointer;" onclick="Order.photolistNew('${order.id}','${order.quarter}',${fns:isNewOrder(order.orderNo)});" ></i>
					</c:if>
				</td>
				<td>${order.orderFee.serviceCharge}</td>
				<td>${order.orderFee.expressCharge}</td>
				<td>${order.orderFee.travelCharge}</td>
				<td>${order.orderFee.materialCharge}</td>
				<td>${order.orderFee.otherCharge}</td>
				<td>${order.orderFee.customerTimeLinessCharge}</td>
				<td>${order.orderFee.customerUrgentCharge}</td>
				<td style="color:green"><B>${order.orderFee.orderCharge}</B></td>
				<td>${order.orderFee.engineerServiceCharge}</td>
				<td>${order.orderFee.engineerExpressCharge}</td>
				<td>${order.orderFee.engineerTravelCharge}</td>
				<td>${order.orderFee.engineerMaterialCharge}</td>
				<td>${order.orderFee.engineerOtherCharge}</td>
				<%--<td style="color:red">${order.orderFee.engineerTotalCharge-order.orderFee.insuranceCharge-order.orderFee.timeLinessCharge-order.orderFee.subsidyTimeLinessCharge}</td>--%>
				<td>${order.orderFee.timeLinessCharge}</td>
				<td>${order.orderFee.subsidyTimeLinessCharge}</td>
				<td>${order.orderFee.engineerUrgentCharge}</td>
				<td>${order.orderFee.insuranceCharge}</td>
				<c:set var="inTimeLinessTotalCharge" value="${inTimeLinessTotalCharge+order.orderFee.customerTimeLinessCharge}" />
				<c:set var="inUrgentTotalCharge" value="${inUrgentTotalCharge+order.orderFee.customerUrgentCharge}" />
				<c:set var="insuranceTotalCharge" value="${insuranceTotalCharge+order.orderFee.insuranceCharge}" />
				<c:set var="customerTimeLinessTotalCharge" value="${customerTimeLinessTotalCharge+order.orderFee.subsidyTimeLinessCharge}" />
				<c:set var="urgentTotalCharge" value="${urgentTotalCharge+order.orderFee.engineerUrgentCharge}" />
				<c:set var="timeLinessTotalCharge" value="${timeLinessTotalCharge+order.orderFee.timeLinessCharge}" />
				<c:set var="inTotalCharge" value="${inTotalCharge+order.orderFee.serviceCharge+order.orderFee.expressCharge+order.orderFee.travelCharge+order.orderFee.materialCharge+order.orderFee.otherCharge}" />
				<td style="color:red"><B>${order.orderFee.engineerTotalCharge}</B></td>
			</tr>
			<c:forEach items="${order.detailList}" var="detail">
				<tr id="${detail.id}" pId="${order.id}">
					<td><td>
					<td colspan="7">第<span style="color:blue">${detail.serviceTimes}</span>次&nbsp;&nbsp;
						服务类型:<span style="color:blue">${detail.serviceType.name}</span>&nbsp;&nbsp;
						数量:<span style="color:blue">${detail.qty}</span>&nbsp;&nbsp;
						产品:${detail.product.name}&nbsp;&nbsp;
						型号/规格:${detail.productSpec}&nbsp;&nbsp;
						安维:${detail.servicePoint.servicePointNo},${detail.servicePoint.name},${detail.engineer.name}
					</td>
					<td>${detail.charge}</td>
					<td>${detail.expressCharge}</td>
					<td>${detail.travelCharge}</td>
					<td>${detail.materialCharge}</td>
					<td>${detail.otherCharge}</td>
					<td></td>
					<td></td>
					<td><B>${detail.charge+detail.expressCharge+detail.travelCharge+detail.materialCharge+detail.otherCharge}</B></td>
					<td>${detail.engineerServiceCharge}</td>
					<td>${detail.engineerExpressCharge}</td>
					<td>${detail.engineerTravelCharge}</td>
					<td>${detail.engineerMaterialCharge}</td>
					<td>${detail.engineerOtherCharge}</td>
					<%--<td><B>${detail.engineerServiceCharge+detail.engineerExpressCharge+detail.engineerTravelCharge+detail.engineerMaterialCharge+detail.engineerOtherCharge}</B></td>--%>
					<td colspan="4"></td>
					<td><B>${detail.engineerServiceCharge+detail.engineerExpressCharge+detail.engineerTravelCharge+detail.engineerMaterialCharge+detail.engineerOtherCharge}</B></td>
					<c:set var="outTotalCharge" value="${outTotalCharge+detail.engineerServiceCharge+detail.engineerExpressCharge+detail.engineerTravelCharge+detail.engineerMaterialCharge+detail.engineerOtherCharge}" />
				</tr>
			</c:forEach>
		</c:forEach>
		<tr>
			<td style="text-align:right;" colspan="9" ><B>合计</B></td>
			<td style="text-align:right;color:green;padding-right:15px;" colspan="5">${inTotalCharge}</td>
			<td style="text-align:right;padding-right:15px;" ><B>${inTimeLinessTotalCharge}</B></td>
			<td style="text-align:right;padding-right:15px;" ><B>${inUrgentTotalCharge}</B></td>
			<td style="text-align:right;color:green;padding-right:15px;" ><B>${inTotalCharge+inTimeLinessTotalCharge+inUrgentTotalCharge}</B></td>
			<td style="text-align:right;color:red;padding-right:15px;" colspan="5">${outTotalCharge}</td>
			<td style="text-align:right;padding-right:15px;"><B>${timeLinessTotalCharge}</B></td>
			<td style="text-align:right;padding-right:15px;"><B>${customerTimeLinessTotalCharge}</B></td>
			<td style="text-align:right;padding-right:15px;"><B>${urgentTotalCharge}</B></td>
			<td style="text-align:right;padding-right:15px;"><B>${insuranceTotalCharge}</B></td>
			<td style="text-align:right;color:red;padding-right:15px;"><B>${outTotalCharge+insuranceTotalCharge+customerTimeLinessTotalCharge+urgentTotalCharge+timeLinessTotalCharge}</B></td>
		</tr>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>
