<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title> 我的JSP网页</title>
</head>
<body>
<h2>Hello MAll!</h2>

springmvc 上传文件
<form name = "form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input name="upload_file" type="file"/>
    <input type="submit" value="springmvc上传文件"/>
</form>

富文本图片 上传文件
<form name = "form1" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input name="upload_file" type="file"/>
    <input type="submit" value="富文本上传文件"/>
</form>
</body>
</html>
