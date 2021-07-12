<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
<title>客服完工(for云米)</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default" />
	<%@include file="/WEB-INF/views/include/treetable.jsp"%>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
<script type="text/javascript">
    Order.rootUrl = "${ctx}";
    <%String parentIndex = request.getParameter("parentIndex");%>
    var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
    var this_index = top.layer.index;
	var clickTag = 0;
    $(document).off('click','#btnSave');//先解除事件绑定
	$(document).on("click", "#btnSave", function() {
		if (clickTag === 1) {
			return false;
		}
		var $btnSubmit = $("#btnSave");
        if ($btnSubmit.prop("disabled") == true) {
            return false;
        }

		$btnSubmit.prop("disabled", true);
		clickTag = 1;
		var completeModel = {};
		var orderId = $("#orderId").val();
		completeModel['orderId'] = orderId;
		completeModel['orderNo'] = $("#orderNo").val();
		completeModel['quarter'] = $("#quarter").val();
		completeModel['remarks'] = $("#remarks").val();
		var buyDateStr= $("#buyDate").val();
		if(buyDateStr === "" ) {
			layer.msg('未设置购买时间。', {
				time: 0,
				icon: 5
				,btn: ['关闭']
				,btnAlign: 'c'
				,yes: function(index){
					layer.close(index);
				}
			});
			clickTag = 0;
			$btnSubmit.removeAttr('disabled');
			return false;
		}
		completeModel['buyDate'] = buyDateStr;
        var loadingIndex;
		$.ajax({
			cache : false,
			type : "POST",
			url : "${ctx}/sd/order/saveCompleteForViomi",
			data : completeModel,
            dataType:'json',
			beforeSend: function () {
				loadingIndex = top.layer.msg('正在提交，请稍等...', {
					icon: 16,
					time: 0,
					shade: 0.3
				});
			},
			complete: function () {
				//console.log("" + new Dte().getTime() + " [complete] clickTag:" + clickTag);
				if(loadingIndex) {
					top.layer.close(loadingIndex);
				}
				setTimeout(function () {
					clickTag = 0;
					$btnSubmit.removeAttr('disabled');
				}, 3000);
			},
			success : function(data) {
			    top.layer.close(loadingIndex);
                if(ajaxLogout(data)){
                    return false;
                }
				if (data.success) {
                    layerMsg('完工成功');
                    // 刷新订单详情页面
                    if (parentIndex != '') {
						//订单详情页调用
						var layero = $("#layui-layer" + parentIndex, top.document);
						var iframeWin = top[layero.find('iframe')[0]['name']];
						iframeWin.successKefuComplete(orderId);
                    }
                    top.layer.close(this_index);
                } else {
                    layerError(data.message,"错误提示");
				}
				$btnSubmit.removeAttr('disabled');
                return false;
			},
			error : function(xhr, ajaxOptions, thrownError) {
                ajaxLogout(e.responseText,null,"完工失败，请重试!");
				$btnSubmit.removeAttr('disabled');
				clickTag = 0;
			}
		});

		return false;
	});

	function closeme(){
	    top.layer.close(this_index);
	}
</script>
</head>
<body>
	<form:form id="searchForm" modelAttribute="completeModel" method="post" class="form-horizontal">
		<form:hidden path="orderId"/>
		<form:hidden path="orderNo"/>
		<form:hidden path="quarter"/>
		<sys:message content="${message}"/>
		<div class="control-group" style="margin-top: 20px;">
			<label class="control-label" style="width: 120px;">购买日期:</label>
			<div class="controls" style="margin-left:130px;">
				<input id="buyDate" name="buyDate" class="Wdate" type="text" readonly="readonly"
					   value="<fmt:formatDate value='${completeModel.buyDate}' pattern='yyyy-MM-dd HH:mm:ss' />"
					   onClick="WdatePicker({startDate: '%y-%M-%d %H:%m:%s' ,dateFmt:'yyyy-MM-dd HH:mm:ss',maxDate:'2050-12-31',isShowClear:true})"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" style="width: 120px;">完工说明:</label>
			<div class="controls" style="margin-left:130px;">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="150" style="width:90%" />
			</div>
		</div>
		<div class="form-actions" style="text-align: center;padding-left:0px;">
			<c:if test="${canAction}">
			<input id="btnSave" class="btn btn-primary" type="button" value="保 存" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closeme()" />
		</div>
	</form:form>
</body>
</html>