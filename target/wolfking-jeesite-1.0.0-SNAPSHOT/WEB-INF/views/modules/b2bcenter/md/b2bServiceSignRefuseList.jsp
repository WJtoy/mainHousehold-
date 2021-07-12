<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>已同意列表</title>
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
		<li>
			<a href="${ctx}/b2bcenter/md/serviceSign/getList">待处理</a>
		</li>
		<li>
			<a href="${ctx}/b2bcenter/md/serviceSign/getAgreeList">已同意</a>
		</li>
		<li class="active"><a href="javascript:void(0);">已拒绝</a></li>
	</ul>
	<form:form id="searchForm" modelAttribute="b2BSign" action="${ctx}/b2bcenter/md/serviceSign/getRefuseList" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
	</form:form>

	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="100">数据源</th>
				<th width="100">签约单号</th>
				<th width="100">店铺ID</th>
				<th width="200">店铺名称</th>
				<th width="100">服务类型</th>
				<th width="200">服务名称</th>
				<th width="150">申请时间</th>
				<th width="100">联系人</th>
				<th>备注</th>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${fns:getDictLabelFromMS(entity.dataSource,'order_data_source','Unknow')}</td>
				<td>${entity.signOrderSn}</td>

				<td>${entity.mallId}</td>
				<td>${entity.mallName}</td>

                <td>${entity.servType}</td>
				<td>${entity.servName}</td>
				<td><fmt:formatDate value="${entity.applyDate}" pattern="yyyy-MM-dd HH:mm:ss "/></td>
				<td>${entity.contactName}</td>
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
