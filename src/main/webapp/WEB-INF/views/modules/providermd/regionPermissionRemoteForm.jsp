<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>远程区域设置</title>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/area/Area-1.2.js" type="text/javascript"></script>
    <script src="${ctxStatic}/layui/layui.js"></script>
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/layui/css/layui.css">
    <c:set var="currentuser" value="${fns:getUser() }"/>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }

        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 0px;
            width: 100%;
            height: 55px;
            background: #fff;
            z-index: 10;
            border-top: 1px solid #ccc;
            border-top: 1px solid #e5e5e5;
            text-align: right;
        }
        .bread_crumb {
            padding: 8px 15px;
            margin: 0px 0px 20px;
            list-style: none;
            border-radius: 4px;
            margin: 12px 0px 0 9px;
        }
    </style>
    <script type="text/javascript">
        var this_index = top.layer.index;
        function child(obj) {
            var data = eval(obj);
            $("#productCategoryName").val(data.productCategoryName);
        }

        $(document).ready(function () {
            var $btnSubmit = $("#btnSubmit");

            // 多选框级联操作
            layui.use('form', function () {
                var form = layui.form,
                $ = layui.$;

                form.render();
                form.on('checkbox(city)', function (data) {
                    if (data.elem.checked) {
                        $(":checkbox[name='area']").attr("checked", "checked");
                        $(":checkbox[name='subArea']").attr("checked", "checked");
                        form.render();
                    } else {
                        $(":checkbox[name='area']").removeAttr("checked");
                        $(":checked[name='subArea']").removeAttr("checked");
                        form.render();
                    }
                });

                form.on('checkbox(area)', function (data) {
                    var parent = data.elem.getAttribute("data-cityid");
                    if (data.elem.checked) {
                        $("input[type='checkbox'][name='subArea'][data-countyid=" + data.value + "]").attr("checked", "checked");
                    } else {
                        $("input[type='checkbox'][name='subArea'][data-countyid=" + data.value + "]").removeAttr("checked");
                    }
                    $("input[type='checkbox'][name='city'][value=" + parent + "]").attr("checked", "checked");
                    form.render();
                });

                form.on('checkbox(subArea)', function (data) {
                    var parent = data.elem.getAttribute("data-countyid");
                    $("input[type='checkbox'][name='area'][value=" + parent + "]").attr("checked", "checked");
                    $("input[type='checkbox'][name='city']").attr("checked", "checked");
                    form.render();
                });

            });


            var clickTag = 0;
            var areas = [];
            $("#btnSubmit").click(function () {
                if (clickTag == 1) {
                    return false;
                }

                var productCategoryId = $("#productCategoryId").val();

                areas = $('input[name="subArea"]:checked').map(function() {
                    return $(this).val();
                });

                var entity = {};
                var groupType = $("#groupType").val();
                $("input[type='checkbox'][name='subArea']").each(function (index, element) {
                    var countyid = $(this).data("countyid");// 获取区县id
                    var checked = $("input[type='checkbox'][name='area'][value=" + countyid + "]").prop('checked');

                    entity['regionPermissions[' + index + '].provinceId'] = parseInt($("#parentId").val());
                    entity['regionPermissions[' + index + '].cityId'] = parseInt($("#subCityId").val());
                    var countyId = countyid;// 获取区县id
                    entity['regionPermissions[' + index + '].areaId'] = parseInt(countyId);
                    var subAreaId = $(this).val();
                    entity['regionPermissions[' + index + '].subAreaId'] = parseInt(subAreaId);
                    entity['regionPermissions[' + index + '].productCategoryId'] = parseInt(productCategoryId);
                    entity['regionPermissions[' + index + '].type'] = 2;
                    entity['regionPermissions[' + index + '].groupType'] = parseInt(groupType);
                    if (checked) {
                        var schecked = $("input[type='checkbox'][name='subArea'][value=" + subAreaId + "]").prop('checked');
                        if (schecked) {
                            entity['regionPermissions[' + index + '].status'] = 1;
                        } else {
                            entity['regionPermissions[' + index + '].status'] = 0;
                        }
                    } else {
                        entity['regionPermissions[' + index + '].status'] = 0;
                    }
                });


                clickTag = 1;
                $btnSubmit.attr('disabled', 'disabled');
                var loadingIndex;
                var options = {
                    url: "${ctx}/provider/md/regionPermissionNew/save",  //默认是form的action， 如果申明，则会覆盖
                    type: 'post',               //默认是form的method（get or post），如果申明，则会覆盖
                    dataType: 'json',           //html(默认), xml, script, json...接受服务端返回的类型
                    data: entity,
                    beforeSubmit: function (formData, jqForm, options) {
                        loadingIndex = layer.msg('正在提交，请稍等...', {
                            icon: 16,
                            time: 0,
                            shade: 0.3
                        });
                        return true;
                    },  //提交前的回调函数
                    success: function (data) {
                        //提交后的回调函数
                        if (loadingIndex) {
                            layer.close(loadingIndex);
                        }
                        if (ajaxLogout(data)) {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            return false;
                        }
                        if (data.success) {
                            clickTag = 0;
                            top.layer.close(this_index);
                            var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                            if (pframe) {
                                pframe.setPage();
                            }
                        } else {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            layerError("数据保存错误:" + data.message, "错误提示");
                        }
                        return false;
                    },
                    error: function (data) {
                        setTimeout(function () {
                            clickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                        }, 2000);
                        ajaxLogout(data, null, "数据保存错误，请重试!");
                    }
                };
                $("#submitForm").ajaxSubmit(options);
            });
        });

        function load(){
            var loadingIndex = top.layer.msg('正在加载，请稍等...', {
                icon: 16,
                time: 0,//不定时关闭
                shade: 0.3
            });
            var data = {
                "cityId" : $("#subCityId").val(),
                "productCategoryId" : $("#productCategoryId").val(),
                "groupType" : $("#groupType").val(),
                "status" : 1
            };
            $.ajax({
                url: "${ctx}/provider/md/regionPermissionNew/findRegionPermission",
                type: 'post',
                data : data,
                success : function (result) {
                    top.layer.close(loadingIndex);
                    if (!result.success) {
                        layerError("数据加载失败:" + result.message, "错误提示");
                    } else {
                        layui.use('form', function () {
                            var form = layui.form,
                                $ = layui.$;
                            $('input[type=checkbox][name=subArea]').attr('checked', false);
                            $('input[type=checkbox][name=city]').attr('checked', false);
                            $('input[type=checkbox][name=area]').attr('checked', false);
                            var subAreaCheck = [];
                            if (result.data && result.data.length > 0) {
                                subAreaCheck = result.data;
                                for (var i in result.data) {
                                    var entity = subAreaCheck[i];
                                    var query = ":checkbox[name='subArea'][value=" + entity.subAreaId + "]";
                                    $(query).attr("checked", "checked");

                                    var query1 = ":checkbox[name='area'][value=" + entity.areaId + "]";
                                    $(query1).attr("checked", "checked");

                                    var query2 = ":checkbox[name='city'][value=" + entity.cityId + "]";
                                    $(query2).attr("checked", "checked");

                                    form.render();
                                }
                            } else {
                                $('input[type=checkbox][name=subArea]').attr('checked', false);
                                $('input[type=checkbox][name=city]').attr('checked', false);
                                $('input[type=checkbox][name=area]').attr('checked', false);
                                form.render();
                            }
                        });
                    }
                }
            });

        }
    </script>
