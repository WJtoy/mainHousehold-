<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>配件申请附件图片查看</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
	<link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
	<meta name="decorator" content="default" />
	<script type="text/javascript">
        $(document).ready(function(){
            $('#viewImg').viewer();
        });
	</script>
<style type="text/css">
.form-horizontal{margin-top:5px;}
.form-horizontal .control-label {width: 100px;margin-top: 20px;}
.form-horizontal .controls {margin-left: 110px;}
.table thead th,.table tbody td {
	text-align: center;
	vertical-align: middle;
	BackColor: Transparent;
}
legend span {
	border-bottom: #0096DA 4px solid;
	padding-bottom: 6px;
}

.upload_warp_img_div img{max-width:100%;max-height:100%;vertical-align:middle;width:100%;height: 100%;margin-bottom: 5px;}
.upload_warp_img_div {position: relative;height: 120px;width: 120px;border: 1px solid #ccc;float: left;
	display: table-cell;text-align: center;background-color: #eee;cursor: pointer;
}
.upload_warp_text{text-align:left;margin-top:10px;text-indent:14px;font-size:14px}
.upload_date{text-align:left;margin-top:10px;text-indent:14px;font-size:14px}
.upload_warp_left img{margin-top:0px}
.upload_warp_left {float: left;width: 120px;height: 180px;border: 1px dashed #999;border-radius: 4px;cursor: pointer;
	margin-right: 10px;padding: 5px;
}
.upload_warp{margin:8px;text-align: center;display: inline-block;}
.fromInput {
	border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;margin-top: 19px;
}
</style>
</head>
<body>
<sys:message content="${message}"/>
<c:if test="${canAction}">
	<div style="width: 90%;margin-left: 5%;margin-top: 12px" id="viewImg" class="form-horizontal">
		<c:choose>
			<c:when test="${attachments !=null && attachments.size()>0}">
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">配件照片：</label>
						<div class="controls">
							<c:forEach items="${attachments}" var="attachment">
								<img alt="" title="${attachment.remarks}" src="${ctxUpload}/${attachment.filePath}" style="width: 120px;height: 120px;padding: 16px 16px 16px 0px">
							</c:forEach>
						</div>
				</div>
			</div>
			</c:when>
			<c:otherwise>
				<div align="center">
					无配件照片
				</div>
			</c:otherwise>
		</c:choose>
		<c:if test="${itemCompleteList !=null && itemCompleteList.size() >0}">
		     <legend style="margin-bottom: 0px"><span>完工图片</span></legend>
			<c:forEach items="${itemCompleteList}" var="item" varStatus="i">
				<div class="row-fluid">
					<div class="span6">
						<div class="control-group">
							<label class="control-label">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
							<div class="controls">
								<input class="fromInput"  value="${item.product.name}" maxlength="30" readonly/>
							</div>
						</div>
					</div>
					<div class="span6">
						<div class="control-group">
							<label class="control-label">产品条码：</label>
							<div class="controls">
								<input class="fromInput" value="${item.unitBarcode}" maxlength="30" readonly/>&nbsp;
							</div>
						</div>
					</div>
				</div>
				<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label" style="margin-top: 2px;">完成图片：</label>
						<div class="controls">
							<%--<div class="upload_warp">--%>
								<c:forEach items="${item.itemList}" var = "pic">
									<div class="upload_warp_left">
										<div class="upload_warp_img_div">
											<img id="logo_image${pic.pictureCode}${i.index}" title="点击放大图片" src="${ctxUpload}/${pic.url}" data-original="${ctxUpload}/${pic.url}"/>
											<a class="upload_warp_text" href="javascript:void(0);" data-toggle="tooltip" title="${pic.title}">${fns:abbr(pic.title,14)}</a>
											<br/>
											<a class="upload_date" href="javascript:void(0);"><fmt:formatDate value="${pic.uploadDate}" pattern="yyyy-MM-dd HH:mm"/></a>
										</div>
									</div>
								</c:forEach>
							<%--</div>--%>
						     </div>
					     </div>
				      </div>
				  </div>
			    <div class="row-fluid">
			</c:forEach>
		</c:if>
	</div>
</c:if>
</body>
</html>