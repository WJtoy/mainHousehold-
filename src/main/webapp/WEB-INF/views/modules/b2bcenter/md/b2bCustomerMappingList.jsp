<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>B2B对接系统客户与客户</title>
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

          function editCustomerShop(type,id) {
              var text = "添加店铺";
              var url = "${ctx}/b2bcenter/md/customer/form";
              if(type == 2){
                  text = "修改店铺"
                  url = "${ctx}/b2bcenter/md/customer/form?id=" + id;
              }
              top.layer.open({
                  type: 2,
                  id:"customerShop",
                  zIndex:19891015,
                  title:text,
                  content: url,
                  area: ['600px', '580px'],
                  shade: 0.3,
                  maxmin: false,
                  success: function(layero,index){
                  },
                  end:function(){
                  }
              });
          }

          var this_index = top.layer.index;
          //删除
          function del(id, dataSource) {
              layer.confirm('确定要删除吗？', {
                  btn: ['确定','取消'] //按钮
              }, function(){
                  var loadingIndex = layerLoading('正在提交，请稍候...');
                  var $btnDelete = $("#btnDelete");
                  if ($btnDelete.prop("disabled") == true) {
                      event.preventDefault();
                      return false;
                  }
                  $btnDelete.prop("disabled", true);
                  $.ajax({
                      url:"${ctx}/b2bcenter/md/customer/delete?id="+ id +"&dataSource=" + dataSource,
					  type:"POST",
                      dataType:"json",
                      success: function(data){
                          //提交后的回调函数
                          if(loadingIndex) {
                              top.layer.close(loadingIndex);
                          }
                          if(ajaxLogout(data)){
                              setTimeout(function () {
                                  $btnDelete.removeAttr('disabled');
                              }, 2000);
                              return false;
                          }
                          if (data.success) {
                              top.layer.close(this_index);//关闭本身
							  layerMsg("删除成功");
                              var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                              if(pframe){
                                  pframe.repage();
                              }
                          }else{
                              setTimeout(function () {
                                  $btnDelete.removeAttr('disabled');
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
                              $btnDelete.removeAttr('disabled');
                          }, 2000);
                          ajaxLogout(data,null,"数据删除错误，请重试!");
                          //var msg = eval(data);
                      },
                      timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                  });
              }, function(){
              });
          }
	  </script>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">列表</a></li>
		<%--<shiro:hasPermission name="md:b2bcustomer:edit"><li><a href="${ctx}/b2bcenter/md/customer/form">添加</a></li></shiro:hasPermission>--%>
	</ul>
	<form:form id="searchForm" modelAttribute="b2BCustomerMapping" action="${ctx}/b2bcenter/md/customer/getList" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>数据源:</label>
		<form:select path="dataSource" cssStyle="width: 150px;">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${fns:getDictExceptListFromMS('order_data_source',1)}"
						  itemLabel="label" itemValue="value" />
		</form:select>
		<label>客户：</label>
		<form:select path="customerId" cssStyle="width: 200px;">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id"></form:options>
		</form:select>
		&nbsp;
		<label>店铺ID：</label><form:input path="shopId" htmlEscape="false" maxlength="50" class="input-small"/>
		&nbsp;
		<label>店铺名称：</label><form:input path="shopName" htmlEscape="false" maxlength="50" class="input-small"/>
		&nbsp;
		<label>客户编码：</label><form:input path="b2bCustomerCode" htmlEscape="false" maxlength="30" class="input-small"/>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>

	<shiro:hasPermission name="md:b2bcustomer:edit">
		<button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 120px;height: 30px" onclick="editCustomerShop(1,null)">
			<i class="icon-plus-sign"></i>&nbsp;添加店铺
		</button>
	</shiro:hasPermission>

	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="100">数据源</th>
				<th width="100">店铺ID</th>
				<th width="200">店铺名称</th>
				<th width="300">客户名称</th>
				<th width="200">b2b客户编码</th>
				<th width="100">销售渠道</th>
				<th width="100">是否默认店铺</th>
				<th width="150">客户账号</th>
				<th>备注</th>
				<shiro:hasPermission name="md:b2bcustomer:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${fns:getDictLabelFromMS(entity.dataSource,'order_data_source','Unknow')}</td>
				<td>${entity.shopId}</td>
				<td>${entity.shopName}</td>
				<td>${entity.customerName}</td>
				<td>${entity.b2bCustomerCode}</td>
				<td>${fns:getDictLabelFromMS(entity.saleChannel,'sale_channel','')}</td>
				<c:choose>
					<c:when test="${entity.defaultShopFlag==1}">
						<td><span class="label status_10">是</span></td>
					</c:when>
					<c:otherwise>
						<td><span class="label status_100">否</span></td>
					</c:otherwise>
				</c:choose>
				<td>${entity.customerAccountName}</td>
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
				<shiro:hasPermission name="md:b2bcustomer:edit"><td>
    				<%--<a href="${ctx}/b2bcenter/md/customer/form?id=${entity.id}">修改</a>--%>
					<a href="javascript:editCustomerShop(2,'${entity.id}')">修改</a>
					<%--<a href="${ctx}/b2bcenter/md/customer/delete?id=${entity.id}&dataSource=${entity.dataSource}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>--%>
						<a href="javascript:del('${entity.id}','${entity.dataSource}','${entity.customerId}')">删除</a>
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
