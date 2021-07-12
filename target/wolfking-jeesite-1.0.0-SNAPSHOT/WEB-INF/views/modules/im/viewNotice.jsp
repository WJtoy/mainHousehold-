<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
	<title>浏览通知</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<style type="text/css">
		html,body
		{
			height:100%;
			margin:0 auto;
		}
		/* background-color: #393D49; color: #fff;*/
	</style>
</head>
<body>
	<div style="padding: 10px; height: 100%; line-height: 22px; font-weight: 300;">
		<sys:message content="${message}"/>
		<form:form id="viewNoticeForm" modelAttribute="notice" cssStyle="height: 100%;">
			<div id="divContent" style="padding: 3px; width:100%; height: 100%; line-height: 22px;  font-weight: 300;">
				${notice.content}
			</div>
		</form:form>
	</div>
</body>
</html>