<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
	<head>
		<title>${feedback.title}附件图片</title>
		<%@ include file="/WEB-INF/views/include/head.jsp" %>
		<meta name="decorator" content="default" />
		<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
		<link type="text/css" href="${ctxStatic}/weixin/css/weixin.css" rel="stylesheet">
		<script type="text/javascript" src="${ctxStatic}/scroll/jquery.slimscroll.min.js"></script>
		<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
		<%@include file="/WEB-INF/views/include/treeview.jsp" %>
		<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
		<style type="text/css">
			.imgfile {  }
		</style>
		<script type="text/javascript">
            <%String parentIndex = request.getParameter("parentIndex");%>
            var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
            var this_index = top.layer.index;
            var fileName="";
            var clickTag = 0;
            $(document).ready(function() {

                $("#fileToUpload1").change(function () {
                    var filepath = $("#fileToUpload1").val();
                    if(Utils.isEmpty(filepath)){
                        return false;
                    }
                    var extStart=filepath.lastIndexOf(".");
                    var ext=filepath.substring(extStart,filepath.length).toUpperCase();
                    if(ext!=".BMP"&&ext!=".PNG"&&ext!=".GIF"&&ext!=".JPG"&&ext!=".JPEG"){
                        layerAlert("图片类型限于bmp,png,gif,jpeg,jpg格式","系统提示",true);
                        $("#buttonUpload1").attr("disabled", true);
                        return false;
                    }
                    //check size
                    var files = document.getElementById("fileToUpload1").files;
                    var fileSize = files[0].size;
                    var size = fileSize / 1024;
                    if(size > 2000){
                        layerAlert("图片不能大于2M","系统提示",true);
                        $("#fileToUpload1").val("");
                        return false;
                    }
                    $("#buttonUpload1").removeAttr("disabled");
                    return true;
                });

                $("#btnSave").click(function()
                {
                    if($("#attachment1").val()=="")
                    {
                        layerAlert("还未上传图片,图片将显示为空","系统提示",true);
                        return false;
                    }
                    if (clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    var ajaxSuccess = 0;
                    var $btnSubmit = $("#btnSave");
                    $btnSubmit.attr('disabled', 'disabled');
                    var loadingIndex;
                    var data={
						feedbackId:$("#feedbackId").val(),
						quarter: $("#quarter").val(),
						remarks:$("#attachment1").val()
                    };
                    $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: "${ctx}/sd/feedback/addImageItem",
                        data: data,
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
                            if (data.success){
                                ajaxSuccess = 1;
                                var layero = $("#layui-layer"+parentIndex,top.document);
                                var iframeWin = top[layero.find('iframe')[0]['name']];
                                iframeWin.addReplyItem(data.data);//调用父页面方法
                                top.layer.close(this_index);
								layerMsg("图片保存成功");
                            }
                            else{
                                layerError(data.message,"错误提示",true);
                            }
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"图片保存错误，请重试!");
                        }
                    });
                });

                $("#buttonUpload1").click(function()
                {
                    var $btn = $(this);
                    if (clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    $btn.attr('disabled', 'disabled');
                    $("#btnSave").attr('disabled', 'disabled');
                    uploadfile($("#attachment1"),$("#attachment1_image"), "fileToUpload1");
                    return false;
                });

            });

            function uploadfile($obj1,$obj1_image, obj2) {
                var $btn = $("#buttonUpload1");
                var filepath = $("#"+obj2).val();
                if(Utils.isEmpty(filepath)){
                    layerError("请先选择文件", "错误提示");
                    clickTag = 0;
                    $btn.removeAttr('disabled');
                    return false;
                }
                var data = {
                    fileName : $obj1.val()
                };
                $.ajaxFileUpload({
                    async: false,
                    cache: false,
                    url : '/servlet/Upload?fileName=' + $obj1.val(),//处理图片脚本
                    secureuri : false,
                    data : data,
                    fileElementId : obj2,//file控件id
                    dataType : 'json',
                    complete: function () {
						setTimeout(function () {
							clickTag = 0;
                            $btn.removeAttr('disabled');
						}, 2000);
                    },
                    success : function(data, status) {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        $obj1.val(data.fileName);
                        fileName=data.fileName;
                        $obj1_image.show();
                        $obj1_image.attr("src","${ctxUpload}/"+data.fileName+"?t="+Math.random());
                        $("#btnSave").removeAttr('disabled');
                        clickTag = 0;
                    },
                    error : function(data, status, e) {
                        layerError(e, "错误提示");
                    }
                });
            }

            function closeFeedback()
            {
                if ($("#btnClose").prop("disabled") == true)
                {
                    return false;
                }
            }

		</script>
	</head>
<body>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<div style="height: 550px;width:100%;">
			<div style="top: 0px; position: relative;" id="chat_chatmsglist"
				class="chatContent">
				<shiro:hasPermission name="sd:feedback:pic">
					<input type="hidden" id="feedbackId" name="feedbackId" value="${feedbackId}">
					<input type="hidden" id="quarter" name="quarter" value="${quarter}">
					<input type="hidden" name="attachment1" id="attachment1" />
					<input id="fileToUpload1" type="file" size="20" name="fileToUpload1" class="input">
					<button id="buttonUpload1" type="button" disabled="disabled" class="btn">上传图片</button>
					<button id="btnSave" name="btnSave" type="button" disabled="disabled" class="btn">保存</button>
				</shiro:hasPermission>
				<br>
				<img id="attachment1_image" class="imgfile"  src="${ctxUpload}/${feedback.attachment1.filePath}"/>

			</div>


	</div>
</body>
</html>