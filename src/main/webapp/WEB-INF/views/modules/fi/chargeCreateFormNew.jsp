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
			     height  : 330,
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
			$("#searchForm").attr("action","${ctx}/fi/chargecreate/new?type="+$("#paymentType").val());
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
                area:['1500px','850px'],
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
		<c:set var="paymentTypes" value="${fns:getDictExceptListFromMS('PaymentType', '30')}" />
		<c:forEach items="${paymentTypes}" var="dict">
			<c:choose>
				<c:when test="${dict.value eq paymentType}">
					<li class="active"><a href="javascript:void(0);">生成对帐单(${dict.label})</a>
				</c:when>
				<c:otherwise>
					<li><a href="${ctx}/fi/chargecreate/new?type=${dict.value}">生成对帐单(${dict.label})</a>
				</c:otherwise>
			</c:choose>
			</li>
		</c:forEach>
	</ul>
	<form:form id="searchForm" action="${ctx}/fi/chargecreate/new?type=${paymentType}" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<input id="orderBy" name="orderBy" type="hidden" value="${page.orderBy}"/>
		<input id="selectedIds" name="selectedIds" type="hidden" value="${selectedIds}"/>
		<input id="paymentType" name="paymentType" type="hidden" value="${paymentType}"/>
		<a id="aSave" type="hidden"></a>
		<div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
			注:当用工单号进行搜索查询时，不受当前时间、地区等其他条件的限制。
		</div>
		<div>
			<label>客　　户：</label>
			<select name="customerId" class="input-large" style="width:258px;">
				<option value="">所有</option>
				<c:forEach items="${fns:getMyCustomerListFromMS()}" var="customer">
					<option value="${customer.id}"  <c:out value="${customer.id==customerId ?'selected':''}"/>>${customer.name}</option>
				</c:forEach>
			</select>
			<label class="control-label">产品类别：</label>
				<sys:treeselect id="category" name="productCategoryId" value="${productCategoryId}" labelName="productCategoryName" labelValue="${productCategoryName}"
				title="产品类别" url="/md/productcategory/treeData" allowClear="true" cssStyle="width:140px;"/>
		</div>
		<div style="margin-top:8px">
			<label>完成日期：</label><input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:98px;margin-left:4px" maxlength="20" class="input-small Wdate"
				value="${beginDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			<label>~</label>&nbsp;&nbsp;&nbsp;<input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
				value="${endDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			<label>订单编号：</label>
			<input type = "text" id="orderNo" name="orderNo" value="${orderNo}" maxlength="30" class="input-small" style="width:179px;"/>
		</div>
		<div style="margin-top:8px">
			<label>服务网点：</label>
			<sd:servicePointSelect id="servicePoint" name="servicePointId" value="${servicePointId}" labelName="servicePointName" labelValue="${servicePointName}"
				 width="1200" height="780" title="选择服务网点" areaId="" cssClass="required"
				 showArea="false" allowClear="true" callbackmethod="" />
			<label>上门次数：</label>
			<input type = "text" id="serviceTimes" name="serviceTimes" value="${serviceTimes}" maxlength="3" class="input-small" style="width:50px;"/>
			<input type="checkbox" id="travelChargeFlag" name="travelChargeFlag" <c:out value="${(travelChargeFlag eq 'on')?'checked=true':''}" ></c:out>><label for="travelChargeFlag" style="margin: 0">远程</label></input>
			<input type="checkbox" id="partsFlag" name="partsFlag" <c:out value="${(partsFlag eq 'on')?'checked=true':''}" ></c:out>><label for="partsFlag" style="margin: 0">配件</label></input>
			<input type="checkbox" id="otherChargeFlag" name="otherChargeFlag" <c:out value="${(otherChargeFlag eq 'on')?'checked=true':''}" ></c:out>><label for="otherChargeFlag" style="margin: 0">其他</label></input>
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
				<th rowspan="2">客户名称</th>
				<th rowspan="2">客服</th>
				<th rowspan="2" class="sort serviceTimes">累计<br/>上门</th>
				<th rowspan="2" width="80" class="sort closeDate">完成<br/>日期</th>
				<th rowspan="2" width="40">配件</th>
				<th rowspan="2" width="30">完成<br/>照片</th>
				<th colspan="9">应收款</th>
				<th colspan="11">应付款</th>
			</tr>
			<tr>
				<th>服务费</th>
				<th>快递费</th>
				<th>远程费</th>
				<th>配件费</th>
				<th>其他</th>
				<th>时效费</th>
				<th>加急费</th>
				<th>好评费</th>
				<th>合计</th>
				<th>服务费</th>
				<th>快递费</th>
				<th>远程费</th>
				<th>配件费</th>
				<th>其他</th>
				<th>时效</th>
				<th>厂商时效</th>
				<th>加急费</th>
				<th>互助基金</th>
				<th>好评费</th>
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
		<c:set var="praiseTotalFee" value="0" />
		<c:set var="engineerPraiseTotalFee" value="0" />
		<c:forEach items="${page.list}" var="order">
			<tr id="${order.orderId}" pId="0">
			<%i++;%>
				<td><c:if test="${order.status eq 80}"><input type="checkbox" id="cbox<%=i%>" value="${order.orderId}" name="checkedRecords"/></c:if></td>
				<td><%=i%></td>
				<td <c:if test="${order.pendingFlag eq 3}">style="background-color:yellow"</c:if> ><a href="javascript:void(0);" onclick="markOrder('${order.orderId}','${order.quarter}');"  ><abbr title="点击查看订单详情">${order.orderNo}</abbr></a></td>
				<td>${order.customerName}</td>
				<td>${order.kefuName}</td>
				<td style="color:blue">${order.serviceTimes}</td>
				<td><fmt:formatDate value="${order.closeDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
				<td>
					<shiro:hasPermission name="md:material:view">
						<c:if test="${order.partsFlag eq 1}">
							<a href="javascript:void(0);" class="btn btn-mini btn-primary"
							   onclick="Order.attachlist('${order.orderId}','${order.orderNo}','${order.quarter}');"><abbr
									title="点击查看配件申请列表">配件</abbr>
							</a>
						</c:if>
					</shiro:hasPermission>
				</td>
				<td>
					<c:if test="${order.finishPhotoQty>0 }">
						<i class="icon-camera" title="已上传完成照片,点击查看" style="cursor: pointer;" onclick="Order.photolistNew('${order.orderId}','${order.quarter}',${fns:isNewOrder(order.orderNo)});" ></i>
					</c:if>
				</td>
				<td><c:if test="${order.serviceCharge ne 0}">${order.serviceCharge}</c:if></td>
				<td><c:if test="${order.expressCharge ne 0}">${order.expressCharge}</c:if></td>
				<td><c:if test="${order.travelCharge ne 0}">${order.travelCharge}</c:if></td>
				<td><c:if test="${order.materialCharge ne 0}">${order.materialCharge}</c:if></td>
				<td><c:if test="${order.otherCharge ne 0}">${order.otherCharge}</c:if></td>
				<td><c:if test="${order.customerTimeLinessCharge ne 0}">${order.customerTimeLinessCharge}</c:if></td>
				<td><c:if test="${order.customerUrgentCharge ne 0}">${order.customerUrgentCharge}</c:if></td>
				<td><c:if test="${order.praiseFee ne 0}">${order.praiseFee}</c:if></td>
				<td style="color:green"><B><c:if test="${order.orderCharge ne 0}">${order.orderCharge}</c:if></B></td>
				<td><c:if test="${order.engineerServiceCharge ne 0}">${order.engineerServiceCharge}</c:if></td>
				<td><c:if test="${order.engineerExpressCharge ne 0}">${order.engineerExpressCharge}</c:if></td>
				<td><c:if test="${order.engineerTravelCharge ne 0}">${order.engineerTravelCharge}</c:if></td>
				<td><c:if test="${order.engineerMaterialCharge ne 0}">${order.engineerMaterialCharge}</c:if></td>
				<td><c:if test="${order.engineerOtherCharge ne 0}">${order.engineerOtherCharge}</c:if></td>
				<td><c:if test="${order.timeLinessCharge ne 0}">${order.timeLinessCharge}</c:if></td>
				<td><c:if test="${order.subsidyTimeLinessCharge ne 0}">${order.subsidyTimeLinessCharge}</c:if></td>
				<td><c:if test="${order.engineerUrgentCharge ne 0}">${order.engineerUrgentCharge}</c:if></td>
				<td><c:if test="${order.insuranceCharge ne 0}">${order.insuranceCharge}</c:if></td>
				<td><c:if test="${order.engineerPraiseFee ne 0}">${order.engineerPraiseFee}</c:if></td>
				<c:set var="inTimeLinessTotalCharge" value="${inTimeLinessTotalCharge+order.customerTimeLinessCharge}" />
				<c:set var="inUrgentTotalCharge" value="${inUrgentTotalCharge+order.customerUrgentCharge}" />
				<c:set var="insuranceTotalCharge" value="${insuranceTotalCharge+order.insuranceCharge}" />
				<c:set var="customerTimeLinessTotalCharge" value="${customerTimeLinessTotalCharge+order.subsidyTimeLinessCharge}" />
				<c:set var="urgentTotalCharge" value="${urgentTotalCharge+order.engineerUrgentCharge}" />
				<c:set var="timeLinessTotalCharge" value="${timeLinessTotalCharge+order.timeLinessCharge}" />
				<c:set var="inTotalCharge" value="${inTotalCharge+order.serviceCharge+order.expressCharge+order.travelCharge+order.materialCharge+order.praiseFee+order.otherCharge}" />
				<c:set var="praiseTotalFee" value="${praiseTotalFee+order.praiseFee}" />
				<c:set var="engineerPraiseTotalFee" value="${engineerPraiseTotalFee+order.engineerPraiseFee}" />
				<td style="color:red"><B>${order.engineerTotalCharge}</B></td>
			</tr>
			<c:forEach items="${order.details}" var="detail">
				<tr id="${detail.id}" pId="${order.orderId}">
					<td><td>
					<td colspan="7">第<span style="color:blue">${detail.serviceTimes}</span>次&nbsp;&nbsp;
						服务类型:<span style="color:blue">${detail.serviceTypeName}</span>&nbsp;&nbsp;
						数量:<span style="color:blue">${detail.qty}</span>&nbsp;&nbsp;
						产品:${detail.productName}&nbsp;&nbsp;
						型号/规格:${detail.productSpec}&nbsp;&nbsp;
						网点:${detail.servicePointNo},${detail.servicePointName},${detail.engineerName}
					</td>
					<td><c:if test="${detail.charge ne 0}">${detail.charge}</c:if></td>
					<td><c:if test="${detail.expressCharge ne 0}">${detail.expressCharge}</c:if></td>
					<td><c:if test="${detail.travelCharge ne 0}">${detail.travelCharge}</c:if></td>
					<td><c:if test="${detail.materialCharge ne 0}">${detail.materialCharge}</c:if></td>
					<td><c:if test="${detail.otherCharge ne 0}">${detail.otherCharge}</c:if></td>
					<td colspan="3"></td>
					<td><B>${detail.charge+detail.expressCharge+detail.travelCharge+detail.materialCharge+detail.otherCharge}</B></td>
					<td><c:if test="${detail.engineerServiceCharge ne 0}">${detail.engineerServiceCharge}</c:if></td>
					<td><c:if test="${detail.engineerExpressCharge ne 0}">${detail.engineerExpressCharge}</c:if></td>
					<td><c:if test="${detail.engineerTravelCharge ne 0}">${detail.engineerTravelCharge}</c:if></td>
					<td><c:if test="${detail.engineerMaterialCharge ne 0}">${detail.engineerMaterialCharge}</c:if></td>
					<td><c:if test="${detail.engineerOtherCharge ne 0}">${detail.engineerOtherCharge}</c:if></td>
					<td colspan="5"></td>
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
			<td style="text-align:right;padding-right:15px;" ><B>${praiseTotalFee}</B></td>
			<td style="text-align:right;color:green;padding-right:15px;" ><B>${inTotalCharge+inTimeLinessTotalCharge+inUrgentTotalCharge+praiseTotalFee}</B></td>
			<td style="text-align:right;color:red;padding-right:15px;" colspan="5">${outTotalCharge}</td>
			<td style="text-align:right;padding-right:15px;"><B>${timeLinessTotalCharge}</B></td>
			<td style="text-align:right;padding-right:15px;"><B>${customerTimeLinessTotalCharge}</B></td>
			<td style="text-align:right;padding-right:15px;"><B>${urgentTotalCharge}</B></td>
			<td style="text-align:right;padding-right:15px;"><B>${insuranceTotalCharge}</B></td>
			<td style="text-align:right;padding-right:15px;"><B>${engineerPraiseTotalFee}</B></td>
			<td style="text-align:right;color:red;padding-right:15px;"><B>${outTotalCharge+insuranceTotalCharge+customerTimeLinessTotalCharge+urgentTotalCharge+timeLinessTotalCharge+engineerPraiseTotalFee}</B></td>
		</tr>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>
