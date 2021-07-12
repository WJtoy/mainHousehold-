<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>突击单</title>
	<meta name="description" content="新开或修改投诉单">
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/modules/sd/crush/tpl/viewCrushServicePointList.html" %>
	<script type="text/javascript">
        //<%String parentIndex = request.getParameter("parentIndex");%>
		var this_index = top.layer.index;
		$(document).ready(function() {
            $('a[data-toggle=tooltip]').darkTooltip();
            var screen = getOpenDialogWidthAndHeight();
            var height = screen.height-43-60;
            $("#leftDiv").css("height",height+"px");
            $("#rightDiv").css("height",height+"px");
            var items = ${fns:toJson(orderCrush.servicePoints)};
            var model = {
                items: items
            };
            loadServicePoint(model)

		});

        function loadServicePoint(model) {
            if(model && model.items && model.items.length>0){
                var tmpl = document.getElementById('tpl-viewCrushServicePoint').innerHTML;
                var doTtmpl = doT.template(tmpl);
                var html = doTtmpl(model);
                $("#tabItems").html(html);
                $('a[data-toggle=tooltip]',"#tabItems").darkTooltip();
            }else{
                var tmpl = document.getElementById('tpl-viewCrushServicePoint').innerHTML;
                var doTtmpl = doT.template(tmpl);
                model = {
                };
                var html = doTtmpl(model);
                $("#tabItems").html(html);
            }
        }

        function findCrushServicePointList(degree) {
            $("#navtabs").find("li").removeClass('active');
            $("#litab"+degree).addClass('active');
            var orderId = $("#orderId").val();
            var quarter = $("#quarter").val();
            var areaId = $("[id='area.id']").val();
            var loadingIndex = layerLoading('正在查询，请稍等...');
            $.ajax({
                type: "POST",
                url: "${ctx}/sd/order/crush/findCrushServicePointList",
                data:{orderId:orderId,quarter:quarter,areaId:areaId,degree:degree},
                success: function (data) {
                    top.layer.close(loadingIndex);
                    $('#btnTempSave').removeAttr('disabled');
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if(data && data.success == true) {
                        /*layerMsg('暂存成功');
                        top.layer.close(this_index);*/
                        var model = {
                            routeAddre:$("#routeAddre").val(),
                            items: data.data
                        };
                        loadServicePoint(model);
                    }
                    else if( data && data.success == false){
                        layerError(data.message,"错误提示");
                    }
                    else {
                        layerError("读取网点错误错误", "错误提示");
                    }
                    return false;
                },
                error: function (e) {
                    top.layer.close(loadingIndex);
                    //$('#btnTempSave').removeAttr('disabled');
                    ajaxLogout(e.responseText,null,"保存错误，请重试!");
                }
            });
        }

		function closeme(){
			top.layer.close(this_index);
		};
	</script>
	<style type="text/css">
		legend span {
			border-bottom: #0096DA 4px solid;
			padding-bottom: 6px;}
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label {width: 100px;}
		.form-horizontal .controls {margin-left: 120px;}
		#contentTable td,#contentTable th {text-align: center; vertical-align: middle;}
		.form-actions {margin-top: 0px;margin-bottom: 0px;padding: 8px 20px 8px;}
		.fromInput {
			border:1px solid #ccc;padding:3px 6px;color:#555;border-radius:4px;
		}
	</style>
