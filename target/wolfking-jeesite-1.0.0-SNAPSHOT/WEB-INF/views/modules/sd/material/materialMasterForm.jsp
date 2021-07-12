<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>配件申请-填写物流信息</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css"
	rel="stylesheet">
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
	<script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
		Order.rootUrl="${ctx}";
        var clickTag = 0;
        var this_index = top.layer.getFrameIndex(window.name);
        var $btnSubmit = $("#btnSubmit");
		//var this_index = top.layer.index;
		$(document).ready(function() {
            $("#btnSubmit").bind("click",function(){$("#inputForm").submit();});

			$("#inputForm").validate({
				submitHandler: function(form,event){
				    if (clickTag == 1){
				        return false;
					}
					clickTag = 1;
                    $btnSubmit.attr('disabled', 'disabled');
                    var confirmClickTag = 0;
                    top.layer.confirm('确定要保存配件申请信息么?', {
                        icon: 3
						,title:'系统确认'
						,cancel: function(index, layero){
                            $btnSubmit.removeAttr('disabled');
                            clickTag = 0;
                        }
                    }, function(index,layero){
                        if(confirmClickTag == 1){
                            return false;
                        }
                        var btn0 = $(".layui-layer-btn0",layero);
                        if(btn0.hasClass("layui-btn-disabled")){
                            return false;
                        }
                        confirmClickTag = 1;
                        btn0.addClass("layui-btn-disabled").attr("disabled","disabled");
                        top.layer.close(index);
                        var ajaxSuccess = 0;
                        $.ajax({
                            async: false,
                            cache: false,
                            type: "POST",
                            url: "${ctx}/sd/material/saveExpress",
                            data:$(form).serialize(),
                            beforeSend: function () {
                            },
                            complete: function () {
                                if(ajaxSuccess == 0) {
                                    setTimeout(function () {
                                        clickTag = 0;
                                        $btnSubmit.removeAttr('disabled');
                                    }, 2000);
                                }
                            },
                            success: function (data) {
                                if(ajaxLogout(data)){
                                    return false;
                                }
                                if(data && data.success == true){
                                    ajaxSuccess = 1;
                                    //刷新父窗口
                                    var parentIndex = getCookie('layer.parent.id');
                                    // console.log("parent:" + parentIndex);
                                    if(parentIndex && parentIndex != 0) {
                                        var layero = $("#layui-layer" + parentIndex, top.document);
                                        var iframeCtl = layero.find('iframe');
                                        if (iframeCtl && iframeCtl.length > 0) {
                                            var iframeWin = top[iframeCtl[0]['name']];
                                            if (typeof iframeWin.reloadApproveList === "function" && iframeWin.reloadApproveList) {
                                                iframeWin.reloadApproveList();
                                            } else {
                                                iframeWin.location.reloadApproveList();
                                            }
                                            var this_index = parent.layer.getFrameIndex(window.name);
                                            parent.layer.close(this_index);//ie下报错
                                        } else {
                                            layerMsg('保存成功,请手动关闭此页面!');
                                        }
                                    }else{
                                        layerMsg('保存成功,请手动关闭此页面!');
                                    }
                                    // var layero = $("#layui-layer"+parentIndex,top.document);
                                    // var iframeWin = top[layero.find('iframe')[0]['name']];
                                    // iframeWin.reload();
                                    // var this_index = parent.layer.getFrameIndex(window.name);
                                    // parent.layer.close(this_index);
                                }
                                else if( data && data.message){
                                    layerError(data.message,"错误提示");
                                }
                                else{
                                    layerError("修改配件申请失败","错误提示");
                                }
                                return false;
                            },
                            error: function (e) {
                                ajaxLogout(e.responseText,null,"修改配件申请失败，请重试!");
                            }
                        });
                    },function(index) {//cancel
                        $btnSubmit.removeAttr('disabled');
                        clickTag = 0;
                    });
                    return false;
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

			$("[id='expressCompany.value']").on('change', function () {
				var companyName = $("[id='expressCompany.value'] option:selected").text();
				$("input[id='expressCompany.label']").val(companyName);
			});
		});
        function closeme() {
            top.layer.close(this_index);
        }
	</script>
	<style type="text/css">
	  body {background-color:#fff;}
	  html {color:#000;}
		#inputForm {margin-top:6px;}
	  .form-horizontal .control-label {width:140px}
	  .form-horizontal .controls {margin-left:100px}
	  .form-horizontal .control-group{border-bottom: none;margin-bottom: 8px;}
	  legend span {
		  border-bottom: #0096DA 4px solid;
		  padding-bottom: 6px;}
	  .table thead th,.table tbody td {
		  text-align: center;
		  vertical-align: middle;
		  BackColor: Transparent;
		  height: 20px;
	  }
	  .praise_status_1{background-color: #0096DA;}
	  .praise_status_2{background-color: #FF9502;}
	  .praise_status_3{background-color: #10B9B9;}
	  .praise_status_4{background-color: #34C758;}
	  .praise_status_5{background-color: #f54142;}
	  .praise_status_6{background-color: #f54142;}
	</style>
</head>
<body style="display:inline">
	<sys:message content="${message}" />
	<c:if test="${canAction}">
	<fieldset style="width: 90%;margin-left: 50px">
	<form:form id="inputForm" modelAttribute="materialMaster" action="${ctx}/sd/material/saveExpress" method="post" class="form-horizontal">
		<form:hidden path="id" />
		<form:hidden path="orderId" />
		<form:hidden path="quarter" />
		<form:hidden path="product.id" />
		<div class="row-fluid" style="margin-top: 12px">
			<div class="span12">
				<div class="control-group">
					<div class="controls">
							<%--<span style="margin-left: 570px">${praise.praiseNo}</span>&nbsp;&nbsp;<span class="praise_status_${praise.status}">${praise.strStatus}</span>--%>
						<div style="float: right">
							<span>${materialMaster.masterNo}</span>
							&nbsp;&nbsp;<span class="label praise_status_${materialMaster.status.value}">${materialMaster.status.label}</span>
						</div>
					</div>
				</div>
			</div>
		</div>
		<legend></legend>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">申请类型：</label>
					<div class="controls">
						<input id="applyType" name="applyType.value"  htmlEscape="false" maxlength="100"
							   value="${fns:getDictLabelFromMS(materialMaster.applyType.value,'material_apply_type','未知')}" class="required uneditable-input" disabled="disabled"/>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
				<%--<label class="control-label">状态:</label>
					<div class="controls">
						<input id="status" name="status"  htmlEscape="false" maxlength="100"
							   value="${materialMaster.status.label}" class="required uneditable-input" disabled="disabled"/>
					</div>
				</div>--%>
				<label class="control-label">申请时间：</label>
				<div class="controls">
					<input htmlEscape="false" maxlength="100"
						   value="<fmt:formatDate value="${materialMaster.createDate}" pattern="yyyy-MM-dd HH:mm"/>" class="required uneditable-input" disabled="disabled"/>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">快递公司：</label>
					<div class="controls">
						<form:select path="expressCompany.value" cssClass="input-medium required" cssStyle="width: 220px">
							<form:option value="" label="请选择" />
							<form:options items="${fns:getDictListFromMS('express_type')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%-- 切换为微服务 --%>
						</form:select>
						<input type="hidden" id="expressCompany.label" name="expressCompany.label" />
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">快递单号：</label>
					<div class="controls">
						<form:input path="expressNo" cssClass="required" htmlEscape="false" maxlength="50" />
					</div>
				</div>
			</div>
		</div>
		<legend><span>配件清单</span></legend>
		<div>
			<table id="treeTable" width="100%"
			class="table table-bordered" style="margin-bottom: 0px;table-layout: fixed;">
				<thead>
					<tr style="background: #F8F8F9" height="40px">
						<th width="40px">序号</th>
						<th>产品</th>
						<th>配件</th>
						<th>价格</th>
						<th>数量（元）</th>
						<th>小计（元）</th>
					<tr>
				</thead>
				<tbody>
					<c:set var="idx" value="0"/>
					<c:forEach items="${materialMaster.mateirals}" var="entry" varStatus="g" begin="0">
						<c:forEach items="${entry.value}" var="item" varStatus="i" begin="0">
							<c:set var="idx" value="${idx+1}" />
							<tr>
								<td>${idx}</td>
								<c:if test="${i.index == 0}">
									<td rowspan="${fn:length(entry.value)}">${entry.key.name}</td>
								</c:if>
								<td>${item.material.name}</td>
								<td>${item.price}<c:if test="${item.recycleFlag ==1}"><br/>回收：${item.recyclePrice}</c:if></td>
								<td>${item.qty}</td>
								<td>${item.totalPrice+item.totalRecyclePrice}</td>
							</tr>
						</c:forEach>
					</c:forEach>
					       <tr>
							   <td colspan="5" style="text-align: right">合计（元）</td>
							   <td>${materialMaster.totalPrice}</td>
						   </tr>
				</tbody>
			</table>
		</div>
	</form:form>
	</fieldset>
	</c:if>
	<div style="height: 60px;width: 100%"></div>
	<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
		<hr style="margin: 0px;"/>
		<div style="float: right;margin-top: 10px;margin-right: 35px">
			<c:if test="${canAction}">
			  <input id="btnSubmit" class="btn btn-primary" type="button" style="margin-right: 5px;width: 96px;height: 40px" value="保 存" />
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="取 消" style="width: 96px;height: 40px;margin-right: 5px" onclick="closeme()"/>
		</div>
	</div>
</body>
</html>