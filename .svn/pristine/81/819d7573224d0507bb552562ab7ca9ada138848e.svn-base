<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
	<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
	<link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <title>好评图片</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
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
        var isExecute = false;
        var this_index = top.layer.getFrameIndex(window.name);
        var clickTag = 0;
		$(document).ready(function() {
			if(${canAction==true && canSave==true}){
                bindImageDragEvent();
			}
			$("#examplePic").viewer({url:'data-original'});
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
        //图片拖拽
        function bindImageDragEvent(){
            //拖拽事件
            $('div.upload_warp_img_div').each(function(index, item) {
                var $item = $(item);
                item.ondragover = function(ev) {
                    if($item.hasClass("drag")){
                        $item.addClass("imgOnDarg");
                    }
                    ev.preventDefault();
                };
                // item.ondragenter = function(ev) {
                //     ev.preventDefault();
                // };
                item.ondragleave = function(ev){
                    if($item.hasClass("drag")){
                        $item.removeClass("imgOnDarg");
                    }
                    ev.preventDefault();
                };
                item.ondrop = function(e) {
                    e.preventDefault();
                    if(!$item.hasClass("drag")){
                        return false;
                    }
                    var index = $item.data("index");
                    var fs=e.dataTransfer.files;
                    var len=fs.length; //获取文件个数
                    if(len == 0){
                        layerInfo("请拖拽要上传的文件","系统提示");
                        return false;
                    }
                    var _type=fs[0].type;
                    if(!_type.match('image.*')){
                        layerInfo("文件不是图片文件!","系统提示");
                        return false;
                    }
                    //console.log(fs[0].name);
                    document.getElementById("upload_file_"+index).files = fs; // 有的浏览器会触发onchange事件,有的不会
                    if(!isExecute){ // 部分浏览器 js给input赋值会触发onchange事件 所以用isExecute判断onchange是否执行了
                        $("#upload_file_" + index).change();
                    }
                };
            });
        }
        //好评费 v2.0
	    function clickFile(id){
            $("#upload_file_"+id).click();
            return false;
        }
        //图片上传
		function uploadFileChange(index){
          isExecute = true;
          var files = document.getElementById("upload_file_" + index).files;
          if(files == null || files.size==0){
             isExecute = false;
             return false;
          }
          var fileSize = files[0].size;
          var size = fileSize / 1024;
          if(size > 2000){
             layerInfo("单个文件不能大于2M","系统提示");
             isExecute = false;
             $file =$("#upload_file");
             $file.val("");
             $file.after($file.clone());
             $file.remove();
             return false;
          }
          uploadfile("upload_file_"+index,index);
     }

        function uploadfile(fileInputId,index) {
            var loadingIndex = top.layer.msg('正在提交，请稍等...', {
                icon: 16,
                time: 0,
                shade: 0.3
            });
            $.ajaxFileUpload({
                url: '${pageContext.request.contextPath}/servlet/Upload?' + (new Date()).getTime(),
                secureuri: false,
                data: {},
                fileElementId: fileInputId,//file控件id
                dataType: 'json',
                success: function (data, status) {
                    if(data && data.status === 'false'){
                        layerError("文件上传失败，请重试!","错误",true);
                        isExecute = false;
                    }else {
                        var $img = $("[id='viewImg_" + index +"']");
                        $img.attr("src","${ctxUpload}/" + data.fileName);
                        $img.attr("data-original","${ctxUpload}/" +data.fileName);
                        $img.attr("title","点击放大图片");
                        $img.before("<a href='javascript:;' title='点击删除图片'" + " onclick=\"deletePic('" + index + "')\" class=\"upload_warp_img_div_del\"></a>");
                        $("#divImg_" + index).removeClass("drag").removeClass("imgOnDarg");
                        $("#divImg_" + index).removeAttr("onclick");
                        $("#upload_file_"+index).val("");
                        $("#pic_info_"+index).val(data.fileName);
                        sumPraiseFee();
						/*$("#divPicConfig_"+index).addClass("upload_config");*/
                        imageViewer();
                        isExecute = false;
                    }
                },
                error: function (data, status, e) {
                    layerError('上传文件错误，请重试', "错误提示");
                    isExecute = false;
                }
            });
            top.layer.close(loadingIndex);
            return false;
        }

        //删除照片
        function deletePic(index){
            event.stopPropagation(); //防止 $("#divImg_" + index) 的函数触发
            $("#divImg_" + index).attr("onclick","clickFile('"+index+"');");
            var $img = $("[id='viewImg_" + index +"']");
            $img.attr("src","${ctxStatic}/images/upload-photo.png");
            $img.removeAttr("data-original");
            $img.attr("title","点击上传图片");
            $img.closest(".upload_warp_img_div").addClass("drag");
            $img.prev().remove();
            $("#pic_info_"+index).val("");
            sumPraiseFee();
         /*   $("#divPicConfig_"+index).removeClass("upload_config");*/
            imageViewer();
            return false;
		}

		//提交好评申请
        $(document).on("click", "#btnSubmit", function () {
            var flag = true;
            if(clickTag == 1){
                return false;
            }
            if (!$("#inputForm").valid()) {
                return false;
            }

            clickTag = 1;
            var uploadSize = 0; //上传图片数量
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            var entity = {};
            entity['id'] = $("#id").val();
            entity['orderId'] = $("#orderId").val();
            entity['quarter'] = $("#quarter").val();
            entity['status'] = $("#status").val();
            entity['servicepointId'] = $("#servicepointId").val();
            entity['applyCustomerPraiseFee'] = $("#applyCustomerPraiseFee").val();
            entity['applyServicepointPraiseFee'] = $("#applyServicepointPraiseFee").val();
            entity['productNames'] = $("#productNames").val();
            var j = 0;
            $("input[name='picInfo']").each(function(i,element){
                var must = $(this).data("must");
                var url = this.value;
                var code = $(this).data("code");
                var name = $(this).data("name");
                if(must==1 && (url==null || url=='')){
                    layerError("请上传"+ name, "错误提示");
					flag = false;
                    return false;
				}
				if(url!=null && url!=''){
                    entity['picItems['+j+'].code'] = code;
                    entity['picItems['+j+'].url'] = url;
                    entity['picItems['+j+'].name'] = name;
                    j=j+1;
                    uploadSize = uploadSize+1;
				}
            });
            if(!flag){
                clickTag = 0;
                $btnSubmit.removeAttr('disabled');
                return false
            }
            if(uploadSize<=0){
                layerError("至少上传一张图片", "错误提示");
                clickTag = 0;
                $btnSubmit.removeAttr('disabled');
                return false
            }
            var loadingIndex;
            var options = {
                url: "${ctx}/praise/orderPraise/applyPraise",                 //默认是form的action， 如果申明，则会覆盖
                type: 'post',               //默认是form的method（get or post），如果申明，则会覆盖
                dataType: 'json',           //html(默认), xml, script, json...接受服务端返回的类型
                data: entity,
                beforeSubmit: function(formData, jqForm, options){
                    loadingIndex = layer.msg('正在提交，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
                    return true;
                },  //提交前的回调函数
                success:function (data)
                {
                    //提交后的回调函数
                    if(loadingIndex) {
                        layer.close(loadingIndex);
                    }
                    if(ajaxLogout(data)){
                        setTimeout(function () {
                            clickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                        }, 2000);
                        return false;
                    }
                    //var msg = eval(data);
                    if (data.success) {
                        layerMsg("保存成功");
                        top.layer.close(this_index);//关闭本身
                    }else{
                        setTimeout(function () {
                            clickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                        }, 2000);
                        layerError("数据保存错误:" + data.message, "错误提示");
                    }
                    return false;
                },
                error: function (data)
                {
                    setTimeout(function () {
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
                    }, 2000);
                    ajaxLogout(data,null,"数据保存错误，请重试!");
                    //var msg = eval(data);
                },
                //clearForm: true,          //成功提交后，清除所有表单元素的值
                //resetForm: true,          //成功提交后，重置所有表单元素的值
                timeout: 50000               //限制请求的时间，当请求大于5秒后，跳出请求
            };
            $("#submitForm").ajaxSubmit(options);
             return false;
        });

        //计算好评费
		function sumPraiseFee() {
		    var praiseFeeFlag = $("#praiseFeeFlag").val();
		    if(praiseFeeFlag==0){
                $("#applyCustomerPraiseFee").val(0.0);
                $("#applyServicepointPraiseFee").val(0.0);
                return
			}
		    var servicePointFee = 0.0;
		    var customerFee = $("#customerMinFee").val(); // 客户底价
			var customerMaxFee = $("#customerMaxFee").val(); //客户最高价
			var discount = $("#discount").val();
            $("input[name='picInfo']").each(function(i,element){
                var url = this.value;
                if(url!=null && url!=''){
                    customerFee = parseFloat($(this).data("customerfee")) + parseFloat(customerFee);
                }
            });
            if(parseFloat(customerFee)>parseFloat(customerMaxFee)){
                customerFee = customerMaxFee;
			}
            servicePointFee = customerFee - (customerFee * discount);
            servicePointFee = servicePointFee.toFixed(2);
            $("#applyCustomerPraiseFee").val(customerFee);
            $("#applyServicepointPraiseFee").val(servicePointFee);
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
		  .upload_warp_text{text-align:left;margin-top:10px;text-indent:14px;font-size:14px}
		  .upload_config{color: white;background-color: #333333;opacity: 0.6}
		  .config{width:100px;z-indent:2;left:0;bottom:0;}
	  </style>
  </head>
  
  <body>
    <ul class="nav nav-tabs" style="margin-left: 24px;margin-top: 24px">
	  <li><a href="${ctx}/sd/orderItemComplete/orderAttachmentFrom?orderId=${praise.orderId}&quarter=${praise.quarter}">完工图片</a></li>
	  <li class="active"><a href="javascript:void(0);">好评图片</a></li>
    </ul>
    <sys:message content="${message}"/>
	<c:if test="${canAction==true}">
	    <input type="hidden" id="servicePointMinFee" value="${servicePointMinFee}">
	    <input type="hidden" id="customerMinFee" value="${customerPraiseFee.praiseFee}">
	    <input type="hidden" id="customerMaxFee" value="${customerPraiseFee.maxPraiseFee}">
	    <input type="hidden" id="discount" value="${customerPraiseFee.discount}">
	    <input type="hidden" id="praiseFeeFlag" value="${customerPraiseFee.praiseFeeFlag}">
		<form:form id="inputForm" modelAttribute="praise" action="" method="post" class="form-horizontal">
			<form:hidden path="id"/>
			<form:hidden path="orderId"/>
			<form:hidden path="quarter"/>
			<form:hidden path="picsJson"/>
			<form:hidden path="status"/>
			<form:hidden path="applyCustomerPraiseFee"/>
			<form:hidden path="servicepointId"/>
			<div class="row-fluid" style="margin-top: 7px">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">网&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;点：</label>
						<div class="controls">
							<input type="text" class="fromInput input-block-level" disabled value="${servicePointName}">
						</div>
					</div>
				</div>
			</div>
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
						<label class="control-label">示例图片：</label>
						<div class="controls" id="examplePic">
							<c:forEach var="examplePic" items="${customerPraiseFee.examplePicItems}" varStatus="picStatus">
								<div class="upload_warp_img_div">
									<img src="${examplePic.url}" data-original="${examplePic.url}"style="width: 100px;height: 100px;padding-right: 16px">
									<div style="position:absolute;width:100px;z-indent:2;left:0;bottom:0;color: white;background-color: #333333;opacity: 0.9;"><span>${examplePic.name}</span></div>
								</div>
							</c:forEach>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">好评图片：</label>
						<div class="controls">
							<%--<div class="input-block-level" style="border:1px solid #BBBBBB;border-radius:5px;height: auto">--%>
								<div class="upload_warp" id="divUploadWarp">
									<c:choose>
										<c:when test="${canSave==true}">
											<c:forEach items="${praiseList}" var="picRequirement" varStatus="picIndex">
												<c:set var="isHasValue" value="true" />
												<c:if test="${praise.picItems!=null && fn:length(praise.picItems) >0}">
													<c:forEach items="${praise.picItems}" var="picItems">
														<c:if test="${picRequirement.code == picItems.code && isHasValue}">
															<c:set var="isHasValue" value="false" />
															<div class="upload_warp_left">
																<div class="upload_warp_img_div" id="divImg_${picIndex.index}" data-index="${picIndex.index}">
																	<a href='javascript:;' title='点击删除图片' onclick="deletePic('${picIndex.index}')" class="upload_warp_img_div_del"></a>
																	<img title="点击放大图片" id="viewImg_${picIndex.index}"  data-original="${ctxUpload}/${picItems.url}" src="${ctxUpload}/${picItems.url}" />
																	<div  id="divPicConfig_${picIndex.index}" class="config">
																		<c:if test="${picRequirement.mustFlag ==1}">
																			<font color="red" style="position:relative; top:2px;">*</font>
																		</c:if>
																			${picRequirement.name}
																	</div>
																</div>
															</div>
															<input id="upload_file_${picIndex.index}" name="upload_file" type="file" style="display: none" accept="image/gif,image/jpeg,image/png" onchange="uploadFileChange('${picIndex.index}')">
															<input id="pic_info_${picIndex.index}" data-customerfee="${picRequirement.fee}" name="picInfo" data-code="${picRequirement.code}" data-must="${picRequirement.mustFlag}" data-name="${picRequirement.name}" value="${picItems.url}" type="hidden">
														</c:if>
													</c:forEach>
												</c:if>
												<c:if test="${isHasValue}">
													<div class="upload_warp_left">
														<div class="upload_warp_img_div drag" id="divImg_${picIndex.index}" onclick="clickFile('${picIndex.index}')" data-index="${picIndex.index}">
															<img title="点击上传图片" id="viewImg_${picIndex.index}" src="${ctxStatic}/images/upload-photo.png" />
															<div id="divPicConfig_${picIndex.index}" class="config">
																<c:if test="${picRequirement.mustFlag ==1}">
																	<font color="red" style="position:relative; top:2px;">*</font>
																</c:if>
																	${picRequirement.name}
															</div>
														</div>
													</div>
													<input id="upload_file_${picIndex.index}" name="upload_file" type="file" style="display: none" accept="image/gif,image/jpeg,image/png" onchange="uploadFileChange('${picIndex.index}')">
													<input id="pic_info_${picIndex.index}" name="picInfo" data-customerfee="${picRequirement.fee}" data-code="${picRequirement.code}" data-must="${picRequirement.mustFlag}" data-name="${picRequirement.name}" value="" type="hidden">
												</c:if>
											</c:forEach>
										</c:when>
										<c:otherwise>
											<c:if test="${praise.picItems!=null && fn:length(praise.picItems) >0}">
												<c:forEach items="${praise.picItems}" var="picItems" varStatus="picIndex">
													<div class="upload_warp_left">
														<div class="upload_warp_img_div">
															<img title='点击放大' src='${ctxUpload}/${picItems.url}' data-original='${ctxUpload}/${picItems.url}'/>
															<div style="width:100px;z-indent:2;left:0;bottom:0;">${picItems.name}</div>
														</div>
													</div>
												</c:forEach>
											</c:if>
										</c:otherwise>
									</c:choose>
								</div>
							<%--</div>--%>
								<div>
									<span style="margin-top: 5px;width: 75px">格式要求：</span>
									<span style="color: red;">支持jpg,jpeg,png格式,且大小不能超过2MB,建议尺寸600*800</span><br/>
									<c:if test="${customerPraiseFee.praiseRequirement !=null && customerPraiseFee.praiseRequirement!=''}">
										<label class="control-label" style="padding-top: 1px;width: 75px;text-align: left">内容要求：</label>
										<div style="color: red;margin-left: 75px">
												${customerPraiseFee.praiseRequirement}
										</div>
									</c:if>
								</div>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">好评费用：</label>
						<div class="controls">
							<form:input  path="applyServicepointPraiseFee" readonly="true" cssStyle="width: 100px"></form:input>
						</div>
					</div>
				</div>
			</div>
		</form:form>
		<form:form id="submitForm" ></form:form>
		<div style="height: 60px;width: 100%"></div>
		<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
			<hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
			<div style="float: right;margin-right: 20px">
				<c:if test="${canSave==true}">
				    <c:choose>
					    <c:when test="${praise.id==null || praise.id==0}">
					       <input id="btnSubmit" class="btn btn-primary" type="submit" value="提 交" style="margin-left: 300px;margin-top: 10px"/>&nbsp;&nbsp;
						</c:when>
					     <c:otherwise>
					          <input id="btnSubmit" class="btn btn-success" type="submit" value="再次提交" style="margin-left: 300px;margin-top: 10px"/>&nbsp;&nbsp;
					     </c:otherwise>
					</c:choose>
				</c:if>
				<input id="btnCancel" class="btn" type="button" value="关 闭" style="margin-top:10px"onclick="cancel()"/>
			<div>
		</div>
	</c:if>
  </body>
</html>
