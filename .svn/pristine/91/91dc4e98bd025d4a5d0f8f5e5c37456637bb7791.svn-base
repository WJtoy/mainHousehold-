<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>B2B质保类型关联关系</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
		<script type="text/javascript">
            //覆盖分页前方法
            function beforePage() {
                var $btnSubmit = $("#btnSubmit");
                $btnSubmit.attr('disabled', 'disabled');
                $("#btnClearSearch").attr('disabled', 'disabled');
                layerLoading("查询中...", true);
            }

            var clicktag = 0;
            $(document).on("click", "#btnSubmit", function () {
                if (clicktag == 0) {
                    clicktag = 1;
                    beforePage();
                    setPage();
                    this.form.submit();
                }
            });

            $(document).ready(function() {
                changeDataSource();
                changeCustomer();
            });
            
            function changeCustomer() {
                var customerId = $("#customerId").val();
                var dataSource = $("#dataSource").val();
                if(dataSource ==null || dataSource ==''){
                    return false;
                }
                if (customerId == "")
                {
                    $("#shopId").empty();
                    $("#shopId").html('<option value="" selected>请选择</option>');
                    $("#shopId").val("");
                    $("#shopId").change();
                    return false;
                }
                var selectShopId = $("#selectShopId").val();
                $.ajax({
                        url:"${ctx}/b2bcenter/md/customer/getShopList?customerId="+customerId +"&dataSource=" + dataSource,
                        success:function (e) {
                            if(e.success){
                                $("#shopId").empty();
                                var shopId_sel=[];
                                shopId_sel.push('<option value="" selected="selected">请选择</option>')
                                for(var i=0,len=e.data.length;i<len;i++){
                                    var customerMapping = e.data[i];
                                    if(customerMapping.shopId==selectShopId){
                                        shopId_sel.push('<option value="'+customerMapping.shopId+'" selected>'+customerMapping.shopName+'</option>')
									}else{
                                        shopId_sel.push('<option value="'+customerMapping.shopId+'">'+customerMapping.shopName+'</option>')
									}
                                }
                                $("#shopId").append(shopId_sel.join(' '));
                                $("#shopId").change();
                            }else {
                                $("#shopId").html('<option value="" selected>请选择</option>');
                                $("#shopId").change();
                            }
                        },
                        error:function (e) {
                            layerError("请求客户产品失败","错误提示");
                        }
                    }
                );
            }

            function changeDataSource() {
                var selectCustomerId = $("#selectCustomerId").val();
                var dataSource =$("#dataSource").val();
                if(dataSource==null || dataSource==''){
                    $("#shopId").empty();
                    $("#shopId").html('<option value="" selected>请选择</option>');
                    $("#shopId").val("");
                    $("#shopId").change();
                    $("#customerId").empty();
                    $("#customerId").html('<option value="" selected>请选择</option>');
                    $("#customerId").val("");
                    $("#customerId").change();
                    return false;
                }
                $.ajax({
                    url:"${ctx}/b2bcenter/md/customer/ajax/getCustomerListByDataSource?dataSource="+dataSource,
                    success:function (e){
                        if(e.success){
                            $("#customerId").empty();
                            var customer_sel=[];
                            customer_sel.push('<option value="" selected="selected">请选择</option>')
                            for(var i=0,len=e.data.length;i<len;i++){
                                var programme = e.data[i];
                                if(programme.id==selectCustomerId){
                                    customer_sel.push('<option value="'+programme.id+'" selected>'+programme.name+'</option>')
								}else {
                                    customer_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
								}
                            }
                            $("#customerId").append(customer_sel.join(' '));
                            $("#customerId").change();
                        }else{
                            layerError("获取客户错误:" + e.message, "错误提示");
                            $("#customerId").html('<option value="" selected>请选择</option>');
                            $("#customerId").change();
                        }
                    },
                    error:function (e) {
                        layerError("获取客户信息错误","错误提示");
                        $("#customerId").html('<option value="" selected>请选择</option>');
                        $("#customerId").change();
                    }
                });
            }

			var ajaxSuccess = 0;
            var loadingIndex;
			function deleteWarranty(id,dataSource){
				top.layer.confirm('确认要删除吗？', {icon: 3, title:'系统确认'}, function(index){
					top.layer.close(index);//关闭本身
					if(clicktag === 1){
						return false;
					}
					clicktag = 1;
					$.ajax({
						async: false,
						cache: false,
						type: "POST",
						url: "${ctx}/b2bcenter/md/warranty/ajaxDelete?id=" + (id || '0') + "&dataSource=" + (dataSource || ''),
						data: null,
						beforeSend: function () {
							loadingIndex = top.layer.msg('正在提交，请稍等...', {
								icon: 16,
								time: 0,
								shade: 0.3
							});
						},
						complete: function () {
							if(loadingIndex) {
								setTimeout(function () {
									top.layer.close(loadingIndex);
								}, 1000);
							}
							if(ajaxSuccess == 0) {
								setTimeout(function () {
									clickTag = 0;
								}, 2000);
							}
						},
						success: function (data) {
							if(ajaxLogout(data)){
								return false;
							}
							if(data && data.success == true){
								top.layer.close(loadingIndex);
								layerMsg("删除成功!", true);
								$("#searchForm").submit();
							}
							else if( data && data.message){
								layerError(data.message,"错误提示");
							}
							else{
								layerError("删除失败，请重试或刷新页面!","错误提示");
							}
							return false;
						},
						error: function (e) {
							ajaxLogout(e.responseText,null,"删除失败，请重试或刷新页面!");
						}
					});
					return false;
				},function(index){//cancel

				});
			}
	    </script>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">列表</a></li>
		<shiro:hasPermission name="md:b2bwarranty:edit"><li><a href="${ctx}/b2bcenter/md/warranty/form">添加</a></li></shiro:hasPermission>
	</ul>
	<input id="selectCustomerId" type="hidden" value="${b2bWarrantyMapping.customerId}">
	<input id="selectShopId" type="hidden" value="${b2bWarrantyMapping.shopId}">
	<form:form id="searchForm" modelAttribute="b2bWarrantyMapping" action="${ctx}/b2bcenter/md/warranty/getList" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>数据源:</label>
		<form:select path="dataSource" cssStyle="width: 120px;" onchange="changeDataSource()">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${fns:getDictExceptListFromMS('order_data_source',1)}"
						  itemLabel="label" itemValue="value" />
		</form:select>
		<label>客户：</label>
		<%--<form:select path="customerId" cssStyle="width: 200px;">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id"></form:options>
		</form:select>--%>
		<select id="customerId" name="customerId" style="width:200px;" onchange="changeCustomer()">
			<option value="">请选择</option>
		</select><c:if test="${empty b2bWarrantyMapping.dataSource}"><span style="color: red">(请选选择数据源)</span></c:if>
		&nbsp;
		<%--<label>店铺ID：</label><form:input path="shopId" htmlEscape="false" maxlength="30" class="input-small"/>--%>
		<label>店铺名称：</label>
		<select id="shopId" name="shopId" style="width:200px;">
			<option value="" selected>请选择</option>
		</select><c:if test="${empty b2bWarrantyMapping.customerId}"><span style="color: red">(请先选择客户)</span></c:if>
		&nbsp;
		<label>outerIdSKU：</label><form:input path="b2bWarrantyCode" htmlEscape="false" maxlength="30" class="input-small"/>
        &nbsp;
        <label>数据源料号：</label><form:input path="customerCategoryId" htmlEscape="false" maxlength="30" class="input-small"/>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="100">数据源</th>
				<th width="100">店铺名称</th>
				<th width="200">outerIdSKU</th>
				<th width="100">数据源料号</th>
				<th width="50">质保类型</th>
				<th width="300">客户名称</th>
				<th width="100">是否自动取消</th>
				<th width="100">是否自动转单</th>
				<th>备注</th>
				<shiro:hasPermission name="md:b2bwarranty:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${fns:getDictLabelFromMS(entity.dataSource,'order_data_source','Unknow')}</td>
				<td>${entity.shopName}</td>
				<td>${entity.b2bWarrantyCode}</td>
				<td>${entity.customerCategoryId}</td>
				<td>${entity.warrantyType}</td>
				<td>${entity.customerName}</td>
				<c:choose>
					<c:when test="${entity.autoCancelFlag==0}">
                        <td><span class="label status_100">否</span></td>
					</c:when>
					<c:otherwise>
                        <td><span class="label status_10">是</span></td>
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="${entity.autoTransferFlag==0}">
                        <td><span class="label status_100">否</span></td>
					</c:when>
					<c:otherwise>
                        <td><span class="label status_10">是</span></td>
					</c:otherwise>
				</c:choose>
				<td>
					<c:choose>
						<c:when test="${fn:length(entity.remarks)>40}">
							<a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.remarks}">${fns:abbr(entity.remarks,80)}</a>
						</c:when>
						<c:otherwise>
							${entity.remarks}
						</c:otherwise>
					</c:choose>
				</td>
				<shiro:hasPermission name="md:b2bwarranty:edit"><td>
    				<a href="${ctx}/b2bcenter/md/warranty/form?id=${entity.id}&shopName=${fns:urlEncode(entity.shopName)}">修改</a>
					<a href="javascript:void(0);" onclick="deleteWarranty('${entity.id}','${entity.dataSource}');">删除</a>
<%--					<a href="${ctx}/b2bcenter/md/warranty/delete?id=${entity.id}&dataSource=${entity.dataSource}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>--%>
				</td></shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
	<script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
        });
	</script>
  </body>
</html>
