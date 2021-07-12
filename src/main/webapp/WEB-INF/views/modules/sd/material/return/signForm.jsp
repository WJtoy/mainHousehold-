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

        var parentLayerIndex = parent.layer.getFrameIndex(window.name);
		Order.rootUrl = "${ctx}";
		var this_index = top.layer.index;
		var clickTag = 0;
        var $btnSubmit = $("#btnSubmit");
		$(document).ready(function() {
            $(document).ready(function(){
                $('#viewImg').viewer();
            });
			$("#btnSubmit").click(function(event){
                if (clickTag == 1){
                    return false;
                }
                var signType = $("input[name='signType']:checked").val();
                if(signType==2 && Utils.isEmpty($("#signRemark").val())){
                    clickTag = 0;
                    layerAlert('请输入签收说明!');
                    return false;
				}
                clickTag = 1;
				$btnSubmit.attr("disabled", "disabled");
			    var loadingIndex;
			    var ajaxSucess = 0;
				$.ajax({
					type: "POST",
					url: "${ctx}/sd/material/return/saveSign",
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
                           top.layer.close(this_index);
				    	   //刷新父窗口
                           var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                           if(iframe != undefined){
                               iframe.resetPage();
                           }else{
                               layerMsg('签收成功,请手动关闭此页面!');
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

        });

		function closethislayer(){
			top.layer.close(this_index);
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
	</style>
</head>
<body style="display:inline">
	<sys:message content="${message}" />
	<c:if test="${canAction}">
	<fieldset style="width: 90%;margin-left: 50px">
	<form:form id="inputForm" modelAttribute="materialReturn" action="${ctx}/sd/material/return/saveSign" method="post" class="form-horizontal">
		<form:hidden path="id" />
		<form:hidden path="orderId" />
		<form:hidden path="quarter" />
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
					<label class="control-label">快递公司：</label>
					<div class="controls">
						<form:input  path="expressCompany.label" disabled="true"></form:input>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">快递单号：</label>
					<div class="controls">
						<label style="margin-top: 3px">
							<a href="http://www.kuaidi100.com/chaxun?com=${materialReturn.expressCompany.value}&nu=${materialReturn.expressNo }" target="_blank" title="点击进入快递100">
									${materialReturn.expressNo }
							</a>
						</label>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">签收类型：</label>
					<div class="controls">
						<input type="radio" id="signType_1" name="signType" value="1" checked><label for="signType_1">正常签收</label>
						<input type="radio" id="signType_2" name="signType" value="2"><label for="signType_2">异常签收</label>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">签收说明：</label>
					<div class="controls">
						<textarea class="fromInput"  id="signRemark" name="signRemark" style="width: 645px" rows="3" maxlength="150"></textarea>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">配件照片：</label>
					<div class="controls" id="viewImg">
						<div class="upload_warp" id="divUploadWarp">
							<c:forEach items="${materialReturn.attachments}" var="attach" varStatus="i" begin="0">
								<div class="upload_warp_img_div" id="divImg_${attach.id}">
									<img title='点击放大' src='${ctxUpload}/${attach.filePath}'  data-original='${ctxUpload}/${attach.filePath}'/>
								</div>
							</c:forEach>
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
			<input id="btnSubmit" class="btn btn-primary" type="button" style="margin-right: 5px;width: 96px;height: 40px" value="保 存" />
			<input id="btnCancel" class="btn" type="button" value="取 消" style="width: 96px;height: 40px;margin-right: 15px" onclick="closethislayer()"/>
		</div>
	</div>
</body>
</html>