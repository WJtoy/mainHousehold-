<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE html>
<head>
	<title>全国区域网点覆盖表</title>
	<meta name="decorator" content="default" />
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
	<style type="text/css">
		.table thead th,.table tbody td {
			text-align: center;
			vertical-align: middle;
			BackColor: Transparent;
		}
	</style>
	<%--<script type="text/javascript" language="javascript">--%>
		<%--$(document).ready(function() {--%>
		<%--});--%>
	<%--</script>--%>
	<script type="text/javascript">
        $(document).ready(function() {
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});

            $("#btnExport").click(function() {
                top.$.jBox.confirm("确认要导出覆盖网点吗？","系统提示",function(v,h,f){
                    if(v=="ok"){
                        $("#searchForm").attr("action","${ctx}/sys/area/coverlist_export");
                        $("#searchForm").submit();
                    }
                },{buttonsFocus:1});
                top.$('.jbox-body .jbox-icon').css('top','55px');
            });

            <%--$("#btnSubmit").click(function(){--%>
                <%--top.$.jBox.tip('请稍候...', 'loading');--%>
                <%--$("#searchForm").attr("action","${ctx}/rpt/orderdetail/completed");--%>
                <%--$("#searchForm").submit();--%>

            <%--});--%>
        });

	</script>
</head>
<body>
	<h3 align="center">网点覆盖表</h3>
    <form:form id="searchForm"  action="${ctx}/sys/area/coverlist_export" method="post" class="breadcrumb form-search">
        <div>

            &nbsp;&nbsp;
            <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
        </div>
    </form:form>


	<sys:message content="${message}" />
	<%--<div>
		<table id="contentTable" class="fancyTable datatable table table-bordered table-condensed" style="table-layout:fixed" cellspacing="0" width="100%">
			<thead>
				<th width="120">省</th>
				<th width="180">市</th>
				<th>区（县）</th>
				<th>街道</th>
			</thead>
			<tbody>
			<c:forEach items="${list}" var="item">
				<tr>
					<td rowspan="${item.maxRow}">${item.provinceName}</td>
					<c:forEach  var="i" begin="0" end="${item.maxRow-1}">
						<c:if test="${i ne 0}">
							<tr>
						</c:if>
						<c:choose>
							<c:when test = "${i lt item.cityList.size()}">
								<td>${item.cityList.get(i).cityName}</td>
								<td>${item.cityList.get(i).countyListString}</td>
							</c:when>
							<c:otherwise>									
								<td></td>
								<td></td>
							</c:otherwise>
						</c:choose>	
						<c:if test="${i eq 0}">
							</tr>
						</c:if>
					</c:forEach>
				</tr>
			</c:forEach>
			</tbody>
		</table>
	</div>--%>
	<table id="contentTable" class="fancyTable datatable table table-bordered table-condensed" style="table-layout:fixed" cellspacing="0" width="100%">
		<thead>
		<th width="120">省</th>
		<th width="180">市</th>
		<th width="180">区（县）</th>
		<th>街道</th>
		</thead>
		<tbody>
		<c:forEach items="${list}" var="item">
			<tr>
			<td rowspan="${item.maxRow}">${item.provinceName}</td>
			<c:forEach  var="i" begin="0" end="${item.cityList.size()-1}">
				<c:if test="${i ne 0}">
					<tr>
				</c:if>
				<c:choose>
					<c:when test = "${i lt item.cityList.size()}">
						<td rowspan="${item.cityList.get(i).countyMaxRow}">${item.cityList.get(i).cityName}</td>
						<c:forEach var="j" begin="0" end="${item.cityList.get(i).countyMaxRow-1}">
							<c:if test="${j ne 0}">
								<tr>
							</c:if>
							 <c:choose>
								 <c:when test="${j lt item.cityList.get(i).countyList.size()}">
									 <td>${item.cityList.get(i).countyList.get(j).countyName}</td>
									 <td>${item.cityList.get(i).countyList.get(j).countyListString}</td>
								 </c:when>
								 <c:otherwise>
									 <td></td>
									 <td></td>
								 </c:otherwise>
							 </c:choose>
							<c:if test="${j eq 0}">
								</tr>
							</c:if>
						</c:forEach>
					</c:when>
					<c:otherwise>
						<td></td>
						<td></td>
						<td></td>
					</c:otherwise>
				</c:choose>
				<c:if test="${i eq 0}">
					</tr>
				</c:if>
			</c:forEach>
			</tr>
		</c:forEach>
		</tbody>
	</table>
</body>
</html>
