<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<%@include file="/WEB-INF/views/include/dialog.jsp" %>
<!DOCTYPE html>
<head>
	<title>充值成功</title>
	<meta name="decorator" content="default"/>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
<script type="text/javascript">
	$(document).ready(function() {
		//updateAlarm();
		setTimeout("closeme()",3000);//3秒后调用tips_pop()这个函数
		
	});


	function closeme(){
	
	}	
</script>
</head>
<body>
<form:form id="inputForm" modelAttribute="customerCurrency"  class="form-horizontal">
	<div class="container" style="text-align: center;">
		<div class="hero-unit" style="height:200px;margin-top:50px;margin-left: 100px;margin-right: 100px;">
			<h3>充值成功</h3>
			<p class="alert-block"><strong>感谢您选择快可立在线自动充值,您的充值已成功,充值金额为:${customerCurrency.amount}元,当前账户可下单金额为:${customerCurrency.balance}元.</strong></p>
			<p></p>
		</div>
	</div>
    </form:form>
</body>
</html>