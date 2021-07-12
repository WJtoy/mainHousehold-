<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>删除记录</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
    <script src="${ctxStatic}/js/ajaxfileupload.js"></script>
    <!-- image viewer -->
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <script type="text/javascript">
        $(document).ready(function () {
            $('#divMain').viewer();
        });

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
            background-size: 20px 20px}

        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
</head>
<body style="margin: 0px 10px 3px 10px;">

<legend style="margin: 0px 5px 0px 5px;">已删除完成照片</legend>
<div id="divMain" class="form-horizontal">
    <c:choose>
        <c:when test ="${!empty picCompleteList && fn:length(picCompleteList) >0}">
            <c:forEach items="${picCompleteList}" var="item">
              <div class="row-fluid">
                <div class="span12">
                    <div class="control-group">
                        <label class="control-label">${item.product.name}:</label>
                        <div class="controls">
                            <div class="upload_warp">
                                <c:forEach items="${item.itemList}" var="picItem">
                                    <div class="upload_warp_left">
                                        <div class="upload_warp_img_div">
                                            <img title="点击放大图片" src="${ctxUpload}/${picItem.url}" data-original="${ctxUpload}/${picItem.url}"/>
                                            <a class="upload_warp_text" href="javascript:void(0);" data-toggle="tooltip" title="${picItem.title}">${fns:abbr(picItem.title,14)}</a>
                                            <br/>
                                            <a class="upload_date" href="javascript:void(0);"><fmt:formatDate value="${picItem.uploadDate}" pattern="yyyy-MM-dd HH:mm"/></a>
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
            <div class="alert alert-false" style="text-align: center"><button data-dismiss="alert" class="close">×</button>无记录</div>
        </c:otherwise>
    </c:choose>
</div>
</body>
</html>