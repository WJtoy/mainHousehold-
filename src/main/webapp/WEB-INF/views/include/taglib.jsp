<%@ taglib prefix="shiro" uri="/WEB-INF/tlds/shiros.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fns" uri="/WEB-INF/tlds/fns.tld" %>
<%@ taglib prefix="sys" tagdir="/WEB-INF/tags/sys" %>
<%@ taglib prefix="md" tagdir="/WEB-INF/tags/md" %>
<%@ taglib prefix="sd" tagdir="/WEB-INF/tags/sd" %>
<%@ taglib prefix="servicePoint" tagdir="/WEB-INF/tags/servicePoint" %>
<%@ taglib prefix="rpt" tagdir="/WEB-INF/tags/rpt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}${fns:getAdminPath()}"/>
<c:set var="ctxStatic" value="${pageContext.request.contextPath}/static"/>
<%--<c:set var="ctxFront" value="${pageContext.request.contextPath}${fns:getFrontPath()}"/>--%>
<c:set var="ctxUpload" value="${pageContext.request.contextPath}/uploads"/>
<c:set var="ctxPlugin" value="${pageContext.request.contextPath}/plugin"/>
<c:set var="OrderJsVersion" value="${fns:getDictLabelFromMS('Version', 'OrderJsVer', '')}"/><%-- --%>