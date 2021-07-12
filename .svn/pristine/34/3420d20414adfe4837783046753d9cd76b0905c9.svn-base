<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>产品分类时效奖励价格</title>
	<meta name="decorator" content="default" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@include file="/WEB-INF/views/include/treetable.jsp"%>
	<script type="text/javascript">
        var this_index = top.layer.index;
        // 关闭页面
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }

        var clickTag=0;

        $(document).ready(
		function()
		{
            $(document).ready(function() {
                $("#inputForm").validate({
                    submitHandler: function(form){
                        var loadingIndex = layerLoading('正在提交，请稍候...');
                        var $btnSubmit = $("#btnSubmit");
                        if ($btnSubmit.prop("disabled") == true) {
                            event.preventDefault();
                            return false;
                        }
						var categoryId = $("#category\\.id").val();
						if (categoryId == undefined || categoryId == 0) {
							layerInfo("请选择产品品类!", "信息提示");
							return false;
						}
                        $btnSubmit.prop("disabled", true);


                        $.ajax({
                            url:"${ctx}/md/timelinessPriceNew/saveTimelinessPricesNew",
                            type:"POST",
                            data:$(form).serialize(),
                            dataType:"json",
                            success: function(data){
                                //提交后的回调函数
                                if(loadingIndex) {
                                    top.layer.close(loadingIndex);
                                }
                                if(ajaxLogout(data)){
                                    setTimeout(function () {
                                        clickTag = 0;
                                        $btnSubmit.removeAttr('disabled');
                                    }, 2000);
                                    return false;
                                }
                                if (data.success) {
                                    layerMsg("保存成功");
									var pframe = getActiveTabIframe();//定义在jeesite.min.js中
									if(pframe){
										pframe.repage();
									}
                                    top.layer.close(this_index);//关闭本身
                                }else{
                                    setTimeout(function () {
                                        clickTag = 0;
                                        $btnSubmit.removeAttr('disabled');
                                    }, 2000);
                                    layerError(data.message, "错误提示");
                                }
                                return false;
                            },
                            error: function (data)
                            {
                                if(loadingIndex) {
                                    layer.close(loadingIndex);
                                }
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                ajaxLogout(data,null,"数据保存错误，请重试!");
                                //var msg = eval(data);
                            },
                            timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                        });
                    },
                    errorContainer: "#messageBox",
                    errorPlacement: function(error, element) {
                        $("#messageBox").text("输入有误，请先更正。");
                        if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
                            error.appendTo(element.parent().parent());
                        } else {
                            error.insertAfter(element);
                        }
                    }
                });

            });
        });

    </script>

</head>

<body>

	<form:form id="inputForm" modelAttribute="timeLinessPrices" action="${ctx}/md/timelinessPriceNew/saveTimelinessPricesNew" method="post" class="form-horizontal">
		<sys:message content="${message}" />
		<c:if test="${canAction == true}">
		<div class="control-group" style="margin-top: 48px;margin-left: 20px">
			<label class="control-label">产品品类：</label>
			<div class="controls">
				<c:choose>
					<c:when test="${timeLinessPrices.category != null and timeLinessPrices.category.id != null}">
						<form:hidden path="category.id" readonly="true"></form:hidden>
						<form:input path="category.name" readonly="true" style="width: 230px;"></form:input>
					</c:when>
					<c:otherwise>
                        <select id="category.id" name="category.id" class="input-small" style="width:245px;">
                            <option value="0" <c:out value="${(empty timeLinessPrices.category.id)?'selected=selected':''}" />>所有</option>
                            <c:forEach items="${fns:getProductCategories()}" var="dict">
                                <option value="${dict.id}" <c:out value="${(timeLinessPrices.category.id eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                            </c:forEach>
                        </select>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<c:forEach var="entity" items="${timeLinessPrices.list}" varStatus="i" begin="0">
			<div class="control-group" style="margin-top: 16px;margin-left: 20px">
				<label class="control-label">${entity.timeLinessLevel.description}:</label>
				<div class="controls">
					<form:hidden path="list[${i.index}].timeLinessLevel.value" htmlEscape="false"/>
					<form:input path="list[${i.index}].amount" htmlEscape="false" maxlength="10"  class="required number" style="border-radius: 4px 0 0 4px;"/>
					<span class="add-on" style="margin-left: -5px;">元</span>
				</div>
			</div>
		</c:forEach>
		</c:if>

		<hr style="=border: 1px solid rgba(238, 238, 238, 1);margin: 0px;margin-top: 48px"/>

			<c:if test="${canAction == true}">
			<shiro:hasPermission name="md:timelinessprice:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit" style="width: 92px;height: 40px;margin-left: 66%;margin-top: 10px"
					value="保存" /></shiro:hasPermission>
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="取消" style="width: 92px;height: 40px;margin-top: 10px;margin-left: 10px"
				   onclick="cancel()" />
	</form:form>
</body>
</html>
