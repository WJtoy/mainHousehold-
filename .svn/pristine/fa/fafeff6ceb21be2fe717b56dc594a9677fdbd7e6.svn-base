<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>上游客户结帐</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	
<style type="text/css">
.table thead th,.table tbody td {
	text-align: center;
	vertical-align: middle;
	BackColor: Transparent;
}
</style>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#treeTable").treeTable({expandLevel : 1});
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
            $("a#aSave").fancybox({
                maxWidth : 1000,
                maxHeight : 800,
                fitToView : false,
                width  : '100%',
                height  : '100%',
                autoSize : false,
                closeClick : false,
                type  : 'iframe',
                openEffect : 'none',
                closeEffect : 'none'
            });
		});
		function page(n,s){
			$("#pageNo").val(n);
			$("#pageSize").val(s);
			$("#searchForm").submit();
	    	return false;
	    }
		function repage(){
			$("#searchForm").submit();
	    	return false;
		}
		function validSearch(){
			if ($("#customerId").val()==""){
				top.$.jBox.error("请选择客户!");
			}
			else{
				top.$.jBox.tip('请稍候...', 'loading');
				$("#searchForm").submit();
			}
		}
		var ids = []; 	
		var totalCharge = 0;		
		function setIds(){
			ids = [];
			totalCharge = 0;
			$("input:checkbox").each(function(){
				 if ($(this).attr("checked")){
				 	var temp=$(this).attr("id");
				 	if(temp!="selectAll")
				 	{
				 		temp=$(this).next().attr("value");
						if(temp!="")
						{
						 	ids.push($(this).val());
						 	var charge = Number($(this).attr("name"));
						 	totalCharge = totalCharge + charge;
						 }
				 	}
				 }
			 });
		}
		$(document).on("click", "#btnSave", function () { 
			setIds();
	        if (ids.length == 0) { 
	            top.$.jBox.error('请选择要结帐的对帐单', '上游客户结帐');
	            return; 
	        } 
	        
	        var submit = function (v, h, f) {
	            if (v == 'ok') {
	                $(this).attr("disabled", "disabled"); 
	    	 		$("#aSave").attr("href", "${ctx}/fi/customerinvoice/save?customerId="+$("#customerId").val()+"&totalCharge="+totalCharge);
	    	 		$("#aSave").val(ids);
	    	 		$("#aSave").click();
	            }
	            else if (v == 'cancel') {
	                // 取消
	            }

	            return true; //close
	        };

	        top.$.jBox.confirm('确定要结帐吗？', '上游客户结帐', submit);

	    });
		function openjBox(url,title,width,height){
			top.$.jBox.open("iframe:" + url , title, width, height,{top:'5%',buttons:{}	});
		} 
	</script>
  </head>
  
  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">上游客户结帐</a></li>
	</ul>
	<form:form id="searchForm" action="${ctx}/fi/customerinvoice" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<input id="customerId" name="customerId" type="hidden" value="${customerId}"/>
		<a id="aSave" type="hidden"></a>
		<div>
			<label>客　　户：</label>
			<sys:treeselect id="customer" name="customerId" value="${customerId}" labelName="customerName" labelValue="${customerName}"
				title="客户" url="/md/customer/treeData" allowClear="false"/>
			<label>下单日期：</label>
			<input id="createBeginDate" name="createBeginDate" type="text" readonly="readonly" style="width:98px; maxlength="20" class="input-small Wdate"
				value="${createBeginDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
			<label>~</label>&nbsp;&nbsp;&nbsp;<input id="createEndDate" name="createEndDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
				value="${createEndDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
			&nbsp;&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询" onclick="top.$.jBox.tip('请稍候...', 'loading');"/>
		</div>
		<div style="margin-top:8px">
			<label>对帐日期：</label>
			<input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:98px;" maxlength="20" class="input-small Wdate"
				value="${beginDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			<label>~</label>&nbsp;&nbsp;&nbsp;<input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
				value="${endDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>		
			<label>订单编号：</label>
			<input type = "text" id="orderNo" name="orderNo" value="${orderNo}" maxlength="30" class="input-small"/>
			<label>完成日期：</label>
			<input id="closeBeginDate" name="closeBeginDate" type="text" readonly="readonly" style="width:98px; maxlength="20" class="input-small Wdate"
				value="${closeBeginDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
			<label>~</label>&nbsp;&nbsp;&nbsp;<input id="closeEndDate" name="closeEndDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
				value="${closeEndDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
			<shiro:hasPermission name="fi:customerinvoice:edit">
				&nbsp;&nbsp;<input id="btnSave" class="btn btn-success" type="button" value="结帐"/>
			</shiro:hasPermission>
		</div>
	</form:form>
	<sys:message content="${message}"/>
	<table id="treeTable" class="table table-striped table-bordered table-condensed">
		<thead>
			<tr>
				<th width="40px"><input type="checkbox" id="selectAll" name="selectAll"/></th>
				<th>序号</th>
				<th>客户编码</th>
				<th>客户名称</th>
				<th>订单编号</th>
				<th>下单时间</th>
				<th>完成时间</th>
				<th>上门次数</th>
				<th>创建时间</th>
				<th>服务费</th>
				<th>拆机费</th>
				<th>远程费</th>
				<th>配件费</th>
				<th>其他</th>
				<th>合计</th>
			</tr>
		</thead>
		<tbody>
		<%int i=0; %>
		<c:set var="totalCharge" value="0" />
		<c:set var= "checkOrderNo"  value = "" ></c:set>
		<c:forEach items="${page.list}" var="customerCharge">
			<!--标记重复订单-->
			<c:if test="${checkOrderNo eq customerCharge.orderNo}">
				<tr id="${customerCharge.id}" pId="0" style="background-color:#F65D20;background-clip: border-box;">
			</c:if>
			<c:if test="${checkOrderNo ne customerCharge.orderNo}">
				<tr id="${customerCharge.id}" pId="0" >
			</c:if>
			
			<%i++;%>
				<td><input type="checkbox" id="cbox<%=i%>" value="${customerCharge.id}" name="${customerCharge.serviceCharge+customerCharge.materialCharge+customerCharge.expressCharge+customerCharge.travelCharge+customerCharge.otherCharge}"/></td>
				<td><%=i%></td>
				<td>${customerCharge.customer.code}</td>
				<td>${customerCharge.customer.name}</td>
				<td><a href="javascript:void(0);" onclick="openjBox('${ctx}/sd/order/ordernewdetailinfo?orderId=${customerCharge.orderId}','订单详情',1000,650)"  ><abbr title="点击查看订单详情">${customerCharge.orderNo}</abbr></a></td>
				<td><fmt:formatDate value="${customerCharge.condition.orderCreateDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
				<td><fmt:formatDate value="${customerCharge.condition.orderCloseDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
				<td style="color:blue;">${customerCharge.serviceTimes}</td>
				<td><fmt:formatDate value="${customerCharge.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
				<td>${customerCharge.serviceCharge}</td>
				<td>${customerCharge.expressCharge}</td>
				<td>${customerCharge.travelCharge}</td>
				<td>${customerCharge.materialCharge}</td>
				<td>${customerCharge.otherCharge}</td>
				<td style="color:green"><B>${customerCharge.serviceCharge+customerCharge.materialCharge+customerCharge.expressCharge+customerCharge.travelCharge+customerCharge.otherCharge}</B></td>
				<c:set var="totalCharge" value="${totalCharge+customerCharge.serviceCharge+customerCharge.materialCharge+customerCharge.expressCharge+customerCharge.travelCharge+customerCharge.otherCharge}" />
				<c:set var= "checkOrderNo"  value = "${customerCharge.orderNo}" ></c:set>
			</tr>			
			<c:if test="${customerCharge.chargeOrderType eq 1}">
			<tr>
				<td></td>
				<td style="background-color: #f2dede;" colspan="2">
					退补描述
				</td>
				<td style="background-color: #f2dede;" colspan="12">${customerCharge.remarks}</td>
			</tr>
			</c:if>
		</c:forEach>
		<tr>
			<td style="text-align:right;" colspan="9" ><B>合计</B></td>
			<td style="text-align:right;color:green;padding-right:15px;" colspan="6"><B>${totalCharge}</B>
			<input id="totalCharge" name="totalCharge" type="hidden" value="${totalCharge}"/>
			</td>
		</tr>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>
