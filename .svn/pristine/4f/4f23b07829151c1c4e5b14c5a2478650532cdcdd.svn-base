<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>投诉申请单</title>
	<meta name="description" content="新开或修改投诉单">
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<!-- 文件上传 -->
	<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/SalesOrderService.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<!-- image viewer -->
	<script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
	<link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
	<%@ include file="/WEB-INF/views/modules/sd/tpl/fileUpload.html" %>
	<!-- 禁用词 -->
	<md:filterDisabledWord />
	<script type="text/javascript">
        SalesOrderService.rootUrl = "${ctx}";
		//防止拖文件到浏览器后自动打开或下载文件
		window.addEventListener("dragover",function(e){
			e = e || event;
			e.preventDefault();
		},false);
		window.addEventListener("drop",function(e){
			e = e || event;
			e.preventDefault();
		},false);
		//end
        //<%String parentIndex = request.getParameter("parentIndex");%>
        //var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
		var parentIndex = getCookie('layer.parent.id');
		//console.log("parentIndex:" + parentIndex);
        var clickTag = 0;
		var this_index = top.layer.index;
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
					var complainContent = $("#complainRemark").val();
					var forbiddenArray = filterForbiddenStr(complainContent);
					if(forbiddenArray != null){
						layerAlert("投诉描述含<font color='#4EB4E4'>【" + forbiddenArray.toLocaleString() + "】</font>等不文明用语,请注意用词文明！","提示");
						return false;
					}
                    if (clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    if(!checkUploadFile()){
						layerError("投诉[<font color='red'>中差评</font>]，需至少上传一张图片!","错误提示");
						clickTag = 0;
						return false;
					}
                    var ajaxSuccess = 0;
                    var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr('disabled', 'disabled');
					var loadingIndex;
					var action = $("#action").val();
					$.ajax({
						async: false,
						cache: false,
						type: "POST",
						url: "${ctx}/sales/sd/complain/save?"+ (new Date()).getTime(),
						data:$(form).serialize(),
						beforeSend: function () {
							loadingIndex = layer.msg('正在提交，请稍等...', {
								icon: 16,
								time: 0,
								shade: 0.3
							});
						},
						complete: function () {
							//console.log("" + new Date().getTime() + " [complete] clickTag:" + clickTag + " ,ajaxSuccess:" + ajaxSuccess);
							if(loadingIndex) {
								layer.close(loadingIndex);
							}
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
						   if(data && data.success == true) {
                               ajaxSuccess = 1;
								if(action=='0') {
									var iframe = getActiveTabIframe();//定义在jeesite.min.js中
									if (iframe != undefined) {
										iframe.repage();
									}
                                    top.layer.close(this_index);
									layerMsg('提交成功');

								}else{
									//回调父窗口方法
									setTimeout(function() {
										var layero = $("#layui-layer" + parentIndex,top.document);
										var iframeWin = top[layero.find('iframe')[0]['name']];
										iframeWin.reloadComplain();
										top.layer.close(this_index);//关闭本窗口
										return false;
									}, 300);
									return false;
								}
						   }
						   else if( data && data.message){
							   layerError(data.message,"错误提示");
						   }
						   else {
							   layerError("保存错误", "错误提示");
						   }
						   return false;
						},
						error: function (e) {
							ajaxLogout(e.responseText,null,"保存错误，请重试!");
						}
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
		
		function closeme(){
			//top.layer.close(this_index);
            layer.confirm(
                '取消后，填写的单据内容不保存，<br/>确定取消保存并关闭窗口吗？'
                ,{icon: 3, title:'系统确认',success: function(layro, index) {
                        $(document).on('keydown', layro, function(e) {
                            if (e.keyCode == 13) {
                                layro.find('a.layui-layer-btn0').trigger('click')
                            }else if(e.keyCode == 27){
                                layer.close(index);//关闭本身
                            }
                        })
                    }
                }
                ,function(index) {
                    layer.close(index);//关闭本身
                    top.layer.close(this_index);
                }
                ,function(index) {});
            return false;
		};
		
		 $(document).ready(function() {
			$('a[data-toggle=tooltip]').darkTooltip();
			$('a[data-toggle=tooltipnorth]').darkTooltip(
			{
				gravity : 'north'
			});
			$('a[data-toggle=tooltipeast]').darkTooltip(
			{
				gravity : 'east'
			});

			$("input[name='complainItemsIds']").on('change', function (event) {
                if ($("input[name='complainItemsIds']:checked").size() > 2) {
                    $(this).prop("checked", false);
                    layerError("请选择至少一个投诉项目，最多两个投诉项目！");
				}
            });
		});

		 function checkUploadFile(){
			 var check_opt = $("input[name='complainItemsIds']").filter('[value=1]');
			 if(check_opt && true == check_opt.prop('checked')){
			 	var fileQty = $("#divUploadFile").find(".upload_warp_left").length;
			 	if(fileQty <= 1){
			 		return false;
				}
			 }
			return true;
		 }

	</script>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label {width: 100px;}
		.form-horizontal .controls {margin-left: 120px;}
		.myalert {padding: 2px 5px 2px 5px;margin-bottom: 2px;}
	</style>
</head>
<body>
	<form:form id="inputForm" modelAttribute="complain" action="${ctx}/sales/sd/complain/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="action"/>
		<form:hidden path="quarter"/>
		<form:hidden path="customer.id" />
		<form:hidden path="complainNo" />
		<form:hidden path="status.value" />
		<form:hidden path="orderId" />
		<form:hidden path="productCategoryId" />
		<form:hidden path="kefu.id" />
		<form:hidden path="kefu.name" />
		<form:hidden path="customer.name"  htmlEscape="false" cssClass="input-block-level required" readonly="true"/>
		<form:hidden path="userName"  htmlEscape="false" readonly="true" cssClass="input-block-level required" />
		<form:hidden path="userPhone"  htmlEscape="false" readonly="true" cssClass="input-block-level required"  />
		<form:hidden path="userAddress"  htmlEscape="false" readonly="true" cssClass="input-block-level required" />
		<form:hidden path="complainBy"  htmlEscape="false" cssClass="input-block-level required" maxlength="10" />
		<form:hidden path="area.id"/>
		<input  id="complainDate" name="complainDate"
				type="hidden" readonly="readonly"
				maxlength="10" class="input-block-level required Wdate" value="${fns:formatDate(complain.complainDate,'yyyy-MM-dd')}"
				onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});" />
		<sys:message content="${message}"/>
	<fieldset>
		<legend>
				<%--<p class="text-right" style="margin-right: 10px;<c:if test="${empty complain.complainNo}">margin-right: 115px;</c:if>">No. ${complain.complainNo}</p>--%>
            <div class="row-fluid">
                <div class="span10">
					<c:if test="${hasOpenForm eq true}">
                    <div class="alert alert-error myalert" id="div-alert" >
                        提醒：此工单还有未处理完成的投诉单，点击投诉单号查看详情。
                    </div>
					</c:if>
                </div>
                <div class="span2">
                    <p>No. </p>
                </div>
            </div>
		</legend>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">工单号:</label>
					<div class="controls">
						<form:hidden path="orderNo"  htmlEscape="false" cssClass="input-block-level required" readonly="true"/>
						<%--onclick="SalesOrderService.viewOrderDetail('${complain.orderId}','${complain.quarter}');"--%>
						<a href="javascript:void(0);">${complain.orderNo}</a>
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label">投诉方:</label>
					<div class="controls">
						<form:select path="complainType" cssClass="input-block-level required">
							<form:options items="${fns:getDictListFromMS('complain_type')}" itemLabel="label"
										  itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
						</form:select>
					</div>
				</div>
			</div>
		</div>

		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">投诉对象:</label>
					<div class="controls">
						<form:checkboxes path="complainObjectsIds" cssClass="required" items="${fns:getDictListFromMS('complain_object')}" itemLabel="label"
										 itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
					</div>
				</div>
			</div>
			<div class="span8">
				<div class="control-group">
					<label class="control-label">投诉项目:</label>
					<div class="controls">
						<form:checkboxes path="complainItemsIds" cssClass="required" items="${fns:getDictListFromMS('complain_item')}" itemLabel="label"
                                 itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">投诉描述:</label>
					<div class="controls">
						<form:textarea path="complainRemark" htmlEscape="false" rows="4" maxlength="500" class="input-block-level required" />
					</div>
				</div>
			</div>
		</div>
		<c:if test="${hasOpenForm eq false}">
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">附件:</label>
					<div class="upload controls">
						<div id="divUploadFile" class="upload_warp">
							<div id="btnUploadFile" class="upload_warp_left"></div>
							<div class=" upload_warp_right upload_warp_img" style="display: none;"></div>
							<input id="upload_file" name="upload_file" accept="image/gif,image/jpeg,image/png" type="file"  multiple style="display: none"/>
						</div>
						<div class="alert alert-info" style="margin:-5px 5px 5px;">
							单个文件不能超过5MB，支持jpg,png,gif类型；如使用ie浏览器，请升级到ie9以上版本
						</div>
					</div>
				</div>
			</div>
		</div>
		</c:if>
		<c:if test="${hasOpenForm eq false}">
		<div class="form-actions">
			<c:if test="${canAction eq true}">
				<%--<shiro:hasPermission name="sd:complain:create"><input id="btnSave" class="btn btn-primary" type="submit" value="提交申请"/>&nbsp;</shiro:hasPermission>--%>
				<input id="btnSave" class="btn btn-primary" type="submit" value="提交申请"/>&nbsp;
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="取 消"  onclick="closeme();" />
		</div>
		</c:if>
		<!-- 已登记的投诉单列表 -->
		<c:if test="${hasOpenForm eq true}">
			<legend>投诉列表</legend>
		<div class="row-fluid" id="div-list">
			<div class="span12">
				<table id="tb_complain" class="table table-striped table-bordered table-condensed table-hover">
					<thead>
					<tr>
						<th width="50px">序号</th>
						<th width="100px">投诉单号</th>
						<th width="100px">投诉日期</th>
						<th width="100px">状态</th>
						<th width="90px">投诉人</th>
						<th width="150px">投诉对象</th>
						<th width="150px">投诉项目</th>
						<th width="*">投诉描述</th>
					</tr>
					</thead>
					<tbody>
					<c:set var="rowNumber" value="0" />
					<c:forEach var="item" items="${list}" >
						<c:set var="rowNumber" value="${rowNumber+1}" />
					<tr>
						<td>${rowNumber}</td>
						<td>
							<a href="javascript:void(0);" onclick="SalesOrderService.complain_view('${item.id}','${item.quarter}');">${item.complainNo}</a>
						</td>
						<td><fmt:formatDate value="${item.complainDate}" pattern="yyyy-MM-dd"/></td>
						<td>${item.status.label}</td>
						<td>${item.complainBy}</td>
						<td>${item.complainObjectLabels}</td>
						<td>${item.complainItemLabels}</td>
						<td>${item.complainRemark}</td>
					</tr>
					</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
		</c:if>
	</fieldset>
	</form:form>
</body>
<script type="text/javascript">
    var data = ${fns:toJson(complain.applyAttaches)};
    var tmpl = document.getElementById('tpl-upload-file-image').innerHTML;
    var doTtmpl = doT.template(tmpl);
    var html = doTtmpl(data);
    //$("#divUploadFile").prepend(html);
    $("#btnUploadFile").before(html);//btnUploadFile前
    file_index = data.length;
    if(file_index>0 ){
        imageViewer();
    }
    <c:if test="${!empty complain.judgeAttaches && complain.judgeAttaches.size()>0}">
		var judgedata = ${fns:toJson(complain.judgeAttaches)};
		var judgetmpl = document.getElementById('tpl-upload-file-image').innerHTML;
		var judgedoTtmpl = doT.template(judgetmpl);
		var judgehtml = judgedoTtmpl(judgedata);
		$("#btnUploadFile").before(judgehtml);//btnUploadFile前
		file_index = judgedata.length;
    </c:if>
</script>
</html>