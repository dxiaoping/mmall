<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>登陆界面</title>

    <style type="text/css">
        @import url("resources/nativestatic/css/testcss.css"); /*这里是通过@import引用CSS的样式内容*/
    </style>
    <%--<link rel="stylesheet" type="text/css" th:href="@{/resources/static/css/testcss.css}" />--%>
    <script src="https://cdn.staticfile.org/jquery/1.10.2/jquery.min.js">
    </script>
    <script type="text/javascript">

        $(document).ready(function () {
            $("#denglubutton").click(
                function () {
                    $("#errormessage").text("执行登陆");
                    $.post("login.json",
                        //发送给后端的数据
                        {
                            "name": $("#username").val(),
                            "password": $("#password").val()
                        },
                        //回调函数
                        function (data) {
                            var json = data[0];
                            if (json.success == 0) {
                                $("#errormessage").text("用户名或密码错误");
                            }
                            else if (json.success == 1) {
                                window.location.href = "index.html";
                            }
                        }
                    )
                });
        });
    </script>
</head>
<body>
<H1>tomcat1</H1>
<h2>Hello World!</h2>

<div class="name">
    <label>用户名:</label>
    <input type="text" name="username" id="username">
</div>
<div class="password">
    <label>密码:</label>
    <input type="password" name="password" id="password">
</div>
<div id="errormessage">45645</div>
<button type="button" id="denglubutton">登陆</button>

</body>
</html>
