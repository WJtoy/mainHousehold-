<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>互助基金列表</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	  <style type="text/css">
		  .table thead th,.table tbody td {
			  text-align: center;
			  vertical-align: middle;
			  BackColor: Transparent;
		  }
	  </style>

	  <script type="text/javascript">
		  $(document).ready(function () {
			  var w = $(window).width();

			  var pagestyle = function() {
				  var width = $(window).width() -0;
				  $("#treeTable_tableLayout").css("width",width);
			  }
			  //注册窗体改变大小事件
			  $(window).resize(pagestyle);
			  $("th").css({"text-align":"center","vertical-align":"middle"});
			  $("td").css({"vertical-align":"middle"});
		  });
		  function editInsurancePrice(type,id) {
			  var text = "添加互助基金";
			  var url = "${ctx}/md/insurancePrice/form";
			  var area = ['727px', '409px'];
			  if(type == 20){
				  text = "修改互助基金";
				  url = "${ctx}/md/insurancePrice/form?id=" + id;
			  }
			  top.layer.open({
				  type: 2,
				  id:"insurancePrice",
				  zIndex:19,
				  title:text,
				  content: url,
				  area: area,
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
	  <li class="active"><a href="javascript:void(0);">互助基金</a></li>
  </ul>
	<form:form id="searchForm" modelAttribute="insurancePrice" action="${ctx}/md/insurancePrice" method="post" cssStyle="margin: 0px 0 0px;">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<shiro:hasPermission name="md:insuranceprice:edit">
		<button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 128px;height: 30px" onclick="editInsurancePrice()">
			<i class="icon-plus-sign"></i>&nbsp;添加互助基金
		</button></shiro:hasPermission>
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="280">产品品类</th>
				<th width="280">基金金额(元/每单)</th>
				<th width="320">创建时间</th>
				<th>描述</th>
				<shiro:hasPermission name="md:insuranceprice:edit"><th width="200">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${entity.category.name}</td>
				<td>
					<fmt:formatNumber value="${entity.insurance}" pattern="0.00"></fmt:formatNumber>
				</td>
				<td><fmt:formatDate value="${entity.createDate}" pattern="yyyy-MM-dd HH:mm:ss"></fmt:formatDate></td>
				<td>${entity.remarks}</td>
				<shiro:hasPermission name="md:insuranceprice:edit"><td>
    				<a href="javascript:editInsurancePrice(20,'${entity.id}')">修改</a>
					<a href="${ctx}/md/insurancePrice/delete?id=${entity.id}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
				</td></shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>
