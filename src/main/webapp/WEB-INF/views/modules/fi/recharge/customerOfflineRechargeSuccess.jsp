<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<head>
<title>充值成功</title>
<meta name="decorator" content="default" />
<%@ include file="/WEB-INF/views/include/head.jsp" %>
<script type="text/javascript">

</script>
	<style type="text/css">
		body{ text-align:center}
		.divcss5{margin:0 auto;padding:10px 10px;width:1200px;background-color: white}
		.form-horizontal .form-actions {padding-left: 0px !important;}
	</style>
</head>
<body style="background-color: #F8F8F9">
	<br />
	<div class="divcss5">
		<div style="width: 800px;margin-left: 400px;margin-top: 30px;text-align: left;padding-bottom: 30px">
				<div style="font-size: 20px;font-family: SourceHanSansSC-regular;"><img src="${ctxStatic}/images/rechargeSuccess.png" style="width: 40px">充值成功</div>
				<div style="margin-top: 20px">账号名称：${customerName}</div>
				<div style="margin-top: 10px">充值金额：<span style="color: red">${amount}元</span></div>
				<div style="margin-top: 10px">充值成功后平台财务审核通过后将充值进您的账户。如果充值金额与实际到账金额<br>不一致，将以实际到账金额为准！</div>
		</div>
	</div>
</body>
</html>