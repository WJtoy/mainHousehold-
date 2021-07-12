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
          var layerIndex = top.layer.index;
          function cancel() {
              top.layer.close(layerIndex)
          }
	  </script>
  </head>

  <body>
	<sys:message content="${message}"/>
	<table align="center" id="contentTable" class="table table-striped table-bordered table-condensed table-hover" style="margin-top: 10px;width: 600px;">
		<thead>
			<tr>
				<th width="250">市</th>
				<th width="250">区/县</th>
				<th width="500">街道</th>
			</tr>
		</thead>
		<tbody>
		<tr>
			<td rowspan="${areaModel.subAreas.size()>0?areaModel.subAreas.size():''}">${areaModel.parent.name}</td>
			<td rowspan="${areaModel.subAreas.size()>0?areaModel.subAreas.size():''}">${areaModel.name}</td>
			<c:choose>
			   <c:when test="${areaModel.subAreas.size() >0}">
			      <c:forEach items="${areaModel.subAreas}" var="area" varStatus="i">
			         <c:choose>
			             <c:when test="${i.index ==0}">
							<td>
								${area.name}
							</td>
			             </c:when>
			             <c:otherwise>
							<tr>
								<td>
									${area.name}
								</td>
							</tr>
		                 </c:otherwise>
		             </c:choose>
		          </c:forEach>
		        </c:when>
		    <c:otherwise>
				<td></td>
				<td></td>
		    </c:otherwise>
		</c:choose>
		</tr>
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
