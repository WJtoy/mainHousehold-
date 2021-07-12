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

          function addWhitelist() {
              top.layer.open({
                  type: 2,
                  id:"layer_whitelist",
                  zIndex:19891015,
                  title:'添加白名单',
                  content: "${ctx}/provider/sys/userWhitelist/addForm",
                  area: ['600px', '700px'],
                  shade: 0.3,
                  maxmin: false,
                  success: function(layero,index){
                  },
                  end:function(){
                  }
              });
          }

          function editWhitelist(id) {
              top.layer.open({
                  type: 2,
                  id:"layer_editWhitelist",
                  zIndex:19891015,
                  title:'修改白名单',
                  content: "${ctx}/provider/sys/userWhitelist/editForm?id=" + id,
                  area: ['500px', '300px'],
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
	</ul>
	<form:form id="searchForm" modelAttribute="sysUserWhiteList" action="${ctx}/provider/sys/userWhitelist/findList" method="post" class="">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<%--<label>分类名称:</label><form:input path="name" htmlEscape="false" maxlength="30" class="input-small"/>&ndash;%&gt;&nbsp;
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />--%>
	</form:form>
    <shiro:hasPermission name="sys:whitelist:edit">
	   <%--<input id="addProductType" class="btn btn-primary" style="margin-top: 10px;margin-bottom: 20px;width: 60px" value="添加分类" onclick="editProductType(1,null)">--%>
		<button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 120px;height: 30px" onclick="addWhitelist()">
			<i class="icon-plus-sign"></i>&nbsp;添加白名单
		</button>
	</shiro:hasPermission>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="250">用户姓名</th>
				<th width="250">登录名</th>
				<th width="250">用户类型</th>
				<th width="200">到期日期</th>
				<th>最后登录IP</th>
				<th>最后登录时间</th>
				<th>备注</th>
				<shiro:hasPermission name="sys:whitelist:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${entity.userName}</td>
				<td>${entity.loginName}</td>
				<td>${entity.userType}</td>
				<td><fmt:formatDate value="${entity.endDate}" pattern="yyyy-MM-dd HH:mm"/></td>
				<td>${entity.loginIp}</td>
				<td><fmt:formatDate value="${entity.loginDate}" pattern="yyyy-MM-dd HH:mm"/></td>
				<td><a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.remarks}">${entity.remarks}</a></td>
				<shiro:hasPermission name="sys:whitelist:edit"><td>
    				<a href="javascript:editWhitelist('${entity.id}')">修改</a>
					<a href="${ctx}/provider/sys/userWhitelist/delete?id=${entity.id}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
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
