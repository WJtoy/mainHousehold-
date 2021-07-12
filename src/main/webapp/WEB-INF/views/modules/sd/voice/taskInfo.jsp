<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>通话流水详情</title>
    <meta name="description" content="智能客服通话流水详情">
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/voice/talkInfo.css" type="text/css" rel="stylesheet"/>
    <link href="${ctxStatic}/jquery.audioplayer/audioplayer.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.audioplayer/audioplayer.js" type="text/javascript"></script>
    <script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        $(document).ready(function () {
            if($("#audioplayer").length > 0) {
                $('#audioplayer').audioPlayer(
                    {
                        classPrefix: 'audioplayer',
                        strPlay: 'Play',
                        strPause: 'Pause',
                        strVolume: 'Volume',
                        duration: ${task.talkTimes},
                    });
            }
        });

    </script>
    <style type="text/css">
        .form-horizontal{margin-top:5px}
        .form-horizontal .control-label{width:60px}
        .form-horizontal .controls{margin-left:65px}
        #contentTable td,#contentTable th{text-align:center;vertical-align:middle}
        .form-actions{margin-top:0;margin-bottom:0;padding:8px 20px 8px}
        legend {margin-bottom: 10px !important;}
    </style>
</head>
<body>
<sys:message content="${message}"/>
<form:form id="inputForm" modelAttribute="task" class="form-horizontal">
    <div class="row-fluid" style="height: 590px;">
        <div class="span4">
            <!-- user info -->
            <legend>
                <p style="margin-left: 10px;">用户信息</p>
            </legend>
            <div class="control-group">
                <label class="control-label">姓名:</label>
                <div class="controls">
                    <form:input path="name" htmlEscape="false" cssClass="input-block-level" readonly="true"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">电话:</label>
                <div class="controls">
                    <form:input path="phone" htmlEscape="false" cssClass="input-block-level" readonly="true"/>
                </div>
            </div>
            <br/>
            <!-- task info -->
            <legend>
                <p style="margin-left: 10px;">通话信息</p>
            </legend>
            <div class="control-group">
                <label class="control-label">任务名称:</label>
                <div class="controls">
                    <form:input path="projectCaption" htmlEscape="false" readonly="true" cssClass="input-block-level"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">呼出结果:</label>
                <div class="controls">
                    <form:input path="endReason" htmlEscape="false" readonly="true" cssClass="input-block-level"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">接通时间:</label>
                <div class="controls">
                    <form:input path="connectedAt" htmlEscape="false" readonly="true" cssClass="input-block-level"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">挂断时间:</label>
                <div class="controls">
                    <form:input path="disconnectedAt" htmlEscape="false" readonly="true" cssClass="input-block-level"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">通话时长:</label>
                <div class="controls">
                    <input id="talkTimes" name="talkTimes" class="input-block-level" readonly="readonly" type="text"
                           value="${task.talkTimes}秒" aria-invalid="false">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">AI分类:</label>
                <div class="controls">
                    <form:input path="status" htmlEscape="false" readonly="true" cssClass="input-block-level"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">分值:</label>
                <div class="controls">
                    <form:input path="score" htmlEscape="false" readonly="true" cssClass="input-block-level"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">标签:</label>
                <div class="controls">
                    <c:forEach var="item" items="${task.labels}">
                        <span class="label label-important">${item}</span>
                    </c:forEach>
                </div>
            </div>
        </div>
        <div class="span8 talk_window">
            <!-- 通话记录 -->
            <%--<legend>--%>
                <%--<p style="margin-left: 10px;">通话记录</p>--%>
            <%--</legend>--%>
            <div class="windows_top">
                <div class="windows_top_box">
                <c:choose>
                    <c:when test="${task == null || empty task.url}">
                        <span>无语音通话记录</span>
                    </c:when>
                    <c:otherwise>
                        <audio id="audioplayer" preload="auto"  controls>
                            <source src="${task.url}" type="audio/ogg">
                            <source src="${task.url}" type="audio/mpeg">
                        </audio>
                    </c:otherwise>
                </c:choose>
                </div>
            </div>
            <div class="windows_body">
                <div class="office_text">
                    <ul class="content" id="chatbox" style="top: -24.2px; position: absolute;">
                        <c:if test="${task !=null && task.talkInfo != null && task.talkInfo.size()>0}">
                            <c:forEach items="${task.talkInfo}" var="item">
                                <c:choose>
                                    <c:when test="${item.vocfile eq 'listen'}">
                                        <li class="user">
                                            <img src="${ctxStatic}/voice/user.png" title="客户">
                                            <span>${item.answer eq 'BasedPTheEnd'?"结束":item.answer}</span>
                                        </li>

                                    </c:when>
                                    <c:otherwise>
                                        <li class="robot">
                                            <img src="${ctxStatic}/voice/robot.jpeg" title="机器人">
                                            <span>${item.caption}</span>
                                        </li>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </c:if>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</form:form>
</body>
</html>