<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>完成照片</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/ServicePointOrderService.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
    <script src="${ctxStatic}/js/ajaxfileupload.js"></script>
    <!-- image viewer -->
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <script type="text/javascript">
        var this_index = top.layer.index;
        ServicePointOrderService.rootUrl = "${ctx}";
        var clickTag = 0;
        $(document).ready(function () {
            var $tbviewer = $('#tb_images');
            $tbviewer.viewer({
                viewed: function () {
                    //this.viewer.zoomTo(1);//100%原始大小显示
                }
            });
        });
    </script>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
</head>
<body style="margin: 0px 10px 3px 10px;">
<fieldset style="margin: 0px 5px 0px 5px;">
    <input type="hidden" id="id" name="id" value="${order.id}"/>
    <input type="hidden" id="quarter" name="quarter" value="${order.quarter}"/>
    <legend>${order.orderNo} 已完成照片列表</legend>
    <table width="100%" border="0" id="tb_images"
           class="table table-striped table-bordered table-condensed"
           style="margin-bottom: 0px;">
        <thead>
        <tr>
            <th width="85%">图片</th>
            <th width="15%">上传时间</th>
        </tr>
        </thead>
        <tbody>
        <c:if test="${order != null && order.attachments != null && fn:length(order.attachments) >0}">
            <c:set var="index" value="0"></c:set>
        <c:forEach items="${order.attachments}" var="attach">
            <c:set var="index" value="${index+1}"></c:set>
        <tr id="accesstr${index}">
            <td>
                <c:set var="userType" value="${attach.createBy.userType}"/>
                <c:set var="userTypeName" value=""/>
                <c:choose>
                    <c:when test="${userType == 0}"><c:set var="userTypeName" value=""/> </c:when>
                    <c:when test="${userType == 1 || userType == 2}"><c:set var="userTypeName" value="(客服)"/> </c:when>
                    <c:when test="${userType == 3 || userType == 4 || userType == 9}"><c:set var="userTypeName"
                                                                                             value="(厂商)"/></c:when>
                    <c:when test="${userType == 5}"><c:set var="userTypeName" value="(网点)"/></c:when>
                    <c:when test="${userType == 7}"><c:set var="userTypeName" value="(业务)"/></c:when>
                    <c:otherwise></c:otherwise>
                </c:choose>
                <a href="javascript:;">
                    <img title="${attach.remarks}" alt="上传人[${attach.createBy.name}]${userTypeName}"
                         src="${ctxUpload}/${attach.filePath}" data-original="${ctxUpload}/${attach.filePath}"
                         style="max-width: 800px;">
                </a>
                <br/>上传人[${attach.createBy.name}](${userTypeName})
            </td>
            <td>
                <fmt:formatDate value="${attach.createDate}" pattern="yyyy-MM-dd HH:mm"/>
            </td>
        </tr>
        </c:forEach>
        </c:if>
        <tbody>
    </table>
</fieldset>
</body>
</html>