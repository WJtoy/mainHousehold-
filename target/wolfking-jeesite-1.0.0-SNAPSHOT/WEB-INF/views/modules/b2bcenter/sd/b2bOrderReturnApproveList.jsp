<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
	<title>退单审核</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<%@ include file="/WEB-INF/views/include/WdateLimitPicker.jsp" %>
	<script type="text/javascript">
        top.layer.closeAll();
        Order.rootUrl = "${ctx}";
		var clickTag = 0;
		$(document).ready(function() {
			oneYearDatePicker('beginDate','endDate',false);
		});
		$(document).on("change", "#selectAll", function() {
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

		function returnaction(btn){
            var $btnApprove = $("#btnApprove");
            var $btnReject = $("#btnReject");

            $btnApprove.attr("disabled", "disabled");
            $btnReject.attr("disabled", "disabled");

			var action ="approve";
			var actionname = "同意退单申请";
			if($(btn).attr("id") != "btnApprove"){
				action = "reject";
				actionname="驳回退单申请";
			}

			var ids = [];
			var orders = [];
			var o;
			var _this;
			$("input:checkbox").each(function(){
				 if ($(this).attr("checked")){
					 _this = $(this);
				 	var id=_this.attr("id");
				 	if(id != "selectAll")
				 	{
						// ids.push($(this).val());
				 		o = {orderId:_this.val(),quarter:_this.data("quarter")};
						orders.push(o);
				 	}
				 }
			 });

	        if (orders.length == 0) {
	            layerAlert('请选择要'+actionname+'的订单', '系统提示');
                $btnApprove.removeAttr('disabled');
                $btnReject.removeAttr('disabled');
                clickTag = 0;
	            return false;
	        }

            top.layer.confirm(
                '确定' + actionname + '吗？'
                ,{id:'layer_orderreturnform',icon: 3,closeBtn: 0,title:'系统确认',success: function(layro, index) {
                    $(document).on('keydown', layro, function(e) {
                        if (e.keyCode == 13) {
                            layro.find('a.layui-layer-btn0').trigger('click')
                        }else if(e.keyCode == 27){
                            $btnApprove.removeAttr('disabled');
                            $btnReject.removeAttr('disabled');
                            top.layer.close(index);//关闭本身
                        }
                    });
                	}
                }
                ,function(index){
                    if(clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    top.layer.close(index);//关闭本身
                    // do something
                    var loadingIndex;
                    var data = {json:JSON.stringify(orders),action:action};
                    $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: "${ctx}/sd/order/approvereturn",
                        data: data,
                        beforeSend: function () {
                            loadingIndex = top.layer.msg('正在提交数据，请稍等...', {
                                icon: 16,
                                time: 0,//不定时关闭
                                shade: 0.3
                            });
                        },
                        complete: function () {
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
							setTimeout(function () {
								clickTag = 0;
								$btnApprove.removeAttr('disabled');
								$btnReject.removeAttr('disabled');
							}, 2000);
                        },
                        success: function (data) {
                            setTimeout(function() {
                                if(ajaxLogout(data)){
                                    return false;
                                }
								if (data.success){
									if(data.message){
										layerMsg(data.message);
									}
									else{
										layerMsg('退单' + actionname +'成功');
									}
									search();
									return false;
								}
								else{
									layerError(data.message);
								}
                            }, 300);
                            return false;
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,'退单' + actionname +'错误，请重试!');
                        }
                    });
                    return false;
                },function(index){//cancel
                    $btnApprove.removeAttr('disabled');
                    $btnReject.removeAttr('disabled');
                    clickTag = 0;
                });
            return false;
		}

	</script>
  </head>
  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">待审核B2B工单列表</a></li>
	</ul>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<form:form id="searchForm" modelAttribute="order"  action="${ctx}/b2b/b2bcenter/abnormalOrder/b2bOrderReturnApproveList" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
			注:当用订单号和电话进行搜索查询时，不受当前时间等其他条件的限制。
		</div>
		<div>
			<c:if test="${currentuser.userType == 3 || currentuser.userType== 4 || currentuser.userType== 9}">
				<label>客户：</label><input type="hidden"  id="customerId" name="customerId" value="${order.customer.id}" />
				<input type="text" readonly="true" id="customerName" name="customerName" value="${order.customer.name}" />
			</c:if>
			<label>用户：</label><input type=text class="input-small" id="userName" name="userName" style="width: 60px;" value="${order.userName}" maxlength="20" />
			<label>电话：</label><input type=text class="input-small" id="phone1" name="phone1" style="width: 90px;" value="${order.phone1}" maxlength="20" />
			<label>订单号：</label><input type=text class="input-small" id="orderNo" name="orderNo" style="width: 120px;" value="${order.orderNo}" maxlength="20" />
			<label>退单人：</label><input type=text class="input-small" id="returnBy" name="returnBy" style="width: 60px;" value="${order.returnBy}" maxlength="20" />
		</div>
		<div style="margin-top: 5px;">
			<label>退单日期：</label><input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:95px;margin-left:4px" maxlength="20" class="input-small Wdate"
									   value="${fns:formatDate(order.beginDate,'yyyy-MM-dd')}" />
			<label>~</label>&nbsp;&nbsp;&nbsp;<input id="endDate" name="endDate" type="text" readonly="readonly" style="width:95px" maxlength="20" class="input-small Wdate"
													 value="${fns:formatDate(order.endDate,'yyyy-MM-dd')}" />
			&nbsp;&nbsp;
			<c:if test="${canSearch==true}">
				<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>&nbsp;&nbsp;
				&nbsp;&nbsp;&nbsp;&nbsp;
					<div class="btn-group">
						<a class="btn btn-success" id="btnApprove" href="javascript:;" onclick="returnaction(this);" title="同意申请">同意 <i class="icon-ok"></i></a>
						<a class="btn btn-danger" id="btnReject" href="javascript:;" onclick="returnaction(this);" title="驳回申请">驳回 <i class="icon-ban-circle"></i></a>
					</div>
			</c:if>
		</div>
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="20"><input type="checkbox" id="selectAll" name="selectAll"/></th>
				<th width="40">序号</th>
				<th width="120">单号</th>
				<th width="80">下单时间</th>
				<th width="80">退单时间</th>
				<th width="60">退单人</th>
				<th width="60">退单类型</th>
				<th width="220">退单原因</th>
				<th width="60">用户</th>
				<th width="80">电话1</th>
				<th width="80">电话2</th>
				<th width="160">安维详细地址</th>
				<th width="120">服务描述</th>
				<th width="60">派单价</th>
				<th width="60">冻结金额</th>
				<th width="60">合计</th>
			</tr>
		</thead>
		<tbody>
			<c:set var="rowcnt" value="${page.list.size()}"/>
			<c:forEach items="${page.list}" var="order" varStatus="i" begin="0">
				<tr>
					<td><input type="checkbox" id="cbox${i.index}" data-quarter="${order.quarter}" value="${order.id}" name="checkedRecords"/></td>
					<td>${i.index+1}</td>
					<td><a href="javascript:void(0);" onclick="Order.viewOrderDetail('${order.id}','${order.quarter}');"><abbr title="点击查看订单详情">${order.orderNo}</abbr></a></td>
					<td><fmt:formatDate value="${order.orderCondition.createDate}" pattern="yyyy-MM-dd HH:mm"/></td>
					<td><fmt:formatDate value="${order.orderStatus.cancelApplyDate}" pattern="yyyy-MM-dd HH:mm"/></td>
					<td>${order.orderStatus.cancelApplyBy.name}</td>
					<td>${order.orderStatus.cancelResponsible.label}</td>
					<td>${order.orderStatus.cancelApplyComment}</td>
					<td>${order.orderCondition.userName}</td>
					<td>${order.orderCondition.phone1}</td>
					<td>${order.orderCondition.phone2}</td>
					<td>${order.orderCondition.area.name}&nbsp;${order.orderCondition.address}</td>
					<td>
						<a href="javascript:void(0);" data-toggle="tooltip"
						   data-tooltip="${order.description}">${fns:abbr(order.description,30)}</a>
					</td>
					<td>${order.orderFee.expectCharge}</td>
					<td>${order.orderFee.blockedCharge}</td>
					<td>${order.orderFee.expectCharge + order.orderFee.blockedCharge}</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<c:if test="${rowcnt > 0}">
		<div class="pagination">${page}</div>
	</c:if>
  </body>
</html>
