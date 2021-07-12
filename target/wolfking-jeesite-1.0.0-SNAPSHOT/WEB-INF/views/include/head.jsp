<%@ page contentType="text/html;charset=UTF-8" %>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
<meta HTTP-EQUIV="pragma" CONTENT="no-cache">
<meta HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
<meta HTTP-EQUIV="expires" CONTENT="0">
<meta name="author" content=""/>
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
<meta name="referrer" content="no-referrer" /> <!--可以让img标签预加载网络图片-->
<%--<meta http-equiv="X-UA-Compatible" content="IE=7,IE=8,IE=9,IE=10" />--%>
<script src="${ctxStatic}/jquery/jquery-1.9.1.min.js" type="text/javascript"></script>
<script src="${ctxStatic}/jquery/jquery.form.min.js" type="text/javascript"></script>
<script src="/static/jquery/jquery-migrate-1.1.1.min.js" type="text/javascript"></script>
<%--<script src="/static/jquery/jquery.cookie.js" type="text/javascript"></script>--%>
<script src="/static/js/cookie.js" type="text/javascript"></script>
<script src="/static/jquery/json3.min.js" type="text/javascript"></script>
<script src="/static/jquery/jquery.ba-resize.min.js" type="text/javascript"></script>
<link href="${ctxStatic}/jquery-validation/1.11.0/jquery.validate.min.css" type="text/css" rel="stylesheet"/>
<script src="${ctxStatic}/jquery-validation/1.11.0/jquery.validate.min.js?_v=${OrderJsVersion}" type="text/javascript"></script>
<script src="${ctxStatic}/jquery-validation/1.11.0/localization/messages_zh.js" type="text/javascript"></script>
<%--<script src="${ctxStatic}/jquery-validation/1.11.0/jquery-validate.bootstrap-tooltip.js" type="text/javascript"></script>--%>
<link href="${ctxStatic}/bootstrap/2.3.1/css_${not empty cookie.theme.value ? cookie.theme.value : 'cerulean'}/bootstrap.min.css"
      type="text/css" rel="stylesheet"/>
<script src="${ctxStatic}/bootstrap/2.3.1/js/bootstrap.min.js" type="text/javascript"></script>
<link href="${ctxStatic}/bootstrap/2.3.1/awesome/font-awesome.min.css" type="text/css" rel="stylesheet"/>
<!--[if lte IE 7]><link href="${ctxStatic}/bootstrap/2.3.1/awesome/font-awesome-ie7.min.css" type="text/css" rel="stylesheet" /><![endif]-->
<!--[if lte IE 6]><link href="${ctxStatic}/bootstrap/bsie/css/bootstrap-ie6.min.css" type="text/css" rel="stylesheet" />
<script src="${ctxStatic}/bootstrap/bsie/js/bootstrap-ie.min.js" type="text/javascript"></script><![endif]-->
<!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]> <script src="/static/common/html5.js"></script><![endif]-->

<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>

<link href="${ctxStatic}/jquery-select2/3.4/select2.min.css" rel="stylesheet"/>
<script src="${ctxStatic}/jquery-select2/3.4/select2.min.js" type="text/javascript"></script>
<link href="${ctxStatic}/jquery-jbox/2.3/Skins/Bootstrap/jbox.min.css" rel="stylesheet"/>
<script src="${ctxStatic}/jquery-jbox/2.3/jquery.jBox-2.3.min.js" type="text/javascript"></script>
<script src="${ctxStatic}/My97DatePicker/WdatePicker.js" type="text/javascript"></script>
<script src="${ctxStatic}/common/mustache.min.js" type="text/javascript"></script>
<link href="${ctxStatic}/common/jeesite.min.css?_v=${OrderJsVersion}" type="text/css" rel="stylesheet"/>
<script src="${ctxStatic}/common/jeesite.min.js?_v=${OrderJsVersion}" type="text/javascript"></script>
<script src="${ctxStatic}/common/Utils.js?_v=${OrderJsVersion}" type="text/javascript"></script>
<script src="${ctxStatic}/common/dateformat.min.js" type="text/javascript"></script>
<link href="${ctxStatic}/fancyBox/source/jquery.fancybox.css" type="text/css" rel="stylesheet"/>
<script src="${ctxStatic}/fancyBox/source/jquery.fancybox.js" type="text/javascript"></script>
<script src="${ctxStatic}/scroll/jquery.slimscroll.min.js" type="text/javascript"></script>
<%--<link href="${ctxStatic}/layer/skin/default/layer.css" type="text/css" rel="stylesheet"/>--%>
<script src="${ctxStatic}/layer/layer.js?_v=${OrderJsVersion}" type="text/javascript"></script>
<script src="${ctxStatic}/jquery/jQueryRotate.min.js" type="text/javascript"></script>
<script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
<script type="text/javascript">var ctx = '${ctx}', ctxStatic = '${ctxStatic}';</script>