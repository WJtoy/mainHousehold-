<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<head>
	<title>充值失败</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <meta name="decorator" content="default"/>
</head>
<script type="text/javascript">
	function closeme(){
		window.opener.repage();
		window.close();
	}
</script>
<body>
    <sys:message content="${message}"/>
    <div class="container" style="text-align: center;">
        <div class="alert alert-block" style="height:200px;margin-top:50px;margin-left: 100px;margin-right: 100px;">
            <h3><font color="#dd5600">充值失败</font></h3>
            <p class="alert-block">
                <strong>请检查网络是否通畅，或通过支付宝确认订单是否已支付；如已支付，支付宝将在10分钟之内通知并更新余额。</strong>
            </p>
        </div>
    </div>
	 <%--<div class="container">--%>
        <%--<div id="login-wraper" style="background: rgba(0, 0, 0, 0.2);">--%>
          <%----%>
            	<%--<div class="body" style="margin-bottom:5px;">--%>
                    <%--<legend><span style="color:#08c;font-size:28px;">充值失败!</span></legend>--%>
                <%--</div>--%>
   <%----%>
        <%--</div>--%>
    <%--</div>--%>
</body>
</html>