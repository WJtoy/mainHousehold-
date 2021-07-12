<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>客户服务价格审核</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
	    var ids = [];
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
					 selectBoxChanged(this);
				 }
			 });
		});
		$(document).on("change", "#contentTable input[type='checkbox'][name='checkedRecords']", function() {
			selectBoxChanged(this);
		});
		function selectBoxChanged(c){
			if ($(c).prop("checked") == true) {
	            var checked = jQuery.inArray($(c).val(), ids);
	            if (checked == -1) {
	                ids.push($(c).val());
	            }
	        }
	        else {
	        	$("#selectAll").prop("checked", false);
	            var checked = jQuery.inArray($(c).val(), ids);
	            if (checked > -1) {
	                //remove id from list.
	                ids = $.grep(ids, function (item, index) {
	                    return item != $(c).val();
	                });
	            }
	        }
		}

		function go2Edit(priceId,customerId){
		    var qFirstSearch=1;
		    if(customerId!=null && customerId!=''&& customerId>0){
                qFirstSearch = 0
			}else{
                customerId = 0;
			}
	    	window.location="${ctx}/md/customer/price/form?id="+priceId+"&qFirstSearch=" + qFirstSearch+"&qCustomerId=" + customerId + "&examine=examine";
	    }

		$(document).on("click", "#btnApprove", function () {
	        if (ids.length == 0) {
	            top.$.jBox.error('请选择要审核的记录', '审核服务价格');
	            return;
	        }

	        var submit = function (v, h, f) {
	            if (v == 'ok') {
	                top.$.jBox.tip('正在审核服务价格...', 'loading');
	    	 		$(this).prop("disabled", true);

	    	        var data = {ids:ids.join(",")};
	    	        $.ajax({
	    	            cache: false,
	    	            type: "POST",
	    	            url: "${ctx}/md/customer/price/approve",
	    	            data: data,
	    	            success: function (data) {
                            if(ajaxLogout(data)){
                                return false;
                            }
	    	            	if (data.success){
	    	            		if(data.message){
	    	            			top.$.jBox.tip(data.message, '信息提示');
	    	            		}
	    	            		else{
	    	            			top.$.jBox.tip('服务价格审核通过成功', '信息提示');
                                    search();
	    	            		}
	    	            	}
	    	            	else{
		    	            	top.$.jBox.closeTip();
	    	            		top.$.jBox.error(data.message);
	    	            	}
	    		            $('#btnApprove').removeAttr('disabled');
	    	            },
	    	            error: function (e) {
	    	            	top.$.jBox.closeTip();
	    	                //top.$.jBox.error(thrownError.toString());
	    	                $('#btnApprove').removeAttr('disabled');
                            ajaxLogout(e.responseText,null,"服务价格审核错误，请重试!");
	    	            }
	    	        });
	            }
	            else if (v == 'cancel') {
	                // 取消
	            }

	            return true; //close
	        };

	        top.$.jBox.confirm('确定要审核通过吗？', '确认', submit);
	    });

		$(document).on("click", "#btnCancel", function () {
	        if (ids.length == 0) {
	            top.$.jBox.error('请选择要停用的记录', '审核服务停用');
	            return;
	        }

	        var submit = function (v, h, f) {
	            if (v == 'ok') {
	                top.$.jBox.tip('正在停用服务价格...', 'loading');

	    	 		$(this).prop("disabled", true);

	    	        var data = {ids:ids.join(",")};
	    	        $.ajax({
	    	            cache: false,
	    	            type: "POST",
	    	            url: "${ctx}/md/customer/price/stop",
	    	            data: data,
	    	            success: function (data) {
                            if(ajaxLogout(data)){
                                return false;
                            }
	    	            	if (data.success){
	    	            		if(data.message){
	    	            			top.$.jBox.tip(data.message, '信息提示');
	    	            		}
	    	            		else{
	    	            			top.$.jBox.tip('服务价格停用成功', '信息提示');
	    	            		}
	    	            		location.href="${ctx}/md/customer/price/approvelist";
	    	            	}
	    	            	else{
		    	            	top.$.jBox.closeTip();
	    	            		top.$.jBox.error(data.message);
	    	            	}
	    		            $('#btnCancel').removeAttr('disabled');
	    	            },
	    	            error: function (e) {
	    	            	top.$.jBox.closeTip();
	    	                //top.$.jBox.error(thrownError.toString());
	    	                $('#btnCancel').removeAttr('disabled');
                            ajaxLogout(e.responseText,null,"服务价格停用错误，请重试!");
	    	            }
	    	        });
	            }
	            else if (v == 'cancel') {
	                // 取消
	            }

	            return true; //close
	        };
	        top.$.jBox.confirm('确定要停用该价格吗？', '确认', submit);
	    });

	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:;">待审核列表</a></li>
	</ul>
	<form:form id="searchForm" modelAttribute="customerPrice" action="${ctx}/md/customer/price/approvelist" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<c:set var="currentuser" value="${fns:getUser() }" />
		<c:if test="${!currentuser.isCustomer() }">
		<label>客户：</label>
		<select id="customerId" name="customer.id" class="input-small" style="width:250px;">
				<option value=""
					<c:out value="${(empty customerId)?'selected=selected':''}" />>所有</option>
				<c:forEach items="${fns:getMyCustomerListFromMS()}" var="dict">
					<option value="${dict.id}"
						<c:out value="${(customerPrice.customer.id eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
				</c:forEach>
		</select>
		&nbsp;
		</c:if>
		<label>产品：</label>
		<%--<sys:treeselect id="product" name="product.id" value="${customerPrice.product.id}" labelName="product.name" labelValue="${customerPrice.product.name}"
				title="产品" url="/md/product/treeData" cssClass="input-small" allowClear="true" cssStyle="width:250px;"/>--%>
		<form:select path="product.id" cssClass="input-small" cssStyle="width:250px;">
			<form:option value="" label="所有"/>
			<form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
		</form:select>
		&nbsp;
		<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>
		<shiro:hasPermission name="md:customerprice:approve">
			<a class="btn btn btn-success" id="btnApprove" style="margin-left: 20px;" href="javascript:;" title="通过审核"><i class="icon-ok"></i>通过</a>
			<%--<a class="btn btn-danger" id="btnCancel" style="margin-left: 20px;" href="javascript:;" title="停用价格"><i class="icon-ban-circle"></i>停用</a>--%>
		</shiro:hasPermission>
	</form:form>
	<sys:message content="${message}"/>

	<c:set value="" var="customerId"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
		<tr>
			<th width="30px"><input type="checkbox" id="selectAll" name="selectAll"/></th>
			<th  width="30px">序号</th>
			<th>客户</th><th>产品名称</th><th>服务类型</th><th>默认价格(元)</th><th>默认折扣价(元)</th><th>冻结价(元)</th><th>操作</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach items="${page.list}" var="customerprice" varStatus="i" begin="0">
			<tr>
			<td><input type="checkbox" id="cbox${i.index}" value="${customerprice.id}" name="checkedRecords"/></td>
			<td>${i.index+1}</td>
			<td>${customerprice.customer.name}</td>
			<td>${customerprice.product.name}</td>
			<td>${customerprice.serviceType.name}</td>
			<td>${customerprice.price}</td>
			<td>${customerprice.discountPrice}</td>
			<td>${customerprice.blockedPrice}</td>
			<shiro:hasPermission name="md:customerprice:edit">
			<td><a href="javascript:void(0);" onclick="go2Edit('${customerprice.id}','${customerprice.customer.id}');">修改</a></td>
			</shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>
