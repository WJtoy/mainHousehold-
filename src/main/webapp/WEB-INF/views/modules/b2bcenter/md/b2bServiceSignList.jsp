<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>B2B对接系统客户与客户签约</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	  <%@ include file="/WEB-INF/views/include/WdateLimitPicker.jsp" %>
	  <script type="text/javascript">

		  $(document).ready(function() {
			  oneYearDatePicker('createDate','updateDate',false);
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
          var this_index = top.layer.index;
		  function form(id,mallId,dataSource) {
              $.ajax({
                  url:"${ctx}/b2bcenter/md/serviceSign/checkShopId?mallId=" + mallId + "&dataSource=" + dataSource,
                  type:"POST",
                  dataType:"json",
                  success: function(data){
                      var text = "设置客户";
                      if(ajaxLogout(data)){
                          setTimeout(function () {
                              $btnDelete.removeAttr('disabled');
                          }, 2000);
                          return false;
                      }
                      if(data.data != null && data.data > 0){
                          top.layer.open({
                              type: 2,
                              id:"customerShop",
                              zIndex:19891015,
                              title:"设置客户",
                              content:"${ctx}/b2bcenter/md/serviceSign/form?id="+ id +"&mallId=" + mallId + "&dataSource=" + dataSource,
                              area: ['600px', '550px'],
                              shade: 0.3,
                              maxmin: false,
                              success: function(layero,index){
                              },
                              end:function(){
                              }
                          });
                      }else {
                          layerError(data.message, "提示");
                      }
                      return false;
                  },
                  error: function (data)
                  {
                      setTimeout(function () {
                          $btnDelete.removeAttr('disabled');
                      }, 2000);
                      ajaxLogout(data,null, "错误，请重试!");
                  },
                  timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
              });

          }

          function serviceType(id,seriverType,dataSource){
              $.ajax({
                  url:"${ctx}/b2bcenter/md/serviceSign/checkIsExist?servType=" + seriverType + "&dataSource=" + dataSource,
                  type:"POST",
                  dataType:"json",
                  success: function(data){
                      var text = "设置服务类型";
                      if(ajaxLogout(data)){
                          setTimeout(function () {
                              $btnDelete.removeAttr('disabled');
                          }, 2000);
                          return false;
                      }
                      if(data.data != null && data.data > 0){
                          top.layer.open({
                              type: 2,
                              id:"customerShop",
                              zIndex:19891015,
                              title:text,
                              content:"${ctx}/b2bcenter/md/serviceSign/serviceTypeForm?id="+ id +"&servType=" + seriverType + "&dataSource=" + dataSource,
                              area: ['600px', '550px'],
                              shade: 0.3,
                              maxmin: false,
                              success: function(layero,index){
                              },
                              end:function(){
                              }
                          });
                      }else {
                          layerError(data.message, "提示");
                      }
                      return false;
                  },
                  error: function (data)
                  {
                      setTimeout(function () {
                          $btnDelete.removeAttr('disabled');
                      }, 2000);
                      ajaxLogout(data,null, "错误，请重试!");
                  },
                  timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
              });
          }
          function audit(id,signStatus,ign) {
              var index = layer.confirm('确定要'+ign+'吗？', {
                  btn: ['确定','取消'] //按钮
              }, function(){
                  layer.close(index);
                  var loadingIndex = layerLoading('正在提交，请稍候...');
                  var $btnDelete = $("#btnDelete");
                  if ($btnDelete.prop("disabled") == true) {
                      event.preventDefault();
                      return false;
                  }
                  $btnDelete.prop("disabled", true);
                  $.ajax({
                      url:"${ctx}/b2bcenter/md/serviceSign/audit?id="+ id +"&signStatus=" + signStatus,
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
                              layerMsg("成功");
                              var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                              if(pframe){
                                  pframe.repage();
                              }
                          }else{
                              setTimeout(function () {
                                  $btnDelete.removeAttr('disabled');
                              }, 2000);
                              layerError(data.message, "提示");
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
                          ajaxLogout(data,null, ign + "错误，请重试!");
                          top.layer.close(loadingIndex);
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
		<li class="active"><a href="javascript:void(0);">待处理</a></li>
		<li>
			<a href="${ctx}/b2bcenter/md/serviceSign/getAgreeList">已同意</a>
		</li>
		<li>
			<a href="${ctx}/b2bcenter/md/serviceSign/getRefuseList">已拒绝</a>
		</li>
	</ul>
	<form:form id="searchForm" modelAttribute="b2BSign" action="${ctx}/b2bcenter/md/serviceSign/getList" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>店铺ID：</label><input id="mallId"  maxlength="50" class="input-small" type="number" name="mallId" value="${b2BSign.mallId}" style="width: 160px"/>
		&nbsp;&nbsp;
		<label>店铺名称：</label><form:input path="mallName" htmlEscape="false" maxlength="50" class="input-small" style="width: 160px"/>
		&nbsp;&nbsp;
		<label>联系电话：</label><form:input path="mobile" htmlEscape="false" maxlength="20" class="input-small" style="width: 160px"/>
		&nbsp;&nbsp;
		<label>申请时间：</label>
		<input id="createDate" name="createDate" type="text" readonly="readonly" style="width:95px;margin-left:4px" maxlength="20" class="input-small Wdate"
			   value="${fns:formatDate(b2BSign.createDate,'yyyy-MM-dd')}" />
		<label>~</label><input id="updateDate" name="updateDate" type="text" readonly="readonly" style="width:95px" maxlength="20" class="input-small Wdate"
							   value="${fns:formatDate(b2BSign.updateDate,'yyyy-MM-dd')}" />
		&nbsp&nbsp
		<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
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
				<shiro:hasPermission name="md:b2bservicesign:edit"><th width="100">操作</th></shiro:hasPermission>
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

				<td><a href="javascript:form('${entity.id}','${entity.mallId}','${entity.dataSource}')">${entity.mallId}</a></td>
				<td>${entity.mallName}</td>

                <td><a href="javascript:serviceType('${entity.id}','${entity.servType}','${entity.dataSource}')">${entity.servType}</a></td>
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

				<shiro:hasPermission name="md:b2bservicesign:edit">
					<td><a href="javascript:audit('${entity.id}',10,'同意')">同意</a>
					<a href="javascript:audit('${entity.id}',20,'拒绝')">拒绝</a></td>
				</shiro:hasPermission>


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
