<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>区域设置</title>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/area/Area-1.2.js" type="text/javascript"></script>
    <c:set var="currentuser" value="${fns:getUser() }"/>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
            height: 32px;
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
        .table-striped tbody>tr:nth-child(odd)>td, .table-striped tbody>tr:nth-child(odd)>th {
             background-color: #ffffff;
        }
        .keFu{
            background-color: #EEF8E8;color: #34C758;padding: 2px 4px;border-radius: 3px;font-size: 12px;
        }
        .tuji{
            background-color: #FEEEEE;color: #F54142;padding: 2px 4px;border-radius: 3px;font-size: 12px;
        }
        .auto{
            background-color: #E9F3FF;color: #1D89FF;padding: 2px 4px;border-radius: 3px;font-size: 12px;
        }
        .unselected{
            background-color: #F8F8F9;color: #808695;padding: 2px 4px;border-radius: 3px;font-size: 12px;
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


            var clickTag = 0;

            $("#btnSubmit").click(function () {
                if (clickTag == 1) {
                    return false;
                }

                var productCategoryId = $("#productCategoryId").val();

                var entity = {};
                var index = 0;
                $("input[name='tuji']:checked").each(function() {
                    var areaId = $(this).data("areaid");// 获取区县id
                    var subAreaId =  $(this).data("subareaid");// 获取街道id
                    entity['regionPermissions[' + index + '].provinceId'] = parseInt($("#parentId").val());
                    entity['regionPermissions[' + index + '].cityId'] = parseInt($("#subCityId").val());
                    entity['regionPermissions[' + index + '].areaId'] = parseInt(areaId);
                    entity['regionPermissions[' + index + '].subAreaId'] = parseInt(subAreaId);
                    entity['regionPermissions[' + index + '].productCategoryId'] = parseInt(productCategoryId);
                    entity['regionPermissions[' + index + '].type'] = 1;
                    entity['regionPermissions[' + index + '].groupType'] = parseInt($("#groupType").val());
                    entity['regionPermissions[' + index + '].status'] = 1;
                    index++
                });
                $("input[name='tuji']:not(:checked)").each(function() {
                    var areaId = $(this).data("areaid");// 获取区县id
                    var subAreaId =  $(this).data("subareaid");// 获取街道id
                    var type = 0;
                    var value = $("[id=" +areaId +"_3][data-areaId=" + areaId +"]").data("value");
                    entity['regionPermissions[' + index + '].provinceId'] = parseInt($("#parentId").val());
                    entity['regionPermissions[' + index + '].cityId'] = parseInt($("#subCityId").val());
                    entity['regionPermissions[' + index + '].areaId'] = parseInt(areaId);
                    entity['regionPermissions[' + index + '].subAreaId'] = parseInt(subAreaId);
                    entity['regionPermissions[' + index + '].productCategoryId'] = parseInt(productCategoryId);
                    if(value == 0){
                        type = 2
                    }else if(value == 1){
                        type = 3
                    }
                    entity['regionPermissions[' + index + '].type'] = type;
                    entity['regionPermissions[' + index + '].groupType'] = parseInt($("#groupType").val());
                    entity['regionPermissions[' + index + '].status'] = 1;
                    index++
                });
                // $(".keFu").each(function () {
                //     var areaId = $(this).data("areaid");// 获取区县id
                //     var subAreaId =  $(this).data("subareaid");// 获取街道id
                //     entity['regionPermissions[' + index + '].provinceId'] = parseInt($("#parentId").val());
                //     entity['regionPermissions[' + index + '].cityId'] = parseInt($("#subCityId").val());
                //     entity['regionPermissions[' + index + '].areaId'] = parseInt(areaId);
                //     entity['regionPermissions[' + index + '].subAreaId'] = parseInt(subAreaId);
                //     entity['regionPermissions[' + index + '].productCategoryId'] = parseInt(productCategoryId);
                //     entity['regionPermissions[' + index + '].type'] = 2;
                //     entity['regionPermissions[' + index + '].groupType'] = parseInt($("#groupType").val());
                //     entity['regionPermissions[' + index + '].status'] = 1;
                //     index++
                // });
                // $(".tuji").each(function () {
                //     var areaId = $(this).data("areaid");// 获取区县id
                //     var subAreaId =  $(this).data("subareaid");// 获取街道id
                //     entity['regionPermissions[' + index + '].provinceId'] = parseInt($("#parentId").val());
                //     entity['regionPermissions[' + index + '].cityId'] = parseInt($("#subCityId").val());
                //     entity['regionPermissions[' + index + '].areaId'] = parseInt(areaId);
                //     entity['regionPermissions[' + index + '].subAreaId'] = parseInt(subAreaId);
                //     entity['regionPermissions[' + index + '].productCategoryId'] = parseInt(productCategoryId);
                //     entity['regionPermissions[' + index + '].type'] = 1;
                //     entity['regionPermissions[' + index + '].groupType'] = parseInt($("#groupType").val());
                //     entity['regionPermissions[' + index + '].status'] = 1;
                //     index++
                // });
                // $(".auto").each(function () {
                //     var areaId = $(this).data("areaid");// 获取区县id
                //     var subAreaId =  $(this).data("subareaid");// 获取街道id
                //     entity['regionPermissions[' + index + '].provinceId'] = parseInt($("#parentId").val());
                //     entity['regionPermissions[' + index + '].cityId'] = parseInt($("#subCityId").val());
                //     entity['regionPermissions[' + index + '].areaId'] = parseInt(areaId);
                //     entity['regionPermissions[' + index + '].subAreaId'] = parseInt(subAreaId);
                //     entity['regionPermissions[' + index + '].productCategoryId'] = parseInt(productCategoryId);
                //     entity['regionPermissions[' + index + '].type'] = 3;
                //     entity['regionPermissions[' + index + '].groupType'] = parseInt($("#groupType").val());
                //     entity['regionPermissions[' + index + '].status'] = 1;
                //     index++
                // });

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
                        var subAreaCheck = [];
                        if (result.data && result.data.length > 0) {
                            subAreaCheck = result.data;
                            for (var i in result.data) {
                                var entity = subAreaCheck[i];
                                if(entity.type == 1){
                                    $("[name=tuji][data-areaid=" + entity.areaId +"][data-subareaid=" +entity.subAreaId +"]").attr("checked",true);
                                }else if(entity.type == 2){
                                    $("[id=" +entity.areaId + "_2][data-areaid=" + entity.areaId +"]").attr("class","keFu");
                                    $("[id=" +entity.areaId + "_3][data-areaid=" + entity.areaId +"]").attr("class","unselected");
                                    $("[id=" +entity.areaId + "_3][data-areaid=" + entity.areaId +"]").attr("data-value","0");

                                }else if(entity.type == 3){
                                    $("[id=" +entity.areaId + "_2][data-areaid=" + entity.areaId +"]").attr("class","unselected");
                                    $("[id=" +entity.areaId + "_3][data-areaid=" + entity.areaId +"]").attr("class","auto");
                                    $("[id=" +entity.areaId + "_3][data-areaid=" + entity.areaId +"]").attr("data-value","1");
                                }
                            }
                        }
                        totalNum();
                    }
                }
            });

        }

        function totalNum() {
            var tujinum = $("input[type='checkbox'][name='tuji']:checked").length;
            var kefunum = [];
            var autonum = [];

            $(".auto").each(function(i,element){
                var index = $(this).data("areaid");
                autonum.push(index);
            });

            $(".keFu").each(function(i,element){
                var index = $(this).data("areaid");
                kefunum.push(index);
            });
            $("#tujiNum").html("" + tujinum +"");
            $("#kefuNum").html("" + new Set(kefunum).size +"");
            $("#autoNum").html("" + new Set(autonum).size +"");

        }
        function editSelectType(areaId,type) {
            if(type == 2){
                $("[id=" +areaId + "_2][data-areaid=" + areaId +"]").attr("class","keFu");
                $("[id=" +areaId + "_3][data-areaid=" + areaId +"]").attr("class","unselected");

                $("[id=" +areaId + "_3][data-areaid=" + areaId +"]").attr("data-value","0");
            }else if(type == 3){
                $("[id=" +areaId + "_2][data-areaid=" + areaId +"]").attr("class","unselected");
                $("[id=" +areaId + "_3][data-areaid=" + areaId +"]").attr("class","auto");

                $("[id=" +areaId + "_3][data-areaid=" + areaId +"]").attr("data-value","1");
            }
            totalNum();
        }

        function editCityType(type) {
            if(type == 2){
                $("[name=kefu]").attr("class","keFu");
                $("[name=auto]").attr("class","unselected");

                $("[name=auto]").attr("data-value",0);
            }else if(type == 3){
                $("[name=kefu]").attr("class","unselected");
                $("[name=auto]").attr("class","auto");

                $("[name=auto]").attr("data-value",1);
            }
            totalNum();
        }

    </script>
