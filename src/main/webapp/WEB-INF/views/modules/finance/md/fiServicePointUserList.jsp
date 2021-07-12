<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
	<script src="${ctxStatic}/area/Area-1.2.js" type="text/javascript"></script>
    <title>网点用户</title>
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
              var mobile = $("#mobile").val();
              if(mobile == null || mobile == ''){
                  layerError("请输入您要查询的手机号","错误提示");
                  return false;
              }
              if(!(/^1[3456789]\d{9}$/.test(mobile))){
                  layerError("手机号码有误，请重新输入","错误提示");
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
  <body style="padding: 12px;">
	<form:form id="searchForm" modelAttribute="user" action="${ctx}/fi/md/servicePoint/findUserListByContactInfo" method="post" class="form-search" cssStyle="margin-top: 24px">
		<label>联系电话：</label>
		<form:input path="mobile" htmlEscape="false" maxlength="11" class=""/>
		<input id="btnSubmit" class="btn btn-primary" type="button" style="width: 70px;margin-left: 10px" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover" style="width: 98%;margin-top: 24px" align="center">
		<thead>
		<tr>
			<th width="150">姓名</th>
			<th width="150">联系电话</th>
		</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${userList}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${entity.name}</td>
				<td>${entity.mobile}</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div style="height: 60px;width: 100%"></div>
	<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
		<hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
		<div style="float: right;margin-top: 10px;margin-right: 20px">
			<input id="btnCancel" class="btn" type="button" value="关 闭" style="width: 96px;height: 40px" onclick="cancel()"/>
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
