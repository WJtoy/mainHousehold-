<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>完成照片</title>
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
        window.addEventListener("dragover",function(e){
            e = e || event;
            e.preventDefault();
        },false);
        window.addEventListener("drop",function(e){
            e = e || event;
            e.preventDefault();
        },false);
        //end

        var orderdetail_index = parent.layer.getFrameIndex(window.name);
        var this_index = top.layer.index;
        Order.rootUrl = "${ctx}";
        var clickTag = 0;
        var viewer;
        var isExecute = false;
        function imageViewer(){
            viewer = $("#divMain").viewer('destroy').viewer(
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

        $(document).ready(function () {
            imageViewer();
            //拖拽事件
            $('div.upload_warp_left').each(function(index, item) {
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
                    var type = $item.data("code");
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
                    document.getElementById("fileToUpload"+type+index).files = fs; // 有的浏览器会触发onchange事件,有的不会
                    if(!isExecute){ // 部分浏览器 js给input赋值会触发onchange事件 所以用isExecute判断onchange是否执行了
                        $("#fileToUpload" + type + index).change();
                    }
                };
            });
        });

        function clickFile(id){
            $(id).click();
        }

        function uploadfile($obj1,obj2,index,type) {
            var filepath = $("#"+obj2).val();
            if(Utils.isEmpty(filepath)){
                layerError("请先选择文件", "错误提示");
                clickTag = 0;
                isExecute = false;
                return false;
            }
            var data = {
                fileName: $obj1.val()
            };
            var loadingIndex = layer.msg('正在上传文件，请耐心等待...', {
                icon: 16,
                time: 0,
                shade: 0.3
            });
            $.ajaxFileUpload({
                url: '${pageContext.request.contextPath}/servlet/Upload?fileName=' + $obj1.val(),//处理图片脚本
                secureuri: false,
                data: data,
                fileElementId: obj2,//file控件id
                dataType: 'json',
                success: function (data, status) {
                    $obj1.val(data.fileName);
                    var filePath = data.fileName;
                    var orignalName = data.origalName;
                    if (data.origalName == "") {
                        layerError("请先选择文件，再点击上传", "错误提示");
                        clickTag = 0;
                        isExecute = false;
                        return true;
                    }
                    //Ajax 保存附件信息
                    $.ajax({
                        type: "POST",
                        url: "${ctx}/sd/orderItemComplete/saveOrderCompletePic",
                        data: {
                            filePath: filePath,
                            orginalName: orignalName,
                            orderId: $("#id").val(),
                            quarter: $("#quarter").val(),
                            productId:$("#productId" + index).val(),
                            id:$("#itemCompleteId" + index).val(),
                            field:type,
                            customerId:$("#customerId").val()
                        },
                        beforeSend: function () {
                        },
                        complete: function () {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                            }, 2000);
                        },
                        success: function (data) {
                            if(ajaxLogout(data)){
                                isExecute = false;
                                return false;
                            }
                            if (data && data.success == true) {
                                var $img = $("[id='logo_image"+type + index +"']");
                                $img.attr("src","${ctxUpload}/" + filePath);
                                $img.attr("data-original","${ctxUpload}/" +filePath);
                                $img.attr("title","点击放大图片");
                                $img.removeAttr("onclick");
                                $img.before("<a href='javascript:;' title='点击删除图片'" + " onclick=\"deletePic('" + type + "','" + index + "','logo_image"+ type + "','fileToUpload" + type + "')\" class=\"upload_warp_img_div_del\"></a>");
                                $img.closest(".upload_warp_left").removeClass("drag").removeClass("imgOnDarg");
                                imageViewer();
                                $img.nextAll("a.upload_date").text(DateFormat.format(new Date(), 'yyyy-MM-dd hh:mm'));//upload date
                                $("#itemCompleteId" + index).val(data.data.id);
                                $("#"+obj2).val("");
                            }
                            else if (data && data.message) {
                                $("#"+obj2).val("");
                                layerError(data.message, "错误提示");
                            }
                            else {
                                $("#"+obj2).val("");
                                layerError("添加附件异常", "错误提示");
                            }
                            isExecute = false;
                            return false;
                        },
                        error: function (e) {
                            $("#"+obj2).val("");
                            ajaxLogout(e.responseText,null,"添加附件错误，请重试!");
                            isExecute = false;
                        }
                    });
                },
                error: function (data, status, e) {
                    if(loadingIndex) {
                        layer.close(loadingIndex);
                    }
                    layerError(e, "错误提示");
                    isExecute = false;
                }
            });
        }
        
        function checkAttachment(type,index) {
            isExecute = true;
            var fileToUploadPicId = "fileToUpload" + type + index;
            var filepath = $("#" + fileToUploadPicId).val();
            if(Utils.isEmpty(filepath)){
                $("#" + fileToUploadPicId).val("");
                isExecute = false;
                return false;
            }
            var extStart=filepath.lastIndexOf(".");
            var ext=filepath.substring(extStart,filepath.length).toUpperCase();
            if(ext!=".BMP"&&ext!=".PNG"&&ext!=".GIF"&&ext!=".JPG"&&ext!=".JPEG"){
                layerInfo("图片类型限于bmp,png,gif,jpeg,jpg格式","系统提示");
                $("#" + fileToUploadPicId).val("");
                isExecute = false;
                return false;
            }
            //check size
            var files = document.getElementById(fileToUploadPicId).files;
            var fileSize = files[0].size;
            //var size = fileSize / 1024;
            var size = fileSize.toFixed(2);
            if(size > (2*1024*1024)){
                layerInfo("图片不能大于2M","系统提示");
                $("#" + fileToUploadPicId).val("");
                isExecute = false;
                return false;
            }
            uploadfile($("logo" + type + index),fileToUploadPicId,index,type);
        }

        //删除图片
        function deletePic(fieldName,idIndex,imageId,uploadImageId) {
            var clicktag = 0;
            top.layer.confirm('确定要删除该附件吗?', {icon: 3, title:'系统确认'}, function(index){
                if(clicktag == 1){
                    return false;
                }
                clicktag = 1;
                top.layer.close(index);//关闭本身
                $.ajax({
                    type:"post",
                    url: "${ctx}/sd/orderItemComplete/deletePic",
                    data: {
                        id:$("#itemCompleteId" + idIndex).val(),
                        field:fieldName
                    },
                    success:function (data) {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if (data && data.success == true) {
                            var $img = $("[id='" + imageId + idIndex+"']");
                            // $img.attr("onclick","$('#"+uploadImageId+idIndex+"').click();");
                            $img.attr("onclick","clickFile('#"+uploadImageId+idIndex+"');");
                            $img.attr("src","${ctxStatic}/images/upload-photo.png");
                            $img.attr("title","点击上传图片");
                            $img.removeAttr("data-original");
                            $img.closest(".upload_warp_left").addClass("drag");
                            //$img.viewer('destroy');
                            $img.prev().remove();
                            imageViewer();
                            if(data.data == 0){
                                $("#itemCompleteId" + idIndex).val("");
                                $("#unitCode"+idIndex).val("");
                            }
                        }
                        else if (data && data.message) {
                            layerError(data.message, "错误提示");
                        }
                        else {
                            layerError("删除附件异常", "错误提示");
                        }
                        return false;
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText,null,"删除附件失败，请重试!");
                    }

                });
            });
        }

        function editUnitCode(index,dataSource,b2bOrderNo){
            var completePicId = $("#itemCompleteId"+index).val();
            if(completePicId ==null || completePicId=='' || completePicId ==undefined){
                layerInfo("请先上传图片","系统提示");
                return false
            }
            var unitCode = $("#unitCode"+index).val();
            var quarter = $("#quarter").val();
            top.layer.open({
                type: 2,
                id:'layer_unitCode',
                zIndex:19891015,
                title:'修改条码',
                content: "${ctx}/sd/orderItemComplete/editBarcode?id="+ (completePicId || '')
                        + "&b2bOrderNo=" + (b2bOrderNo || '')
                         +"&parentIndex=" + (orderdetail_index || '')
                         +"&productIndex=" +(index || '')
                         +"&quarter=" + (quarter || '')
                         +"&dataSource=" + (dataSource || 0),
                area: ['530px', '380px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

        function updateBarCode(data,index){
            $("#itemCompleteId"+index).val(data.data.id);
            $("#unitCode"+index).val(data.data.unitBarcode);
        }

        /**
         * 查看已删除的照片记录
         */
        function getDeleteList(orderId,quarter) {
            var h = $(top.window).height();
            var w = $(top.window).width();
            top.layer.open({
                type: 2,
                id:'layer_deleteList',
                zIndex:19891015,
                title:'查看已删除记录',
                content: "${ctx}/sd/orderItemComplete/getDelListByOrderId?orderId="+ (orderId || '') + "&quarter=" + (quarter || ''),
                area:[(w-40)+'px',(h-40)+'px'],
                shade: 0.3,
                maxmin: true
            });
        }

        //取消
        function cancel() {
            top.layer.close(this_index);//关闭本身
        }

    </script>
    <style type="text/css">
        .form-horizontal{margin-top:5px;}
        .form-horizontal .control-label {width: 100px;margin-top: 20px;}
        .form-horizontal .controls {margin-left: 110px;}
        .upload_warp_img_div img{max-width:100%;max-height:100%;vertical-align:middle;width:100%;height: 100%;margin-bottom: 5px;}
        .upload_warp_img_div {position: relative;height: 100px;width: 100px;border: 1px solid #ccc;float: left;
            display: table-cell;text-align: center;background-color: #eee;cursor: pointer;margin: 5px;
        }
        .upload_warp_text{text-align:left;margin-top:10px;text-indent:14px;font-size:12px}
        .upload_date{text-align:left;margin-top:10px;text-indent:14px;font-size:12px}
        .upload_warp_left img{margin-top:0px}
        .upload_warp_left {float: left;width: 110px;height: 150px;border: 1px dashed #999;border-radius: 4px;cursor: pointer;
            margin-right: 10px;padding: 5px;
        }
        .upload_warp{text-align: center;display: inline-block;}
        .upload_warp_img_div .upload_warp_img_div_del{position:absolute;top:0px;width:20px !important;height:20px !important;right:0px;margin-top: 0px !important;
            background-size: 20px 20px !important;background:url('${ctxStatic}/images/delUploadFile.png') no-repeat; }

        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        .fromInput {
            border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;margin-top: 19px;
        }

        .imageFile {
            width: 130px;
            height: 130px;
            padding: 0px;
            margin-top: 10px;
            pxmargin-bottom: 0px;
            padding-left: 5%;
        }
        .imgOnDarg{
            border:2px dashed indianred;
            background: bisque;
        }

    </style>
</head>
<body style="margin: 0px 10px 3px 10px;">

<c:set var="addattach" value="0"/>
<shiro:hasPermission name="sd:order:addattach">
    <c:set var="addattach" value="1"/>
</shiro:hasPermission>
<c:set var="praiseAction" value="1"/>
<c:if test="${order.orderCondition.status.value>=80 and praiseFlag==0}">
    <c:set var="praiseAction" value="0"/>
</c:if>
<c:if test="${addattach == 1}">
    <c:if test="${praiseAction==1}">
        <ul class="nav nav-tabs" style="margin-left: 24px;margin-top: 24px">
            <li class="active"><a href="javascript:;">完工图片</a></li>
            <li><a href="${ctx}/praise/orderPraise/praiseForm?orderId=${order.id}&customerId=${order.orderCondition.customer.id}&quarter=${order.quarter}">好评图片</a></li>
        </ul>
    </c:if>
</c:if>
<sys:message content="${message}"/>
<div id="divMain" class="form-horizontal">
    <div class="row-fluid">
        <div class="span12">
            <div class="control-group">
                <div class="controls">
                    <a href="javascript:getDeleteList('${order.id}','${order.quarter}')" style="float: right">查看删除记录</a>
                </div>
            </div>
        </div>
    </div>
<c:if test="${!empty order.id }">
    <c:choose>
        <c:when test="${list !=null && fn:length(list) >0}">
            <c:forEach items="${list}" var="map" varStatus="i">
                <c:choose>
                    <c:when test="${!empty map.itemComplete}">
                        <div class="row-fluid">
                            <div class="span6">
                                <div class="control-group">
                                    <label class="control-label">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
                                    <div class="controls">
                                        <input class="fromInput input-block-level"  value="${map.completePic.product.name}" maxlength="30" readonly/>
                                    </div>
                                </div>
                            </div>
                            <div class="span6">
                                <div class="control-group">
                                    <label class="control-label">产品条码：</label>
                                    <div class="controls">
                                        <input class="fromInput" id="unitCode${i.index}" value="${map.itemComplete.unitBarcode}" maxlength="30" readonly/>&nbsp;
                                        <c:if test="${addattach eq 1}">
                                            <input id="btnSubmit${i.index}" class="btn btn-primary" style="margin-top: 19px" type="submit" onclick="editUnitCode('${i.index}','${dataSource}','${b2bOrderNo}')" value="编辑" />
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="row-fluid">
                            <div class="span12">
                                <div class="control-group">
                                    <label class="control-label" style="margin-top: 2px;">完成图片：</label>
                                    <input type="hidden" id="productId${i.index}" name="productId" value="${map.completePic.product.id}">
                                    <input type="hidden" id="itemCompleteId${i.index}" value="${map.itemComplete.id}">
                                    <div class="controls">
                                        <c:set var="uploaded" value="0" />
                                        <div class="upload_warp">
                                            <c:forEach items="${map.completePic.items}" var = "item">
                                                <c:set var="isHasValue" value="true" />
                                                <c:forEach items="${map.itemComplete.itemList}" var = "itemList">
                                                    <c:if test="${item.pictureCode eq itemList.pictureCode && isHasValue}">
                                                        <c:set var="isHasValue" value="false" />
                                                        <div class="upload_warp_left ${addattach eq 1 && itemList.url == ''?'drag':''}" data-code="${item.pictureCode}" data-index="${i.index}">
                                                            <input name="logo" id="logo${item.pictureCode}${i.index}" type="hidden" htmlEscape="false"/>
                                                            <input name="orignalName" id="orignalName${item.pictureCode}${i.index}" type="hidden" htmlEscape="false"/>
                                                            <input id="fileToUpload${item.pictureCode}${i.index}" type="file" class="hero-unit" style="display: none" size="20" name="fileToUploadlogo" value="${fileToUploadlogo}"
                                                                   onchange="checkAttachment('${item.pictureCode}','${i.index}')">
                                                            <div class="upload_warp_img_div">
                                                                <c:if test="${addattach eq 1}">
                                                                    <a href="javascript:;" title="点击删除图片" onclick="deletePic('${item.pictureCode}','${i.index}','logo_image${item.pictureCode}','fileToUpload${item.pictureCode}')" class="upload_warp_img_div_del"></a>
                                                                </c:if>
                                                                <img id="logo_image${item.pictureCode}${i.index}" title="点击放大图片" src="${ctxUpload}/${itemList.url}" data-original="${ctxUpload}/${itemList.url}"/>
                                                                <a class="upload_warp_text" href="javascript:void(0);" data-toggle="tooltip" title="${item.title}">${fns:abbr(item.title,14)}</a>
                                                                <br/>
                                                                <a class="upload_date" href="javascript:void(0);"><fmt:formatDate value="${itemList.uploadDate}" pattern="yyyy-MM-dd HH:mm"/></a>
                                                            </div>
                                                        </div>
                                                    </c:if>
                                                </c:forEach>
                                                <c:if test="${isHasValue}">
                                                    <div class="upload_warp_left ${addattach eq 1?'drag':''}" data-code="${item.pictureCode}" data-index="${i.index}">
                                                        <input name="logo" id="logo${item.pictureCode}${i.index}" type="hidden" htmlEscape="false"/>
                                                        <input name="orignalName" id="orignalNamePic2${i.index}" type="hidden" htmlEscape="false"/>
                                                        <input id="fileToUpload${item.pictureCode}${i.index}" type="file" class="hero-unit" style="display: none" size="20" name="fileToUploadlogo" value="${fileToUploadlogo}"
                                                               onchange="checkAttachment('${item.pictureCode}','${i.index}')" />
                                                        <div class="upload_warp_img_div">
                                                            <c:choose>
                                                                <c:when test="${addattach eq 1}">
                                                                    <img id="logo_image${item.pictureCode}${i.index}" title="点击上传图片" src="${ctxStatic}/images/upload-photo.png" onclick="clickFile('#fileToUpload${item.pictureCode}${i.index}')"/>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <img id="logo_image${item.pictureCode}${i.index}" title="点击上传图片" src="${ctxStatic}/images/upload-photo.png" />
                                                                </c:otherwise>
                                                            </c:choose>
                                                            <a class="upload_warp_text" href="javascript:void(0);" data-toggle="tooltip" title="${item.title}">${fns:abbr(item.title,14)}</a>
                                                            <br/>
                                                            <a class="upload_date" href="javascript:void(0);">${item.uploadDate}</a>
                                                        </div>
                                                    </div>
                                                </c:if>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:when test="${map.completePic!=null && (map.completePic.items ==null || fn:length(map.completePic.items) <=0)}">
                        <div class="row-fluid">
                            <div class="span6">
                                <div class="control-group">
                                    <label class="control-label">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
                                    <div class="controls">
                                        <input class="fromInput input-block-level"  value="${map.completePic.product.name}" maxlength="30" readonly/>
                                    </div>
                                </div>
                            </div>
                            <div class="span6">
                                <div class="control-group">
                                    <label class="control-label">产品条码：</label>
                                    <div class="controls">
                                        <input class="fromInput" id="unitCode${i.index}" value="" maxlength="30" readonly/>&nbsp;
                                        <c:if test="${addattach eq 1}">
                                            <input id="btnSubmit${i.index}" class="btn btn-primary" style="margin-top: 19px" type="submit" onclick="editUnitCode('${i.index}','${dataSource}','${b2bOrderNo}')" value="编辑" />
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                            <div class="span12">
                                <div class="control-group">
                                    <input type="hidden" id="productId${i.index}" name="productId" value="${map.completePic.product.id}">
                                    <input type="hidden" id="itemCompleteId${i.index}" value="">
                                   <%-- <label class="control-label">${map.completePic.product.name}</label>--%>
                                    <div class="controls">
                                        <div class="upload_warp">
                                            <div class="alert alert-false">【${map.completePic.product.name}】未配置完成照片</div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="row-fluid">
                            <div class="span6">
                                <div class="control-group">
                                    <label class="control-label">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
                                    <div class="controls">
                                        <input class="fromInput input-block-level"  value="${map.completePic.product.name}" maxlength="30" readonly/>
                                    </div>
                                </div>
                            </div>
                            <div class="span6">
                                <div class="control-group">
                                    <label class="control-label">产品条码：</label>
                                    <div class="controls">
                                        <input class="fromInput" id="unitCode${i.index}" value="" maxlength="30" readonly/>&nbsp;
                                        <c:if test="${addattach eq 1}">
                                            <input id="btnSubmit${i.index}" class="btn btn-primary" style="margin-top: 19px" type="submit" onclick="editUnitCode('${i.index}','${dataSource}','${b2bOrderNo}')" value="编辑" />
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="row-fluid">
                            <div class="span12">
                                <div class="control-group">
                                    <label class="control-label" style="margin-top: 2px;">完成图片：</label>
                                    <input type="hidden" id="productId${i.index}" name="productId" value="${map.completePic.product.id}">
                                    <input type="hidden" id="itemCompleteId${i.index}" value="">
                                        <%-- <label class="control-label">${map.completePic.product.name}</label>--%>
                                    <div class="controls">
                                        <div class="upload_warp">
                                            <c:forEach items="${map.completePic.items}" var = "item">
                                                <div class="upload_warp_left  ${addattach eq 1?'drag':''}" data-code="${item.pictureCode}" data-index="${i.index}">
                                                    <input name="logo" id="logo${item.pictureCode}${i.index}" type="hidden" htmlEscape="false"/>
                                                    <input name="orignalName" id="orignalNamePic2${i.index}" type="hidden" htmlEscape="false"/>
                                                    <input id="fileToUpload${item.pictureCode}${i.index}" type="file" class="hero-unit" style="display: none" size="20" name="fileToUploadlogo" value="${fileToUploadlogo}"
                                                           onchange="checkAttachment('${item.pictureCode}','${i.index}')" />
                                                    <div class="upload_warp_img_div">
                                                        <c:choose>
                                                            <c:when test="${addattach eq 1}">
                                                                <img id="logo_image${item.pictureCode}${i.index}" title="点击上传图片" src="${ctxStatic}/images/upload-photo.png" onclick="clickFile('#fileToUpload${item.pictureCode}${i.index}')"/>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <img id="logo_image${item.pictureCode}${i.index}" title="点击上传图片" src="${ctxStatic}/images/upload-photo.png" />
                                                            </c:otherwise>
                                                        </c:choose>
                                                        <a class="upload_warp_text" href="javascript:void(0);" data-toggle="tooltip" title="${item.title}">${fns:abbr(item.title,14)}</a>
                                                        <br/>
                                                        <a class="upload_date" href="javascript:void(0);">${item.uploadDate}</a>
                                                    </div>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <div id="messageBox" class="alert alert-false"><button data-dismiss="alert" class="close">×</button>此工单产品还没有配置产品完成图片</div>
        </c:otherwise>
    </c:choose>
</c:if>
<div style="height: 60px;width: 100%"></div>
<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
    <hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
    <div style="float: right;margin-right: 60px">
       <input id="btnCancel" class="btn" type="button" value="关 闭" style="margin-top:10px"onclick="cancel()"/>
    <div>
</div>
<fieldset style="margin: 0px 5px 0px 5px;">
    <input type="hidden" id="id" name="id" value="${order.id}"/>
    <input type="hidden" id="quarter" name="quarter" value="${order.quarter}"/>
    <input type="hidden" id="customerId" name="customerId" value="${order.orderCondition.customer.id}"/>
</fieldset>
</div>
</body>
</html>