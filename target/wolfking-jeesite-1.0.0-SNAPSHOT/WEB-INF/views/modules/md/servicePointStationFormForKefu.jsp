<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点服务点管理</title>
    <meta name="decorator" content="default"/>
    <c:set var="currentuser" value="${fns:getUser() }" />
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <script type="text/javascript">
        var this_index = top.layer.index;
        var $btnSubmit = $("#btnSubmit");
        $(document).ready(function () {
            $("#selectAll").on("click",function(e){
                var checkedValue = $(this).attr("checked") || "";
                if (checkedValue == "") {
                    $(":checked[name='points']").removeAttr("checked");
                } else {
                    $(":checkbox[name='points']").attr("checked","checked");
                }
            });

            $(":checkbox[name='points']").on("click",function(e){
                if ($(":checkbox[name='points']").length != $(":checked[name='points']").length){
                    $("#selectAll").removeAttr("checked");
                } else {
                    $("#selectAll").attr("checked","checked");
                }
            });

            var clickTag = 0;
            $("#btnSubmit").click(function () {
                if(clickTag ==1){
                    return false;
                }

                if(!$("#inputForm").valid()){
                    return false;
                }

                var areas = [];
                areas = $(":checked[name='points']").map(function(){
                    return $(this).attr("value");
                })

                if (areas.length ==0 ){
                    layerError("至少要选择一个街道","错误提示");
                    clickTag = 0;
                    $btnSubmit.removeAttr('disabled');
                    return false;
                }
                clickTag = 1;
                $("#areaIds").val(areas.get().join(","));
                $btnSubmit.attr('disabled', 'disabled');
                var loadingIndex;
                var areaIds = $("#areaIds").val();
                var servicePointId = $("#id").val();
                var options = {
                    url: "${ctx}/md/servicepointstation/saveServicePointStationByAreaId",  //默认是form的action， 如果申明，则会覆盖
                    type: 'post',               //默认是form的method（get or post），如果申明，则会覆盖
                    dataType: 'json',           //html(默认), xml, script, json...接受服务端返回的类型
                    data: {areaIds:areaIds,servicePointId:servicePointId},
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
                            setTimeout(function () {
                                top.layer.close(this_index);
                            }, 2000);
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
                };
                $("#submitForm").ajaxSubmit(options);
            });
        });
    </script>
</head>
<body>
<sys:message content="${message}"/>
<form:form id="inputForm" modelAttribute="servicePoint" action="" method="post" class="form-horizontal">
    <form:hidden path="id"/>
    <form:hidden path="delFlag"/>
    <input id="areaIds" name="areaIds" type="hidden" value=""/>

    <%--<legend>网点信息</legend>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">网点编号:</label>
                <div class="controls">
                    <form:input path="servicePointNo" readonly="true" htmlEscape="false" class="input-small required"/>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group">
                <label class="control-label">网点名称:</label>
                <div class="controls">
                    <form:input path="name" readonly="true" htmlEscape="false" class="input-small required" />
                </div>
            </div>
        </div>
    </div>--%>
  <%--  <legend>服务点信息</legend>--%>
    <div class="control-group" style="margin-top: 50px">
        <label class="control-label">服务区域:</label>
        <div class="controls">
            <table id="contentTable" class="table table-bordered table-condensed table-hover" style="width: 90%">
                <thead>
                    <tr>
                        <th style="width:60px">市</th>
                        <th style="width:110px">区/县</th>
                        <th width="100">乡镇/街道</th>
                        <th width="60"><input id="selectAll" type="checkbox" value="0"/>服务街道</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td rowspan="${areaModel.subAreas.size()>0?areaModel.subAreas.size():''}">${areaModel.parent.name}</td>
                        <td rowspan="${areaModel.subAreas.size()>0?areaModel.subAreas.size():''}">${areaModel.name}</td>
                        <c:choose>
                            <c:when test="${areaModel.subAreas.size() >0}">
                                <c:forEach items="${areaModel.subAreas}" var="area" varStatus="i">
                                  <c:choose>
                                     <c:when test="${i.index ==0}">
                                        <td>
                                           <a href="javascript:" data-toggle="tooltip" data-tooltip="${area.name}" style="cursor: pointer;">${area.name}</a>
                                        </td>
                                        <td>
                                           <input name="points" type="checkbox" value="${area.id}" />
                                        </td>
                                     </c:when>
                                     <c:otherwise>
                                       <tr>
                                         <td>
                                           <a href="javascript:" data-toggle="tooltip" data-tooltip="${area.name}" style="cursor: pointer;">${area.name}</a>
                                         </td>
                                         <td>
                                           <input name="points" type="checkbox" value="${area.id}" />
                                         </td>
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
    <div class="form-actions">
        <input id="btnSubmit" class="btn btn-primary" type="button" value="保 存"/>&nbsp;
        <input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
    </div>
</form:form>
<form:form id="submitForm" ></form:form>
<script class="removedscript" type="text/javascript">
    function initCheckBoxNew(){
        var stationList = {};
        <c:if test="${not empty servicePointStationList && !(servicePointStationList eq null)}">
        stationList = ${servicePointStationList}
            </c:if>
            $(":checkbox[name='autoplan']").attr("disabled","disabled");
        if (stationList && stationList.length >0) {
            var n = 0;
            for(var i in stationList){
                var areaId = stationList[i].area.id;
                var query = ":checkbox[name='points'][value="+areaId+"]";
                $(query).attr("checked","checked");

            }
            var tr = $("#contentTable tbody tr").length;// 获取当前表格中tr的个数
            if (tr == stationList.length) {
                $("#selectAll").attr("checked","checked");
            }
        }
    }
    initCheckBoxNew();
</script>
</body>
</html>
