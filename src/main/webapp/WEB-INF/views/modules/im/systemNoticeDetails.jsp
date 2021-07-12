<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>通知详情</title>
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
        .table thead th,.table tbody td {text-align: center;vertical-align: middle;}
        .tdlable {width:90px;text-align: right;}
        .tdbody {width:300px ;}
        .table th,.table td {padding: 4px;}
        .table thead th {text-align: center;vertical-align: middle;}
        .table .tdcenter {text-align: center;vertical-align: middle;}
        .alert {padding: 4px 5px 4px 4px; margin-right: 5px;}
        #toolbar{height: 40px;line-height: 40px;}
        .form-horizontal .control-label{width:90px;}
        .form-horizontal .controls {margin-left:120px;}
        i[class^="icon-"] {font-size:18px;}
    </style>
</head>
<body>
    <sys:message content="${message}"/>
    <div class="accordion-group form-horizontal" style="margin-top:0px;">
        <div id="divheader" class="accordion-body">
            <table class="table table-bordered table-hover" style="margin-bottom: 0px;">
                <tbody>
                <tr>
                    <td class="tdlable">
                        <label>标题:</label>
                    </td>
                    <td class="tdbody">
                        ${systemNotice.title}
                    </td>
                </tr>
                <tr>
                    <td class="tdlable"><label>副标题:</label></td>
                    <td class="tdbody">${systemNotice.subtitle}</td>
                </tr>
                <tr style="height: 200px;">
                    <td class="tdlable"><label>通知内容:</label></td>
                    <td class="tdbody" style="text-align: left">${fns:unescapeHtml(systemNotice.content)}</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>
