<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>产品安装规范</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <style type="text/css">
        html,body{height:100%;margin:0 auto}
        #editBtn{position:fixed;left:0px;bottom:3px;width:100%;height:55px;background:#fff;z-index:10;border-top:1px solid #ccc;border-top:1px solid #e5e5e5;text-align:right}
    </style>
    <script type="text/javascript">
        var this_index = top.layer.index;
        function closeme(){
            top.layer.close(this_index);
        }
    </script>
</head>
<body>
<div style="padding: 20px 20px 0px 20px;height: auto; line-height: 22px; font-weight: 300;">
    <sys:message content="${message}"/>
    <form:form id="viewNoticeForm" modelAttribute="customerProduct" cssStyle="height: auto;">
        <div id="divContent" style="padding: 3px; width:100%; height: 100%; line-height: 22px;  font-weight: 300;">
                ${customerProduct.fixSpec}
        </div>
    </form:form>
    <div id="editBtn">
        <input id="btnCancel" class="btn" type="button" value="关闭" onclick="closeme()" style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
    </div>
</div>
</body>
</html>