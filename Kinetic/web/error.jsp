<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error</title>
    <style>
        body { font-family: Segoe UI, sans-serif; margin: 2rem; background: #fff8f8; color: #5a1212; }
        .box { max-width: 700px; margin: 0 auto; border: 1px solid #e9a5a5; border-radius: 8px; padding: 1.2rem; background: #fff; }
        a { color: #9a2020; }
    </style>
</head>
<body>
    <div class="box">
        <h2>Something went wrong</h2>
        <p><%= request.getParameter("message") == null ? "An unexpected error occurred." : request.getParameter("message") %></p>
        <p><a href="index.html">Back to signup</a> | <a href="login">Try login</a></p>
    </div>
</body>
</html>
