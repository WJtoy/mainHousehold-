<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>B2B对接系统客户与产品</title>
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
      </script>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">列表</a></li>
		<shiro:hasPermission name="md:b2bproduct:edit"><li><a href="${ctx}/b2bcenter/md/product/form">添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="b2BProductMapping" action="${ctx}/b2bcenter/md/product/getList" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>数据源:</label>
		<form:select path="dataSource" cssStyle="width: 220px;">
		<form:option value="" label="请选择"></form:option>
		<form:options items="${fns:getDictExceptListFromMS('order_data_source',1)}"
					  itemLabel="label" itemValue="value" />
	    </form:select>
		&nbsp;
		<label>客户：</label>
		<form:select path="customerId" cssStyle="width: 200px;">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id"></form:options>
		</form:select>
		&nbsp;
		<label>客户料号：</label>
		<form:input path="customerCategoryId" class="input-small" maxlength="100"></form:input>
		&nbsp;
		<label>产品名称：</label>
		<form:select path="productId" cssStyle="width: 200px;">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id"></form:options>
		</form:select>
		<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		<input class="btn btn-primary" type="button" onclick="syncRoutingConfig()" value="一键同步" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="100">数据源</th>
				<th width="100">数据源料号</th>
				<th width="150">数据源产品型号</th>
				<th width="200">工单产品名称</th>
				<th width="300">客户名称</th>
				<th width="200">店铺名称</th>
				<th width="200">客户料号</th>
				<th width="200">客户产品型号</th>
				<th width="200">备注</th>
				<shiro:hasPermission name="md:b2bproduct:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${fns:getDictLabelFromMS(entity.dataSource,'order_data_source','Unknow')}</td>
				<td>
					<a href="${ctx}/b2bcenter/md/product/form?dataSource=${entity.dataSource}&id=${entity.id}&productId=${entity.productId}&remarks=${entity.remarks}&customerCategoryId=${entity.customerCategoryId}&customerId=${entity.customerId}&shopId=${entity.shopId}&shopName=${fns:urlEncode(entity.shopName)}&productCode=${entity.productCode}">${entity.customerCategoryId}</a>
				</td>
				<td>${entity.productCode}</td>
				<td>${entity.productName}</td>
				<td>${entity.customerName}</td>
				<td>${entity.shopName}</td>
				<td>${entity.customerCode}</td>
				<td>${entity.customerProductModel}</td>
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
				<shiro:hasPermission name="md:b2bproduct:edit"><td>
    				<a href="${ctx}/b2bcenter/md/product/form?dataSource=${entity.dataSource}&id=${entity.id}&productId=${entity.productId}&remarks=${entity.remarks}&customerCategoryId=${entity.customerCategoryId}&customerId=${entity.customerId}&shopId=${entity.shopId}&shopName=${fns:urlEncode(entity.shopName)}&productCode=${entity.productCode}">修改</a>
					<a href="${ctx}/b2bcenter/md/product/delete?id=${entity.id}&dataSource=${entity.dataSource}&customerCategoryId=${entity.customerCategoryId}&shopId=${entity.shopId}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
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
        //一键同步
        function sync() {
            top.$.jBox.tip("正在同步...", "loading");
            $.ajax({
                type: "post",
                url: "${ctx}/b2bcenter/md/product/ajax/syncByDataSource?dataSource=2",
                dataType: "json",
                success: function(data){
                    top.$.jBox.closeTip();
                    layerAlert(data.message,"提示");
                },
                error:function (data) {
                    top.$.jBox.closeTip();
                    //layerError("一键同步失败","错误提示");
                    ajaxLogout(data.responseText,null,"一键同步失败","错误提示！");
                }
            });
        }


        function syncRoutingConfig(){
            var self = this;
            top.layer.open({
                type: 2,
                id:'layer_sync_routing_config',
                zIndex:19891015,
                title:'同步',
                content: "${ctx}/b2bcenter/md/product/syncRoutingConfig",
                area: ['550px', '400px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }
	</script>
  </body>
</html>