</head>
<body>

<form:form id="search_form" modelAttribute="regionPermission" action="" method="post" class="bread_crumb form-search">
    <div class="controls" style="margin-top: 20px;margin-left: 35px">
        <label class="control-label">产品品类:</label>
        <input id="productCategoryName" class="input-medium valid" style="width:200px;" readonly="readonly" type="text" value="" aria-invalid="false">
        <label style="margin-top: 5px" >突击街道：</label>
        <label style="margin-top: 5px;color: red;margin-left: 0px;" id="tujiNum">0</label>
        <label style="margin-top: 5px" >自动区/县：</label>
        <label style="margin-top: 5px;color: #0096DA;margin-left: 0px;" id="autoNum">0</label>
        <label style="margin-top: 5px">大客服区/县：</label>
        <label style="margin-top: 5px;color: #0096DA;margin-left: 0px;" id="kefuNum">0</label>
    </div>
</form:form>

<sys:message content="${message}"/>


<input type="hidden" id="type" value="${regionPermission.type}">
<%--突击or远程--%>
<input type="hidden" id="groupType" value="${regionPermission.groupType}">
<%--品类id--%>
<input type="hidden" id="productCategoryId" value="${regionPermission.productCategoryId}">
<%--市id--%>
<input type="hidden" id="subCityId" value="${regionPermission.cityId}">
<%--省id--%>
<input type="hidden" value="${area.parent.id}" id="parentId">
<div>
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover" style="margin-left: 40px;width: 93%">
        <thead>
            <tr>
                <th width="200px">市</th>
                <th width="200px">区县</th>
                <th width="200px">街道</th>
                <th>突击街道</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${areaModelList}" var="areaCounty" varStatus="i">
                    <c:forEach items="${areaCounty.subAreas}" var="subArea" varStatus="j">
                        <tr>
                            <td>${area.name}
                                <div>
                                    <a id="${area.id}_3" data-cityId="${area.id}" href="javascript:void(0);" onclick="editCityType(3)" class="unselected">自动</a>&nbsp;
                                    <a id="${area.id}_2" data-cityId="${area.id}" href="javascript:void(0);"  onclick="editCityType(2)" class="unselected">大客服</a>
                                </div>

                    </td>
                            <td>${areaCounty.name}
                                <div>
                                    <a id="${areaCounty.id}_3" data-cityId="${area.id}" name="auto" data-areaId="${areaCounty.id}" data-value="0" href="javascript:void(0);" onclick="editSelectType('${areaCounty.id}',3)" class="unselected">自动</a>&nbsp;
                                    <a id="${areaCounty.id}_2" data-cityId="${area.id}" name="kefu" data-areaId="${areaCounty.id}" href="javascript:void(0);" onclick="editSelectType('${areaCounty.id}',2)" class="keFu">大客服</a>
                                </div>
                               </td>

                            <td>${subArea.name}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${subArea.id == 0}">
                                        <input type="checkbox" id="${subArea.id}_1" name="tuji" disabled="true" data-cityId="${area.id}" data-areaId="${areaCounty.id}" style="zoom: 1.4" data-subAreaId="${subArea.id}"></input>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="checkbox" id="${subArea.id}_1" name="tuji" data-cityId="${area.id}" data-areaId="${areaCounty.id}" style="zoom: 1.4" data-subAreaId="${subArea.id}"></input>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </c:forEach>


            </tbody>
        </table>


