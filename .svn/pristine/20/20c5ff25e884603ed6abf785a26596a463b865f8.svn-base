<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
<title>客评</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default" />
	<%@include file="/WEB-INF/views/include/treetable.jsp"%>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
<script type="text/javascript">
    Order.rootUrl = "${ctx}";
    <%String parentIndex = request.getParameter("parentIndex");%>
    var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
    var this_index = top.layer.index;
    $(document).off('click','#btnSave');//先接触事件绑定
	$(document).on("click", "#btnSave", function() {
        if ($("#btnSave").prop("disabled") == true)
        {
            return false;
        }

	    $("#btnSave").prop("disabled", true);

		var engineerId = $("[id='engineer.id']").val();
		if(Utils.isEmpty(engineerId)){
			top.$.jBox.error('没有安维人员，不能客评', '安维评价');
            $('#btnSave').removeAttr('disabled');
			return;
		}

		var gradeModel = {};
        $("input:radio:checked").each(function(i){
            //if($(this).prop("disabled") == false) {
                gradeModel['gradeList[' + i + '].gradeId'] = $(this).data("grade");
                gradeModel['gradeList[' + i + '].gradeName'] = $(this).data("gradename");
                gradeModel['gradeList[' + i + '].gradeItemId'] = $(this).val();
                gradeModel['gradeList[' + i + '].gradeItemName'] = $(this).data("itemname");
                gradeModel['gradeList[' + i + '].point'] = $(this).data("point");
                gradeModel['gradeList[' + i + '].sort'] = $(this).data("sort");
                gradeModel['gradeList[' + i + '].dictType'] = $(this).data("dicttype");
                gradeModel['gradeList[' + i + '].dictValue'] = $(this).data("dictvalue");
            //}
        });
		gradeModel['orderId'] = $("#orderId").val();
        gradeModel['engineer.id'] = engineerId;
        gradeModel['engineer.name'] = $("[id='engineer.name']").val();
        gradeModel['quarter'] = $("#quarter").val();

        var loadingIndex = layerLoading("正在保存客评...");
		$.ajax({
			cache : false,
			type : "POST",
			url : "${ctx}/sd/order/savegrade",
			data : gradeModel,
            dataType:'json',
			success : function(data) {
			    top.layer.close(loadingIndex);
                if(ajaxLogout(data)){
                    return false;
                }
				if (data.success) {
                    layerMsg('客评成功');
                    // 刷新订单详情页面
                    if (parentIndex != '') {
                        top.layer.close(parentIndex);
                        var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                        if(iframe != undefined){
                            iframe.repage();
                        }
                    }
                    top.layer.close(this_index);
                } else {
                    layerError(data.message,"错误提示");
				}
				$('#btnSave').removeAttr('disabled');
                return false;
			},
			error : function(xhr, ajaxOptions, thrownError) {
                ajaxLogout(e.responseText,null,"客评失败，请重试!");
				$('#btnSave').removeAttr('disabled');
			}
		});

		return false;
	});

	function closeme(){
	    top.layer.close(this_index);
	}
	/*
	$(document).on("change", "#chkNograde", function() {
        if ($(this).prop("checked") == true) {
            $("input:radio").each(function () {
                $(this).prop("disabled", true);
            });
        } else {
            $("input:radio").each(function () {
                $(this).removeAttr("disabled");
            });
        }
    }); */
</script>
</head>
<body>
	<%--<sys:message content="${message}" />--%>
	<c:if test="${not empty message}">
		<div class="row-fluid" style="margin-top: 24px">
			<div class="span12">
				<div class="control-group">
					<div class="controls" style="margin-left: 20px;">
						<div class="input-block-level" style=" border:1px solid #FF9502;border-radius:3px;background-color: #FFF7EC;width: 98%;padding: 5px">
							<i class="icon-question-sign" style="color: #FF9502;margin-left: 20px"></i>
							<span style="color: #FF9502;">${message}</span>
						</div>
					</div>
				</div>
			</div>
		</div>
	</c:if>
	<c:if test="${canAction}">
	<form:form id="searchForm" modelAttribute="orderGrade" method="post" class="breadcrumb form-search">
		<form:hidden path="engineer.id"/>
		<form:hidden path="orderId"/>
		<form:hidden path="quarter"/>
		<form:hidden path="servicePoint.id"/>
		<label style="margin-top: 5px;">订单编号 ：</label>
		<input type="text" id="orderNo" name="orderNo" maxlength="50" readonly="readonly" class="input-small" value="${orderGrade.orderNo }" />
		&nbsp;<label>安维人员 ：</label>
		<form:input path="engineer.name" readonly="true" htmlEscape="false" maxlength="50" class="input-small" />
		&nbsp;
		<c:if test="${voiceResult != null}">
			<c:choose>
				<c:when test="${voiceResult eq 0}"><span class="alert alert-info">正在智能回访中</span></c:when>
				<c:when test="${voiceResult eq 1}"><a class="btn btn-primary" data-toggle="tooltip" data-tooltip="点击查看智能回访详情" href="javascript:;" onclick="Order.voiceService('${orderGrade.quarter}','${orderGrade.orderId}');">智能回访成功</a></c:when>
				<c:when test="${voiceResult eq 2}"><a class="btn btn-danger" data-toggle="tooltip" data-tooltip="点击查看智能回访详情" href="javascript:;" onclick="Order.voiceService('${orderGrade.quarter}','${orderGrade.orderId}');">智能回访失败</a></c:when>
                <c:otherwise></c:otherwise>
			</c:choose>
		</c:if>
		<%--<input type="checkbox"  id="chkNograde" name="chkNograde" /><label>不予评分</label>--%>
	</form:form>
	<div style="margin:15px 10px 0px 10px;">
	<table  class="table table-striped table-bordered table-condensed">
		<tbody>
			<c:forEach items="${orderGrade.gradeList}" var="grade">
				<tr>
					<td>${grade.gradeName}</td>
					<td>${grade.remarks}</td>
				</tr>
				<tr>
					<td colspan="2">
					<c:forEach items="${grade.items}" var="item">
						<input type="radio" id="${grade.id}_${item.id}" name="${grade.id}" value="${item.id}" data-dicttype="${grade.dictType}" data-dictvalue="${item.dictValue}" data-sort="${grade.sort}" data-point="${item.point}" data-grade="${grade.id}" data-gradename="${grade.gradeName}" data-itemname="${item.remarks}" <c:choose><c:when test="${item.id eq grade.gradeItemId }">checked='checked' </c:when><c:when test="${canSave ne true}">disabled='disabled' </c:when><c:otherwise></c:otherwise></c:choose>	/>
						<label for="${grade.id}_${item.id}">${item.remarks}</label>&nbsp;&nbsp;
					</c:forEach>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	</div>
	</c:if>
	<div class="form-actions" style="text-align: center;">
		<c:if test="${canAction}">
		<input id="btnSave" class="btn btn-primary" type="button" value="保 存" ${canSave eq true? '':'disabled'} />
		</c:if>
		<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closeme()" />
        &nbsp;&nbsp;
        <c:if test="${manuCloseMaterialForm != null && manuCloseMaterialForm == 1}">
            <a class="btn btn-warning" data-toggle="tooltip" data-tooltip="点击处理配件" href="javascript:;" onclick="Order.attachlist('${orderGrade.orderId}','${orderGrade.orderNo}','${orderGrade.quarter}',this_index);">配件单</a>
        </c:if>
	</div>
</body>
</html>