</head>
<body>

<form:form id="search_form" modelAttribute="regionPermission" action="" method="post" class="bread_crumb form-search">
    <div class="controls">
        <label class="control-label">产品品类:</label>
        <input id="productCategoryName" class="input-medium valid" style="width:200px;" readonly="readonly" type="text" value="" aria-invalid="false">
    </div>
</form:form>

<sys:message content="${message}"/>

<%--远程--%>
<input type="hidden" id="type" value="${regionPermission.type}">

<input type="hidden" id="groupType" value="${regionPermission.groupType}">
<%--品类id--%>
<input type="hidden" id="productCategoryId" value="${regionPermission.productCategoryId}">
<%--市id--%>
<input type="hidden" id="subCityId" value="${regionPermission.cityId}">
<%--省id--%>
<input type="hidden" value="${area.parent.id}" id="parentId">
<div class="layui-form" style="text-align: center;">
    <div style="height: 695px;overflow: auto;">
        <table id="contentTable" class="layui-table" style="width: 95%;margin: 10px auto;">
            <thead>
                <tr>
                    <th style="width:30px;text-align: center">市</th>
                    <th style="width:30px;text-align: center">区县</th>
                    <th style="width: 100px;text-align: center">街道</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td rowspan="${areaModelList.size() > 0 ? areaModelList.size() : ''}">
                        <input type="checkbox" name="city" lay-filter="city" data-areaid="${area.id}" value="${area.id}"
                               lay-skin='primary'>${area.name}
                    </td>
                <c:choose>
                    <c:when test="${areaModelList.size() > 0}">
                    <c:forEach items="${areaModelList}" var="areaCounty" varStatus="i">
                    <c:choose>
                        <c:when test="${i.index == 0}">
                            <td>
                                <input type="checkbox" name="area" lay-filter="area" data-cityid="${areaCounty.parent.id}"
                                       value="${areaCounty.id}" lay-skin='primary'>${areaCounty.name}
                            </td>
                            <c:choose>
                                <c:when test="${areaCounty.subAreas.size()>0}">
                                <td style="width: 75%;line-height: 35px;">
                                    <c:forEach items="${areaCounty.subAreas}" var="subArea" varStatus="j">
                                        <div style="width: 20%;float: left;text-align: left;">
                                            <input type="checkbox" name="subArea" lay-filter="subArea" data-countyid="${areaCounty.id}"
                                                   value="${subArea.id}" lay-skin='primary'>${subArea.name}
                                        </div>
                                    </c:forEach>
                                </td>
                                </c:when>
                                <c:otherwise>
                                    <td style="width: 75%;line-height: 35px;">

                                    </td>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td>
                                    <input type="checkbox" name="area" lay-filter="area" data-cityid="${areaCounty.parent.id}"
                                           value="${areaCounty.id}" lay-skin='primary'>${areaCounty.name}
                                </td>
                            <c:choose>
                                <c:when test="${areaCounty.subAreas.size()>0}">
                                    <td style="width: 75%;line-height: 35px;">
                                        <c:forEach items="${areaCounty.subAreas}" var="subArea" varStatus="j">
                                            <div style="width: 20%;float: left;text-align: left;">
                                                <input type="checkbox" name="subArea" lay-filter="subArea"
                                                       data-countyid="${areaCounty.id}" value="${subArea.id}"
                                                       lay-skin='primary'>${subArea.name}
                                            </div>
                                        </c:forEach>
                                    </td>
                                </c:when>
                                <c:otherwise>
                                    <td style="width: 75%;line-height: 35px;">

                                    </td>
                                </c:otherwise>
                            </c:choose>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </c:forEach>
                    </c:when>

                    <c:otherwise>
                        <td></td>
                        <td></td>
                    </c:otherwise>
                    </c:choose>
                </tr>
            </tbody>
        </table>
    </div>

</div>

<div id="editBtn">
    <input id="btnSubmit" class="btn btn-primary layui-btn" type="button" lay-submit lay-filter="formSave" value="保 存"
           style="margin-left: 410px;margin-top: 10px;width: 85px;background: #0096DA;border-radius: 4px;"/>
    <input id="btnCancel" class="btn layui-btn layui-btn-primary" type="button" value="取 消"
           style="margin-right: 40px;margin-top:10px;width: 85px;border-radius: 4px;" onclick="cancel()"/>
</div>
<form:form id="submitForm"></form:form>
<script class="removedscript" type="text/javascript">
    // 关闭页面
    function cancel() {
        top.layer.close(this_index);// 关闭本身
    }

    $(document).ready(function() {
       load();
    });

</script>
</body>
</html>
