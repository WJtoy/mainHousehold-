<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>浏览完成照片</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
    <!-- image viewer -->
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        var viewer;
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
        });

        function clickFile(id){
            $(id).click();
        }
    </script>
    <style type="text/css">
        .form-horizontal{margin-top:5px;}
        .form-horizontal .control-label {width: 100px;margin-top: 20px;}
        .form-horizontal .controls {margin-left: 120px;}
        .upload_warp_img_div img{max-width:100%;max-height:100%;vertical-align:middle;width:100%;height: 100%;margin-bottom: 5px;}
        .upload_warp_img_div {position: relative;height: 130px;width: 130px;border: 1px solid #ccc;float: left;
            display: table-cell;text-align: center;background-color: #eee;cursor: pointer;
        }
        .upload_warp_text{text-align:left;margin-top:10px;text-indent:14px;font-size:14px}
        .upload_date{text-align:left;margin-top:10px;text-indent:14px;font-size:14px}
        .upload_warp_left img{margin-top:0px}
        .upload_warp_left {float: left;width: 130px;height: 180px;border: 1px dashed #999;border-radius: 4px;cursor: pointer;
            margin-right: 10px;padding: 5px;
        }
        .upload_warp{margin:8px;text-align: center;display: inline-block;}
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
<div id="divMain" class="form-horizontal">
<c:choose>
    <c:when test="${list !=null && list.size() >0}">
        <c:forEach items="${list}" var="item" varStatus="i">
            <div class="row-fluid">
                <div class="span12">
                    <div class="control-group">
                        <label class="control-label">${item.product.name}</label>
                        <label class="control-label">产品条码:</label>
                        <div class="controls">
                            <input class="fromInput"  value="${item.unitBarcode}" readonly/>&nbsp;
                        </div>
                    </div>
                </div>
                <div class="span12">
                    <div class="control-group">
                        <div class="controls">
                            <div class="upload_warp">
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
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </c:forEach>
    </c:when>
    <c:otherwise>
        <div id="messageBox" class="alert alert-false"><button data-dismiss="alert" class="close">×</button>此工单还未上传完成图片</div>
    </c:otherwise>
</c:choose>
</div>
</body>
</html>