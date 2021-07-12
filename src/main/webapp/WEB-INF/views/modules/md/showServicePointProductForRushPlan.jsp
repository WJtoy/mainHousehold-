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
	<div style="border: 1px solid #BBBBBB;width: 600px;border-bottom: none;background-color: #F6F6F6;margin-left: 22px;height: 50px;margin-top: 24px">
		<span style="line-height: 3.5;font-size: 15px;font-weight: bold;margin-left: 24px">${categoryName}</span>
	</div>
	<div style="border: 1px solid #BBBBBB;width: 600px;height: 200px;margin-left: 22px">
		<c:forEach items="${productList}" var="entity">
			<div style="float: left;width: 28%;text-align: left;margin-top: 10px;margin-left: 24px">
				${entity.name}
			</div>
		</c:forEach>
	</div>
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
