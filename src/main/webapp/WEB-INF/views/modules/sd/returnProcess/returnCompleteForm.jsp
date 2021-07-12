<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>退货完工表单</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
    <script src="${ctxStatic}/js/ajaxfileupload.js"></script>
    <!-- image viewer -->
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <script type="text/javascript">
        //防止拖文件到浏览器后自动打开或下载文件
        window.addEventListener("dragover", function (e) {
            e = e || event;
            e.preventDefault();
        }, false);
        window.addEventListener("drop", function (e) {
            e = e || event;
            e.preventDefault();
        }, false);
        //end

        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var this_index = top.layer.index;
        Order.rootUrl = "${ctx}";
        var clickTag = 0;
        var viewer;
        var isExecute = false;
        var uploadPrefix = '${ctxUpload}';

        function imageViewer() {
            viewer = $("#divMain").viewer('destroy').viewer(
                {
                    url: "data-original",
                    filter: function (image) {
                        if (image.src.lastIndexOf("/upload-photo.png") > 0) {
                            return false;
                        }
                        return true;
                    },
                    viewed: function (image) {
                    },
                    shown: function () {
                        if (this.viewer.index == -1) {
                            this.viewer.hide();
                        }
                    }
                }
            );
        }

        $(document).ready(function () {
            imageViewer();
            //拖拽事件
            $('div.upload_warp_left').each(function (index, item) {
                var $item = $(item);
                item.ondragover = function (ev) {
                    if ($item.hasClass("drag")) {
                        $item.addClass("imgOnDarg");
                    }
                    ev.preventDefault();
                };
                item.ondragleave = function (ev) {
                    if ($item.hasClass("drag")) {
                        $item.removeClass("imgOnDarg");
                    }
                    ev.preventDefault();
                };
                item.ondrop = function (e) {
                    e.preventDefault();
                    if (!$item.hasClass("drag")) {
                        return false;
                    }
                    var type = $item.data("code");
                    var index = $item.data("index");
                    var fs = e.dataTransfer.files;
                    var len = fs.length; //获取文件个数
                    if (len == 0) {
                        layerInfo("请拖拽要上传的文件", "系统提示");
                        return false;
                    }
                    var _type = fs[0].type;
                    if (!_type.match('image.*')) {
                        layerInfo("文件不是图片文件!", "系统提示");
                        return false;
                    }
                    document.getElementById("file_" + type + "_" + index).files = fs; // 有的浏览器会触发onchange事件,有的不会
                    if (!isExecute) { // 部分浏览器 js给input赋值会触发onchange事件 所以用isExecute判断onchange是否执行了
                        $("#file_" + type + "_" + index).change();
                    }
                };
            });

            //submit
            $("#btnSubmit").click(function(event){
                if(clickTag == 1){
                    event.preventDefault();
                    return false;
                }
                clickTag = 1;
                var $btnSubmit = $("#btnSubmit");
                $btnSubmit.attr('disabled', 'disabled');
                var validResult = validForm();
                if(validResult.validResult == false){
                    event.preventDefault();
                    $btnSubmit.removeAttr('disabled');
                    clickTag = 0;
                    return false;
                }
                //sn
                if(dismountData.uploadFlag === 0) {
                    var oldSN = $("#dismountOldSN").val();
                    validSN(oldSN, '正在请求第三方系统检验[产品SN码]，请耐心等待...', '请输入[产品SN码]！', '检验产品SN码错误，请重试!');
                    if (snValidResult === false) {
                        event.preventDefault();
                        $btnSubmit.removeAttr('disabled');
                        clickTag = 0;
                        return false;
                    }
                }
                //data
                var postForm = completeModel;
                var items = [];
                items.push(dismountData);
                items.push(logisticsData);
                postForm['items'] = items;

                var loadingIndex;
                var ajaxSuccess = 0;
                $.ajax({
                    async: false,
                    cache: false,
                    type: "POST",
                    url: "${ctx}/sd/order/return/saveCompleteFrom?at="+ (new Date()).getTime(),
                    contentType:"application/json",
                    data: JSON.stringify(postForm),
                    dataType: 'json',
                    beforeSend: function () {
                        loadingIndex = top.layer.msg('正在提交，请稍等...', {
                            icon: 16,
                            time: 0,//不定时关闭
                            shade: 0.3
                        });
                    },
                    complete: function () {
                        if(ajaxSuccess == 0) {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                        }
                        if(loadingIndex) {
                            top.layer.close(loadingIndex);
                        }
                    },
                    success: function (data) {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if (!data){
                            layerError("完工失败","错误提示");
                            return false;
                        }
                        if(data.success == false) {
                            console.log('data.data',data.data);
                            if(data.data){
                                if(data.data.dismount){
                                    dismountData['id'] = data.data.dismount.id;
                                    dismountData['uploadFlag'] = data.data.dismount.uploadFlag;
                                    if(dismountData['uploadFlag'] === 1){
                                        syncB2BSuccess(data.data.dismount.itemType);
                                    }
                                }
                                if(data.data.logistics){
                                    logisticsData['id'] = data.data.logistics.id;
                                    logisticsData['uploadFlag'] = data.data.logistics.uploadFlag;
                                    if(logisticsData['uploadFlag'] === 1){
                                        syncB2BSuccess(data.data.logistics.itemType);
                                    }
                                }
                            }
                            layerError(data.message || '完工失败', "错误提示");
                            return false;
                        }else{
                            layerMsg('完工成功！');
                            if (parentIndex != '') {
                                //订单详情页调用
                                var layero = $("#layui-layer" + parentIndex, top.document);
                                var iframeWin = top[layero.find('iframe')[0]['name']];
                                iframeWin.successReturnComplete(orderId);
                            }
                            top.layer.close(this_index);
                        }
                        return false;
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText,null,"完工失败，请重试!");
                    }
                });
            });
        });

        //上传B2B成功刷新UI
        function syncB2BSuccess(itemType){
            $("#span_"+ itemType).show();
            //图片不允许删除
            $("div#div_" + itemType).find("a.upload_warp_img_div_del").remove();
        }

        //检查表单内容
        function validForm(){
            var submitForm = {validResult:false};
            if(!completeModel  || !dismountData || !(dismountData.jsonItem)
                || !logisticsData || !(logisticsData.jsonItem) ){
                layerError("输入内容检查不通过","错误提示");
                return submitForm;
            }
            //dismount
            var oldSN = $("#dismountOldSN").val();
            oldSN = oldSN.replace(/(^\s*)|(\s*$)/g, '');  //去除空格;
            if(oldSN === "" || oldSN === undefined || oldSN === null){
                layerError("请输入[产品SN码]！","错误提示");
                return submitForm;
            }
            dismountData['oldSN'] = oldSN;

            for(var index=0,len=dismountData.jsonItem.photos.length;index<len;index++){
                var item = dismountData.jsonItem.photos[index];
                var $img = $("#dismount_photo_" + index);
                var url = $img.attr("src");
                if(url.indexOf('upload-photo.png') != -1){
                    url = '';
                }
                if(item.required == 1 && url === '') {
                    layerError("请上传[" + item.title + "]！","错误提示");
                    return submitForm;
                }
                item.url = url;
                var uploadDate = $img.nextAll("a.upload_date").text();
                item.updateDate = uploadDate;
            }

            //logistics
            var option = $("#logisticsCompany option:selected");
            if(!option || option.val() == ''){
                layerError("请选择[快递公司]！","错误提示");
                return submitForm;
            }
            logisticsData.jsonItem.company = option.text();
            logisticsData.jsonItem.companyCode = option.val();
            var logisticsSN = $("#logisticsSN").val();
            logisticsSN = logisticsSN.replace(/(^\s*)|(\s*$)/g, '');  //去除空格;
            if(logisticsSN === "" || logisticsSN === undefined || logisticsSN === null){
                layerError("请输入[快递单号]！","错误提示");
                return submitForm;
            }
            logisticsData.jsonItem.number = logisticsSN;
            for(var index=0,len=logisticsData.jsonItem.photos.length;index<len;index++){
                var item = logisticsData.jsonItem.photos[index];
                var $img = $("#logistics_photo_" + index);
                var url = $img.attr("src");
                if(url.indexOf('upload-photo.png') != -1){
                    url = '';
                }
                if(item.required == 1 && url === '') {
                    layerError("请上传[" + item.title + "]！","错误提示");
                    return submitForm;
                }
                item.url = url;
                var uploadDate = $img.nextAll("a.upload_date").text();
                item.updateDate = uploadDate;
            }

            submitForm['validResult'] = true;//检查通过
            return submitForm;
        }

        //检查SN
        var snValidResult;
        function validSN(sn,loadingMsg,requiredMsg,validErrorMsg){
            snValidResult = false;
            sn = sn.replace(/(^\s*)|(\s*$)/g, '');  //去除空格;
            if(sn === "" || sn === undefined || sn === null){
                layerError(requiredMsg,"错误提示");
                return;
            }
            var loadingIndex;
            var postData = {
                dataSourceId: completeModel.dataSource,
                b2bOrderNo: completeModel.b2bOrderNo,
                sn: sn
            };
            $.ajax({
                async: false,
                cache: false,
                type: "POST",
                url: "${ctx}/sd/order/return/validSN",
                dataType: 'json',
                data: postData,
                beforeSend: function () {
                    loadingIndex = layer.msg(loadingMsg, {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
                },
                complete: function () {
                    if(loadingIndex) {
                        layer.close(loadingIndex);
                    }
                },
                success: function (data) {
                    if(ajaxLogout(data)){
                        isExecute = false;
                        return;
                    }
                    if (data && data.success == true) {
                        snValidResult = true;
                        return;
                    }
                    else if (data && data.message) {
                        layerError(data.message, "错误提示");
                        return;
                    }
                    isExecute = false;
                    return;
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,validErrorMsg);
                    return;
                }
            });
        }


        function clickFile(id) {
            $(id).click();
        }

        function uploadfile(fileInputId, index, type) {
            var data = {
                fileName: $("#"+fileInputId).val()
            };
            var loadingIndex = top.layer.msg('正在上传文件，请耐心等待...', {
                icon: 16,
                time: 0,
                shade: 0.3
            });
            $.ajaxFileUpload({
                url: '${pageContext.request.contextPath}/servlet/Upload',//处理图片脚本
                secureuri: false,
                data: data,
                fileElementId: fileInputId,//file控件id
                dataType: 'json',
                success: function (data, status) {
                    if (data && data.status == "success") {
                        var filePath = data.fileName;
                        var $img = $("[id='" + type + "_photo_" + index + "']");
                        $img.attr("src", "${ctxUpload}/" + filePath);
                        $img.attr("data-original", "${ctxUpload}/" + filePath);
                        $img.attr("title", "点击放大照片");
                        $img.removeAttr("onclick");
                        $img.before("<a href='javascript:;' title='点击删除照片'" + " onclick=\"deletePic('" + type + "','" + index + "')\" class=\"upload_warp_img_div_del\"></a>");
                        $img.closest(".upload_warp_left").removeClass("drag").removeClass("imgOnDarg");
                        imageViewer();
                        $img.nextAll("a.upload_date").text(DateFormat.format(new Date(), 'yyyy-MM-dd hh:mm'));//upload date
                    } else {
                        layerError("上传照片失败", "错误提示");
                    }
                    if (loadingIndex) {
                        top.layer.close(loadingIndex);
                    }
                },
                error: function (data, status, e) {
                    if (loadingIndex) {
                        top.layer.close(loadingIndex);
                    }
                    layerError(e, "错误提示");
                    isExecute = false;
                }
            });
            return false;
        }

        function checkAttachment(type, index) {
            isExecute = true;
            var fileToUploadPicId = "file_" + type + "_" + index;
            var filepath = $("#" + fileToUploadPicId).val();
            if (Utils.isEmpty(filepath)) {
                $("#" + fileToUploadPicId).val("");
                isExecute = false;
                return false;
            }
            var extStart = filepath.lastIndexOf(".");
            var ext = filepath.substring(extStart, filepath.length).toUpperCase();
            if (ext != ".BMP" && ext != ".PNG" && ext != ".GIF" && ext != ".JPG" && ext != ".JPEG") {
                layerInfo("照片类型限于bmp,png,gif,jpeg,jpg格式", "系统提示");
                $("#" + fileToUploadPicId).val("");
                isExecute = false;
                return false;
            }
            //check size
            var files = document.getElementById(fileToUploadPicId).files;
            var fileSize = files[0].size;
            var size = fileSize.toFixed(2);
            if (size > (2 * 1024 * 1024)) {
                layerInfo("照片不能大于2M", "系统提示");
                $("#" + fileToUploadPicId).val("");
                isExecute = false;
                return false;
            }
            //var pathInput = "path_" + type + "_" + index;
            uploadfile(fileToUploadPicId, index, type);
        }

        //删除图片
        function deletePic(type, idIndex) {
            //type:dismount/logistics
            //idIndex:索引
            var clicktag = 0;
            top.layer.confirm('确定要删除该照片吗?', {icon: 3, title: '系统确认'}, function (index) {
                if (clicktag == 1) {
                    return false;
                }
                clicktag = 1;
                top.layer.close(index);//关闭本身
                var $img = $("[id='" + type + "_photo_" + idIndex + "']");
                $img.attr("onclick", "clickFile('#file_" + type + "_" + idIndex + "');");
                $img.attr("src", "${ctxStatic}/images/upload-photo.png");
                $img.attr("title", "点击上传照片");
                $img.removeAttr("data-original");
                $img.closest(".upload_warp_left").addClass("drag");
                $img.prev().remove();
                $img.nextAll("a.upload_date").text("");//upload date
                imageViewer();
            });
        }

        //取消
        function cancel() {
            top.layer.close(this_index);//关闭本身
        }

    </script>
    <style type="text/css">
        .form-horizontal{margin-top:5px}
        .form-horizontal .control-label{width:100px;margin-top:20px}
        .form-horizontal .controls{margin-left:110px}
        .upload_warp_img_div img{max-width:100%;max-height:100%;vertical-align:middle;width:100%;height:100%;margin-bottom:5px}
        .upload_warp_img_div{position:relative;height:100px;width:120px;border:1px solid #ccc;float:left;display:table-cell;text-align:center;background-color:#eee;cursor:pointer;margin:5px}
        .upload_warp_text{text-align:left;margin-top:10px;text-indent:14px;font-size:12px}
        .upload_date{text-align:left;margin-top:10px;text-indent:14px;font-size:12px}
        .upload_warp_left img{margin-top:0}
        .upload_warp_left{float:left;width:130px;height:160px;border:1px dashed #999;border-radius:4px;cursor:pointer;margin-right:10px;padding:5px}
        .upload_warp{text-align:center;display:inline-block}
        .upload_warp_img_div .upload_warp_img_div_del{position:absolute;top:0;width:20px!important;height:20px!important;right:0;margin-top:0!important;background-size:20px 20px!important;background:url(${ctxStatic}/images/delUploadFile.png) no-repeat}
        .table tbody td,.table thead th{text-align:center;vertical-align:middle;background-color:transparent;}
        .fromInput{border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;margin-top:19px}
        .imageFile{width:130px;height:130px;padding:0;margin-top:10px;margin-bottom:0;padding-left:5%}
        .imgOnDarg{border:2px dashed #cd5c5c;background:bisque}
        .form-horizontal .controls .select2-container {margin-top: 19px;}
        legend >span{font-size: 14px;margin-left: 20px;}
    </style>
</head>
<body style="margin: 0px 10px 3px 10px;">
<sys:message content="${message}"/>
<c:if test="${errorFlag == 0}">
<script class="removedscript" type="text/javascript">
    var completeModel = ${fns:toGson(completeModel)};
</script>
<input type="hidden" id="orderId" name="orderId" value="${completeModel.orderId}"/>
<input type="hidden" id="quarter" name="quarter" value="${completeModel.quarter}"/>
<input type="hidden" id="dataSource" name="dataSource" value="${completeModel.dataSource}"/>
<input type="hidden" id="productId" name="productId" value="${completeModel.productId}">
<input type="hidden" id="productName" name="productName" value="${completeModel.productName}">
<input type="hidden" id="b2bOrderId" name="b2bOrderId" value="${completeModel.b2bOrderId}">
<input type="hidden" id="b2bOrderNo" name="b2bOrderNo" value="${completeModel.b2bOrderNo}">
<input type="hidden" id="orderServiceType" name="orderServiceType" value="${completeModel.orderServiceType}">
<div id="divMain" class="form-horizontal">
    <c:if test="${dismountItem != null}">
        <script class="removedscript" type="text/javascript">
            var dismountData =  ${fns:toGson(dismountItem)};
        </script>
    <input type="hidden" id="dismountId" value="${dismountItem.id}">
    <input type="hidden" id="dismountItemType" value="${dismountItem.itemType}">
    <input type="hidden" id="dismountUploadFlag" value="${dismountItem.uploadFlag}">
    <legend>产品信息
        <span id="span_dismount" style="${dismountItem.uploadFlag == 1?'display:display;':'display:none;'}"><i
                class="icon-info-sign" style="color: blue"></i>&nbsp;信息已同步第三方系统</span>
    </legend>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label"><font color="red">*</font>产品SN码：</label>
                <div class="controls">
                    <input id="dismountOldSN" placeholder="请输入产品SN码"
                           class="fromInput input-block-level" ${dismountItem.uploadFlag == 1?'disabled':''}
                           value="${dismountItem.oldSN}" maxlength="64"/>
                </div>
            </div>
        </div>
        <div class="span6" style="margin-top: 22px;">
            <span><i class="icon-info-sign"></i>&nbsp;退回产品的SN码</span>
        </div>
    </div>
        <!-- 照片 -->
        <div class="row-fluid">
            <div class="span12">
                <div class="control-group">
                    <label class="control-label" style="margin-top: 2px;"><font color="red">*</font>完成照片：</label>
                    <div class="controls">
                        <div id="div_dismount" class="upload_warp">
                            <!-- photo item -->
                            <c:forEach items="${dismountItem.jsonItem.photos}" var="item" varStatus="idx">
                                <div class="upload_warp_left ${item.url == ''?'drag':''}" data-code="dismount"
                                     data-index="${idx.index}">
                                    <input name="path_dismount_${idx.index}" id="path_dismount_${idx.index}" type="hidden"
                                           htmlEscape="false"/>
                                    <input id="file_dismount_${idx.index}" name="file_dismount_${idx.index}"
                                           data-type="dismount" data-index="${idx.index}" type="file"
                                           class="hero-unit" style="display: none" size="20" value=""
                                           onchange="checkAttachment('dismount','${idx.index}')">
                                    <div class="upload_warp_img_div">
                                        <c:if test="${item.url ne '' && dismountItem.uploadFlag == 0}">
                                            <a href="javascript:;" title="点击删除照片"
                                               onclick="deletePic('dismount','${idx.index}')"
                                               class="upload_warp_img_div_del"></a>
                                        </c:if>
                                        <c:choose>
                                            <c:when test="${item.url eq ''}">
                                                <img id="dismount_photo_${idx.index}" title="点击上传照片"
                                                     src="${ctxStatic}/images/upload-photo.png"
                                                     onclick="clickFile('#file_dismount_${idx.index}')"/>
                                            </c:when>
                                            <c:otherwise>
                                                <img id="dismount_photo_${idx.index}" title="点击放大照片"
                                                     src="${ctxUpload}/${item.url}"
                                                     data-original="${ctxUpload}/${item.url}"/>
                                            </c:otherwise>
                                        </c:choose>
                                        <span class="upload_warp_text">${fns:abbr(item.title,14)}</span>
                                        <c:if test="${item.required == 1}">
                                            <br/>
                                            <span class="upload_warp_text">(必填)</span>
                                        </c:if>
                                        <a style="display: none;" class="upload_date" href="javascript:void(0);">${item.updateDate}</a>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <c:if test="${logisticsItem != null}">
        <script class="removedscript" type="text/javascript">
            var logisticsData = ${fns:toGson(logisticsItem)};
        </script>
        <input type="hidden" id="logisticsId" value="${logisticsItem.id}">
        <input type="hidden" id="logisticsItemType" value="${logisticsItem.itemType}">
        <input type="hidden" id="logisticsUploadFlag" value="${logisticsItem.uploadFlag}">
        <legend>物流信息
            <span id="span_logistics" style="${logisticsItem.uploadFlag == 1?'display:display;':'display:none;'}"><i
                    class="icon-info-sign" style="color: blue"></i>&nbsp;信息已同步第三方系统</span>
        </legend>
        <div class="row-fluid">
            <div class="span6">
                <div class="control-group">
                    <label class="control-label"><font color="red">*</font>快递公司：</label>
                    <div class="controls">
                        <select id="logisticsCompany" name="logisticsCompany" class="input-block-level" ${logisticsItem.uploadFlag == 1?'disabled':''} >
                            <option value=""
                                    <c:out value="${(logisticsItem.jsonItem.companyCode eq '')?'selected=selected':''}"/>>
                                请选择
                            </option>
                            <c:forEach items="${fns:getDictListFromMS('express_type')}" var="dict">
                                <option value="${dict.value}"<c:out
                                        value="${(logisticsItem.jsonItem.companyCode eq dict.value)?'selected=selected':''}"/>>${dict.label}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </div>
            <div class="span6">
                <div class="control-group">
                    <label class="control-label"><font color="red">*</font>快递单号：</label>
                    <div class="controls">
                        <input id="logisticsSN"
                               class="fromInput input-block-level" ${logisticsItem.uploadFlag == 1?'disabled':''}
                               value="${logisticsItem.jsonItem.number}" maxlength="64"/>
                    </div>
                </div>
            </div>
        </div>
        <!-- 照片 -->
        <div class="row-fluid">
            <div class="span12">
                <div class="control-group">
                    <label class="control-label" style="margin-top: 2px;"><font color="red">*</font>快递照片：</label>
                    <div class="controls">
                        <div id="div_logistics" class="upload_warp">
                            <!-- photo item -->
                            <c:set var="blankUrl" value="${ctxStatic}/images/upload-photo.png"/>
                            <c:forEach items="${logisticsItem.jsonItem.photos}" var="item" varStatus="idx">
                                <div class="upload_warp_left ${item.url == ''?'drag':''}" data-code="logistics"
                                     data-index="${idx.index}">
                                    <input name="path_logistics_${idx.index}" id="path_logistics_${idx.index}" type="hidden"
                                           htmlEscape="false"/>
                                    <input id="file_logistics_${idx.index}" name="file_logistics_${idx.index}"
                                           data-type="logistics" data-index="${idx.index}" type="file"
                                           class="hero-unit" style="display: none" size="20" value=""
                                           onchange="checkAttachment('logistics','${idx.index}')">
                                    <div class="upload_warp_img_div">
                                        <c:if test="${item.url ne '' && logisticsItem.uploadFlag == 0}">
                                            <a href="javascript:;" title="点击删除照片"
                                               onclick="deletePic('logistics','${idx.index}')"
                                               class="upload_warp_img_div_del"></a>
                                        </c:if>
                                        <c:choose>
                                            <c:when test="${item.url eq ''}">
                                                <img id="logistics_photo_${idx.index}" title="点击上传照片"
                                                     src="${ctxStatic}/images/upload-photo.png"
                                                     onclick="clickFile('#file_logistics_${idx.index}')"/>
                                            </c:when>
                                            <c:otherwise>
                                                <img id="logistics_photo_${idx.index}" title="点击放大照片"
                                                     src="${ctxUpload}/${item.url}"
                                                     data-original="${ctxUpload}/${item.url}"/>
                                            </c:otherwise>
                                        </c:choose>
                                        <span class="upload_warp_text">${fns:abbr(item.title,14)}</span>
                                        <c:if test="${item.required == 1}">
                                            <br/>
                                            <span class="upload_warp_text">(必填)</span>
                                        </c:if>
                                        <a style="display: none;" class="upload_date" href="javascript:void(0);">${item.updateDate}</a>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        </c:if>
    <div style="height: 60px;width: 100%"></div>
    <!-- Button -->
    <div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
        <hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
        <div style="float: right;margin-right: 60px;margin-top:10px;">
            <c:if test="${completeModel.completeType eq ''}">
            <input id="btnSubmit" class="btn btn-primary" type="button" value="提交" />
            </c:if>
            <input id="btnCancel" class="btn" type="button" value="取消"  onclick="cancel()"/>
        </div>
    </div>
    </c:if>

</body>
</html>