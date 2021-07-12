<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>图库选择窗口</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <script src="${ctxStatic}/sd/SecondOrder.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <!-- image viewer -->
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <!-- upload -->
    <link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
    <script src="${ctxStatic}/js/ajaxfileupload.js"></script>
    <!-- image picker -->
    <link href="${ctxStatic}/jquery-imagepicker/image-picker.css" rel="stylesheet">
    <script src="${ctxStatic}/jquery-imagepicker/image-picker.min.js"></script>
    <%@ include file="/WEB-INF/views/modules/sd/secondOrder/tpl/galleryUpload.html" %>
    <script type="text/javascript">
        SecondOrder.rootUrl = "${ctx}";
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
        var this_index = top.layer.index;
        var clickTag = 0;
        $(document).ready(function() {
            //删除图片
            $("a.thumbnail_del").off().on("click",SecondOrder.deleteGallery);
            //select gallery
            <c:if test="${idList.size()>0}">
            <c:forEach items="${idList}" var="gelleryId">
            $("ul.thumbnails").find("div.thumbnail[data-option-value='" + ${gelleryId} + "']").trigger("click");
            </c:forEach>
            </c:if>
        });
    </script>
    <style type="text/css">
        .gallery-button {padding: 4px 20px 4px 20px;}
        #tab1 { max-height: 380px;overflow-x: hidden;overflow-y: auto;}
        .alert-info {color: #178acc;background-color: #d9edf7;border-color: #bce8f1;}
        .thumbnail_del {height: 80px;width: 80px !important;position: absolute;top:0px;width:20px !important;
            height:20px !important;right:0px;margin-top: 0px !important;background-size: 20px 20px !important;
            background: url('${ctxStatic}/images/gallery/icon-del-gallery.png') no-repeat;
        }
    </style>
</head>
<body style="margin: 10px 10px 3px 10px;">
<input type="hidden" id="customerId" name="customerId" alt="客户" value="${customerId}"/>
<input type="hidden" id="productCategoryId" name="productCategoryId" alt="品类" value="${productCategoryId}"/>
<input type="hidden" id="productTypeId" name="productTypeId" alt="产品分类" value="${productTypeId}"/>
<input type="hidden" id="productTypeItemId" name="productTypeItemId" alt="产品二级分类" value="${productTypeItemId}"/>
<input type="hidden" id="limitQty" name="limitQty" alt="可选择图片数量" value="${limitQty}"/>
<input type="hidden" id="maxPicQty" name="maxPicQty" alt="图库最大文件数量" value="${maxPicQty}"/>
<sys:message content="${message}"/>
<div class="row">
<div class="tabbable">
    <ul class="nav nav-tabs">
        <li class="active"><a href="#tab1" id="lnkGalleryTab" data-toggle="tab">从图库选择</a></li>
        <li><a id="lnkUploadTab" href="#tab2" data-toggle="tab">本地上传</a></li>
    </ul>
    <div class="tab-content" style="height: 380px;">
        <!-- gallery -->
        <div class="tab-pane active" id="tab1">
            <div class="alert alert-info">
                 图库最多可上传 <strong>${maxPicQty}</strong> 张图片.
            </div>
            <div id="divGallery" class="controls">
                <select id="selGallery" multiple="multiple" data-limit="${limitQty}" class="image-picker noselect2" style="display: none;">
                    <c:forEach items="${gallery}" var = "item">
                        <option data-img-src="${ctxUpload}/${item.picUrl}" value="${item.id}">${item.id}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="tab-pane " id="tab2">
            <div class="upload row-fluid">
                <div class="row">
                    <div class="span2"></div>
                    <div id="divUploadFile" class="upload_warp span8">
                        <div id="btnUploadFile" class="upload_warp_left"></div>
                        <div class=" upload_warp_right upload_warp_img" style="display: none;"></div>
                        <input id="upload_file" name="upload_file" accept="image/gif,image/jpeg,image/png" type="file" multiple="" style="display: none">
                    </div>
                    <div class="span2"></div>
                </div>
                <div class="row">
                    <div class="span2"></div>
                    <div class="span8" style="margin-top: 10px;">
                        点击或将文件拖到此处上传图片
                    </div>
                    <div class="span2"></div>
                </div>
                <div class="row">
                    <div class="span2"></div>
                    <div class="span8" style="text-align: center;color:#bbb;">
                        图片大小不能超过<strong style="color:#ee4247">2MB</strong>，仅支持jpg,png,gif格式
                    </div>
                    <div class="span2"></div>
                </div>
            </div>
        </div>
    </div>
</div>
</div>
<%--<div class="row" align="right" style="padding-right: 20px;padding-bottom: 2px;">--%>
<%--    <a id="btnSelect" class="btn btn-primary gallery-button" style="margin-right: 10px;" href="javascript:;">确定</a>--%>
<%--    <a id="btnClose" class="btn gallery-button" href="javascript:;">关闭</a>--%>
<%--</div>--%>
</body>
</html>