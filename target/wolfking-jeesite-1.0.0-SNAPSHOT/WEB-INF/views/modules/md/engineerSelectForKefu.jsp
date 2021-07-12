<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>选择安维人员</title>
	<meta name="decorator" content="default" />
	<script src="${ctxStatic}/js/fixtable.js" type="text/javascript"></script>
	<style type="text/css">
	.pagination {  margin: 10px 0;  }
	.td {  word-break: break-all;  }
	</style>
	<script type="text/javascript">
		function page(n,s){
		    var name = $("#name").val();
		    var contactInfo = $("#contactInfo").val();
            var servicePointNo = $("#servicePointNo").val();
		    if(isEmpty(name) && isEmpty(contactInfo) && isEmpty(servicePointNo)){
                layerAlert("请选择网点编号，姓名，手机号中至少一项进行查询");
                return false;
			}
			$("#pageNo").val(n);
			$("#pageSize").val(s);
			$("#searchForm").submit();
	    	return false;
	    }
	    var data = null;
        var index = top.layer.index;
		function setData(i){
			data = items[i];
            top.$("#layui-layer"+index).find(".layui-layer-btn0").trigger("click");
		}

		var items =[
					<c:forEach items="${page.list}" var="engineer">
						{
						    id:'${engineer.id}',
							name:'${engineer.name}',
							phone:'${engineer.contactInfo}',
							grade:${engineer.grade}
						},
		            </c:forEach>
				   ];

        function isEmpty(obj){
            if(typeof obj == "undefined" || obj == null || obj == ""){
                return true;
            }else{
                return false;
            }
        }
	</script>
</head>

<body>
    <c:if test="${searchType==1}">
		<div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
			请选择网点编号，姓名，手机号中至少一项进行查询
		</div>
	</c:if>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<div style="margin-left:3px;margin-right:3px;">
		<form:form id="searchForm" modelAttribute="engineer"  action="${ctx}/md/engineer/selectForKefu" method="post" class="breadcrumb form-search">
			<input name="searchType"  type="hidden" value="2">
			<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
			<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
			<div style="margin:10px 0px 10px 0px;">
				<label>网点编号：</label>
				&nbsp;
				<form:input path="servicePoint.servicePointNo" id="servicePointNo" htmlEscape="false" class="input-small" maxlength="20" cssStyle="width: 200px;"/>
				<label>姓名：</label>
				<form:input path="name" maxlength="50" class="input-mini;" cssStyle="width: 100px;" />
				&nbsp;<label>电话 ：</label>
				<form:input path="contactInfo" maxlength="50" class="input-mini" cssStyle="width: 100px;"/>
				&nbsp;
				<input id="btnSubmit" class="btn btn-primary" type="button" value="查询"  onclick="page()"/>
			</div>
		</form:form>
		<sys:message content="${message}" />
		<table id="contentTable" class="datatable table table-bordered table-condensed table-hover">
			<thead>
				<tr>
					<th width="45">序号</th>
					<th width="120">姓名</th>
					<th width="120">手机</th>
					<th width="120">网点编号</th>
					<th width="80">派单数</th>
					<th width="80">完成单数</th>
					<th width="80">违约单数</th>
					<th width="55">评价分数</th>
					<th>地址</th>
				</tr>
			</thead>
			<tbody>
				<c:set var="index" value="0" />
				<c:forEach items="${page.list}" var="engineer">
					<c:set var="i" value="${i+1}" />
					<tr>
						<td>${i+(page.pageNo-1)*page.pageSize}</td>
						<td><a href="javascript:void(0);" onclick="setData(${index});"> ${engineer.name}</a></td>
						<td>${engineer.contactInfo}</td>
						<td>${engineer.servicePoint.servicePointNo}</td>
						<td><span class="label label-info">${engineer.planCount}</span>
						</td>
						<td><span class="label label-success">${engineer.orderCount}</span>
						</td>
						<td><span class="label label-important">${engineer.breakCount}</span>
						</td>
						<td>${engineer.grade}</td>
						<td>${engineer.address}</td>
					</tr>
					<c:set var="index" value="${index+1}" />
				</c:forEach>
			</tbody>
		</table>
		<div class="pagination">${page}</div>
	</div>
	<script type="text/javascript" language="javascript" class="init">
        $(document).ready(function() {
            $("td,th").css({"text-align":"center","vertical-align":"middle"});
        });
	</script>
</body>
</html>
