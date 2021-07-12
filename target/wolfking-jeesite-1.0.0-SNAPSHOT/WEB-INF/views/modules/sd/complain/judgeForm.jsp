<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>投诉单-判定</title>
	<meta name="description" content="判定投诉单">
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<!-- 文件上传 -->
	<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <!-- image viewer -->
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
	<%@ include file="/WEB-INF/views/modules/sd/tpl/judgeFileUpload.html" %>
	<%@ include file="/WEB-INF/views/modules/sd/complain/tpl/complainList.html" %>
	<!-- 禁用词 -->
	<md:filterDisabledWord />
	<script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
		var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
            loadComplainLogList();

            $(".judgeObject").trigger("change");//触发checkbox

			$("#inputForm").validate({
				submitHandler: function(form){
					var judgeRemark = $("#judgeRemark").val();
					var forbiddenArray = filterForbiddenStr(judgeRemark);
					if(forbiddenArray != null){
						layerAlert("判责意见含<font color='#4EB4E4'>【" + forbiddenArray.toLocaleString() + "】</font>等不文明用语,请注意用词文明！","提示");
						return false;
					}
					if(clickTag == 1){
						return false;
					}
					clickTag = 1;
                    var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr('disabled', 'disabled');
				    //对判责项目的完整性验证
                    var html="<label for='judgeItemsIds' class='error' style='display: inline-block;'>必选信息</label>";
                    //判断勾中了哪些
					var canSubmit=true;
                    $(".judgeObject").each(function(index){
                        //如果判责对象有勾选的话
                        var chk = $(".judgeObject")[index];
						if(chk.checked){
                            var name=".judgeItem_"+(index+1);
                            if($(name).length>0){
                                var nameSpan=".judgeItemSpan_"+(index+1);
                                var isItemSelected=false;
                                $(name).each(function (itemindex) {
                                    if( $(name)[itemindex].checked){
                                        isItemSelected=true;
                                        return false;
                                    }
                                });
                                //没有一个判责子对象有勾选
                                if(!isItemSelected){
                                    $(nameSpan).html(html);
                                    canSubmit=false;
                                }
                                //网点
                                if($(chk).val() == '1'){
                                    var selServicePoint = $("[id='servicePoint.id']");
                                    if(!selServicePoint || selServicePoint.val() == '0'){
                                        $("#spnServicePointInfo").html("<label for='judgeItemsIds' class='error' style='display: inline-block;'>请选择责任网点</label>");
                                        $("#spnServicePointInfo").show();
                                        //selServicePoint.next("span").html("<label for='judgeItemsIds' class='error' style='display: inline-block;'>请选择责任网点</label>");
                                        canSubmit=false;
                                        layerMsg("因判责对象选择了网点，请选择具体的责任网点！");
									}
								}
							}
                        }
                    });
                    if(!canSubmit){
					    clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
					    return false;
					}
					//中差评，必须上传附件
                    var checkedFlag = $("input[name='judgeItemsIds']").filter('[value=1]').is(':checked');
                    if(checkedFlag && $(".upload_warp_img_div").length==0){
                        layerError("责任判定：中差评，请上传附件相关附件", "提示");
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
                        return false;
                    }

				    var confirmClickTag = 0;
                    top.layer.confirm('确认提交判定结果吗?', {icon: 3, title:'系统确认'}, function(index){
                        if(confirmClickTag == 1){
                            return false;
						}
						confirmClickTag = 1;
                        top.layer.close(index);//关闭本身
                        var loadingIndex = layer.msg('正在提交，请稍等...', {
                            icon: 16,
                            time: 0,
                            shade: 0.3
                        });
                        var ajaxSuccess = 0;
                        $.ajax({
                            async: false,
                            cache: false,
                            type: "POST",
                            url: "${ctx}/sd/complain/savejudge?"+ (new Date()).getTime(),
                            data:$(form).serialize(),
                            beforeSend: function () {
                            },
                            complete: function () {
								layer.close(loadingIndex);
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
                                    setTimeout(function() {
                                        top.layer.close(this_index);
                                        layerMsg('提交成功');
                                        var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                                        if (iframe != undefined) {
                                            iframe.repage();
                                        }
                                    }, 300);
                                    return false;
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
                    },function(index){
                        //cancel
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
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
		});

        function openApppointForm(id,quarter) {
            top.layer.open({
                type: 2,
                id:'layer_appoint',
                zIndex:19891015,
                title:'待跟进',
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

        function loadComplainLogList(){
            Order.showComplainLogList('${complain.id}','${complain.quarter}');
        }
        $(document).on("click", "#btnSaveLog", function () {
            var self = this;
            if (clickTag == 1) {
                return false;
            }
            clickTag = 1;
            var $btnSubmit = $(self);
            $btnSubmit.attr('disabled', 'disabled');
            var complainLogContent =  $("#complainLogContent").val();
            if(Utils.isEmpty(complainLogContent)){
                layerError("请输入处理日志内容.","错误提示");
                $btnSubmit.removeAttr('disabled');
                clickTag = 0;
                return false;
            }
			var forbiddenArray = filterForbiddenStr(complainLogContent);
			if(forbiddenArray != null){
				layerAlert("日志内容含<font color='#4EB4E4'>【" + forbiddenArray.toLocaleString() + "】</font>等不文明用语,请注意用词文明！","提示");
				$btnSubmit.removeAttr('disabled');
				clickTag = 0;
				return false;
			}
            //日志简明增加操作名称
            complainLogContent="判定处理日志:"+complainLogContent;
            $("#btnSaveLog").attr("disabled", "disabled");
            var loadingIndex;
            var complainId = $("#id").val();
            var quarter = $("#quarter").val();
            var visibilityFlag = $("input[name='visibilityFlag']:checked").val();
            $.ajax({
                async: false,
                cache: false,
                type: "POST",
                data:{"complainId":complainId,"quarter":quarter,"complainLogContent":complainLogContent, "visibilityFlag":visibilityFlag},
                url: "${ctx}/sd/complain/ajax/saveComplainlog?"+ (new Date()).getTime(),
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
					setTimeout(function () {
						clickTag = 0;
						$btnSubmit.removeAttr('disabled');
						$('#btnSaveLog').removeAttr('disabled');
					}, 2000);
                },
                success: function (data) {
                    if(data && data.success == true){
                        $("#complainLogContent").val("");
                        loadComplainLogList();
                    }
                    else if( data && data.message){
                        layerError(data.message,"错误提示");
                    }
                    else{
                        layerError("保存处理日志错误","错误提示");
                    }
                    return false;
                },
                error: function (e) {
                    layerError("保存处理日志错误:"+e,"错误提示");
                }
            });
            return false;
        });
		
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
	<form:form id="inputForm" modelAttribute="complain" action="${ctx}/sd/complain/save" method="post" class="form-horizontal">
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
						<input type="text" name="complainType.label" disabled="disabled" cssClass="input-block-level" value="${fns:getDictLabelFromMS(complain.complainType.value,'complain_type','')}" />
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
					<label class="control-label">附件:</label>
					<div class="controls">
						<div id="divUploadFile" class="upload_warp">
						</div>
					</div>
				</div>
			</div>
		</div>
        </c:if>
		<legend>投诉判责</legend>
		<fieldset id="divJudge" <c:if test="${complain.status.value == '0'}"> style="display:none;" </c:if> >
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">责任判定:</label>
					<div class="controls">
						<table class="table table-bordered table-striped">
							<thead>
							<tr style="display: none;">
								<th width="160px"></th>
								<th></th>
							</tr>
							</thead>
							<c:set var="judgeObjects" value="${fns:getDictListFromMS('judge_object')}" />
							<c:set var="objIdx" value="1"/>
							<c:set var="itmIdx" value="1"/>
							<c:forEach items="${judgeObjects}" var="dict">
								<tr>
									<td>
										<spring:eval var="containsObject" expression="complain.judgeObjectsIds.contains(dict.value)" />
										<span><input id="judgeObjectsIds${objIdx}" name="judgeObjectsIds" class="required judgeObject" type="checkbox"
													 <c:if test="${containsObject}">checked="checked"</c:if>
													 value="${dict.value}"><label for="judgeObjectsIds${objIdx}">${dict.label}</label></span>
									</td>
									<td>
										<c:set var="dictType" value="judge_item_${dict.value}" />
										<c:set var="judgeItems" value="${fns:getDictListFromMS(dictType)}" /><%--切换为微服务--%>
										<c:forEach items="${judgeItems}" var="item">
											<spring:eval var="containsItem" expression="complain.judgeItemsIds.contains(item.value)" />
										<span><input id="judgeItemsIds${itmIdx}" name="judgeItemsIds" data-Objectid="${objIdx}" class="judgeItemCheck judgeItem_${objIdx}" type="checkbox"
													 <c:if test="${containsItem}">checked="checked"</c:if>
													 <c:if test="${not containsObject}">disabled="disabled"</c:if>
													 value="${item.value}"><label for="judgeItemsIds${itmIdx}">${item.label}</label></span>
											<c:set var="itmIdx" value="${itmIdx+1}"/>
										</c:forEach>
										<c:if test="${judgeItems.size() gt 0}">
											<span class="judgeItemSpan_${objIdx}"></span>
										</c:if>
										<!-- 网点 -->
										<c:if test="${dict.value eq '1'}">
											<br/>
											<label>责任网点:</label>
											<form:select path="servicePoint.id" cssClass="amerceResultIds_1 required input-medium" cssStyle="width:260px;">
												<form:option value="0">无</form:option>
												<form:options items="${complain.servicePoints}" itemLabel="name" itemValue="id" htmlEscape="false" />
											</form:select>
											<span id="spnServicePointInfo"></span>
										</c:if>
									</td>
								</tr>
								<c:set var="objIdx" value="${objIdx+1}"/>
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
						<form:textarea path="judgeRemark" htmlEscape="false" rows="4" maxlength="497" class="input-block-level required" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span3">
				<div class="control-group">
					<label class="control-label">判责人:</label>
					<div class="controls">
						<form:input path="judgeBy.name" disabled="true" htmlEscape="false" cssClass="input-block-level required" maxlength="10" />
					</div>
				</div>
			</div>
			<div class="span3">
				<div class="control-group">
					<label class="control-label">判责时间:</label>
					<div class="controls">
						<input id="judgeDate" name="judgeDate"
							   type="text" readonly="readonly"
							   maxlength="21" class="input-block-level required Wdate"
							   value="${fns:formatDate(complain.judgeDate,'yyyy-MM-dd HH:mm:ss')}" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">附件:</label>
					<div class="upload controls">
						<div id="divUploadFileJudge" class="upload_warp">
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
		</fieldset>

		<div class="form-actions">
			<c:if test="${canAction eq true }">
				<shiro:hasPermission name="sd:complain:judge">
					<c:if test="${complain.status.value eq '0'}">
						<input id="btnAccept" name="btnAccept" class="btn btn-primary" type="button" value="开始处理"/>&nbsp;
						<input id="btnAppoint" name="btnAppoint" style="display: none;" class="btn btn-primary" type="button" value="待跟进" onclick="openApppointForm('${complain.id}','${complain.quarter}');"/>&nbsp;
						<input id="btnSave" name="btnSave" style="display: none;" class="btn btn-primary" type="submit" value="提交判定"/>&nbsp;
					</c:if>
					<c:if test="${complain.status.value eq '1'}">
						<input id="btnAppoint" name="btnAppoint" class="btn btn-primary" type="button" value="待跟进" onclick="openApppointForm('${complain.id}','${complain.quarter}');"/>&nbsp;
						<input id="btnSave" name="btnSave" class="btn btn-primary" type="submit" value="提交判定"/>&nbsp;
					</c:if>
				</shiro:hasPermission>
			</c:if>
			<input id="btnCancel" name="btnCancel" class="btn" type="button" value="取 消"  onclick="closeme();" />
		</div>
	</fieldset>
	</form:form>
	<div style="margin: 0 20px;margin-bottom: 20px">
		<legend>处理日志</legend>
		<c:if test="${complain.status.value==1}">
			<form id="complainLogForm" class="form-horizontal" modelAttribute="complainLog" method="post" novalidate="novalidate">
				<div class="row-fluid">
					<div class="span8">
						<div class="control-group">
							<label class="control-label">日志内容:</label>
							<div class="controls">
								<textarea id="complainLogContent" name="complainLogContent" maxlength="200"  style="width:100%;max-width: 100%" class="required" rows="3"></textarea>
							</div>
						</div>
					</div>
					<div class="span3">
						<div class="control-group">
							<span>
                            	<input id="visibilityFlag1" name="visibilityFlag" class="required" type="radio" value="2"  checked="checked" />
								<label for="visibilityFlag1">是</label>
                        	</span>
							<span>
                            	<input id="visibilityFlag2" name="visibilityFlag" class="required" type="radio" value="0"/>
                            	<label for="visibilityFlag2">否</label>
                        	</span>
							（客户是否可见）
						</div>
						<div class="controls" style="margin: 10px 0px 0px 0px;">
							<button id="btnSaveLog" name="btnSaveLog" class="btn btn-primary" type="button"><i class="icon-save icon-white"></i> 保存</button>
						</div>
					</div>
				</div>

			</form>
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
	</div>

	<script type="text/javascript">
        var applyAttachNum = 0;
        var judgeAttachNum = 0;
        <c:if test="${!empty complain.applyAttaches && complain.applyAttaches.size()>0}">
        var data = ${fns:toJson(complain.applyAttaches)};
        var tmpl = document.getElementById('tpl-upload-file-image-view').innerHTML;
        var doTtmpl = doT.template(tmpl);
        var html = doTtmpl(data);
        $("#divUploadFile").append(html);
        $("#divUploadFile").viewer('destroy').viewer({ url: "data-original"});
        applyAttachNum = data.length;
        </c:if>


        <c:if test="${!empty complain.judgeAttaches && complain.judgeAttaches.size()>0}">
        var judgedata = ${fns:toJson(complain.judgeAttaches)};
        var judgetmpl = document.getElementById('tpl-upload-file-image').innerHTML;
        var judgedoTtmpl = doT.template(judgetmpl);
        var judgehtml = judgedoTtmpl(judgedata);
        $("#btnUploadFile").before(judgehtml);//btnUploadFile前
        file_index = judgedata.length;
        judgeAttachNum =judgedata.length;
        </c:if>
        if(applyAttachNum>0 || judgeAttachNum>0){
            imageViewer();
        }
        $("#btnAccept").on('click',function(){
            if(clickTag == 1){
                return false;
			}
			clickTag = 1;
            var $btnAccept = $("#btnAccept");
            $btnAccept.attr("disabled", "disabled");
            var id = $("#id").val();
            if(!id || Utils.isEmpty(id)){
                layerError("投诉单id为空，请重新操作。","错误提示");
                clickTag = 0;
                $btnAccept.removeAttr("disabled");
                return false;
            }
            var quarter = $("#quarter").val();
            var ajaxSuccess = 0;
            $.ajax({
                type: "POST",
                url: "${ctx}/sd/complain/accept",
                data:{id:id,quarter:quarter},
                complete: function () {
					setTimeout(function () {
						clickTag = 0;
						$btnAccept.removeAttr('disabled');
					}, 2000);
                },
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if(data && data.success == true){
                        $btnAccept.remove();
                        $("#btnSave").show();
                        $("#divJudge").show();
                        $("#btnAppoint").show();
                        return false;
                    }
                    else if( data && data.message){
                        layerError(data.message,"错误提示");
                    }
                    else{
                        layerError("开始处理失败，请重试","错误提示");
                    }
                    return false;
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"修改配件申请失败，请重试!");
                }
            });
        });

        $(".judgeObject").on('change',function(){
            var $this = $(this);
            var id =$this.val();
            var checked = $this.is(':checked');
            if(!checked) {
                $(".judgeItem_" + id).prop("checked", checked);
                //如果有Item的必选提示就去掉
                var name=".judgeItemSpan_"+id;
                if($(name).length>0 && $(name)[0].lastChild!=null){
                    $(name)[0].lastChild.style.display ='none';
                }
                if(id == '1'){
                    var spnServicePointInfo = $("#spnServicePointInfo");
                    if(spnServicePointInfo){
                        spnServicePointInfo.hide();
					}
				}
            }
            $(".judgeItem_" + id).prop("disabled",!checked);
        });

        $(".judgeItemCheck").on('change',function(e){
            if(e.target.checked)
            {
                //var objectId=e.target.dataset.objectid;
                var objectId = e.target.value;
                var name = ".judgeItemSpan_"+objectId;
                if($(name).length>0 && $(name)[0].lastChild!=null){
                    $(name)[0].lastChild.style.display ='none';
                }
            }
        });

	</script>
</body>
</html>