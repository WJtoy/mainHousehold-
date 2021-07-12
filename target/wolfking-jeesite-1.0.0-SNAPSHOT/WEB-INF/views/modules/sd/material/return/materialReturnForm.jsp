<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<html>
<head>
	<title>配件申请-返件物流信息</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
	<script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
	<link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
	<script type="text/javascript">
        <%--<%String parentIndex = request.getParameter("parentIndex");%>--%>
        <%--var parentIndex = '<%=parentIndex==null?"":parentIndex %>';--%>
        var parentLayerIndex = parent.layer.getFrameIndex(window.name);
		Order.rootUrl = "${ctx}";
		var this_index = top.layer.index;
		var clickTag = 0;
		var isExecute = false;
        var $btnSubmit = $("#btnSubmit");
		$(document).ready(function() {
			/*$("#buttonUploadLogo").click(function() {
                var $btn = $(self);
                if (clickTag == 1){
                    return false;
                }
                clickTag = 1;
                $btn.attr('disabled', 'disabled');
                $btnSubmit.attr('disabled', 'disabled');
				uploadfile($("#logo"),$("#logo_image"), "fileToUploadlogo2");
				return false;
			});*/
            $("#divImg_0").click(function () {
                $("#upload_file").click();
                return false;
            });
			
			$("#btnSubmit").click(function(event){
                //console.log("" + new Date().getTime() + " clickTag:" + clickTag);
                if (clickTag == 1){
                    return false;
                }
                clickTag = 1;
                if(Utils.isEmpty($("[id='expressCompany.value']").val())){
                    event.preventDefault();
                    clickTag = 0;
                    layerAlert('请选择快递公司!');
                    return false;
                }
			    if(Utils.isEmpty($("#expressNo").val())){
			        event.preventDefault();
                    clickTag = 0;
                    layerAlert('请输入快递单号!');
			        return false;
				}
                if(Utils.isEmpty($("#receivorAddress").val())){
                    event.preventDefault();
                    clickTag = 0;
                    layerAlert('收件地址不能为空,请重新添加地址!');
                    return false;
                }
                if(Utils.isEmpty($("#receiverAreaId").val()) || $("#receiverAreaId").val()=='0'){
                    event.preventDefault();
                    clickTag = 0;
                    layerAlert('收件区域不能为空,请重新添加地址!');
                    return false;
                }
                if(Utils.isEmpty($("#receivor").val())){
                    event.preventDefault();
                    clickTag = 0;
                    layerAlert('收件人不能为空,请重新添加收件人!');
                    return false;
                }
                if(Utils.isEmpty($("#receivorPhone").val())){
                    event.preventDefault();
                    clickTag = 0;
                    layerAlert('收件人联系电话不能为空,请重新添加手机号!');
                    return false;
                }
				$btnSubmit.attr("disabled", "disabled");
			    var loadingIndex;
			    var ajaxSucess = 0;
				$.ajax({
					type: "POST",
					url: "${ctx}/sd/material/return/saveExpress",
					data:$("#inputForm").serialize(),
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
                        //console.log("" + new Date().getTime() + " clickTag:" + clickTag);
						if(ajaxSucess == 0) {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                                //console.log("" + new Date().getTime() + " clickTag:" + clickTag);
                            }, 2000);
                        }
                    },
					success: function (data) {
                        if(ajaxLogout(data)){
                            return false;
                        }
				       if(data && data.success == true){
                           clickTag = 0;
                           ajaxSucess = 1;
				    	   //刷新父窗口
                           var parentIndex = getCookie('layer.parent.id');
                           //console.log("parent:" + parentIndex);
						   if(parentIndex && parentIndex != 0){
                               //var this_index = parent.layer.getFrameIndex(window.name);
                               var layero = $("#layui-layer"+parentIndex,top.document);
                               var iframeCtl = layero.find('iframe');
                               if(iframeCtl && iframeCtl.length>0) {
                                   var iframeWin = top[iframeCtl[0]['name']];
                                   if(typeof iframeWin.reloadApproveList === "function" && iframeWin.reloadApproveList ) {
                                       iframeWin.reloadApproveList();
                                   }else{
                                       iframeWin.location.reloadApproveList();
                                   }
                                   top.layer.close(this_index);//ie下报错
                               }else{
                                   layerMsg('返件成功,请手动关闭此页面!');
                               }
						   }
						   else{
                               layerMsg('返件成功,请手动关闭此页面!');
                           }
				       }
				       else if( data && data.message){
				    	   layerError(data.message,"错误提示");
				       }
				       else{
                           layerError("返件失败","错误提示");
				       }
				       return false;
					},
					error: function (e) {
                        ajaxLogout(e.responseText,null,"返件失败，请重试!");
					}
				});
				return false;
			});

            $("[id='expressCompany.value']").on('change', function () {
                var companyName = $("[id='expressCompany.value'] option:selected").text();
                $("input[id='expressCompany.label']").val(companyName);
            });
            imageViewer();
            bindImageDragEvent();
        });

        function uploadFileChange(){
            isExecute = true;
            var files = document.getElementById("upload_file").files;
            if(files == null || files.size==0){
                isExecute = false;
                return false;
            }
            uploadfile($("#logo"),$("#logo_image"),"upload_file");
        }

        function uploadfile($obj1, $obj1_image, obj2)
        {
            var loadingIndex = top.layer.msg('正在上传，请稍等...', {
                icon: 16,
                time: 0,
                shade: 0.3
            });
            var data = {fileName : $obj1.val()};
            var ajaxSuccess = 0;
            $.ajaxFileUpload(
                {
                    url : '${pageContext.request.contextPath}/servlet/Upload?fileName='
                    + $obj1.val(),//处理图片脚本
                    secureuri : false,
                    data : data,
                    fileElementId : obj2,//file控件id
                    dataType : 'json',
                    success : function(data, status)
                    {
                        top.layer.close(loadingIndex);
                        isExecute = true;
                        if(ajaxLogout(data)){
                            return false;
                        }
                        $obj1.val(data.fileName);
                        $("#orignalName").val(data.origalName);
                        if(data.origalName =="" )
                        {
                            layerError("请先选择文件，再点击上传","错误提示");
                            return true;
                        }
                        ajaxSuccess = 1;
                       /* $("#inputForm").attr("action","${ctx}/sd/material/return/saveTempAttachment");
                        $("#inputForm").submit();*/
                        $.ajax(
                            {
                                cache : false,
                                type : "POST",
                                async : false,
                                url : "${ctx}/sd/material/ajax/return/saveAjaxReturnTempAttachment",
                                data : $("#inputForm").serialize(),
                                success : function(data)
                                {
                                    if(ajaxLogout(data)){
                                        return false;
                                    }
                                    if (data)
                                    {
                                        if (data && data.success == true)
                                        {
                                            addUploadWarp(data.data.id,$("#logo").val());
                                            $("#upload_file").val('');
                                            imageViewer();
                                            $("#divImg_0").removeClass("imgOnDarg");
                                            isExecute = false;
                                        } else
                                        {
                                            layerError("照片上传失败!");
                                        }
                                    }
                                    return false;
                                },
                                error : function(e)
                                {
                                    isExecute = true;
                                    layerError("照片上传失败!");
                                }
                            });

                    },
                    error : function(data, status, e)
                    {
                        isExecute = true;
                        top.layer.close(loadingIndex);
                        layerError(e, "错误提示");
                    }
                });
        }

		function closeForm(id,quarter){
			top.layer.open({
				type: 2,
				id: 'layer_material_close',
				zIndex:19891015,
				title:'<font color="red">取消</font>返件申请单',
				content: '${ctx}/sd/material/return/close?id=' + id + "&quarter=" + (quarter || ''),
				area: ['500px', '300px'],
				shade: 0.3,
				maxmin: false,
				success: function(layero,index){
					setCookie('layer.parent.id',this_index);
				},
				end:function(layero,index){}
			});
		}

		function closethislayer(){
			top.layer.close(this_index);
		}
		
		//修改收货地址
		function returnAddressForm(customerId,returnMaterialId,quarter) {
		    var receiverAreaId = $("#receiverAreaId").val();
		    var receivor = $("#receivor").val();
		    var receivorPhone = $("#receivorPhone").val();
		    var receivorAddress = $("#receivorAddress").val();
            top.layer.open({
                type: 2,
                id:'layer_returnAddress',
                zIndex:19891016,
                title:'修改返件地址',
                content:"${ctx}sd/material/return/returnAddressForm?customerId="+ customerId + "&returnMaterialId="+returnMaterialId+"&quarter="+quarter + "&parentIndex=" + (parentLayerIndex || '')+"&receiverAreaId="+receiverAreaId+
                          "&receivor="+encodeURI(receivor) +"&receivorPhone="+receivorPhone+"&receivorAddress="+encodeURI(receivorAddress),
                area: ['800px', '500px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){

                }
            })
        }

        function setDate(data) {
            $("#receivorAddressText").text(data.areaName+data.subAreaName);
            $("#receivorText").text(data.receivor);
            $("#receivorPhoneText").text(data.receivorPhone);
            $("#receiverAreaId").val(data.receiverAreaId);
            $("#receivor").val(data.receivor);
            $("#receivorPhone").val(data.receivorPhone);
            $("#receivorAddress").val(data.subAreaName);
            $("#recevierInfoDiv").css("height","84px");
            $("#supportText").show();
            $("#recevierInfoBtn").text("修改");
        }

        function addUploadWarp(imgIndex,imagesUrl) {
            var strHtml = "<div class='upload_warp_img_div' id='divImg_"+imgIndex+ "'>" +
                "<a href=\"javascript:;\" title=\"点击删除图片\" onclick=\"delRow('"+imgIndex+"')\" class=\"upload_warp_img_div_del\"></a>" +
                "<img title='点击放大' src='${ctxUpload}/"+imagesUrl+"'  data-original='${ctxUpload}/"+imagesUrl+"'/>" + "</div>"
            $("#divImg_0").before(strHtml);
        }

        function imageViewer(){
            var viewer = $("#divUploadWarp").viewer('destroy').viewer(
                {
                    url: "data-original",
                    filter:function(image) {
                        if(image.src.lastIndexOf("/upload-photo.png")>0){
                            return false;
                        }
                        return true;
                    },
                    viewed: function(image) {
                    },
                    shown:function () {
                        if(this.viewer.index == -1){
                            this.viewer.hide();
                        }
                    }
                }
            );
        }

        //图片拖拽
        function bindImageDragEvent(){
            var _item = $("#divImg_0")[0];
            var $item = $(_item);
            _item.ondragover = function(ev) {
                $item.addClass("imgOnDarg");
                ev.preventDefault();
            };
            _item.ondragleave = function(ev){
                $item.removeClass("imgOnDarg");
                ev.preventDefault();
            };
            _item.ondrop = function(e) {
                e.preventDefault();
                var fs = e.dataTransfer.files;
                var len = fs.length; //获取文件个数
                if(len === 0){
                    layerInfo("请拖拽要上传的文件","系统提示");
                    $item.removeClass("imgOnDarg");
                    return false;
                }
                if(len > 1){
                    layerInfo("一次只能上传一个文件","系统提示");
                    $item.removeClass("imgOnDarg");
                    return false;
                }
                var _type = fs[0].type;
                if(!_type.match('image.*')){
                    layerInfo("文件不是图片文件!","系统提示");
                    $item.removeClass("imgOnDarg");
                    return false;
                }
                document.getElementById("upload_file").files = fs; // 有的浏览器会触发onchange事件,有的不会
                if(!isExecute){ // 部分浏览器 js给input赋值会触发onchange事件 所以用isExecute判断onchange是否执行了
                    $("#upload_file").change();
                }
            };
        }


        var confirmClickTag = 0;
        function delRow(i)
        {
            if (!i)
            {
                return false;
            }
            //var id = $("#id" + i).val();
            var attachmentId=i;
            if (id.length == 0){
                return false;
            }
            if(confirmClickTag == 1){
                return false;
            }
            confirmClickTag = 1;
            var data1 =
                {
                    attachmentId : attachmentId,
                    returnMaterialId : $("#id").val(),
                    quarter:$("#quarter").val()
                };
            $.ajax(
                {
                    cache : false,
                    type : "POST",
                    async : false,
                    url : "${ctx}/sd/material/return/deleteReturnMaterialAttachment",
                    data : data1,
                    beforeSend: function () {
                    },
                    complete: function () {
                    },
                    success : function(data)
                    {
                        confirmClickTag = 0;
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if (data)
                        {
                            if (data && data.success == true)
                            {
                                //$("#attach_tr_" + i).remove();
                                //refreshUploadButon();
                                $("#divImg_" + attachmentId).remove();
                                $("#divImg_0").show();
                            } else
                            {
                                layerError(" 删除失败，请重试!");
                            }
                        }
                        return false;
                    },
                    error : function(e)
                    {
                        confirmClickTag = 0;
                        ajaxLogout(e.responseText,null,"删除失败，请重试!");
                    }
                });
        }

	</script>
	<style type="text/css">
	  body {background-color:#fff;}
	  html {color:#000;}
	  .table thead th,.table tbody td {
		  text-align: center;
		  vertical-align: middle;
		  BackColor: Transparent;
		  height: 20px;
	  }
	  .form-horizontal{margin-top:10px}
	  .form-horizontal .control-label{width:140px}
	  .form-horizontal .controls{margin-left:100px}
	  .nav-tabs>li {margin-top: 5px;}
	  .fromInput {
		  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
	  }
	  .form-horizontal .control-group{border-bottom: none;margin-bottom: 8px;}
	  legend span {
		  border-bottom: #0096DA 4px solid;
		  padding-bottom: 6px;}
	  .upload_warp_img_div img{max-width:100%;max-height:100%;vertical-align:middle;width:100%;height: 100%;margin-bottom: 5px;}
	  .upload_warp_img_div {position: relative;height: 70px;width: 70px;border: 1px solid #ccc;float: left;
		  display: table-cell;text-align: center;background-color: #eee;cursor: pointer;margin-right: 10px;
	  }
	  .upload_warp{margin-top:8px;text-align: center;display: inline-block;}
	  .upload_warp_img_div .upload_warp_img_div_del{position:absolute;top:0px;width:20px !important;height:20px !important;right:0px;margin-top: 0px !important;
		  background-size: 20px 20px !important;background:url('${ctxStatic}/images/delUploadFile.png') no-repeat; }
	  .imgOnDarg{
		  border:2px dashed indianred;
		  background: bisque;
	  }
	  .praise_status_1{background-color: #0096DA;}
	  .praise_status_2{background-color: #FF9502;}
	  .praise_status_3{background-color: #10B9B9;}
	  .praise_status_4{background-color: #34C758;}
	  .praise_status_5{background-color: #f54142;}
	  .praise_status_6{background-color: #f54142;}
	  .praise_status_7{background-color: #468847;}
	</style>
</head>
<body style="display:inline">
	<sys:message content="${message}" />
	<c:if test="${canAction}">
	<fieldset style="width: 90%;margin-left: 50px">
	<form:form id="inputForm" modelAttribute="materialReturn" action="${ctx}/sd/material/return/saveExpress" method="post" class="form-horizontal">
		<form:hidden path="id" />
		<form:hidden path="orderId" />
		<form:hidden path="applyType.label" />
		<form:hidden path="applyType.value" />
		<form:hidden path="status.label" />
		<form:hidden path="quarter" />
		<input type="hidden" name="createDate" value="${fns:formatDate(materialReturn.createDate,"yyyy-MM-dd HH:mm:ss")}" />
		<form:hidden path="remarks" cssClass="form-horizontal" />
		<div class="row-fluid" style="margin-top: 12px">
			<div class="span12">
				<div class="control-group">
					<div class="controls">
							<%--<span style="margin-left: 570px">${praise.praiseNo}</span>&nbsp;&nbsp;<span class="praise_status_${praise.status}">${praise.strStatus}</span>--%>
						<div style="float: right">
							<span>${materialReturn.returnNo}</span>
							&nbsp;&nbsp;<span class="label praise_status_${materialReturn.status.value}">${materialReturn.status.label}</span>
						</div>
					</div>
				</div>
			</div>
		</div>
		<legend></legend>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">配件单号：</label>
					<div class="controls">
						<input class="fromInput" disabled="disabled" value="${formNo}" />
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">申请时间：</label>
					<div class="controls">
						<input class="fromInput" disabled="disabled" value="<fmt:formatDate value="${materialReturn.createDate}" pattern="yyyy-MM-dd HH:mm"/>" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">快递公司：</label>
					<div class="controls">
						<c:choose>
							<c:when test="${materialReturn.status.value eq '2'}">
								<form:select path="expressCompany.value" cssClass="input-medium required" cssStyle="width: 220px">
									<form:option value="" label="请选择" />
									<form:options items="${fns:getDictListFromMS('express_type')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%-- 切换为微服务 --%>
								</form:select>
								<input type="hidden" id="expressCompany.label" name="expressCompany.label" />
							</c:when>
							<c:otherwise>
								<form:input  path="expressCompany.label" disabled="true"></form:input>
							</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">快递单号：</label>
					<div class="controls">
						<c:choose>
							<c:when test="${materialReturn.status.value eq '2'}">
								<form:input path="expressNo" htmlEscape="false" cssClass="required" maxlength="50" />
							</c:when>
							<c:otherwise>
                            <label style="margin-top: 3px">
								<a href="http://www.kuaidi100.com/chaxun?com=${materialReturn.expressCompany.value}&nu=${materialReturn.expressNo }" target="_blank" title="点击进入快递100">
										${materialReturn.expressNo }
								</a>
                            </label>
							</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>
		</div>

		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">返件信息：</label>
					<div class="controls" style="margin-left: 138px;">
						<c:choose>
							<c:when test="${materialReturn.status.value==2}">
								<input id="receiverAreaId" name="receiverAreaId" style="display: none" value="${materialReturn.receiverAreaId}">
								<input id="receivor" name="receivor" style="display: none" value="${materialReturn.receivor}">
								<input id="receivorPhone" name="receivorPhone" style="display: none" value="${materialReturn.receivorPhone}">
								<input id="receivorAddress" name="receivorAddress" style="display: none" value="${materialReturn.receivorAddress}">
							   <c:choose>
                                   <c:when test="${empty materialReturn.receivor || empty materialReturn.receivorPhone || empty materialReturn.receivorAddress || empty materialReturn.receiverAreaId}">
									   <div style="width: 660px;height: auto;background-color: #F6F6F6;height: 46px" id="recevierInfoDiv">
										   <button class="btn btn-primary" type="button" onclick="returnAddressForm('${customerId}','${materialReturn.id}','${materialReturn.quarter}')" style="margin: 7px 0px 7px 12px">
											   <i class="icon-edit"></i> <span id="recevierInfoBtn">添加</span>
										   </button><br/>
										   <div style="padding-bottom: 5px;margin-left: 12px">
											   <span id="receivorText"></span>&nbsp;&nbsp;
											   <span id="receivorPhoneText"></span>&nbsp;&nbsp;
											   <span id="supportText" style="display: none">地址：</span>
											   <span id="receivorAddressText"></span>
										   </div>
									   </div>
								   </c:when>
								   <c:otherwise>
									   <div style="width: 660px;height: auto;background-color: #F6F6F6;height: 84px" id="recevierInfoDiv">
										   <button class="btn btn-primary" type="button" onclick="returnAddressForm('${customerId}','${materialReturn.id}','${materialReturn.quarter}')" style="margin: 7px 0px 7px 12px">
											   <i class="icon-edit"></i> <span id="recevierInfoBtn">修改</span>
										   </button><br/>
										   <div style="padding-bottom: 5px;margin-left: 12px">
											   <span id="receivorText">${materialReturn.receivor}</span>&nbsp;&nbsp;
											   <span id="receivorPhoneText">${materialReturn.receivorPhone}</span>&nbsp;&nbsp;
											   地址：
											   <span id="receivorAddressText">${receiverAreaName}${materialReturn.receivorAddress}</span>
										   </div>
									   </div>
								   </c:otherwise>
							   </c:choose>
							</c:when>
							<c:otherwise>
								<c:set var="receivorInfo" value="${materialReturn.receivor}  ${materialReturn.receivorPhone}  地址:${receiverAreaName}${materialReturn.receivorAddress}"/>
								<textarea class="fromInput" disabled style="width: 645px" rows="3">${receivorInfo}</textarea>
							</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">配件照片：</label>
					<div class="controls">
						<div class="upload_warp" id="divUploadWarp">
							<c:forEach items="${materialReturn.attachments}" var="attach" varStatus="i" begin="0">
								<div class="upload_warp_img_div" id="divImg_${attach.id}">
									<c:if test="${empty materialReturn.expressNo && materialReturn.status.value eq '2'}">
									   <a href="javascript:" title="点击删除图片" onclick="delRow('${attach.id}')" class="upload_warp_img_div_del"></a>
									</c:if>
									<img title='点击放大' src='${ctxUpload}/${attach.filePath}'  data-original='${ctxUpload}/${attach.filePath}'/>
								</div>
							</c:forEach>
							<c:if test="${ empty materialReturn.expressNo && materialReturn.status.value eq '2'}">
							<div class="upload_warp_img_div" id="divImg_0">
								<img title="点击上传图片" src="${ctxStatic}/images/upload-photo.png" />
							</div>
							<input name="logo" id="logo" type="hidden"  htmlEscape="false" />
							<input name="orignalName" id="orignalName" type="hidden" htmlEscape="false" />
							<input id="upload_file" name="upload_file" type="file" style="display: none" accept="image/gif,image/jpeg,image/png" onchange="uploadFileChange()">
							</c:if>
						</div>
					</div>
				</div>
			</div>
		</div>
		<legend style="margin-top: 20px"><span>配件信息</span></legend>
		<c:if test="${materialReturn.items != null and materialReturn.items.size()>0}">
			<table id="tbMaterial" width="100%" border="0" class="table table-striped table-bordered table-condensed" style="margin-bottom: 0px;">
				<thead>
				<tr>
					<th width="60px">序号</th>
					<th>配件</th>
					<th width="80px">数量</th>
				</tr>
				</thead>
				<tbody>
				<c:forEach items="${materialReturn.items}" var="item" varStatus="i" begin="0">
					<c:set var="index" value="${i.index+1}"></c:set>
				<tr>
					<td>${index}</td>
					<td>${item.material.name}</td>
					<td>${item.qty}</td>
				</tr>
				</c:forEach>
				<tbody>
			</table>
		</c:if>
	</form:form>
	</fieldset>
	</c:if>
	<div style="height: 60px;width: 100%"></div>
	<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
		<hr style="margin: 0px;"/>
		<div style="float: right;margin-top: 10px;">
			<c:if test="${ empty materialReturn.expressNo && materialReturn.status.value eq '2'}">
			   <input id="btnSubmit" class="btn btn-primary" type="button" style="margin-right: 5px;width: 96px;height: 40px" value="保 存" />
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="<c:out value="${ empty materialReturn.expressNo && materialReturn.status.value eq '2'?'取 消':'关 闭'}"/>" style="width: 96px;height: 40px;margin-right: 15px" onclick="closethislayer()"/>
		</div>
	</div>
</body>
</html>