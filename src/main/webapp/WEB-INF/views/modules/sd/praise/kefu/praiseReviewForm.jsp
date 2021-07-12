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
    <title>好评单审核(客服)</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		var imgIndex = 0;
        var this_index = top.layer.index;
        var clickTag = 0;
        var maxUploadSize = 5; //最大上传数量
		var uploadCount = 0; //当前已经上传的数量
		$(document).ready(function() {
            imageViewer();
			if(${praise.id!=null && praise.pics!=null}){
                imgIndex = ${fn:length(praise.pics)}
				uploadCount = ${fn:length(praise.pics)}
				if(uploadCount>=maxUploadSize){
                    $("#divImg_0").hide();
				}
			}
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

        //通过好评单审核
		function praisePass() {
            var id = $("#id").val();
            var quarter = $("#quarter").val();
            var orderId = $("#cancelled").val();

            var loadingIndex = layerLoading('正在提交，请稍候...');
            var $btnSubmit = $("#btnSubmit");
            if ($btnSubmit.prop("disabled") == true) {
                event.preventDefault();
                return false;
            }
            $btnSubmit.prop("disabled", true);
            $.ajax({
                url:"${ctx}/praise/orderPraise/approve",
                type:"POST",
                data:{id:id,quarter:quarter,orderId:orderId,customerId:$("#customerId").val()},
                dataType:"json",
				complete: function () {
					if(loadingIndex) {
						layer.close(loadingIndex);
					}
				},
                success: function(data){
                    //提交后的回调函数
                    if(ajaxLogout(data)){
                        setTimeout(function () {
                            $btnSubmit.removeAttr('disabled');
                        }, 2000);
                        return false;
                    }
                    if (data.success) {
                        layer.close(loadingIndex);
                        layerMsg("好评单审核成功！");
                        top.layer.close(this_index);//关闭本身
                         var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                         if(pframe){
                             pframe.repage();
                         }
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
        
        //驳回
		function reject(){
			var id = $("#id").val();
			var quarter = $("#quarter").val();
            var rejectionCategory = $("input[name='rejectionCategory']:checked").val();
            var orderId = $("#orderId").val();
            if(rejectionCategory==null || rejectionCategory==''){
                layerMsg("请选中驳回原因");
                return false
			}
            var loadingIndex = layerLoading('正在提交，请稍候...');
            var $btnSubmit = $("#reject");
            if ($btnSubmit.prop("disabled") == true) {
                event.preventDefault();
                return false;
            }
            $btnSubmit.prop("disabled", true);

            $.ajax({
                url:"${ctx}/praise/orderPraise/reject",
                type:"POST",
                data:{id:id,quarter:quarter,rejectionCategory:rejectionCategory,orderId:orderId,remarks:$("#remarks").val()},
                dataType:"json",
				complete: function () {
					if(loadingIndex) {
						layer.close(loadingIndex);
					}
				},
                success: function(data){
                    //提交后的回调函数
                    if(ajaxLogout(data)){
                        setTimeout(function () {
                            $btnSubmit.removeAttr('disabled');
                        }, 2000);
                        return false;
                    }
                    if (data.success) {
                        layerMsg("好评单驳回成功!");
                        top.layer.close(this_index);//关闭本身
                         var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                         if(pframe){
                             pframe.repage();
                         }

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

		//取消好评单
		function closePraise() {
            var rejectionCategory = $("input[name='rejectionCategory']:checked").val();
            if(rejectionCategory==null || rejectionCategory==''){
                layerMsg("请选择驳回原因");
                return false
            }
            top.layer.confirm('取消后不能重新申请好评费,确定取消吗', {icon: 3, title:'系统确认'}, function(index){
                var id = $("#id").val();
                var quarter = $("#quarter").val();
                var orderId = $("#orderId").val();
                var loadingIndex = layerLoading('正在提交，请稍候...');
                var $btnSubmit = $("#close");
                if ($btnSubmit.prop("disabled") == true) {
                    event.preventDefault();
                    return false;
                }
                $btnSubmit.prop("disabled", true);

                $.ajax({
                    url:"${ctx}/praise/orderPraise/cancelled",
                    type:"POST",
                    data:{id:id,quarter:quarter,rejectionCategory:rejectionCategory,remarks:$("#remarks").val(),orderId:orderId},
                    dataType:"json",
					complete: function () {
						if(loadingIndex) {
							layer.close(loadingIndex);
						}
					},
                    success: function(data){
                        //提交后的回调函数
                        if(ajaxLogout(data)){
                            setTimeout(function () {
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            return false;
                        }
                        if (data.success) {
                            layerMsg("好评单取消成功！");
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
			});
        }

	</script>
	  <style type="text/css">
		  .upload_warp_img_div img{max-width:100%;max-height:100%;vertical-align:middle;width:100%;height: 100%;margin-bottom: 5px;}
		  .upload_warp_img_div {position: relative;height: 100px;width: 100px;border: 1px solid #ccc;float: left;
			  display: table-cell;text-align: center;background-color: #eee;cursor: pointer;margin-right: 10px;
		  }
		  .upload_warp{margin:8px;text-align: center;display: inline-block;}
		  .upload_warp_img_div .upload_warp_img_div_del{position:absolute;top:0px;width:20px !important;height:20px !important;right:0px;margin-top: 0px !important;
			  background-size: 20px 20px !important;background:url('${ctxStatic}/images/delUploadFile.png') no-repeat; }
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
		  .form-horizontal {margin-top: 5px;}
		  .form-horizontal .control-label {width: 80px;}
		  .form-horizontal .controls {margin-left: 90px;}
          .imgOnDarg{
              border:2px dashed indianred;
              background: bisque;
          }
		  .praise_status_10{background-color: #0096DA;color: white;padding: 2px 4px;border-radius: 3px}
		  .praise_status_20{background-color: #0096DA;color: white;padding: 2px 4px;border-radius: 3px}
		  .praise_status_30{background-color: #FF3A30;color: white;padding: 2px 4px;border-radius: 3px}
		  .praise_status_40{background-color: #34C758;color: white;padding: 2px 4px;border-radius: 3px}
		  .praise_status_50{background-color: #AEAEB2;color: white;padding: 2px 4px;border-radius: 3px}
		  .common{height: 30px;border-radius: 4px;color: white;border: 1px solid rgba(255, 255, 255, 0);}
		  .reject_btn{background-color: rgba(255, 58, 48, 1);}
		  .cance_btn{background-color: rgba(255, 149, 2, 1);}
		  .common:active{
			  border: 1px solid #0096DA;
		  }
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
			<form:hidden path="customerId"/>
            <form:hidden path="customerPraiseFee"/>
			<div class="row-fluid" style="margin-top: 7px">
				<div class="span12">
					<div class="control-group">
						<div class="controls">
							<%--<span style="margin-left: 570px">${praise.praiseNo}</span>&nbsp;&nbsp;<span class="praise_status_${praise.status}">${praise.strStatus}</span>--%>
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
						<label class="control-label">工单号:</label>
						<div class="controls">
							<form:input  path="orderNo" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">销售单号:</label>
						<div class="controls">
							<form:input  path="parentBizOrderId" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">服务单号:</label>
						<div class="controls">
							<form:input  path="workcardId" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">客户:</label>
						<div class="controls">
							<form:input  path="customerName" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">用户姓名:</label>
						<div class="controls">
							<form:input  path="userName" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">用户电话:</label>
						<div class="controls">
							<form:input  path="userPhone" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">用户地址:</label>
						<div class="controls">
							<form:input  path="userAddress" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
			</div>
			<legend style="margin-top: 20px"><span>申请</span></legend>
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">产品:</label>
						<div class="controls">
							<form:input  path="productNames" readonly="true" cssClass="input-block-level"></form:input>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">好评图片:</label>
						<div class="controls">
							<div class="input-block-level" style="border:1px solid #BBBBBB;border-radius:5px;height: 120px">
								<div class="upload_warp" id="divUploadWarp">
									<%--<c:if test="${praise.pics!=null && fn:length(praise.pics) >0}">
										<c:forEach items="${praise.pics}" var="pic" varStatus="picIndex">
											<div class="upload_warp_img_div" id="divImg_${picIndex.index + 1}">
												<img title='点击放大' src='${ctxUpload}/${pic}'  data-original='${ctxUpload}/${pic}'/>
											</div>
										</c:forEach>
									</c:if>--%>
									<c:if test="${praise.picItems!=null && fn:length(praise.picItems) >0}">
										<c:forEach items="${praise.picItems}" var="picItems" varStatus="picIndex">
											<div class="upload_warp_img_div">
												<img title='点击放大' src='${ctxUpload}/${picItems.url}'  data-original='${ctxUpload}/${picItems.url}'/>
												<div style="position:absolute;width:100px;z-indent:2;left:0;bottom:0;color: white;background-color: #333333;opacity: 0.6">${picItems.name}</div>
											</div>
										</c:forEach>
									</c:if>
								</div>
							</div>
							<span style="margin-top: 5px;width: 60px">格式要求:</span>
							<span style="color: red">支持jpg,jpeg,png格式,且大小不能超过2MB,建议尺寸600*800</span><br/>
							<c:if test="${customerPraiseFee.praiseRequirement!=null && customerPraiseFee.praiseRequirement!=''}">
								<label class="control-label" style="padding-top: 1px;width: 60px">内容要求:</label>
								<div style="color: red;margin-left: 65px">
										${customerPraiseFee.praiseRequirement}
								</div>
							</c:if>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">好评费:</label>
						<div class="controls">
							<form:input  path="applyServicepointPraiseFee" readonly="true" cssClass="input-block-level" cssStyle="width: 30%; display:inline;" maxlength="6"></form:input>
							<%--<form:input  path="applyCustomerPraiseFee" cssClass="input-block-level" readonly="true" cssStyle="width: 30%; display:inline;" maxlength="6"></form:input>--%>
						</div>
					</div>
				</div>
			</div>
			<legend style="margin-top: 20px"><span>审核</span></legend>
			 <c:choose>
				 <c:when test="${canSave==true}">
					 <div class="row-fluid">
						 <div class="span12">
							 <div class="control-group">
								 <label class="control-label">驳回原因:</label>
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
								 <label class="control-label">异常描述:</label>
								 <div class="controls">
									 <textarea name="remarks" id="remarks" rows="5" maxlength="255" style="height:56px" class="input-block-level"></textarea>
								 </div>
							 </div>
						 </div>
					 </div>
				 </c:when>
				 <c:otherwise>
					 <div class="row-fluid">
						 <div class="span12">
							 <div class="control-group">
								 <label class="control-label">审核内容:</label>
								 <div class="controls">
									 <form:textarea path="remarks" htmlEscape="false" rows="5" maxlength="255" cssClass="input-block-level" cssStyle="height:56px" readonly="true"/><br/>
								 </div>
							 </div>
						 </div>
					 </div>
				 </c:otherwise>
			</c:choose>
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
		<shiro:hasPermission name="sd:praise:kefu:review">
			<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
				<hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
				<c:choose>
					<c:when test="${canSave==true}">
						<input id="btnSubmit" class="btn btn-primary" type="button" onclick="praisePass()" value="通 过" style="margin-left: 550px;margin-top: 10px"/>&nbsp;&nbsp;
						<input id="reject" class="common reject_btn" type="button" onclick="reject()" value="驳 回" style="margin-top: 10px;width: 82px"/>&nbsp;&nbsp;
						<input id="close" class="common cance_btn" type="button" onclick="closePraise()" value="取消好评" style="margin-top: 10px;width: 82px"/>&nbsp;&nbsp;
						<input id="btnCancel" class="btn" type="button" value="关 闭" style="margin-top:10px"onclick="cancel()"/>
					</c:when>
					<c:otherwise>
						<input id="btnCancel" class="btn" type="button" value="关 闭" style="margin-left:780px;margin-top:10px"onclick="cancel()"/>
					</c:otherwise>
				</c:choose>
			</div>
		</shiro:hasPermission>
	</c:if>
  </body>
</html>
