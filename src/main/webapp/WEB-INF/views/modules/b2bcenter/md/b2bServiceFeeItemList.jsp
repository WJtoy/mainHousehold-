<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>B2B客户料号</title>
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
              getServiceFeeCategory();
          });
          //根据数据源获取服务费项目分类
          function getServiceFeeCategory() {
              var dataSource = $("#dataSource").val();
              if(dataSource ==null || dataSource ==''){
                  $("#categoryId").empty();
                  $("#categoryId").html('<option value="" selected>请选择</option>');
                  $("#categoryId").change();
                  return false;
              }
              $.ajax({
                      url:"${ctx}/b2bcenter/md/serviceFeeCategory/ajax/getServiceFeeCategory?dataSource=" + dataSource,
                      success:function (e) {
                          if(e && e.success == true){
                              $("#categoryId").empty();
                              var programme_sel=[];
                              programme_sel.push('<option value="" selected="selected">请选择</option>')
                              for(var i=0,len=e.data.length;i<len;i++){
                                  var programme = e.data[i];
                                  if(programme.id == ${not empty serviceFeeItem.category.id?serviceFeeItem.category.id:0}){
                                      programme_sel.push('<option value="'+programme.id+'" selected="selected" >'+programme.categoryName+'</option>')
                                  }else{
                                      programme_sel.push('<option value="'+programme.id+'">'+programme.categoryName+'</option>')
                                  }
                              }
                              $("#categoryId").append(programme_sel.join(' '));
                              $("#categoryId").change();
                          }else if(e.success==false){
                              $("#categoryId").html('<option value="" selected>请选择</option>');
                              $("#categoryId").change();
                              layerError(e.message,"错误提示");
                          }
                      },
                      error:function (e) {
                          ajaxLogout(e.responseText,null,"请求服务费项目分类失败","错误提示！");
                      }
                  }
              );
          }

	  </script>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">列表</a></li>
		<shiro:hasPermission name="md:b2bservicefeeitem:edit"><li><a href="${ctx}/b2bcenter/md/serviceFeeItem/form">添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="serviceFeeItem" action="${ctx}/b2bcenter/md/serviceFeeItem/getList" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
        <label>数据源:</label>
        <form:select path="dataSource" cssStyle="width: 150px;" onclick="getServiceFeeCategory()">
            <form:option value="" label="请选择"></form:option>
            <form:options items="${fns:getDictListFromMS('order_data_source')}"
                          itemLabel="label" itemValue="value" />
        </form:select> &nbsp;
        <label>分类:</label>
        <select id="categoryId" name="category.id" style="width:150px;" class="required">
            <option value="">请选择</option>
        </select>
        &nbsp;
        <label>产品名称：</label>
        <form:select path="productId" cssStyle="width: 150px;">
            <form:option value="" label="请选择"></form:option>
            <form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id"></form:options>
        </form:select>
		&nbsp;
		<label>项目名称:</label><form:input path="itemName" htmlEscape="false" maxlength="30" class="input-small"/>&nbsp;
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
                <th width="100">数据源</th>
				<th width="250">分类名称</th>
				<th width="200">项目名称</th>
				<th width="200">产品</th>
				<th width="100">金额</th>
				<th width="100">价格类别</th>
				<th width="100">单位</th>
				<th>备注</th>
				<shiro:hasPermission name="md:b2bservicefeeitem:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
                <td>${fns:getDictLabelFromMS(entity.dataSource,'order_data_source','Unknow')}</td>
				<td>${entity.category.categoryName}</td>
				<td>
                    <a href="${ctx}/b2bcenter/md/serviceFeeItem/form?id=${entity.id}">${entity.itemName}</a>
                </td>
				<td>${entity.productName}</td>
				<td>${entity.charge}</td>
				<c:choose>
					<c:when test="${entity.customPriceFlag==0}">
						<td>固定价格</td>
					</c:when>
					<c:when test="${entity.customPriceFlag==1}">
						<td>自定义价格</td>
					</c:when>
					<c:otherwise>
						<td>未知</td>
					</c:otherwise>
				</c:choose>
				<td>${entity.unit}</td>
				<td>
					<c:choose>
						<c:when test="${fn:length(entity.remarks)>20}">
							<a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.remarks}">${fns:abbr(entity.remarks,28)}</a>
						</c:when>
						<c:otherwise>
							${entity.remarks}
						</c:otherwise>
					</c:choose>
				</td>
				<shiro:hasPermission name="md:b2bservicefeeitem:edit"><td>
    				<a href="${ctx}/b2bcenter/md/serviceFeeItem/form?id=${entity.id}">修改</a>
					<a href="${ctx}/b2bcenter/md/serviceFeeItem/delete?id=${entity.id}&dataSource=${entity.dataSource}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
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
