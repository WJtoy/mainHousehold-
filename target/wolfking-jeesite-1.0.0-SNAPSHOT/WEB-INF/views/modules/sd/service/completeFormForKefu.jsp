<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
<title>完成服务(for客服|网点)</title>
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
		var option =$('input:radio[name="completeType"]:checked');
		if(option.val() ==="" ){
			layer.msg('请选择完工类型。', {
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
		var remarks = $("#remarks").val();
		if(option.val() != "compeled_all" && remarks.trim().length == 0){
			layer.msg('请填写完工说明。', {
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
		var completeModel = {};
		var orderId = $("#orderId").val();
		completeModel['orderId'] = orderId;
		completeModel['orderNo'] = $("#orderNo").val();
		completeModel['quarter'] = $("#quarter").val();
		completeModel['remarks'] = remarks;
		completeModel["completeType.label"] = option.data("label");
		completeModel["completeType.value"] = option.val();
        var loadingIndex;
		$.ajax({
			cache : false,
			type : "POST",
			url : "${ctx}/sd/order/saveCompleteForKefu",
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
		<c:if test="${!empty canAction and canAction eq true}">
			<div class="control-group" style="margin-top:30px;">
				<label class="control-label">完工类型:</label>
				<div class="controls btn-group-vertical" style="margin-left:0px;">
					<c:set var="completeTypes" value="${fns:getDictListFromMS('completed_type')}" />
					<c:forEach items="${completeTypes}" var="dict">
						<label class="radio" style="display: block;">
							<input type="radio" name="completeType" style="margin:4px 6px 0px 0px !important;" id="completeType_${dict.value}" value="${dict.value}" data-label="${dict.label}" class="required">
								${dict.label}
						</label>
					</c:forEach>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">完工说明:</label>
				<div class="controls">
					<form:textarea path="remarks" htmlEscape="false" rows="4" maxlength="150" class="input-block-level"/>
				</div>
			</div>
		</c:if>
<%--		<div class="form-actions" style="text-align: center;padding-left:0px;">--%>
<%--			<c:if test="${canAction}">--%>
<%--			<input id="btnSave" class="btn btn-primary" type="button" value="保 存" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;--%>
<%--			</c:if>--%>
<%--			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closeme()" />--%>
<%--		</div>--%>
	</form:form>
    <div id="editBtn">
        <c:if test="${canAction}">
            <input id="btnSave" class="btn btn-primary" type="button" value="保 存" style="margin-top: 10px;width: 85px;height: 35px;" />&nbsp;
        </c:if>
        <input id="btnCancel" class="btn" type="button" value="取 消" onclick="closeme()" style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
    </div>
</body>
<style type="text/css">
    #editBtn {
        position: fixed;
        left: 0px;
        bottom: 3px;
        width: 100%;
        height: 55px;
        background: #fff;
        z-index: 10;
        border-top: 1px solid #ccc;
        border-top: 1px solid #e5e5e5;
        text-align: right;
    }
    .form-horizontal .control-label{width:100px}
    .form-horizontal .controls{margin-left:120px}
</style>
</html>