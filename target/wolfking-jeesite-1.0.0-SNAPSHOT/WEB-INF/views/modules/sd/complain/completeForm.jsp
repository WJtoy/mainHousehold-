<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>投诉单-完成</title>
	<meta name="description" content="完成投诉单">
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<!-- image viewer -->
	<script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
	<link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
	<!-- 文件上传 -->
	<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
	<%@ include file="/WEB-INF/views/modules/sd/complain/tpl/attacheView.html" %>
	<%@ include file="/WEB-INF/views/modules/sd/complain/tpl/complainList.html" %>
	<!-- 禁用词 -->
	<md:filterDisabledWord />
	<script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
		var this_index = top.layer.index;
		$(document).ready(function() {

            loadComplainLogList();

            $(".compensateResultIds").trigger("change");//触发checkbox
            $(".amerceResultIds").trigger("change");//触发checkbox

			$("#inputForm").validate({
                rules: {
                    'customerAmount':{ min: 0},
                    'userAmount':{min:0},
                    'servicePointAmount':{min:0},
                    'kefuAmount':{min:0}
                },
                messages: {
                    'customerAmount' : {min: "不能小于0"},
                    'userAmount':{min:"不能小于0"},
                    'servicePointAmount':{min:"不能小于0"},
                    'kefuAmount':{min:"不能小于0"}
                },
				submitHandler: function(form){
                    var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr('disabled', 'disabled');
                    var clickTag = 0;

					var completeRemark = $("#completeRemark").val();
					var forbiddenArray = filterForbiddenStr(completeRemark);
					if(forbiddenArray != null){
						layerAlert("处理意见含<font color='#4EB4E4'>【" + forbiddenArray.toLocaleString() + "】</font>等不文明用语,请注意用词文明！","提示");
						return false;
					}
                    layer.confirm(
                        '完成后，投诉单不能更改，确定完成吗？'
                        ,{icon: 3, title:'系统确认',success: function(layro, index) {
                                $(document).on('keydown', layro, function(e) {
                                    if (e.keyCode == 13) {
                                        layro.find('a.layui-layer-btn0').trigger('click');
                                        layer.close(index);//关闭本身
                                    }else if(e.keyCode == 27){
                                        $btnSubmit.removeAttr('disabled');
                                        layer.close(index);//关闭本身
                                    }
                                })
                            }
                        }
                        ,function(index) {
                            if(clickTag == 1){
                                return false;
                            }
                            clickTag = 1;
                            layer.close(index);//关闭本身
                            // do something
                            var loadingIndex;
                            var ajaxSuccess = 0;
                            $.ajax({
                                async: false,
                                cache: false,
                                type: "POST",
                                url: "${ctx}/sd/complain/savecomplete?"+ (new Date()).getTime(),
                                data:$(form).serialize(),
                                beforeSend: function () {
                                    loadingIndex = layer.msg('正在提交，请稍等...', {
                                        icon: 16,
                                        time: 0,
                                        shade: 0.3
                                    });
                                },
                                complete: function () {
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
                                        top.layer.close(this_index);
										layerMsg('提交成功');
										var iframe = getActiveTabIframe();//定义在jeesite.min.js中
										if (iframe != undefined) {
											iframe.repage();
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
                            return false;
                        }
                        ,function(index) {
                            //cancel
                            $btnSubmit.removeAttr('disabled');
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

        function loadComplainLogList(){
            Order.showComplainLogList('${complain.id}','${complain.quarter}');
        }

        function openApppointForm(id,quarter) {
            top.layer.open({
                type: 2,
                id:'layer_appoint',
                zIndex:19891015,
                title:'时间设定',
                content: "/sd/complain/appointForm?orderComlpainId="+ id +"&quarter=" + quarter || '',
                area: ['550px', '320px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

		function closeme(){
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
		});
	</script>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label {width: 100px;}
		.form-horizontal .controls {margin-left: 120px;}
	</style>
</head>
<body>
	<form:form id="inputForm" modelAttribute="complain"  method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="quarter"/>
		<form:hidden path="complainNo"/>
		<sys:message content="${message}"/>
	<fieldset>
		<legend>
				<p class="text-right" style="margin-right: 10px;<c:if test="${empty complain.complainNo}">margin-right: 115px;</c:if>">No. ${complain.complainNo}</p>
		</legend>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">工单号:</label>
					<div class="controls">
						<form:hidden path="orderNo" disabled="true" htmlEscape="false" cssClass="input-block-level required" readonly="true"/>
						<a style="line-height: 30px" class="input-block-level"  href="javascript:void(0);" onclick="Order.showComplainOrderDetail('${complain.orderId}','${complain.quarter}');">${complain.orderNo}</a>
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label">投诉方:</label>
					<div class="controls">
						<form:select path="complainType" cssClass="input-block-level required" disabled="true">
							<form:options items="${fns:getDictListFromMS('complain_type')}" itemLabel="label"
										  itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
						</form:select>
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label">状态:</label>
					<div class="controls">
						<form:input path="status.label"  htmlEscape="false" disabled="true" cssClass="input-block-level " maxlength="10" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">投诉对象:</label>
					<div class="controls">
						<%--<form:input path="complainObjectLabels"  htmlEscape="false" disabled="true" cssClass="input-block-level"  />--%>
						<c:forEach var="item" items="${fn:split(complain.complainObjectLabels,',')}" >
							<span class="label label-important">${item}</span>
						</c:forEach>
					</div>
				</div>
			</div>
			<div class="span8">
				<div class="control-group">
					<label class="control-label">投诉项目:</label>
					<div class="controls">
						<%--<form:input path="complainItemLabels"  htmlEscape="false" disabled="true" cssClass="input-block-level" />--%>
						<c:forEach var="item" items="${fn:split(complain.complainItemLabels,',')}" >
							<span class="label label-important">${item}</span>
						</c:forEach>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">投诉人:</label>
					<div class="controls">
						<form:input path="complainBy" disabled="true" htmlEscape="false" cssClass="input-block-level " maxlength="10" />
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label">投诉日期:</label>
					<div class="controls">
						<input id="complainDate" name="complainDate"
							   type="text" readonly="readonly"
							   maxlength="10" class="input-block-level  Wdate"
							   value="${fns:formatDate(complain.complainDate,'yyyy-MM-dd')}" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">投诉描述:</label>
					<div class="controls">
						<form:textarea path="complainRemark" htmlEscape="false" readonly="true" disabled="true" rows="3" maxlength="490" class="input-block-level" cssStyle="resize: vertical" />
					</div>
				</div>
			</div>
		</div>
		<c:if test="${complain.attachmentQty>0}">
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">投诉附件:</label>
					<div class="controls">
						<div id="divUploadFile" class="upload_warp">
						</div>
					</div>
				</div>
			</div>
		</div>
		</c:if>
		<legend>投诉判责</legend>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">责任判定:</label>
					<div class="controls">
						<table class="table table-bordered table-striped">
							<c:set var="judgeObjects" value="${fns:getDictListFromMS('judge_object')}" /><%--切换为微服务--%>
							<c:forEach items="${judgeObjects}" var="dict">
								<spring:eval var="containsObject" expression="complain.judgeObjectsIds.contains(dict.value)" />
								<c:if test="${containsObject}">
									<tr>
										<td width="140px">
											<span>${dict.label}</span>
										</td>
										<td>
											<c:set var="dictType" value="judge_item_${dict.value}" />
											<c:set var="judgeItems" value="${fns:getDictListFromMS(dictType)}" />
											<c:forEach items="${judgeItems}" var="item">
												<spring:eval var="containsItem" expression="complain.judgeItemsIds.contains(item.value)" />
												<c:if test="${containsItem}">
													<span class="label label-important">${item.label}</span>
												</c:if>
											</c:forEach>
											<!-- 网点 -->
											<c:if test="${dict.value eq '1' and complain.servicePoint != null and complain.servicePoint.id >0}">
												<label>责任网点:</label>
												<span class="label label-info">${complain.servicePoint.servicePointNo} - ${complain.servicePoint.name}</span>
											</c:if>
										</td>
									</tr>
								</c:if>
							</c:forEach>
						</table>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">判责意见:</label>
					<div class="controls">
						<form:textarea path="judgeRemark" htmlEscape="false" readonly="true" disabled="true" rows="3" maxlength="490" class="input-block-level" cssStyle="resize: vertical" />
					</div>
				</div>
			</div>
		</div>
		<c:if test="${complain.judgeAttachmentQty >0}">
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">判责附件:</label>
						<div class="controls">
							<div id="divJudgeUploadFile" class="upload_warp">
							</div>
						</div>
					</div>
				</div>
			</div>
		</c:if>
		<!-- log -->
		<div class="tabbable">
			<ul class="nav nav-tabs">
				<li id="liLoglist" class="active"><a href="#tabComplainLogList" data-toggle="tab" id="lnktabComplainLogList" >处理日志</a></li>
			</ul>
			<!-- tab content -->
			<div class="tab-content">
				<div class="tab-pane active" id="tabComplainLogList" >
				</div>
			</div>
		</div>
		<!-- 完成 -->
		<legend>处理方案</legend>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">处理方案:</label>
					<div class="controls">
						<form:checkboxes path="completeResultIds" cssClass="required" items="${fns:getDictListFromMS('complete_result')}"
						itemLabel="label" itemValue="value" htmlEscape="false" /><%--切换为微服务--%>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">处理意见:</label>
					<div class="controls">
						<form:textarea path="completeRemark" htmlEscape="false" rows="4" maxlength="500" class="input-block-level required" />
					</div>
				</div>
			</div>
		</div>
		<legend>赔偿</legend>
		<div class="row-fluid">
			<div class="row-fluid">
				<div class="span4">
					<div class="control-group">
						<label class="control-label"><form:checkbox path="compensateResultIds" value="1" label="厂商" cssClass="compensateResultIds" /></label>
						<div class="controls">
							<form:input path="customer.name" disabled="true" htmlEscape="false" />
						</div>
					</div>
				</div>
				<div class="span4">
					<div class="control-group">
						<label class="control-label">金额:</label>
						<div class="controls">
							<form:input path="customerAmount" cssClass="number compensateResultIds_1 required" />
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="row-fluid">
				<div class="span4">
					<div class="control-group">
						<label class="control-label"><form:checkbox path="compensateResultIds" value="2" label="用户" cssClass="compensateResultIds" /></label>
						<div class="controls">
							<form:input path="userName" disabled="true" htmlEscape="false" />
						</div>
					</div>
				</div>
				<div class="span4">
					<div class="control-group">
						<label class="control-label">金额:</label>
						<div class="controls">
							<form:input path="userAmount" cssClass="number compensateResultIds_2 required" />
						</div>
					</div>
				</div>
			</div>
		</div>
		<legend>罚款</legend>
		<div class="row-fluid">
			<div class="row-fluid">
				<div class="span4">
					<div class="control-group">
						<c:set var="canCheckedServicePoint" value="false" />
						<c:if test="${complain.servicePoint != null and complain.servicePoint.id>0}">
							<c:set var="canCheckedServicePoint" value="true" />
						</c:if>
						<label class="control-label">
							<form:checkbox path="amerceResultIds" value="1" label="网点" disabled="${canCheckedServicePoint == 'false'?'true':'false'}" cssClass="amerceResultIds" />
						</label>
						<div class="controls">
							<input id="servicePointName" name="servicePointName" disabled="disabled" type="text" value="${complain.servicePoint.servicePointNo} - ${complain.servicePoint.name}">
							<%--<form:select path="servicePoint.id" cssClass="amerceResultIds_1 required input-medium" cssStyle="width: 220px;">--%>
								<%--<form:options items="${complain.servicePoints}" itemLabel="name" itemValue="id" htmlEscape="false" />--%>
							<%--</form:select>--%>
						</div>
					</div>
				</div>
				<div class="span4">
					<div class="control-group">
						<label class="control-label">金额:</label>
						<div class="controls">
							<form:input path="servicePointAmount" cssClass="amerceResultIds_1 number required" />
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="row-fluid">
				<div class="span4">
					<div class="control-group">
						<label class="control-label"><form:checkbox path="amerceResultIds" value="2" label="客服" cssClass="amerceResultIds" /></label>
						<div class="controls">
							<form:input path="kefu.name" disabled="true" htmlEscape="false" />
						</div>
					</div>
				</div>
				<div class="span4">
					<div class="control-group">
						<label class="control-label">金额:</label>
						<div class="controls">
							<form:input path="kefuAmount" cssClass="amerceResultIds_2 number required" />
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="form-actions">
			<c:if test="${canAction eq true }">
				<shiro:hasPermission name="sd:complain:complete">
					<c:if test="${complain.status.value eq '1' && complain.judgeDate ne null}">
					<input id="btnAppoint" name="btnAppoint" class="btn btn-primary" type="button" value="待跟进" onclick="openApppointForm('${complain.id}','${complain.quarter}');"/>&nbsp;
					<input id="btnSave" name="btnSave" class="btn btn-primary" type="submit" value="确认完成"/>&nbsp;
					</c:if>
				</shiro:hasPermission>
			</c:if>
			<input id="btnCancel" name="btnCancel" class="btn" type="button" value="取 消"  onclick="closeme();" />
		</div>
	</fieldset>
	</form:form>
</body>
<script type="text/javascript">

    <c:if test="${!empty complain.applyAttaches && complain.applyAttaches.size()>0}">
		var data = ${fns:toJson(complain.applyAttaches)};
		var tmpl = document.getElementById('tpl-upload-file-image-view').innerHTML;
		var doTtmpl = doT.template(tmpl);
		var html = doTtmpl(data);
		var $divUploadFile = $("#divUploadFile");
    	$divUploadFile.append(html);
    	$divUploadFile.viewer('destroy').viewer({ url: "data-original"});
    </c:if>

    <c:if test="${!empty complain.judgeAttaches && complain.judgeAttaches.size()>0}">
		var judgedata = ${fns:toJson(complain.judgeAttaches)};
		var judgetmpl = document.getElementById('tpl-upload-file-image-view').innerHTML;
		var judgedoTtmpl = doT.template(judgetmpl);
		var judgehtml = judgedoTtmpl(judgedata);
		var $divJudgeUploadFile = $("#divJudgeUploadFile");
		$divJudgeUploadFile.append(judgehtml);
    	$divJudgeUploadFile.viewer('destroy').viewer({ url: "data-original"});
    </c:if>


    $(".compensateResultIds").on('change',function(){
        var $this = $(this);
        var id =$this.val();
        var checked = $this.is(':checked');
        $(".compensateResultIds_" + id).prop("disabled",!checked);
    });
    $(".amerceResultIds").on('change',function(){
        var $this = $(this);
        var id =$this.val();
        var checked = $this.is(':checked');
        $(".amerceResultIds_" + id).prop("disabled",!checked);
    });

</script>
</html>