</div>
<div style="float: left;height: 40px"></div>
<div id="editBtn">
    <input id="btnSubmit" class="btn btn-primary layui-btn" type="button"  value="保 存"
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
        $("input[name='tuji']").change(function () {
            totalNum();
        });
       load();
    });
    function mergeCell(columnIndex) {
        tr = $("#contentTable tr").length;// 获取当前表格中tr的个数
        var mark = 0; //要合并的单元格数
        var index = 0; //起始行数
        /*
         *  要合并单元格，需要存储两个参数，
         * 1，开始合并的单元格的第一行的行数，
         * 2.要合并的单元格的个数
        **/
        //console.log(tr);
        //判断 若只有一行数据，则不做调整
        if(tr <= 2){
        } else {
            //var i=1  比较当前的tr和上一个tr的值
            for(var i=0;i < tr ;i++){
                var ford = $("#contentTable tr:gt(0):eq("+i+") td:eq("+columnIndex+")").text();
                //根据下标获取单元格的值
                // tr:gt(0)  从下标0 开始获取
                // tr:gt(0):eq( i ) :i 标识 当前行的下标 ，0 开始
                // td:eq(0) 当前行的第一个单元格，下标从0开始
                var behind = $("#contentTable tr:gt(0):eq("+(parseInt(i)+1)+") td:eq("+columnIndex+")").text();
                if(ford == behind){
                    $("#contentTable tr:gt(0):eq("+(parseInt(i)+1)+") td:eq("+columnIndex+")").hide();
                    mark = mark +1;
                } else if(ford != behind){
                    //如果值不匹配则遍历到不同种的分类,将旧分类隐藏
                    index = i-mark;
                    if (mark >0) {
                        $("#contentTable tr:gt(0):eq(" + index + ") td:eq(" + columnIndex + ")").attr("rowspan", mark + 1);//+1 操作标识，将当前的行加入到隐藏
                        //rowspan 列上横跨， colspan 行上横跨
                        //后面的参数，表示横跨的单元格个数，
                        //合并单元格就是将其他的单元格隐藏（hide）,或删除（remove）。
                        //将一个单元格的rowspan 或colsspan 加大
                        mark = 0;
                        $("#contentTable tr:gt(0):eq(" + (parseInt(i)) + ") td:eq(" + columnIndex + ")").hide();
                    }
                }
            }
        }
    }

    mergeCell(0);
    mergeCell(1);
</script>
</body>
</html>
