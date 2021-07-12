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
	  <script src="${ctxStatic}/area/AreaProvince.js" type="text/javascript"></script>
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
              if (clicktag == 0) {
                  clicktag = 1;
                  beforePage();
                  setPage();
                  this.form.submit();
              }
          });

          function editCustomerProduct(id) {
              top.layer.open({
                  type: 2,
                  id:"custoemrProduct",
                  zIndex:19891015,
                  title:"修改",
                  content: "${ctx}/md/servicepoint/formServiceTimeliness?id="+ id,
                  area: ['576px', '420px'],
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
		<li><a href="${ctx}/md/timelinessPriceNew/list">补贴金额</a></li>
		<li>
			<a href="${ctx}/md/areaTimelinessNew/list">区域设置</a>
		</li>
		<li class="active"><a href="javascript:void(0);">网点设置</a></li>
		<li><a href="${ctx}/md/servicepoint/servicePointAreaTimelinessList">网点批量设置</a></li>
	</ul>
	<form:form id="searchForm" modelAttribute="servicePointDto" action="${ctx}/md/servicepoint/findServicePointTimelinessList" method="post" class="breadcrumb form-search">
		<div>
			<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
			<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
			<label>网点编号：</label>
			<input type=text class="input-small" name="servicePointNo" value="${servicePointDto.servicePointNo}" maxlength="30" style="width: 200px"/>&nbsp;
			&nbsp;
			<label>网点名称：</label>
			<input type=text class="input-small" name="name" value="${servicePointDto.name}" maxlength="30" style="width: 200px"/>&nbsp;
			&nbsp;
			<label>网点电话：</label>
			<input type=text class="input-small" name="contactInfo1" value="${servicePointDto.contactInfo1}" maxlength="30" style="width: 200px"/>&nbsp;
			&nbsp;
			<label>区&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;域：</label>
			<sys:newtableareaselect id="areaId" name="areaId" value="${servicePointDto.areaId}"
									labelValue="${servicePointDto.areaName}" labelName="areaName"
									title="区域" mustSelectCounty="false" cssClass="required"> </sys:newtableareaselect>
			&nbsp;
			<label>快可立补贴：</label>
			<form:select path="timeLinessFlag" cssClass="input-small" cssStyle="width:215px;">
				<form:option value="" label="  "/>
				<form:option value="0" label="关"/>
				<form:option value="1" label="开"/>
			</form:select>&nbsp;
		</div>
		<div style="margin-top: 8px">
			<label>客户时效：</label>
			<form:select path="customerTimeLinessFlag" cssClass="input-small" cssStyle="width:215px;">
				<form:option value="" label="  "/>
				<form:option value="0" label="关"/>
				<form:option value="1" label="开"/>
			</form:select>&nbsp;&nbsp;&nbsp;
			&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询" />
		</div>
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="350">网点编号</th>
				<th width="350">网点名称</th>
				<th width="200">网点电话</th>
				<th>快可立补贴</th>
				<th>客户时效</th>
				<shiro:hasPermission name="md:servicepointtimeliness:edit"> <th>操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${entity.servicePointNo}</td>
				<td>${entity.name}</td>
				<td>${entity.contactInfo1}</td>
				<td>
					<c:choose>
						<c:when test="${entity.timeLinessFlag==0}">
							<span style="background-color: #F54142;color: white;padding: 2px 4px;border-radius: 3px">关</span>
						</c:when>
						<c:otherwise>
							<span style="background-color: #0096DA;color: white;padding: 2px 4px;border-radius: 3px">开</span>
						</c:otherwise>
					</c:choose>
				</td>
				<td>
					<c:choose>
						<c:when test="${entity.customerTimeLinessFlag==0}">
							<span style="background-color: #F54142;color: white;padding: 2px 4px;border-radius: 3px">关</span>
						</c:when>
						<c:otherwise>
							<span style="background-color: #0096DA;color: white;padding: 2px 4px;border-radius: 3px">开</span>
						</c:otherwise>
					</c:choose>
				</td>
				<shiro:hasPermission name="md:servicepointtimeliness:edit">
				<td>
					<a href="javascript:editCustomerProduct(${entity.id})">修改</a>
				</td>
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
