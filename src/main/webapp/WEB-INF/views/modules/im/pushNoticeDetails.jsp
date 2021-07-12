<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>消息提醒</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default" />
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
    <meta name="generator" content="1.0"/>
    <%@include file="/WEB-INF/views/include/dialog.jsp"%>
    <script src="${ctxStatic}/zeroClipboard/ZeroClipboard.js" type="text/javascript"></script>
    <script type="text/javascript" src="${ctxStatic}/zeroClipboard/jquery.zclip.min.js"></script>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
    <link href="${ctxStatic}/bootstrap/2.3.1/bwizard/bwizard.min.css" type="text/css" rel="stylesheet" />

    <style type="text/css">
        /*body{ text-align:center}*/
    </style>
</head>
<script type="text/javascript">
    var HtmlUtil = {
     /*2.用浏览器内部转换器实现html解码*/
        htmlDecode:function (text){
         //1.首先动态创建一个容器标签元素，如DIV
         var temp = document.createElement("div");
         //2.然后将要转换的字符串设置为这个元素的innerHTML(ie，火狐，google都支持)
         temp.innerHTML = text;
         //3.最后返回这个元素的innerText(ie支持)或者textContent(火狐，google支持)，即得到经过HTML解码的字符串了。
         var output = temp.innerText || temp.textContent;
         temp = null;
         return output;
        }
    };
    function getMessage(title,content,unreadCount) {
        var contentText = HtmlUtil.htmlDecode(content);
        $("#title").text(title);
        $("#content").html(contentText);
        /*if(parseInt(unreadCount)>0){
            $("#unreadCount").text("您还有"+ unreadCount +"条未读消息,请在通知列表查看")
            $("#unreadCount").css("display","block");
        }*/
    }
</script>
<body>
    <div class="" style="margin-top:0px;">
        <div style="text-align: center;padding-top: 5%;width: 50%;word-break: break-all;word-wrap: break-word;height: auto;margin-left: 25%;" id="title"></div>
        <div style="padding-top: 3%;width: 80%;word-break: break-all;word-wrap: break-word;height: auto;margin-left: 10%;" id="content"></div>
        <%--<div style="padding-top: 3%;width: 50%;word-break: break-all;word-wrap: break-word;height: auto;margin-left: 10%;color: red;display: none" id="unreadCount"></div>--%>
    </div>
</body>
</html>
