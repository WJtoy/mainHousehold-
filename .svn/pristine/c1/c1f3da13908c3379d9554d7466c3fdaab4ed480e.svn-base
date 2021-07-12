<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>完成照片</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
    <script src="${ctxStatic}/js/ajaxfileupload.js"></script>
    <!-- image viewer -->
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <script type="text/javascript">
        var this_index = top.layer.index;
        Order.rootUrl = "${ctx}";
        var clickTag = 0;
        $(document).ready(function () {
            $("#buttonUploadLogo").click(function () {
                var $btn = $(self);
                if (clickTag == 1){
                    return false;
                }
                clickTag = 1;
                $btn.attr('disabled', 'disabled');
                uploadfile($("#logo"), $("#logo_image"), "fileToUploadlogo");
                return false;
            });
            var $tbviewer = $('#tb_images');
            $tbviewer.viewer({
                viewed: function() {

                    //this.viewer.zoomTo(1);//100%原始大小显示
                }
            });
        });

        //检查附件类型
        $("#fileToUploadlogo").change(function () {
            var filepath=$("#fileToUploadlogo").val();
            if(Utils.isEmpty(filepath)){
                return false;
            }
            var extStart=filepath.lastIndexOf(".");
            var ext=filepath.substring(extStart,filepath.length).toUpperCase();
            if(ext!=".BMP"&&ext!=".PNG"&&ext!=".GIF"&&ext!=".JPG"&&ext!=".JPEG"){
                layerInfo("图片类型限于bmp,png,gif,jpeg,jpg格式","系统提示");
                $("#buttonUploadLogo").attr("disabled", true);
                return false;
            }
            //check size
            var files = document.getElementById("fileToUploadlogo").files;
            var fileSize = files[0].size;
            // var size = fileSize / 1024;
            var size = fileSize.toFixed(2);
            if(size > (4*1024*1024)){
                layerInfo("图片不能大于4M","系统提示");
                $("#fileToUploadlogo").val("");
                return false;
            }
            $("#buttonUploadLogo").removeAttr("disabled");
            return true;
        });

        function uploadfile($obj1, $obj1_image, obj2) {
            var filepath = $("#"+obj2).val();
            if(Utils.isEmpty(filepath)){
                layerError("请先选择文件", "错误提示");
                clickTag = 0;
                return false;
            }
            var data = {
                fileName: $obj1.val()
            };
            var $btnSubmit = $("#buttonUploadLogo");
            $.ajaxFileUpload({
                url: '${pageContext.request.contextPath}/servlet/Upload?fileName=' + $obj1.val(),//处理图片脚本
                secureuri: false,
                data: data,
                fileElementId: obj2,//file控件id
                dataType: 'json',
                success: function (data, status) {
                    $obj1.val(data.fileName);
                    $("#orignalName").val(data.origalName);

                    if (data.origalName == "") {
                        layerError("请先选择文件，再点击上传", "错误提示");
                        clickTag = 0;
                        $btnSubmit.removeAttr("disabled");
                        return true;
                    }
                    //Ajax 保存附件信息
                    $.ajax({
                        type: "POST",
                        url: "${ctx}/sd/order/saveAttach",
                        data: {
                            filePath: $("#logo").val(),
                            orginalName: $("#orignalName").val(),
                            orderId: $("#id").val(),
                            quarter: $("#quarter").val()
                        },
                        complete: function () {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                        },
                        success: function (data) {
                            if(ajaxLogout(data)){
                                return false;
                            }
                            if (data && data.success == true) {
                                window.location = window.location;
                            }
                            else if (data && data.message) {
                                layerError(data.message, "错误提示");
                            }
                            else {
                                layerError("添加附件异常", "错误提示");
                            }
                            return false;
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"添加附件错误，请重试!");
                        }
                    });
                },
                error: function (data, status, e) {
                    layerError(e, "错误提示");
                }
            });
        }
    </script>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
</head>
<body style="margin: 0px 10px 3px 10px;">
<shiro:hasPermission name="sd:order:addattach">
<%--<legend style="margin: 0px 5px 0px 5px;">上传新的完成照片</legend>
<div style="margin: 10px 0;">
    <c:if test="${!empty order.id }">
        <img id="logo_image" class="logo_image" alt=""/>
        <input name="logo" id="logo" type="hidden" htmlEscape="false"/>
        <input name="orignalName" id="orignalName" type="hidden" htmlEscape="false"/>
        <input id="fileToUploadlogo" type="file" class="hero-unit" style=" padding: 0px; width: 400px; margin-bottom: 0px;height: 55px" size="20" name="fileToUploadlogo" value="${fileToUploadlogo}">
        <button id="buttonUploadLogo" type="button" class="btn">上传</button>
    </c:if>
</div>--%>
</shiro:hasPermission>
<fieldset style="margin: 0px 5px 0px 5px;">
    <input type="hidden" id="id" name="id" value="${order.id}"/>
    <input type="hidden" id="quarter" name="quarter" value="${order.quarter}"/>
    <legend>${order.orderNo} 已完成照片列表</legend>
    <table width="100%" border="0" id="tb_images"
           class="table table-striped table-bordered table-condensed"
           style="margin-bottom: 0px;">
        <thead>
        <tr>
            <th width="85%">图片</th>
            <th width="15%">上传时间</th>
            <shiro:hasPermission name="sd:order:delattach">
            <th></th>
            </shiro:hasPermission>
        </tr>
        </thead>
        <tbody>
        <c:if test="${order != null && order.attachments != null && fn:length(order.attachments) >0}">
        <c:set var="index" value="0"></c:set>
        <c:forEach items="${order.attachments}" var="attach">
            <c:set var="index" value="${index+1}"></c:set>
            <tr id="accesstr${index}">
                <td>
                    <c:set var="userType" value="${attach.createBy.userType}" />
                    <c:set var="userTypeName" value="" />
                    <c:choose>
                        <c:when test="${userType == 0}"><c:set var="userTypeName" value="" /> </c:when>
                        <c:when test="${userType == 1 || userType == 2}"><c:set var="userTypeName" value="(客服)" /> </c:when>
                        <c:when test="${userType == 3 || userType == 4 || userType == 9}"><c:set var="userTypeName" value="(厂商)" /></c:when>
                        <c:when test="${userType == 5}"><c:set var="userTypeName" value="(网点)" /></c:when>
                        <c:when test="${userType == 7}"><c:set var="userTypeName" value="(业务)" /></c:when>
                        <c:otherwise></c:otherwise>
                    </c:choose>
                    <a href="javascript:;" >
                        <img title="${attach.remarks}" alt="上传人[${attach.createBy.name}]${userTypeName}" src="${ctxUpload}/${attach.filePath}"  data-original="${ctxUpload}/${attach.filePath}" style="max-width: 800px;">
                    </a>
                    <br/>上传人[${attach.createBy.name}](${userTypeName})
                </td>
                <td>
                    <fmt:formatDate value="${attach.createDate}" pattern="yyyy-MM-dd HH:mm"/>
                </td>
                <shiro:hasPermission name="sd:order:delattach">
                <td>
                    <a class="" href="#" data-rowid="${index}"
                       onclick="Order.deleteServiceAttachment(${index});" title="刪除"><i class="icon-delete" style="margin-top: 0px;"></i>
                    </a>
                    <input type="hidden" id="id${index}" name="id${index}" value="${attach.id}" />
                </td>
                </shiro:hasPermission>
            </tr>
        </c:forEach>
        </c:if>
        <tbody>
    </table>
</fieldset>
</body>
</html>