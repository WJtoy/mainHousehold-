<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
	<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <title>无效好评</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        //end
        var isExecute = false;
		var imgIndex = 0;
        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
            imageViewer();
		});

		//取消
		function cancel() {
            top.layer.close(this_index);//关闭本身
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
                        // console.log(this.viewer);
                        if(this.viewer.index == -1){
                            this.viewer.hide();
                            //$(".viewer-container").removeClass("viewer-in").addClass("viewer-hide");
                        }
                    }
                }
            );
        }

		//无效好评
        function invalidPraise() {
            var rejectionCategory = $("input[name='rejectionCategory']:checked").val();
            if(rejectionCategory==null || rejectionCategory==''){
                layerMsg("请选中驳回原因");
                return false
            }
            var remarks = $("#remarks").val();
            var id = $("#id").val();
            var quarter = $("#quarter").val();
            var orderId = $("#orderId").val();
            var servicePointId = $("#servicepointId").val();
            var engineerId = $("#engineerId").val();
            var orderNo = $("#orderNo").val();
            var loadingIndex = layerLoading('正在提交，请稍候...');
            var $btnSubmit = $("#invalidSubmit");
            if ($btnSubmit.prop("disabled") == true) {
                event.preventDefault();
                return false;
            }
            $btnSubmit.prop("disabled", true);

            $.ajax({
                url:"${ctx}/praise/orderPraise/invalidPraise",
                type:"POST",
                data:{id:id,quarter:quarter,orderId:orderId,remarks:remarks,rejectionCategory:rejectionCategory,
                      servicepointId:servicePointId,engineerId:engineerId,orderNo:orderNo},
                dataType:"json",
                success: function(data){
                    //提交后的回调函数
                    if(loadingIndex) {
                        layer.close(loadingIndex);
                    }
                    if(ajaxLogout(data)){
                        setTimeout(function () {
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
                        $btnSubmit.removeAttr('disabled');
                    }, 2000);
                    ajaxLogout(data,null,"数据保存错误，请重试!");
                    //var msg = eval(data);
                },
                timeout: 50000               //限制请求的时间，当请求大于5秒后，跳出请求
            });
        }

	</script>
	  <style type="text/css">
		  .upload_warp_img_div img{max-width:100%;max-height:100%;vertical-align:middle;width:100%;height: 100%;margin-bottom: 5px;}
		  .upload_warp_img_div {position: relative;height: 100px;width: 100px;border: 1px solid #ccc;float: left;
			  display: table-cell;text-align: center;background-color: #eee;cursor: pointer;margin-right: 10px;
		  }
		  .upload_warp{text-align: center;display: inline-block;}
		  .upload_warp_img_div .upload_warp_img_div_del{position:absolute;top:0px;width:20px !important;height:20px !important;right:0px;margin-top: 0px !important;
			  background-size: 20px 20px !important;background:url('${ctxStatic}/images/delUploadFile.png') no-repeat; }
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
		  .upload_warp_left img{margin-top:0px}
		  .upload_warp_left {float: left;width: 100px;height: 130px;border: 1px dashed #999;border-radius: 4px;cursor: pointer;
			  margin-right: 10px;padding: 5px;
		  }
		  .form-horizontal {margin-top: 5px;}
		  .form-horizontal .control-label {width: 80px;}
		  .form-horizontal .controls {margin-left: 90px;}
          .imgOnDarg{
              border:2px dashed indianred;
              background: bisque;
          }
		  .common{height: 30px;border-radius: 4px;color: white;border: 1px solid rgba(255, 255, 255, 0);}
		  .cance_btn{background-color: rgba(255, 149, 2, 1);}
		  .over_time{background-color: #999999}
		  .common:active{
			  border: 1px solid #0096DA;
		  }
		  .common.over_time:disabled{
			  background-color: #999999;
		  }
		  .praise_status_40{background-color: #0096DA;color: white;padding: 2px 4px;border-radius: 3px}
		  .praise_status_60{background-color: #AEAEB2;color: white;padding: 2px 4px;border-radius: 3px}
		  legend span {
			  border-bottom: #0096DA 4px solid;
			  padding-bottom: 6px;}
	  </style>
  </head>
  
  <body>
    <sys:message content="${message}"/>
	<c:if test="${canAction==true}">
		<input type="hidden" id="mixPicCount" value="${customerPraiseFee.picCount}">
		<form:form id="inputForm" modelAttribute="praise" action="" method="post" class="form-horizontal">
			<form:hidden path="id"/>
			<form:hidden path="orderId"/>
			<form:hidden path="quarter"/>
			<form:hidden path="servicepointId"/>
			<form:hidden path="engineerId"/>
			<form:hidden path="orderNo"/>
			<div class="row-fluid" style="margin-top: 7px">
				<div class="span12">
					<div class="control-group">
						<div class="controls">
							<%--<span style="margin-left: 570px">${praise.praiseNo}</span>&nbsp;&nbsp;<span class="praise_status_${praise.status}" style="color: white;padding: 2px 4px;border-radius: 3px">${praise.strStatus}</span>--%>
							<div style="float: right">
								<span>${praise.praiseNo}</span>
								&nbsp;&nbsp;<span class="praise_status_${praise.status}">${praise.strStatus}</span>
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">工单单号：</label>
						<div class="controls">
							<form:input  path="orderNo" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
						<div class="controls">
							<form:input  path="customerName" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">网点编号：</label>
						<div class="controls">
							<form:input  path="servicePointNo" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">网点电话：</label>
						<div class="controls">
							<form:input  path="servicePointPhone" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">用户姓名：</label>
						<div class="controls">
							<form:input  path="userName" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">用户电话：</label>
						<div class="controls">
							<form:input  path="userPhone" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">用户地址：</label>
						<div class="controls">
							<form:input  path="userAddress" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
			</div>
			<legend style="margin-top: 20px"><span>申请</span></legend>
			<%--<c:if test="${praise.status==40 && praise.overtimeFlag ==1}">
				<div class="row-fluid">
					<div class="control-group">
						<div class="controls" style="margin-left: 20px;">
							<div class="input-block-level" style=" border:1px solid;border-radius:3px;height: 30px; line-height:30px;vertical-align:middle;border-color: red;background-color: rgb(255, 250, 250)">
								<i class="icon-question-sign" style="color: red;margin-left: 20px"></i>
								<span style="color: red">审核时间超过24小时,不可进行操作</span>
							</div>
						</div>
					</div>
				</div>
			</c:if>--%>
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
						<div class="controls">
							<form:input  path="productNames" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">好评图片：</label>
						<div class="controls">
							<%--<div class="input-block-level" style="border:1px solid #BBBBBB;border-radius:5px;height: 120px;">--%>
							<div class="upload_warp" id="divUploadWarp">
								<c:if test="${praise.picItems!=null && fn:length(praise.picItems) >0}">
									<c:forEach items="${praise.picItems}" var="picItems" varStatus="picIndex">
										<div class="upload_warp_left">
											<div class="upload_warp_img_div">
												<img title="点击放大图片"  src="${ctxUpload}/${picItems.url}" data-original='${ctxUpload}/${picItems.url}'/>
												<div style="width:100px;z-indent:2;left:0;bottom:0;">
														${picItems.name}
												</div>
											</div>
										</div>
									</c:forEach>
								</c:if>
							</div>
							<%--</div>--%>
						</div>
					</div>
				</div>
			</div>
			<c:if test="${praise.overtimeFlag == 0 && praise.status==40}">
				<legend style="margin-top: 20px"><span>审核</span></legend>
				<div class="row-fluid">
					<div class="span12">
						<div class="control-group">
							<label class="control-label">驳回原因：</label>
							<div class="controls">
								<c:forEach items="${fns:getDictListFromMS('praise_abnormal_type')}" var="dict">
									<input type="radio" id="rejectionCategory${dict.value}" name="rejectionCategory" value="${dict.value}">
									<label for="rejectionCategory${dict.value}">${dict.label}</label>&nbsp;&nbsp;
								</c:forEach>
							</div>
						</div>
					</div>
				</div>
				<div class="row-fluid">
					<div class="span12">
						<div class="control-group">
							<label class="control-label">描&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;述：</label>
							<div class="controls">
								<textarea name="remarks" id="remarks" rows="5" maxlength="255" style="height:56px" class="input-block-level"></textarea>
							</div>
						</div>
					</div>
				</div>
			</c:if>
			<legend><span>处理进度</span></legend>
			<div class="row-fluid">
				<div class="span12">
					<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
						<thead>
						<tr>
							<th width="20">序号</th>
							<th width="200">操作人</th>
							<th>操作时间</th>
							<th>内容</th>
						</tr>
						</thead>
						<tbody>
						<c:set var="rowcnt" value="${praiseLogModelList.size()}"/>
						<c:forEach items="${praiseLogModelList}" var="model">
							<c:set var="rowNumber" value="${rowNumber+1}"/>
							<tr>
								<td>${rowNumber}</td>
								<td>${model.createName}</td>
								<td>${model.strCreateDate}</td>
								<td>${model.content}</td>
							</tr>
						</c:forEach>
						</tbody>
					</table>
				</div>
			</div>
		</form:form>
		<div style="height: 60px;width: 100%"></div>
			<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
				<hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
				<c:choose>
					<c:when test="${praise.status==40}">
						<%--<c:choose>
							<c:when test="${praise.overtimeFlag == 0}">
								<input id="invalidSubmit" class="common cance_btn" type="button" value="无效好评" onclick="invalidPraise()" style="margin-left: 80%;margin-top: 10px;width: 82px"/>
							</c:when>
							<c:otherwise>
								<input id="invalidSubmit" class="common over_time" type="button" value="无效好评" onclick="invalidPraise()" disabled="disabled" style="margin-left: 80%;margin-top: 10px;width: 82px"/>
							</c:otherwise>
						</c:choose>--%>
						<input id="invalidSubmit" class="common cance_btn" type="button" value="无效好评" onclick="invalidPraise()" style="margin-left: 80%;margin-top: 10px;width: 82px"/>
						&nbsp;&nbsp;<input id="btnCancel" class="btn" type="button" value="关 闭" style="margin-top:10px"onclick="cancel()"/>
					</c:when>
					<c:otherwise>
						<input id="btnCancel" class="btn" type="button" value="关 闭" style="margin-top:10px;margin-left: 90%"onclick="cancel()"/>
					</c:otherwise>
				</c:choose>
			</div>
	</c:if>
  </body>
</html>
