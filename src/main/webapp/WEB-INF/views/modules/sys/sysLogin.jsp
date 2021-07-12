<%@ page import="com.wolfking.jeesite.modules.sys.security.FormAuthenticationFilter" %>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>${fns:getConfig('productName')} 登录</title>
    <script src="${ctxStatic}/common/backstretch.min.js"></script>
    <link rel="stylesheet" href="${ctxStatic}/common/typica-login.css">
    <meta name="decorator" content="blank"/>
    <style type="text/css">
        .control-group{border-bottom:0px;}
        label.error{display:block;background:transparent !important;padding-left:0px;}
        .alert{*margin-bottom:0px;}
    </style>
    <script type="text/javascript">

        $(document).ready(function() {
            var date=new Date;
            var year=date.getFullYear();
            $("#currentYear").html(year.toString());

            $.backstretch([
                "${ctxStatic}/images/bg2.jpg",
                "${ctxStatic}/images/bg3.jpg"
            ], {duration: 10000, fade: 2000});

            $("#loginForm").validate({
                rules: {
                    validateCode: {remote: "${pageContext.request.contextPath}/servlet/validateCodeServlet"}
                },
                messages: {
                    username: {required: "请填写用户名."},password: {required: "请填写密码."},
                    validateCode: {remote: "验证码不正确.", required: "请填写验证码."}
                },
                errorLabelContainer: "#messageBox",
                errorPlacement: function(error, element) {
                    error.appendTo($("#loginError").parent());
                }
            });
            $("#username").focus();

        });
        // 如果在框架中，则跳转刷新上级页面
        if(self.frameElement && self.frameElement.tagName=="IFRAME"){
            parent.location.reload();
        }
    </script>
</head>
<body>
<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <span class="alert alert-block" style="line-height: 30px;height: 30px;">
                <span>温馨提示：为保证更好的用户体验，建议您使用谷歌、火狐、或UC浏览器访问</span>
            </span>
            <a class="brand" href="${ctx}"><img src="${ctxStatic}/images/big-logo.jpg" alt="${fns:getConfig('productName')}" style="height:40px;"></a>
        </div>
    </div>
</div>

<div class="container">
    <!--[if lte IE 7]><br/><div class='alert alert-block' style="text-align:left;padding-bottom:0px;"><a class="close" data-dismiss="alert">x</a><h4>温馨提示：</h4><p>你使用的浏览器版本过低。为了获得更好的浏览体验，我们强烈建议您 <a href="http://browsehappy.com" target="_blank">升级</a> 到最新版本的IE浏览器，或者使用较新版本的 Chrome、Firefox、Safari 等。</p></div><![endif]-->
    <%String error = (String) request.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);%>
    <%String errorMsg = (String)request.getAttribute(FormAuthenticationFilter.DEFAULT_MESSAGE_PARAM);%>
    <%--<div id="messageBox" class="alert alert-error <%=error==null?"hide":""%>">--%>
        <%--<label id="loginError" class="error"><%=error==null?"":"com.thinkgem.jeesite.modules.sys.security.CaptchaException".equals(error)?"验证码错误, 请重试.":"用户或密码错误, 请重试." %></label>--%>
    <%--</div>--%>
    <div id="messageBox" class="alert alert-error <%=error==null?"hide":""%>">
        <label id="loginError" class="error"><%=errorMsg%></label>
    </div>
    <div id="login-wraper" style="background: rgba(0, 0, 0, 0.2);">
        <%--<sys:message content="${message}" />--%>
        <form id="loginForm"  class="form login-form" action="${ctx}/login" method="post">
            <div class="body" style="margin-bottom:5px;">
                <legend><span style="color:#08c;font-size:28px;">系统登录</span></legend>
            </div>
            <div class="body">
                <div class="control-group">
                    <div class="controls">
                        <input type="text" id="username" name="username" class="required" value="${username}"  autocomplete="off" placeholder="登录名">
                        <a style="visibility: hidden;">占位置的</a>
                    </div>
                </div>

                <div class="control-group">
                    <div class="controls">
                        <input type="password" id="password" name="password" class="required" placeholder="密码"/>
                        <a id="reSetPassWord" name="reSetPassWord" href="${ctx}/reSetPassWord" >忘记密码</a>
                    </div>

                </div>
                <c:if test="${isValidateCodeLogin}">
                    <div class="validateCode">
                        <label class="input-label mid" for="validateCode">验证码</label>
                        <sys:validateCode name="validateCode" inputCssStyle="margin-bottom:0;"/>
                    </div>
                </c:if>
            </div>
            <div class="footer">
                <label class="checkbox inline">
                    <input type="checkbox" id="rememberMe" name="rememberMe"> <span style="color:#08c;">记住我</span>
                </label>
                <input class="btn btn-primary" type="submit" value="登 录"/>
            </div>
            <div id="themeSwitch" class="dropdown pull-right">
                <a class="dropdown-toggle" data-toggle="dropdown" href="#">${fns:getDictLabelFromMS(cookie.theme.value,'theme','默认主题')}<b class="caret"></b></a><%-- 切换为微服务--%>
                <ul class="dropdown-menu">
                    <c:forEach items="${fns:getDictListFromMS('theme')}" var="dict"><%-- 切换为微服务--%><li><a href="#" onclick="location='${pageContext.request.contextPath}/theme/${dict.value}?url='+location.href">${dict.label}</a></li></c:forEach>
                </ul>
                <!--[if lte IE 6]><script type="text/javascript">$('#themeSwitch').hide();</script><![endif]-->
            </div>
        </form>
    </div>
</div>
<footer class="white navbar-fixed-bottom">
    Copyright &copy; 2012-<span id = "currentYear"></span> 广东快可立家电服务有限公司
    <a href="http://www.beian.miit.gov.cn">粤ICP备15007222号</a>
</footer>
</body>
</html>