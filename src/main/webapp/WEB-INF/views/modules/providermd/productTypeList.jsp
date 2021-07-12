<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品分类</title>
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

          function editProductType(type,id) {
              var text = "产品分类-添加分类";
              var url = "${ctx}/provider/md/productType/form";
              if(type == 2){
                  text = "产品分类-修改"
                  url = "${ctx}/provider/md/productType/form?id=" + id ;
			  }
              top.layer.open({
                  type: 2,
                  id:"productType",
                  zIndex:19891015,
                  title:text,
                  content: url,
                  area: ['650px', '330px'],
                  shade: 0.3,
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
		<li class="active"><a href="javascript:void(0);">列表</a></li>
		<%--<shiro:hasPermission name="md:auxiliarymaterialcategory:edit"><li><a href="${ctx}/provider/md/auxiliaryMaterialCategory/form">添加</a></li></shiro:hasPermission>--%>
	</ul>
	<form:form id="searchForm" modelAttribute="mdProductTypeDto" action="${ctx}/provider/md/productType/findList" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>分类名称:</label><form:input path="name" htmlEscape="false" maxlength="30" class="input-small"/>&nbsp;
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
    <shiro:hasPermission name="md:producttype:edit">
	   <%--<input id="addProductType" class="btn btn-primary" style="margin-top: 10px;margin-bottom: 20px;width: 60px" value="添加分类" onclick="editProductType(1,null)">--%>
		<button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 100px;height: 30px" onclick="editProductType(1,null)">
			<i class="icon-plus-sign"></i>&nbsp;添加分类
		</button>
	</shiro:hasPermission>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="250">品类</th>
				<th width="200">一级分类</th>
				<th>二级分类</th>
				<shiro:hasPermission name="md:producttype:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${entity.productCategoryName}</td>
				<td>${entity.name}</td>
				<td><a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.itemNames}">${fns:abbr(entity.itemNames,250)}</a></td>
				<shiro:hasPermission name="md:producttype:edit"><td>
    				<a href="javascript:editProductType(2,'${entity.id}')">修改</a>
					<a href="${ctx}/provider/md/productType/delete?id=${entity.id}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
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
