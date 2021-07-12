<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户产品</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	  <script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
	  <link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
	  <style type="text/css">
		  .table thead th,.table tbody td {
			  text-align: center;
			  vertical-align: middle;
		  }
	  </style>
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
              var areaId = $("#areaIdId").val();
              if(areaId==null || areaId==''){
                  layerMsg("请选择区域");
                  return false;
			  }
              if (clicktag == 0) {
                  clicktag = 1;
                  beforePage();
                  setPage();
                  this.form.submit();
              }
          });

          $(document).on("change", "#areaId", function () {
              var itemText = $("#areaId option:selected").text();
             $("#provinceName").val(itemText)
          });

          var timelinessClickTag = 0
          //时效开启或者关闭
		  function editTimelinessFlag(areaId,flag) {
              var text ="确定该区域开启快可立补贴吗？"
			  if(flag==0){
                  text ="确定该区域关闭快可立补贴吗？"
			  }
              top.layer.confirm(text, {icon: 3, title:'系统确认'}, function(index){
                  if(timelinessClickTag==1){
                      return false;
				  }
                  timelinessClickTag=1
                  var loadingIndex = layerLoading('正在提交，请稍候...');
                  $.ajax({
                      url:"${ctx}/md/servicepoint/saveTimeliness",
                      type:"POST",
                      data:{areaId:areaId,timeLinessFlag:flag},
                      dataType:"json",
                      success: function(data){
                          //提交后的回调函数
                          if(loadingIndex) {
                              top.layer.close(loadingIndex);
                          }
                          if(ajaxLogout(data)){
                              setTimeout(function () {
                                  timelinessClickTag = 0;
                              }, 2000);
                              return false;
                          }
                          if (data.success) {
                              layerMsg("保存成功");
                              timelinessClickTag = 0;
                              $("#searchForm").submit();
                          }else{
                              setTimeout(function () {
                                  timelinessClickTag = 0;
                              }, 2000);
                              layerError(data.message, "错误提示");
                          }
                          return false;
                      },
                      error: function (data)
                      {
                          if(loadingIndex) {
                              top.layer.close(loadingIndex);
                          }
                          setTimeout(function () {
                              timelinessClickTag = 0;
                          }, 2000);
                          ajaxLogout(data,null,"数据保存错误，请重试!");
                          //var msg = eval(data);
                      },
                      timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                  });
			  });
          }

          //客户时效开启或关闭
		  var customerTimelinessTag
		  function editCustomerTimelinessFlag(areaId,flag) {
              var text ="确定该区域开启客户时效吗？"
              if(flag==0){
                  text ="确定该区域关闭客户时效吗？"
              }
              top.layer.confirm(text, {icon: 3, title:'系统确认'}, function(index){
                  if(customerTimelinessTag==1){
                      return false;
                  }
                  customerTimelinessTag=1
                  var loadingIndex = layerLoading('正在提交，请稍候...');
                  $.ajax({
                      url:"${ctx}/md/servicepoint/saveCustomerTimeliness",
                      type:"POST",
                      data:{areaId:areaId,customerTimeLinessFlag:flag},
                      dataType:"json",
                      success: function(data){
                          //提交后的回调函数
                          if(loadingIndex) {
                              top.layer.close(loadingIndex);
                          }
                          if(ajaxLogout(data)){
                              setTimeout(function () {
                                  customerTimelinessTag = 0;
                              }, 2000);
                              return false;
                          }
                          if (data.success) {
                              layerMsg("保存成功");
                              customerTimelinessTag = 0;
                              $("#searchForm").submit();
                          }else{
                              setTimeout(function () {
                                  customerTimelinessTag = 0;
                              }, 2000);
                              layerError(data.message, "错误提示");
                          }
                          return false;
                      },
                      error: function (data)
                      {
                          if(loadingIndex) {
                              top.layer.close(loadingIndex);
                          }
                          setTimeout(function () {
                              customerTimelinessTag = 0;
                          }, 2000);
                          ajaxLogout(data,null,"数据保存错误，请重试!");
                          //var msg = eval(data);
                      },
                      timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                  });
              });
          }
	  </script>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/md/timelinessPriceNew/list">补贴金额</a></li>
		<li>
			<a href="${ctx}/md/areaTimelinessNew/list">区域设置</a>
		</li>
		<li><a href="${ctx}/md/servicepoint/findServicePointTimelinessList">网点设置</a></li>
		<li class="active"><a href="javascript:void(0);">网点批量设置</a></li>
	</ul>
	<form:form id="searchForm" modelAttribute="servicePointTimeLinessSummaryDto" action="${ctx}/md/servicepoint/servicePointAreaTimelinessList" method="post" class="breadcrumb form-search">
		<div style="margin-top: 8px">
			<label><span style="color: red">*</span>区&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;域：</label>
			<%--<form:select path="areaId" cssClass="input-small" cssStyle="width:150px;">
				<form:option value="" label="所有"/>
				<form:options items="${provinceList}" itemLabel="name" itemValue="id" htmlEscape="false"/>
			</form:select>--%>
            <sys:newprovinceeareaselect id="areaId" name="areaId" value="${servicePointTimeLinessSummaryDto.areaId}"
                                    labelValue="${servicePointTimeLinessSummaryDto.areaName}" labelName="areaName"
                                    title="区域" mustSelectCounty="false" cssClass="required"> </sys:newprovinceeareaselect>
            &nbsp;&nbsp;&nbsp;
			<input type="hidden" id="provinceName" name="provinceName" value="${provinceName}">
			&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询" />
		</div>
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="250" rowspan="2">省</th>
				<th width="250" rowspan="2">市</th>
				<th width="500" colspan="3">快可立补贴</th>
				<th width="500" colspan="3">客户时效</th>
			</tr>
			<tr>
				<th width="150">开启</th>
				<th width="150">关闭</th>
				<th width="200">操作</th>
				<th width="150">开启</th>
				<th width="150">关闭</th>
				<th width="200">操作</th>
			</tr>
		</thead>
		<tbody>
		<c:if test="${list!=null && list.size()>0}">
			<tr><td rowspan="${list.size() > 0 ? list.size() +1 : ''}">${servicePointTimeLinessSummaryDto.areaName}</td></tr>
		</c:if>
		<c:forEach items="${list}" var="entity">
			<tr>
				<td>${entity.areaName}（${entity.timeLinessCount}）</td>
				<td><a href="${ctx}/md/servicepoint/findServicePointTimelinessList?areaId=${entity.areaId}&timeLinessFlag=1&areaName=${fns:urlEncode(entity.areaName)}">${entity.timeLinessEnabled}</a></td>
				<td><a href="${ctx}/md/servicepoint/findServicePointTimelinessList?areaId=${entity.areaId}&timeLinessFlag=0&areaName=${fns:urlEncode(entity.areaName)}" style="color: red">${entity.timeLinessDisabled}</a></td>
				<td>
					<c:choose>
						<c:when test="${entity.timeLinessEnabled==0}">
							<a href="javascript:editTimelinessFlag('${entity.areaId}',1);"><span style="background-color: #0096DA;color: white;padding: 2px 4px;border-radius: 3px">开启</span></a>
						</c:when>
						<c:when test="${entity.timeLinessDisabled==0}">
							<a href="javascript:editTimelinessFlag('${entity.areaId}',0)"><span style="background-color: #F54142;color: white;padding: 2px 4px;border-radius: 3px">关闭</span></a>
						</c:when>
						<c:otherwise>
							<a href="javascript:editTimelinessFlag('${entity.areaId}',1);">开启</a><a href="javascript:editTimelinessFlag('${entity.areaId}',0)" style="margin-left: 24px">关闭</a>
						</c:otherwise>
					</c:choose>
				</td>
				<td><a href="${ctx}/md/servicepoint/findServicePointTimelinessList?areaId=${entity.areaId}&customerTimeLinessFlag=1&areaName=${fns:urlEncode(entity.areaName)}">${entity.customerTimeLinessEnabled}</a></td>
				<td><a href="${ctx}/md/servicepoint/findServicePointTimelinessList?areaId=${entity.areaId}&customerTimeLinessFlag=0&areaName=${fns:urlEncode(entity.areaName)}" style="color: red">${entity.customerTimeLinessDisabled}</a></td>
				<td>
					<c:choose>
						<c:when test="${entity.customerTimeLinessEnabled==0}">
							<a href="javascript:editCustomerTimelinessFlag('${entity.areaId}',1);"><span style="background-color: #0096DA;color: white;padding: 2px 4px;border-radius: 3px">开启</span></a>
						</c:when>
						<c:when test="${entity.customerTimeLinessDisabled==0}">
							<a href="javascript:editCustomerTimelinessFlag('${entity.areaId}',0)"><span style="background-color: #F54142;color: white;padding: 2px 4px;border-radius: 3px">关闭</span></a>
						</c:when>
						<c:otherwise>
							<a href="javascript:editCustomerTimelinessFlag('${entity.areaId}',1);">开启</a><a href="javascript:editCustomerTimelinessFlag('${entity.areaId}',0)" style="margin-left: 24px">关闭</a>
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
        });
	</script>
  </body>
</html>
