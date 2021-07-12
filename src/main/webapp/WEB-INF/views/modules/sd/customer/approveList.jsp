<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>订单审核-主帐号</title>
	  <%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script src="${ctxStatic}/area/AreaFourLevel.js" type="text/javascript"></script>
	<script type="text/javascript">
		top.layer.closeAll();
        Order.rootUrl = "${ctx}";
        //覆盖分页前方法
        function beforePage(){
            var $btnSubmit  = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            // $btnSubmit.val("...");
            $("#btnClearSearch").attr('disabled', 'disabled');
            layerLoading("查询中...",true);
        }
        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function(){
            if (clicktag == 0) {
                clicktag = 1;
                beforePage();
                setPage();
                this.form.submit();
            }
        });
		$(document).on("change", "#selectAll", function() {
			 var $check = $(this);
			 $("input:checkbox").each(function(){
				 if ($(this).val() != "on"){
					 if ($check.prop("checked") == true) {
						 $(this).prop("checked", true);
					 }
					 else{
						 $(this).prop("checked", false);
					 }
					 //selectBoxChanged(this);
				 }
			 });
		});

		$(document).on("click", "#btnApprove", function () {
            var self = this;
            var values = [];
            if($("input[type='checkbox'][name='checkedRecords']:checked").length == 0){
                layerError('请选择要审核的订单', '错误提示');
                return;
            }

            $.each($("input[type=checkbox][name='checkedRecords']:checked"),function(){
                var order = {};
                order.orderId = $(this).val();
                order.version = $(this).data("version");
                order.quarter = $(this).data("quarter");
                values.push(order);
            });
            if(clicktag == 1){
                return false;
            }
            $(self).attr('disabled', 'disabled');
			clicktag = 1;
			var confirmtag = 0;
            top.layer.confirm('确定要审核订单吗?', {icon: 3, title:'系统确认'}, function(index){
                top.layer.close(index);//关闭本身
				if(confirmtag == 1){
					return false;
				}
                    confirmtag = 1;
				// do something
				var loadingIndex;
                $.ajax({
                    async: false,
                    cache: false,
                    type: "POST",
                    contentType: "application/json; charset=utf-8",
                    dataType:"json",
                    url: "${ctx}/sd/order/approve",
                    data: JSON.stringify(values),
                    beforeSend: function () {
                        loadingIndex = top.layer.msg('正在审核订单，请稍等...', {
                            icon: 16,
                            time: 0,//不定时关闭
                            shade: 0.3
                        });
                    },
                    complete: function () {
                        if(loadingIndex) {
                            top.layer.close(loadingIndex);
                        }
                        $(self).removeAttr('disabled');
                        clicktag = 0;
                        confirmtag = 0;
                    },
                    success: function (data) {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if (data.success){
                            layerMsg('订单审核成功!');
                            location.href="${ctx}/sd/order/customer/approvelist";
                        }
                        else{
                            layerError(data.message,'错误提示');
                        }
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText,null,"订单审核错误，请重试!");
                    }
                });
            },
			function (index) {
				//cancel
                clicktag = 0;
                $(self).removeAttr('disabled');
            });
            return false;
	    });
	</script>
	<style>
		.form-search .input-append .btn { border-radius: 0px 4px 4px 0px; }
	</style>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:;">待审核列表</a></li>
	</ul>
	<sys:message content="${message}"/>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<form:form id="searchForm" modelAttribute="order" action="${ctx}/sd/order/customer/approvelist" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<div>
			<c:choose>
				<c:when test="${currentuser.isCustomer()}">
					<form:hidden path="customer.id" />
					<form:hidden path="customer.name" />
				</c:when>
				<c:otherwise>
					<label>客户:</label>
					<form:select path="customer.id" style="width:318px;">
						<form:option value="0" label="所有" />
						<form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id"
									  htmlEscape="false"/>
					</form:select>
				</c:otherwise>
			</c:choose>
			<label>用户：</label><input type=text class="input-small" id="userName" name="userName" value="${order.userName}" maxlength="20" />
			<label>区域：</label>
			<%--
			<sys:treeselect id="area" name="area.id" value="${order.area.id}" labelName="area.name" labelValue="${order.area.name}"
							title="区域" url="/sys/area/treeData" nodeLevel="true" levelValue="${order.areaLevel}" allowClear="true" />
							--%>
			<sys:areaSelectFourLevel id="area" name="area.id"  value="${order.area.id}" levelValue="${order.areaLevel}"
									 labelValue="${order.area.fullName}" labelName="area.fullName" title=""
									 mustSelectCounty="true" cssClass="required" showMaxLevel="3"></sys:areaSelectFourLevel>
		</div>
		<div style="margin-top:8px">
			<label>下单日期：</label><input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:95px;margin-left:4px" maxlength="20" class="input-small Wdate" value="${fns:formatDate(order.beginDate,'yyyy-MM-dd')}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			<label>~</label>&nbsp;&nbsp;&nbsp;<input id="endDate" name="endDate" type="text" readonly="readonly" style="width:95px" maxlength="20" class="input-small Wdate"
				value="${fns:formatDate(order.endDate,'yyyy-MM-dd')}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			<shiro:hasPermission name="sd:order:approvenew">
			&nbsp;&nbsp;<c:if test="${canSearch==true}">
			<input id="btnSubmit" class="btn btn-primary"  type="submit" value="查询"/>
			&nbsp;&nbsp;<input type="button" id="btnApprove" style="margin-left:20px;" class="btn btn-primary" value="审核"/>
			</c:if>
			</shiro:hasPermission>
		</div>
	</form:form>
	<c:set var="rowNumber" value="0" />
	<table id="contentTable" class="table table-striped table-bordered table-condensed">
		<thead>
		<tr>
			<th width="30px"><input type="checkbox" id="selectAll" name="selectAll"/></th>
			<th width="30px">序号</th>
			<th>单号</th>
			<th width="100">来源</th>
			<th>下单时间</th>
			<th>用户</th>
			<th>电话1</th>
			<th>电话2</th>
			<th>地址</th>
			<th>服务描述</th>
			<shiro:hasPermission name="sd:order:showreceive">
			<th>服务金额</th>
			<th>冻结金额</th>
			<th>合计</th>
			</shiro:hasPermission>
			<shiro:hasPermission name="sd:order:edit">
				<th width="70">操作</th>
			</shiro:hasPermission>
		</tr>
		</thead>
		<tbody>
			<c:forEach items="${page.list}" var="order" varStatus="i" begin="0">
			<tr>
			<td><input type="checkbox" id="cbox${i.index}" value="${order.id}" data-version="${order.orderCondition.version}" data-quarter="${order.quarter}" data-no="${order.orderNo}" name="checkedRecords"/></td>
			<td>${i.index+1}</td>
			<td><a href ="javascript:void(0);"  onclick="Order.viewOrderDetail('${order.id}','${order.quarter}');" class="orderdetailinfo" ><abbr title="点击查看订单详情">${order.orderNo}</abbr></a></td>
			<td >${order.dataSource.label}<br>${o.b2bShop==null?"":o.b2bShop.shopName}</td>
			<td><fmt:formatDate value="${order.orderCondition.createDate}" pattern="yyyy-MM-dd HH:mm"/></td>
			<td>${order.orderCondition.userName}</td>
			<td>${order.orderCondition.phone1}</td>
			<td>${order.orderCondition.phone2}</td>
			<td>${order.orderCondition.area.name} ${order.orderCondition.address}</td>
			<td>${order.description}</td>
				<shiro:hasPermission name="sd:order:showreceive">
			<td>${order.orderFee.expectCharge}</td>
			<td>${order.orderFee.blockedCharge}</td>
			<td>${order.orderFee.expectCharge+order.orderFee.blockedCharge}</td>
				</shiro:hasPermission>
			<shiro:hasPermission name="sd:order:edit">
				<td>
					<div class="btn-group">
						<a class="btn btn-primary dropdown-toggle"
							data-toggle="dropdown" href="#">操作 <span class="caret"></span>
						</a>
						<ul class="dropdown-menu">
								<shiro:hasPermission name="sd:order:edit">
									<li><a
										href="${ctx}/sd/order/edit?id=${order.id}&actiontype=approve" ><i
											class="icon-pencil"></i>修改</a></li>
								</shiro:hasPermission>
								<shiro:hasPermission name="sd:order:cancel">
									<li><a href="javascript:void(0);" onclick="Order.cancelOrder('${order.id}','${order.orderNo}','${order.quarter}')"><i class="icon-ban-circle"></i>取消</a></li>
								</shiro:hasPermission>
						</ul>
					</div>
				</td>
			</shiro:hasPermission>
			</tr>
			</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>