</head>
<body>
	<form:form id="inputForm" modelAttribute="orderCrush" action="${ctx}/sd/assault/save" method="post" class="form-horizontal" cssStyle="margin-top: 20px">
		<form:hidden path="id"/>
		<form:hidden path="action"/>
		<form:hidden path="quarter"/>
		<form:hidden path="crushNo"/>
		<form:hidden path="status"/>
		<form:hidden path="orderId"/>
		<form:hidden path="area.id"/>
		<sys:message content="${message}"/>
		<div style="width: 35%;border-right: solid 1px #EEEEEE;float: left" id="leftDiv">
			<div style="width: 90%;height: auto;background-color: #F6F6F6;margin-top: 24px">
				<div class="row-fluid">
					<div class="span12" style="margin-top: 16px">
						<label class="control-label" style="padding-top: 0px">工单单号：</label>
						<div class="controls">
							<a href="javascript:void(0);" onclick="Order.showKefuOrderDetail('${orderCrush.orderId}','${orderCrush.quarter}',1);">${orderCrush.orderNo}</a>
						</div>
					</div>
				</div>
				<div class="row-fluid">
					<div class="span12">
						<label class="control-label" style="padding-top: 0px">突击单号：</label>
						<div class="controls">
							<p>${orderCrush.crushNo}</p>
						</div>
					</div>
				</div>
			</div>
			<legend style="margin-top: 15px;width: 90%"><span>发起</span></legend>
			<div style="width: 90%;height: auto;background-color: #F6F6F6">
				<div class="row-fluid">
					<div class="span12" style="margin-top: 16px">
						<label class="control-label" style="padding-top: 0px">发起人：</label>
						<div class="controls">
							<p>${orderCrush.createBy.name}</p>
						</div>
					</div>
				</div>
				<div class="row-fluid">
					<div class="span12">
						<label class="control-label" style="padding-top: 0px">发起时间：</label>
						<div class="controls">
							<p>${fns:formatDate(orderCrush.createDate,'yyyy-MM-dd HH:mm')}</p>
						</div>
					</div>
				</div>
				<div class="row-fluid" style="padding-bottom: 10px">
					<div class="span12">
						<label class="control-label" style="padding-top: 0px">发起内容：</label>
						<div class="controls">
							<p>${orderCrush.createRemark}</p>
						</div>
					</div>
				</div>
			</div>
			<legend style="margin-top: 15px;width: 90%"><span>处理</span></legend>
			<div style="width: 90%;height: auto;background-color: #F6F6F6">
				<div class="row-fluid">
					<div class="span12" style="margin-top: 16px">
						<label class="control-label" style="padding-top: 0px">处理人：</label>
						<div class="controls">
							<p>${orderCrush.closeBy.name}</p>
						</div>
					</div>
				</div>
				<div class="row-fluid">
					<div class="span12">
						<label class="control-label" style="padding-top: 0px">处理时间：</label>
						<div class="controls">
							<p>${fns:formatDate(orderCrush.closeDate,'yyyy-MM-dd HH:mm')}</p>
						</div>
					</div>
				</div>
				<div class="row-fluid" style="padding-bottom: 10px">
					<div class="span12">
						<label class="control-label" style="padding-top: 0px">处理内容：</label>
						<div class="controls">
							<p>${orderCrush.closeRemark}</p>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div style="float: right;width: 60%;margin-right: 20px;overflow-x:auto;" id="rightDiv">
			<ul id="navtabs" class="nav nav-tabs">
				<c:forEach items="${fns:getDictListFromMS('degreeType')}" var="dict">
					<c:choose>
						<c:when test="${dict.value == degreeType}">
							<li class="active" id="litab${dict.value}"><a href="javascript:findCrushServicePointList('${dict.value}');" style="width: 70px;text-align: center" title="${dict.label}">${dict.label}</a></li>
						</c:when>
						<c:otherwise>
							<li id="litab${dict.value}"><a href="javascript:findCrushServicePointList(${dict.value})" style="width: 70px;text-align: center" title="${dict.label}">${dict.label}</a></li>
						</c:otherwise>
					</c:choose>
				</c:forEach>
			</ul>
			<div class="tab-pane active" id="tabItems" style="margin-top: 24px;"></div>
		</div>
	</form:form>
	<div style="height: 60px;width: 100%"></div>
	<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
		<hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
		<div style="float: right;margin-top: 10px;margin-right: 20px">
			<button id="btnCancel" class="btn" type="button" value="关 闭"  onclick="closeme();" style="width: 96px;height: 40px">
				关 闭
			</button>
		</div>
	</div>
	<object id="plugin0" type="application/x-nyteleactivex" width="0" height="0">
		<param name="onload" value="pluginLoaded"/>
		<param name="install-url" value="${ctxPlugin}/npNYTeleActiveX.dll"/>
	</object>
</body>
</html>