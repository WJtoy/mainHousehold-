<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
	<script src="${ctxStatic}/area/Area-1.2.js" type="text/javascript"></script>
    <title>网点编号</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	  <style type="text/css">
		  .table thead th,.table tbody td {
			  text-align: center;
			  vertical-align: middle;
		  }
	  </style>
	  <script type="text/javascript" language="javascript">

          var this_index = top.layer.index;

          //覆盖分页前方法
          function beforePage() {
              var $btnSubmit = $("#btnSubmit");
              $btnSubmit.attr('disabled', 'disabled');
              layerLoading("查询中...", true);
          }

          var clicktag = 0;
          $(document).on("click", "#btnSubmit", function () {
              var areaId = $("#areaId").val();
              if(areaId==null || areaId<=0){
                  layerError("请选择城市","错误提示");
                  return false;
              }
              if (clicktag == 0) {
                  clicktag = 1;
                  beforePage();
                  this.form.submit();
              }
          });

          //取消
          function cancel() {
              top.layer.close(this_index);//关闭本身
          }

	  </script>
  </head>
  <body>
	<form:form id="searchForm" modelAttribute="servicePointDto" action="${ctx}/fi/md/servicepoint/findListByAreaIds" method="post" class="breadcrumb form-search" cssStyle="margin-top: 24px">
		<label>区&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;域:</label> <md:arearemotselect name="areaId" id="area" value="${servicePointDto.areaId}"
											   labelValue="${servicePointDto.areaName}" labelName="areaName" title=""
											   mustSelectCounty="true" cssClass="required"></md:arearemotselect>&nbsp;
		<input id="btnSubmit" class="btn btn-primary" type="button" style="width: 70px" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover" style="width: 98%;margin-top: 24px" align="center">
		<thead>
		<tr>
			<th width="50">序号</th>
			<th width="150">网点编号</th>
			<th width="300">网点名称</th>
		</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index}</td>
				<td>${entity.servicePointNo}</td>
				<td>${entity.name}</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div style="height: 60px;width: 100%"></div>
	<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
		<hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
		<div style="float: right;margin-top: 10px;margin-right: 20px">
			<input id="btnCancel" class="btn" type="button" value="取 消" style="width: 96px;height: 40px" onclick="cancel()"/>
		</div>
	</div>
	<script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
        });
	</script>
  </body>
</html>
