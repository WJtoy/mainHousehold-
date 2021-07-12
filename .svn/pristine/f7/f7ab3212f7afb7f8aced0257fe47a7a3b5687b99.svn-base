<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <title>app反馈类型</title>
	<meta name="decorator" content="default"/>
	  <script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
	  <link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>

	  <script type="text/javascript">

          $(document).ready(function () {
              $("#contentTable").treeTable({expandLevel : 2});

              /*switchEvent(this,function(){
                  alert($(this).hasClass('switch-on'))
              },function() {
                  alert($(this).hasClass('switch-on'))
              });*/

          });
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

          function editAppFeedbackType(type,id) {
              var text = "工单反馈-添加分类";
              var url = "${ctx}/provider/md/appFeedbackType/form?id=" + id;
              if(type == 2){
                  text = "工单反馈-修改"
			  }
              top.layer.open({
                  type: 2,
                  id:"appFeedbackType",
                  zIndex:19891015,
                  title:text,
                  content: url,
                  area: ['580px', '280px'],
                  shade: 0.3,
                  maxmin: false,
                  success: function(layero,index){
                  },
                  end:function(){
                  }
              });
          }


          function addReason(type,id,parentId) {
              var text = "工单反馈-添加原因";
              var url = "${ctx}/provider/md/appFeedbackType/feedbackReasonFrom?id=" + id + "&parentId=" + parentId;
              if(type == 2){
                  text = "工单反馈-修改"
              }
              top.layer.open({
                  type: 2,
                  id:"appFeedbackReason",
                  zIndex:19891015,
                  title:text,
                  content: url,
                  area: ['800px', '480px'],
                  shade: 0.3,
                  maxmin: false,
                  success: function(layero,index){
                  },
                  end:function(){
                  }
              });
          }

          function disableOrEnable(ieEffect,value,parentId) {
             var item;
             if(ieEffect == 0){
                 item = 1
			 }else{
                 item = 0
			 }
              $.ajax({
                  url:"${ctx}/provider/md/appFeedbackType/disableOrEnable?id=" + value + "&isEffect=" + item + "&parentId=" + parentId,
                  type:"POST",
                  dataType:"json",
                  success: function(data){
                      if(ajaxLogout(data)){
                          return false;
                      }
                      if (data.success) {
                          layerMsg(data.message);
                          repage()
                      }else{
                          layerMsg(data.message);
                          repage()
                      }
                      return false;
                  },
                  error: function (data)
                  {
                      layerMsg("保存数据错误")
                      repage()

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
	<form:form id="searchForm" modelAttribute="appFeedbackType" action="${ctx}/provider/md/appFeedbackType/findList" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>名称:</label><form:input path="label" htmlEscape="false" maxlength="30" class="input-small"/>&nbsp;
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
    <shiro:hasPermission name="md:appfeedbacktype:edit">
		<button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 100px;height: 30px" onclick="editAppFeedbackType(1,'')">
			<i class="icon-plus-sign"></i>&nbsp;添加分类
		</button>
	</shiro:hasPermission>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="350">反馈名称</th>
				<th width="350">描述</th>
				<th width="200">简称</th>
				<th width="100">数值</th>
				<th width="70">使用方</th>
				<th width="50">次数</th>
				<th width="70">计次方式</th>
				<th width="50">标记异常</th>
				<th width="70">处理方式</th>
				<th width="50">排序</th>
				<th width="60">启用</th>
				<shiro:hasPermission name="md:appfeedbacktype:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr id="${entity.id}" pId="0">
				<td style="display:table-cell; vertical-align:middle">${entity.label}</td>
				<td></td>
				<td></td>
				<td>${entity.value}</td>
				<td></td>
				<%--<td>${entity.actionType}</td>--%>
				<td></td>
				<td></td>
				<td></td>
				<td></td>
				<td style=" display:table-cell; vertical-align:middle">${entity.sortBy}</td>
				<c:choose>
					<c:when test="${entity.isEffect==0}">
						<td style="display:table-cell; vertical-align:middle"><span class="switch-off" style="zoom: 0.7" onclick="disableOrEnable('${entity.isEffect}','${entity.id}','${entity.parentId}')"></span></td>
					</c:when>
					<c:otherwise>
						<td style="display:table-cell; vertical-align:middle"><span class="switch-on"  style="zoom: 0.7" onclick="disableOrEnable('${entity.isEffect}','${entity.id}','${entity.parentId}')"></span></td>
					</c:otherwise>
				</c:choose>
				<shiro:hasPermission name="md:appfeedbacktype:edit"><td style="display:table-cell; vertical-align:middle">
    				<a href="javascript:editAppFeedbackType(2,'${entity.id}')">修改</a>
					<a href="javascript:addReason(1,'','${entity.id}')">添加原因</a>
				<%--	<a href="${ctx}/provider/md/customerProduct/delete?id=${entity.id}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>--%>
				</td></shiro:hasPermission>
			</tr>
			<c:forEach items="${entity.feedbackTypeVModelList}" var="item">
				<tr id="${item.id}" pId="${item.parentId}">
					<td style=" display:table-cell; vertical-align:middle">
						<a href="javascript:addReason(2,'${item.id}','')">${item.label}</a>
					</td>
					<td style="display:table-cell; vertical-align:middle">${item.remarks}</td>
					<%--<c:choose>
						<c:when test="${item.feedbackType == 1}">
							<td>停滞</td>
						</c:when>
						<c:when test="${item.feedbackType == 2}">
							<td>异常</td>
						</c:when>
						<c:otherwise>
							<td>未知类型</td>
						</c:otherwise>
					</c:choose>--%>
					<td style="display:table-cell; vertical-align:middle">${item.name}</td>
					<td style="display:table-cell; vertical-align:middle">${item.value}</td>
					<td style="display:table-cell; vertical-align:middle">${item.userTypeName}</td>
					<td style="display:table-cell; vertical-align:middle">${item.abnormalyOverTimes}</td>
					<td style="display:table-cell; vertical-align:middle">${item.sumTypeName}</td>
					<%--<td>${item.actionType}</td>--%>
					<c:choose>
						<c:when test="${item.isAbnormaly==0}">
							<td style="display:table-cell; vertical-align:middle">否</td>
						</c:when>
						<c:when test="${item.isAbnormaly==1}">
							<td style="display:table-cell; vertical-align:middle">是</td>
						</c:when>
						<c:otherwise>
							<td style="display:table-cell; vertical-align:middle">未知</td>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${item.actionType==0}">
							<td style="display:table-cell; vertical-align:middle">无</td>
						</c:when>
						<c:otherwise>
							<td style="display:table-cell; vertical-align:middle">${item.actionTypeName}</td>
						</c:otherwise>
					</c:choose>
					<td style="display:table-cell; vertical-align:middle">${item.sortBy}</td>
					<c:choose>
						<c:when test="${item.isEffect==0}">
							<td style="display:table-cell; vertical-align:middle"><span class="switch-off" style="zoom: 0.7" onclick="disableOrEnable('${item.isEffect}','${item.id}','${item.parentId}')"></span></td>
						</c:when>
						<c:otherwise>
							<td style="display:table-cell; vertical-align:middle"><span class="switch-on"  style="zoom: 0.7" onclick="disableOrEnable('${item.isEffect}','${item.id}','${item.parentId}')"></span></td>
						</c:otherwise>
					</c:choose>
					<shiro:hasPermission name="md:appfeedbacktype:edit"><td style="display:table-cell; vertical-align:middle;text-align: center">
						<a href="javascript:addReason(2,'${item.id}','')">修改</a>
						<%--<a href="${ctx}/provider/md/customerProduct/delete?id=${item.id}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>--%>
					</td></shiro:hasPermission>
				</tr>
			</c:forEach>
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
